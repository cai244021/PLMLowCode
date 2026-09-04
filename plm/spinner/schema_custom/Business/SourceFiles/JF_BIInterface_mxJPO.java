import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.PostProcessCallable;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.RelationshipType;
import matrix.db.FileList;
import matrix.db.Page;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import static com.matrixone.apps.domain.DomainConstants.*;

/**
 * @ClassName JF_BIInterface_mxJPO
 * @Author: caipan
 * @CreateDate: 2025/2/8 10:48
 * @UpdateRemark:
 * @Version: 1.0
 * @Description: BI和AI接口相关的代码
 */
public class JF_BIInterface_mxJPO implements JF_PLMConstants_mxJPO{
    private static final Logger logger = LoggerFactory.getLogger(JF_BIInterface_mxJPO.class);
    private static final Logger ThreadLog = LoggerFactory.getLogger("MY_CUSTOM_LOGGER");

    private static final StringList busSelectsList = new StringList();
    private static final StringList relSelectsList = new StringList();
    private static final String SUITE_KEY = "emxComponentsStringResource";
    private static final String RELATIONSHIP_JFCO2ECOTASK = "JFCO2ECOTask";
    private static final String TYPE_JF_ECOTASK = "JF_ECOTask";
    private static final String RELATIONSHIP_JFDA2JFDATASK = "JFDA2JFDATask";
    private static final String TYPE_JFDA = "JFDA";
    private static final String TYPE_JF_DATASK = "JF_DATask";
    private static final String TYPE_VPMREFERENCE = "VPMReference";
    private static final String TYPE_WORKSPACE_VAULT = "Workspace Vault";
    private static final String RELATIONSHIP_DATA_VAULTS = "Data Vaults";
    private static final String RELATIONSHIP_SUB_VAULTS = "Sub Vaults";
    private static final String RELATIONSHIP_VAULTED_OBJECTS = "Vaulted Objects";
    private static final String RELATIONSHIP_LATEST_VERSION = "Latest Version";
    private static final String PROJECT_DOCUMENT_FOLDER_KEY = "project.folder";
    private static final String PROJECT_DOCUMENT_TRANSFER_PATH_KEY = "project.dataOutSource.share.path";
    private static final String DOC_JSON_FILE_NAME = "doc.json";
    private static final String BI_JSON_FILE_DIR = "/data/cp/BI/";
    private static final String BI_INTERFACE_DA_INFO = "DAInfo";
    private static final String BI_INTERFACE_PARTLIST_INFO = "PartListInfo";
    private static final String BI_INTERFACE_ECR_INFO = "ECRInfo";
    private static final String BI_INTERFACE_INIT_ECR_INFO = "InitECRInfo";
    private static final String BI_FILE_TIME_PATTERN = "yyyyMMddHHmmss";
    private static final int BI_PROJECT_CHUNK_SIZE = 20;
    private static final String BI_SFTP_HOST_KEY = "BI.SFTP.Host";
    private static final String BI_SFTP_PORT_KEY = "BI.SFTP.Port";
    private static final String BI_SFTP_USER_KEY = "BI.SFTP.User";
    private static final String BI_SFTP_PASSWORD_KEY = "BI.SFTP.Password";
    private static final String BI_SFTP_REMOTE_DIR_KEY = "BI.SFTP.RemoteDir";
    private static final String BI_SFTP_DA_REMOTE_DIR_KEY = "BI.SFTP.DAInfo.RemoteDir";
    private static final String BI_SFTP_PARTLIST_REMOTE_DIR_KEY = "BI.SFTP.PartListInfo.RemoteDir";
    private static final String SELECT_ATTR_JFECRCHANGETYPE = "attribute[JFECRChangeType]";
    private static final String SELECT_ATTR_TASK_TITLE = "attribute[Title]";
    private static final String SELECT_ATTR_PROJECT_ROLE = "attribute[Project Role]";
    private static final String SELECT_ATTR_JF_DAACTUALFINISHTIME = "attribute[JF_DAActualFinishTime]";
    private static final String SELECT_ATTR_JFCHANGETYPE = "attribute[JFChangeType]";
    private static final String SELECT_ATTR_JFAFFECTSFACTORY = "attribute[" + ATTR_JFAFFECTEDFACTORY + "]";
    private static final String SELECT_ATTR_JFREASONDEVIATION = "attribute[JFReasonDeviation]";
    private static final String SELECT_ATTR_JFBEFORECHANGE = "attribute[JFBeforeChange]";
    private static final String SELECT_ATTR_JFAFTERCHANGE = "attribute[JFAfterChange]";
    private static final String SELECT_ATTR_JFDASTARTTIME = "attribute[JFDAStartTime]";
    private static final String SELECT_ATTR_JFDACLOSETIME = "attribute[JFDACloseTime]";
    private static final String SELECT_ATTR_JFDAISDELAY = "attribute[JFDAIsDelay]";
    private static final String SELECT_ATTR_JFDAEXTENSIONTIME = "attribute[JFDAExtensionTime]";
    private static final String SELECT_ATTR_JF_COSTCHANGES = "attribute[JF_CostChanges]";
    private static final String SELECT_ATTR_JF_CONCLUSION = "attribute[JF_Conclusion]";
    private static final String SELECT_ATTR_JF_RISKDESCRIPTION = "attribute[JF_RiskDescription]";
    private static final String SELECT_ATTR_JF_RECOMMENDEDMEASURES = "attribute[JF_RecommendedMeasures]";
    private static final String SELECT_ATTR_JF_OPERATOR = "attribute[JF_Operator]";
    private static final String SELECT_ATTR_JF_OPERATIONDATE = "attribute[JF_OperationDate]";
    private static final String SELECT_ATTR_JF_PCRFUNCTION = "attribute[JF_PCRFunction]";
    private static final String SELECT_ATTR_JF_PCRACTUALFINISHTIME = "attribute[JF_PCRActualFinishTime]";
    private static final String SELECT_ATTR_JF_REMARK = "attribute[JF_Remark]";
    private static final String SELECT_ASSIGNED_TASK_OWNER = "to[Assigned Tasks].from[Person].name";
    private static final String SELECT_ECR_CHANGE_EVENT_TYPE = "to[" + REL_JFChangeEventECR + "].from.name";
    private static final String SELECT_PARTLIST_SNAPSHOT_TITLE = "from[" + rel_JFPartList2Snapshot + "].to.attribute[Title]";
    private static final String SELECT_PARTLIST_SNAPSHOT_SUBTYPE = "from[" + rel_JFPartList2Snapshot + "].to.attribute[JFSnapshotSubType]";
    private static final String SELECT_STATE_RELEASED_ACTUAL = "state[Released].actual";
    private static final String SELECT_REL_ORIGINATED = "originated";
    private static final String SELECT_ATTR_PLMENTITY_V_NAME = "attribute[PLMEntity.V_Name]";
    private static final String SELECT_ATTR_JFROOTPARTNUMBER = "attribute[JFRootPartNumber]";
    private static final String SELECT_ATTR_JFROOTPARTDESCRIPTION = "attribute[JFRootPartDescription]";
    private static final String SELECT_ATTR_JSREASON = "attribute[JSReason]";
    private static final String SELECT_ATTR_JFPROPORTIONCONFIGURATION = "attribute[JFProportionConfiguration]";
    private static final String SELECT_ATTR_JFPARTSUPPLIERINFO = "attribute[JFPartSupplierInfo]";
    private static final String SELECT_ATTR_JFSUPPLIERSOURINGDATE = "attribute[JFSupplierSouringDate]";
    private static final String SELECT_ATTR_JFSOURINGRESPNAME = "attribute[JFSouringRespName]";
    private static final String SELECT_ATTR_JFSOURINGSUPCODE = "attribute[JFSouringSupCode]";
    private static final String SELECT_ATTR_JFSOURINGSUPNAME = "attribute[JFSouringSupName]";
    private static final String SELECT_ECR_PROJECT_ID = "from[" + REL_JFChange2Project + "].to.id";
    private static final String SELECT_ECR_PROJECT_NAME = "from[" + REL_JFChange2Project + "].to.name";
    private static final String SELECT_ECR_PROJECT_DESCRIPTION = "from[" + REL_JFChange2Project + "].to.description";
    private static final String SELECT_TASK_REVIEW_ACTUAL = "state[Review].actual";
    private static final String SELECT_TASK_COMPLETE_ACTUAL = "state[Complete].actual";
    private static final String SELECT_MODIFIED_VALUE = "modified";
    private static final String SELECT_LEVEL_VALUE = "level";
    private static final String SELECT_CONNECTION_ID_VALUE = "id[connection]";
    private static final String SELECT_ATTR_JFCUSTOMERCHANGENUM = "attribute[JFCustomerChangeNum]";
    private static final String SELECT_ATTR_JFEXPECTEDLAUNCHTIME = "attribute[JFExpectedLaunchTime]";
    private static final String SELECT_ATTR_JFISLASTQUOTE = "attribute[" + ATTR_JFIsLastQuote + "]";
    private static final String SELECT_ATTR_JFISTKODATA = "attribute[" + ATTR_JFIsTKOData + "]";
    private static final String SELECT_ECR_CONNECT_ECO_NAME = "from[" + REL_JFECR2CO + "].to.name";
    private static final String SELECT_ATTR_JFECRTYPE = "attribute[" + ATTR_JFECRTYPE + "]";
    private static final String SELECT_ATTR_JFBREAKPOINTMODE = "attribute[" + ATTR_JFBREAKPOINTMODE + "]";
    private static final String SELECT_ATTR_JFISBREAKPOINTINVALID = "attribute[JF_IsBreakpointInvalid]";
    private static final String SELECT_ATTR_JFAFFECTEDPROJECT = "attribute[JFAffectedProject]";

    static {
        busSelectsList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        busSelectsList.add(DomainConstants.SELECT_DESCRIPTION);
        busSelectsList.add(DomainConstants.SELECT_REVISION);
        busSelectsList.add(DomainConstants.SELECT_NAME);
        busSelectsList.add(DomainConstants.SELECT_ID);
        busSelectsList.add(DomainConstants.SELECT_TYPE);
        busSelectsList.add(DomainConstants.SELECT_CURRENT);
        busSelectsList.add(DomainConstants.SELECT_OWNER);
        busSelectsList.add(DomainConstants.SELECT_PROJECT);
        busSelectsList.add(DomainConstants.SELECT_ORGANIZATION);
        busSelectsList.add(DomainConstants.SELECT_ORIGINATED);
        busSelectsList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        relSelectsList.add(DomainRelationship.SELECT_ID);
    }

    /**
     * 提供给BI的接口，查询ECO的基本信息+ECO全部的执行计划
     **
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/12 10:39
     */
    public JSONObject getProjectConnectECOInfo(Context context, String[]args)throws Exception{
        JSONObject returnJosn=new JSONObject();
        JSONArray dataarray=new JSONArray();
        boolean pushed = false;
        try {
            ContextUtil.pushContext(context);
            pushed = true;
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            logger.info("getProjectConnectECOInfo begin, params:{}", programMap);
            MapList projectList = getProjectConnectBIProjectList(context, programMap);
            for (int i = 0; i < projectList.size(); i++) {
                Map projectMap = (Map) projectList.get(i);
                dataarray.addAll(getProjectECODataArray(context, projectMap));
            }

            returnJosn.put("dataArray",dataarray);
            returnJosn.put("code","200");
            returnJosn.put("msg","");
            returnJosn.put("result","success");

        }catch (Exception e){
            e.printStackTrace();
            returnJosn.put("code","500");
            returnJosn.put("msg",e.getMessage());
            returnJosn.put("result","failed");
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
            logger.info("getProjectConnectECOInfo end");
        }
        return returnJosn;
    }

    /**
     * 提供给BI的接口，查询DA的基本信息+DA全部的执行计划+零件清单
     **
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/7/31 11:45
     */
    public JSONObject getProjectConnectDAInfo(Context context, String[]args)throws Exception{
        JSONObject returnJosn=new JSONObject();
        boolean pushed = false;
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            logger.info("getProjectConnectDAInfo begin, params:{}", programMap);
            ContextUtil.pushContext(context);
            pushed = true;
            returnJosn = buildProjectConnectDAInfoJson(context, programMap);
        }catch (Exception e){
            e.printStackTrace();
            returnJosn.put("code","500");
            returnJosn.put("msg",e.getMessage());
            returnJosn.put("result","failed");
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
            logger.info("getProjectConnectDAInfo end");
        }
        return returnJosn;
    }

    /**
     * 提供给BI的接口，查询PCR的基本信息+PCR全部的执行计划
     **
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/12 16:00
     */
    public JSONObject getProjectConnectPCRInfo(Context context, String[]args)throws Exception{
        JSONObject returnJosn=new JSONObject();
        JSONArray dataarray=new JSONArray();
        boolean pushed = false;
        try {
            ContextUtil.pushContext(context);
            pushed = true;
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            logger.info("getProjectConnectPCRInfo begin, params:{}", programMap);
            MapList projectList = getProjectConnectBIProjectList(context, programMap);
            for (int i = 0; i < projectList.size(); i++) {
                Map projectMap = (Map) projectList.get(i);
                dataarray.addAll(getProjectPCRDataArray(context, projectMap));
            }

            returnJosn.put("dataArray",dataarray);
            returnJosn.put("code","200");
            returnJosn.put("msg","");
            returnJosn.put("result","success");

        }catch (Exception e){
            e.printStackTrace();
            returnJosn.put("code","500");
            returnJosn.put("msg",e.getMessage());
            returnJosn.put("result","failed");
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
            logger.info("getProjectConnectPCRInfo end");
        }
        return returnJosn;
    }

    /**
     * 提供给BI的接口，查询PartList的基本信息+零件信息+图纸信息
     **
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/7/31 11:45
     */
    public JSONObject getProjectConnectPartListInfo(Context context, String[]args)throws Exception{
        JSONObject returnJosn=new JSONObject();
        boolean pushed = false;
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            logger.info("getProjectConnectPartListInfo begin, params:{}", programMap);
            ContextUtil.pushContext(context);
            pushed = true;
            returnJosn = buildProjectConnectPartListInfoJson(context, programMap);
        }catch (Exception e){
            e.printStackTrace();
            returnJosn.put("code","500");
            returnJosn.put("msg",e.getMessage());
            returnJosn.put("result","failed");
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
            logger.info("getProjectConnectPartListInfo end");
        }
        return returnJosn;
    }

    /**
     * 获取BI项目关联接口需要处理的项目，ProjectIds为空时查询全部项目。
     **
     * @param context
     * @param programMap 请求参数
     * @return MapList 项目列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 16:13
     */
    private MapList getProjectConnectBIProjectList(Context context, Map programMap) throws Exception {
        StringList projectSelects = new StringList();
        projectSelects.add(DomainConstants.SELECT_ID);
        projectSelects.add(DomainConstants.SELECT_NAME);
        projectSelects.add(DomainConstants.SELECT_DESCRIPTION);

        List projectNames = getECRBIStringListParam(programMap, "ProjectIds");
        if (projectNames.isEmpty()) {
            return DomainObject.findObjects(context, DomainConstants.TYPE_PROJECT_SPACE,
                    DomainConstants.QUERY_WILDCARD,
                    "",
                    projectSelects);
        }

        MapList projectList = new MapList();
        for (int i = 0; i < projectNames.size(); i++) {
            String projectName = String.valueOf(projectNames.get(i));
            MapList oneProjectList = DomainObject.findObjects(context, DomainConstants.TYPE_PROJECT_SPACE,
                    DomainConstants.QUERY_WILDCARD,
                    "name == '" + escapeWhereValue(projectName) + "'",
                    projectSelects);
            if (oneProjectList == null || oneProjectList.isEmpty()) {
                continue;
            }
            projectList.addAll(oneProjectList);
        }
        return projectList;
    }

    /**
     * 提供给BI的接口，按ECR状态到达时间查询正式ECR基本信息+会签任务+受影响零件+BOM。
     **
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/7/31 11:45
     */
    public JSONObject getFormalECRBIInfo(Context context, String[] args) throws Exception {
        JSONObject returnJosn = new JSONObject();
        boolean pushed = false;
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            logger.info("getFormalECRBIInfo begin, params:{}", programMap);

            String state = getMapValue(programMap, "State");
            String startTime = getMapValue(programMap, "StartTime");
            String endTime = getMapValue(programMap, "EndTime");
            validateFormalECRBIParams(state, startTime, endTime);
            ContextUtil.pushContext(context);
            pushed = true;
            returnJosn = buildFormalECRBIInfoJson(context, programMap);
        } catch (Exception e) {
            e.printStackTrace();
            returnJosn.put("code", "500");
            returnJosn.put("msg", e.getMessage());
            returnJosn.put("result", "failed");
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
            logger.info("getFormalECRBIInfo end");
        }
        return returnJosn;
    }

    /**
     * 提供给BI的接口，按项目号和当前状态初始化ECR基本信息+会签任务+受影响零件+BOM。
     **
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/7/31 11:45
     */
    public JSONObject initECRBIInfo(Context context, String[] args) throws Exception {
        JSONObject returnJosn = new JSONObject();
        boolean pushed = false;
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            logger.info("initECRBIInfo begin, params:{}", programMap);
            validateInitECRBIParams(programMap);
            ContextUtil.pushContext(context);
            pushed = true;
            returnJosn = buildInitECRBIInfoJson(context, programMap);
        } catch (Exception e) {
            e.printStackTrace();
            returnJosn.put("code", "500");
            returnJosn.put("msg", e.getMessage());
            returnJosn.put("result", "failed");
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
            logger.info("initECRBIInfo end");
        }
        return returnJosn;
    }

    /**
     * 异步生成BI接口JSON文件。
     **
     * @param context
     * @param args 请求参数，包含interfaceType、fileName、programMap
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 17:30
     */
    public void generateBIInterfaceJsonFile(Context context, String[] args) throws Exception {
        JSONObject fileJson;
        String interfaceType = "";
        String fileName = "";
        boolean pushed = false;
        try {
            Map asyncMap = getBIInterfaceAsyncParamMap(args);
            interfaceType = getMapValue(asyncMap, "interfaceType");
            fileName = getMapValue(asyncMap, "fileName");
            Map programMap = (Map) asyncMap.get("programMap");
            MapList projectList = (MapList) asyncMap.get("projectList");
            logger.info("generateBIInterfaceJsonFile begin, interfaceType:{}, fileName:{}, params:{}",
                    interfaceType, fileName, programMap);

            ContextUtil.pushContext(context);
            pushed = true;
            fileJson = buildBIInterfaceFileJson(context, interfaceType, programMap, projectList);
        } catch (Exception e) {
            e.printStackTrace();
            fileJson = buildBIInterfaceFailedJson(e.getMessage());
        } finally {
            if (pushed) {
                try {
                    ContextUtil.popContext(context);
                } catch (Exception e) {
                    logger.info("generateBIInterfaceJsonFile pop context failed, fileName:{}", fileName, e);
                }
            }
        }

        try {
            writeBIInterfaceJsonFile(context, interfaceType, fileName, fileJson);
        } catch (Exception e) {
            logger.info("generateBIInterfaceJsonFile write or upload file failed, fileName:{}", fileName, e);
        }
        logger.info("generateBIInterfaceJsonFile end, fileName:{}", fileName);
    }

    /**
     * 提交BI接口异步文件生成任务并立即返回文件名。
     **
     * @param context
     * @param programMap 请求参数
     * @param interfaceType 接口类型
     * @return JSONObject 接口即时返回信息
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 17:30
     */
    private JSONObject submitBIInterfaceAsyncTask(Context context, Map programMap, String interfaceType) throws Exception {
        String fileName = buildBIInterfaceFileName(interfaceType);
        String programMapJson = JSONObject.toJSONString(programMap == null ? new HashMap() : programMap);
        JF_Util_mxJPO.runAsync(context,
                new String[]{interfaceType, fileName, programMapJson},
                "JF_BIInterface",
                "generateBIInterfaceJsonFile");

        JSONObject returnJson = new JSONObject();
        returnJson.put("fileName", fileName);
        returnJson.put("msg", "");
        returnJson.put("result", "success");
        returnJson.put("code", "200");
        return returnJson;
    }

    /**
     * 提交按项目分片的BI接口异步文件生成任务并立即返回文件名数组。
     **
     * @param context
     * @param programMap 请求参数
     * @param interfaceType 接口类型
     * @return JSONObject 接口即时返回信息
     * @throws Exception
     * @author caipan
     * @date 2026/7/20 14:35
     */
    private JSONObject submitBIInterfaceProjectChunkAsyncTask(Context context, Map programMap, String interfaceType) throws Exception {
        MapList projectList;
        boolean pushed = false;
        try {
            ContextUtil.pushContext(context);
            pushed = true;
            projectList = getProjectConnectBIProjectList(context, programMap);
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
        }

        JSONArray fileNameArray = new JSONArray();
        String baseFileName = buildBIInterfaceFileName(interfaceType);
        String programMapJson = JSONObject.toJSONString(programMap == null ? new HashMap() : programMap);
        int projectCount = projectList == null ? 0 : projectList.size();
        int chunkCount = projectCount == 0 ? 0 : (projectCount + BI_PROJECT_CHUNK_SIZE - 1) / BI_PROJECT_CHUNK_SIZE;
        for (int i = 0; i < chunkCount; i++) {
            int start = i * BI_PROJECT_CHUNK_SIZE;
            int end = Math.min(start + BI_PROJECT_CHUNK_SIZE, projectCount);
            String fileName = buildBIInterfaceChunkFileName(baseFileName, i, chunkCount);
            fileNameArray.add(fileName);
            JF_Util_mxJPO.runAsync(context,
                    new String[]{interfaceType, fileName, programMapJson, buildProjectChunkJson(projectList, start, end)},
                    "JF_BIInterface",
                    "generateBIInterfaceJsonFile");
        }

        JSONObject returnJson = new JSONObject();
        returnJson.put("fileName", fileNameArray);
        returnJson.put("msg", "");
        returnJson.put("result", "success");
        returnJson.put("code", "200");
        return returnJson;
    }

    /**
     * 生成BI接口分片文件名，只有一个分片时保持原文件名。
     **
     * @param baseFileName 基础文件名
     * @param chunkIndex 分片下标
     * @param chunkCount 分片总数
     * @return String 分片文件名
     * @throws Exception
     * @author caipan
     * @date 2026/7/20 14:35
     */
    private String buildBIInterfaceChunkFileName(String baseFileName, int chunkIndex, int chunkCount) {
        if (chunkCount <= 1) {
            return baseFileName;
        }
        int dotIndex = baseFileName.lastIndexOf(".");
        if (dotIndex < 0) {
            return baseFileName + "_" + (chunkIndex + 1);
        }
        return baseFileName.substring(0, dotIndex) + "_" + (chunkIndex + 1) + baseFileName.substring(dotIndex);
    }

    /**
     * 构建项目分片JSON字符串。
     **
     * @param projectList 项目列表
     * @param start 起始下标
     * @param end 结束下标
     * @return String 项目分片JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/20 14:35
     */
    private String buildProjectChunkJson(MapList projectList, int start, int end) {
        JSONArray projectArray = new JSONArray();
        for (int i = start; i < end; i++) {
            projectArray.add(projectList.get(i));
        }
        return projectArray.toJSONString();
    }

    /**
     * 生成BI接口异步文件名。
     **
     * @param interfaceType 接口类型
     * @return String 文件名
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 17:30
     */
    private String buildBIInterfaceFileName(String interfaceType) {
        return interfaceType + LocalDateTime.now().format(DateTimeFormatter.ofPattern(BI_FILE_TIME_PATTERN)) + ".json";
    }

    /**
     * 获取BI接口异步任务参数，优先兼容普通数组参数。
     **
     * @param args 异步任务参数
     * @return Map 参数Map
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 18:10
     */
    private Map getBIInterfaceAsyncParamMap(String[] args) throws Exception {
        Map asyncMap = new HashMap();
        if (args != null && args.length >= 3) {
            asyncMap.put("interfaceType", args[0]);
            asyncMap.put("fileName", args[1]);
            JSONObject programMapJson = JSONObject.parseObject(args[2]);
            asyncMap.put("programMap", programMapJson == null ? new HashMap() : programMapJson);
            if (args.length >= 4) {
                JSONArray projectArray = JSONObject.parseArray(args[3]);
                MapList projectList = new MapList();
                if (projectArray != null) {
                    for (int i = 0; i < projectArray.size(); i++) {
                        Object projectObj = projectArray.get(i);
                        if (projectObj instanceof Map) {
                            projectList.add(projectObj);
                        }
                    }
                }
                asyncMap.put("projectList", projectList);
            }
            return asyncMap;
        }

        Map packedMap = (Map) JPO.unpackArgs(args);
        if (packedMap == null) {
            throw new Exception("BI接口异步任务参数为空");
        }
        Object programMap = packedMap.get("programMap");
        if (programMap == null) {
            packedMap.put("programMap", new HashMap());
        }
        return packedMap;
    }

    /**
     * 构建BI接口文件JSON内容。
     **
     * @param context
     * @param interfaceType 接口类型
     * @param programMap 请求参数
     * @param projectList 指定项目列表，为空时按请求参数查询
     * @return JSONObject 文件内容
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 17:30
     */
    private JSONObject buildBIInterfaceFileJson(Context context, String interfaceType, Map programMap, MapList projectList) throws Exception {
        if (BI_INTERFACE_DA_INFO.equals(interfaceType)) {
            return projectList == null ? buildProjectConnectDAInfoJson(context, programMap) : buildProjectConnectDAInfoJson(context, projectList);
        } else if (BI_INTERFACE_PARTLIST_INFO.equals(interfaceType)) {
            return projectList == null ? buildProjectConnectPartListInfoJson(context, programMap) : buildProjectConnectPartListInfoJson(context, projectList);
        } else if (BI_INTERFACE_ECR_INFO.equals(interfaceType)) {
            return buildFormalECRBIInfoJson(context, programMap);
        } else if (BI_INTERFACE_INIT_ECR_INFO.equals(interfaceType)) {
            return buildInitECRBIInfoJson(context, programMap);
        }
        throw new Exception("不支持的BI接口类型:" + interfaceType);
    }

    /**
     * 构建DA接口原始返回JSON。
     **
     * @param context
     * @param programMap 请求参数
     * @return JSONObject DA接口文件内容
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 17:30
     */
    private JSONObject buildProjectConnectDAInfoJson(Context context, Map programMap) throws Exception {
        MapList projectList = getProjectConnectBIProjectList(context, programMap);
        return buildProjectConnectDAInfoJson(context, projectList);
    }

    /**
     * 按指定项目列表构建DA接口原始返回JSON。
     **
     * @param context
     * @param projectList 项目列表
     * @return JSONObject DA接口文件内容
     * @throws Exception
     * @author caipan
     * @date 2026/7/20 14:35
     */
    private JSONObject buildProjectConnectDAInfoJson(Context context, MapList projectList) throws Exception {
        JSONArray dataarray = new JSONArray();
        for (int i = 0; i < projectList.size(); i++) {
            Map projectMap = (Map) projectList.get(i);
            dataarray.addAll(getProjectDADataArray(context, projectMap));
        }
        return buildBIInterfaceSuccessJson(dataarray);
    }

    /**
     * 构建PartList接口原始返回JSON。
     **
     * @param context
     * @param programMap 请求参数
     * @return JSONObject PartList接口文件内容
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 17:30
     */
    private JSONObject buildProjectConnectPartListInfoJson(Context context, Map programMap) throws Exception {
        MapList projectList = getProjectConnectBIProjectList(context, programMap);
        return buildProjectConnectPartListInfoJson(context, projectList);
    }

    /**
     * 按指定项目列表构建PartList接口原始返回JSON。
     **
     * @param context
     * @param projectList 项目列表
     * @return JSONObject PartList接口文件内容
     * @throws Exception
     * @author caipan
     * @date 2026/7/20 15:05
     */
    private JSONObject buildProjectConnectPartListInfoJson(Context context, MapList projectList) throws Exception {
        JSONArray dataarray = new JSONArray();
        for (int i = 0; i < projectList.size(); i++) {
            Map projectMap = (Map) projectList.get(i);
            dataarray.addAll(getProjectPartListDataArray(context, projectMap));
        }
        return buildBIInterfaceSuccessJson(dataarray);
    }

    /**
     * 构建正式ECR接口原始返回JSON。
     **
     * @param context
     * @param programMap 请求参数
     * @return JSONObject 正式ECR接口文件内容
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 17:30
     */
    private JSONObject buildFormalECRBIInfoJson(Context context, Map programMap) throws Exception {
        JSONArray dataarray = new JSONArray();
        String state = getMapValue(programMap, "State");
        String startTime = getMapValue(programMap, "StartTime");
        String endTime = getMapValue(programMap, "EndTime");
        validateFormalECRBIParams(state, startTime, endTime);
        startTime = normalizeFormalECRBIStartTime(startTime);
        endTime = normalizeFormalECRBIEndTime(endTime);

        MapList formalECRList = DomainObject.findObjects(context,
                TYPE_JFECR + "," + TYPE_JFNewECR + "," + TYPE_JFFormalECR,
                DomainConstants.QUERY_WILDCARD,
                buildFormalECRBIWhere(state, startTime, endTime),
                getFormalECRBISelectList(state));
        if (formalECRList != null && !formalECRList.isEmpty()) {
            for (int i = 0; i < formalECRList.size(); i++) {
                Map ecrMap = (Map) formalECRList.get(i);
                dataarray.add(buildFormalECRBIJson(context, ecrMap));
            }
        }
        return buildBIInterfaceSuccessJson(dataarray);
    }

    /**
     * 构建ECR初始化接口原始返回JSON。
     **
     * @param context
     * @param programMap 请求参数
     * @return JSONObject ECR初始化接口文件内容
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 17:30
     */
    private JSONObject buildInitECRBIInfoJson(Context context, Map programMap) throws Exception {
        JSONArray dataarray = new JSONArray();
        List projectIds = getECRBIStringListParam(programMap, "ProjectIds");
        List states = getECRBIStringListParam(programMap, "States");
        validateInitECRBIParams(programMap);
        MapList ecrList = DomainObject.findObjects(context,
                TYPE_JFECR + "," + TYPE_JFNewECR + "," + TYPE_JFFormalECR,
                DomainConstants.QUERY_WILDCARD,
                buildInitECRBIWhere(projectIds, states),
                getInitECRBISelectList(states));
        if (ecrList != null && !ecrList.isEmpty()) {
            for (int i = 0; i < ecrList.size(); i++) {
                Map ecrMap = (Map) ecrList.get(i);
                dataarray.add(buildFormalECRBIJson(context, ecrMap));
            }
        }
        return buildBIInterfaceSuccessJson(dataarray);
    }

    /**
     * 构建BI接口成功JSON。
     **
     * @param dataarray 数据数组
     * @return JSONObject 成功返回结构
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 17:30
     */
    private JSONObject buildBIInterfaceSuccessJson(JSONArray dataarray) {
        JSONObject returnJson = new JSONObject();
        returnJson.put("dataArray", dataarray);
        returnJson.put("code", "200");
        returnJson.put("msg", "");
        returnJson.put("result", "success");
        return returnJson;
    }

    /**
     * 构建BI接口失败JSON。
     **
     * @param message 错误消息
     * @return JSONObject 失败返回结构
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 17:30
     */
    private JSONObject buildBIInterfaceFailedJson(String message) {
        JSONObject returnJson = new JSONObject();
        returnJson.put("dataArray", new JSONArray());
        returnJson.put("code", "500");
        returnJson.put("msg", message);
        returnJson.put("result", "failed");
        return returnJson;
    }

    /**
     * 将BI接口JSON写入文件。
     **
     * @param context
     * @param interfaceType 接口类型
     * @param fileName 文件名
     * @param fileJson 文件内容
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 17:30
     */
    private void writeBIInterfaceJsonFile(Context context, String interfaceType, String fileName, JSONObject fileJson) throws Exception {
        File rootDir = new File(BI_JSON_FILE_DIR);
        if (!rootDir.exists() && !rootDir.mkdirs()) {
            throw new Exception("创建BI接口文件目录失败:" + rootDir.getAbsolutePath());
        }
        File jsonFile = new File(rootDir, fileName);
        File tempFile = new File(rootDir, fileName + ".tmp");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            writer.write(fileJson.toJSONString());
        }
        if (jsonFile.exists() && !jsonFile.delete()) {
            throw new Exception("删除旧BI接口文件失败:" + jsonFile.getAbsolutePath());
        }
        if (!tempFile.renameTo(jsonFile)) {
            throw new Exception("重命名BI接口临时文件失败:" + tempFile.getAbsolutePath());
        }
        uploadBIInterfaceJsonFileToSftp(context, interfaceType, jsonFile);
    }

    /**
     * 获取PartList相关信息
     **
     * @param context
     * @param projectMap
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/12 17:31
     */
    private JSONArray getProjectPartListDataArray(Context context, Map projectMap) throws Exception {
        JSONArray dataarray = new JSONArray();
        String projectId = getMapValue(projectMap, DomainConstants.SELECT_ID);
        if (UIUtil.isNullOrEmpty(projectId)) {
            return dataarray;
        }

        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        MapList partListList = projectObj.getRelatedObjects(context,
                rel_JFProject2PartList,
                TYPE_JFPartList,
                getPartListSelectList(),
                relSelectsList,
                false,
                true,
                (short) 1,
                "",
                "",
                0);

        if (partListList == null || partListList.isEmpty()) {
            return dataarray;
        }

        for (int i = 0; i < partListList.size(); i++) {
            Map partListMap = (Map) partListList.get(i);
            dataarray.add(buildPartListJson(context, projectMap, partListMap));
        }
        return dataarray;
    }

    private JSONObject buildPartListJson(Context context, Map projectMap, Map partListMap) throws Exception {
        JSONObject partListJson = new JSONObject();
        String partListId = getMapValue(partListMap, DomainConstants.SELECT_ID);
        partListJson.put("Title", getMapValue(partListMap, DomainConstants.SELECT_ATTRIBUTE_TITLE));
        partListJson.put("JFProjectId", getMapValue(projectMap, DomainConstants.SELECT_NAME));
        partListJson.put("Name", getMapValue(partListMap, DomainConstants.SELECT_NAME));
        partListJson.put("State", getMapValue(partListMap, DomainConstants.SELECT_CURRENT));
        partListJson.put("CreateTime", getMapValue(partListMap, DomainConstants.SELECT_ORIGINATED));
        partListJson.put("ReleasedTime", getMapValue(partListMap, SELECT_STATE_RELEASED_ACTUAL));
        partListJson.put("JFPartListProfessional", getMapValue(partListMap, SELECT_ATTR_JFPartListProfessional));
        partListJson.put("JFProjectPhase", getMapValue(partListMap, SELECT_ATTR_JFProjectPhase));
        partListJson.put("JFPartListType", getMapValue(partListMap, SELECT_ATTR_JFPartListType));
        partListJson.put("JFSyncSRM", getMapValue(partListMap, SELECT_ATTR_JFSyncSRM));
        partListJson.put("type", getMapValue(partListMap, SELECT_PARTLIST_SNAPSHOT_SUBTYPE));
        partListJson.put("ConnectionSnapShot", getMapValue(partListMap, SELECT_PARTLIST_SNAPSHOT_TITLE));
        partListJson.put("description", getMapValue(partListMap, DomainConstants.SELECT_DESCRIPTION));
        partListJson.put("Parts", getPartListPartArray(context, partListId));
        return partListJson;
    }

    /**
     * 获取PartList零件清单，按零件全部发布版本展开
     **
     * @param context
     * @param partListId
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/12 17:32
     */
    private JSONArray getPartListPartArray(Context context, String partListId) throws Exception {
        JSONArray partArray = new JSONArray();
        if (UIUtil.isNullOrEmpty(partListId)) {
            return partArray;
        }
        DomainObject partListObj = DomainObject.newInstance(context, partListId);
        MapList partList = partListObj.getRelatedObjects(context,
                rel_JFPartList2VPMReference,
                TYPE_VPMReference,
                getPartListCurrentPartSelectList(),
                getPartListRelSelectList(),
                false,
                true,
                (short) 1,
                "",
                "",
                0);
        if (partList == null || partList.isEmpty()) {
            return partArray;
        }

        for (int i = 0; i < partList.size(); i++) {
            Map currentPartMap = (Map) partList.get(i);
            String currentPartId = getMapValue(currentPartMap, DomainConstants.SELECT_ID);
            if (UIUtil.isNullOrEmpty(currentPartId)) {
                continue;
            }
            BusinessObjectList revisionList = JF_Util_mxJPO.sortMapListInRevisionCurrent(context, new String[]{currentPartId});
            if (revisionList == null || revisionList.size() == 0) {
                continue;
            }
            for (int j = 0; j < revisionList.size(); j++) {
                BusinessObject revisionObj = revisionList.get(j);
                DomainObject revisionDomainObj = DomainObject.newInstance(context, revisionObj);
                Map revisionPartMap = revisionDomainObj.getInfo(context, getPartListRevisionPartSelectList());
                partArray.add(buildPartJson(context, currentPartMap, revisionPartMap));
            }
        }
        return partArray;
    }

    private JSONObject buildPartJson(Context context, Map currentPartMap, Map revisionPartMap) throws Exception {
        JSONObject partJson = new JSONObject();
        String revisionPartId = getMapValue(revisionPartMap, DomainConstants.SELECT_ID);
        partJson.put("Name", getMapValue(revisionPartMap, DomainConstants.SELECT_NAME));
        partJson.put("PartNumber", getMapValue(revisionPartMap, SELECT_ATTR_V_PART_NUMBER));
        partJson.put("Revision", getMapValue(revisionPartMap, DomainConstants.SELECT_REVISION));
        partJson.put("State", getMapValue(revisionPartMap, DomainConstants.SELECT_CURRENT));
        partJson.put("PartNameEN", getMapValue(revisionPartMap, SELECT_ATTR_JF_PartNameEN));
        partJson.put("PartNameCN", getMapValue(revisionPartMap, SELECT_ATTR_JF_PartNameCN));
        partJson.put("Owner", getMapValue(revisionPartMap, DomainConstants.SELECT_OWNER));
        partJson.put("JFRootPartNumber", getMapValue(currentPartMap, SELECT_ATTR_JFROOTPARTNUMBER));
        partJson.put("JFRootPartDescription", getMapValue(currentPartMap, SELECT_ATTR_JFROOTPARTDESCRIPTION));
        partJson.put("DirectBuy", getMapValue(revisionPartMap, SELECT_ATTR_JFDIRECT_BUY));
        partJson.put("JSdataProcessProgress", getMapValue(currentPartMap, SELECT_ATTR_JSdataProcessProgress));
        partJson.put("JSReason", getMapValue(currentPartMap, SELECT_ATTR_JSREASON));
        partJson.put("JF_SyncStatus", getMapValue(currentPartMap, SELECT_ATTR_JF_SyncStatus));
        partJson.put("ProcurementType", getMapValue(revisionPartMap, SELECT_ATTR_JF_ProcurementType));
        partJson.put("JFProcurementType", getMapValue(currentPartMap, SELECT_PartList_ATTR_JFProcurementType));
        partJson.put("JFCarryOver", getMapValue(currentPartMap, SELECT_ATTR_JFCarryOver));
        partJson.put("JF_VPMReference.JF_ProcurementGroup", getMapValue(revisionPartMap, SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup));
        partJson.put("JFPartSpecifications", getMapValue(currentPartMap, SELECT_ATTR_JFPartSpecifications));
        partJson.put("JFProportionConfiguration", getMapValue(currentPartMap, SELECT_ATTR_JFPROPORTIONCONFIGURATION));
        partJson.put("JFBicycleUsage", getMapValue(currentPartMap, SELECT_ATTR_JFBicycleUsage));
        partJson.put("JFTotalDemand", getMapValue(currentPartMap, SELECT_ATTR_JFTotalDemand));
        partJson.put("JFDeliveryDate", getMapValue(currentPartMap, SELECT_ATTR_JFDeliveryDate));
        partJson.put("JFPartRequirementPlanDate", getMapValue(currentPartMap, SELECT_ATTR_JFPartRequirementPlanDate));
        partJson.put("JFCurrency", getMapValue(currentPartMap, SELECT_ATTR_JFCurrency));
        partJson.put("JFOutputLocation", getMapValue(currentPartMap, SELECT_ATTR_JFOutputLocation));
        partJson.put("JFBudgetUnitPrice", getMapValue(currentPartMap, SELECT_ATTR_JFBudgetUnitPrice));
        partJson.put("JFBudgetMoldFee", getMapValue(currentPartMap, SELECT_ATTR_JFBudgetMoldFee));
        partJson.put("JFBudgetExperimentalFee", getMapValue(currentPartMap, SELECT_ATTR_JFBudgetExperimentalFee));
        partJson.put("JFBudgetInspectionToolFee", getMapValue(currentPartMap, SELECT_ATTR_JFBudgetInspectionToolFee));
        partJson.put("JFBudgetLeatherTextureFee", getMapValue(currentPartMap, SELECT_ATTR_JFBudgetLeatherTextureFee));
        partJson.put("JFDeliveryPhase", getMapValue(currentPartMap, SELECT_ATTR_JFDeliveryPhase));
        partJson.put("JFSSOWIssueDate", getMapValue(currentPartMap, SELECT_ATTR_JFSSOWIssueDate));
        partJson.put("JFDrawDataCompletionPlanDate", getMapValue(currentPartMap, SELECT_ATTR_JFDrawDataCompletionPlanDate));
        partJson.put("JF3DReleasedDesignatedPlanDate", getMapValue(currentPartMap, SELECT_ATTR_JF3DReleasedDesignatedPlanDate));
        partJson.put("JFSupplierDesignatedPlanDate", getMapValue(currentPartMap, SELECT_ATTR_JFSupplierDesignatedPlanDate));
        partJson.put("JFPartRequirementCommitmentDate", getMapValue(currentPartMap, SELECT_ATTR_JFPartRequirementCommitmentDate));
        partJson.put("Description", getMapValue(currentPartMap, SELECT_ATTR_JF_REMARK));
        partJson.put("JFPartSupplierInfo", getMapValue(currentPartMap, SELECT_ATTR_JFPARTSUPPLIERINFO));
        partJson.put("JFSupplierSouringDate", getMapValue(currentPartMap, SELECT_ATTR_JFSUPPLIERSOURINGDATE));
        partJson.put("JFSouringRespName", getMapValue(currentPartMap, SELECT_ATTR_JFSOURINGRESPNAME));
        partJson.put("JFSouringSupCode", getMapValue(currentPartMap, SELECT_ATTR_JFSOURINGSUPCODE));
        partJson.put("JFSouringSupName", getMapValue(currentPartMap, SELECT_ATTR_JFSOURINGSUPNAME));
        partJson.put("Drawings", getPartDrawingArray(context, revisionPartId));
        return partJson;
    }

    /**
     * 获取零件关联图纸，包含文档图纸和Drawing图纸
     **
     * @param context
     * @param partId
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/12 17:33
     */
    private JSONArray getPartDrawingArray(Context context, String partId) throws Exception {
        JSONArray drawingArray = new JSONArray();
        if (UIUtil.isNullOrEmpty(partId)) {
            return drawingArray;
        }
        DomainObject partObj = DomainObject.newInstance(context, partId);
        MapList documentDrawingList = partObj.getRelatedObjects(context,
                REL_ReferenceDocument,
                TYPE_Document,
                getDrawingSelectList(),
                getDrawingRelSelectList(),
                false,
                true,
                (short) 1,
                SELECT_ATTR_JF_DocumentType + ".value==Drawing",
                "",
                0);
        addDrawingJsonArray(drawingArray, documentDrawingList);

        MapList drawingList = partObj.getRelatedObjects(context,
                REL_XCADBaseDependency,
                TYPE_Drawing,
                getDrawingSelectList(),
                getDrawingRelSelectList(),
                true,
                false,
                (short) 1,
                "",
                "",
                0);
        addDrawingJsonArray(drawingArray, drawingList);
        return drawingArray;
    }

    private void addDrawingJsonArray(JSONArray drawingArray, MapList drawingList) {
        if (drawingList == null || drawingList.isEmpty()) {
            return;
        }
        for (int i = 0; i < drawingList.size(); i++) {
            Map drawingMap = (Map) drawingList.get(i);
            JSONObject drawingJson = new JSONObject();
            String title = getMapValue(drawingMap, DomainConstants.SELECT_ATTRIBUTE_TITLE);
            if (UIUtil.isNullOrEmpty(title)) {
                title = getMapValue(drawingMap, SELECT_ATTR_PLMENTITY_V_NAME);
            }
            drawingJson.put("connectDrwName", getMapValue(drawingMap, DomainConstants.SELECT_NAME));
            drawingJson.put("connectDrwTitle", title);
            drawingJson.put("connectDrwState", getMapValue(drawingMap, DomainConstants.SELECT_CURRENT));
            drawingJson.put("connectDrwReleaseDate", getMapValue(drawingMap, SELECT_STATE_RELEASED_ACTUAL));
            drawingJson.put("connectDrwRevision", getMapValue(drawingMap, DomainConstants.SELECT_REVISION));
            drawingJson.put("connectDrwCreateDate", getMapValue(drawingMap, SELECT_REL_ORIGINATED));
            drawingArray.add(drawingJson);
        }
    }

    /**
     * 获取PCR相关信息
     **
     * @param context
     * @param projectMap
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/12 16:00
     */
    private JSONArray getProjectPCRDataArray(Context context, Map projectMap) throws Exception {
        JSONArray dataarray = new JSONArray();
        String projectId = getMapValue(projectMap, DomainConstants.SELECT_ID);
        if (UIUtil.isNullOrEmpty(projectId)) {
            return dataarray;
        }

        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        MapList pcrList = projectObj.getRelatedObjects(context,
                REL_JFChange2Project,
                Type_JF_PCR,
                getPCRSelectList(),
                relSelectsList,
                true,
                false,
                (short) 1,
                "",
                "",
                0);

        if (pcrList == null || pcrList.isEmpty()) {
            return dataarray;
        }

        for (int i = 0; i < pcrList.size(); i++) {
            Map pcrMap = (Map) pcrList.get(i);
            dataarray.add(buildPCRJson(context, projectMap, pcrMap));
        }
        return dataarray;
    }

    private JSONObject buildPCRJson(Context context, Map projectMap, Map pcrMap) throws Exception {
        JSONObject pcrJson = new JSONObject();
        String pcrId = getMapValue(pcrMap, DomainConstants.SELECT_ID);
        pcrJson.put("Title", getMapValue(pcrMap, DomainConstants.SELECT_ATTRIBUTE_TITLE));
        pcrJson.put("state", getMapValue(pcrMap, SELECT_CURRENT));
        pcrJson.put("Name", getMapValue(pcrMap, DomainConstants.SELECT_NAME));
        pcrJson.put("JF_PCRType", getMapValue(pcrMap, SELECT_ATTR_JF_PCRType));
        pcrJson.put("JF_PCRLevel", getMapValue(pcrMap, SELECT_ATTR_JF_PCRLevel));
        pcrJson.put("mainProject", getMapValue(projectMap, DomainConstants.SELECT_NAME));
        pcrJson.put("JF_PCRProjectPhase", getMapValue(pcrMap, SELECT_ATTR_JF_PCRProjectPhase));
        pcrJson.put("JFPCRFactory", getMapValue(pcrMap, SELECT_ATTR_JFPCRFactory));
        pcrJson.put("JF_PCRProductUnit", getMapValue(pcrMap, SELECT_ATTR_JF_PCRProductUnit));
        pcrJson.put("JFIsPlatformPart", getMapValue(pcrMap, SELECT_ATTR_JF_PCRIsInOFactories));
        pcrJson.put("JFAffectsFactory", getMapValue(pcrMap, SELECT_ATTR_JFAFFECTEDFACTORY));
        pcrJson.put("JFProjectName", getMapValue(projectMap, DomainConstants.SELECT_DESCRIPTION));
        pcrJson.put("JF_PCRRelatedZone", getMapValue(pcrMap, SELECT_ATTR_JF_PCRRelatedZone));
        pcrJson.put("JF_PCRRelatedProcess", getMapValue(pcrMap, SELECT_ATTR_JF_PCRRelatedProcess));
        pcrJson.put("JF_PCRIsAProductChar", getMapValue(pcrMap, SELECT_ATTR_JF_PCRIsAProductChar));
        pcrJson.put("JF_PCRProductSAChar", getMapValue(pcrMap, SELECT_ATTR_JF_PCRProductSAChar));
        pcrJson.put("JF_PCRChangeReason", getMapValue(pcrMap, SELECT_ATTR_JF_PCRChangeReason));
        pcrJson.put("JF_PCRDBeforeChange", getMapValue(pcrMap, SELECT_ATTR_JF_PCRDBeforeChange));
        pcrJson.put("JF_PCRDAfterChange", getMapValue(pcrMap, SELECT_ATTR_JF_PCRDAfterChange));
        pcrJson.put("JF_PCRIsAffectFKPI", getMapValue(pcrMap, SELECT_ATTR_JF_PCRIsAffectFKPI));
        pcrJson.put("JF_PCRKPI", getMapValue(pcrMap, SELECT_ATTR_JF_PCRKPI));
        pcrJson.put("JF_PCRSwitchingMethod", getMapValue(pcrMap, SELECT_ATTR_JF_PCRSwitchingMethod));
        pcrJson.put("JF_PCRDemandBPDate", getMapValue(pcrMap, SELECT_ATTR_JF_PCRDemandBPDate));
        pcrJson.put("EvaluationTasks", getPCREvaluationTaskArray(context, pcrId));
        pcrJson.put("PerformTasks", getPCRPerformTaskArray(context, pcrId));
        pcrJson.put("ValidationTasks", getPCRValidationTaskArray(context, pcrId));
        return pcrJson;
    }

    /**
     * 获取PCR评估任务
     **
     * @param context
     * @param pcrId
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/12 16:01
     */
    private JSONArray getPCREvaluationTaskArray(Context context, String pcrId) throws Exception {
        JSONArray taskArray = new JSONArray();
        if (UIUtil.isNullOrEmpty(pcrId)) {
            return taskArray;
        }
        DomainObject pcrObj = DomainObject.newInstance(context, pcrId);
        MapList taskList = pcrObj.getRelatedObjects(context,
                Rel_JFPCR2Task,
                Type_JFPCRTask,
                getPCREvaluationTaskSelectList(),
                relSelectsList,
                false,
                true,
                (short) 1,
                "",
                "",
                0);
        if (taskList == null || taskList.isEmpty()) {
            return taskArray;
        }
        for (int i = 0; i < taskList.size(); i++) {
            Map taskMap = (Map) taskList.get(i);
            JSONObject taskJson = new JSONObject();
            taskJson.put("Title", getMapValue(taskMap, DomainConstants.SELECT_ATTRIBUTE_TITLE));
            taskJson.put("Owner", getMapValue(taskMap, DomainConstants.SELECT_OWNER));
            taskJson.put("JF_Conclusion", getMapValue(taskMap, SELECT_ATTR_JF_CONCLUSION));
            taskJson.put("JF_RiskDescription", getMapValue(taskMap, SELECT_ATTR_JF_RISKDESCRIPTION));
            taskJson.put("JF_RecommendedMeasures", getMapValue(taskMap, SELECT_ATTR_JF_RECOMMENDEDMEASURES));
            taskJson.put("JF_Operator", getMapValue(taskMap, SELECT_ATTR_JF_OPERATOR));
            taskJson.put("JF_OperationDate", getMapValue(taskMap, SELECT_ATTR_JF_OPERATIONDATE));
            taskJson.put("State", getMapValue(taskMap, DomainConstants.SELECT_CURRENT));
            taskArray.add(taskJson);
        }
        return taskArray;
    }

    /**
     * 获取PCR执行任务
     **
     * @param context
     * @param pcrId
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/12 16:01
     */
    private JSONArray getPCRPerformTaskArray(Context context, String pcrId) throws Exception {
        JSONArray taskArray = new JSONArray();
        if (UIUtil.isNullOrEmpty(pcrId)) {
            return taskArray;
        }
        DomainObject pcrObj = DomainObject.newInstance(context, pcrId);
        MapList taskList = pcrObj.getRelatedObjects(context,
                Rel_JF_PCR2ExecuteTask,
                Type_JF_PCRExecuteTask,
                getPCRPerformTaskSelectList(),
                relSelectsList,
                false,
                true,
                (short) 1,
                "",
                "",
                0);
        if (taskList == null || taskList.isEmpty()) {
            return taskArray;
        }
        for (int i = 0; i < taskList.size(); i++) {
            Map taskMap = (Map) taskList.get(i);
            JSONObject taskJson = new JSONObject();
            taskJson.put("JF_PCRFunction", getMapValue(taskMap, SELECT_ATTR_JF_PCRFUNCTION));
            taskJson.put("Title", getMapValue(taskMap, DomainConstants.SELECT_ATTRIBUTE_TITLE));
            taskJson.put("Owner", getMapValue(taskMap, DomainConstants.SELECT_OWNER));
            taskJson.put("State", getMapValue(taskMap, DomainConstants.SELECT_CURRENT));
            taskJson.put("TaskEstimatedFinishDate", getMapValue(taskMap, SELECT_ATTR_Task_Estimated_Finish_Date));
            taskJson.put("JF_PCRActualFinishTime", getMapValue(taskMap, SELECT_ATTR_JF_PCRACTUALFINISHTIME));
            taskJson.put("JF_Remark", getMapValue(taskMap, SELECT_ATTR_JF_REMARK));
            taskArray.add(taskJson);
        }
        return taskArray;
    }

    /**
     * 获取PCR验证任务
     **
     * @param context
     * @param pcrId
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/12 16:01
     */
    private JSONArray getPCRValidationTaskArray(Context context, String pcrId) throws Exception {
        JSONArray taskArray = new JSONArray();
        if (UIUtil.isNullOrEmpty(pcrId)) {
            return taskArray;
        }
        DomainObject pcrObj = DomainObject.newInstance(context, pcrId);
        MapList taskList = pcrObj.getRelatedObjects(context,
                Rel_JF_PCR2VerificationTask,
                Type_JF_PCRVerificationTask,
                getPCRValidationTaskSelectList(),
                relSelectsList,
                false,
                true,
                (short) 1,
                "",
                "",
                0);
        if (taskList == null || taskList.isEmpty()) {
            return taskArray;
        }
        for (int i = 0; i < taskList.size(); i++) {
            Map taskMap = (Map) taskList.get(i);
            JSONObject taskJson = new JSONObject();
            taskJson.put("JF_Participant", getMapValue(taskMap, SELECT_Attr_JF_Participant));
            taskJson.put("JF_Content", getMapValue(taskMap, SELECT_Attr_JF_Content));
            taskJson.put("JF_DateOfSignature", getMapValue(taskMap, SELECT_Attr_JF_DateOfSignature));
            taskArray.add(taskJson);
        }
        return taskArray;
    }

    /**
     * 获取DA相关信息
     **
     * @param context
     * @param projectMap
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/12 14:00
     */
    private JSONArray getProjectDADataArray(Context context, Map projectMap) throws Exception {
        JSONArray dataarray = new JSONArray();
        String projectId = getMapValue(projectMap, DomainConstants.SELECT_ID);
        if (UIUtil.isNullOrEmpty(projectId)) {
            return dataarray;
        }

        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        MapList daList = projectObj.getRelatedObjects(context,
                REL_JFChange2Project,
                TYPE_JFDA,
                getDASelectList(),
                relSelectsList,
                true,
                false,
                (short) 1,
                "",
                "",
                0);

        if (daList == null || daList.isEmpty()) {
            return dataarray;
        }

        for (int i = 0; i < daList.size(); i++) {
            Map daMap = (Map) daList.get(i);
            dataarray.add(buildDAJson(context, projectMap, daMap));
        }
        return dataarray;
    }

    private JSONObject buildDAJson(Context context, Map projectMap, Map daMap) throws Exception {
        JSONObject daJson = new JSONObject();
        String daId = getMapValue(daMap, DomainConstants.SELECT_ID);
        daJson.put("Title", getMapValue(daMap, DomainConstants.SELECT_ATTRIBUTE_TITLE));
        daJson.put("current", getMapValue(daMap, SELECT_CURRENT));
        daJson.put("JFProjectId", getMapValue(projectMap, DomainConstants.SELECT_NAME));
        daJson.put("Name", getMapValue(daMap, DomainConstants.SELECT_NAME));
        daJson.put("JFProjectName", getMapValue(projectMap, DomainConstants.SELECT_DESCRIPTION));
        daJson.put("JFChangeType", getMapValue(daMap, SELECT_ATTR_JFCHANGETYPE));
        daJson.put("JFProjectPhase", getMapValue(daMap, SELECT_ATTR_JFProjectPhase));
        daJson.put("JFAffectsFactory", getMapValue(daMap, SELECT_ATTR_JFAFFECTSFACTORY));
        daJson.put("JFReasonDeviation", getMapValue(daMap, SELECT_ATTR_JFREASONDEVIATION));
        daJson.put("JFCreator", getMapValue(daMap, DomainConstants.SELECT_OWNER));
        daJson.put("JFBeforeChange", getMapValue(daMap, SELECT_ATTR_JFBEFORECHANGE));
        daJson.put("JFAfterChange", getMapValue(daMap, SELECT_ATTR_JFAFTERCHANGE));
        daJson.put("JFDAStartTime", getMapValue(daMap, SELECT_ATTR_JFDASTARTTIME));
        daJson.put("JFDACloseTime", getMapValue(daMap, SELECT_ATTR_JFDACLOSETIME));
        daJson.put("JFDAIsDelay", getMapValue(daMap, SELECT_ATTR_JFDAISDELAY));
        daJson.put("JFDAExtensionTime", getMapValue(daMap, SELECT_ATTR_JFDAEXTENSIONTIME));
        daJson.put("Tasks", getDATaskArray(context, daId));
        daJson.put("Parts", getDAPartArray(context, daId));
        return daJson;
    }

    /**
     * 获取DA的执行任务
     **
     * @param context
     * @param daId
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/12 14:01
     */
    private JSONArray getDATaskArray(Context context, String daId) throws Exception {
        JSONArray taskArray = new JSONArray();
        if (UIUtil.isNullOrEmpty(daId)) {
            return taskArray;
        }
        DomainObject daObj = DomainObject.newInstance(context, daId);
        MapList taskList = daObj.getRelatedObjects(context,
                RELATIONSHIP_JFDA2JFDATASK,
                TYPE_JF_DATASK,
                getDATaskSelectList(),
                relSelectsList,
                false,
                true,
                (short) 0,
                "",
                "",
                0);
        if (taskList == null || taskList.isEmpty()) {
            return taskArray;
        }
        for (int i = 0; i < taskList.size(); i++) {
            Map taskMap = (Map) taskList.get(i);
            JSONObject taskJson = new JSONObject();
            taskJson.put("Name", getMapValue(taskMap, DomainConstants.SELECT_NAME));
            taskJson.put("TaskName", getMapValue(taskMap, SELECT_ATTR_TASK_TITLE));
            taskJson.put("Project_Role", getMapValue(taskMap, SELECT_ATTR_PROJECT_ROLE));
            taskJson.put("TaskOwner", getMapValue(taskMap, SELECT_ASSIGNED_TASK_OWNER));
            taskJson.put("Task Estimated Finish Date", getMapValue(taskMap, SELECT_ATTR_Task_Estimated_Finish_Date));
            taskJson.put("JF_DAActualFinishTime", getMapValue(taskMap, SELECT_ATTR_JF_DAACTUALFINISHTIME));
            taskJson.put("JF_DAwhetherToPostpone", getMapValue(taskMap, SELECT_ATTR_DAWHETHERTOPOSTPONE));
            taskJson.put("JF_CostChanges", getMapValue(taskMap, SELECT_ATTR_JF_COSTCHANGES));
            taskJson.put("CreationTime", getMapValue(taskMap, DomainConstants.SELECT_ORIGINATED));
            taskJson.put("State", getMapValue(taskMap, DomainConstants.SELECT_CURRENT));
            taskArray.add(taskJson);
        }
        return taskArray;
    }

    /**
     * 获取DA的零件清单
     **
     * @param context
     * @param daId
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/12 14:01
     */
    private JSONArray getDAPartArray(Context context, String daId) throws Exception {
        JSONArray partArray = new JSONArray();
        if (UIUtil.isNullOrEmpty(daId)) {
            return partArray;
        }
        DomainObject daObj = DomainObject.newInstance(context, daId);
        MapList partList = daObj.getRelatedObjects(context,
                REL_JFDA2VPMREFERENCE,
                TYPE_VPMREFERENCE,
                getDAPartSelectList(),
                relSelectsList,
                false,
                true,
                (short) 0,
                "",
                "",
                0);
        if (partList == null || partList.isEmpty()) {
            return partArray;
        }
        for (int i = 0; i < partList.size(); i++) {
            Map partMap = (Map) partList.get(i);
            JSONObject partJson = new JSONObject();
            partJson.put("name", getMapValue(partMap, DomainConstants.SELECT_NAME));
            partJson.put("PartNumber", getMapValue(partMap, SELECT_ATTR_V_PART_NUMBER));
            partJson.put("revision", getMapValue(partMap, DomainConstants.SELECT_REVISION));
            partJson.put("State", getMapValue(partMap, DomainConstants.SELECT_CURRENT));
            partJson.put("PartNameEN", getMapValue(partMap, SELECT_ATTR_JF_PartNameEN));
            partJson.put("PartNameCN", getMapValue(partMap, SELECT_ATTR_JF_PartNameCN));
            partJson.put("Owner", getMapValue(partMap, DomainConstants.SELECT_OWNER));
            partArray.add(partJson);
        }
        return partArray;
    }

    /**
     * 获取ECO相关信息
     **
     * @param context
     * @param projectMap
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/12 10:39
     */
    private JSONArray getProjectECODataArray(Context context, Map projectMap) throws Exception {
        JSONArray dataarray = new JSONArray();
        String projectId = getMapValue(projectMap, DomainConstants.SELECT_ID);
        if (UIUtil.isNullOrEmpty(projectId)) {
            return dataarray;
        }

        StringList ecrSelects = new StringList();
        ecrSelects.add(DomainConstants.SELECT_ID);
        ecrSelects.add(DomainConstants.SELECT_NAME);
        ecrSelects.add(SELECT_ECR_CHANGE_EVENT_TYPE);

        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        MapList ecrList = projectObj.getRelatedObjects(context,
                REL_JFChange2Project,
                TYPE_JFECR + "," + TYPE_JFNewECR + "," + TYPE_JFFormalECR,
                ecrSelects,
                relSelectsList,
                true,
                false,
                (short) 1,
                "",
                "",
                0);

        if (ecrList == null || ecrList.isEmpty()) {
            return dataarray;
        }

        for (int i = 0; i < ecrList.size(); i++) {
            Map ecrMap = (Map) ecrList.get(i);
            String ecrId = getMapValue(ecrMap, DomainConstants.SELECT_ID);
            if (UIUtil.isNullOrEmpty(ecrId)) {
                continue;
            }
            DomainObject ecrObj = DomainObject.newInstance(context, ecrId);
            MapList ecoList = ecrObj.getRelatedObjects(context,
                    REL_JFECR2CO,
                    TYPE_JFECO,
                    getECOSelectList(),
                    relSelectsList,
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    0);
            if (ecoList == null || ecoList.isEmpty()) {
                continue;
            }
            for (int j = 0; j < ecoList.size(); j++) {
                Map ecoMap = (Map) ecoList.get(j);
                dataarray.add(buildECOJson(context, projectMap, ecrMap, ecoMap));
            }
        }
        return dataarray;
    }

    private JSONObject buildECOJson(Context context, Map projectMap, Map ecrMap, Map ecoMap) throws Exception {
        JSONObject ecoJson = new JSONObject();
        ecoJson.put("JFProjectId", getMapValue(projectMap, DomainConstants.SELECT_NAME));
        ecoJson.put("JFProjectName", getMapValue(projectMap, DomainConstants.SELECT_DESCRIPTION));
        ecoJson.put("JFProjectPhase", getMapValue(ecoMap, SELECT_ATTR_JFProjectPhase));
        ecoJson.put("JFECRChangeType", getMapValue(ecoMap, SELECT_ATTR_JFECRCHANGETYPE));
        ecoJson.put("JFChangeSource", getMapValue(ecoMap, SELECT_ATTR_JFCHANGESOURCE));
        ecoJson.put("JFIsPlatformPart", getMapValue(ecoMap, SELECT_ATTR_JFISPLATFORMPART));
        ecoJson.put("JFAffectsFactory", getMapValue(ecoMap, SELECT_ATTR_JFAFFECTEDFACTORY));
        ecoJson.put("JFChangeEventType", getMapValue(ecrMap, SELECT_ECR_CHANGE_EVENT_TYPE));
        ecoJson.put("JFChangesDeveExpensesManHours", getMapValue(ecoMap, SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS));
        ecoJson.put("JFChangesDeveExpensesManHoursExternal", getMapValue(ecoMap, SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL));
        ecoJson.put("JFChangeReson", getMapValue(ecoMap, SELECT_ATTR_JFCHANGERESON));
        ecoJson.put("Description", getMapValue(ecoMap, DomainConstants.SELECT_DESCRIPTION));
        ecoJson.put("JFBreakpointMode", getMapValue(ecoMap, SELECT_ATTR_JFBREAKPOINTMODE));
        ecoJson.put("JFBreakpointTime", getMapValue(ecoMap, SELECT_ATTR_JFBREAKPOINTTIME));
        //20260827 update by codex caipan ECO基本信息增加断点失效标识
        ecoJson.put("JF_IsBreakpointInvalid", getMapValue(ecoMap, SELECT_ATTR_JFISBREAKPOINTINVALID));
        ecoJson.put("JFConnectECR", getMapValue(ecrMap, DomainConstants.SELECT_NAME));
        ecoJson.put("Name", getMapValue(ecoMap, DomainConstants.SELECT_NAME));
        ecoJson.put("Title", getMapValue(ecoMap, SELECT_ATTRIBUTE_TITLE));
        ecoJson.put("current", getMapValue(ecoMap, SELECT_CURRENT));
        ecoJson.put("Tasks", getECOTaskArray(context, getMapValue(ecoMap, DomainConstants.SELECT_ID)));
        return ecoJson;
    }

    /**
     * 获取ECO的执行任务
     **
     * @param context
     * @param ecoId
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/12 10:41
     */
    private JSONArray getECOTaskArray(Context context, String ecoId) throws Exception {
        JSONArray taskArray = new JSONArray();
        if (UIUtil.isNullOrEmpty(ecoId)) {
            return taskArray;
        }
        DomainObject ecoObj = DomainObject.newInstance(context, ecoId);
        MapList taskList = ecoObj.getRelatedObjects(context,
                RELATIONSHIP_JFCO2ECOTASK,
                TYPE_JF_ECOTASK,
                getECOTaskSelectList(),
                relSelectsList,
                false,
                true,
                (short) 1,
                "",
                "",
                0);
        if (taskList == null || taskList.isEmpty()) {
            return taskArray;
        }
        for (int i = 0; i < taskList.size(); i++) {
            Map taskMap = (Map) taskList.get(i);
            JSONObject taskJson = new JSONObject();
            taskJson.put("Name", getMapValue(taskMap, DomainConstants.SELECT_NAME));
            taskJson.put("TaskName", getMapValue(taskMap, SELECT_ATTR_TASK_TITLE));
            taskJson.put("Project_Role", getMapValue(taskMap, SELECT_ATTR_PROJECT_ROLE));
            taskJson.put("TaskOwner", getMapValue(taskMap, SELECT_ASSIGNED_TASK_OWNER));
            taskJson.put("Task_Estimated_Finish_Date", getMapValue(taskMap, SELECT_ATTR_Task_Estimated_Finish_Date));
            taskJson.put("JF_DAActualFinishTime", getMapValue(taskMap, SELECT_ATTR_JF_DAACTUALFINISHTIME));
            taskJson.put("JF_ECPwhetherToPostpone", getMapValue(taskMap, SELECT_ATTR_WHETHERTOPOSTPONE));
            taskJson.put("CreationTime", getMapValue(taskMap, DomainConstants.SELECT_ORIGINATED));
            taskJson.put("State", getMapValue(taskMap, DomainConstants.SELECT_CURRENT));
            taskArray.add(taskJson);
        }
        return taskArray;
    }

    private StringList getECOSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_DESCRIPTION);
        selects.add(SELECT_ATTR_JFProjectPhase);
        selects.add(SELECT_CURRENT);
        selects.add(SELECT_ATTRIBUTE_TITLE);
        selects.add(SELECT_ATTR_JFECRCHANGETYPE);
        selects.add(SELECT_ATTR_JFCHANGESOURCE);
        selects.add(SELECT_ATTR_JFISPLATFORMPART);
        selects.add(SELECT_ATTR_JFAFFECTEDFACTORY);
        selects.add(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS);
        selects.add(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL);
        selects.add(SELECT_ATTR_JFCHANGERESON);
        selects.add(SELECT_ATTR_JFBREAKPOINTMODE);
        selects.add(SELECT_ATTR_JFBREAKPOINTTIME);
        //20260827 update by codex caipan ECO基本信息查询断点失效标识
        selects.add(SELECT_ATTR_JFISBREAKPOINTINVALID);
        return selects;
    }

    private StringList getECOTaskSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_ORIGINATED);
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add(SELECT_ATTR_TASK_TITLE);
        selects.add(SELECT_ATTR_PROJECT_ROLE);
        selects.add(SELECT_ASSIGNED_TASK_OWNER);
        selects.add(SELECT_ATTR_Task_Estimated_Finish_Date);
        selects.add(SELECT_ATTR_JF_DAACTUALFINISHTIME);
        selects.add(SELECT_ATTR_WHETHERTOPOSTPONE);
        return selects;
    }

    private StringList getPCRSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(SELECT_CURRENT);
        selects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        selects.add(SELECT_ATTR_JF_PCRType);
        selects.add(SELECT_ATTR_JF_PCRLevel);
        selects.add(SELECT_ATTR_JF_PCRProjectPhase);
        selects.add(SELECT_ATTR_JFPCRFactory);
        selects.add(SELECT_ATTR_JF_PCRProductUnit);
        selects.add(SELECT_ATTR_JF_PCRIsInOFactories);
        selects.add(SELECT_ATTR_JFAFFECTEDFACTORY);
        selects.add(SELECT_ATTR_JF_PCRRelatedZone);
        selects.add(SELECT_ATTR_JF_PCRRelatedProcess);
        selects.add(SELECT_ATTR_JF_PCRIsAProductChar);
        selects.add(SELECT_ATTR_JF_PCRProductSAChar);
        selects.add(SELECT_ATTR_JF_PCRChangeReason);
        selects.add(SELECT_ATTR_JF_PCRDBeforeChange);
        selects.add(SELECT_ATTR_JF_PCRDAfterChange);
        selects.add(SELECT_ATTR_JF_PCRIsAffectFKPI);
        selects.add(SELECT_ATTR_JF_PCRKPI);
        selects.add(SELECT_ATTR_JF_PCRSwitchingMethod);
        selects.add(SELECT_ATTR_JF_PCRDemandBPDate);
        return selects;
    }

    private StringList getPCREvaluationTaskSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        selects.add(DomainConstants.SELECT_OWNER);
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add(SELECT_ATTR_JF_CONCLUSION);
        selects.add(SELECT_ATTR_JF_RISKDESCRIPTION);
        selects.add(SELECT_ATTR_JF_RECOMMENDEDMEASURES);
        selects.add(SELECT_ATTR_JF_OPERATOR);
        selects.add(SELECT_ATTR_JF_OPERATIONDATE);
        return selects;
    }

    private StringList getPCRPerformTaskSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        selects.add(DomainConstants.SELECT_OWNER);
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add(SELECT_ATTR_JF_PCRFUNCTION);
        selects.add(SELECT_ATTR_Task_Estimated_Finish_Date);
        selects.add(SELECT_ATTR_JF_PCRACTUALFINISHTIME);
        selects.add(SELECT_ATTR_JF_REMARK);
        return selects;
    }

    private StringList getPCRValidationTaskSelectList() {
        StringList selects = new StringList();
        selects.add(SELECT_Attr_JF_Participant);
        selects.add(SELECT_Attr_JF_Content);
        selects.add(SELECT_Attr_JF_DateOfSignature);
        return selects;
    }

    private StringList getPartListSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_TYPE);
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add(DomainConstants.SELECT_ORIGINATED);
        selects.add(DomainConstants.SELECT_DESCRIPTION);
        selects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        selects.add(SELECT_STATE_RELEASED_ACTUAL);
        selects.add(SELECT_ATTR_JFPartListProfessional);
        selects.add(SELECT_ATTR_JFProjectPhase);
        selects.add(SELECT_ATTR_JFPartListType);
        selects.add(SELECT_ATTR_JFSyncSRM);
        selects.add(SELECT_PARTLIST_SNAPSHOT_TITLE);
        selects.add(SELECT_PARTLIST_SNAPSHOT_SUBTYPE);
        return selects;
    }

    private StringList getPartListCurrentPartSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        return selects;
    }

    private StringList getPartListRevisionPartSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_REVISION);
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add(DomainConstants.SELECT_OWNER);
        selects.add(SELECT_ATTR_V_PART_NUMBER);
        selects.add(SELECT_ATTR_JF_PartNameEN);
        selects.add(SELECT_ATTR_JF_PartNameCN);
        selects.add(SELECT_ATTR_JFDIRECT_BUY);
        selects.add(SELECT_ATTR_JF_ProcurementType);
        selects.add(SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup);
        return selects;
    }

    private StringList getPartListRelSelectList() {
        StringList selects = new StringList();
        selects.add(DomainRelationship.SELECT_ID);
        selects.add(SELECT_ATTR_JFROOTPARTNUMBER);
        selects.add(SELECT_ATTR_JFROOTPARTDESCRIPTION);
        selects.add(SELECT_ATTR_JSdataProcessProgress);
        selects.add(SELECT_ATTR_JSREASON);
        selects.add(SELECT_ATTR_JF_SyncStatus);
        selects.add(SELECT_PartList_ATTR_JFProcurementType);
        selects.add(SELECT_ATTR_JFCarryOver);
        selects.add(SELECT_ATTR_JFPartSpecifications);
        selects.add(SELECT_ATTR_JFPROPORTIONCONFIGURATION);
        selects.add(SELECT_ATTR_JFBicycleUsage);
        selects.add(SELECT_ATTR_JFTotalDemand);
        selects.add(SELECT_ATTR_JFDeliveryDate);
        selects.add(SELECT_ATTR_JFPartRequirementPlanDate);
        selects.add(SELECT_ATTR_JFCurrency);
        selects.add(SELECT_ATTR_JFOutputLocation);
        selects.add(SELECT_ATTR_JFBudgetUnitPrice);
        selects.add(SELECT_ATTR_JFBudgetMoldFee);
        selects.add(SELECT_ATTR_JFBudgetExperimentalFee);
        selects.add(SELECT_ATTR_JFBudgetInspectionToolFee);
        selects.add(SELECT_ATTR_JFBudgetLeatherTextureFee);
        selects.add(SELECT_ATTR_JFDeliveryPhase);
        selects.add(SELECT_ATTR_JFSSOWIssueDate);
        selects.add(SELECT_ATTR_JFDrawDataCompletionPlanDate);
        selects.add(SELECT_ATTR_JF3DReleasedDesignatedPlanDate);
        selects.add(SELECT_ATTR_JFSupplierDesignatedPlanDate);
        selects.add(SELECT_ATTR_JFPartRequirementCommitmentDate);
        selects.add(SELECT_ATTR_JF_REMARK);
        selects.add(SELECT_ATTR_JFPARTSUPPLIERINFO);
        selects.add(SELECT_ATTR_JFSUPPLIERSOURINGDATE);
        selects.add(SELECT_ATTR_JFSOURINGRESPNAME);
        selects.add(SELECT_ATTR_JFSOURINGSUPCODE);
        selects.add(SELECT_ATTR_JFSOURINGSUPNAME);
        return selects;
    }

    private StringList getDrawingSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_REVISION);
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        selects.add(SELECT_ATTR_PLMENTITY_V_NAME);
        selects.add(SELECT_STATE_RELEASED_ACTUAL);
        return selects;
    }

    private StringList getDrawingRelSelectList() {
        StringList selects = new StringList();
        selects.add(DomainRelationship.SELECT_ID);
        selects.add(SELECT_REL_ORIGINATED);
        return selects;
    }

    private StringList getDASelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_OWNER);
        selects.add(SELECT_CURRENT);
        selects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        selects.add(SELECT_ATTR_JFCHANGETYPE);
        selects.add(SELECT_ATTR_JFProjectPhase);
        selects.add(SELECT_ATTR_JFAFFECTSFACTORY);
        selects.add(SELECT_ATTR_JFREASONDEVIATION);
        selects.add(SELECT_ATTR_JFBEFORECHANGE);
        selects.add(SELECT_ATTR_JFAFTERCHANGE);
        selects.add(SELECT_ATTR_JFDASTARTTIME);
        selects.add(SELECT_ATTR_JFDACLOSETIME);
        selects.add(SELECT_ATTR_JFDAISDELAY);
        selects.add(SELECT_ATTR_JFDAEXTENSIONTIME);
        return selects;
    }

    private StringList getDATaskSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_ORIGINATED);
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add(SELECT_ATTR_TASK_TITLE);
        selects.add(SELECT_ATTR_PROJECT_ROLE);
        selects.add(SELECT_ASSIGNED_TASK_OWNER);
        selects.add(SELECT_ATTR_Task_Estimated_Finish_Date);
        selects.add(SELECT_ATTR_JF_DAACTUALFINISHTIME);
        selects.add(SELECT_ATTR_DAWHETHERTOPOSTPONE);
        selects.add(SELECT_ATTR_JF_COSTCHANGES);
        return selects;
    }

    private StringList getDAPartSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_REVISION);
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add(DomainConstants.SELECT_OWNER);
        selects.add(SELECT_ATTR_V_PART_NUMBER);
        selects.add(SELECT_ATTR_JF_PartNameEN);
        selects.add(SELECT_ATTR_JF_PartNameCN);
        return selects;
    }

    private String getMapValue(Map map, String key) {
        if (map == null || UIUtil.isNullOrEmpty(key)) {
            return "";
        }
        Object value = map.get(key);
        if (value == null) {
            return "";
        }
        if (value instanceof StringList) {
            return ((StringList) value).join(",");
        }
        if (value instanceof List) {
            StringList valueList = new StringList();
            valueList.addAll((List) value);
            return valueList.join(",");
        }
        return String.valueOf(value);
    }

    /**
     * 初始化项目文件夹为研发技术文档文件夹到中转站，目的是是给AI使用
      **
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @author caipan
     * @date 2026/6/15 10:08
     */
    public JSONObject getProjectConnectFileInfo(Context context, String[]args)throws Exception{
        JSONObject returnJosn=new JSONObject();
        boolean pushed = false;
        int projectCount = 0;
        int documentCount = 0;
        try {
            ContextUtil.pushContext(context);
            pushed = true;
            logger.info("getProjectConnectFileInfo begin, params:{}", args);
            String transferRootPath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{PROJECT_DOCUMENT_TRANSFER_PATH_KEY});
            if (UIUtil.isNullOrEmpty(transferRootPath)) {
                throw new Exception("配置项 " + PROJECT_DOCUMENT_TRANSFER_PATH_KEY + " 为空");
            }
            String projectDocumentFolderName = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{PROJECT_DOCUMENT_FOLDER_KEY});
            if (UIUtil.isNullOrEmpty(projectDocumentFolderName)) {
                throw new Exception("配置项 " + PROJECT_DOCUMENT_FOLDER_KEY + " 为空");
            }

            MapList projectList = getProjectConnectFileProjectList(context, args);
            projectCount = projectList == null ? 0 : projectList.size();
            if (projectList != null && !projectList.isEmpty()) {
                for (int i = 0; i < projectList.size(); i++) {
                    Map projectMap = (Map) projectList.get(i);
                    documentCount += initProjectConnectFileInfo(context, projectMap, transferRootPath, projectDocumentFolderName);
                }
            }

            returnJosn.put("code","200");
            returnJosn.put("msg","");
            returnJosn.put("result","success");
            returnJosn.put("projectCount", projectCount);
            returnJosn.put("documentCount", documentCount);
        }catch (Exception e){
            e.printStackTrace();
            returnJosn.put("code","500");
            returnJosn.put("msg",e.getMessage());
            returnJosn.put("result","failed");
            returnJosn.put("projectCount", projectCount);
            returnJosn.put("documentCount", documentCount);
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
            logger.info("getProjectConnectFileInfo end, projectCount:{}, documentCount:{}", projectCount, documentCount);
        }
        return returnJosn;
    }


    /**
     * 文档发布ActionTrigger：发布时如果文档位于配置的项目研发技术文档目录下，则同步到文件中转站。
     * 该同步失败不阻断发布流程，只记录日志。
     **
     * @param context
     * @param args
     * @throws Exception
     * @author caipan
     * @date 2026/6/15 15:36
     */
    public void syncReleasedProjectDocumentToFileStation(Context context, String[] args) throws Exception {
        logger.info("getProjectByFolderChain:{}",args);
        boolean pushed = false;
        try {
            if (args == null || args.length == 0 || UIUtil.isNullOrEmpty(args[0])) {
                return;
            }
            ContextUtil.pushContext(context);
            pushed = true;
            String documentId = args[0];
            String transferRootPath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{PROJECT_DOCUMENT_TRANSFER_PATH_KEY});
            String projectDocumentFolderName = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{PROJECT_DOCUMENT_FOLDER_KEY});
            if (UIUtil.isNullOrEmpty(transferRootPath) || UIUtil.isNullOrEmpty(projectDocumentFolderName)) {
                logger.info("syncReleasedProjectDocumentToFileStation config empty, documentId:{}, path:{}, folder:{}",
                        documentId, transferRootPath, projectDocumentFolderName);
                return;
            }

            Map projectMap = getDocumentProjectByFolderPath(context, documentId, projectDocumentFolderName);
            if (projectMap == null || projectMap.isEmpty()) {
                logger.info("syncReleasedProjectDocumentToFileStation skip non project document folder, documentId:{}", documentId);
                return;
            }
            syncProjectDocumentToFileStation(context, projectMap, documentId, transferRootPath);
        } catch (Exception e) {
            logger.info("syncReleasedProjectDocumentToFileStation failed and ignore", e);
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
        }
        logger.info("getProjectByFolderChain end");
    }

    /**
     * 获取本次初始化需要处理的项目；args为空时处理全部项目，否则按项目name精确查询。
     */
    private MapList getProjectConnectFileProjectList(Context context, String[] args) throws Exception {
        StringList projectSelects = new StringList();
        projectSelects.add(DomainConstants.SELECT_ID);
        projectSelects.add(DomainConstants.SELECT_NAME);
        projectSelects.add(DomainConstants.SELECT_DESCRIPTION);
        MapList projectList = new MapList();

        if (args == null || args.length == 0) {
            return DomainObject.findObjects(context, DomainConstants.TYPE_PROJECT_SPACE,
                    DomainConstants.QUERY_WILDCARD,
                    "",
                    projectSelects);
        }

        Set<String> projectNameSet = new LinkedHashSet<>();
        for (String projectName : args) {
            if (UIUtil.isNotNullAndNotEmpty(projectName)) {
                projectNameSet.add(projectName.trim());
            }
        }
        for (String projectName : projectNameSet) {
            MapList oneProjectList = DomainObject.findObjects(context, DomainConstants.TYPE_PROJECT_SPACE,
                    DomainConstants.QUERY_WILDCARD,
                    "name == '" + escapeWhereValue(projectName) + "'",
                    projectSelects);
            if (oneProjectList == null || oneProjectList.isEmpty()) {
                logger.info("getProjectConnectFileInfo project not found:{}", projectName);
                continue;
            }
            projectList.addAll(oneProjectList);
        }
        return projectList;
    }

    /**
     * 初始化单个项目下研发技术文档目录中的全部历史文档到中转站。
     */
    private int initProjectConnectFileInfo(Context context, Map projectMap, String transferRootPath, String projectDocumentFolderName) throws Exception {
        int documentCount = 0;
        String projectId = getMapValue(projectMap, DomainConstants.SELECT_ID);
        if (UIUtil.isNullOrEmpty(projectId)) {
            return documentCount;
        }

        String projectDocumentFolderId = getProjectDocumentFolderId(context, projectId, projectDocumentFolderName);
        if (UIUtil.isNullOrEmpty(projectDocumentFolderId)) {
            logger.info("getProjectConnectFileInfo no project document folder, project:{}", getMapValue(projectMap, DomainConstants.SELECT_NAME));
            return documentCount;
        }

        MapList documentList = getProjectDocumentList(context, projectDocumentFolderId);
        if (documentList == null || documentList.isEmpty()) {
            return documentCount;
        }
        logger.info("documentList size:{}",documentList.size());
        for (int i = 0; i < documentList.size(); i++) {
            try {
                Map documentMap = (Map) documentList.get(i);
                String documentId = getMapValue(documentMap, DomainConstants.SELECT_ID);
                if (UIUtil.isNullOrEmpty(documentId)) {
                    continue;
                }
                if (syncProjectDocumentToFileStation(context, projectMap, documentId, transferRootPath)) {
                    documentCount++;
                }
            } catch (Exception e) {
                logger.info("getProjectConnectFileInfo skip document by error, project:{}", getMapValue(projectMap, DomainConstants.SELECT_NAME), e);
            }
        }
        return documentCount;
    }

    /**
     * 从项目的Data Vaults关系下查找配置指定的研发技术文档根文件夹。
     */
    private String getProjectDocumentFolderId(Context context, String projectId, String projectDocumentFolderName) throws Exception {
        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        StringList folderSelects = new StringList();
        folderSelects.add(DomainConstants.SELECT_ID);
        folderSelects.add(DomainConstants.SELECT_NAME);
        folderSelects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);

        MapList folderList = projectObj.getRelatedObjects(context,
                RELATIONSHIP_DATA_VAULTS,
                TYPE_WORKSPACE_VAULT,
                folderSelects,
                relSelectsList,
                false,
                true,
                (short) 1,
                "",
                "",
                0);
        if (folderList == null || folderList.isEmpty()) {
            return "";
        }

        for (int i = 0; i < folderList.size(); i++) {
            Map folderMap = (Map) folderList.get(i);
            String folderName = getMapValue(folderMap, DomainConstants.SELECT_NAME);
            String folderTitle = getMapValue(folderMap, DomainConstants.SELECT_ATTRIBUTE_TITLE);
            if (projectDocumentFolderName.equals(folderName) || projectDocumentFolderName.equals(folderTitle)) {
                return getMapValue(folderMap, DomainConstants.SELECT_ID);
            }
        }
        return "";
    }

    /**
     * 递归展开研发技术文档文件夹，仅返回最终需要同步的Document对象。
     */
    private MapList getProjectDocumentList(Context context, String projectDocumentFolderId) throws Exception {
        DomainObject folderObj = DomainObject.newInstance(context, projectDocumentFolderId);
        StringList documentSelects = getProjectDocumentSelectList();
        MapList allList = folderObj.getRelatedObjects(context,
                RELATIONSHIP_SUB_VAULTS + "," + RELATIONSHIP_VAULTED_OBJECTS,
                TYPE_WORKSPACE_VAULT + "," + TYPE_Document,
                documentSelects,
                relSelectsList,
                false,
                true,
                (short) 0,
                "",
                "",
                0);

        MapList documentList = new MapList();
        Set<String> documentIdSet = new LinkedHashSet<>();
        if (allList == null || allList.isEmpty()) {
            return documentList;
        }
        for (int i = 0; i < allList.size(); i++) {
            Map objectMap = (Map) allList.get(i);
            if (!TYPE_Document.equals(getMapValue(objectMap, DomainConstants.SELECT_TYPE))) {
                continue;
            }  if (!"RELEASED".equals(getMapValue(objectMap, SELECT_CURRENT))) {
                continue;
            }
            String documentId = getMapValue(objectMap, DomainConstants.SELECT_ID);
            if (UIUtil.isNullOrEmpty(documentId) || documentIdSet.contains(documentId)) {
                continue;
            }
            documentIdSet.add(documentId);
            documentList.add(objectMap);
        }
        return documentList;
    }

    /**
     * 同步单个项目文档到中转站，初始化和发布触发器共用，确保目录、文件和doc.json规则一致。
     */
    private boolean syncProjectDocumentToFileStation(Context context, Map projectMap, String documentId, String transferRootPath) throws Exception {
        if (projectMap == null || projectMap.isEmpty() || UIUtil.isNullOrEmpty(documentId)) {
            return false;
        }
        DomainObject documentObj = DomainObject.newInstance(context, documentId);
        Map currentDocumentMap = documentObj.getInfo(context, getProjectDocumentSelectList());
        JSONObject documentJson = buildProjectDocumentJson(context, projectMap, currentDocumentMap);
        String docId = documentJson.getString("doc_id");
        String docEdition = documentJson.getString("doc_edition");
        File documentDir = createProjectDocumentDir(transferRootPath, docId, docEdition);
        JSONArray filesArray = getProjectDocumentFilesArray(context, documentId, documentDir.getAbsolutePath() + File.separator);
        documentJson.put("files", filesArray);
        writeProjectDocumentJson(documentDir, documentJson);
        return true;
    }

    /**
     * 根据文档所在文件夹链路判断是否属于配置的项目研发技术文档目录，并返回所属项目。
     */
    private Map getDocumentProjectByFolderPath(Context context, String documentId, String projectDocumentFolderName) throws Exception {
        DomainObject documentObj = DomainObject.newInstance(context, documentId);
        MapList folderList = documentObj.getRelatedObjects(context,
                RELATIONSHIP_VAULTED_OBJECTS,
                TYPE_WORKSPACE_VAULT,
                getWorkspaceVaultSelectList(),
                relSelectsList,
                true,
                false,
                (short) 1,
                "",
                "",
                0);
        if (folderList == null || folderList.isEmpty()) {
            return null;
        }

        Set<String> checkedFolderIdSet = new LinkedHashSet<>();
        for (int i = 0; i < folderList.size(); i++) {
            Map folderMap = (Map) folderList.get(i);
            Map projectMap = getProjectByFolderChain(context, folderMap, projectDocumentFolderName, checkedFolderIdSet);
            if (projectMap != null && !projectMap.isEmpty()) {
                return projectMap;
            }
        }
        return null;
    }

    /**
     * 沿Sub Vaults父级逐级向上查找配置文件夹，命中后再通过Data Vaults取项目。
     */
    private Map getProjectByFolderChain(Context context, Map startFolderMap, String projectDocumentFolderName, Set<String> checkedFolderIdSet) throws Exception {
        Map currentFolderMap = startFolderMap;
        while (currentFolderMap != null && !currentFolderMap.isEmpty()) {
            String folderId = getMapValue(currentFolderMap, DomainConstants.SELECT_ID);
            if (UIUtil.isNullOrEmpty(folderId) || checkedFolderIdSet.contains(folderId)) {
                return null;
            }
            checkedFolderIdSet.add(folderId);

            DomainObject folderObj = DomainObject.newInstance(context, folderId);
            currentFolderMap = folderObj.getInfo(context, getWorkspaceVaultSelectList());
            if (isProjectDocumentFolder(currentFolderMap, projectDocumentFolderName)) {
                return getProjectByDocumentFolder(context, folderId);
            }

            MapList parentFolderList = folderObj.getRelatedObjects(context,
                    RELATIONSHIP_SUB_VAULTS,
                    TYPE_WORKSPACE_VAULT,
                    getWorkspaceVaultSelectList(),
                    relSelectsList,
                    true,
                    false,
                    (short) 1,
                    "",
                    "",
                    0);
            if (parentFolderList == null || parentFolderList.isEmpty()) {
                return null;
            }
            currentFolderMap = (Map) parentFolderList.get(0);
        }
        return null;
    }

    /**
     * 从研发技术文档根目录通过Data Vaults关系获取项目。
     */
    private Map getProjectByDocumentFolder(Context context, String projectDocumentFolderId) throws Exception {
        DomainObject folderObj = DomainObject.newInstance(context, projectDocumentFolderId);
        MapList projectList = folderObj.getRelatedObjects(context,
                RELATIONSHIP_DATA_VAULTS,
                DomainConstants.TYPE_PROJECT_SPACE,
                getProjectSelectList(),
                relSelectsList,
                true,
                false,
                (short) 1,
                "",
                "",
                0);
        if (projectList == null || projectList.isEmpty()) {
            return null;
        }
        return (Map) projectList.get(0);
    }

    /**
     * 判断当前文件夹是否为配置的研发技术文档根目录。
     */
    private boolean isProjectDocumentFolder(Map folderMap, String projectDocumentFolderName) {
        if (folderMap == null || folderMap.isEmpty() || UIUtil.isNullOrEmpty(projectDocumentFolderName)) {
            return false;
        }
        String folderName = getMapValue(folderMap, DomainConstants.SELECT_NAME);
        String folderTitle = getMapValue(folderMap, DomainConstants.SELECT_ATTRIBUTE_TITLE);
        return projectDocumentFolderName.equals(folderName) || projectDocumentFolderName.equals(folderTitle);
    }

    /**
     * 项目基础select，供批量初始化和发布触发器复用。
     */
    private StringList getProjectSelectList() {
        StringList projectSelects = new StringList();
        projectSelects.add(DomainConstants.SELECT_ID);
        projectSelects.add(DomainConstants.SELECT_NAME);
        projectSelects.add(DomainConstants.SELECT_DESCRIPTION);
        return projectSelects;
    }

    /**
     * 文件夹基础select，用于判断研发技术文档目录链路。
     */
    private StringList getWorkspaceVaultSelectList() {
        StringList folderSelects = new StringList();
        folderSelects.add(DomainConstants.SELECT_ID);
        folderSelects.add(DomainConstants.SELECT_NAME);
        folderSelects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        return folderSelects;
    }

    /**
     * 构造生成doc.json所需的文档select列表。
     */
    private StringList getProjectDocumentSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_TYPE);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_REVISION);
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add(DomainConstants.SELECT_DESCRIPTION);
        selects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        selects.add(SELECT_ATTR_JF_ConnProjectPhase);
        selects.add(SELECT_ATTR_JF_ProjectDocType);
        selects.add(SELECT_ATTR_JF_DocSpecialty);
        return selects;
    }

    /**
     * 按AI中转站约定结构生成单个文档的doc.json主体。
     */
    private JSONObject buildProjectDocumentJson(Context context, Map projectMap, Map documentMap) {
        JSONObject documentJson = new JSONObject();
        String docName = getMapValue(documentMap, DomainConstants.SELECT_ATTRIBUTE_TITLE);
        if (UIUtil.isNullOrEmpty(docName)) {
            docName = getMapValue(documentMap, DomainConstants.SELECT_NAME);
        }
        documentJson.put("project_id", getMapValue(projectMap, DomainConstants.SELECT_NAME));
        documentJson.put("project_name", getMapValue(projectMap, DomainConstants.SELECT_DESCRIPTION));
        documentJson.put("doc_id", getMapValue(documentMap, DomainConstants.SELECT_NAME));
        documentJson.put("doc_edition", getMapValue(documentMap, DomainConstants.SELECT_REVISION));
        documentJson.put("doc_name", docName);
        documentJson.put("doc_describe", getMapValue(documentMap, DomainConstants.SELECT_DESCRIPTION));
        documentJson.put("phase", getMapValue(documentMap, SELECT_ATTR_JF_ConnProjectPhase));
        documentJson.put("classification", getMapValue(documentMap, SELECT_ATTR_JF_ProjectDocType));
        documentJson.put("state", getMapValue(documentMap, DomainConstants.SELECT_CURRENT));
        documentJson.put("part", getMapValue(documentMap, SELECT_ATTR_JF_DocSpecialty));
        return documentJson;
    }

    private void validateFormalECRBIParams(String state, String startTime, String endTime) throws Exception {
        if (UIUtil.isNullOrEmpty(state)) {
            throw new Exception("State不能为空");
        }
        if (UIUtil.isNullOrEmpty(startTime)) {
            throw new Exception("StartTime不能为空");
        }
        if (UIUtil.isNullOrEmpty(endTime)) {
            throw new Exception("EndTime不能为空");
        }
    }

    /**
     * 校验ECR初始化BI接口参数，ProjectIds允许为空，States不能为空。
     **
     * @param programMap 请求参数
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 18:40
     */
    private void validateInitECRBIParams(Map programMap) throws Exception {
        List states = getECRBIStringListParam(programMap, "States");
        if (states.isEmpty()) {
            throw new Exception("States不能为空");
        }
    }

    private String normalizeFormalECRBIStartTime(String startTime) {
        if (isFormalECRBIDateOnly(startTime)) {
            return startTime + " 12:00:00 AM";
        }
        return startTime;
    }

    private String normalizeFormalECRBIEndTime(String endTime) {
        if (isFormalECRBIDateOnly(endTime)) {
            return endTime + " 11:59:59 PM";
        }
        return endTime;
    }

    private boolean isFormalECRBIDateOnly(String time) {
        return UIUtil.isNotNullAndNotEmpty(time)
                && !time.contains(":")
                && !time.toUpperCase().contains("AM")
                && !time.toUpperCase().contains("PM");
    }

    private String buildFormalECRBIWhere(String state, String startTime, String endTime) {
        String stateActualSelect = "state[" + escapeWhereValue(state) + "].actual";
        return "current == '" + escapeWhereValue(state) + "'"
                + " && '" + stateActualSelect + "' >= '" + escapeWhereValue(startTime) + "'"
                + " && '" + stateActualSelect + "' <= '" + escapeWhereValue(endTime) + "'";
    }

    private List getECRBIStringListParam(Map programMap, String key) {
        List paramList = new ArrayList();
        if (programMap == null || UIUtil.isNullOrEmpty(key)) {
            return paramList;
        }
        Object paramValue = programMap.get(key);
        if (paramValue instanceof List) {
            List valueList = (List) paramValue;
            for (int i = 0; i < valueList.size(); i++) {
                addECRBIStringParam(paramList, valueList.get(i));
            }
        } else {
            addECRBIStringParam(paramList, paramValue);
        }
        return paramList;
    }

    private void addECRBIStringParam(List paramList, Object value) {
        if (value == null) {
            return;
        }
        String strValue = String.valueOf(value).trim();
        if (UIUtil.isNotNullAndNotEmpty(strValue) && !paramList.contains(strValue)) {
            paramList.add(strValue);
        }
    }

    /**
     * 构建ECR初始化BI查询条件，ProjectIds为空时按状态查询全部项目。
     **
     * @param projectIds 项目号集合
     * @param states 状态集合
     * @return String 查询条件
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 18:40
     */
    private String buildInitECRBIWhere(List projectIds, List states) {
        String where = "(" + buildECRBIInWhere(DomainConstants.SELECT_CURRENT, states) + ")";
        if (!projectIds.isEmpty()) {
            where += " && (" + buildECRBIInWhere(SELECT_ECR_PROJECT_NAME, projectIds) + ")";
        }
        return where;
    }

    private String buildECRBIInWhere(String select, List values) {
        StringBuilder whereBuilder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (whereBuilder.length() > 0) {
                whereBuilder.append(" || ");
            }
            whereBuilder.append(select).append(" == '").append(escapeWhereValue(String.valueOf(values.get(i)))).append("'");
        }
        return whereBuilder.toString();
    }

    private StringList getInitECRBISelectList(List states) {
        String firstState = String.valueOf(states.get(0));
        StringList selects = getFormalECRBISelectList(firstState);
        for (int i = 0; i < states.size(); i++) {
            String stateActualSelect = "state[" + String.valueOf(states.get(i)) + "].actual";
            if (!selects.contains(stateActualSelect)) {
                selects.add(stateActualSelect);
            }
        }
        return selects;
    }

    private StringList getFormalECRBISelectList(String state) {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_OWNER);
        selects.add(DomainConstants.SELECT_TYPE);
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        selects.add(DomainConstants.SELECT_DESCRIPTION);
        selects.add(DomainConstants.SELECT_ORIGINATED);
        selects.add(SELECT_MODIFIED_VALUE);
        selects.add("state[" + state + "].actual");
        selects.add(SELECT_ECR_PROJECT_ID);
        selects.add(SELECT_ECR_PROJECT_NAME);
        selects.add(SELECT_ECR_PROJECT_DESCRIPTION);
        selects.add(SELECT_ATTR_JFECRCHANGETYPE);
        selects.add(SELECT_ATTR_JFCUSTOMERCHANGENUM);
        selects.add(SELECT_ATTR_JFEXPECTEDLAUNCHTIME);
        selects.add(SELECT_ATTR_JFISLASTQUOTE);
        selects.add(SELECT_ATTR_JFISTKODATA);
        selects.add(SELECT_ATTR_JFCHANGESOURCE);
        selects.add("attribute[" + ATTR_JFISPLATFORMPART + "]");
        selects.add(SELECT_ATTR_JFAFFECTSFACTORY);
        selects.add(SELECT_ECR_CHANGE_EVENT_TYPE);
        selects.add(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS);
        selects.add(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL);
        selects.add(SELECT_ATTR_JFQQID);
        selects.add(SELECT_ATTR_JFAssociatedOtherProjects);
        selects.add(SELECT_ATTR_JFAFFECTEDPROJECT);
        selects.add(SELECT_ATTR_JFCHANGERESON);
        selects.add(SELECT_ECR_CONNECT_ECO_NAME);
        selects.add(SELECT_ATTR_JFECRTYPE);
        selects.add(SELECT_ATTR_JFBREAKPOINTMODE);
        selects.add(SELECT_ATTR_JFChangeCyclesSelfMake);
        selects.add(SELECT_ATTR_JFChangeCyclesOutsourcing);
        selects.add(SELECT_ATTR_JFChangesFixtures);
        selects.add(SELECT_ATTR_JFChangesFixturesExternal);
        selects.add(SELECT_ATTR_JFCostOfTrial);
        selects.add(SELECT_ATTR_JFCostOfTrialExternal);
        selects.add(SELECT_ATTR_JFCostOfQuality);
        selects.add(SELECT_ATTR_JFCostOfQualityExternal);
        selects.add(SELECT_ATTR_JFCostPackagingLogistics);
        selects.add(SELECT_ATTR_JFCostPackagingLogisticsExternal);
        selects.add(SELECT_ATTR_JFMaterialsFinishedProductsRework);
        selects.add(SELECT_ATTR_JFChangesInvestmentsWholeChair);
        selects.add(SELECT_ATTR_JFChangesInvestmentsFaceCovers);
        selects.add(SELECT_ATTR_JFChangesInvestmentsFoaming);
        selects.add(SELECT_ATTR_JFChangesDevelopment);
        selects.add(SELECT_ATTR_JFChangesInvestment);
        selects.add(SELECT_ATTR_JFChangesMould);
        selects.add(SELECT_ATTR_JFSumInventoryScrapAmount);
        selects.add(SELECT_ATTR_JFTestFee);
        selects.add(SELECT_ATTR_JFOtherFee);
        selects.add(SELECT_ATTR_JFAPRNotes);
        selects.add(SELECT_ATTR_JFProjectPhase);
        return selects;
    }

    private JSONObject buildFormalECRBIJson(Context context, Map ecrMap) throws Exception {
        JSONObject ecrJson = new JSONObject();
        String ecrId = getMapValue(ecrMap, DomainConstants.SELECT_ID);
        ecrJson.put("Owner", getMapValue(ecrMap, DomainConstants.SELECT_OWNER));
        ecrJson.put("Type", getMapValue(ecrMap, DomainConstants.SELECT_TYPE));
        ecrJson.put("state", getMapValue(ecrMap, DomainConstants.SELECT_CURRENT));
        ecrJson.put("JFProjectId", getMapValue(ecrMap, SELECT_ECR_PROJECT_NAME));
        ecrJson.put("JFProjectName", getMapValue(ecrMap, SELECT_ECR_PROJECT_DESCRIPTION));
        ecrJson.put("JFProjectPhase", getMapValue(ecrMap, SELECT_ATTR_JFProjectPhase));
        ecrJson.put("JFProjectLevel", getFormalECRProjectLevel(context, ecrMap));
        ecrJson.put("Name", getMapValue(ecrMap, DomainConstants.SELECT_NAME));
        ecrJson.put("Title", getMapValue(ecrMap, DomainConstants.SELECT_ATTRIBUTE_TITLE));
        ecrJson.put("JFECRChangeType", getMapValue(ecrMap, SELECT_ATTR_JFECRCHANGETYPE));
        ecrJson.put("JFCustomerChangeNum", getMapValue(ecrMap, SELECT_ATTR_JFCUSTOMERCHANGENUM));
        ecrJson.put("JFExpectedLaunchTime", getMapValue(ecrMap, SELECT_ATTR_JFEXPECTEDLAUNCHTIME));
        ecrJson.put("JFIsLastQuote", getMapValue(ecrMap, SELECT_ATTR_JFISLASTQUOTE));
        ecrJson.put("JFIsTKOData", getMapValue(ecrMap, SELECT_ATTR_JFISTKODATA));
        ecrJson.put("JFChangeSource", getMapValue(ecrMap, SELECT_ATTR_JFCHANGESOURCE));
        ecrJson.put("JFIsPlatformPart", getMapValue(ecrMap, "attribute[" + ATTR_JFISPLATFORMPART + "]"));
        ecrJson.put("JFAffectsFactory", getMapValue(ecrMap, SELECT_ATTR_JFAFFECTSFACTORY));
        ecrJson.put("JFChangeEventType", getMapValue(ecrMap, SELECT_ECR_CHANGE_EVENT_TYPE));
        ecrJson.put("JFChangesDeveExpensesManHours", getMapValue(ecrMap, SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS));
        ecrJson.put("JFChangesDeveExpensesManHoursExternal", getMapValue(ecrMap, SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL));
        ecrJson.put("JFQQ", getMapValue(ecrMap, SELECT_ATTR_JFQQID));
        ecrJson.put("JFAssociatedOtherProjects", getMapValue(ecrMap, SELECT_ATTR_JFAssociatedOtherProjects));
        ecrJson.put("JFAffectedProject", getMapValue(ecrMap, SELECT_ATTR_JFAFFECTEDPROJECT));
        ecrJson.put("JFChangeReson", getMapValue(ecrMap, SELECT_ATTR_JFCHANGERESON));
        ecrJson.put("Description", getMapValue(ecrMap, DomainConstants.SELECT_DESCRIPTION));
        ecrJson.put("JFECRECO", getMapValue(ecrMap, SELECT_ECR_CONNECT_ECO_NAME));
        ecrJson.put("JFECRType", getMapValue(ecrMap, SELECT_ATTR_JFECRTYPE));
        ecrJson.put("JFBreakpointMode", getMapValue(ecrMap, SELECT_ATTR_JFBREAKPOINTMODE));
        ecrJson.put("JFChangeCyclesSelfMake", getMapValue(ecrMap, SELECT_ATTR_JFChangeCyclesSelfMake));
        ecrJson.put("JFChangeCyclesOutsourcing", getMapValue(ecrMap, SELECT_ATTR_JFChangeCyclesOutsourcing));
        ecrJson.put("JFChangesFixtures", getMapValue(ecrMap, SELECT_ATTR_JFChangesFixtures));
        ecrJson.put("JFChangesFixturesExternal", getMapValue(ecrMap, SELECT_ATTR_JFChangesFixturesExternal));
        ecrJson.put("JFCostOfTrial", getMapValue(ecrMap, SELECT_ATTR_JFCostOfTrial));
        ecrJson.put("JFCostOfTrialExternal", getMapValue(ecrMap, SELECT_ATTR_JFCostOfTrialExternal));
        ecrJson.put("JFCostOfQuality", getMapValue(ecrMap, SELECT_ATTR_JFCostOfQuality));
        ecrJson.put("JFCostOfQualityExternal", getMapValue(ecrMap, SELECT_ATTR_JFCostOfQualityExternal));
        ecrJson.put("JFCostPackagingLogistics", getMapValue(ecrMap, SELECT_ATTR_JFCostPackagingLogistics));
        ecrJson.put("JFCostPackagingLogisticsExternal", getMapValue(ecrMap, SELECT_ATTR_JFCostPackagingLogisticsExternal));
        ecrJson.put("JFMaterialsFinishedProductsRework", getMapValue(ecrMap, SELECT_ATTR_JFMaterialsFinishedProductsRework));
        ecrJson.put("JFChangesInvestmentsWholeChair", getMapValue(ecrMap, SELECT_ATTR_JFChangesInvestmentsWholeChair));
        ecrJson.put("JFChangesInvestmentsFaceCovers", getMapValue(ecrMap, SELECT_ATTR_JFChangesInvestmentsFaceCovers));
        ecrJson.put("JFChangesInvestmentsFoaming", getMapValue(ecrMap, SELECT_ATTR_JFChangesInvestmentsFoaming));
        ecrJson.put("JFChangesDevelopment", getMapValue(ecrMap, SELECT_ATTR_JFChangesDevelopment));
        ecrJson.put("JFChangesInvestment", getMapValue(ecrMap, SELECT_ATTR_JFChangesInvestment));
        ecrJson.put("JFChangesMould", getMapValue(ecrMap, SELECT_ATTR_JFChangesMould));
        ecrJson.put("JFSumInventoryScrapAmount", getMapValue(ecrMap, SELECT_ATTR_JFSumInventoryScrapAmount));
        ecrJson.put("JFTestFee", getMapValue(ecrMap, SELECT_ATTR_JFTestFee));
        ecrJson.put("JFOtherFee", getMapValue(ecrMap, SELECT_ATTR_JFOtherFee));
        ecrJson.put("JFAPRNotes", getMapValue(ecrMap, SELECT_ATTR_JFAPRNotes));
        ecrJson.put("modified", getMapValue(ecrMap, SELECT_MODIFIED_VALUE));
        ecrJson.put("Tasks", getFormalECRTaskArray(context, ecrId));
        String ecrType = getMapValue(ecrMap, DomainConstants.SELECT_TYPE);
        MapList tableDataList = getECRBITableData(context, ecrId, ecrType);
        ecrJson.put("Parts", buildFormalECRPartArray(tableDataList, ecrType));
        ecrJson.put("BOM", buildFormalECRBOMArray(context, tableDataList, ecrType));
        return ecrJson;
    }

    private String getFormalECRProjectLevel(Context context, Map ecrMap) throws Exception {
        String projectLevelCN = "";
        String projectSpaceId = getMapValue(ecrMap, SELECT_ECR_PROJECT_ID);
        if (UIUtil.isNotNullAndNotEmpty(projectSpaceId)) {
            DomainObject projectSpaceObj = DomainObject.newInstance(context, projectSpaceId);
            String projectLevel = projectSpaceObj.getAttributeValue(context, "JFProjectLevel");
            if (UIUtil.isNotNullAndNotEmpty(projectLevel)) {
                projectLevelCN = EnoviaResourceBundle.getRangeI18NString(context, "JFProjectLevel", projectLevel, context.getSession().getLanguage());
            }
        }
        return projectLevelCN;
    }

    private JSONArray getFormalECRTaskArray(Context context, String ecrId) throws Exception {
        JSONArray taskArray = new JSONArray();
        if (UIUtil.isNullOrEmpty(ecrId)) {
            return taskArray;
        }
        DomainObject ecrObj = DomainObject.newInstance(context, ecrId);
        MapList taskList = ecrObj.getRelatedObjects(context,
                RELATIONSHIP_JF_ECR_TASK,
                TYPE_JS_SIGN_TASK+","+TYPE_JF_APRTask+","+TYPE_JF_CustomerTask,
                getFormalECRTaskSelectList(),
                relSelectsList,
                false,
                true,
                (short) 1,
                "",
                "",
                0);
        if (taskList == null || taskList.isEmpty()) {
            return taskArray;
        }
        for (int i = 0; i < taskList.size(); i++) {
            Map taskMap = (Map) taskList.get(i);
            JSONObject taskJson = new JSONObject();
            taskJson.put("TaskName", getMapValue(taskMap, DomainConstants.SELECT_NAME));
            taskJson.put("TaskTitle", getMapValue(taskMap, DomainConstants.SELECT_ATTRIBUTE_TITLE));
            taskJson.put("TaskState", getMapValue(taskMap, DomainConstants.SELECT_CURRENT));
            taskJson.put("TaskSubmitDate", getMapValue(taskMap, SELECT_TASK_REVIEW_ACTUAL));
            taskJson.put("TaskReviewDate", getMapValue(taskMap, SELECT_TASK_COMPLETE_ACTUAL));
            taskJson.put("TaskOwner", getMapValue(taskMap, DomainConstants.SELECT_OWNER));
            taskJson.put("APRNotes", getMapValue(taskMap, DomainConstants.SELECT_DESCRIPTION));
            taskArray.add(taskJson);
        }
        return taskArray;
    }

    private StringList getFormalECRTaskSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add(DomainConstants.SELECT_OWNER);
        selects.add(DomainConstants.SELECT_DESCRIPTION);
        selects.add(SELECT_TASK_REVIEW_ACTUAL);
        selects.add(SELECT_TASK_COMPLETE_ACTUAL);
        return selects;
    }

    private MapList getECRBITableData(Context context, String ecrId, String ecrType) throws Exception {
        MapList tableDataList = new MapList();
        Set<String> existKeySet = new LinkedHashSet<>();
        if (TYPE_JFFormalECR.equals(ecrType)) {
            addECRTableData(context, ecrId, ecrType, "JF_FormalECRService", "JFFormalECRCosting", "getFormalECRBuyTableData", tableDataList, existKeySet);
            addECRTableData(context, ecrId, ecrType, "JF_FormalECRService", "JFFormalECRController", "getFormalECRMakeTableData", tableDataList, existKeySet);
        } else if (TYPE_JFNewECR.equals(ecrType)) {
            addECRTableData(context, ecrId, ecrType, "JF_NewECRService", "JFNewECRCosting", "getNewECRBuyTableData", tableDataList, existKeySet);
            addECRTableData(context, ecrId, ecrType, "JF_NewECRService", "JFNewECRController", "getNewECRMakeTableData", tableDataList, existKeySet);
        } else if (TYPE_JFECR.equals(ecrType)) {
            addECRTableData(context, ecrId, ecrType, "JF_ECRService", "JFECRCosting", "getECRCostingTableData", tableDataList, existKeySet);
            addECRTableData(context, ecrId, ecrType, "JF_ECRService", "JFECRController", "getECRMakeTableData", tableDataList, existKeySet);
        }
        return tableDataList;
    }

    private void addECRTableData(Context context, String ecrId, String ecrType, String programName, String tableName, String methodName, MapList targetList, Set<String> existKeySet) throws Exception {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(STRING_OBJECTID, ecrId);
        parameters.put(STRING_SELECT_TABLE, tableName);
        MapList tableData = (MapList) JPO.invoke(context, programName, null, methodName, JPO.packArgs(parameters), MapList.class);
        if (tableData == null || tableData.isEmpty()) {
            return;
        }
        for (int i = 0; i < tableData.size(); i++) {
            Map rowMap = (Map) tableData.get(i);
            enrichECRBIRowData(context, ecrId, ecrType, rowMap);
            String rowKey = getMapValue(rowMap, DomainConstants.SELECT_ID) + "|" + getMapValue(rowMap, SELECT_CONNECTION_ID_VALUE);
            if (existKeySet.contains(rowKey)) {
                continue;
            }
            existKeySet.add(rowKey);
            targetList.add(rowMap);
        }
    }

    private void enrichECRBIRowData(Context context, String ecrId, String ecrType, Map rowMap) throws Exception {
        String partId = getMapValue(rowMap, DomainConstants.SELECT_ID);
        if (UIUtil.isNullOrEmpty(partId)) {
            return;
        }
        if (UIUtil.isNullOrEmpty(getMapValue(rowMap, DomainConstants.SELECT_TYPE))
                || UIUtil.isNullOrEmpty(getMapValue(rowMap, DomainConstants.SELECT_NAME))
                || UIUtil.isNullOrEmpty(getMapValue(rowMap, DomainConstants.SELECT_CURRENT))) {
            DomainObject partObj = DomainObject.newInstance(context, partId);
            Map partInfo = partObj.getInfo(context, getECRBIPartBasicSelectList());
            for (Object entryObj : partInfo.entrySet()) {
                Map.Entry entry = (Map.Entry) entryObj;
                String key = String.valueOf(entry.getKey());
                if (UIUtil.isNullOrEmpty(getMapValue(rowMap, key))) {
                    rowMap.put(key, entry.getValue());
                }
            }
        }
        if (TYPE_JFECR.equals(ecrType)) {
            enrichJFECRPriceRelationData(context, ecrId, rowMap);
        }
    }

    private StringList getECRBIPartBasicSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_TYPE);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_REVISION);
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add(SELECT_ATTR_V_PART_NUMBER);
        selects.add(SELECT_ATTR_JFPartType);
        selects.add(SELECT_ATTR_JF_PartNameCN);
        selects.add(SELECT_ATTR_JF_PartNameEN);
        selects.add(SELECT_ATTR_JF_ProcurementType);
        selects.add(SELECT_ATTR_JFDIRECT_BUY);
        selects.add(SELECT_ATTR_JFFREEState);
        selects.add("attribute[" + Attr_JF_IsBubbling + "]");
        return selects;
    }

    private void enrichJFECRPriceRelationData(Context context, String ecrId, Map rowMap) throws Exception {
        String partId = getMapValue(rowMap, DomainConstants.SELECT_ID);
        if (UIUtil.isNullOrEmpty(partId)) {
            return;
        }
        DomainObject partObj = DomainObject.newInstance(context, partId);
        enrichJFECRPriceRelationData(context, ecrId, partObj, rowMap, REL_JFECR2PartPrice, getECRBICostingLegacyPriceAttrs());
        enrichJFECRPriceRelationData(context, ecrId, partObj, rowMap, "JFECR2MakePartPrice", getECRBIMakeLegacyPriceAttrs());
    }

    private void enrichJFECRPriceRelationData(Context context, String ecrId, DomainObject partObj, Map rowMap, String relName, StringList attrList) throws Exception {
        StringList relSelects = new StringList(DomainRelationship.SELECT_ID);
        for (int i = 0; i < attrList.size(); i++) {
            relSelects.add("attribute[" + attrList.get(i) + "]");
        }
        MapList priceList = partObj.getRelatedObjects(context,
                relName,
                TYPE_JFECR,
                new StringList(DomainConstants.SELECT_ID),
                relSelects,
                true,
                false,
                (short) 1,
                "id==" + ecrId,
                "",
                (short) 0);
        if (priceList == null || priceList.isEmpty()) {
            return;
        }
        Map priceMap = (Map) priceList.get(0);
        for (int i = 0; i < attrList.size(); i++) {
            String attrName = (String) attrList.get(i);
            String value = getMapValue(priceMap, "attribute[" + attrName + "]");
            if (UIUtil.isNotNullAndNotEmpty(value)) {
                rowMap.put("to[" + relName + "].attribute[" + attrName + "]", value);
            }
        }
    }

    private StringList getECRBICostingLegacyPriceAttrs() {
        StringList attrs = new StringList();
        attrs.add("JFChangeUnitPrice");
        attrs.add("JFChangeMold");
        attrs.add("JFChangeUnitPriceExternal");
        attrs.add("JFChangeMoldCostExternal");
        attrs.add("JFChangeUnitPriceCost");
        attrs.add("JFChangeMoldCost");
        attrs.add("JFChangeUnitPriceCostExternal");
        attrs.add("JFChangeMoldPriceCostExternal");
        attrs.add("JFStagnationOfSuppliersInternal");
        attrs.add("JFStagnationOfSuppliersExternal");
        attrs.add("JFChangesTrialExpensesManHours");
        attrs.add("JFChangesTrialExpensesManHoursExternal");
        return attrs;
    }

    private StringList getECRBIMakeLegacyPriceAttrs() {
        StringList attrs = new StringList();
        attrs.add("JFChangeMan-hour");
        attrs.add("JFChangeMan-hourExternal");
        attrs.add("JFChangeSeatCost");
        attrs.add("JFChangeSeatCostExternal");
        attrs.add("JFChangeTargetPrice");
        attrs.add("JFChangeTargetPriceExternal");
        attrs.add("JFChangeWholeSeatPrice");
        attrs.add("JFChangeWholeSeatPriceExternal");
        attrs.add("JFChangeMold");
        attrs.add("JFChangeMoldCostExternal");
        attrs.add("JFChangeMoldCost");
        attrs.add("JFChangeMoldPriceCostExternal");
        attrs.add("JFChangeUnitPriceCost");
        attrs.add("JFChangeUnitPriceCostExternal");
        return attrs;
    }

    private JSONArray buildFormalECRPartArray(MapList tableDataList, String ecrType) {
        JSONArray partArray = new JSONArray();
        if (tableDataList == null || tableDataList.isEmpty()) {
            return partArray;
        }
        Map<String, String> onePartMap = buildFormalECROnePartMap(tableDataList);
        Map<String, JSONObject> partJsonMap = new LinkedHashMap<>();
        for (int i = 0; i < tableDataList.size(); i++) {
            Map partMap = (Map) tableDataList.get(i);
            if (!TYPE_VPMREFERENCE.equals(getMapValue(partMap, DomainConstants.SELECT_TYPE))) {
                continue;
            }
            String partId = getMapValue(partMap, DomainConstants.SELECT_ID);
            if (UIUtil.isNullOrEmpty(partId) || partJsonMap.containsKey(partId)) {
                continue;
            }
            JSONObject partJson = new JSONObject();
            partJson.put("PartNumber", getMapValue(partMap, SELECT_ATTR_V_PART_NUMBER));
            partJson.put("PartName", getMapValue(partMap, DomainConstants.SELECT_NAME));
            partJson.put("PartRevision", getMapValue(partMap, DomainConstants.SELECT_REVISION));
            partJson.put("state", getMapValue(partMap, DomainConstants.SELECT_CURRENT));
            partJson.put("JF_PartType", getMapValue(partMap, SELECT_ATTR_JFPartType));
            partJson.put("PartNameCN", getMapValue(partMap, SELECT_ATTR_JF_PartNameCN));
            partJson.put("PartNameEN", getMapValue(partMap, SELECT_ATTR_JF_PartNameEN));
            partJson.put("ProcurementType", getMapValue(partMap, SELECT_ATTR_JF_ProcurementType));
            partJson.put("JF_freeState", getMapValue(partMap, SELECT_ATTR_JFFREEState));
            partJson.put("JF_OnePart", getFormalECROnePartValue(onePartMap, partId));
            partJson.put("DirectBuy", getMapValue(partMap, SELECT_ATTR_JFDIRECT_BUY));
            partJson.put("JFChangeResource", getFormalECRChangeResource(partMap));
            partJson.put("JF_ChangesTrialExpensesManHours", getECRBIPriceValue(partMap, ecrType, REL_JFECR2PartPrice, SELECT_ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHours, "JFChangesTrialExpensesManHours"));
            partJson.put("JF_StagnationOfSuppliersInternal", getECRBIPriceValue(partMap, ecrType, REL_JFECR2PartPrice, "attribute[JF_VPMReferenceCost.JF_StagnationOfSuppliersInternal]", "JFStagnationOfSuppliersInternal"));
            partJson.put("JF_ChangeUnitPrice", getECRBIPriceValue(partMap, ecrType, REL_JFECR2PartPrice, SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice, "JFChangeUnitPrice"));
            partJson.put("JF_ChangeMold", getECRBIPriceValue(partMap, ecrType, REL_JFECR2PartPrice, SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMold, "JFChangeMold"));
            partJson.put("JF_ChangeUnitPriceCost", getECRBIPriceValue(partMap, ecrType, REL_JFECR2PartPrice, SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCost, "JFChangeUnitPriceCost"));
            partJson.put("JF_ChangeMoldCost", getECRBIPriceValue(partMap, ecrType, REL_JFECR2PartPrice, SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldCost, "JFChangeMoldCost"));
            partJson.put("JF_ChangesTrialExpensesManHoursExternal", getECRBIPriceValue(partMap, ecrType, REL_JFECR2PartPrice, SELECT_ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHoursExternal, "JFChangesTrialExpensesManHoursExternal"));
            partJson.put("JF_StagnationOfSuppliersExternal", getECRBIPriceValue(partMap, ecrType, REL_JFECR2PartPrice, "attribute[JF_VPMReferenceCost.JF_StagnationOfSuppliersExternal]", "JFStagnationOfSuppliersExternal"));
            partJson.put("JF_ChangeUnitPriceExternal", getECRBIPriceValue(partMap, ecrType, REL_JFECR2PartPrice, SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceExternal, "JFChangeUnitPriceExternal"));
            partJson.put("JF_ChangeMoldCostExternal", getECRBIPriceValue(partMap, ecrType, REL_JFECR2PartPrice, SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal, "JFChangeMoldCostExternal"));
            partJson.put("JF_ChangeUnitPriceCostExternal", getECRBIPriceValue(partMap, ecrType, REL_JFECR2PartPrice, SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal, "JFChangeUnitPriceCostExternal"));
            partJson.put("JF_ChangeMoldPriceCostExternal", getECRBIPriceValue(partMap, ecrType, REL_JFECR2PartPrice, SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldPriceCostExternal, "JFChangeMoldPriceCostExternal"));
            partJson.put("JF_ChangeMan_hour", getECRBIPriceValue(partMap, ecrType, "JFECR2MakePartPrice", SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeManHour, "JFChangeMan-hour"));
            partJson.put("JF_ChangeMan_hourExternal", getECRBIPriceValue(partMap, ecrType, "JFECR2MakePartPrice", SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeManHourExternal, "JFChangeMan-hourExternal"));
            partJson.put("JF_ChangeSeatCost", getECRBIPriceValue(partMap, ecrType, "JFECR2MakePartPrice", SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeSeatCost, "JFChangeSeatCost"));
            partJson.put("JF_ChangeSeatCostExternal", getECRBIPriceValue(partMap, ecrType, "JFECR2MakePartPrice", SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeSeatCostExternal, "JFChangeSeatCostExternal"));
            partJson.put("JF_ChangeTargetPrice", getECRBIPriceValue(partMap, ecrType, "JFECR2MakePartPrice", SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeTargetPrice, "JFChangeTargetPrice"));
            partJson.put("JF_ChangeWholeSeatPrice", getECRBIPriceValue(partMap, ecrType, "JFECR2MakePartPrice", SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPrice, "JFChangeWholeSeatPrice"));
            partJson.put("JF_ChangeWholeSeatPriceExternal", getECRBIPriceValue(partMap, ecrType, "JFECR2MakePartPrice", SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPriceExternal, "JFChangeWholeSeatPriceExternal"));
            partJson.put("JF_IsBubbling", getMapValue(partMap, "attribute[" + Attr_JF_IsBubbling + "]"));
            partJsonMap.put(partId, partJson);
        }
        for (JSONObject partJson : partJsonMap.values()) {
            partArray.add(partJson);
        }
        return partArray;
    }

    private String getECRBIPriceValue(Map partMap, String ecrType, String legacyRelName, String formalSelect, String legacyAttrName) {
        if (TYPE_JFFormalECR.equals(ecrType)) {
            return getMapValue(partMap, formalSelect);
        }
        String value = getMapValue(partMap, "tomid[" + legacyRelName + "].attribute[" + legacyAttrName + "]");
        if (UIUtil.isNullOrEmpty(value)) {
            value = getMapValue(partMap, "to[" + legacyRelName + "].attribute[" + legacyAttrName + "]");
        }
        if (UIUtil.isNullOrEmpty(value) && partMap != null) {
            Set keySet = partMap.keySet();
            for (Object keyObj : keySet) {
                String key = String.valueOf(keyObj);
                if (key.contains(legacyRelName) && key.contains("attribute[" + legacyAttrName + "]")) {
                    value = getMapValue(partMap, key);
                    if (UIUtil.isNotNullAndNotEmpty(value)) {
                        break;
                    }
                }
            }
        }
        return value;
    }

    private Map<String, String> buildFormalECROnePartMap(MapList tableDataList) {
        Map<String, String> onePartMap = new HashMap<>();
        if (tableDataList == null || tableDataList.isEmpty()) {
            return onePartMap;
        }
        for (int i = 0; i < tableDataList.size(); i++) {
            Map partMap = (Map) tableDataList.get(i);
            String partId = getMapValue(partMap, DomainConstants.SELECT_ID);
            if (TYPE_VPMREFERENCE.equals(getMapValue(partMap, DomainConstants.SELECT_TYPE)) && UIUtil.isNotNullAndNotEmpty(partId)) {
                onePartMap.put(partId, "N");
            }
        }
        for (int i = 0; i < tableDataList.size(); i++) {
            Map rootMap = (Map) tableDataList.get(i);
            if (!isFormalECRSupplyRoot(rootMap)) {
                continue;
            }
            int subtreeEnd = findFormalECRSubtreeEnd(tableDataList, i);
            for (int j = i + 1; j < subtreeEnd;) {
                Map childMap = (Map) tableDataList.get(j);
                if (!isFormalECROnePartCandidate(childMap)) {
                    j++;
                    continue;
                }
                String childId = getMapValue(childMap, DomainConstants.SELECT_ID);
                if (UIUtil.isNotNullAndNotEmpty(childId)) {
                    onePartMap.put(childId, "Y");
                }
                j = findFormalECRSubtreeEnd(tableDataList, j);
            }
        }
        return onePartMap;
    }

    private int findFormalECRSubtreeEnd(MapList tableDataList, int rootIndex) {
        Map rootMap = (Map) tableDataList.get(rootIndex);
        int rootLevel = getFormalECRLevel(rootMap);
        for (int i = rootIndex + 1; i < tableDataList.size(); i++) {
            Map nextMap = (Map) tableDataList.get(i);
            if (getFormalECRLevel(nextMap) <= rootLevel) {
                return i;
            }
        }
        return tableDataList.size();
    }

    private boolean isFormalECRSupplyRoot(Map partMap) {
        return TYPE_VPMREFERENCE.equals(getMapValue(partMap, DomainConstants.SELECT_TYPE))
                && "N".equalsIgnoreCase(getMapValue(partMap, SELECT_ATTR_JFFREEState));
    }

    private boolean isFormalECROnePartCandidate(Map partMap) {
        if (!TYPE_VPMREFERENCE.equals(getMapValue(partMap, DomainConstants.SELECT_TYPE))) {
            return false;
        }
        String procurementType = getMapValue(partMap, SELECT_ATTR_JF_ProcurementType);
        return ATTR_JF_ProcurementType_RANGE_BUY.equalsIgnoreCase(procurementType)
                || ATTR_JF_ProcurementType_RANGE_ICO.equalsIgnoreCase(procurementType);
    }

    private String getFormalECROnePartValue(Map<String, String> onePartMap, String partId) {
        if (onePartMap == null || UIUtil.isNullOrEmpty(partId)) {
            return "N";
        }
        String value = onePartMap.get(partId);
        return UIUtil.isNotNullAndNotEmpty(value) ? value : "N";
    }

    private JSONArray buildFormalECRBOMArray(Context context, MapList tableDataList, String ecrType) throws Exception {
        JSONArray bomArray = new JSONArray();
        if (tableDataList == null || tableDataList.isEmpty()) {
            return bomArray;
        }
        for (int i = 0; i < tableDataList.size(); i++) {
            Map childMap = (Map) tableDataList.get(i);
            if (!TYPE_VPMREFERENCE.equals(getMapValue(childMap, DomainConstants.SELECT_TYPE))) {
                continue;
            }
            Map parentMap = findFormalECRParentPartMap(tableDataList, i);
            if (parentMap == null) {
                continue;
            }
            JSONObject bomJson = new JSONObject();
            bomJson.put("ParentName", getMapValue(parentMap, DomainConstants.SELECT_NAME));
            bomJson.put("ParentRevision", getMapValue(parentMap, DomainConstants.SELECT_REVISION));
            bomJson.put("ChildName", getMapValue(childMap, DomainConstants.SELECT_NAME));
            bomJson.put("ChildRevision", getMapValue(childMap, DomainConstants.SELECT_REVISION));
            bomJson.put("JF_BOMChangeDes", getMapValue(childMap, SELECT_ATTRIBUTE_JF_BOMChangeDes));
            bomJson.put("JF_ChangeBeforeRev", getFormalECRBeforeRevision(context, getMapValue(childMap, SELECT_ATTRIBUTE_JF_ChangeBeforeRev)));
            bomJson.put("JF_BOMQuantity", getMapValue(childMap, SELECT_ATTRIBUTE_JF_BOMQuantity));
            bomJson.put("JFBOMIncrement", calculateFormalECRBOMIncrement(context, childMap, ecrType));
            bomArray.add(bomJson);
        }
        return bomArray;
    }

    private Map findFormalECRParentPartMap(MapList tableDataList, int childIndex) {
        Map childMap = (Map) tableDataList.get(childIndex);
        int childLevel = getFormalECRLevel(childMap);
        for (int i = childIndex - 1; i >= 0; i--) {
            Map parentMap = (Map) tableDataList.get(i);
            if (getFormalECRLevel(parentMap) < childLevel) {
                return parentMap;
            }
        }
        return null;
    }

    private int getFormalECRLevel(Map partMap) {
        try {
            return Integer.parseInt(getMapValue(partMap, SELECT_LEVEL_VALUE));
        } catch (Exception e) {
            return 0;
        }
    }

    private String getFormalECRChangeResource(Map partMap) {
        String changeResource = getMapValue(partMap, "to[JFRelateItem].attribute[JFChangeSource]");
        if (UIUtil.isNullOrEmpty(changeResource)) {
            changeResource = getMapValue(partMap, "to[JFRelateItem].attribute[JFChangeSource].value");
        }
        if (UIUtil.isNullOrEmpty(changeResource) && partMap != null) {
            Set keySet = partMap.keySet();
            for (Object keyObj : keySet) {
                String key = String.valueOf(keyObj);
                if (key.contains("to[" + RELATIONSHIP_JFRelateItem) && key.contains("attribute[JFChangeSource]")) {
                    changeResource = getMapValue(partMap, key);
                    if (UIUtil.isNotNullAndNotEmpty(changeResource)) {
                        break;
                    }
                }
            }
        }
        return changeResource;
    }

    private String getFormalECRBeforeRevision(Context context, String beforeId) {
        if (UIUtil.isNullOrEmpty(beforeId)) {
            return "";
        }
        try {
            DomainObject beforeObj = DomainObject.newInstance(context, beforeId);
            return beforeObj.getInfo(context, DomainConstants.SELECT_REVISION);
        } catch (Exception e) {
            logger.info("getFormalECRBeforeRevision failed, beforeId:{}", beforeId, e);
            return beforeId;
        }
    }

    private String calculateFormalECRBOMIncrement(Context context, Map partMap, String ecrType) {
        String currentPriceValue = getECRBIPriceValue(partMap, ecrType, REL_JFECR2PartPrice, SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost, "JFChangeUnitPriceCost");
        if (UIUtil.isNullOrEmpty(currentPriceValue)) {
            currentPriceValue = getECRBIPriceValue(partMap, ecrType, "JFECR2MakePartPrice", SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost, "JFChangeUnitPriceCost");
        }
        BigDecimal currentPrice = newBigDecimal(currentPriceValue);
        BigDecimal currentQuantity = newBigDecimal(getMapValue(partMap, SELECT_ATTRIBUTE_JF_BOMQuantity));
        BigDecimal bomIncrementPrice = currentPrice.multiply(currentQuantity);
        String beforeId = getMapValue(partMap, SELECT_ATTRIBUTE_JF_ChangeBeforeRev);
        if (UIUtil.isNotNullAndNotEmpty(beforeId)) {
            try {
                DomainObject beforeObj = DomainObject.newInstance(context, beforeId);
                BigDecimal beforePrice = newBigDecimal(beforeObj.getInfo(context, SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost));
                BigDecimal beforeQuantity = newBigDecimal(getMapValue(partMap, SELECT_ATTRIBUTE_JF_BOMBeforeQuantity));
                bomIncrementPrice = bomIncrementPrice.subtract(beforePrice.multiply(beforeQuantity));
            } catch (Exception e) {
                logger.info("calculateFormalECRBOMIncrement before part failed, beforeId:{}", beforeId, e);
            }
        }
        return bomIncrementPrice.toString();
    }

    private BigDecimal newBigDecimal(String value) {
        try {
            if (UIUtil.isNotNullAndNotEmpty(value)) {
                return new BigDecimal(value.trim());
            }
        } catch (Exception e) {
            logger.info("newBigDecimal value invalid:{}", value);
        }
        return new BigDecimal(0);
    }

    /**
     * 获取文档Latest Version文件信息，过滤自动转换PDF，并将有效文件下载到文档目录。
     */
    private JSONArray getProjectDocumentFilesArray(Context context, String documentId, String documentDirPath) throws Exception {
        JSONArray filesArray = new JSONArray();
        if (UIUtil.isNullOrEmpty(documentId)) {
            return filesArray;
        }

        DomainObject documentObj = DomainObject.newInstance(context, documentId);
        MapList versionList = documentObj.getRelatedObjects(context,
                RELATIONSHIP_LATEST_VERSION,
                DomainConstants.QUERY_WILDCARD,
                getProjectDocumentFileSelectList(),
                relSelectsList,
                false,
                true,
                (short) 1,
                "",
                "",
                0);
        logger.info("documentId:{},versionList:{}", documentId,versionList);
        if (versionList == null || versionList.isEmpty()) {
            return filesArray;
        }

        CommonDocument commonDocument = new CommonDocument();
        commonDocument.setId(documentId);
        for (int i = 0; i < versionList.size(); i++) {
            Map versionMap = (Map) versionList.get(i);
            if (PDF_COVERT_FLAG.equals(getMapValue(versionMap, ATTR_PDF_SOURCE))) {
                continue;
            }
            String fileName = getMapValue(versionMap, CommonDocument.SELECT_TITLE);
            if (UIUtil.isNullOrEmpty(fileName)) {
                continue;
            }
            FileList fileList = new FileList();
            matrix.db.File checkoutFile = new matrix.db.File(fileName, "generic");
            fileList.add(checkoutFile);
            deleteCheckoutTargetFiles(documentDirPath, fileList);
            logger.info("fileList:{}",fileList);
            logger.info("versionDocument:{} fileList:{}", documentDirPath,fileList);
            if (!checkoutProjectDocumentFile(context, commonDocument, fileList, documentDirPath)) {
                continue;
            }
            for (int j = 0; j < fileList.size(); j++) {
                matrix.db.File file = fileList.get(j);
                JSONObject fileJson = new JSONObject();
                String owner = getMapValue(versionMap, DomainConstants.SELECT_OWNER);
                fileJson.put("data_name", file.getName());
                fileJson.put("data_time", getMapValue(versionMap, DomainConstants.SELECT_ORIGINATED));
                fileJson.put("data_owner", owner);
                fileJson.put("file_revision", getMapValue(versionMap, DomainConstants.SELECT_REVISION));
                fileJson.put("owner_email", getPersonEmail(context, owner));
                filesArray.add(fileJson);
            }
        }
        return filesArray;
    }

    /**
     * 构造文件版本对象select列表，用于生成files数组。
     */
    private StringList getProjectDocumentFileSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_OWNER);
        selects.add(DomainConstants.SELECT_REVISION);
        selects.add(DomainConstants.SELECT_ORIGINATED);
        selects.add(CommonDocument.SELECT_TITLE);
        selects.add(CommonDocument.SELECT_FILE_NAME);
        selects.add(ATTR_PDF_SOURCE);
        return selects;
    }

    /**
     * 下载单个文件；FCS抛异常但目标文件已落盘且非空时视为成功，否则跳过该文件。
     */
    private boolean checkoutProjectDocumentFile(Context context, CommonDocument commonDocument, FileList fileList, String documentDirPath) {
        try {
            commonDocument.checkoutFiles(context, false, "generic", fileList, documentDirPath);
            return true;
        } catch (Exception e) {
            if (isCheckoutFileExists(documentDirPath, fileList)) {
                logger.info("getProjectConnectFileInfo checkout exception but file exists, dir:{}, fileList:{}", documentDirPath, fileList, e);
                return true;
            }
            logger.info("getProjectConnectFileInfo checkout failed and skip file, dir:{}, fileList:{}", documentDirPath, fileList, e);
            return false;
        }
    }

    /**
     * 检查checkout目标文件是否已经存在且非空。
     */
    private boolean isCheckoutFileExists(String documentDirPath, FileList fileList) {
        if (fileList == null || fileList.isEmpty()) {
            return false;
        }
        for (int i = 0; i < fileList.size(); i++) {
            matrix.db.File checkOutFile = fileList.get(i);
            if (checkOutFile == null || UIUtil.isNullOrEmpty(checkOutFile.getName())) {
                return false;
            }
            File targetFile = new File(documentDirPath, checkOutFile.getName());
            if (!targetFile.exists() || targetFile.length() <= 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 下载前删除目标同名文件，保证目录和文件存在时重新同步会覆盖旧文件。
     */
    private void deleteCheckoutTargetFiles(String documentDirPath, FileList fileList) {
        if (fileList == null || fileList.isEmpty()) {
            return;
        }
        for (int i = 0; i < fileList.size(); i++) {
            matrix.db.File checkOutFile = fileList.get(i);
            if (checkOutFile == null || UIUtil.isNullOrEmpty(checkOutFile.getName())) {
                continue;
            }
            File targetFile = new File(documentDirPath, checkOutFile.getName());
            if (targetFile.exists() && !targetFile.delete()) {
                logger.info("getProjectConnectFileInfo delete old checkout file failed:{}", targetFile.getAbsolutePath());
            }
        }
    }

    /**
     * 创建单个文档的中转站目录，目录名为doc_id_doc_edition。
     */
    private File createProjectDocumentDir(String transferRootPath, String docId, String docEdition) throws Exception {
        File rootDir = new File(transferRootPath);
        File documentDir = new File(rootDir, docId + "_" + docEdition);
        if (!documentDir.exists() && !documentDir.mkdirs()) {
            throw new Exception("创建中转站文档目录失败:" + documentDir.getAbsolutePath());
        }
        return documentDir;
    }

    /**
     * 将单个文档JSON写入中转站目录下的doc.json。
     */
    private void writeProjectDocumentJson(File documentDir, JSONObject documentJson) throws Exception {
        if (!documentDir.exists() && !documentDir.mkdirs()) {
            throw new Exception("创建中转站文档目录失败:" + documentDir.getAbsolutePath());
        }
        File jsonFile = new File(documentDir, DOC_JSON_FILE_NAME);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            writer.write(documentJson.toJSONString());
        }
    }

    /**
     * 先以.tmp临时文件名上传BI接口JSON文件，上传完成后重命名为正式文件。
     **
     * @param context
     * @param interfaceType 接口类型
     * @param jsonFile 本地JSON文件
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/7/17 19:15
     */
    private void uploadBIInterfaceJsonFileToSftp(Context context, String interfaceType, File jsonFile) throws Exception {
        Properties properties = JF_Util_mxJPO.readPageObject(context, "JFJDConfig");
        String host = getPropertyValue(properties, BI_SFTP_HOST_KEY);
        String portValue = getPropertyValue(properties, BI_SFTP_PORT_KEY);
        String user = getPropertyValue(properties, BI_SFTP_USER_KEY);
        String password = getPropertyValue(properties, BI_SFTP_PASSWORD_KEY);
        String remoteDir = getBIInterfaceSftpRemoteDir(properties, interfaceType);

        if (UIUtil.isNullOrEmpty(host) || UIUtil.isNullOrEmpty(portValue)
                || UIUtil.isNullOrEmpty(user) || UIUtil.isNullOrEmpty(password)) {
            throw new Exception("BI接口SFTP配置不完整");
        }
        if (UIUtil.isNullOrEmpty(remoteDir)) {
            remoteDir = ".";
        }

        Object session = null;
        Object channel = null;
        String remoteFileName = jsonFile.getName();
        String remoteTempFileName = remoteFileName + ".tmp";
        try (InputStream inputStream = new FileInputStream(jsonFile)) {
            Class jschClass = Class.forName("com.jcraft.jsch.JSch");
            Class sessionClass = Class.forName("com.jcraft.jsch.Session");
            Class channelSftpClass = Class.forName("com.jcraft.jsch.ChannelSftp");

            Object jsch = jschClass.newInstance();
            Method getSessionMethod = jschClass.getMethod("getSession", String.class, String.class, int.class);
            session = getSessionMethod.invoke(jsch, user, host, Integer.parseInt(portValue));
            sessionClass.getMethod("setPassword", String.class).invoke(session, password);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            sessionClass.getMethod("setConfig", Properties.class).invoke(session, config);
            sessionClass.getMethod("connect").invoke(session);

            channel = sessionClass.getMethod("openChannel", String.class).invoke(session, "sftp");
            channel.getClass().getMethod("connect").invoke(channel);
            ensureSftpRemoteDir(channel, channelSftpClass, remoteDir);
            channelSftpClass.getMethod("put", InputStream.class, String.class).invoke(channel, inputStream, remoteTempFileName);
            channelSftpClass.getMethod("rename", String.class, String.class).invoke(channel, remoteTempFileName, remoteFileName);
            logger.info("uploadBIInterfaceJsonFileToSftp success, fileName:{}, host:{}, remoteDir:{}",
                    remoteFileName, host, remoteDir);
        } catch (ClassNotFoundException e) {
            throw new Exception("缺少JSch依赖，无法上传BI接口JSON文件到SFTP", e);
        } finally {
            disconnectSftpObject(channel);
            disconnectSftpObject(session);
        }
    }

    /**
     * 确保SFTP远端目录存在并进入该目录。
     **
     * @param channel SFTP Channel
     * @param channelSftpClass ChannelSftp类型
     * @param remoteDir 远端目录
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 20:20
     */
    private void ensureSftpRemoteDir(Object channel, Class channelSftpClass, String remoteDir) throws Exception {
        if (UIUtil.isNullOrEmpty(remoteDir) || ".".equals(remoteDir)) {
            return;
        }

        Method cdMethod = channelSftpClass.getMethod("cd", String.class);
        Method mkdirMethod = channelSftpClass.getMethod("mkdir", String.class);
        if (remoteDir.startsWith("/")) {
            cdMethod.invoke(channel, "/");
        }

        String[] dirArray = remoteDir.split("/");
        for (int i = 0; i < dirArray.length; i++) {
            String dirName = dirArray[i];
            if (UIUtil.isNullOrEmpty(dirName)) {
                continue;
            }
            try {
                cdMethod.invoke(channel, dirName);
            } catch (InvocationTargetException e) {
                mkdirMethod.invoke(channel, dirName);
                cdMethod.invoke(channel, dirName);
            }
        }
    }

    /**
     * 获取BI接口SFTP上传目录，DA和PartList按接口读取配置目录。
     **
     * @param properties 配置
     * @param interfaceType 接口类型
     * @return String 远端目录
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 20:10
     */
    private String getBIInterfaceSftpRemoteDir(Properties properties, String interfaceType) {
        String baseDir = "";
        if (BI_INTERFACE_DA_INFO.equals(interfaceType)) {
            baseDir = getPropertyValue(properties, BI_SFTP_DA_REMOTE_DIR_KEY);
        } else if (BI_INTERFACE_PARTLIST_INFO.equals(interfaceType)) {
            baseDir = getPropertyValue(properties, BI_SFTP_PARTLIST_REMOTE_DIR_KEY);
        }

        return UIUtil.isNotNullAndNotEmpty(baseDir) ? baseDir : getPropertyValue(properties, BI_SFTP_REMOTE_DIR_KEY);
    }

    /**
     * 断开JSch Session或Channel连接。
     **
     * @param sftpObject JSch连接对象
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 19:15
     */
    private void disconnectSftpObject(Object sftpObject) {
        if (sftpObject == null) {
            return;
        }
        try {
            sftpObject.getClass().getMethod("disconnect").invoke(sftpObject);
        } catch (Exception e) {
            logger.info("disconnectSftpObject failed", e);
        }
    }

    /**
     * 获取配置值并去除首尾空格。
     **
     * @param properties 配置
     * @param key 配置项
     * @return String 配置值
     * @throws Exception
     * @author caipan
     * @date 2026/7/6 19:15
     */
    private String getPropertyValue(Properties properties, String key) {
        if (properties == null || UIUtil.isNullOrEmpty(key)) {
            return "";
        }
        String value = properties.getProperty(key);
        return value == null ? "" : value.trim();
    }

    /**
     * 按人员name获取邮箱；查询失败时返回空，避免单个邮箱问题中断整体初始化。
     */
    private String getPersonEmail(Context context, String owner) {
        try {
            if (UIUtil.isNullOrEmpty(owner)) {
                return "";
            }
            return JF_NotificationUtils_mxJPO.getPersonEmail(context, owner, null);
        } catch (Exception e) {
            logger.info("getProjectConnectFileInfo get owner email failed, owner:{}", owner, e);
            return "";
        }
    }

    /**
     * 转义findObjects where条件中的单引号。
     */
    private String escapeWhereValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "\\'");
    }

    /**
     * 同步项目供货件最新发布版本的EBOM信息到JSON文件。
     **
     * @param context 上下文
     * @param args 项目名称数组，为空时查询全部项目
     * @return JSONObject 接口即时返回结果
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 16:00
     */
    public JSONObject syncHistorySupplyPartEBOM(Context context, String[] args) throws Exception {
        JSONObject returnJson = new JSONObject();
        JSONArray filePathArray = new JSONArray();
        boolean pushed = false;
        try {
            ContextUtil.pushContext(context);
            pushed = true;
            logger.info("syncHistorySupplyPartEBOM begin, projectNames:{}", Arrays.toString(args));
            MapList projectList = getHistorySupplyPartEBOMProjectList(context, args);
            String fileTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            for (int i = 0; i < projectList.size(); i++) {
                Map projectMap = (Map) projectList.get(i);
                String projectId = getMapValue(projectMap, DomainConstants.SELECT_ID);
                String projectName = getMapValue(projectMap, DomainConstants.SELECT_DESCRIPTION);
                if (UIUtil.isNullOrEmpty(projectName)) {
                    projectName = getMapValue(projectMap, DomainConstants.SELECT_NAME);
                }
                JSONObject projectJson = buildHistorySupplyPartEBOMJson(context, projectMap, createTime);
                String fileName = buildHistorySupplyPartEBOMFileName(projectName, fileTime);
                File jsonFile = writeHistorySupplyPartEBOMJsonFile(fileName, projectJson);
                filePathArray.add(jsonFile.getAbsolutePath());
            }
            returnJson.put("filePath", filePathArray);
            returnJson.put("code", "200");
            returnJson.put("msg", "");
            returnJson.put("result", "success");
        } catch (Exception e) {
            logger.error("syncHistorySupplyPartEBOM failed", e);
            returnJson.put("filePath", filePathArray);
            returnJson.put("code", "500");
            returnJson.put("msg", e.getMessage());
            returnJson.put("result", "failed");
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
            logger.info("syncHistorySupplyPartEBOM end, filePath:{}", filePathArray);
        }
        return returnJson;
    }

    /**
     * 获取需要同步供货件EBOM的项目。
     **
     * @param context 上下文
     * @param projectNames 项目名称数组
     * @return MapList 项目列表
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private MapList getHistorySupplyPartEBOMProjectList(Context context, String[] projectNames) throws Exception {
        StringList projectSelects = new StringList();
        projectSelects.add(DomainConstants.SELECT_ID);
        projectSelects.add(DomainConstants.SELECT_NAME);
        projectSelects.add(DomainConstants.SELECT_DESCRIPTION);
        if (projectNames == null || projectNames.length == 0) {
            return DomainObject.findObjects(context, DomainConstants.TYPE_PROJECT_SPACE,
                    DomainConstants.QUERY_WILDCARD, "", projectSelects);
        }
        MapList projectList = new MapList();
        Set<String> projectNameSet = new LinkedHashSet<String>();
        for (int i = 0; i < projectNames.length; i++) {
            if (UIUtil.isNotNullAndNotEmpty(projectNames[i])) {
                projectNameSet.add(projectNames[i].trim());
            }
        }
        for (String projectName : projectNameSet) {
            projectList.addAll(DomainObject.findObjects(context, DomainConstants.TYPE_PROJECT_SPACE,
                    DomainConstants.QUERY_WILDCARD,
                    "name == '" + escapeWhereValue(projectName) + "'", projectSelects));
        }
        return projectList;
    }

    /**
     * 构建单个项目的供货件EBOM JSON内容。
     **
     * @param context 上下文
     * @param projectMap 项目数据
     * @param createTime 生成时间
     * @return JSONObject 项目EBOM JSON
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private JSONObject buildHistorySupplyPartEBOMJson(Context context, Map projectMap, String createTime) throws Exception {
        String projectId = getMapValue(projectMap, DomainConstants.SELECT_ID);
        String projectCode = getMapValue(projectMap, DomainConstants.SELECT_NAME);
        String projectName = getMapValue(projectMap, DomainConstants.SELECT_DESCRIPTION);
        if (UIUtil.isNullOrEmpty(projectName)) {
            projectName = projectCode;
        }
        JSONObject fileJson = new JSONObject(true);
        JSONObject syncInfo = new JSONObject(true);
        JSONArray rootList = new JSONArray();
        JSONArray partArray = new JSONArray();
        JSONArray bomArray = new JSONArray();
        syncInfo.put("rootList", rootList);
        syncInfo.put("bomType", "EBOM");
        syncInfo.put("createTime", createTime);
        syncInfo.put("projectCode", projectCode);
        syncInfo.put("projectName", projectName);
        fileJson.put("syncInfo", syncInfo);
        fileJson.put("parts", partArray);
        fileJson.put("boms", bomArray);

        Map<String, Map> latestRootPartMap = getHistorySupplyPartLatestReleasedRootPartMap(context, projectId);
        Set<String> partKeySet = new LinkedHashSet<String>();
        JF_DR_mxJPO drJpo = new JF_DR_mxJPO();
        Document mandatoryAttributeDocument = getHistorySupplyPartMandatoryAttributeDocument(context);
        //20260827 update by codex caipan 在单个项目JSON范围内合并重复BOM关系
        Map<String, Map> sameBomMap = new LinkedHashMap<String, Map>();
        for (Map rootPartMap : latestRootPartMap.values()) {
            JSONObject rootJson = new JSONObject();
            rootJson.put("rootPartNumber", getHistorySupplyPartJsonValue(rootPartMap, SELECT_ATTR_V_PART_NUMBER));
            rootJson.put("name", getHistorySupplyPartJsonValue(rootPartMap, DomainConstants.SELECT_NAME));
            rootJson.put("revision", getHistorySupplyPartJsonValue(rootPartMap, DomainConstants.SELECT_REVISION));
            rootList.add(rootJson);
            addHistorySupplyPartJsonPart(context, partArray, partKeySet, rootPartMap, projectId, drJpo,
                    mandatoryAttributeDocument);

            String rootPartId = getMapValue(rootPartMap, DomainConstants.SELECT_ID);
            DomainObject rootPartObject = DomainObject.newInstance(context, rootPartId);
            MapList childPartList = rootPartObject.getRelatedObjects(context,
                    "VPMInstance",
                    TYPE_VPMREFERENCE,
                    getHistorySupplyPartSelectList(),
                    getHistorySupplyPartBOMRelationshipSelectList(),
                    false,
                    true,
                    (short) 0,
                    "",
                    "",
                    0);
            int mergedLevel = Integer.MAX_VALUE;
            for (int i = 0; i < childPartList.size(); i++) {
                Map childPartMap = (Map) childPartList.get(i);
                addHistorySupplyPartJsonPart(context, partArray, partKeySet, childPartMap, projectId, drJpo,
                        mandatoryAttributeDocument);
                //20260817 update by codex caipan 按父子零件合并重复BOM关系并累计用量
                int childLevel = Integer.parseInt(getMapValue(childPartMap, "level"));
                if (childLevel > mergedLevel) {
                    continue;
                }
                mergedLevel = Integer.MAX_VALUE;
                if (mergeHistorySupplyPartBOM(sameBomMap, childPartMap)) {
                    mergedLevel = childLevel;
                }
            }
        }
        for (Map mergedBomMap : sameBomMap.values()) {
            bomArray.add(buildHistorySupplyPartBOMJson(mergedBomMap));
        }
        return fileJson;
    }

    /**
     * 获取项目供货件中每个逻辑零件的最新发布版本。
     **
     * @param context 上下文
     * @param projectId 项目ID
     * @return Map 逻辑ID与最新发布供货件的映射
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private Map<String, Map> getHistorySupplyPartLatestReleasedRootPartMap(Context context, String projectId) throws Exception {
        Map<String, Map> latestPartMap = new LinkedHashMap<String, Map>();
        if (UIUtil.isNullOrEmpty(projectId)) {
            return latestPartMap;
        }
        DomainObject projectObject = DomainObject.newInstance(context, projectId);
        MapList supplyPartList = projectObject.getRelatedObjects(context,
                rel_JFProject2RootPart,
                TYPE_VPMREFERENCE,
                getHistorySupplyPartSelectList(),
                new StringList(),
                false,
                true,
                (short) 1,
                "",
                "attribute[JFZeroPart]==Y",
                0);
        for (int i = 0; i < supplyPartList.size(); i++) {
            Map supplyPartMap = (Map) supplyPartList.get(i);
            if (!"RELEASED".equals(getMapValue(supplyPartMap, DomainConstants.SELECT_CURRENT))) {
                continue;
            }
            String logicalId = getMapValue(supplyPartMap, "logicalid");
            if (UIUtil.isNullOrEmpty(logicalId)) {
                logicalId = getMapValue(supplyPartMap, DomainConstants.SELECT_ID);
            }
            Map existingPartMap = latestPartMap.get(logicalId);
            if (existingPartMap == null || isHistorySupplyPartLaterReleased(supplyPartMap, existingPartMap)) {
                latestPartMap.put(logicalId, supplyPartMap);
            }
        }
        return latestPartMap;
    }

    /**
     * 判断候选零件是否为更晚发布的版本。
     **
     * @param candidateMap 候选零件
     * @param existingMap 已选零件
     * @return boolean 候选零件发布时间更晚时返回true
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private boolean isHistorySupplyPartLaterReleased(Map candidateMap, Map existingMap) {
        String candidateReleasedTime = getMapValue(candidateMap, SELECT_STATE_RELEASED_ACTUAL);
        String existingReleasedTime = getMapValue(existingMap, SELECT_STATE_RELEASED_ACTUAL);
        Date candidateReleasedDate = getHistorySupplyPartReleasedDate(candidateReleasedTime);
        Date existingReleasedDate = getHistorySupplyPartReleasedDate(existingReleasedTime);
        int timeCompare;
        if (candidateReleasedDate != null && existingReleasedDate != null) {
            timeCompare = candidateReleasedDate.compareTo(existingReleasedDate);
        } else {
            timeCompare = candidateReleasedTime.compareTo(existingReleasedTime);
        }
        if (timeCompare != 0) {
            return timeCompare > 0;
        }
        return getMapValue(candidateMap, DomainConstants.SELECT_REVISION)
                .compareTo(getMapValue(existingMap, DomainConstants.SELECT_REVISION)) > 0;
    }

    /**
     * 将ENOVIA发布时间转换为Java日期。
     **
     * @param releasedTime ENOVIA发布时间
     * @return Date 发布时间无法解析时返回null
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private Date getHistorySupplyPartReleasedDate(String releasedTime) {
        if (UIUtil.isNullOrEmpty(releasedTime)) {
            return null;
        }
        try {
            return eMatrixDateFormat.getJavaDate(releasedTime);
        } catch (Exception e) {
            logger.info("syncHistorySupplyPartEBOM parse released time failed, releasedTime:{}", releasedTime);
            return null;
        }
    }

    /**
     * 将零件信息按零件号和版本去重后写入JSON。
     **
     * @param context 上下文
     * @param partArray 零件JSON数组
     * @param partKeySet 已写入零件键集合
     * @param partMap 零件数据
     * @param projectId 项目ID
     * @param drJpo DR业务JPO
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private void addHistorySupplyPartJsonPart(Context context, JSONArray partArray, Set<String> partKeySet,
                                              Map partMap, String projectId, JF_DR_mxJPO drJpo,
                                              Document mandatoryAttributeDocument) throws Exception {
        String partNumber = getMapValue(partMap, SELECT_ATTR_V_PART_NUMBER);
        String revision = getMapValue(partMap, DomainConstants.SELECT_REVISION);
        String partKey = partNumber + "|" + revision;
        if (!partKeySet.add(partKey)) {
            return;
        }
        String partId = getMapValue(partMap, DomainConstants.SELECT_ID);
        Map customerPartsMap = getDRCustomerPartsDBRelationInfo(context, partId, projectId);
        JSONObject partJson = new JSONObject();
        putHistorySupplyPartJsonValue(partJson, "EnterpriseExtension.V_PartNumber", partMap, SELECT_ATTR_V_PART_NUMBER);
        putHistorySupplyPartJsonValue(partJson, "name", partMap, DomainConstants.SELECT_NAME);
        putHistorySupplyPartJsonValue(partJson, "current", partMap, DomainConstants.SELECT_CURRENT);
        putHistorySupplyPartJsonValue(partJson, "revision", partMap, DomainConstants.SELECT_REVISION);
        putHistorySupplyPartJsonValue(partJson, "ProjectID", partMap,
                "to[JFProject2RootPart|attribute[JF_BelongPart]==Y].from.name");
        putHistorySupplyPartJsonValue(partJson, "JF_VPMReference.JF_PartNameCN", partMap, SELECT_ATTR_JF_PartNameCN);
        putHistorySupplyPartJsonValue(partJson, "JF_VPMReference.JF_PartNameEN", partMap, SELECT_ATTR_JF_PartNameEN);
        putHistorySupplyPartJsonValue(partJson, "JF_VPMReference.JF_PartType", partMap, SELECT_ATTR_JFPartType);
        putHistorySupplyPartJsonValue(partJson, "JF_VPMReference.JF_ProcurementType", partMap, SELECT_ATTR_JF_ProcurementType);
        putHistorySupplyPartJsonValue(partJson, "JF_VPMReference.JF_Detail_CN", partMap, SELECT_ATTR_JF_Detail_CN);
        putHistorySupplyPartJsonValue(partJson, "JF_VPMReference.JF_Detail_EN", partMap,
                "attribute[JF_VPMReference.JF_Detail_EN]");
        putHistorySupplyPartJsonValue(partJson, "JFCustomerPartNumber", customerPartsMap, Select_Attr_JFCustomerPartNumber);
        putHistorySupplyPartJsonValue(partJson, "JFCustomerPartName", customerPartsMap, Select_Attr_JFCustomerPartName);
        putHistorySupplyPartJsonValue(partJson, "JFCustomerPartRevision", customerPartsMap, Select_Attr_JFCustomerPartRevision);
        putHistorySupplyPartJsonValue(partJson, "JF_DirectBuy", customerPartsMap, Select_Attr_JF_DirectBuy);
        for (String[] field : getHistorySupplyPartAttributeFieldList()) {
            putHistorySupplyPartJsonValue(partJson, field[0], partMap, field[1]);
        }
        partJson.put("required", buildHistorySupplyPartRequiredJson(partMap, mandatoryAttributeDocument));
        //20260827 update by codex caipan 所有历史同步接口统一输出零件关联图纸
        partJson.put("drawings", getHistoryPartDrawingArray(context, partId));
        partArray.add(partJson);
    }

    /**
     * 构建BOM关系JSON。
     **
     * @param bomMap BOM关系数据
     * @return JSONObject BOM关系JSON
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private JSONObject buildHistorySupplyPartBOMJson(Map bomMap) {
        JSONObject bomJson = new JSONObject();
        putHistorySupplyPartJsonValue(bomJson, "instanceId", bomMap, DomainRelationship.SELECT_ID);
        putHistorySupplyPartJsonValue(bomJson, "parentName", bomMap, "from.name");
        putHistorySupplyPartJsonValue(bomJson, "parentRevision", bomMap, "from.revision");
        putHistorySupplyPartJsonValue(bomJson, "childName", bomMap, "to.name");
        putHistorySupplyPartJsonValue(bomJson, "childRevision", bomMap, "to.revision");
        putHistorySupplyPartJsonValue(bomJson, "JF_VPMInstance.JF_Dosage", bomMap, "attribute[JF_VPMInstance.JF_Dosage]");
        putHistorySupplyPartJsonValue(bomJson, "JF_VPMInstance.JF_FNA", bomMap, "attribute[JF_VPMInstance.JF_FNA]");
        putHistorySupplyPartJsonValue(bomJson, "JF_VPMInstance.JF_OnlyEBOMPart", bomMap, "attribute[JF_VPMInstance.JF_OnlyEBOMPart]");
        putHistorySupplyPartJsonValue(bomJson, "JF_VPMInstance.JF_Symmetry", bomMap, "attribute[JF_VPMInstance.JF_Symmetry]");
        putHistorySupplyPartJsonValue(bomJson, "PLMInstance.PLM_ExternalID", bomMap,
                "attribute[PLMInstance.PLM_ExternalID]");
        return bomJson;
    }

    /**
     * 获取供货件EBOM零件查询字段。
     **
     * @return StringList 零件查询字段
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private StringList getHistorySupplyPartSelectList() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_REVISION);
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add("logicalid");
        selects.add(SELECT_STATE_RELEASED_ACTUAL);
        selects.add(SELECT_ATTR_V_PART_NUMBER);
        selects.add(SELECT_ATTR_JF_PartNameCN);
        selects.add(SELECT_ATTR_JF_PartNameEN);
        selects.add(SELECT_ATTR_JFPartType);
        selects.add(SELECT_ATTR_JF_ProcurementType);
        selects.add(SELECT_ATTR_JF_Detail_CN);
        selects.add("attribute[JF_VPMReference.JF_Detail_EN]");
        selects.add("to[JFProject2RootPart|attribute[JF_BelongPart]==Y].from.name");
        for (String[] field : getHistorySupplyPartAttributeFieldList()) {
            selects.add(field[1]);
        }
        return selects;
    }

    /**
     * 获取供货件EBOM关系查询字段。
     **
     * @return StringList BOM关系查询字段
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private StringList getHistorySupplyPartBOMRelationshipSelectList() {
        StringList selects = new StringList();
        selects.add(DomainRelationship.SELECT_ID);
        selects.add("from.id");
        selects.add("from.name");
        selects.add("from.revision");
        selects.add("to.name");
        selects.add("to.revision");
        selects.add("attribute[JF_VPMInstance.JF_Dosage]");
        selects.add("attribute[JF_VPMInstance.JF_FNA]");
        selects.add("attribute[JF_VPMInstance.JF_OnlyEBOMPart]");
        selects.add("attribute[JF_VPMInstance.JF_Symmetry]");
        selects.add("attribute[PLMInstance.PLM_ExternalID]");
        return selects;
    }

    /**
     * 获取供货件EBOM零件属性与JSON字段映射。
     **
     * @return String[][] JSON字段与对象select表达式
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private String[][] getHistorySupplyPartAttributeFieldList() {
        return new String[][]{
                {"JF_VPMReference.JF_ProjectRel", "attribute[JF_VPMReference.JF_ProjectRel]"},
                {"JF_SupplierDevPart", "attribute[JF_VPMReference.JF_SupplierDevelopedPart]"},
                {"JF_VPMReference.JF_FlexiblePart", "attribute[JF_VPMReference.JF_FlexiblePart]"},
                {"JF_VPMReference.JF_TransformationPlan", "attribute[JF_VPMReference.JF_TransformationPlan]"},
                {"JF_VPMReference.JF_Weight", "attribute[JF_VPMReference.JF_Weight]"},
                {"JF_VPMReference.JF_WeightTarget", "attribute[JF_VPMReference.JF_WeightTarget]"},
                {"JF_VPMReference.JF_Unit", "attribute[JF_VPMReference.JF_Unit]"},
                {"JF_VPMReference.JF_Material", "attribute[JF_VPMReference.JF_Material]"},
                {"JF_VPMReference.JF_MaterialCode", "attribute[JF_VPMReference.JF_MaterialCode]"},
                {"JF_VPMReference.JF_RecycleMaterialRatio", "attribute[JF_VPMReference.JF_RecycleMaterialRatio]"},
                {"JF_VPMReference.JF_MaterialStar", "attribute[JF_VPMReference.JF_MaterialStar]"},
                {"JF_VPMReference.JF_MaterialDensity", "attribute[JF_VPMReference.JF_MaterialDensity]"},
                {"JF_VPMReference.JF_SubstituteMaterial", "attribute[JF_VPMReference.JF_SubstituteMaterial]"},
                {"Function", "attribute[Function]"}, {"JFLength", "attribute[JFLength]"},
                {"JFWidth", "attribute[JFWidth]"}, {"JFThickness", "attribute[JFThickness]"},
                {"Diam", "attribute[Diam]"}, {"Inside Diameter", "attribute[Inside Diameter]"},
                {"RollPartNumber", "attribute[RollPartNumber]"}, {"CustomerDrawingNumber", "attribute[CustomerDrawingNumber]"},
                {"Supplier", "attribute[Supplier]"}, {"SupplierPartNumber", "attribute[SupplierPartNumber]"},
                {"Craft", "attribute[Craft]"}, {"JF Fabric Material Type", "attribute[JF Fabric Material Type]"},
                {"Surface Treatment", "attribute[Surface Treatment]"}, {"PlatingThickness", "attribute[PlatingThickness]"},
                {"SurfaceTreatmentArea", "attribute[SurfaceTreatmentArea]"}, {"SurfaceTreatmentGrade", "attribute[SurfaceTreatmentGrade]"},
                {"Area", "attribute[Area]"}, {"NetArea", "attribute[NetArea]"}, {"Volume", "attribute[Volume]"},
                {"Circumference", "attribute[Circumference]"}, {"GramWeight", "attribute[GramWeight]"},
                {"WeldingType", "attribute[WeldingType]"}, {"WeldingSpotQuantity", "attribute[WeldingSpotQuantity]"},
                {"WeldingLength", "attribute[WeldingLength]"}, {"BendingMumber", "attribute[BendingMumber]"},
                {"FlatteningQuantity", "attribute[FlatteningQuantity]"}, {"PunchingQuantity", "attribute[PunchingQuantity]"},
                {"CutQuantity", "attribute[CutQuantity]"}, {"ContractedTubeQuantity", "attribute[ContractedTubeQuantity]"},
                {"CompositeLayerType", "attribute[CompositeLayerType]"}, {"CompositeLayerDensity", "attribute[CompositeLayerDensity]"},
                {"BackingType", "attribute[BackingType]"}, {"BackingDensity", "attribute[BackingDensity]"},
                {"ZipperType", "attribute[ZipperType]"}, {"ThreadType", "attribute[ThreadType]"},
                {"StitchesQuantity", "attribute[StitchesQuantity]"}, {"LengthofSingleSuture", "attribute[LengthofSingleSuture]"},
                {"TypeofPipingCore", "attribute[TypeofPipingCore]"}, {"DiameterofPipingCore", "attribute[DiameterofPipingCore]"},
                {"Perforation", "attribute[Perforation]"}, {"PerforationQuantity", "attribute[PerforationQuantity]"},
                {"Quilting", "attribute[Quilting]"}, {"QuiltingAttachment", "attribute[QuiltingAttachment]"},
                {"BondingProcess", "attribute[BondingProcess]"}, {"BlindStitch", "attribute[BlindStitch]"},
                {"Pattern", "attribute[Pattern]"}, {"PatternAttachment", "attribute[PatternAttachment]"},
                {"HotPressingorNot", "attribute[HotPressingorNot]"}, {"BackAdhesiveorNot", "attribute[BackAdhesiveorNot]"},
                {"BackAdhesiveArea", "attribute[BackAdhesiveArea]"}, {"AdhesiveSprayingorNot", "attribute[AdhesiveSprayingorNot]"},
                {"SlowReboundorNot", "attribute[SlowReboundorNot]"}, {"FoamMoldType", "attribute[FoamMoldType]"},
                {"MagneticPowderIronNails", "attribute[MagneticPowderIronNails]"}, {"MagneticPowderIronNailsQuantity", "attribute[MagneticPowderIronNailsQuantity]"},
                {"Specification", "attribute[Specification]"}, {"JFPitch", "attribute[JFPitch]"},
                {"AcrossFlats", "attribute[AcrossFlats]"}, {"Thickness", "attribute[Thickness]"},
                {"PerformanceLevel", "attribute[PerformanceLevel]"}, {"Rigidity", "attribute[Rigidity]"},
                {"CaseDepth", "attribute[CaseDepth]"}, {"ShearLoad", "attribute[ShearLoad]"},
                {"TensileLoad", "attribute[TensileLoad]"}, {"GuarantLoad", "attribute[GuarantLoad]"},
                {"Salt Spray Test Time", "attribute[Salt Spray Test Time]"}, {"NominalLength", "attribute[NominalLength]"},
                {"Step Height", "attribute[Step Height]"}, {"StepDiameter", "attribute[StepDiameter]"},
                {"Beatle", "attribute[Beatle]"}, {"Grip Range", "attribute[Grip Range]"},
                {"Matching Hole Dameter", "attribute[Matching Hole Dameter]"}, {"DestructiveTorque", "attribute[DestructiveTorque]"},
                {"MountingTorque", "attribute[MountingTorque]"}, {"MatchStructuralHoleDiameter", "attribute[MatchStructuralHoleDiameter]"},
                {"Match The Thickness of the Structure", "attribute[Match The Thickness of the Structure]"},
                {"InsertionForce", "attribute[InsertionForce]"}, {"PulloutForce", "attribute[PulloutForce]"},
                {"BundledDiameter", "attribute[BundledDiameter]"}, {"StitchingThickness", "attribute[StitchingThickness]"},
                {"ScrewInsertionTorque", "attribute[ScrewInsertionTorque]"}, {"MatchStructuralWallThickness", "attribute[MatchStructuralWallThickness]"},
                {"IS Anti Loosening Adhesive", "attribute[IS Anti Loosening Adhesive]"}, {"ExpandedPipeNumber", "attribute[ExpandedPipeNumber]"},
                {"ColdDrawn", "attribute[ColdDrawn]"}, {"GripNumber", "attribute[GripNumber]"},
                {"HeatTreatment", "attribute[HeatTreatment]"}, {"Thread", "attribute[Thread]"},
                {"Chamfer", "attribute[Chamfer]"}, {"MassPerMeters", "attribute[MassPerMeters]"},
                {"LockStrength", "attribute[LockStrength]"}, {"PeelStrength", "attribute[PeelStrength]"},
                {"BlockPointStrength", "attribute[BlockPointStrength]"}, {"XaxisClearance", "attribute[XaxisClearance]"},
                {"YaxisClearance", "attribute[YaxisClearance]"}, {"ZaxisClearance", "attribute[ZaxisClearance]"},
                {"SingleTrackCrossSecDimensions", "attribute[SingleTrackCrossSecDimensions]"}, {"RailTypeThickness", "attribute[RailTypeThickness]"},
                {"Stroke", "attribute[Stroke]"}, {"Durability", "attribute[Durability]"},
                {"AdjustAngleDeviceSpecification", "attribute[AdjustAngleDeviceSpecification]"}, {"TransmissionRatio", "attribute[TransmissionRatio]"},
                {"OperatingIdleStroke", "attribute[OperatingIdleStroke]"}, {"HandleExcessiveForce", "attribute[HandleExcessiveForce]"},
                {"AxialFailureStrength", "attribute[AxialFailureStrength]"}, {"OutputTeethNumber", "attribute[OutputTeethNumber]"},
                {"JFTolerance", "attribute[JFTolerance]"}, {"UnLockForce", "attribute[UnLockForce]"},
                {"LockForce", "attribute[LockForce]"}, {"DrivingTorque", "attribute[DrivingTorque]"},
                {"OperatingForce", "attribute[OperatingForce]"}, {"Speed", "attribute[Speed]"},
                {"BackrestAngle", "attribute[BackrestAngle]"}, {"OperatingSound", "attribute[OperatingSound]"},
                {"NoLoadSpeed", "attribute[NoLoadSpeed]"}, {"NoLoadCurrent", "attribute[NoLoadCurrent]"},
                {"LockedTorque", "attribute[LockedTorque]"}, {"LockedCurrent", "attribute[LockedCurrent]"},
                {"DamageTorque", "attribute[DamageTorque]"}, {"WhetherMemory", "attribute[WhetherMemory]"},
                {"EMCLevel", "attribute[EMCLevel]"}, {"NoiseLevel", "attribute[NoiseLevel]"},
                {"CycleLife", "attribute[CycleLife]"}
        };
    }

    /**
     * 读取零件详细类别必填属性配置。
     **
     * @param context 上下文
     * @return Document 必填属性配置XML
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private Document getHistorySupplyPartMandatoryAttributeDocument(Context context) throws Exception {
        Page pageAttributePopulation = new Page("PartAttributeProperties.xml");
        String properties;
        pageAttributePopulation.open(context);
        try {
            properties = pageAttributePopulation.getContents(context);
        } finally {
            pageAttributePopulation.close(context);
        }
        InputStream inputStream = new ByteArrayInputStream(properties.getBytes(StandardCharsets.UTF_8));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(inputStream);
    }

    /**
     * 根据零件详细类别构建字段必填标记。
     **
     * @param partMap 零件数据
     * @param mandatoryAttributeDocument 必填属性配置XML
     * @return JSONObject 字段必填标记
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private JSONObject buildHistorySupplyPartRequiredJson(Map partMap, Document mandatoryAttributeDocument) {
        JSONObject requiredJson = new JSONObject();
        Map<String, Set<String>> requiredFieldMap = getHistorySupplyPartRequiredFieldMap(mandatoryAttributeDocument,
                getMapValue(partMap, SELECT_ATTR_JF_Detail_CN));
        Set<String> requiredAttributeSet = requiredFieldMap.get("attribute");
        Set<String> requiredRelationshipSet = requiredFieldMap.get("relationship");
        putHistorySupplyPartRequiredValue(requiredJson, "EnterpriseExtension.V_PartNumber",
                requiredAttributeSet.contains("EnterpriseExtension.V_PartNumber"));
        putHistorySupplyPartRequiredValue(requiredJson, "JF_VPMReference.JF_PartNameCN",
                requiredAttributeSet.contains("JF_VPMReference.JF_PartNameCN"));
        putHistorySupplyPartRequiredValue(requiredJson, "JF_VPMReference.JF_PartNameEN",
                requiredAttributeSet.contains("JF_VPMReference.JF_PartNameEN"));
        putHistorySupplyPartRequiredValue(requiredJson, "JF_VPMReference.JF_PartType",
                requiredAttributeSet.contains("JF_VPMReference.JF_PartType"));
        putHistorySupplyPartRequiredValue(requiredJson, "JF_VPMReference.JF_ProcurementType",
                requiredAttributeSet.contains("JF_VPMReference.JF_ProcurementType"));
        putHistorySupplyPartRequiredValue(requiredJson, "JF_VPMReference.JF_Detail_CN",
                requiredAttributeSet.contains("JF_VPMReference.JF_Detail_CN"));
        putHistorySupplyPartRequiredValue(requiredJson, "JF_VPMReference.JF_Detail_EN",
                requiredAttributeSet.contains("JF_VPMReference.JF_Detail_EN"));
        putHistorySupplyPartRequiredValue(requiredJson, "JFCustomerPartNumber",
                requiredRelationshipSet.contains("JFCustomerPartNumber"));
        putHistorySupplyPartRequiredValue(requiredJson, "JFCustomerPartName",
                requiredRelationshipSet.contains("JFCustomerPartName"));
        putHistorySupplyPartRequiredValue(requiredJson, "JFCustomerPartRevision",
                requiredRelationshipSet.contains("JFCustomerPartRevision"));
        putHistorySupplyPartRequiredValue(requiredJson, "JF_DirectBuy",
                requiredRelationshipSet.contains("JF_DirectBuy"));
        for (String[] field : getHistorySupplyPartAttributeFieldList()) {
            putHistorySupplyPartRequiredValue(requiredJson, field[0],
                    requiredAttributeSet.contains(getHistorySupplyPartAttributeName(field[1])));
        }
        return requiredJson;
    }

    /**
     * 必填字段写入required节点，非必填字段不输出。
     **
     * @param requiredJson 必填字段JSON
     * @param fieldName JSON字段名称
     * @param required 是否必填
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private void putHistorySupplyPartRequiredValue(JSONObject requiredJson, String fieldName, boolean required) {
        if (required) {
            requiredJson.put(fieldName, true);
        }
    }

    /**
     * 获取详细类别配置的零件属性和关系必填字段。
     **
     * @param mandatoryAttributeDocument 必填属性配置XML
     * @param detailCN 零件详细类别-CN
     * @return Map 属性和关系必填字段集合
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private Map<String, Set<String>> getHistorySupplyPartRequiredFieldMap(Document mandatoryAttributeDocument,
                                                                            String detailCN) {
        Map<String, Set<String>> requiredFieldMap = new HashMap<String, Set<String>>();
        requiredFieldMap.put("attribute", new HashSet<String>());
        requiredFieldMap.put("relationship", new HashSet<String>());
        if (mandatoryAttributeDocument == null || UIUtil.isNullOrEmpty(detailCN)) {
            return requiredFieldMap;
        }
        NodeList partTypeNodeList = mandatoryAttributeDocument.getElementsByTagName("partType");
        for (int i = 0; i < partTypeNodeList.getLength(); i++) {
            Node partTypeNode = partTypeNodeList.item(i);
            Node idNode = partTypeNode.getAttributes().getNamedItem("id");
            if (idNode == null || !detailCN.equals(idNode.getNodeValue())) {
                continue;
            }
            NodeList fieldNodeList = partTypeNode.getChildNodes();
            for (int j = 0; j < fieldNodeList.getLength(); j++) {
                Node fieldNode = fieldNodeList.item(j);
                String fieldType = fieldNode.getNodeName();
                if (!"attribute".equals(fieldType) && !"relationship".equals(fieldType)) {
                    continue;
                }
                Node nameNode = fieldNode.getAttributes().getNamedItem("name");
                if (nameNode != null) {
                    requiredFieldMap.get(fieldType).add(nameNode.getNodeValue());
                }
            }
            break;
        }
        return requiredFieldMap;
    }

    /**
     * 从属性select表达式中获取属性名称。
     **
     * @param selectExpression 属性select表达式
     * @return String 属性名称
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private String getHistorySupplyPartAttributeName(String selectExpression) {
        if (UIUtil.isNullOrEmpty(selectExpression)
                || !selectExpression.startsWith("attribute[")
                || !selectExpression.endsWith("]")) {
            return "";
        }
        return selectExpression.substring("attribute[".length(), selectExpression.length() - 1);
    }

    /**
     * 将Map中的值写入JSON，空值统一输出为空字符串。
     **
     * @param json JSON对象
     * @param key JSON字段
     * @param sourceMap 数据来源
     * @param selectKey select表达式
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private void putHistorySupplyPartJsonValue(JSONObject json, String key, Map sourceMap, String selectKey) {
        json.put(key, getHistorySupplyPartJsonValue(sourceMap, selectKey));
    }

    /**
     * 获取供货件EBOM JSON字段值，空值统一输出为空字符串。
     **
     * @param sourceMap 数据来源
     * @param selectKey select表达式
     * @return String JSON字段值
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private String getHistorySupplyPartJsonValue(Map sourceMap, String selectKey) {
        return getMapValue(sourceMap, selectKey);
    }

    /**
     * 构建供货件EBOM JSON文件名。
     **
     * @param projectName 项目名称
     * @param fileTime 文件时间
     * @return String 文件名
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private String buildHistorySupplyPartEBOMFileName(String projectName, String fileTime) {
        String safeProjectName = UIUtil.isNullOrEmpty(projectName) ? "Project" : projectName;
        safeProjectName = safeProjectName.replaceAll("[\\\\/:*?\"<>|]", "_");
        return safeProjectName + "_" + fileTime + ".json";
    }

    /**
     * 将供货件EBOM JSON写入/data/cp/AI/目录。
     **
     * @param fileName 文件名
     * @param fileJson JSON内容
     * @return File 已生成文件
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 16:00
     */
    private File writeHistorySupplyPartEBOMJsonFile(String fileName, JSONObject fileJson) throws Exception {
        File rootDir = new File("/data/cp/AI/");
        if (!rootDir.exists() && !rootDir.mkdirs()) {
            throw new Exception("创建供货件EBOM文件目录失败:" + rootDir.getAbsolutePath());
        }
        File jsonFile = new File(rootDir, fileName);
        File tempFile = new File(rootDir, fileName + ".tmp");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            writer.write(fileJson.toJSONString());
        }
        if (jsonFile.exists() && !jsonFile.delete()) {
            throw new Exception("删除旧供货件EBOM文件失败:" + jsonFile.getAbsolutePath());
        }
        if (!tempFile.renameTo(jsonFile)) {
            throw new Exception("重命名供货件EBOM临时文件失败:" + tempFile.getAbsolutePath());
        }
        return jsonFile;
    }

    /**
     * 同步存量标准件最新发布版本信息到JSON文件。
     **
     * @param context 上下文
     * @param args 保留的JPO调用参数，不参与标准件范围筛选
     * @return JSONObject 文件生成结果
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 17:00
     */
    public JSONObject syncHistorystandardPart(Context context, String[] args) throws Exception {
        JSONObject returnJson = new JSONObject();
        JSONArray filePathArray = new JSONArray();
        boolean pushed = false;
        try {
            ContextUtil.pushContext(context);
            pushed = true;
            logger.info("syncHistorystandardPart begin");
            MapList standardRootList = getHistoryStandardPartRootList(context);
            String fileTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Document mandatoryAttributeDocument = getHistorySupplyPartMandatoryAttributeDocument(context);
            for (int i = 0; i < standardRootList.size(); i++) {
                Map rootMap = (Map) standardRootList.get(i);
                JSONObject standardPartJson = buildHistoryStandardPartJson(context, rootMap, createTime,
                        mandatoryAttributeDocument);
                String rootTitle = getMapValue(rootMap, DomainConstants.SELECT_ATTRIBUTE_TITLE);
                if (UIUtil.isNullOrEmpty(rootTitle)) {
                    rootTitle = getMapValue(rootMap, DomainConstants.SELECT_NAME);
                }
                String rootType = getMapValue(rootMap, DomainConstants.SELECT_TYPE);
                String fileName = buildHistoryStandardPartFileName(rootTitle, rootType, fileTime);
                File jsonFile = writeHistoryStandardPartJsonFile(fileName, standardPartJson);
                filePathArray.add(jsonFile.getAbsolutePath());
            }
            returnJson.put("filePath", filePathArray);
            returnJson.put("code", "200");
            returnJson.put("msg", "");
            returnJson.put("result", "success");
        } catch (Exception e) {
            logger.error("syncHistorystandardPart failed", e);
            returnJson.put("filePath", filePathArray);
            returnJson.put("code", "500");
            returnJson.put("msg", e.getMessage());
            returnJson.put("result", "failed");
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
            logger.info("syncHistorystandardPart end, filePath:{}", filePathArray);
        }
        return returnJson;
    }

    /**
     * 获取需要同步的标准件分类根节点。
     **
     * @param context 上下文
     * @return MapList General Library与General Class根节点
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 17:00
     */
    private MapList getHistoryStandardPartRootList(Context context) throws Exception {
        String classIdConfig = JF_PublicMethodClass_mxJPO.getBasicUrl(context,
                new String[]{"JfStandardPart.GeneralClassId"});
        String libraryConfig = JF_PublicMethodClass_mxJPO.getBasicUrl(context,
                new String[]{"JfStandardPart.Library"});
        if (UIUtil.isNullOrEmpty(classIdConfig) || UIUtil.isNullOrEmpty(libraryConfig)) {
            throw new Exception("未配置JfStandardPart.GeneralClassId或JfStandardPart.Library，无法同步标准件");
        }

        StringList rootSelects = new StringList();
        rootSelects.add(DomainConstants.SELECT_ID);
        rootSelects.add(DomainConstants.SELECT_NAME);
        rootSelects.add(DomainConstants.SELECT_TYPE);
        rootSelects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        MapList rootList = new MapList();
        Set<String> rootIdSet = new LinkedHashSet<String>();
        StringList libraryTitleList = FrameworkUtil.split(libraryConfig, ",");
        for (int i = 0; i < libraryTitleList.size(); i++) {
            String libraryTitle = ((String) libraryTitleList.get(i)).trim();
            if (UIUtil.isNullOrEmpty(libraryTitle)) {
                continue;
            }
            MapList libraryList = DomainObject.findObjects(context, "General Library", DomainConstants.QUERY_WILDCARD,
                    "attribute[Title] == '" + escapeWhereValue(libraryTitle) + "'", rootSelects);
            if (libraryList.isEmpty()) {
                throw new Exception("未找到标准件库:" + libraryTitle);
            }
            for (int j = 0; j < libraryList.size(); j++) {
                Map libraryMap = (Map) libraryList.get(j);
                if (rootIdSet.add(getMapValue(libraryMap, DomainConstants.SELECT_ID))) {
                    rootList.add(libraryMap);
                }
            }
        }

        StringList classIdList = FrameworkUtil.split(classIdConfig, ",");
        for (int i = 0; i < classIdList.size(); i++) {
            String classId = ((String) classIdList.get(i)).trim();
            if (UIUtil.isNullOrEmpty(classId)) {
                continue;
            }
            DomainObject classObject = DomainObject.newInstance(context, classId);
            Map classMap = classObject.getInfo(context, rootSelects);
            if (rootIdSet.add(classId)) {
                rootList.add(classMap);
            }
        }
        if (rootList.isEmpty()) {
            throw new Exception("未找到可同步的标准件库分类");
        }
        return rootList;
    }

    /**
     * 构建单个标准件库或分类根节点的JSON内容。
     **
     * @param context 上下文
     * @param rootMap 标准件库或分类根节点
     * @param createTime 生成时间
     * @param mandatoryAttributeDocument 必填属性配置文档
     * @return JSONObject 标准件JSON
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 17:00
     */
    private JSONObject buildHistoryStandardPartJson(Context context, Map rootMap, String createTime,
                                                    Document mandatoryAttributeDocument) throws Exception {
        JSONObject fileJson = new JSONObject(true);
        JSONObject syncInfo = new JSONObject(true);
        JSONArray partArray = new JSONArray();
        syncInfo.put("bomType", "standardPart");
        syncInfo.put("createTime", createTime);
        fileJson.put("syncInfo", syncInfo);
        fileJson.put("parts", partArray);

        String rootId = getMapValue(rootMap, DomainConstants.SELECT_ID);
        Set<String> classIdSet = getHistoryStandardPartClassIdSet(context, rootId);
        Map<String, Map> latestPartMap = getHistoryStandardPartLatestReleasedMap(context, classIdSet);
        Set<String> partKeySet = new LinkedHashSet<String>();
        JF_DR_mxJPO drJpo = new JF_DR_mxJPO();
        for (Map partMap : latestPartMap.values()) {
            addHistoryStandardPartJsonPart(context, partArray, partKeySet, partMap, drJpo,
                    mandatoryAttributeDocument);
        }
        return fileJson;
    }

    /**
     * 获取标准件根节点及其全部下级分类ID。
     **
     * @param context 上下文
     * @param rootId General Library或General Class ID
     * @return Set 分类ID集合
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 17:00
     */
    private Set<String> getHistoryStandardPartClassIdSet(Context context, String rootId) throws Exception {
        Set<String> classIdSet = new LinkedHashSet<String>();
        DomainObject rootObject = DomainObject.newInstance(context, rootId);
        classIdSet.add(rootId);
        StringList classSelects = new StringList(DomainConstants.SELECT_ID);
        MapList classList = rootObject.getRelatedObjects(context, "Subclass", "General Class", classSelects,
                new StringList(), false, true, (short) 0, "", "", 0);
        for (int i = 0; i < classList.size(); i++) {
            classIdSet.add(getMapValue((Map) classList.get(i), DomainConstants.SELECT_ID));
        }
        return classIdSet;
    }

    /**
     * 获取分类树下每个逻辑零件的最新已发布版本。
     **
     * @param context 上下文
     * @param classIdSet 分类ID集合
     * @return Map 逻辑ID与零件信息映射
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 17:00
     */
    private Map<String, Map> getHistoryStandardPartLatestReleasedMap(Context context, Set<String> classIdSet)
            throws Exception {
        Map<String, Map> latestPartMap = new LinkedHashMap<String, Map>();
        StringList partSelects = getHistoryStandardPartSelectList();
        for (String classId : classIdSet) {
            DomainObject classObject = DomainObject.newInstance(context, classId);
            MapList partList = classObject.getRelatedObjects(context, DomainRelationship.RELATIONSHIP_CLASSIFIED_ITEM,
                    TYPE_VPMREFERENCE, partSelects, new StringList(), false, true, (short) 1, "", "", 0);
            for (int i = 0; i < partList.size(); i++) {
                Map partMap = (Map) partList.get(i);
                if (!"RELEASED".equals(getMapValue(partMap, DomainConstants.SELECT_CURRENT))) {
                    continue;
                }
                String logicalId = getMapValue(partMap, "logicalid");
                if (UIUtil.isNullOrEmpty(logicalId)) {
                    logicalId = getMapValue(partMap, DomainConstants.SELECT_ID);
                }
                Map existingPartMap = latestPartMap.get(logicalId);
                if (existingPartMap == null || isHistorySupplyPartLaterReleased(partMap, existingPartMap)) {
                    latestPartMap.put(logicalId, partMap);
                }
            }
        }
        return latestPartMap;
    }

    /**
     * 获取标准件零件查询字段。
     **
     * @return StringList 零件查询字段
     * @author caipan
     * @date 2026/8/10 17:00
     */
    private StringList getHistoryStandardPartSelectList() {
        StringList selects = getHistorySupplyPartSelectList();
        selects.add("to[JFProject2RootPart|attribute[JF_BelongPart]==Y].from.id");
        //20260827 update by codex caipan 标准件增量接口按零件修改时间过滤
        selects.add(SELECT_MODIFIED_VALUE);
        selects.add("attribute[PLMReference.V_isLastVersion]");
        return selects;
    }

    /**
     * 将标准件信息写入JSON零件数组。
     **
     * @param context 上下文
     * @param partArray 零件JSON数组
     * @param partKeySet 已写入零件键集合
     * @param partMap 零件数据
     * @param drJpo DR业务JPO
     * @param mandatoryAttributeDocument 必填属性配置文档
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 17:00
     */
    private void addHistoryStandardPartJsonPart(Context context, JSONArray partArray, Set<String> partKeySet,
                                                Map partMap, JF_DR_mxJPO drJpo,
                                                Document mandatoryAttributeDocument) throws Exception {
        String projectId = getHistoryStandardPartProjectId(partMap);
        addHistorySupplyPartJsonPart(context, partArray, partKeySet, partMap, projectId, drJpo,
                mandatoryAttributeDocument);
    }

    /**
     * 获取标准件的首个归属项目ID。
     **
     * @param partMap 零件数据
     * @return String 归属项目ID
     * @author caipan
     * @date 2026/8/10 17:00
     */
    private String getHistoryStandardPartProjectId(Map partMap) {
        Object projectIdValue = partMap.get("to[JFProject2RootPart|attribute[JF_BelongPart]==Y].from.id");
        if (projectIdValue instanceof Collection) {
            Iterator iterator = ((Collection) projectIdValue).iterator();
            return iterator.hasNext() ? String.valueOf(iterator.next()) : DomainConstants.EMPTY_STRING;
        }
        return projectIdValue == null ? DomainConstants.EMPTY_STRING : String.valueOf(projectIdValue);
    }

    /**
     * 构建标准件同步文件名。
     **
     * @param rootTitle 根节点名称
     * @param rootType 根节点类型
     * @param fileTime 文件时间
     * @return String 文件名
     * @author caipan
     * @date 2026/8/10 17:00
     */
    private String buildHistoryStandardPartFileName(String rootTitle, String rootType, String fileTime) {
        String safeRootTitle = UIUtil.isNullOrEmpty(rootTitle) ? "StandardPart" : rootTitle;
        safeRootTitle = safeRootTitle.replaceAll("[\\\\/:*?\"<>|]", "_");
        String fileType = "General Class".equals(rootType) ? "standardPartClass" : "standardPart";
        return safeRootTitle + "_" + fileType + "_" + fileTime + ".json";
    }

    /**
     * 将标准件JSON写入/tmp目录。
     **
     * @param fileName 文件名
     * @param fileJson JSON内容
     * @return File 已生成文件
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 17:00
     */
    private File writeHistoryStandardPartJsonFile(String fileName, JSONObject fileJson) throws Exception {
        File rootDir = new File("/data/cp/AI/");
        if (!rootDir.exists() && !rootDir.mkdirs()) {
            throw new Exception("创建标准件同步文件目录失败:" + rootDir.getAbsolutePath());
        }
        File jsonFile = new File(rootDir, fileName);
        File tempFile = new File(rootDir, fileName + ".tmp");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            writer.write(fileJson.toJSONString());
        }
        if (jsonFile.exists() && !jsonFile.delete()) {
            throw new Exception("删除旧标准件同步文件失败:" + jsonFile.getAbsolutePath());
        }
        if (!tempFile.renameTo(jsonFile)) {
            throw new Exception("重命名标准件同步临时文件失败:" + tempFile.getAbsolutePath());
        }
        return jsonFile;
    }

    /**
     * 同步正式ECR关联制造件清单到AI接口。
     **
     * @param context 上下文
     * @param args ECR ID数组
     * @return JSONObject 接口即时返回结果
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 18:00
     */
    public JSONObject syncECRConnectMakePartList(Context context, String[] args) throws Exception {
        JSONObject returnJson = new JSONObject();
        JSONArray dataArray = new JSONArray();
        boolean pushed = false;
        try {
            ContextUtil.pushContext(context);
            pushed = true;
            if (args == null || args.length == 0) {
                throw new Exception("未传入ECR ID，无法同步制造件清单");
            }
            String fileTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Document mandatoryAttributeDocument = getHistorySupplyPartMandatoryAttributeDocument(context);
            Set<String> ecrIdSet = new LinkedHashSet<String>();
            for (int i = 0; i < args.length; i++) {
                if (UIUtil.isNotNullAndNotEmpty(args[i])) {
                    ecrIdSet.add(args[i].trim());
                }
            }
            if (ecrIdSet.isEmpty()) {
                throw new Exception("未传入有效ECR ID，无法同步制造件清单");
            }
            for (String ecrId : ecrIdSet) {
                DomainObject ecrObject = DomainObject.newInstance(context, ecrId);
                String ecrName = ecrObject.getInfo(context, DomainConstants.SELECT_NAME);
                String projectId = getHistoryECRProjectId(context, ecrObject);
                //20260827 update by codex caipan 获取ECR关联项目名称和描述写入同步信息
                String projectCode = DomainConstants.EMPTY_STRING;
                String projectName = DomainConstants.EMPTY_STRING;
                if (UIUtil.isNotNullAndNotEmpty(projectId)) {
                    DomainObject projectObject = DomainObject.newInstance(context, projectId);
                    projectCode = projectObject.getInfo(context, DomainConstants.SELECT_NAME);
                    projectName = projectObject.getInfo(context, DomainConstants.SELECT_DESCRIPTION);
                }
                MapList tableDataList = getHistoryECRMakeTableData(context, ecrId);
                JSONObject ecrJson = buildHistoryECRMakePartJson(context, tableDataList, projectId, ecrName,
                        projectCode, projectName, createTime, mandatoryAttributeDocument);
                dataArray.add(ecrJson);
                File jsonFile = writeHistorySupplyPartEBOMJsonFile(buildHistoryECRMakePartFileName(ecrName, fileTime),
                        ecrJson);
                //20260827 update by codex caipan 记录生成文件路径，避免改变对外接口返回结构
                ThreadLog.info("syncECRConnectMakePartList generated file, ECR ID:{}, filePath:{}", ecrId,
                        jsonFile.getAbsolutePath());
            }
            returnJson.put("dataArray", dataArray);
            returnJson.put("code", "200");
            returnJson.put("msg", "");
            returnJson.put("result", "success");
        } catch (Exception e) {
            logger.error("syncECRConnectMakePartList failed", e);
            returnJson.put("dataArray", dataArray);
            returnJson.put("code", "500");
            returnJson.put("msg", e.getMessage());
            returnJson.put("result", "failed");
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
        }
        return returnJson;
    }

    /**
     * 获取正式ECR制造件清单表格数据。
     **
     * @param context 上下文
     * @param ecrId ECR ID
     * @return MapList 制造件清单数据
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 18:00
     */
    private MapList getHistoryECRMakeTableData(Context context, String ecrId) throws Exception {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(STRING_OBJECTID, ecrId);
        parameters.put(STRING_SELECT_TABLE, "JFFormalECRController");
        return (MapList) JPO.invoke(context, "JF_FormalECRService", null, "getFormalECRMakeTableData",
                JPO.packArgs(parameters), MapList.class);
    }

    /**
     * 构建正式ECR制造件清单AI JSON。
     **
     * @param context 上下文
     * @param tableDataList 制造件清单数据
     * @param projectId ECR关联项目ID
     * @param ecrName ECR名称
     * @param projectCode ECR关联项目名称
     * @param projectName ECR关联项目描述
     * @param createTime 生成时间
     * @param mandatoryAttributeDocument 必填属性配置文档
     * @return JSONObject ECR制造件清单JSON
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 18:00
     */
    private JSONObject buildHistoryECRMakePartJson(Context context, MapList tableDataList, String projectId,
                                                   String ecrName, String projectCode, String projectName, String createTime,
                                                   Document mandatoryAttributeDocument) throws Exception {
        JSONObject fileJson = new JSONObject(true);
        JSONObject syncInfo = new JSONObject(true);
        JSONArray rootList = new JSONArray();
        JSONArray partArray = new JSONArray();
        JSONArray changeDifferenceArray = new JSONArray();
        syncInfo.put("rootList", rootList);
        syncInfo.put("bomType", "ECR");
        syncInfo.put("ECRName", ecrName);
        syncInfo.put("ProjectId", projectCode);
        syncInfo.put("ProjectName", projectName);
        syncInfo.put("createTime", createTime);
        fileJson.put("syncInfo", syncInfo);
        fileJson.put("parts", partArray);
        fileJson.put("changeDifferences", changeDifferenceArray);
        if (tableDataList == null || tableDataList.isEmpty()) {
            return fileJson;
        }

        StringList partIdList = new StringList();
        Set<String> partIdSet = new LinkedHashSet<String>();
        for (int i = 0; i < tableDataList.size(); i++) {
            Map tableRowMap = (Map) tableDataList.get(i);
            String partId = getMapValue(tableRowMap, DomainConstants.SELECT_ID);
            if (UIUtil.isNotNullAndNotEmpty(partId) && partIdSet.add(partId)) {
                partIdList.add(partId);
            }
        }
        Map<String, Map> partInfoMap = getHistoryECRPartInfoMap(context, partIdList);
        Set<String> partKeySet = new LinkedHashSet<String>();
        Set<String> rootKeySet = new LinkedHashSet<String>();
        JF_DR_mxJPO drJpo = new JF_DR_mxJPO();
        for (int i = 0; i < tableDataList.size(); i++) {
            Map tableRowMap = (Map) tableDataList.get(i);
            String partId = getMapValue(tableRowMap, DomainConstants.SELECT_ID);
            Map partInfoMapRow = partInfoMap.get(partId);
            if (partInfoMapRow == null) {
                continue;
            }
            if (isHistoryECRRootRelation(getMapValue(tableRowMap, RELATIONSHIP))) {
                String rootKey = partId + "|" + getMapValue(partInfoMapRow, DomainConstants.SELECT_REVISION);
                if (rootKeySet.add(rootKey)) {
                    JSONObject rootJson = new JSONObject();
                    rootJson.put("name", getHistorySupplyPartJsonValue(partInfoMapRow, DomainConstants.SELECT_NAME));
                    rootJson.put("rootPartNumber", getHistorySupplyPartJsonValue(partInfoMapRow, SELECT_ATTR_V_PART_NUMBER));
                    rootJson.put("revision", getHistorySupplyPartJsonValue(partInfoMapRow, DomainConstants.SELECT_REVISION));
                    rootList.add(rootJson);
                }
            }
            addHistorySupplyPartJsonPart(context, partArray, partKeySet, partInfoMapRow, projectId, drJpo,
                    mandatoryAttributeDocument);
            //20260827 update by codex caipan 根节点不作为BOM子件输出变更关系
            if (!isHistoryECRRootRelation(getMapValue(tableRowMap, RELATIONSHIP))) {
                changeDifferenceArray.add(buildHistoryECRChangeDifferenceJson(tableDataList, i, tableRowMap));
            }
        }
        return fileJson;
    }

    /**
     * 批量获取ECR制造件完整属性信息。
     **
     * @param context 上下文
     * @param partIdList 零件ID集合
     * @return Map 零件ID与属性信息映射
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 18:00
     */
    private Map<String, Map> getHistoryECRPartInfoMap(Context context, StringList partIdList) throws Exception {
        Map<String, Map> partInfoMap = new LinkedHashMap<String, Map>();
        if (partIdList == null || partIdList.isEmpty()) {
            return partInfoMap;
        }
        MapList partInfoList = DomainObject.getInfo(context, partIdList.toStringArray(), getHistorySupplyPartSelectList());
        for (int i = 0; i < partInfoList.size(); i++) {
            Map partMap = (Map) partInfoList.get(i);
            partInfoMap.put(getMapValue(partMap, DomainConstants.SELECT_ID), partMap);
        }
        return partInfoMap;
    }

    /**
     * 构建ECR BOM变更关系JSON。
     **
     * @param tableDataList 制造件清单数据
     * @param rowIndex 当前行索引
     * @param childMap 当前子件关系数据
     * @return JSONObject 变更关系JSON
     * @author caipan
     * @date 2026/8/10 18:00
     */
    private JSONObject buildHistoryECRChangeDifferenceJson(MapList tableDataList, int rowIndex, Map childMap) {
        Map parentMap = findFormalECRParentPartMap(tableDataList, rowIndex);
        JSONObject differenceJson = new JSONObject();
        differenceJson.put("parentName", parentMap == null ? "" : getMapValue(parentMap, DomainConstants.SELECT_NAME));
        differenceJson.put("instanceId", getMapValue(childMap, SELECT_CONNECTION_ID_VALUE));
        differenceJson.put("parentRevision", parentMap == null ? "" : getMapValue(parentMap, DomainConstants.SELECT_REVISION));
        differenceJson.put("childName", getMapValue(childMap, DomainConstants.SELECT_NAME));
        differenceJson.put("childRevision", getMapValue(childMap, DomainConstants.SELECT_REVISION));
        differenceJson.put("JF_BOMQuantity", getMapValue(childMap, SELECT_ATTRIBUTE_JF_BOMQuantity));
        differenceJson.put("JF_BOMChangeDes", getMapValue(childMap, SELECT_ATTRIBUTE_JF_BOMChangeDes));
        differenceJson.put("JF_BOMChangeQuantity", getMapValue(childMap, SELECT_ATTRIBUTE_JF_BOMChangeQuantity));
        differenceJson.put("JF_BOMBeforeQuantity", getMapValue(childMap, SELECT_ATTRIBUTE_JF_BOMBeforeQuantity));
        return differenceJson;
    }

    /**
     * 判断是否为ECR根零件关系。
     **
     * @param relationshipName 关系名称
     * @return boolean 根零件关系时返回true
     * @author caipan
     * @date 2026/8/10 18:00
     */
    private boolean isHistoryECRRootRelation(String relationshipName) {
        return REL_JFECRRelateRoot.equals(relationshipName) || REL_JFECRRelateRootBubble.equals(relationshipName);
    }

    /**
     * 获取ECR关联项目ID。
     **
     * @param context 上下文
     * @param ecrObject ECR对象
     * @return String 项目ID
     * @throws Exception 异常
     * @author caipan
     * @date 2026/8/10 18:00
     */
    private String getHistoryECRProjectId(Context context, DomainObject ecrObject) throws Exception {
        Object projectIdValue = ecrObject.getInfo(context, "from[JFChange2Project].to.id");
        if (projectIdValue instanceof Collection) {
            Iterator iterator = ((Collection) projectIdValue).iterator();
            return iterator.hasNext() ? String.valueOf(iterator.next()) : DomainConstants.EMPTY_STRING;
        }
        return projectIdValue == null ? DomainConstants.EMPTY_STRING : String.valueOf(projectIdValue);
    }

    /**
     * 构建ECR制造件清单文件名。
     **
     * @param ecrName ECR名称
     * @param fileTime 文件时间
     * @return String 文件名
     * @author caipan
     * @date 2026/8/10 18:00
     */
    private String buildHistoryECRMakePartFileName(String ecrName, String fileTime) {
        String safeECRName = UIUtil.isNullOrEmpty(ecrName) ? "ECR" : ecrName;
        safeECRName = safeECRName.replaceAll("[\\\\/:*?\"<>|]", "_");
        //20260827 update by codex caipan ECR同步文件名使用ECR名称加时间戳
        return safeECRName + "_" + fileTime + ".json";
    }
    /**
     * 查询零件项目对应的客户零件号/DB主关系
     * @param context 上下文
     * @param partId 零件ID
     * @param projectId 所属项目ID
     * @author LIUJR
     * @throws Exception 异常
     * @return java.util.Map 客户零件号/DB关系信息
     * @date 2026/7/21
     * @description 一个零件可以维护多个客户零件号/DB，DR校验只读取项目对应的JFVPMReference2CustomerParts关系属性。
     */
    public Map getDRCustomerPartsDBRelationInfo(Context context, String partId, String projectId) throws Exception {
        if (UIUtil.isNullOrEmpty(partId) || UIUtil.isNullOrEmpty(projectId)) {
            return new HashMap();
        }
        String projectIdSelect = "frommid[" + RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT + "].to.id";
        StringList objectSelectList = JF_Util_mxJPO.basicBolistSel();
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add(projectIdSelect);
        relSelectList.add(Select_Attr_JFCustomerPartNumber);
        relSelectList.add(Select_Attr_JFCustomerPartName);
        relSelectList.add(Select_Attr_JFCustomerPartRevision);
        relSelectList.add(Select_Attr_JF_DirectBuy);
        DomainObject partObject = DomainObject.newInstance(context, partId);
        MapList relatedList = partObject.getRelatedObjects(context,
                RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS,
                TYPE_JFCUSTOMERPARTS,
                objectSelectList,
                relSelectList,
                true,
                false,
                (short) 1,
                "",
                "",
                0);
        for (int i = 0; i < relatedList.size(); i++) {
            Map relatedMap = (Map) relatedList.get(i);
            if (projectId.equals(UIUtil.getValue(relatedMap, projectIdSelect))) {
                return relatedMap;
            }
        }
        return new HashMap();
    }

    /**
     * 同步快照关联零件及其EBOM信息到JSON文件。
     **
     * @param context 上下文
     * @param args 快照ID数组
     * @return JSONObject 接口即时返回结果
     * @throws Exception 异常
     * @author caipan by codex
     * @date 2026/8/27
     */
    public JSONObject syncSnapshotPartList(Context context, String[] args) throws Exception {
        JSONObject returnJson = new JSONObject();
        JSONArray dataArray = new JSONArray();
        boolean pushed = false;
        try {
            ContextUtil.pushContext(context);
            pushed = true;
            if (args == null || args.length == 0) {
                throw new Exception("未传入快照ID，无法同步快照零件清单");
            }
            Set<String> snapshotIdSet = new LinkedHashSet<String>();
            for (int i = 0; i < args.length; i++) {
                if (UIUtil.isNotNullAndNotEmpty(args[i])) {
                    snapshotIdSet.add(args[i].trim());
                }
            }
            if (snapshotIdSet.isEmpty()) {
                throw new Exception("未传入有效快照ID，无法同步快照零件清单");
            }
            String fileTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Document mandatoryAttributeDocument = getHistorySupplyPartMandatoryAttributeDocument(context);
            for (String snapshotId : snapshotIdSet) {
                DomainObject snapshotObject = DomainObject.newInstance(context, snapshotId);
                String snapshotName = snapshotObject.getInfo(context, DomainConstants.SELECT_NAME);
                JSONObject snapshotJson = buildSnapshotPartListJson(context, snapshotObject, createTime,
                        mandatoryAttributeDocument);
                dataArray.add(snapshotJson);
                File jsonFile = writeHistorySupplyPartEBOMJsonFile(buildSnapshotPartListFileName(snapshotName, fileTime),
                        snapshotJson);
                ThreadLog.info("syncSnapshotPartList generated file, snapshot ID:{}, snapshot:{}, filePath:{}", snapshotId,
                        snapshotName, jsonFile.getAbsolutePath());
            }
            returnJson.put("dataArray", dataArray);
            returnJson.put("code", "200");
            returnJson.put("msg", "");
            returnJson.put("result", "success");
        } catch (Exception e) {
            logger.error("syncSnapshotPartList failed", e);
            returnJson.put("dataArray", dataArray);
            returnJson.put("code", "500");
            returnJson.put("msg", e.getMessage());
            returnJson.put("result", "failed");
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
        }
        return returnJson;
    }

    /**
     * 构建单个快照关联零件及其EBOM的JSON内容。
     **
     * @param context 上下文
     * @param snapshotObject 快照对象
     * @param createTime 生成时间
     * @param mandatoryAttributeDocument 必填属性配置文档
     * @return JSONObject 快照零件清单JSON
     * @throws Exception 异常
     * @author caipan by codex
     * @date 2026/8/27
     */
    private JSONObject buildSnapshotPartListJson(Context context, DomainObject snapshotObject, String createTime,
                                                 Document mandatoryAttributeDocument) throws Exception {
        String projectIdSelect = "to[" + rel_JFProject2Snapshot + "].from.id";
        String projectNameSelect = "to[" + rel_JFProject2Snapshot + "].from.name";
        String projectDescriptionSelect = "to[" + rel_JFProject2Snapshot + "].from.description";
        StringList snapshotSelects = new StringList();
        snapshotSelects.add(DomainConstants.SELECT_NAME);
        snapshotSelects.add(projectIdSelect);
        snapshotSelects.add(projectNameSelect);
        snapshotSelects.add(projectDescriptionSelect);
        Map snapshotInfo = snapshotObject.getInfo(context, snapshotSelects);
        String projectId = getMapValue(snapshotInfo, projectIdSelect);
        String projectCode = getMapValue(snapshotInfo, projectNameSelect);
        String projectName = getMapValue(snapshotInfo, projectDescriptionSelect);
        String snapshotName = getMapValue(snapshotInfo, DomainConstants.SELECT_NAME);

        JSONObject fileJson = new JSONObject(true);
        JSONObject syncInfo = new JSONObject(true);
        JSONArray rootList = new JSONArray();
        JSONArray partArray = new JSONArray();
        JSONArray bomArray = new JSONArray();
        syncInfo.put("rootList", rootList);
        syncInfo.put("bomType", "COSTING");
        syncInfo.put("createTime", createTime);
        syncInfo.put("Snapshot", snapshotName);
        syncInfo.put("projectId", projectCode);
        syncInfo.put("projectName", projectName);
        fileJson.put("syncInfo", syncInfo);
        fileJson.put("parts", partArray);
        fileJson.put("boms", bomArray);

        StringList rootPartIdList = snapshotObject.getInfoList(context,
                "from[" + rel_JFSnapshot2VPMReference + "].to.id");
        Set<String> rootPartIdSet = new LinkedHashSet<String>();
        if (rootPartIdList != null) {
            for (Object rootPartIdObject : rootPartIdList) {
                String rootPartId = rootPartIdObject == null ? "" : String.valueOf(rootPartIdObject);
                if (UIUtil.isNotNullAndNotEmpty(rootPartId)) {
                    rootPartIdSet.add(rootPartId);
                }
            }
        }
        if (rootPartIdSet.isEmpty()) {
            return fileJson;
        }

        Map<String, Map> rootPartMapById = new LinkedHashMap<String, Map>();
        MapList rootPartInfoList = DomainObject.getInfo(context, rootPartIdSet.toArray(new String[rootPartIdSet.size()]),
                getHistorySupplyPartSelectList());
        for (int i = 0; i < rootPartInfoList.size(); i++) {
            Map rootPartMap = (Map) rootPartInfoList.get(i);
            rootPartMapById.put(getMapValue(rootPartMap, DomainConstants.SELECT_ID), rootPartMap);
        }

        Set<String> partKeySet = new LinkedHashSet<String>();
        Map<String, Map> sameBomMap = new LinkedHashMap<String, Map>();
        JF_DR_mxJPO drJpo = new JF_DR_mxJPO();
        for (String rootPartId : rootPartIdSet) {
            Map rootPartMap = rootPartMapById.get(rootPartId);
            if (rootPartMap == null) {
                continue;
            }
            JSONObject rootJson = new JSONObject();
            rootJson.put("name", getHistorySupplyPartJsonValue(rootPartMap, DomainConstants.SELECT_NAME));
            rootJson.put("rootPartNumber", getHistorySupplyPartJsonValue(rootPartMap, SELECT_ATTR_V_PART_NUMBER));
            rootJson.put("revision", getHistorySupplyPartJsonValue(rootPartMap, DomainConstants.SELECT_REVISION));
            rootList.add(rootJson);
            addSnapshotPartJsonPart(context, partArray, partKeySet, rootPartMap, projectId, drJpo,
                    mandatoryAttributeDocument);

            DomainObject rootPartObject = DomainObject.newInstance(context, rootPartId);
            MapList childPartList = rootPartObject.getRelatedObjects(context,
                    "VPMInstance",
                    TYPE_VPMREFERENCE,
                    getHistorySupplyPartSelectList(),
                    getHistorySupplyPartBOMRelationshipSelectList(),
                    false,
                    true,
                    (short) 0,
                    "",
                    "",
                    0);
            int mergedLevel = Integer.MAX_VALUE;
            for (int i = 0; i < childPartList.size(); i++) {
                Map childPartMap = (Map) childPartList.get(i);
                addSnapshotPartJsonPart(context, partArray, partKeySet, childPartMap, projectId, drJpo,
                        mandatoryAttributeDocument);
                int childLevel = Integer.parseInt(getMapValue(childPartMap, "level"));
                if (childLevel > mergedLevel) {
                    continue;
                }
                mergedLevel = Integer.MAX_VALUE;
                if (mergeHistorySupplyPartBOM(sameBomMap, childPartMap)) {
                    mergedLevel = childLevel;
                }
            }
        }
        for (Map mergedBomMap : sameBomMap.values()) {
            bomArray.add(buildHistorySupplyPartBOMJson(mergedBomMap));
        }
        return fileJson;
    }

    /**
     * 将快照零件写入JSON，并以快照项目查询客户零件号信息。
     **
     * @param context 上下文
     * @param partArray 零件JSON数组
     * @param partKeySet 已写入零件键集合
     * @param partMap 零件数据
     * @param snapshotProjectId 快照关联项目ID
     * @param drJpo DR业务JPO
     * @param mandatoryAttributeDocument 必填属性配置文档
     * @return void
     * @throws Exception 异常
     * @author caipan by codex
     * @date 2026/8/27
     */
    private void addSnapshotPartJsonPart(Context context, JSONArray partArray, Set<String> partKeySet, Map partMap,
                                         String snapshotProjectId, JF_DR_mxJPO drJpo,
                                         Document mandatoryAttributeDocument) throws Exception {
        int partCount = partArray.size();
        addHistorySupplyPartJsonPart(context, partArray, partKeySet, partMap, snapshotProjectId, drJpo,
                mandatoryAttributeDocument);
        if (partArray.size() == partCount) {
            return;
        }
        JSONObject partJson = (JSONObject) partArray.get(partArray.size() - 1);
        Object belongProjectId = partJson.remove("ProjectID");
        partJson.put("ProjectId", belongProjectId == null ? "" : belongProjectId);
    }

    /**
     * 在单个JSON范围内合并相同父子零件的BOM关系。
     **
     * @param sameBomMap 已合并的BOM关系
     * @param childPartMap 当前BOM关系数据
     * @return boolean 发生合并时返回true
     * @author caipan by codex
     * @date 2026/8/27
     */
    private boolean mergeHistorySupplyPartBOM(Map<String, Map> sameBomMap, Map childPartMap) {
        String bomKey = getMapValue(childPartMap, "from.id") + getMapValue(childPartMap, DomainConstants.SELECT_ID);
        Map mergedBomMap = sameBomMap.get(bomKey);
        if (mergedBomMap == null) {
            sameBomMap.put(bomKey, childPartMap);
            return false;
        }
        double dosage = Double.parseDouble(getMapValue(mergedBomMap, "attribute[JF_VPMInstance.JF_Dosage]"))
                + Double.parseDouble(getMapValue(childPartMap, "attribute[JF_VPMInstance.JF_Dosage]"));
        mergedBomMap.put("attribute[JF_VPMInstance.JF_Dosage]", String.valueOf(dosage));
        String externalId = getMapValue(childPartMap, "attribute[PLMInstance.PLM_ExternalID]");
        if (UIUtil.isNotNullAndNotEmpty(externalId)) {
            String mergedExternalId = getMapValue(mergedBomMap, "attribute[PLMInstance.PLM_ExternalID]");
            mergedBomMap.put("attribute[PLMInstance.PLM_ExternalID]", UIUtil.isNullOrEmpty(mergedExternalId)
                    ? externalId : mergedExternalId + "," + externalId);
        }
        return true;
    }

    /**
     * 构建快照零件清单JSON文件名。
     **
     * @param snapshotName 快照编码
     * @param fileTime 文件时间
     * @return String 文件名
     * @author caipan by codex
     * @date 2026/8/27
     */
    private String buildSnapshotPartListFileName(String snapshotName, String fileTime) {
        String safeSnapshotName = UIUtil.isNullOrEmpty(snapshotName) ? "Snapshot" : snapshotName;
        safeSnapshotName = safeSnapshotName.replaceAll("[\\\\/:*?\"<>|]", "_");
        return safeSnapshotName + "_" + fileTime + ".json";
    }

    /**
     * 获取历史同步零件关联的图纸信息。
     **
     * @param context 上下文
     * @param partId 零件ID
     * @return JSONArray 图纸JSON数组
     * @throws Exception 异常
     * @author caipan by codex
     * @date 2026/8/27
     */
    private JSONArray getHistoryPartDrawingArray(Context context, String partId) throws Exception {
        JSONArray drawingArray = new JSONArray();
        if (UIUtil.isNullOrEmpty(partId)) {
            return drawingArray;
        }
        DomainObject partObject = DomainObject.newInstance(context, partId);
        MapList documentDrawingList = partObject.getRelatedObjects(context,
                REL_ReferenceDocument,
                TYPE_Document,
                getDrawingSelectList(),
                getDrawingRelSelectList(),
                false,
                true,
                (short) 1,
                SELECT_ATTR_JF_DocumentType + ".value==Drawing",
                "",
                0);
        addHistoryPartDrawingJsonArray(drawingArray, documentDrawingList, DomainConstants.SELECT_ATTRIBUTE_TITLE);

        MapList drawingList = partObject.getRelatedObjects(context,
                REL_XCADBaseDependency,
                TYPE_Drawing,
                getDrawingSelectList(),
                getDrawingRelSelectList(),
                true,
                false,
                (short) 1,
                "",
                "",
                0);
        addHistoryPartDrawingJsonArray(drawingArray, drawingList, SELECT_ATTR_PLMENTITY_V_NAME);
        return drawingArray;
    }

    /**
     * 将图纸数据转换为历史同步使用的JSON结构。
     **
     * @param drawingArray 图纸JSON数组
     * @param drawingList 图纸数据
     * @param titleSelect 图纸标题查询字段
     * @return void
     * @author caipan by codex
     * @date 2026/8/27
     */
    private void addHistoryPartDrawingJsonArray(JSONArray drawingArray, MapList drawingList, String titleSelect) {
        for (Object drawingObject : drawingList) {
            Map drawingMap = (Map) drawingObject;
            JSONObject drawingJson = new JSONObject();
            drawingJson.put("revision", getHistorySupplyPartJsonValue(drawingMap, DomainConstants.SELECT_REVISION));
            drawingJson.put("name", getHistorySupplyPartJsonValue(drawingMap, DomainConstants.SELECT_NAME));
            drawingJson.put("Title", getHistorySupplyPartJsonValue(drawingMap, titleSelect));
            drawingJson.put("current", getHistorySupplyPartJsonValue(drawingMap, DomainConstants.SELECT_CURRENT));
            drawingArray.add(drawingJson);
        }
    }

    /**
     * 同步标准件申请单直接关联的零件信息。
     **
     * @param context 上下文
     * @param args 标准件申请单ID数组
     * @return JSONObject 接口返回结果
     * @throws Exception 异常
     * @author caipan by codex
     * @date 2026/8/27
     */
    public JSONObject syncStandardPartList(Context context, String[] args) throws Exception {
        JSONObject returnJson = new JSONObject();
        JSONArray dataArray = new JSONArray();
        boolean pushed = false;
        try {
            ContextUtil.pushContext(context);
            pushed = true;
            Set<String> applicationIdSet = new LinkedHashSet<String>();
            if (args != null) {
                for (String applicationId : args) {
                    if (UIUtil.isNotNullAndNotEmpty(applicationId)) {
                        applicationIdSet.add(applicationId.trim());
                    }
                }
            }
            if (applicationIdSet.isEmpty()) {
                throw new Exception("未传入有效标准件申请单ID，无法同步标准件申请单零件");
            }
            String fileTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Document mandatoryAttributeDocument = getHistorySupplyPartMandatoryAttributeDocument(context);
            for (String applicationId : applicationIdSet) {
                DomainObject applicationObject = DomainObject.newInstance(context, applicationId);
                String applicationName = applicationObject.getInfo(context, DomainConstants.SELECT_NAME);
                JSONObject applicationJson = buildStandardPartListJson(context, applicationObject, applicationName,
                        createTime, mandatoryAttributeDocument);
                dataArray.add(applicationJson);
                File jsonFile = writeHistorySupplyPartEBOMJsonFile(buildStandardPartListFileName(applicationName, fileTime),
                        applicationJson);
                ThreadLog.info("syncStandardPartList generated file, application ID:{}, name:{}, filePath:{}",
                        applicationId, applicationName, jsonFile.getAbsolutePath());
            }
            returnJson.put("dataArray", dataArray);
            returnJson.put("code", "200");
            returnJson.put("msg", "");
            returnJson.put("result", "success");
        } catch (Exception e) {
            logger.error("syncStandardPartList failed", e);
            returnJson.put("dataArray", dataArray);
            returnJson.put("code", "500");
            returnJson.put("msg", e.getMessage());
            returnJson.put("result", "failed");
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
        }
        return returnJson;
    }

    /**
     * 构建单个标准件申请单零件同步JSON。
     **
     * @param context 上下文
     * @param applicationObject 标准件申请单对象
     * @param applicationName 标准件申请单名称
     * @param createTime 生成时间
     * @param mandatoryAttributeDocument 必填属性配置文档
     * @return JSONObject 标准件申请单JSON
     * @throws Exception 异常
     * @author caipan by codex
     * @date 2026/8/27
     */
    private JSONObject buildStandardPartListJson(Context context, DomainObject applicationObject, String applicationName,
                                                 String createTime, Document mandatoryAttributeDocument) throws Exception {
        JSONObject applicationJson = new JSONObject(true);
        JSONObject syncInfo = new JSONObject(true);
        JSONArray partArray = new JSONArray();
        syncInfo.put("bomType", "standardPart");
        syncInfo.put("createTime", createTime);
        syncInfo.put("name", applicationName);
        applicationJson.put("syncInfo", syncInfo);
        applicationJson.put("parts", partArray);

        MapList partList = applicationObject.getRelatedObjects(context,
                REL_JFS_PARTS_APPLICATION_2_VPM,
                TYPE_VPMREFERENCE,
                getHistoryStandardPartSelectList(),
                new StringList(),
                false,
                true,
                (short) 1,
                "",
                "",
                0);
        Set<String> partKeySet = new LinkedHashSet<String>();
        JF_DR_mxJPO drJpo = new JF_DR_mxJPO();
        for (Object partObject : partList) {
            addHistoryStandardPartJsonPart(context, partArray, partKeySet, (Map) partObject, drJpo,
                    mandatoryAttributeDocument);
        }
        return applicationJson;
    }

    /**
     * 构建标准件申请单同步文件名。
     **
     * @param applicationName 标准件申请单名称
     * @param fileTime 文件时间
     * @return String 文件名
     * @author caipan by codex
     * @date 2026/8/27
     */
    private String buildStandardPartListFileName(String applicationName, String fileTime) {
        String safeApplicationName = UIUtil.isNullOrEmpty(applicationName) ? "StandardPartApplication" : applicationName;
        safeApplicationName = safeApplicationName.replaceAll("[\\\\/:*?\"<>|]", "_");
        return safeApplicationName + "_" + fileTime + ".json";
    }

    /**
     * 根据库或分类标题查询标准件详细信息。
     **
     * @param context 上下文
     * @param args 请求参数，Title为General Class或General Library标题，SyncType为increment或full
     * @return JSONObject 标准件详细信息
     * @throws Exception 异常
     * @author caipan by codex
     * @date 2026/8/27
     */
    public JSONObject getStandardPartList(Context context, String[] args) throws Exception {
        JSONObject returnJson = new JSONObject();
        JSONArray dataArray = new JSONArray();
        boolean pushed = false;
        try {
            Map requestMap = (Map) JPO.unpackArgs(args);
            logger.info("requestMap:{}",requestMap);
            String title = getMapValue(requestMap, "Title");
            String syncType = getMapValue(requestMap, "SyncType");
            if (UIUtil.isNullOrEmpty(title)) {
                throw new Exception("请求参数Title为空，无法查询标准件");
            }
            if (!"increment".equalsIgnoreCase(syncType) && !"full".equalsIgnoreCase(syncType)) {
                throw new Exception("请求参数SyncType仅支持increment或full");
            }
            ContextUtil.pushContext(context);
            pushed = true;

            StringList rootSelects = new StringList();
            rootSelects.add(DomainConstants.SELECT_ID);
            MapList rootList = DomainObject.findObjects(context,
                    "General Class,General Library",
                    DomainConstants.QUERY_WILDCARD,
                    "attribute[Title] == '" + escapeWhereValue(title.trim()) + "'",
                    rootSelects);

            Set<String> classIdSet = new LinkedHashSet<String>();
            for (Object rootObject : rootList) {
                String rootId = getMapValue((Map) rootObject, DomainConstants.SELECT_ID);
                if (UIUtil.isNotNullAndNotEmpty(rootId)) {
                    classIdSet.addAll(getHistoryStandardPartClassIdSet(context, rootId));
                }
            }
            Map<String, Map> latestPartMap = getStandardPartLatestMap(context, classIdSet);
            Set<String> partKeySet = new LinkedHashSet<String>();
            JF_DR_mxJPO drJpo = new JF_DR_mxJPO();
            Document mandatoryAttributeDocument = getHistorySupplyPartMandatoryAttributeDocument(context);
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.set(Calendar.HOUR_OF_DAY, 0);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.SECOND, 0);
            startCalendar.set(Calendar.MILLISECOND, 0);
            Date startTime = startCalendar.getTime();
            Calendar endCalendar = (Calendar) startCalendar.clone();
            endCalendar.add(Calendar.DAY_OF_MONTH, 1);
            Date endTime = endCalendar.getTime();
            for (Map partMap : latestPartMap.values()) {
                if ("increment".equalsIgnoreCase(syncType)) {
                    String modified = getMapValue(partMap, SELECT_MODIFIED_VALUE);
                    Date modifiedTime;
                    try {
                        modifiedTime = eMatrixDateFormat.getJavaDate(modified);
                    } catch (Exception e) {
                        logger.info("getStandardPartList parse modified time failed, partId:{}, modified:{}",
                                getMapValue(partMap, DomainConstants.SELECT_ID), modified);
                        continue;
                    }
                    if (modifiedTime.before(startTime) || !modifiedTime.before(endTime)) {
                        continue;
                    }
                }
                addHistoryStandardPartJsonPart(context, dataArray, partKeySet, partMap, drJpo,
                        mandatoryAttributeDocument);
            }
            returnJson.put("dataArray", dataArray);
            returnJson.put("code", "200");
            returnJson.put("msg", "");
            returnJson.put("result", "success");
        } catch (Exception e) {
            logger.error("getStandardPartList failed", e);
            returnJson.put("dataArray", dataArray);
            returnJson.put("code", "500");
            returnJson.put("msg", e.getMessage());
            returnJson.put("result", "failed");
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
        }
        return returnJson;
    }

    /**
     * 获取分类树下每个逻辑零件的最新版，不限制零件成熟度状态。
     **
     * @param context 上下文
     * @param classIdSet 分类ID集合
     * @return Map 逻辑ID与最新版本零件信息映射
     * @throws Exception 异常
     * @author caipan by codex
     * @date 2026/8/27
     */
    private Map<String, Map> getStandardPartLatestMap(Context context, Set<String> classIdSet) throws Exception {
        Map<String, Map> latestPartMap = new LinkedHashMap<String, Map>();
        StringList partSelects = getHistoryStandardPartSelectList();
        String lastVersionSelect = "attribute[PLMReference.V_isLastVersion]";
        for (String classId : classIdSet) {
            DomainObject classObject = DomainObject.newInstance(context, classId);
            MapList partList = classObject.getRelatedObjects(context,
                    DomainRelationship.RELATIONSHIP_CLASSIFIED_ITEM,
                    TYPE_VPMREFERENCE,
                    partSelects,
                    new StringList(),
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    0);
            for (Object partObject : partList) {
                Map partMap = (Map) partObject;
                if (!"TRUE".equalsIgnoreCase(getMapValue(partMap, lastVersionSelect))) {
                    continue;
                }
                String logicalId = getMapValue(partMap, "logicalid");
                if (UIUtil.isNullOrEmpty(logicalId)) {
                    logicalId = getMapValue(partMap, DomainConstants.SELECT_ID);
                }
                latestPartMap.put(logicalId, partMap);
            }
        }
        return latestPartMap;
    }

    /**
     * 查询同时具有指定许可的人员列表。
     **
     * @param context 上下文
     * @param args 请求参数，products为许可名称数组
     * @return JSONObject 具有全部指定许可的人员信息
     * @throws Exception 异常
     * @author caipan by codex
     * @date 2026/9/2
     */
    public JSONObject getLicensedPersonList(Context context, String[] args) throws Exception {
        JSONObject returnJson = new JSONObject(true);
        JSONArray dataArray = new JSONArray();
        boolean pushed = false;
        try {
            Map requestMap = (Map) JPO.unpackArgs(args);
            Object productsValue = requestMap == null ? null : requestMap.get("products");
            if (!(productsValue instanceof Collection)) {
                throw new Exception("请求参数products必须为非空数组");
            }

            Set<String> productSet = new LinkedHashSet<String>();
            for (Object productValue : (Collection) productsValue) {
                String product = productValue == null ? "" : String.valueOf(productValue).trim();
                if (UIUtil.isNotNullAndNotEmpty(product)) {
                    productSet.add(product);
                }
            }
            if (productSet.isEmpty()) {
                throw new Exception("请求参数products必须为非空数组");
            }

            ContextUtil.pushContext(context);
            pushed = true;
            Set<String> matchedPersonSet = null;
            for (String product : productSet) {
                Set<String> productPersonSet = getProductPersonNameSet(context, product);
                if (matchedPersonSet == null) {
                    matchedPersonSet = new LinkedHashSet<String>(productPersonSet);
                } else {
                    matchedPersonSet.retainAll(productPersonSet);
                }
                if (matchedPersonSet.isEmpty()) {
                    break;
                }
            }

            if (matchedPersonSet != null) {
                for (String username : matchedPersonSet) {
                    dataArray.add(getLicensedPersonInfo(context, username));
                }
            }
            returnJson.put("code", "200");
            returnJson.put("msg", "");
            returnJson.put("result", "success");
            returnJson.put("dataArray", dataArray);
        } catch (Exception e) {
            logger.error("getLicensedPersonList failed", e);
            returnJson.put("code", "500");
            returnJson.put("msg", e.getMessage());
            returnJson.put("result", "failed");
            returnJson.put("dataArray", dataArray);
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
        }
        return returnJson;
    }

    /**
     * 查询指定许可关联的人员名称集合。
     **
     * @param context 上下文
     * @param product 许可名称
     * @return Set 人员名称集合
     * @throws Exception 异常
     * @author caipan by codex
     * @date 2026/9/2
     */
    private Set<String> getProductPersonNameSet(Context context, String product) throws Exception {
        String personNames = MqlUtil.mqlCommand(context,
                "list product $1 select $2 dump $3",
                product,
                "person.object.name",
                "|");
        Set<String> personNameSet = new LinkedHashSet<String>();
        if (UIUtil.isNullOrEmpty(personNames)) {
            return personNameSet;
        }
        String[] values = personNames.split("[\\u0007\\r\\n|]+", -1);
        for (String value : values) {
            String personName = value == null ? "" : value.trim();
            if (UIUtil.isNotNullAndNotEmpty(personName)) {
                personNameSet.add(personName);
            }
        }
        return personNameSet;
    }

    /**
     * 查询人员的账号、邮箱和全名。
     **
     * @param context 上下文
     * @param username 人员账号
     * @return JSONObject 人员信息
     * @throws Exception 异常
     * @author caipan by codex
     * @date 2026/9/2
     */
    private JSONObject getLicensedPersonInfo(Context context, String username) throws Exception {
        String personInfo = MqlUtil.mqlCommand(context,
                "print person $1 select $2 $3 dump $4",
                username,
                "object.attribute[Email Address]",
                "fullname",
                "|");
        String[] values = UIUtil.isNullOrEmpty(personInfo)
                ? new String[0]
                : personInfo.split("\\|", -1);
        JSONObject personJson = new JSONObject(true);
        personJson.put("username", username);
        personJson.put("email", values.length > 0 ? values[0].trim() : "");
        personJson.put("fullname", values.length > 1 ? values[1].trim() : "");
        return personJson;
    }

}
