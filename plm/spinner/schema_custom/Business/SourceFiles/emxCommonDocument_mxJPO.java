/*
**   emxCommonDocument.java
**
**   Copyright (c) 1992-2020 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/

import com.aspose.pdf.operators.Do;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.util.ComponentsUtil;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.xmlbeans.impl.store.DomImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class emxCommonDocument_mxJPO extends emxCommonDocumentBase_mxJPO
{

    private static final String PARAM_OBJECT_ID = "objectId";
    private static final String PARAM_NO_OF_FILES = "noOfFiles";
    private static final String PARAM_FILENAME = "fileName";
    private static final String PARAM_FCSENABLED = "fcsEnabled";
    private static final String PARAM_NULL = "null";
    private static final String PARAM_TRUE = "true";
    private static final String PARAM_FALSE = "false";
    private static final String PARAM_PARENT_ID = "parentId";
    private static final String EMXCOMPONENTS_STR_RESOURCE = "emxComponentsStringResource";
    private static final String PARAM_ERROR_MESSAGE = "errorMessage";
    private static final String PARAM_COMMENTS = "comments";

    private static final Logger _log = LoggerFactory.getLogger(emxCommonDocumentBase_mxJPO.class);


    /**
       * Constructor.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @throws Exception if the operation fails
       * @since Common 10.0.0.0
       * @grade 0
       */
      public emxCommonDocument_mxJPO (Context context, String[] args)
          throws Exception
      {
          super(context, args);
      }

      /**
       * This method is executed if a specific method is not specified.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @returns int
       * @throws Exception if the operation fails
       * @since Common 10.0.0.0
       */
      public int mxMain(Context context, String[] args)
          throws Exception
      {
          if (true)
          {
              throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.MethodOnCommonFile", context.getLocale().getLanguage()));
          }
          return 0;
      }


    /**
     * This is the method executed in common Document model to create master using
     * FCS/NonFCS.
     *
     * @param context         the eMatrix <code>Context</code> object
     * @param uploadParamsMap holds all arguments passed through checkin screens.
     * @param createVersion   boolean to create version objects for master object.
     * @returns Map objectMap which contains objectId, filename, format pairs and
     *          errorMessage if any error.
     * @throws Exception if the operation fails
     * @since VCP 10.5.0.0
     */
    public HashMap createMaster(Context context, HashMap uploadParamsMap, boolean createVersion) throws Exception {
        HashMap objectMap = new HashMap();
        try {
            //yb start 2024.8.1
            String JF_DocumentType = (String) uploadParamsMap.get("JF_DocumentType");
            String JF_DocSecurity = (String) uploadParamsMap.get("JF_DocSecurity");
            String personId = (String) uploadParamsMap.get("personNameOID");
            //yb end 2024.8.1

            String parentId = (String) uploadParamsMap.get(PARAM_PARENT_ID);
            String objectId = (String) uploadParamsMap.get(PARAM_OBJECT_ID);
            String fcsEnabled = (String) uploadParamsMap.get(PARAM_FCSENABLED);
            String parentRelName = (String) uploadParamsMap.get("parentRelName");
            if (parentRelName != null) {
                parentRelName = PropertyUtil.getSchemaProperty(context, parentRelName);
            }
            String isFrom = (String) uploadParamsMap.get("isFrom");

            String strCounnt = (String) uploadParamsMap.get(PARAM_NO_OF_FILES);
            int count = new Integer(strCounnt).intValue();

            boolean isFilePresent = false;

            // Defining the ObjectMap parameters and putting them into the Map.
            StringList objectIds = new StringList(count);
            StringList formats = new StringList(count);
            StringList fileNames = new StringList(count);
            objectMap.put(DomainConstants.KEY_FORMAT, formats);
            objectMap.put(PARAM_FILENAME, fileNames);
            objectMap.put(PARAM_OBJECT_ID, objectIds);
            String errorMessage = "";

            // Master Object Parameters
            String type = (String) uploadParamsMap.get(DomainConstants.SELECT_TYPE);
            String name = (String) uploadParamsMap.get("name");
            String revision = (String) uploadParamsMap.get("revision");
            String policy = (String) uploadParamsMap.get("policy");
            String mDescription = (String) uploadParamsMap.get(DomainConstants.SELECT_DESCRIPTION);
            String title = (String) uploadParamsMap.get("title");
            String language = (String) uploadParamsMap.get("language");
            String vault = (String) uploadParamsMap.get("vault");
            Map mAttrMap = (Map) uploadParamsMap.get("attributeMap");
            String objectGeneratorRevision = (String) uploadParamsMap.get("objectGeneratorRevision");
            String owner = (String) uploadParamsMap.get("person");

            if (mDescription == null || "".equals(mDescription) || PARAM_NULL.equals(mDescription)) {
                mDescription = (String) uploadParamsMap.get("mDescription");
            }

            if (title == null || "".equals(title) || PARAM_NULL.equals(title)) {
                title = null;
            }

            // passing relationship name as a url parameter with out using Mapping File
            CommonDocument object = (CommonDocument) DomainObject.newInstance(context, CommonDocument.TYPE_DOCUMENTS);
            DomainObject parentObject = null;
            if (parentId != null && !"".equals(parentId) && !PARAM_NULL.equals(parentId)) {
                parentObject = DomainObject.newInstance(context, parentId);
            }
            // FZS - Hitachi IR-368889
            PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", PARAM_TRUE, false);
            object = object.createAndConnect(context, type, name, revision, policy, mDescription, vault, title, language, parentObject, parentRelName, isFrom, mAttrMap, objectGeneratorRevision);
            StringList selects = new StringList(2);
            selects.add(DomainConstants.SELECT_ID);
            selects.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
            // Need to Add back -SC
            // selects.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
            Map objectSelectMap = object.getInfo(context, selects);

            objectId = (String) objectSelectMap.get(DomainConstants.SELECT_ID);
            boolean moveFilesToVersion = Boolean.valueOf((String) objectSelectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION)).booleanValue();

            // Iterating through multiple files and creating version objects
            // for each file and connecting with Master Object.
            boolean deleteDummy = false;
            CommonDocument dummyObject = (CommonDocument) DomainObject.newInstance(context, CommonDocument.TYPE_DOCUMENT);
            for (int i = 0; i < count; i++) {
                Map attrMap = (Map) uploadParamsMap.get("attrMap" + i);
                if (attrMap == null) {
                    attrMap = new HashMap();
                }
                // 364067 - inheriting attributest to files
                Set attrSet = mAttrMap.keySet();
                Iterator attrItr = attrSet.iterator();
                String attrName = null;
                String attrvalue = "";
                while (attrItr.hasNext()) {
                    attrName = (String) attrItr.next();
                    attrvalue = (String) mAttrMap.get(attrName);
                    attrMap.put(attrName, attrvalue);
                }
                // 364067 - inheriting attributest to files ends
                String description = (String) uploadParamsMap.get(DomainConstants.SELECT_DESCRIPTION + i);
                String comments = (String) uploadParamsMap.get(PARAM_COMMENTS + i);
                attrMap.put(CommonDocument.ATTRIBUTE_CHECKIN_REASON, comments);
                String fileName = (String) uploadParamsMap.get(PARAM_FILENAME + i);
                String format = (String) uploadParamsMap.get(DomainConstants.KEY_FORMAT + i);
                if (fileName != null && !"".equals(fileName) && !PARAM_NULL.equals(fileName)) {
                    isFilePresent = true;
                    if (!checkDuplicate(context, uploadParamsMap, fileName, objectId)) {
                        formats.addElement(format);
                        fileNames.addElement(fileName);
                        String checkinId = objectId;
                        if (createVersion) {
                            checkinId = object.createVersion(context, description, fileName, attrMap);
                        }
                        if (moveFilesToVersion) {
                            objectIds.addElement(checkinId);
                        } else {
                            objectIds.addElement(objectId);
                        }

                    } else {
                        if (!errorMessage.equals("")) {
                            errorMessage += ", ";
                        } else if (PARAM_TRUE.equalsIgnoreCase(fcsEnabled)) {
                            dummyObject.createObject(context, CommonDocument.TYPE_DOCUMENT, null, null, null, null);
                            deleteDummy = true;
                        }
                        if (PARAM_TRUE.equalsIgnoreCase(fcsEnabled)) {
                            formats.addElement(format);
                            fileNames.addElement(fileName);
                            objectIds.addElement(dummyObject.getObjectId());
                        }
                        errorMessage += fileName;
                    }
                }
            }

            if (isFilePresent == false) // if no files are uploaded, Document id is added to the return map
            {
                objectIds.addElement(objectId);
            }

            //yb start 2024.8.1
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            if (JF_DocumentType != null && !"".equals(JF_DocumentType)){
                domainObject.setAttributeValue(context, "JF_DocumentType", JF_DocumentType);
            }
            if (JF_DocSecurity != null && !"".equals(JF_DocSecurity)){
                domainObject.setAttributeValue(context, "JF_DocSecurity", JF_DocSecurity);
            }
            if (personId != null && !"".equals(personId)){
                domainObject.addToObject(context,new RelationshipType("JFDocument2Person"),personId);
            }
            //yb end 2024.8.1

            if (!errorMessage.equals("")) {
                errorMessage += "<BR> \n" + EnoviaResourceBundle.getProperty(context, EMXCOMPONENTS_STR_RESOURCE, context.getLocale(), "emxComponentsDocumentManagement.Checkin.DocumentsAlreadyExists");
            }
            objectMap.put(PARAM_ERROR_MESSAGE, errorMessage);
            objectCheckin(context, uploadParamsMap, objectMap);
            if (owner != null && !context.getUser().equals(owner)) {
                object.setOwner(context, owner);
            }
            if (deleteDummy) {
                dummyObject.deleteObject(context, true);
            }
            return objectMap;
        } catch (Exception ex) {
            PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", PARAM_FALSE, false);
            ex.printStackTrace();
            throw ex;
        }
    }


    /**
     * This is the base method executed in common Document model to
     * checkin/update/createmaster using FCS/NonFCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds no arguments
     * @returns Map objectMap which contains objectId, filename, format pairs and
     *          errorMessage if any error.
     * @throws Exception if the operation fails
     * @since VCP 10.5.0.0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Map commonDocumentCheckin(Context context, String[] args) throws Exception {

        try {
            if (args == null || args.length < 1) {
                throw (new IllegalArgumentException());
            }
            HashMap uploadParamsMap = (HashMap) JPO.unpackArgs(args);

            String objectId = (String) uploadParamsMap.get(PARAM_OBJECT_ID);
            String parentId = (String) uploadParamsMap.get(PARAM_PARENT_ID);
            String objectAction = (String) uploadParamsMap.get("objectAction");
            //by cyl 20250424
            String drawingFlag = (String) uploadParamsMap.get("drawingFlag");

            Map objectMap = new HashMap();
            String strCounnt = (String) uploadParamsMap.get(PARAM_NO_OF_FILES);
            int count = 0;
            try {
                count = new Integer(strCounnt).intValue();
            } catch (Exception ex) {
            }

            for (int i = 0; i < count; i++) {
                String fileName = (String) uploadParamsMap.get(PARAM_FILENAME + i);
                if (fileName.length() >= 255) {
                    objectMap.put(PARAM_ERROR_MESSAGE, EnoviaResourceBundle.getProperty(context, EMXCOMPONENTS_STR_RESOURCE, context.getLocale(), "emxComponents.CommonDocument.FileNameLengthInvalid"));
                    return objectMap;
                }
            }

            ContextUtil.startTransaction(context, true);

            Map preCheckinMap = preCheckin(context, uploadParamsMap, parentId);
            String newParentId = (String) preCheckinMap.get(PARAM_PARENT_ID);
            if (newParentId != null && !"".equals(newParentId) && !PARAM_NULL.equals(newParentId)) {
                parentId = newParentId;
                uploadParamsMap.put(PARAM_PARENT_ID, parentId);
            }

            if (objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_CREATE_MASTER)) {
                /*
                 * create Master with given 'type' 'name' 'revision' and other attributes
                 * and checckin all file selected in second step to the object created.
                 * and Crete Version object for each file.
                 */
                objectMap = createMaster(context, uploadParamsMap);

            } else if (objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_CREATE_MASTER_PER_FILE)) {
                /*
                 * create Master Document with given 'Type' = Document 'name' = autoName
                 * 'revision' = default
                 * and checckin each file in separate object created.
                 * and Crete Version object for each file.
                 */
                objectMap = createMasterPerFile(context, uploadParamsMap);
                //by cyl 20250424
                _log.info("createMasterPerFile====objectMap:{}",objectMap);
                if ("Drawing".equals(drawingFlag)){
                    StringList objectIds = (StringList) objectMap.get("objectId");
                        for (String s : objectIds) {
                            //设置为Drawing 代表该文档是图纸
                            DomainObject.newInstance(context,s).setAttributeValue(context,"JF_DocumentType","Drawing");
                            _log.info("createMasterPerFile====JF_DocumentType:设值成功");
                        }
                }
                //by cyl 20250424 end
            } else if (objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_MASTER)) {
                /*
                 * create Document with given 'Type' = Document 'name' = autoName 'revision' =
                 * default
                 * and checckin each file in separate object created.
                 */
                objectMap = updateMaster(context, uploadParamsMap);

            } else if (objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_VERSION_FILE)) {
                /*
                 * version a File in given object
                 */
                objectMap = versionFile(context, uploadParamsMap);
            } else if (objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_CHECKIN_WITH_VERSION)) {
                /*
                 * Checkin the files into the given object
                 * and for each file create version object.
                 */
                objectMap = checkinWithVersion(context, uploadParamsMap);
            } else if (objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_HOLDER)) {
                /*
                 * Checkin the files into the given object
                 * and for each file create version object.
                 */
                objectMap = updateHolder(context, uploadParamsMap);
            } else {
                if (objectId != null && !"".equals(objectId) && !PARAM_NULL.equals(objectId)) {
                    objectMap = objectCheckin(context, uploadParamsMap, null);
                } else {
                    /*
                     * create given type of object and checkin files in created object with out
                     * version.
                     */
                    objectMap = checkinCreateWithOutVersion(context, uploadParamsMap);

                }
            }
            if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                DomainObject newMasterObject = DomainObject.newInstance(context, objectId);
                CommonDocument.sendSubscriptionNotification(context, newMasterObject);
            }
            ContextUtil.commitTransaction(context);
            return objectMap;
        } catch (Exception ex) {
            ContextUtil.abortTransaction(context);
            ex.printStackTrace();
            throw ex;
        }
    }


    /**
     * This is the method executed in common Document model to create master object
     * using FCS/NonFCS.
     *
     * @param context         the eMatrix <code>Context</code> object
     * @param uploadParamsMap holds all arguments passed through checkin screens.
     * @returns Map objectMap which contains objectId, filename, format pairs and
     *          errorMessage if any error.
     * @throws Exception if the operation fails
     * @since VCP 10.5.0.0
     */
    public HashMap createMaster(Context context, HashMap uploadParamsMap) throws Exception {
        try {
            return createMaster(context, uploadParamsMap, true);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
public void setDocumentAccess(Context context,String[] args)throws Exception{
        String objectId = args[0];
//        String attName = args[1];
//        String oldValue = args[2];
        String newValue = args[3];
        String oldValue = args[4];
        String role = "";
        DomainObject docObj = DomainObject.newInstance(context);
        docObj.setId(objectId);
   StringList selList =  JF_Util_mxJPO.basicBolistSel();
   selList.add(DomainConstants.SELECT_PROJECT);
    MapList objectList = docObj.getRelatedObjects(context,
            DomainConstants.MVL_RELATIONSHIP_ACTIVE_VERSION, //pattern to match relationships
            DomainConstants.TYPE_DOCUMENT, //pattern to match types
            selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
            JF_Util_mxJPO.basicRellistSel(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
            false, //get To relationships
            true, //get From relationships
            (short) 1, //the number of levels to expand, 0 equals expand all.
            DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
            DomainConstants.EMPTY_STRING, //where clause to apply to relationship, can be empty ""
            (short)1 //limit
    );

    if("Y".equalsIgnoreCase(newValue)) {
        role = context.getUser() + "_PRJ";
    }else{
        //获取文件version的协作区
        role="JFSeat";
        if(objectList.size()>0){
            role=(String)((Map)(objectList.get(0))).get(DomainConstants.SELECT_PROJECT);
        }
        }
    try {
        _log.info("commn :{}",role);
        ContextUtil.startTransaction(context, true);
        String mql2 = "mod bus "+objectId+" project '"+role+"'";
        _log.info("mql:{}",mql2);
        MqlUtil.mqlCommand(context, false,mql2,true);
        ContextUtil.commitTransaction(context);
    }catch (Exception e){
        e.printStackTrace();
        ContextUtil.abortTransaction(context);
    }
}


}
