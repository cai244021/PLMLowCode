import com.dscn.plm.util.NioJDUtils;
import com.matrixone.apps.common.SubscriptionManager;
import com.matrixone.apps.common.Workspace;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.team.WorkspaceCreateMdl;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dassault_systemes.enovia.workspace.modeler.WorkspaceVault.ATTRIBUTE_ACCESSTYPE_SPECIFIC;
import static com.matrixone.apps.domain.DomainConstants.*;
import static com.matrixone.apps.domain.MultiValueSelects.ATTRIBUTE_TITLE;
import static com.matrixone.apps.domain.MultiValueSelects.SELECT_FROM_ID;

/**
 * @author CHENYAN
 * @version v1.0.0
 * 创建时间：2024/10/17 13:11
 * @description
 */
public class JF_ProjectDocumentService_mxJPO implements JF_PLMConstants_mxJPO{
    private static final Logger _logger = LoggerFactory.getLogger(JF_ProjectDocumentService_mxJPO.class);

    /**
    *
    *@description 项目任务完成时获取任务交付物并挂载到具体的项目文件夹里面去
    *@param context
	*@param arg
    *@return int
    *@throws
    *@author CHENYAN
    *@date 2024/10/17 17:15
    */

    public int taskDeliverablesClassify(Context context, String[] arg) throws Exception{
        boolean isPush = false ;
        try {
            String strObjectId = arg[0];
            String strCurrent = arg[1];
            _logger.info("strObjectId:{}",strObjectId);
            _logger.info("strCurrent:{}",strCurrent);
            DomainObject task = DomainObject.newInstance(context, strObjectId);
            //判断该任务是否是执行任务，执行任务直接结束
            StringList selectTaskList = new StringList();
            selectTaskList.add("to[Project Access Key]");
            selectTaskList.add(SELECT_OWNER);
            selectTaskList.add("to[Project Access Key].from.from[Project Access List].to.name");
            selectTaskList.add("to[Project Access Key].from.from[Project Access List].to.description");
            Map taskMap = task.getInfo(context,selectTaskList) ;
            String strIsProjectTask = (String)taskMap.get("to[Project Access Key]");
            String strProjectName = (String)taskMap.get("to[Project Access Key].from.from[Project Access List].to.name");
            String strProjectDescription = (String)taskMap.get("to[Project Access Key].from.from[Project Access List].to.description");
            //项目文件夹名称
            String strProjectDocumentName = JF_PublicMethodClass_mxJPO.buildStringInStrings(strProjectDescription, "|", strProjectName);
            _logger.info("strProjectDocumentName:{}",strProjectDocumentName);
            if ("TRUE".equalsIgnoreCase(strIsProjectTask)){
                //登录人id
                String strPersonId = PersonUtil.getPersonObjectID(context,UIUtil.getValue(taskMap, SELECT_OWNER));
                ContextUtil.pushContext(context);
                isPush = true;
                //文件夹
                MapList documentList = DomainObject.findObjects(context, TYPE_WORKSPACE_VAULT, "*", JF_PublicMethodClass_mxJPO.buildStringInStrings("attribute[Title].value=='", strProjectDocumentName, "'"), StringList.create(SELECT_ID));
                _logger.info("------------------------------- 项目任务归档交付物 begin -------------------------------------------------------");
                StringList deliverableIdList = getTaskDeliverableByTask(context, task);
                _logger.info("deliverableIdList:{}",deliverableIdList);
                //不存在任务交付物
                if (deliverableIdList == null || deliverableIdList.size() == 0){
                    return  0;
                }
                try {
                    ContextUtil.startTransaction(context,true);
                    boolean isStartTransaction = true;
                    Map rootTask = getRootTaskBySunTask(context, task);
                    //如果当前任务维护了项目角色则使用当前任务的项目角色
                    String strTaskProjectRole = task.getInfo(context, SELECT_ATTR_PROJECT_ROLE);
                    _logger.info("strTaskProjectRole:{}",strTaskProjectRole);
                    if (UIUtil.isNotNullAndNotEmpty(strTaskProjectRole)){
                        rootTask.put(SELECT_ATTR_PROJECT_ROLE,strTaskProjectRole);
                    }
                    _logger.info("rootTask:{}",rootTask);
                    String strProjectRole = (String) rootTask.get(SELECT_ATTR_PROJECT_ROLE);
                    String strProjectDocumentId = "";
                    //项目文件夹已经创建
                    if (documentList.size() > 0){
                        Map projectDocument = (Map) documentList.get(0);
                        strProjectDocumentId = (String) projectDocument.get(SELECT_ID);
                    }else {
                        //获取page文件中保存的项目文档库title
                        String strRootProjectName = NioJDUtils.getPageStr(context, "Project.Document.Title");
                        //获取项目文档库根节点
                        MapList rootList = DomainObject.findObjects(context, TYPE_WORKSPACE, "*", JF_PublicMethodClass_mxJPO.buildStringInStrings("attribute[Title].value=='", strRootProjectName, "'"), StringList.create(SELECT_ID));
                        //没有找到根节点
                        if (rootList == null || rootList.size() == 0){
                            String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ProjectDocumentRootIsNull");
                            emxContextUtilBase_mxJPO.mqlWarning(context,strMess);
                            if (isStartTransaction){
                                ContextUtil.abortTransaction(context);
                            }
                            return 1;
                        }else {
                            Map root = (Map) rootList.get(0);
                            String strRootId = (String) root.get(SELECT_ID);
                            Map attrMap = new HashMap<>();
                            attrMap.put(ATTRIBUTE_TITLE,strProjectDocumentName);
                            strProjectDocumentId = createVault(context, attrMap, strPersonId, strRootId, "Inherited");
                        }
                    }
                    _logger.info("strProjectDocumentId:{}",strProjectDocumentId);
                    DomainObject projectDoc = DomainObject.newInstance(context, strProjectDocumentId);
                    StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
                    StringList reSelectList = JF_Util_mxJPO.basicRellistSel();
                    typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
                    reSelectList.add(SELECT_FROM_NAME);
                    reSelectList.add("from.attribute[Title]");
                    reSelectList.add(SELECT_FROM_ID);
                    //获取到项目文件夹下的一级任务文件夹和二级角色文件夹
                    MapList maps = projectDoc.getRelatedObjects(context, RELATIONSHIP_SUB_VAULTS, // relationship pattern
                            TYPE_WORKSPACE_VAULT,                                    // object pattern
                            typeSelectList,                            // object selects
                            reSelectList, // relationship selects
                            false,                                        // to direction
                            true,                                        // from direction
                            (short) 2,                                    // recursion level
                            "",                // object where clause
                            "",
                            (short) 0);
                    Map groupMap = (Map)maps.stream().collect(Collectors.groupingBy(m -> {
                        Map workSpaceMap = (Map) m;
                        return workSpaceMap.get(SELECT_LEVEL);
                    }));
                    //判断一级文件夹是否创建
                    boolean isCreateOneLevel = false;
                    String strOneLevelId = "";
                    String strOneLeveName = "";
                    List oneLevelList = (List) groupMap.get("1");
                    if (oneLevelList != null) {
                        for (int i = 0; i < oneLevelList.size(); i++) {
                            Map oneLevel = (Map) oneLevelList.get(i);
                            String strOneTitle = (String) oneLevel.get(SELECT_ATTRIBUTE_TITLE);
                            String strRootName = (String)rootTask.get(SELECT_NAME);
                            strOneLeveName = strRootName ;
                            //一级文件夹已经创建了
                            if (strOneTitle.equals(strRootName)){
                                String strOneId = (String) oneLevel.get(SELECT_ID);
                                isCreateOneLevel = true;
                                strOneLevelId = strOneId;
                                break;
                            }

                        }
                    }else {
                        strOneLeveName = (String)rootTask.get(SELECT_NAME) ;
                    }
                    _logger.info("strOneLevelId:{}",strOneLevelId);
                    _logger.info("strOneLeveName:{}",strOneLeveName);
                    DomainObject oneLevel = DomainObject.newInstance(context);
                    //创建一级文件夹
                    if (!isCreateOneLevel){
                        Map attrMap = new HashMap<>();
                        attrMap.put(ATTRIBUTE_TITLE,strOneLeveName);
                        strOneLevelId = createSubVault(context, attrMap, strPersonId, strProjectDocumentId, "Inherited");
                        //连接项目文件夹
                    }
                    oneLevel.setId(strOneLevelId);
                    _logger.info("strProjectRole:{}",strProjectRole);
                    //为空直接挂在一级节点上
                    if (UIUtil.isNullOrEmpty(strProjectRole)){
                        //获取到当前节点关联的交付物
                        StringList alreadyConnList = oneLevel.getInfoList(context, "from[Vaulted Objects].to.id");
                        _logger.info("alreadyConnList:{}",alreadyConnList);
                        //如果存在已经关联交付物文档需要移除
                        if (alreadyConnList.size() > 0){
                            deliverableIdList.removeAll(alreadyConnList);
                        }
                        if (deliverableIdList.size() > 0){
                            DomainRelationship.connect(context,oneLevel, RELATIONSHIP_VAULTED_OBJECTS, true, deliverableIdList.toStringArray());
                        }
                        if (isStartTransaction){
                            ContextUtil.commitTransaction(context);
                        }
                        return 0;
                    }
                    List twoLevelList = (List) groupMap.get("2");
                    //获取项目角色中文国际化翻译
                    StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, ATTR_PROJECT_ROLE, StringList.create(strProjectRole), Locale.CHINA.toString());
                    StringList nlsRangesUs = EnoviaResourceBundle.getAttrRangeI18NStringList(context, ATTR_PROJECT_ROLE, StringList.create(strProjectRole), Locale.US.toString());
                    String strProjectRoleNlsCN = nlsRanges.get(0);
                    String strProjectRoleNlsEN = nlsRangesUs.get(0);
                    _logger.info("strProjectRoleNls:{},{}",strProjectRoleNlsCN,strProjectRoleNlsEN);
                    String strFullProjectRoleName = JF_PublicMethodClass_mxJPO.buildStringInStrings(strProjectRoleNlsCN,"|",strProjectRoleNlsEN);
                    //判断二级文件夹是否创建
                    boolean isCreateTwoLevel = false;
                    String strTwoLevelId = "";
                    if (twoLevelList != null) {
                        for (int i = 0; i < twoLevelList.size(); i++) {
                            Map twoLevel = (Map) twoLevelList.get(i);
                            String strOneName = (String) twoLevel.get("from.attribute[Title]");
                            String strTwoName = (String) twoLevel.get(SELECT_ATTRIBUTE_TITLE);
                            //一级文件夹名称等于 from的名称
                            //二级文件夹的名称等于project role 国际化
                            if (strOneName.equals(strOneLeveName) && strTwoName.equals(strFullProjectRoleName)){
                                isCreateTwoLevel = true;
                                strTwoLevelId = (String) twoLevel.get(SELECT_ID);
                                break;
                            }
                        }
                    }
                    _logger.info("strTwoLevelId:{}",strTwoLevelId);
                    DomainObject twoLevel = DomainObject.newInstance(context);
                    if (!isCreateTwoLevel){
                        Map attrMap = new HashMap<>();
                        attrMap.put(ATTRIBUTE_TITLE,strFullProjectRoleName);
                        strTwoLevelId = createSubVault(context, attrMap, strPersonId, strOneLevelId, "Inherited");
                    }
                    twoLevel.setId(strTwoLevelId);
                    //获取到当前节点关联的交付物
                    StringList alreadyConnList = twoLevel.getInfoList(context, "from[Vaulted Objects].to.id");
                    _logger.info("alreadyConnList:{}",alreadyConnList);
                    //如果存在已经关联交付物文档需要移除
                    if (alreadyConnList.size() > 0){
                        deliverableIdList.removeAll(alreadyConnList);
                    }
                    if (deliverableIdList.size() > 0){
                        //二级文件夹关联交付物
                        DomainRelationship.connect(context,twoLevel, RELATIONSHIP_VAULTED_OBJECTS, true, deliverableIdList.toStringArray());
                    }
                    ContextUtil.commitTransaction(context);
                } catch (Exception e) {
                    _logger.error(e.getMessage());
                    ContextUtil.abortTransaction(context);
                    throw new RuntimeException(e);
                }
            }
            return 0;
        }finally {
            if (isPush){
                ContextUtil.popContext(context);
            }
        }
    }

    public Map getRootTaskBySunTask(Context context,DomainObject task) throws Exception{
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(SELECT_ATTR_PROJECT_ROLE);
        MapList objectList = task.getRelatedObjects(context,
                DomainConstants.RELATIONSHIP_SUBTASK, // relationship pattern
                DomainConstants.TYPE_TASK_MANAGEMENT,                                // object pattern
                typeSelectList,                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                true,                                        // to direction
                false,                                        // from direction
                (short) 0,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        //重新排序从从低到高
        objectList.addSortKey(STRING_LEVEL, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);
        objectList.sort();
        boolean isInit = false;
        Map res = new HashMap<>();
        String strSaveProjectRole = "";
        for (int i = 0; i < objectList.size(); i++) {
            Map sunTask = (Map) objectList.get(i);
            String strProjectRole = (String) sunTask.get(SELECT_ATTR_PROJECT_ROLE);
            if (UIUtil.isNotNullAndNotEmpty(strProjectRole) && (!isInit)){
                isInit = true;
                strSaveProjectRole = strProjectRole;
            }
            //获取根节点
            if (i == objectList.size()-1){
                res = sunTask;
            }
        }
        //覆盖实际的项目角色值
        res.put(SELECT_ATTR_PROJECT_ROLE,strSaveProjectRole);
        return res;
    }
    public StringList getTaskDeliverableByTask(Context context,DomainObject task) throws Exception{
        MapList objectList = task.getRelatedObjects(context,
                "Task Deliverable", // relationship pattern
                TYPE_DOCUMENT,                                // object pattern
                JF_Util_mxJPO.basicBolistSel(),                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 0,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        StringList res = (StringList)objectList.stream().map(m ->{
            Map taskMap = (Map) m;
            return taskMap.get(SELECT_ID);
        }).collect(Collectors.toCollection(StringList::new));
        return  res;
    }

    /**
    *
    *@description copyOOTB创建子书签方法
    *@param context
	*@param attrMap 属性 map
	*@param strPersonId 创建人id
	*@param strParentId 父书签id
	*@param strAccessType 权限类型
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2024/10/21 13:23
    */
    public static String createVault(Context context ,Map attrMap, String strPersonId,String strParentId,String strAccessType) throws Exception{
        BusinessObject boProject = new BusinessObject(strParentId);
        boProject.open(context);
        Workspace workspace  = (Workspace) DomainObject.newInstance(context,DomainConstants.TYPE_WORKSPACE,DomainConstants.TEAM);
        workspace.setId(strParentId);
        StringList selects = new StringList(3);
        selects.add("physicalid");
        selects.add("project");
        selects.add("organization");
        selects.add("vault");
        Map workspaceData = workspace.getInfo(context, selects);
        String strProjectRevision = (String)workspaceData.get("physicalid");
        String strProject = (String)workspaceData.get("project");
        String strOrg = (String)workspaceData.get("organization");
        String strFolderOwnership = "context";
        WorkspaceVault wVault =  new WorkspaceVault();
        WorkspaceCreateMdl workspaceValutMod = new WorkspaceCreateMdl();
        String strWorkpsaceObjId = workspaceValutMod.createVault(context, TYPE_WORKSPACE_VAULT, null, POLICY_PROJECT, null, attrMap, "", true);
        wVault.setId(strWorkpsaceObjId);
        strFolderOwnership = EnoviaResourceBundle.getProperty(context,"enoFolderManagement.FolderOwnership");
        _logger.info("strFolderOwnership:{}",strFolderOwnership);
        if(!"context".equalsIgnoreCase(strFolderOwnership))
        {
            if(UIUtil.isNullOrEmpty(strProject) && UIUtil.isNullOrEmpty(strOrg) ) {
                wVault.removePrimaryOwnership(context);
            } else {
                wVault.setPrimaryOwnership(context, strProject, strOrg);
            }
        }
        String strFolderId = wVault.getObjectId();
        //创建ownership
        DomainAccess.createObjectOwnership(context, strFolderId, strPersonId, DomainAccess.getOwnerAccessName(context, strFolderId), DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
        if(WorkspaceVault.ATTRIBUTE_ACCESSTYPE_SPECIFIC.equals(strAccessType)){
            if(WorkspaceVault.isDefaultWSOaccessGrantEnabled(context)){
                String workspaceOwner = workspace.getInfo(context,DomainConstants.SELECT_OWNER);
                String sUserId = com.matrixone.apps.domain.util.PersonUtil.getPersonObjectID(context, workspaceOwner);
                if(UIUtil.isNotNullAndNotEmpty(workspaceOwner) && !(workspaceOwner.equals(context.getUser()))){
                    DomainAccess.createObjectOwnership(context, strFolderId,sUserId, DomainAccess.getOwnerAccessName(context, strFolderId), DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                }
            }
        }
        //连接父级书签
        wVault.connect(context, RELATIONSHIP_PROJECT_VAULTS, (DomainObject) workspace, true);
        SubscriptionManager subscriptionMgr = workspace.getSubscriptionManager();
        subscriptionMgr.publishEvent(context, workspace.EVENT_FOLDER_CREATED, strFolderId);
        return strWorkpsaceObjId;
    }
    public static String createSubVault(Context context ,Map attrMap, String strPersonId,String strParentId,String strAccessType) throws Exception{
        Workspace workspace = (Workspace) DomainObject.newInstance(context,DomainConstants.TYPE_WORKSPACE,DomainConstants.TEAM);
        workspace.setId(strParentId);
        com.dassault_systemes.enovia.workspace.modeler.WorkspaceVault parentBookmark = new com.dassault_systemes.enovia.workspace.modeler.WorkspaceVault(strParentId);
        com.dassault_systemes.enovia.workspace.modeler.WorkspaceVault workspacesubBookmark = parentBookmark.createSubVault(context,DomainObject.TYPE_PROJECT_VAULT, null, POLICY_PROJECT, attrMap, "", true);
        String strObjectId = workspacesubBookmark.getObjectId();
        // Publish workspace  Event 'Folder Created'.
        SubscriptionManager subscriptionMgr = workspace.getSubscriptionManager();
        subscriptionMgr.publishEvent(context, workspace.EVENT_FOLDER_CREATED, workspacesubBookmark.getObjectId());
        //不会执行
//        if(WorkspaceVault.ATTRIBUTE_ACCESSTYPE_SPECIFIC.equals(strAccessType)){
//            attrMap.put(DomainConstants.ATTRIBUTE_ACCESS_TYPE,strAccessType);
//            DomainAccess.createObjectOwnership(context, strObjectId, strPersonId, DomainAccess.getOwnerAccessName(context,strObjectId), DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
//            if(WorkspaceVault.isDefaultWSOaccessGrantEnabled(context)){
//                String workspaceOwner = workspace.getInfo(context,DomainConstants.SELECT_OWNER);
//                if(UIUtil.isNotNullAndNotEmpty(workspaceOwner) && !(workspaceOwner.equals(context.getUser()))){
//                    DomainAccess.createObjectOwnership(context, strObjectId, PersonUtil.getPersonObjectID(context, workspaceOwner), DomainAccess.getOwnerAccessName(context,strObjectId), DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
//                }
//            }
//        }
        return strObjectId;
    }
}
