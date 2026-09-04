import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.matrixone.apps.domain.DomainConstants.*;
import static com.matrixone.apps.domain.MultiValueSelects.RELATIONSHIP_REFERENCE_DOCUMENT;

/**
 * 文档批量审批业务处理。
 *
 * @author LIUJR
 * @date 2026/8/10
 */
public class JF_BatchDocumentReview_mxJPO implements JF_PLMConstants_mxJPO{
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_BatchDocumentReview_mxJPO.class);
    private static final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
    private static final String RELATIONSHIP_REFERENCE_DOCUMENT = "Reference Document";
    private static final String RELATIONSHIP_DOCUMENT_SIGN_PERSON = "JFDocument2SignPerson";
    private static final String RELATIONSHIP_OBJECT_ROUTE = "Object Route";
    private static final String TYPE_DOCUMENT = CommonDocument.TYPE_DOCUMENTS;
    private static final String STATE_DOCUMENT_INWORK = "IN_WORK";
    private static final String STATE_DOCUMENT_FROZEN = "FROZEN";
    private static final String STATE_DOCUMENT_RELEASED = "RELEASED";
    private static final String SELECT_DOCUMENT_KINDOF = ProgramCentralConstants.SELECT_IS_DOCUMENTS;
    //20260811 update by ljr 从文档端直接查询Reference Document关系from端的批量审批单，避免关系过滤表达式未返回数据。
    private static final String SELECT_BATCH_REVIEW_IDS = "to[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].from["
            + JF_PLMConstants_mxJPO.TYPE_JFBatchDocumentReview + "].id";
    private static final String SELECT_PROJECT_PATH = "to[Vaulted Objects].from.attribute[Folder Path]";
    private static final String SELECT_TASK_PROJECT_ID = "to[Task Deliverable].from.to[Project Access Key].from.from[Project Access List].to.id";
    private static final String SELECT_SIGN_PERSON_IDS = "from[" + RELATIONSHIP_DOCUMENT_SIGN_PERSON + "].to.id";
    private static final String SELECT_DEPARTMENT_MANAGER_IDS = "from[JFDocument2DepManager].to.id";

    /**
     * 校验一组文档是否满足批量审批规则。
     *
     * 项目中创建批量审批单的时候校验选择的文档是否满足条件
     * @param context Matrix上下文
     * @param args docIds为待校验文档ID，batchReviewId为当前批量审批单ID
     * @return Map code为200时返回项目、owner、文档类型和专业
     * @throws Exception 查询文档信息失败时抛出异常
     * @author LIUJR
     * @date 2026/8/10
     */
    public Map validateBatchReviewDocuments(Context context, String[] args) throws Exception {
        JF_LOGGER.info("validateBatchReviewDocuments。。。。。。。。。。。。。。。。");
        Map paramMap = (Map) JPO.unpackArgs(args);
        String batchReviewId = UIUtil.getValue(paramMap, "batchReviewId");
        Map result = validateDocuments(context, toStringList(paramMap.get("docIds")), batchReviewId);
        if ("200".equals(String.valueOf(result.get("code")))
                && UIUtil.isNullOrEmpty(batchReviewId)
                && !context.getUser().equals(String.valueOf(result.get("owner")))) {
            return getResult(context, "emxProgramCentral.BatchDocumentReview.OwnerMismatch", false);
        }
        JF_LOGGER.info("validateBatchReviewDocuments。。。。。。。。。。。。。。。。");
        return result;
    }

    /**
     * 创建文档批量审批单并关联项目和文档。
     * 项目空间页面 点击创建文档批量审批单按钮 的创建方法
     *
     * @param context Matrix上下文
     * @param args title、description、docIds、signPersonIds和submitReview
     * @return Map 新建对象ID及处理结果
     * @throws Exception 创建或提交失败时抛出异常并回滚事务
     * @author LIUJR
     * @date 2026/8/10
     */
    public Map createBatchDocumentReview(Context context, String[] args) {
        Map result = new HashMap();
        boolean transactionStarted = false;
        try {
            JF_LOGGER.info("createBatchDocumentReview!!!!!!!!!!!!!!!!!!");
            Map paramMap = (Map) JPO.unpackArgs(args);
            String title = UIUtil.getValue(paramMap, "title");
            String description = UIUtil.getValue(paramMap, "description");
            String projectId = UIUtil.getValue(paramMap, "projectId");
            //false  是保存  true是提交
            boolean submitReview = "true".equalsIgnoreCase(UIUtil.getValue(paramMap, "submitReview"));
            StringList documentIds = toStringList(paramMap.get("docIds"));
            StringList signPersonIds = toStringList(paramMap.get("signPersonIds"));
            StringList departPersonIds = toStringList(paramMap.get("departPersonIds"));
            JF_LOGGER.info("departPersonIds:{}", departPersonIds);
            ContextUtil.startTransaction(context, true);
            transactionStarted = true;
            //创建审批单
            String batchReviewId = FrameworkUtil.autoName(context, "type_JFBatchDocumentReview", "policy_JFBatchDocumentReview");
            DomainObject batchReview = DomainObject.newInstance(context, batchReviewId);
            Map attributeMap = new HashMap();
            attributeMap.put(DomainConstants.ATTRIBUTE_TITLE, title.trim());
            batchReview.setAttributeValues(context, attributeMap);
            batchReview.setDescription(context, description == null ? DomainConstants.EMPTY_STRING : description);
            //关联关系
            DomainRelationship.connect(context, batchReview, JF_PLMConstants_mxJPO.RELATIONSHIP_JFBatchDocumentReview2Project, DomainObject.newInstance(context, projectId));
            batchReview.addRelatedObjects(context, new RelationshipType(RELATIONSHIP_REFERENCE_DOCUMENT), true, documentIds.toStringArray());
            //20260811 update by ljr 会签人员为非必填；填写后由批量审批申请单通过JFDocument2SignPerson统一关联。
            batchReview.addRelatedObjects(context, new RelationshipType(RELATIONSHIP_DOCUMENT_SIGN_PERSON), true, signPersonIds.toStringArray());
            batchReview.addRelatedObjects(context, new RelationshipType("JFDocument2DepManager"), true, departPersonIds.toStringArray());
            //提交 触发trigger
            if (submitReview) {
                batchReview.promote(context);
            }
            ContextUtil.commitTransaction(context);
            transactionStarted = false;
            result.put("code", "200");
            result.put("objectId", batchReviewId);

        } catch (Exception e) {
            e.printStackTrace();
            if (transactionStarted) {
                ContextUtil.abortTransaction(context);
            }
            JF_LOGGER.error("createBatchDocumentReview error", e);
            result.put("code", "500");
            result.put("objectId", "");
        }
        return result;
    }

    /**
     * 查询当前用户创建的批量审批单。
     *
     * @param context Matrix上下文
     * @param args 页面请求参数
     * @return MapList 当前用户拥有的批量审批单
     * @throws Exception 查询失败时抛出异常
     * @author LIUJR
     * @date 2026/8/10
     */
    public MapList getMyBatchDocumentReviewList(Context context, String[] args) throws Exception {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add(DomainConstants.SELECT_OWNER);
        selects.add(DomainConstants.SELECT_ORIGINATED);
        selects.add(DomainConstants.SELECT_DESCRIPTION);
        selects.add("attribute[Title]");
        selects.add("from[" + JF_PLMConstants_mxJPO.RELATIONSHIP_JFBatchDocumentReview2Project + "].to.id");
        selects.add("from[" + JF_PLMConstants_mxJPO.RELATIONSHIP_JFBatchDocumentReview2Project + "].to.name");
        selects.add("from[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].to.attribute[JF_ProjectDocType]");
        selects.add("from[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].to.attribute[JF_DocSpecialty]");
        MapList result = DomainObject.findObjects(
                context,
                JF_PLMConstants_mxJPO.TYPE_JFBatchDocumentReview,
                DomainConstants.QUERY_WILDCARD,
                "owner == '" + context.getUser() + "'",
                selects);
        for (Object item : result) {
            Map batchMap = (Map) item;
            batchMap.put("BatchProjectId", firstValue(batchMap.get(
                    "from[" + JF_PLMConstants_mxJPO.RELATIONSHIP_JFBatchDocumentReview2Project + "].to.id")));
            batchMap.put("BatchProjectName", firstValue(batchMap.get(
                    "from[" + JF_PLMConstants_mxJPO.RELATIONSHIP_JFBatchDocumentReview2Project + "].to.name")));
            batchMap.put("BatchDocumentType", firstValue(batchMap.get(
                    "from[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].to.attribute[JF_ProjectDocType]")));
            batchMap.put("BatchSpecialty", firstValue(batchMap.get(
                    "from[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].to.attribute[JF_DocSpecialty]")));
        }
        return result;
    }

    /**
     * 查询批量审批单关联的文档。
     *
     * @param context Matrix上下文
     * @param args objectId为批量审批单ID
     * @return MapList 关联文档及Reference Document关系ID
     * @throws Exception 查询失败时抛出异常
     * @author LIUJR
     * @date 2026/8/10
     */
    public MapList getBatchDocumentReviewDocumentList(Context context, String[] args) throws Exception {
        Map paramMap = (Map) JPO.unpackArgs(args);
        String batchReviewId = getObjectId(paramMap);
        if (UIUtil.isNullOrEmpty(batchReviewId)) {
            return new MapList();
        }
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_TYPE);
        objectSelects.add(DomainConstants.SELECT_NAME);
        objectSelects.add(DomainConstants.SELECT_REVISION);
        objectSelects.add(DomainConstants.SELECT_CURRENT);
        objectSelects.add(DomainConstants.SELECT_OWNER);
        objectSelects.add(DomainConstants.SELECT_DESCRIPTION);
        objectSelects.add("attribute[Title]");
        objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDocType);
        objectSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialty);
        StringList relationshipSelects = new StringList(DomainRelationship.SELECT_ID);
        DomainObject batchReview = DomainObject.newInstance(context, batchReviewId);
        MapList documentList = batchReview.getRelatedObjects(
                context,
                RELATIONSHIP_REFERENCE_DOCUMENT,
                TYPE_DOCUMENT,
                objectSelects,
                relationshipSelects,
                false,
                true,
                (short) 1,
                DomainConstants.EMPTY_STRING,
                DomainConstants.EMPTY_STRING,
                0);
        String projectName = batchReview.getInfo(context, "from[" + JF_PLMConstants_mxJPO.RELATIONSHIP_JFBatchDocumentReview2Project + "].to.name");
        for (Object item : documentList) {
            ((Map) item).put("BatchProjectName", projectName);
        }
        return documentList;
    }


    /**
     * 查询批量审批单可以继续添加的文档ID。
     * 文档批量审批单页面关联文档中的添加文档按钮
     *
     * @param context Matrix上下文
     * @param args objectId为批量审批单ID
     * @return StringList 满足状态、owner、项目、类型和专业条件的文档ID
     * @throws Exception 查询失败时抛出异常
     * @author LIUJR
     * @date 2026/8/10
     */
    public StringList getBatchDocumentReviewAddDocumentIds(Context context, String[] args) throws Exception {
        Map paramMap = (Map) JPO.unpackArgs(args);
        String batchReviewId = getObjectId(paramMap);
        DomainObject object = DomainObject.newInstance(context, batchReviewId);
        StringList relatedDocumentIds = object.getInfoList(context, "from[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].to.id");
        String documentType = DomainConstants.EMPTY_STRING;
        String specialty = DomainConstants.EMPTY_STRING;
        String projectId;
        StringList selects = StringList.create(DomainConstants.SELECT_ID,
                JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ConnProjectId,
                SELECT_PROJECT_PATH, SELECT_TASK_PROJECT_ID, SELECT_ATTR_JF_ProjectDocType, SELECT_ATTR_JF_DocSpecialty);
        if (relatedDocumentIds.isEmpty()) {
            String projectSelect = "from[" + JF_PLMConstants_mxJPO.RELATIONSHIP_JFBatchDocumentReview2Project + "].to.id";
            Map batchInfo = DomainObject.newInstance(context, batchReviewId).getInfo(
                    context,
                    StringList.create(DomainConstants.SELECT_OWNER, projectSelect));
            projectId = firstValue(batchInfo.get(projectSelect));
        } else {
            String docId = relatedDocumentIds.get(0);
            DomainObject objectDoc = DomainObject.newInstance(context, docId);
            Map info = objectDoc.getInfo(context, selects);
            documentType = UIUtil.getValue(info, SELECT_ATTR_JF_ProjectDocType);
            specialty = UIUtil.getValue(info, SELECT_ATTR_JF_DocSpecialty);
            projectId = getProjectId(info);
        }
        String where = "current == '" + STATE_DOCUMENT_INWORK + "'"
                + " && owner == '" + context.getUser() + "'"
                + " && attribute[JF_DocReceiptConfirmation] != 'Y'"
                + " && attribute[JF_DocSpecialistReceipt] != 'Y'"
                + " && to[Reference Document|from.type==JFBatchDocumentReview] == FALSE";
        if (UIUtil.isNotNullAndNotEmpty(documentType)) {
            where += " && attribute[JF_ProjectDocType] == '" + documentType + "'"
                    + " && attribute[JF_DocSpecialty] == '" + specialty + "'";
        }
        //只会查询工作中的文档，同类型同专业 同项目 无回执的文档
        MapList documentList = DomainObject.findObjects(context, TYPE_DOCUMENT, DomainConstants.QUERY_WILDCARD, where, selects);
        StringList result = new StringList();
        for (Object item : documentList) {
            Map documentMap = (Map) item;
            String documentId = UIUtil.getValue(documentMap, DomainConstants.SELECT_ID);
            if (!relatedDocumentIds.contains(documentId) && projectId.equals(getProjectId(documentMap))) {
                result.add(documentId);
            }
        }
        return result;
    }

    /**
     * 给工作中的批量审批单添加文档。
     *
     * @param context Matrix上下文
     * @param args batchReviewId和docIds
     * @return Map 处理结果
     * @throws Exception 权限或文档校验失败时抛出异常
     * @author LIUJR
     * @date 2026/8/10
     */
    public Map addBatchDocumentReviewDocuments(Context context, String[] args) throws Exception {
        Map paramMap = (Map) JPO.unpackArgs(args);
        String batchReviewId = UIUtil.getValue(paramMap, "batchReviewId");
        StringList newDocumentIds = toStringList(paramMap.get("docIds"));
        DomainObject object = DomainObject.newInstance(context, batchReviewId);
        StringList allDocumentIds = object.getInfoList(context, "from[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].to.id");
        StringList connList = new StringList();
        for (String documentId : newDocumentIds) {
            if (!allDocumentIds.contains(documentId)) {
                connList.add(documentId);
            }
        }
        boolean transactionStarted = false;
        try {
            ContextUtil.startTransaction(context, true);
            transactionStarted = true;
            DomainObject batchReview = DomainObject.newInstance(context, batchReviewId);
            batchReview.addRelatedObjects(context, new RelationshipType(RELATIONSHIP_REFERENCE_DOCUMENT), true, connList.toStringArray());
            ContextUtil.commitTransaction(context);
            transactionStarted = false;
            return getResult(context, batchReviewId, true);
        } catch (Exception e) {
            if (transactionStarted) {
                ContextUtil.abortTransaction(context);
            }
            throw e;
        }
    }


    /**
     * 批量审批单由工作中提升到审核中时校验全部关联文档：
     * 工作中提交到审批的check trigger
     * 1. 全部文档必须处于工作中状态；
     * 2. 全部文档必须属于同一项目、同一文档类型、同一专业；
     * 3. 全部文档owner必须为同一人；
     * 4. 文档不能配置发起人回执或专家回执；
     * 5. 文档不能关联当前批量审批单之外的其他批量审批单。
     *
     * @param context Matrix上下文
     * @param args Trigger传入的批量审批单ID
     * @return int 校验通过返回0，否则返回1
     * @throws Exception 查询失败时抛出异常
     * @author LIUJR
     * @date 2026/8/10
     */
    public int checkBatchDocumentReviewPromote(Context context, String[] args) throws Exception {
        String batchReviewId = args[0];
        StringList documentIds = getRelatedDocumentIds(context, batchReviewId);
        //20260811 update by ljr Promote Check中统一执行批量文档的状态、项目、类型、专业、owner、回执及重复关联校验。
        Map validationResult = validateDocuments(context, documentIds, batchReviewId);
        if (!"200".equals(String.valueOf(validationResult.get("code")))) {
            emxContextUtil_mxJPO.mqlNotice(context, String.valueOf(validationResult.get("mess")));
            return 1;
        }
        //逐份复用单文档提交审批校验，避免批量流程绕过文件、审批人、专家及项目角色校验。
        JF_DocumentTrigger_mxJPO documentTrigger = new JF_DocumentTrigger_mxJPO();
        for (String documentId : documentIds) {
            if (documentTrigger.checkDocumentRelateSignAndSpecialistPerson(context, new String[]{documentId}) != 0) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * 批量审批单进入审核中后，为批量审批单创建审批流程，并冻结全部关联文档。
     * 工作中提交到审批的action trigger
     * @param context Matrix上下文
     * @param args Trigger传入的批量审批单ID
     * @throws Exception 创建流程或冻结文档失败时抛出异常
     * @author LIUJR
     * @date 2026/8/10
     */
    public void createBatchDocumentReviewRoute(Context context, String[] args) throws Exception {
        try {
            String batchReviewId = args[0];
            DomainObject object = DomainObject.newInstance(context, batchReviewId);
            //20260811 update by ljr 文档集合及审批前置条件统一由Check Trigger校验，Action Trigger只执行流程创建和文档状态变更。
            StringList documentIds = object.getInfoList(context, "from[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].to.id");
            //20260811 update by ljr 批量审批节点及Route创建统一收口到批量专用方法，避免Action入口散落流程规则。
            String batchDocumentReviewApprovalRoute = createBatchDocumentReviewApprovalRoute(context, batchReviewId, documentIds);
            JF_LOGGER.info("batchDocumentReviewApprovalRoute:{}", batchDocumentReviewApprovalRoute);
            JF_LOGGER.info("流程创建完成后统一冻结全部文档..............");
            JF_LOGGER.info("documentIds：{}", documentIds);
            if (UIUtil.isNullOrEmpty(batchDocumentReviewApprovalRoute)) {
                return;
            }
            if ("FROZEN".equalsIgnoreCase(batchDocumentReviewApprovalRoute)) {
                //流程创建完成后统一冻结全部文档，避免逐份文档触发原有的单文档审批流程。
                for (int i = 0; i < documentIds.size(); i++) {
                    String id = documentIds.get(i);
                    MqlUtil.mqlCommand(context, false, "mod bus " + id + " current FROZEN", true);
                }
                //20260827 update by caipan 文档冻结后再增加Change Control，避免接口提前锁定生命周期而影响批量冻结。
                JF_DocumentTrigger_mxJPO.addDocumentChangeControlForDocuments(context, documentIds);
                JF_LOGGER.info("流程创建完成后统一冻结全部文档..............");
            } else {
                //没有流程直接发布
                try {
                    ContextUtil.pushContext(context);
                    object.promote(context);
                } catch (Exception e) {
                    throw e;
                } finally {
                    ContextUtil.popContext(context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.info(Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }

    /**
     * 批量审批单发布时发布全部关联文档。
     * 审批中提交到发布的action trigger
     * @param context Matrix上下文
     * @param args Trigger传入的批量审批单ID
     * @throws Exception 文档状态异常或发布失败时抛出异常
     * @author LIUJR
     * @date 2026/8/10
     */
    public void releaseBatchDocumentReviewDocuments(Context context, String[] args) throws Exception {
        String batchReviewId = args[0];
        StringList documentIds = DomainObject.newInstance(context, batchReviewId).getInfoList(context, "from[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].to.id");
        //发布阶段保留文档原有Trigger，继续执行水印及文件传输逻辑。
        JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
        jfUtilMxJPO.promoteVPM(context, documentIds, "ToRelease");
        //20260827 update by caipan 文档全部发布成功后统一释放Change Control，恢复发布文档的升版权限。
        JF_DocumentTrigger_mxJPO.removeDocumentChangeControlForDocuments(context, documentIds);
        //20260826 update by caipan 批量发布只在审批单上创建一条通知流程，不在每份文档上重复创建。
        createBatchDocumentReviewReleaseNoticeRoute(context, batchReviewId, documentIds, args);
    }

    /**
     * 批量审批单发布后，汇总关联文档的SDT通知配置并在审批单上创建通知流程。
     *
     * @param context Matrix上下文
     * @param batchReviewId 批量审批单ID
     * @param documentIds 批量审批单关联的文档ID
     * @param args Trigger参数
     * @return void
     * @throws Exception 查询通知人员或创建流程失败时抛出异常
     * @author caipan by codex
     * @date 2026/8/26
     */
    private void createBatchDocumentReviewReleaseNoticeRoute(Context context, String batchReviewId,
                                                              StringList documentIds, String[] args) throws Exception {
        DomainObject batchReview = DomainObject.newInstance(context, batchReviewId);
        String projectId = firstValue(batchReview.getInfo(
                context,
                "from[" + JF_PLMConstants_mxJPO.RELATIONSHIP_JFBatchDocumentReview2Project + "].to.id"));
        Map<String, StringList> noticeRolePersonMap = new LinkedHashMap<>();
        for (String documentId : documentIds) {
            Map<String, StringList> documentNoticeRolePersonMap =
                    JF_DocumentTrigger_mxJPO.getDocumentNoticeRolePersonMap(context, documentId, projectId);
            for (Map.Entry<String, StringList> entry : documentNoticeRolePersonMap.entrySet()) {
                StringList noticePersonIds = noticeRolePersonMap.get(entry.getKey());
                if (noticePersonIds == null) {
                    noticePersonIds = new StringList();
                    noticeRolePersonMap.put(entry.getKey(), noticePersonIds);
                }
                for (String noticePersonId : entry.getValue()) {
                    if (!noticePersonIds.contains(noticePersonId)) {
                        noticePersonIds.add(noticePersonId);
                    }
                }
            }
        }

        MapList noticeTaskList = new MapList();
        for (Map.Entry<String, StringList> entry : noticeRolePersonMap.entrySet()) {
            String roleTitle = EnoviaResourceBundle.getProperty(
                    context,
                    "emxFrameworkStringResource",
                    context.getLocale(),
                    "emxFramework.Range.Project_Role." + entry.getKey().replace(" ", "_"));
            for (String noticePersonId : entry.getValue()) {
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
                batchReviewId,
                "state_Released",
                "policy_JFBatchDocumentReview",
                routeDescription);
    }

    /**
     * 获取列表中批量审批单的当前审批节点。
     *
     * @param context Matrix上下文
     * @param args objectList为列表行数据
     * @return Vector 当前未完成任务标题
     * @throws Exception 查询失败时抛出异常
     * @author LIUJR
     * @date 2026/8/10
     */
    public Vector getBatchDocumentReviewCurrentNode(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector result = new Vector();
        for (Object item : objectList) {
            String objectId = UIUtil.getValue((Map) item, DomainConstants.SELECT_ID);
            Set<String> titleSet = new LinkedHashSet<>();
            DomainObject batchReview = DomainObject.newInstance(context, objectId);
            MapList routeList = batchReview.getRelatedObjects(
                    context,
                    RELATIONSHIP_OBJECT_ROUTE,
                    DomainConstants.TYPE_ROUTE,
                    new StringList(DomainConstants.SELECT_ID),
                    null,
                    false,
                    true,
                    (short) 1,
                    "current == '" + DomainConstants.STATE_ROUTE_IN_PROCESS + "'",
                    DomainConstants.EMPTY_STRING,
                    0);
            for (Object routeItem : routeList) {
                DomainObject routeObject = DomainObject.newInstance(
                        context,
                        UIUtil.getValue((Map) routeItem, DomainConstants.SELECT_ID));
                MapList taskList = routeObject.getRelatedObjects(
                        context,
                        DomainConstants.RELATIONSHIP_ROUTE_TASK,
                        DomainConstants.TYPE_INBOX_TASK,
                        StringList.create("attribute[Title]", DomainConstants.SELECT_CURRENT),
                        null,
                        false,
                        true,
                        (short) 1,
                        "current != Complete",
                        DomainConstants.EMPTY_STRING,
                        0);
                for (Object taskItem : taskList) {
                    String title = UIUtil.getValue((Map) taskItem, "attribute[Title]");
                    if (UIUtil.isNotNullAndNotEmpty(title)) {
                        titleSet.add(title);
                    }
                }
            }
            result.add(FrameworkUtil.join(StringList.create(titleSet), ", "));
        }
        return result;
    }

    /**
     * 一次性查询并校验批量文档的状态、项目、owner、类型、专业、审批人员及回执配置。
     *
     * @param context Matrix上下文
     * @param documentIds 待校验的文档ID
     * @param batchReviewId 当前批量审批单ID；创建审批单时为空
     * @return Map 校验结果及统一的项目、owner、文档类型和专业
     * @throws Exception 查询对象信息失败时抛出异常
     * @author LIUJR
     * @date 2026/8/11
     */
    private Map validateDocuments(Context context, StringList documentIds, String batchReviewId) throws Exception {
        Map result = new HashMap();
        Set<String> uniqueDocumentIds = new LinkedHashSet<>(documentIds);
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add("attribute[Title]");
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add(DomainConstants.SELECT_OWNER);
        selects.add(SELECT_DOCUMENT_KINDOF);
        selects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDocType);
        selects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialty);
        selects.add(JF_PLMConstants_mxJPO.select_attr_JF_DocReceiptConfirmation);
        selects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialistReceipt);
        selects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ConnProjectId);
        selects.add("to[Reference Document|from.type==JFBatchDocumentReview]");
        selects.add(SELECT_PROJECT_PATH);
        selects.add(SELECT_TASK_PROJECT_ID);
        selects.add(SELECT_BATCH_REVIEW_IDS);
        MapList documentInfoList = DomainObject.getInfo(context, uniqueDocumentIds.toArray(new String[uniqueDocumentIds.size()]), selects);
        String projectId = null;
        String owner = context.getUser();
        String documentType = null;
        String specialty = null;
        //非工作中的文档
        List<String> stateErrorDocuments = new ArrayList<>();
        //是否关联了其他的审批单
        List<String> repeatedDocumentTitles = new ArrayList<>();
        //是否关联了其他的审批单
        Boolean notDocuments = Boolean.FALSE;
        //owner不正确
        Boolean ownerErrors = Boolean.FALSE;
        Boolean projectErrors = Boolean.FALSE;
        Boolean documentTypeErrors = Boolean.FALSE;
        Boolean specialtyErrors = Boolean.FALSE;
        Boolean docReceiptErrors = Boolean.FALSE;
        //20260811 update by ljr 检查文档是否关联当前审批单之外的其他批量审批单，重复关联时优先提示并阻止提升。
        StringBuilder sb = new StringBuilder();
        if (documentInfoList.isEmpty()) {
            sb.append(getResult(context, "emxProgramCentral.BatchDocumentReview.NODOC", false).get("mess"));
            sb.append("\n");
            Map validationResult = new HashMap();
            validationResult.put("mess", sb.toString());
            validationResult.put("code", 400);
            return validationResult;
        }
        JF_LOGGER.info("!!!!!!!!!!!!!!!!!");
        String id = UIUtil.getValue((Map) documentInfoList.get(0), SELECT_ID);
        JF_LOGGER.info("@@@@@@@@@@@@@@@@@@@@@@id:{}", id);
        DomainObject object = DomainObject.newInstance(context, id);
        String attributeValue = object.getAttributeValue(context, "JF_DocDepartmentManager");
        for (Object item : documentInfoList) {
            Map documentMap = (Map) item;
            String documentName = UIUtil.getValue(documentMap, DomainConstants.SELECT_NAME);
            String documentTitle = UIUtil.getValue(documentMap, "attribute[Title]");
            String documentDisplayName = UIUtil.isNotNullAndNotEmpty(documentTitle) ? documentTitle : documentName;
            String currentProjectId = getProjectId(documentMap);
            String currentOwner = UIUtil.getValue(documentMap, DomainConstants.SELECT_OWNER);
            String currentDocumentType = UIUtil.getValue(documentMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProjectDocType);
            String currentSpecialty = UIUtil.getValue(documentMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialty);
            if (!"true".equalsIgnoreCase(UIUtil.getValue(documentMap, SELECT_DOCUMENT_KINDOF))) {
                notDocuments = Boolean.TRUE;
                continue;
            }
            String hasApply = UIUtil.getValue(documentMap, "to[Reference Document|from.type==JFBatchDocumentReview]");
            if ("TRUE".equalsIgnoreCase(hasApply)) {
                repeatedDocumentTitles.add(documentDisplayName);
                continue;
            }
            if (!STATE_DOCUMENT_INWORK.equals(UIUtil.getValue(documentMap, DomainConstants.SELECT_CURRENT))) {
                stateErrorDocuments.add(documentDisplayName);
                continue;
            }
            if (projectId == null) {
                projectId = currentProjectId;
                documentType = currentDocumentType;
                specialty = currentSpecialty;
                if (!owner.equalsIgnoreCase(currentOwner)){
                    ownerErrors = Boolean.TRUE;
                    continue;
                }
            }
            if (!projectId.equalsIgnoreCase(currentProjectId)) {
                projectErrors = Boolean.TRUE;
                continue;
            }
            if (!owner.equalsIgnoreCase(currentOwner)) {
                ownerErrors = Boolean.TRUE;
                continue;
            }
            if (!documentType.equalsIgnoreCase(currentDocumentType)) {
                documentTypeErrors = Boolean.TRUE;
                continue;
            }
            if (!specialty.equalsIgnoreCase(currentSpecialty)) {
                specialtyErrors = Boolean.TRUE;
                continue;
            }
            //批量审批不支持发起人回执和专家回执，任意一份文档配置回执都阻止提升。
            if ("Y".equalsIgnoreCase(UIUtil.getValue(documentMap, JF_PLMConstants_mxJPO.select_attr_JF_DocReceiptConfirmation))
                    || "Y".equalsIgnoreCase(UIUtil.getValue(documentMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialistReceipt))) {
                docReceiptErrors = Boolean.TRUE;
            }
        }
        if (notDocuments) {
            sb.append(getResult(context, "emxProgramCentral.BatchDocumentReview.DocumentInvalid", false).get("mess"));
            sb.append("\n");
        }
        if (!repeatedDocumentTitles.isEmpty()) {
            Map validationResult = getResult(context, "emxProgramCentral.BatchDocumentReview.AlreadyInReview", false);
            sb.append(validationResult.get("mess") + FrameworkUtil.join(StringList.create(repeatedDocumentTitles), ", "));
            sb.append("\n");
        }
        if (!stateErrorDocuments.isEmpty()) {
            Map validationResult = getResult(context, "emxProgramCentral.BatchDocumentReview.InWorkRequired", false);
            sb.append(validationResult.get("mess") + FrameworkUtil.join(StringList.create(stateErrorDocuments), ", "));
            sb.append("\n");
        }
        if (ownerErrors) {
            sb.append(getResult(context, "emxProgramCentral.BatchDocumentReview.OwnerMismatch", false).get("mess"));
            sb.append("\n");
        }
        if (projectErrors) {
            sb.append(getResult(context, "emxProgramCentral.BatchDocumentReview.ProjectMismatch", false).get("mess"));
            sb.append("\n");
        }
        if (documentTypeErrors) {
            sb.append(getResult(context, "emxProgramCentral.BatchDocumentReview.DocumentTypeMismatch", false).get("mess"));
            sb.append("\n");
        }
        if (specialtyErrors) {
            sb.append(getResult(context, "emxProgramCentral.BatchDocumentReview.SpecialtyMismatch", false).get("mess"));
            sb.append("\n");
        }
        if (docReceiptErrors) {
            sb.append(getResult(context, "emxProgramCentral.BatchDocumentReview.ReceiptNotAllowed", false).get("mess"));
            sb.append("\n");
        }
        //需要部门经理审核
        if ("Y".equalsIgnoreCase(attributeValue)) {
            if (UIUtil.isNotNullAndNotEmpty(batchReviewId)) {
                StringList infoList = DomainObject.newInstance(context, batchReviewId).getInfoList(context, "from[JFDocument2DepManager].to.id");
                if (infoList.isEmpty()) {
                    sb.append(getResult(context, "emxComponents.Document.DepManagerError", false).get("mess"));
                    sb.append("\n");
                }
            }
        }
        String mess = sb.toString();
        if (UIUtil.isNotNullAndNotEmpty(mess)) {
            Map validationResult = new HashMap();
            validationResult.put("mess", mess);
            return validationResult;
        }
        if (UIUtil.isNotNullAndNotEmpty(batchReviewId)) {
            StringList batchSelects = StringList.create(
                    DomainConstants.SELECT_OWNER,
                    "from[" + JF_PLMConstants_mxJPO.RELATIONSHIP_JFBatchDocumentReview2Project + "].to.id");
            Map batchInfo = DomainObject.newInstance(context, batchReviewId).getInfo(context, batchSelects);
            String batchOwner = UIUtil.getValue(batchInfo, DomainConstants.SELECT_OWNER);
            String batchProjectId = firstValue(batchInfo.get(
                    "from[" + JF_PLMConstants_mxJPO.RELATIONSHIP_JFBatchDocumentReview2Project + "].to.id"));
            //给已有审批单追加文档时，必须以审批单本身的项目和owner为基准再次校验。
            if (!owner.equalsIgnoreCase(batchOwner)) {
                return getResult(context, "emxProgramCentral.BatchDocumentReview.OwnerMismatch", false);
            }
            if (!projectId.equalsIgnoreCase(batchProjectId)) {
                return getResult(context, "emxProgramCentral.BatchDocumentReview.ProjectMismatch", false);
            }
        }

        result.put("code", "200");
        result.put("projectId", projectId);
        result.put("owner", owner);
        result.put("documentType", documentType);
        result.put("specialty", specialty);
        return result;
    }

    /**
     * 获取批量审批单通过Reference Document关系关联的全部文档ID。
     *
     * @param context Matrix上下文
     * @param batchReviewId 批量审批单ID
     * @return StringList 关联文档ID；对象ID为空时返回空集合
     * @throws Exception 查询关系失败时抛出异常
     * @author LIUJR
     * @date 2026/8/11
     */
    private StringList getRelatedDocumentIds(Context context, String batchReviewId) throws Exception {
        if (UIUtil.isNullOrEmpty(batchReviewId)) {
            return new StringList();
        }
        return DomainObject.newInstance(context, batchReviewId)
                .getInfoList(context, "from[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].to.id");
    }

    /**
     * 按项目属性、项目文件夹路径、任务交付物的顺序解析文档所属项目ID。
     *
     * @param documentMap 文档查询结果
     * @return String 项目ID，无法取得时返回空字符串
     * @author LIUJR
     * @date 2026/8/11
     */
    private String getProjectId(Map documentMap) {
        //项目文件夹中的技术文档以JF_ConnProjectId作为所属项目的唯一判断依据。
        String projectId = UIUtil.getValue(documentMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ConnProjectId);
        if (UIUtil.isNotNullAndNotEmpty(projectId)) {
            return projectId;
        }
        StringList pathList = toStringList(documentMap.get(SELECT_PROJECT_PATH));
        for (String path : pathList) {
            if (UIUtil.isNotNullAndNotEmpty(path)) {
                int separatorIndex = path.indexOf('|');
                return separatorIndex > -1 ? path.substring(0, separatorIndex) : path;
            }
        }
        StringList taskProjectIds = toStringList(documentMap.get(SELECT_TASK_PROJECT_ID));
        return taskProjectIds.isEmpty() ? DomainConstants.EMPTY_STRING : taskProjectIds.get(0);
    }

    /**
     * 兼容Table、Form及普通JPO请求结构取得objectId。
     *
     * @param paramMap JPO请求参数
     * @return String 对象ID
     * @author LIUJR
     * @date 2026/8/11
     */
    private String getObjectId(Map paramMap) {
        String objectId = UIUtil.getValue(paramMap, "objectId");
        if (UIUtil.isNullOrEmpty(objectId) && paramMap.get("paramMap") instanceof Map) {
            objectId = UIUtil.getValue((Map) paramMap.get("paramMap"), "objectId");
        }
        if (UIUtil.isNullOrEmpty(objectId) && paramMap.get("requestMap") instanceof Map) {
            objectId = UIUtil.getValue((Map) paramMap.get("requestMap"), "objectId");
        }
        return objectId;
    }

    /**
     * 将String、数组或集合统一转换为StringList。
     *
     * @param value 待转换数据
     * @return StringList 非空字符串集合
     * @author LIUJR
     * @date 2026/8/11
     */
    private StringList toStringList(Object value) {
        StringList result = new StringList();
        if (value instanceof StringList) {
            result.addAll((StringList) value);
        } else if (value instanceof String[]) {
            for (String item : (String[]) value) {
                if (UIUtil.isNotNullAndNotEmpty(item)) {
                    result.add(item);
                }
            }
        } else if (value instanceof Collection) {
            for (Object item : (Collection) value) {
                if (item != null && UIUtil.isNotNullAndNotEmpty(String.valueOf(item))) {
                    result.add(String.valueOf(item));
                }
            }
        } else if (value != null && UIUtil.isNotNullAndNotEmpty(String.valueOf(value))) {
            String stringValue = String.valueOf(value);
            if (stringValue.indexOf(',') > -1) {
                result.addAll(FrameworkUtil.split(stringValue, ","));
            } else {
                result.add(stringValue);
            }
        }
        return result;
    }

    /**
     * 获取单值或多值查询结果中的第一个值。
     *
     * @param value 查询结果
     * @return String 第一个值；无值时返回空字符串
     * @author LIUJR
     * @date 2026/8/11
     */
    private String firstValue(Object value) {
        StringList values = toStringList(value);
        return values.isEmpty() ? DomainConstants.EMPTY_STRING : values.get(0);
    }

    /**
     * 根据成功标识构造统一处理结果；成功时写入对象ID，失败时写入国际化提示。
     *
     * @param context Matrix上下文
     * @param value 成功时为对象ID，失败时为国际化Key
     * @param success 是否处理成功
     * @return Map 统一处理结果
     * @throws Exception 读取国际化资源失败时抛出异常
     * @author LIUJR
     * @date 2026/8/11
     */
    private Map getResult(Context context, String value, boolean success) throws Exception {
        Map result = new HashMap();
        result.put("code", success ? "200" : "400");
        if (success) {
            result.put("objectId", value);
        } else {
            result.put("mess", EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE, context.getLocale(), value));
        }
        return result;
    }

    /**
     * 根据批量审批单关联文档的统一配置，为批量审批单创建审批流程。
     * 第一份文档仅作为审批配置来源，Route内容对象始终为批量审批单；批量审批不生成任何回执节点。
     *
     * @param context Matrix上下文
     * @param batchReviewId 批量审批单ID
     * @param documentIds 批量审批单关联的全部文档ID
     * @throws Exception 查询审批配置或创建流程失败时抛出异常
     * @author LIUJR
     * @date 2026/8/11
     */
    private String createBatchDocumentReviewApprovalRoute(Context context, String batchReviewId, StringList documentIds) throws Exception {
        String flag = "FROZEN";
        DomainObject batchReview = DomainObject.newInstance(context, batchReviewId);
        String routeConfigDocumentId = documentIds.get(0);
        DomainObject routeConfigDocument = DomainObject.newInstance(context, routeConfigDocumentId);
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
        Map attributeMap = routeConfigDocument.getInfo(context, attributeList);
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
        String projectId = firstValue(batchReview.getInfo(context, "from[" + JF_PLMConstants_mxJPO.RELATIONSHIP_JFBatchDocumentReview2Project + "].to.id"));
        //不是项目文档，不生成流程
        if ("N".equalsIgnoreCase(strJF_ProjectDoc)) {
            return "";
        }
        MapList approverList = new MapList();
        StringList addedPersonIds = new StringList();
        Integer approveIndex = 1;
        String routeDescription = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.DOCManger.ROUTE.TitleMess", new String[]{});
        String titleMess = EMPTY_STRING;
        String personId = EMPTY_STRING;
        if ("XSO".equalsIgnoreCase(strJF_ProjectDocType)) {
            titleMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DOCManger.ROUTE.DepartmentManager");
            //XSO文档取文档owner所属部门维护的负责人作为第一审批节点。
            DomainObject ownerPerson = PersonUtil.getPersonObject(context, strOwner);
            MapList mapList = ownerPerson.getRelatedObjects(
                    context,
                    DomainConstants.RELATIONSHIP_MEMBER,
                    DomainConstants.TYPE_DEPARTMENT,
                    StringList.create(DomainConstants.SELECT_DESCRIPTION),
                    null,
                    true,
                    false,
                    (short) 1,
                    DomainConstants.EMPTY_STRING,
                    DomainConstants.EMPTY_STRING,
                    0);
            HashSet personSet = (HashSet) mapList.stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, SELECT_DESCRIPTION);
            }).collect(Collectors.toCollection(HashSet::new));
            StringList personList = StringList.create(personSet);
            for (int i = 0; i < personList.size(); i++) {
                String personName = personList.get(i);
                personId = PersonUtil.getPersonObjectID(context, personName);
                Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(personId, titleMess, "true", approveIndex.toString(), "All");
                approverList.add(managerMap);
            }
        } else {
            StringList signPersonIds = batchReview.getInfoList(context, SELECT_SIGN_PERSON_IDS);
            String signPerson = signPersonIds.size() > 0 ? "Y" : "N";
            //20260826 update by caipan 分类通知节点移至各文档发布后的通知流程，不再参与批量审批流程。
            //判断所有审批节点是否均为否且会签人员是否为空。
            boolean allAreN = Stream.of(strJF_DocSpecialistReview, strJF_DocSpecialistReceipt,
                            strJF_DocChairManagerReview, strJF_DocDepartmentManager,
                            strJF_DocLeadReview, strJF_DocProjectReview,
                            signPerson)
                    .allMatch(str -> "N".equalsIgnoreCase(str));
            if (allAreN) {
                //直接发布
                return "Release";
            }

            //直线经理审批。
            if ("Y".equalsIgnoreCase(strJF_DocLeadReview)) {
                titleMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(),
                        "emxComponents.DOCManger.ROUTE.LineManager");
                personId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, strOwner);
                if (UIUtil.isNotNullAndNotEmpty(personId)) {
                    approverList.add(JF_PublicMethodClass_mxJPO.getMapAnyOrAll(
                            personId, titleMess, "true", String.valueOf(approveIndex), "All"));
                    addedPersonIds.add(personId);
                    approveIndex++;
                }
            }

            //20260811 update by ljr 批量审批会签人员取申请单自身关系，不读取第一份文档的会签人员。
            if (!signPersonIds.isEmpty()) {
                titleMess = EnoviaResourceBundle.getProperty(
                        context, "emxComponentsStringResource", context.getLocale(),
                        "emxComponents.DOCManger.ROUTE.SignPerson");
                for (String signPersonId : signPersonIds) {
                    approverList.add(addedPersonIds.contains(signPersonId)
                            ? JF_PublicMethodClass_mxJPO.getMapAnyOrAllNotifyOnly(
                                    signPersonId, titleMess, "true", String.valueOf(approveIndex), "All")
                            : JF_PublicMethodClass_mxJPO.getMapAnyOrAll(
                                    signPersonId, titleMess, "true", String.valueOf(approveIndex), "All"));
                    addedPersonIds.add(signPersonId);
                }
                approveIndex++;
            }

            //整椅经理审批。
            if ("Y".equalsIgnoreCase(strJF_DocChairManagerReview)) {
                titleMess = EnoviaResourceBundle.getProperty(
                        context, "emxComponentsStringResource", context.getLocale(),
                        "emxComponents.DRChairManger.ROUTE.MESS");
                personId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(
                        context, projectId, DomainConstants.SELECT_ID, DomainConstants.EMPTY_STRING,
                        "attribute[Project Role]=='Chair manager'");
                if (UIUtil.isNotNullAndNotEmpty(personId)) {
                    approverList.add(addedPersonIds.contains(personId)
                            ? JF_PublicMethodClass_mxJPO.getMapAnyOrAllNotifyOnly(
                                    personId, titleMess, "true", String.valueOf(approveIndex), "All")
                            : JF_PublicMethodClass_mxJPO.getMapAnyOrAll(
                                    personId, titleMess, "true", String.valueOf(approveIndex), "All"));
                    addedPersonIds.add(personId);
                    approveIndex++;
                }
            }

            //部门经理审批。
            if ("Y".equalsIgnoreCase(strJF_DocDepartmentManager)) {
                StringList departmentManagerIds = batchReview.getInfoList(
                        context, "from[JFDocument2DepManager].to.id");
                titleMess = EnoviaResourceBundle.getProperty(
                        context, "emxComponentsStringResource", context.getLocale(),
                        "emxComponents.CommonDocument.JF_DepartmentManger");
                for (String departmentManagerId : departmentManagerIds) {
                    approverList.add(addedPersonIds.contains(departmentManagerId)
                            ? JF_PublicMethodClass_mxJPO.getMapAnyOrAllNotifyOnly(
                                    departmentManagerId, titleMess, "true", String.valueOf(approveIndex), "All")
                            : JF_PublicMethodClass_mxJPO.getMapAnyOrAll(
                                    departmentManagerId, titleMess, "true", String.valueOf(approveIndex), "All"));
                    addedPersonIds.add(departmentManagerId);
                }
                if (!departmentManagerIds.isEmpty()) {
                    approveIndex++;
                }
            }

            //项目经理审批。
            if ("Y".equalsIgnoreCase(strJF_DocProjectReview)) {
                titleMess = EnoviaResourceBundle.getProperty(
                        context, "emxComponentsStringResource", context.getLocale(),
                        "emxComponents.DOCManger.ROUTE.ProjectManager");
                personId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(
                        context, projectId, DomainConstants.SELECT_ID, DomainConstants.EMPTY_STRING,
                        "attribute[Project Role]=='Project manager'");
                if (UIUtil.isNotNullAndNotEmpty(personId)) {
                    approverList.add(addedPersonIds.contains(personId)
                            ? JF_PublicMethodClass_mxJPO.getMapAnyOrAllNotifyOnly(
                                    personId, titleMess, "true", String.valueOf(approveIndex), "All")
                            : JF_PublicMethodClass_mxJPO.getMapAnyOrAll(
                                    personId, titleMess, "true", String.valueOf(approveIndex), "All"));
                    addedPersonIds.add(personId);
                    approveIndex++;
                }
            }

            //专家审核
            StringList specialistPersonIdList = new StringList();
            if ("Y".equalsIgnoreCase(strJF_DocSpecialistReview)
                    || "Y".equalsIgnoreCase(strJF_DocSpecialistReceipt)) {
                //20260805 update by ljr 复用提交校验的专家匹配逻辑，多个专家使用同一审批顺序并行处理。
                specialistPersonIdList = JF_PublicMethodClass_mxJPO.getProjectDocumentExpertPersonIds(
                        context,projectId, strJF_ProjectDocType, strJF_DocSpecialty);
            }
            //批量审批禁止回执流程，只按配置生成专家审核节点。
            //审核专家审批
            if ("Y".equals(strJF_DocSpecialistReview)) {
                titleMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DOCManger.ROUTE.SpecialistPerson");
                // 标记是否添加了新审批人
                boolean addedNewPerson = false;
                for (int i = 0; i < specialistPersonIdList.size(); i++) {
                    personId = specialistPersonIdList.get(i);
                    Map managerMap;
                    if (addedPersonIds.contains(personId)) {
                        managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAllNotifyOnly(personId, titleMess, "true", approveIndex.toString(), "All");
                    } else {
                        managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(personId, titleMess, "true", approveIndex.toString(), "All");
                    }
                    approverList.add(managerMap);
                    addedPersonIds.add(personId);
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
                    Map managerMap;
                    if (addedPersonIds.contains(personId)) {
                        managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAllNotifyOnly(personId, titleMess, "true", approveIndex.toString(), "All");
                    } else {
                        managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(personId, titleMess, "true", approveIndex.toString(), "All");
                    }
                    approverList.add(managerMap);
                    addedPersonIds.add(personId);
                    // 标记为添加了新审批人
                    addedNewPerson = true;
                }
                if (addedNewPerson) {
                    approveIndex++;
                }
            }
        }
        //Route直接关联批量审批单，使用批量审批策略和Review状态作为Route Base State。
        JF_LOGGER.info("approverList:{}", approverList);
        if (!approverList.isEmpty()) {
            new JF_Route_mxJPO(context, new String[0]).createAndStartRoute(
                    context,
                    approverList,
                    batchReviewId,
                    "state_Review",
                    "policy_JFBatchDocumentReview",
                    routeDescription);
        }
        return flag;
    }

    /**
     * 查询批量审批单通过JFDocument2SignPerson关联的会签人员。
     *
     * @param context Matrix上下文
     * @param args 页面请求参数
     * @return MapList 会签人员列表
     * @throws Exception 查询关系失败时抛出异常
     * @author LIUJR
     * @date 2026/8/12
     */
    public MapList getBatchDocumentReviewDepManagerPersonList(Context context, String[] args) throws Exception {
        Map paramMap = (Map) JPO.unpackArgs(args);
        String batchReviewId = getObjectId(paramMap);
        if (UIUtil.isNullOrEmpty(batchReviewId)) {
            return new MapList();
        }

        // 会签人员Table复用JFSPartsApplicationTable，一次查询返回该Table需要的人员基本信息。
        StringList personSelects = StringList.create(
                DomainConstants.SELECT_ID,
                DomainConstants.SELECT_TYPE,
                DomainConstants.SELECT_NAME,
                DomainConstants.SELECT_CURRENT,
                DomainConstants.SELECT_OWNER,
                DomainConstants.SELECT_ORIGINATED,
                DomainConstants.SELECT_DESCRIPTION,
                "attribute[Title]");
        StringList relationshipSelects = new StringList(DomainRelationship.SELECT_ID);
        return DomainObject.newInstance(context, batchReviewId).getRelatedObjects(
                context,
                "JFDocument2DepManager",
                DomainConstants.TYPE_PERSON,
                personSelects,
                relationshipSelects,
                false,
                true,
                (short) 1,
                DomainConstants.EMPTY_STRING,
                DomainConstants.EMPTY_STRING,
                0);
    }

    /**
     * 查询批量审批单通过JFDocument2SignPerson关联的会签人员。
     *
     * @param context Matrix上下文
     * @param args 页面请求参数
     * @return MapList 会签人员列表
     * @throws Exception 查询关系失败时抛出异常
     * @author LIUJR
     * @date 2026/8/12
     */
    public MapList getBatchDocumentReviewSignPersonList(Context context, String[] args) throws Exception {
        Map paramMap = (Map) JPO.unpackArgs(args);
        String batchReviewId = getObjectId(paramMap);
        if (UIUtil.isNullOrEmpty(batchReviewId)) {
            return new MapList();
        }

        // 会签人员Table复用JFSPartsApplicationTable，一次查询返回该Table需要的人员基本信息。
        StringList personSelects = StringList.create(
                DomainConstants.SELECT_ID,
                DomainConstants.SELECT_TYPE,
                DomainConstants.SELECT_NAME,
                DomainConstants.SELECT_CURRENT,
                DomainConstants.SELECT_OWNER,
                DomainConstants.SELECT_ORIGINATED,
                DomainConstants.SELECT_DESCRIPTION,
                "attribute[Title]");
        StringList relationshipSelects = new StringList(DomainRelationship.SELECT_ID);
        return DomainObject.newInstance(context, batchReviewId).getRelatedObjects(
                context,
                RELATIONSHIP_DOCUMENT_SIGN_PERSON,
                DomainConstants.TYPE_PERSON,
                personSelects,
                relationshipSelects,
                false,
                true,
                (short) 1,
                DomainConstants.EMPTY_STRING,
                DomainConstants.EMPTY_STRING,
                0);
    }

    /**
     * 查询批量审批单尚未添加的活动会签人员ID。
     *
     * @param context Matrix上下文
     * @param args 页面请求参数
     * @return StringList 活动且未与当前批量审批单关联的人员ID
     * @throws Exception 当前用户无权或人员查询失败时抛出异常
     * @author LIUJR
     * @date 2026/8/12
     */
    @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList getBatchDocumentReviewAvailableSignPersonIds(Context context, String[] args) throws Exception {
        Map paramMap = (Map) JPO.unpackArgs(args);
        String batchReviewId = getObjectId(paramMap);

        StringList existingPersonIds = DomainObject.newInstance(context, batchReviewId).getInfoList(context, SELECT_SIGN_PERSON_IDS);
        StringList personSelects = new StringList(DomainConstants.SELECT_ID);
        MapList activePersonList = DomainObject.findObjects(
                context,
                DomainConstants.TYPE_PERSON,
                DomainConstants.QUERY_WILDCARD,
                "current==Active",
                personSelects);
        StringList availablePersonIds = new StringList();
        for (Object item : activePersonList) {
            String personId = UIUtil.getValue((Map) item, DomainConstants.SELECT_ID);
            if (UIUtil.isNotNullAndNotEmpty(personId) && !existingPersonIds.contains(personId)) {
                availablePersonIds.add(personId);
            }
        }
        return availablePersonIds;
    }

    /**
     * 生成批量审批单会签人员Table字段，并清理嵌入字段的边框和间距。
     *
     * @param context Matrix上下文
     * @param args Form字段参数
     * @return String 会签人员Table页面HTML
     * @throws Exception 页面生成失败时抛出异常
     * @author LIUJR
     * @date 2026/8/12
     */
    public String getBatchDocumentReviewSignPersonTableField(Context context, String[] args) throws Exception {
        // 复用OOTB fieldURL生成iframe，避免在自定义方法中重复拼装Table页面。
        String tableFieldHtml = JPO.invoke(
                context,
                "emxGenericFields",
                null,
                "fieldURL",
                args,
                String.class);

        //20260828 update by caipan 每个人员区域使用完整可用高度，并同步外层容器高度，避免后续分组标题被iframe覆盖。
        StringBuilder result = new StringBuilder(tableFieldHtml);
        result.append("<script type='text/javascript'>")
                .append("(function(){")
                .append("if(!window.JFBatchReviewPersonnelResizeInstalled){")
                .append("window.JFBatchReviewPersonnelResizeInstalled=true;")
                .append("window.JFBatchReviewResizePersonnelTables=function(){")
                .append("var ids=['frameFieldSignPersonTable','frameFieldDepartmentMangerTable'],frames=[];")
                .append("for(var i=0;i<ids.length;i++){var f=document.getElementById(ids[i]);")
                .append("if(f&&f.offsetParent!==null&&window.getComputedStyle(f).display!=='none'){frames.push(f);}}")
                .append("if(!frames.length){return;}")
                .append("var top=frames[0].getBoundingClientRect().top;")
                .append("var viewportHeight=document.documentElement.clientHeight||window.innerHeight;")
                .append("var sectionGap=0;")
                .append("for(var g=1;g<frames.length;g++){sectionGap+=Math.max(0,frames[g].getBoundingClientRect().top-(frames[g-1].getBoundingClientRect().top+frames[g-1].offsetHeight));}")
                .append("var available=Math.max(180,viewportHeight-top-8-sectionGap);")
                .append("var height=Math.max(150,Math.floor(available));")
                .append("for(var j=0;j<frames.length;j++){var frame=frames[j];")
                .append("frame.style.display='block';frame.style.width='100%';frame.style.setProperty('height',height+'px','important');frame.setAttribute('height',String(height));")
                .append("frame.style.padding='0';frame.style.margin='0';frame.style.border='none';")
                .append("if(frame.parentNode){frame.parentNode.style.setProperty('height',height+'px','important');frame.parentNode.style.padding='0';frame.parentNode.style.margin='0';}")
                .append("if(frame.closest){var td=frame.closest('td');if(td){td.style.padding='0';td.style.border='none';}}")
                .append("if(!frame.getAttribute('data-jf-resize-bound')){frame.setAttribute('data-jf-resize-bound','true');frame.addEventListener('load',window.JFBatchReviewSchedulePersonnelResize);}}")
                .append("};")
                .append("window.JFBatchReviewSchedulePersonnelResize=function(){")
                .append("if(window.JFBatchReviewPersonnelResizeTimer){window.clearTimeout(window.JFBatchReviewPersonnelResizeTimer);}")
                .append("window.requestAnimationFrame(window.JFBatchReviewResizePersonnelTables);")
                .append("window.JFBatchReviewPersonnelResizeTimer=window.setTimeout(window.JFBatchReviewResizePersonnelTables,300);")
                .append("};")
                .append("window.addEventListener('resize',window.JFBatchReviewSchedulePersonnelResize);")
                .append("window.addEventListener('load',window.JFBatchReviewSchedulePersonnelResize);")
                .append("}")
                .append("window.JFBatchReviewSchedulePersonnelResize();")
                .append("window.setTimeout(window.JFBatchReviewSchedulePersonnelResize,800);")
                .append("}());")
                .append("</script>");
        return result.toString();
    }

    /**
    * 判断文档类型是否需要部门经理填写权限
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2026/8/13 14:34
    * @description
    */
    public Boolean getDepartmentMangerAccess(Context context, String[] args) {
        Boolean flag = Boolean.TRUE;
        try {
            HashMap inputMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) inputMap.get("requestMap");
            String strObjectId = null;
            if (requestMap == null) {
                strObjectId = (String) inputMap.get("objectId");
            } else {
                strObjectId = (String) requestMap.get("objectId");
            }
            DomainObject obj = DomainObject.newInstance(context,strObjectId);
            StringList infoList = obj.getInfoList(context, "from[Reference Document].to.id");
            if (infoList.size() == 0) {
                flag = Boolean.FALSE;
            }  else {
                String id = infoList.get(0);
                DomainObject object = DomainObject.newInstance(context, id);
                String attributeValue = object.getAttributeValue(context, "JF_DocDepartmentManager");
                if ("N".equalsIgnoreCase(attributeValue)) {
                    flag = Boolean.FALSE;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
}
