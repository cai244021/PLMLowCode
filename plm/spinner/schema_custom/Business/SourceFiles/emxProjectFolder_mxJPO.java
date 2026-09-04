/*
 *  emxProjectFolder.java
 *
 * Copyright (c) 1992-2020 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.7.2.2 Thu Dec  4 07:56:10 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.7.2.1 Thu Dec  4 01:55:03 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.7 Wed Oct 22 15:50:28 2008 przemek Experimental przemek $
 */
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import matrix.db.*;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @version PMC 10-6 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxProjectFolder_mxJPO extends emxProjectFolderBase_mxJPO
{
    private static final Logger logger = LoggerFactory.getLogger(emxProjectFolder_mxJPO.class);
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public emxProjectFolder_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }
    /** copy base
     * Gets Workspace Vault table expanded data
     *
     * @param context The Matrix Context object
     * @param args Packed program and request maps for the table
     * @return MapList containing all table data
     * @throws Exception if operation fails
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableExpandProjectVaultData(Context context, String[]args) throws Exception
    {
        MapList mlProjectVaultContentList = null;

        Map programMap = (Map)JPO.unpackArgs(args);
        String strObjectId =(String) programMap.get("objectId");
        String strSelectedTable =(String) programMap.get("selectedTable");
        String strExpandLevel = (String) programMap.get("expandLevel");
        String PMCShowArchievedStructure = (String) programMap.get("PMCShowArchievedStructure");
        //20260806 update by ljr 项目文件夹文档版本过滤；未传参数时保持原逻辑，默认显示所有版本。
        String documentVersionFilter = (String) programMap.get("JFDocumentVersionFilter");
        boolean showLatestVersion = "latest".equalsIgnoreCase(documentVersionFilter);
        boolean showLatestReleasedVersion = "latestReleased".equalsIgnoreCase(documentVersionFilter);

        String strTypePattern = DomainConstants.EMPTY_STRING;
        String strRelationshipPattern = DomainConstants.EMPTY_STRING;
        short recurseToLevel = ProgramCentralUtil.getExpandLevel(strExpandLevel);
        boolean getFrom = true;
        boolean getTo = false;
        String strRelWhere = null;
        String strBusWhere = DomainObject.SELECT_CURRENT+"!~~"+DomainConstants.STATE_CONTROLLED_FOLDER_SUPERCEDED;
        final String SELECT_IS_PART  =  "type.kindof["+ CommonDocument.TYPE_PART+"]";

        StringList slRelSelect = new StringList(3);
        slRelSelect.add(DomainRelationship.SELECT_ID);
        slRelSelect.add(DomainRelationship.SELECT_FROM_ID);
        slRelSelect.add(DomainRelationship.SELECT_FROM_TYPE);
        slRelSelect.add(DomainRelationship.SELECT_FROM_REVISION);
        slRelSelect.add("from.current");
        slRelSelect.add("from.current.access[fromdisconnect]");

        StringList slBusSelect = new StringList(10);
        //slBusSelect.add(DomainConstants.SELECT_NAME); di7
        slBusSelect.add(DomainConstants.SELECT_ID);
        slBusSelect.add(DomainConstants.SELECT_TYPE);
        slBusSelect.add(DomainConstants.SELECT_REVISION);
        slBusSelect.add(DomainConstants.SELECT_OWNER);
        slBusSelect.add(DomainConstants.SELECT_OWNER+".isaperson");
        slBusSelect.add(DomainConstants.SELECT_OWNER+".isagroup");
        slBusSelect.add(DomainConstants.SELECT_OWNER+".isarole");
        slBusSelect.add(ProgramCentralConstants.SELECT_GRANTEE);
        slBusSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);
        slBusSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_ROLE_VAULT_ACCESS);

        //selectables for performance optimization: Start

        slBusSelect.add(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);//editAccessToFolderRows //editAccessToTitleNameColumn //columnDropZone
        slBusSelect.add(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);//editAccessToFolderRows//editAccessToTitleNameColumn
        slBusSelect.add(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);//editAccessToFolderRows//editAccessToTitleNameColumn
        //owner   //editAccessToFolderRows //editAccessToTitleNameColumn

        slBusSelect.add(CommonDocument.SELECT_IS_VERSION_OBJECT);   //editAccessToTitleNameColumn  //columnDragIcon
        slBusSelect.add(ProgramCentralConstants.SELECT_IS_DOCUMENTS); //editAccessToTitleNameColumn  //columnDragIcon
        slBusSelect.add(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER); //editAccessToTitleNameColumn

        slBusSelect.add(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT); //columnDragIcon //columnDropZone
        slBusSelect.add(ProgramCentralConstants.SELECT_CURRENT);//columnDragIcon //columnDropZone
        slBusSelect.add("current.access[modify]");
        slBusSelect.add("current.access[fromconnect]");
        slBusSelect.add("current.access[fromdisconnect]");
        slBusSelect.add("current.access[toconnect]");
        slBusSelect.add("current.access[todisconnect]");
        slBusSelect.add(SELECT_IS_PART);

        //20260810 update by ljr 版本过滤统一按last.id分组；发布版本额外查询策略以解析实际发布状态名。
        if (showLatestVersion || showLatestReleasedVersion) {
            slBusSelect.add("last.id");
        }
        if (showLatestReleasedVersion) {
            slBusSelect.add(DomainConstants.SELECT_POLICY);
        }

        String SELECT_IS_URL  =  "type.kindof["+CommonDocument.TYPE_URL+"]";
        slBusSelect.add(SELECT_IS_URL);//Action column

        //	slBusSelect.add("current.access"); //columnDragIcon  //columnDropZone

        //selectables for performance optimization: end
        StringList slObjTypeSelectables = new StringList(5);
        slObjTypeSelectables.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_MANAGEMENT);
        slObjTypeSelectables.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
        slObjTypeSelectables.add(ProgramCentralConstants.SELECT_IS_KINDOF_RISKMANAGEMENT);
        slObjTypeSelectables.add(ProgramCentralConstants.SELECT_KINDOF_ASSESSMENT);
        slObjTypeSelectables.add(ProgramCentralConstants.SELECT_IS_DOCUMENTS);
        slObjTypeSelectables.add(ProgramCentralConstants.SELECT_CURRENT);

        //DomainObject domObject = DomainObject.newInstance(context,strObjectId);
        //Map objInfoMap = domObject.getInfo(context, slObjTypeSelectables);

        MapList objecInfoList = ProgramCentralUtil.getObjectDetails(context, new String[]{strObjectId}, slObjTypeSelectables,false);
        Map objectMap = (Map)objecInfoList.get(0);

        String isKindOfProjectMgmt = (String) objectMap.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_MANAGEMENT);
        String isKindOfTaskMgmt = (String) objectMap.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
        String isKindOfRiskMgmt = (String) objectMap.get(ProgramCentralConstants.SELECT_IS_KINDOF_RISKMANAGEMENT);
        String isKindOfAssessment = (String) objectMap.get(ProgramCentralConstants.SELECT_KINDOF_ASSESSMENT);
        String isDocument 	= (String) objectMap.get(ProgramCentralConstants.SELECT_IS_DOCUMENTS);
        String documentState = (String) objectMap.get(ProgramCentralConstants.SELECT_CURRENT);
        logger.info("objectMap:{}",objectMap);
        logger.info("strExpandLevel:{}",strExpandLevel);
        //Added If condition to show the archived structure of archived controlled Bookmark
        if("true".equalsIgnoreCase(PMCShowArchievedStructure) && "Superceded".equalsIgnoreCase(documentState)){
            strBusWhere = "";
        }

        DomainObject domObject = DomainObject.newInstance(context,strObjectId);

        //For initial loading of folder page.
        if("true".equalsIgnoreCase(isKindOfRiskMgmt) && "1".equals(strExpandLevel)) {

            String selectable = "to["+DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY+"].from.from["+DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST+"].to.id";
            String projectId = domObject.getInfo(context, selectable);
            DomainObject parentProject = DomainObject.newInstance(context,projectId);
            boolean isProject=parentProject.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_MANAGEMENT);
            if(isProject){
                strRelationshipPattern =  DomainConstants.RELATIONSHIP_PROJECT_VAULTS;
                strTypePattern = DomainConstants.TYPE_WORKSPACE_VAULT;
                domObject.setId(projectId);
            }
        } else if("true".equalsIgnoreCase(isKindOfTaskMgmt)) {
            String projectId = domObject.getInfo(context,  ProgramCentralConstants.SELECT_PROJECT_ID);
            domObject.setId(projectId);
            strRelationshipPattern =  DomainConstants.RELATIONSHIP_PROJECT_VAULTS;
            strTypePattern = DomainConstants.TYPE_WORKSPACE_VAULT;
        }
        else if("true".equalsIgnoreCase(isKindOfProjectMgmt)&& "false".equalsIgnoreCase(isKindOfTaskMgmt)
                && "1".equals(strExpandLevel)) {
            String SELECT_BOOKMARK_PHYSICAL_ID = DomainObject.getAttributeSelect(PropertyUtil.getSchemaProperty("attribute_BookmarkPhysicalId"));
            String bookmarkWSId = domObject.getInfo(context,  SELECT_BOOKMARK_PHYSICAL_ID);
            if((bookmarkWSId != null) && (!bookmarkWSId.equals(""))){
                domObject = DomainObject.newInstance(context,bookmarkWSId);
            }

            strRelationshipPattern =  DomainConstants.RELATIONSHIP_PROJECT_VAULTS;
            strTypePattern = DomainConstants.TYPE_WORKSPACE_VAULT;

        }else if("true".equalsIgnoreCase(isKindOfAssessment)){

            StringList busSelects = new StringList();
            busSelects.add(DomainConstants.SELECT_ID);
            Map projectMap = domObject.getRelatedObject(context,ProgramCentralConstants.RELATIONSHIP_PROJECT_ASSESSMENT,false,busSelects,null);

            String projectId = (String)projectMap.get(DomainConstants.SELECT_ID);
            domObject = DomainObject.newInstance(context,projectId);

            strRelationshipPattern = DomainConstants.RELATIONSHIP_PROJECT_VAULTS;
            strTypePattern = DomainConstants.QUERY_WILDCARD;
        }else if("true".equalsIgnoreCase(isDocument)){
            strRelationshipPattern = DomainConstants.RELATIONSHIP_LINK_URL + "," +
                    CommonDocument.RELATIONSHIP_ACTIVE_VERSION;

            strTypePattern = DomainConstants.QUERY_WILDCARD;

            slBusSelect.add(CommonDocument.SELECT_IS_VERSION_OBJECT);

        }else { //For subsequent loading of folder page.

			/*slBusSelect.add(CommonDocument.SELECT_HAS_LOCK_ACCESS);
			slBusSelect.add(CommonDocument.SELECT_LOCKER);
			slBusSelect.add(CommonDocument.SELECT_LOCKED);
			slBusSelect.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
			slBusSelect.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
			slBusSelect.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
			slBusSelect.add(CommonDocument.SELECT_HAS_UNLOCK_ACCESS);

			slBusSelect.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
			slBusSelect.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
			slBusSelect.add(CommonDocument.SELECT_FILE_NAME);
			slBusSelect.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
			slBusSelect.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
			slBusSelect.add(CommonDocument.SELECT_FILE_FORMAT);
			slBusSelect.add(CommonDocument.SELECT_FILE_SIZE);
			slBusSelect.add(CommonDocument.SELECT_TITLE );*/
            slBusSelect.add(CommonDocument.SELECT_IS_VERSION_OBJECT);

			/*slBusSelect.add("revisions");
			slBusSelect.add("revisions.id");
			slBusSelect.add("last.revision");*/
            slBusSelect.add(ProgramCentralConstants.SELECT_ACTIVE_VERSION_PRESENT);

            strRelationshipPattern = DomainConstants.RELATIONSHIP_PROJECT_VAULTS + "," +
                    DomainConstants.RELATIONSHIP_LINK_URL + "," +
                    DomainConstants.RELATIONSHIP_SUB_VAULTS + "," +
                    DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2 + "," +
                    DomainConstants.RELATIONSHIP_LINKED_FOLDERS;

            strTypePattern = DomainConstants.QUERY_WILDCARD;
        }
logger.info("strRelationshipPattern:{} strTypePattern:{}",strRelationshipPattern,strTypePattern);
        mlProjectVaultContentList = domObject.getRelatedObjects(context,
                strRelationshipPattern,
                strTypePattern,
                slBusSelect,
                slRelSelect,
                getTo,
                getFrom,
                recurseToLevel,
                strBusWhere,
                strRelWhere,
                0);

        //20260810 update by ljr 只过滤文档主对象，按平台修订链从后向前选择当前文件夹内符合条件的修订。
        if (showLatestVersion || showLatestReleasedVersion) {
            Map<String, Map> documentMapById = new HashMap<String, Map>();
            Map<String, Set<String>> documentIdsByRevisionChain = new HashMap<String, Set<String>>();
            for (int i = 0; i < mlProjectVaultContentList.size(); i++) {
                Map contentMap = (Map) mlProjectVaultContentList.get(i);
                String isDocuments = (String) contentMap.get(ProgramCentralConstants.SELECT_IS_DOCUMENTS);
                String isVersionObject = (String) contentMap.get(CommonDocument.SELECT_IS_VERSION_OBJECT);
                if (!"true".equalsIgnoreCase(isDocuments) || "true".equalsIgnoreCase(isVersionObject)) {
                    continue;
                }

                String documentId = (String) contentMap.get(DomainConstants.SELECT_ID);
                String revisionChainId = (String) contentMap.get("last.id");
                if (revisionChainId == null || revisionChainId.length() == 0) {
                    revisionChainId = documentId;
                }
                documentMapById.put(documentId, contentMap);
                Set<String> documentIds = documentIdsByRevisionChain.get(revisionChainId);
                if (documentIds == null) {
                    documentIds = new HashSet<String>();
                    documentIdsByRevisionChain.put(revisionChainId, documentIds);
                }
                documentIds.add(documentId);
            }

            Map<String, String> targetDocumentIdByRevisionChain = new HashMap<String, String>();
            Map<String, String> releasedStateByPolicy = new HashMap<String, String>();
            for (Map.Entry<String, Set<String>> revisionChainEntry : documentIdsByRevisionChain.entrySet()) {
                String targetDocumentId = DomainConstants.EMPTY_STRING;
                BusinessObjectList revisionList = DomainObject.newInstance(context, revisionChainEntry.getKey()).getRevisions(context);
                for (int revisionIndex = revisionList.size() - 1; revisionIndex >= 0; revisionIndex--) {
                    BusinessObject revisionObject = revisionList.get(revisionIndex);
                    String revisionId = revisionObject.getObjectId(context);
                    if (!revisionChainEntry.getValue().contains(revisionId)) {
                        continue;
                    }
                    if (showLatestVersion) {
                        targetDocumentId = revisionId;
                        break;
                    }

                    Map revisionMap = documentMapById.get(revisionId);
                    String policy = (String) revisionMap.get(DomainConstants.SELECT_POLICY);
                    String current = (String) revisionMap.get(ProgramCentralConstants.SELECT_CURRENT);
                    String releasedState = releasedStateByPolicy.get(policy);
                    if (releasedState == null && policy != null && policy.length() > 0) {
                        releasedState = FrameworkUtil.lookupStateName(context, policy, "state_RELEASED");
                        releasedStateByPolicy.put(policy, releasedState);
                    }
                    if (releasedState != null && releasedState.equalsIgnoreCase(current)) {
                        targetDocumentId = revisionId;
                        break;
                    }
                }
                targetDocumentIdByRevisionChain.put(revisionChainEntry.getKey(), targetDocumentId);
            }

            for (int i = mlProjectVaultContentList.size() - 1; i >= 0; i--) {
                Map contentMap = (Map) mlProjectVaultContentList.get(i);
                String documentId = (String) contentMap.get(DomainConstants.SELECT_ID);
                if (!documentMapById.containsKey(documentId)) {
                    continue;
                }
                String revisionChainId = (String) contentMap.get("last.id");
                if (revisionChainId == null || revisionChainId.length() == 0) {
                    revisionChainId = documentId;
                }
                if (!documentId.equals(targetDocumentIdByRevisionChain.get(revisionChainId))) {
                    mlProjectVaultContentList.remove(i);
                }
            }
        }

        int size = mlProjectVaultContentList.size();
        //Removing Project BookMarks from the Folder Summary page, only Folder BookMarks will be shown.
        if("true".equalsIgnoreCase(isKindOfProjectMgmt) && "false".equalsIgnoreCase(isKindOfTaskMgmt)) {

            for (int i = 0; i < size; i++) {

                Map mapProjectVaultContent = (Map) mlProjectVaultContentList.get(i);
                String level = (String) mapProjectVaultContent.get(DomainConstants.SELECT_LEVEL);
                String type = (String) mapProjectVaultContent.get(DomainConstants.SELECT_TYPE);

                if ("1".equals(level) && DomainConstants.TYPE_URL.equalsIgnoreCase(type)) {
                    mlProjectVaultContentList.remove(mapProjectVaultContent);
                    size = size-1;
                    i--;
                }
            }
        }

        String strCurrentReadAccess = null;
        String masterId = null;
        for (int i = 0; i < size; i++) {

            Map mapProjectVaultContent = (Map) mlProjectVaultContentList.get(i);
            masterId = (String)mapProjectVaultContent.get(DomainRelationship.SELECT_FROM_ID);
            mapProjectVaultContent.put("masterId", masterId);

            String isVersionObj = (String)mapProjectVaultContent.get(CommonDocument.SELECT_IS_VERSION_OBJECT);

            String isDoc  = (String)mapProjectVaultContent.get(ProgramCentralConstants.SELECT_IS_DOCUMENTS);
            String aciveVersionPresent = (String)mapProjectVaultContent.get(ProgramCentralConstants.SELECT_ACTIVE_VERSION_PRESENT);
            if("true".equalsIgnoreCase(isDoc) && "true".equalsIgnoreCase(aciveVersionPresent)){
                mapProjectVaultContent.put("hasChildren","true");
            }

            if("true".equalsIgnoreCase(isVersionObj)){
                mapProjectVaultContent.put("disableSelection","true");
                strCurrentReadAccess =  MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",masterId,SELECT_CURRENT_READ_ACCESS);
                if (!"TRUE".equalsIgnoreCase(strCurrentReadAccess))
                {
                    mlProjectVaultContentList.remove(mapProjectVaultContent);
                }
            }

        }

        return mlProjectVaultContentList;
    }

}
