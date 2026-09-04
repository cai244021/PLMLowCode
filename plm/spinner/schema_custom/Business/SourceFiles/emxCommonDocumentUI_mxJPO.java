/*
 *  emxCommonDocumentUI.java
 *
 * Copyright (c) 1992-2020 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.VCDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.StringList;

import java.lang.*;
import java.util.*;

/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxCommonDocumentUI_mxJPO extends emxCommonDocumentUIBase_mxJPO {

    /**
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     * @since AEF Rossini
     */
    public emxCommonDocumentUI_mxJPO(Context context, String[] args)
            throws Exception {
        super(context, args);
    }

    /**
    *
    *@description 重载 Base里面方法防止冲突 在Action列加入预览PDF文件
    *@param context
	*@param args
    *@return java.util.Vector
    *@throws
    *@author CHENYAN
    *@date 2024/7/17 17:36
    */
    public static Vector getDocumentActions(Context context, String[] args)
            throws Exception {
        Vector vActions = new Vector();
        try {
            // Start MSF
            String msfRequestData = "";
            String msfFileFormatDetails = "";
            boolean isAdded = false;
            // End MSF
            HashMap programMap = (HashMap) JPO.unpackArgs(args);

            MapList objectList = (MapList) programMap.get("objectList");
            if (objectList.size() <= 0) {
                return vActions;
            }
            Map paramList = (Map) programMap.get("paramList");
            String uiType = (String) paramList.get("uiType");
            String parentOID = (String) paramList.get("parentOID");
            String customSortColumns = (String) paramList.get("customSortColumns");
            String customSortDirections = (String) paramList.get("customSortDirections");
            String table = (String) paramList.get("table");
            if (objectList == null || objectList.size() <= 0) {
                return vActions;
            }

            boolean isprinterFriendly = false;
            if (paramList.get("reportFormat") != null) {
                isprinterFriendly = true;
            }

            String languageStr = (String) context.getSession().getLanguage();
            Locale strLocale = context.getLocale();
            String sTipDownload = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", strLocale, "emxComponents.DocumentSummary.ToolTipDownload");
            String sTipCheckout = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", strLocale, "emxComponents.DocumentSummary.ToolTipCheckout");
            String sTipCheckin = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", strLocale, "emxComponents.DocumentSummary.ToolTipCheckin");
            String sTipUpdate = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", strLocale, "emxComponents.DocumentSummary.ToolTipAddFiles");
            String sTipSubscriptions = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", strLocale, "emxComponents.Command.Subscriptions");

            Map objectMap = null;
            if (objectList != null && objectList.size() > 0) {
                objectMap = (Map) objectList.get(0);
            }

            Iterator objectListItr = null;
            if (objectMap != null
                    && objectMap.containsKey(CommonDocument.SELECT_TYPE)
                    && objectMap.containsKey(CommonDocument.SELECT_SUSPEND_VERSIONING)
                    && objectMap.containsKey(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS)
                    && objectMap.containsKey(CommonDocument.SELECT_HAS_CHECKIN_ACCESS)
                    && objectMap.containsKey(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION)
                    && objectMap.containsKey(CommonDocument.SELECT_FILE_NAME)
                    && objectMap.containsKey(CommonDocument.SELECT_MOVE_FILES_TO_VERSION)
                    && objectMap.containsKey(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT)
                    && objectMap.containsKey("vcfile")
                    && objectMap.containsKey("vcmodule")
                    && objectMap.containsKey(CommonDocument.SELECT_ACTIVE_FILE_LOCKED)
                    && objectMap.containsKey(CommonDocument.SELECT_ACTIVE_FILE_LOCKER)
                    && objectMap.containsKey(CommonDocument.SELECT_HAS_TOCONNECT_ACCESS)
                    && objectMap.containsKey(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID)
                    && objectMap.containsKey(CommonDocument.SELECT_OWNER)
                    && objectMap.containsKey(CommonDocument.SELECT_LOCKED)
                    && objectMap.containsKey(CommonDocument.SELECT_LOCKER)) {

                objectListItr = objectList.iterator();
            } else {
                StringList selectTypeStmts = new StringList(1);
                selectTypeStmts.add(DomainConstants.SELECT_ID);
                selectTypeStmts.add(DomainConstants.SELECT_TYPE);
                selectTypeStmts.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
                selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
                selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
                selectTypeStmts.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
                selectTypeStmts.add(CommonDocument.SELECT_FILE_NAME);
                selectTypeStmts.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
                selectTypeStmts.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
                selectTypeStmts.add("vcfile");
                selectTypeStmts.add("vcmodule");
                selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
                selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
                selectTypeStmts.add(CommonDocument.SELECT_HAS_TOCONNECT_ACCESS);
                selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
                selectTypeStmts.add(CommonDocument.SELECT_OWNER);
                selectTypeStmts.add(CommonDocument.SELECT_LOCKED);
                selectTypeStmts.add(CommonDocument.SELECT_LOCKER);
                selectTypeStmts.add(CommonDocument.SELECT_CURRENT);

                //Getting all the content ids
                String oidsArray[] = new String[objectList.size()];
                for (int i = 0; i < objectList.size(); i++) {
                    try {
                        oidsArray[i] = (String) ((HashMap) objectList.get(i)).get("id");
                    } catch (Exception ex) {
                        oidsArray[i] = (String) ((Hashtable) objectList.get(i)).get("id");
                    }
                }

                MapList objList = DomainObject.getInfo(context, oidsArray, selectTypeStmts);

                objectListItr = objList.iterator();
            }


            HashMap versionMap = new HashMap();
            String linkAttrName = PropertyUtil.getSchemaProperty(context, "attribute_MxCCIsObjectLinked");
            while (objectListItr.hasNext()) {
                // Start MSF
                msfFileFormatDetails = "MSFFileFormatDetails:[";
                // End MSF
                Map contentObjectMap = (Map) objectListItr.next();
                int fileCount = 0;
                String vcInterface = "";
                boolean vcDocument = false;
                boolean vcFile = false;
                String docType = "";
                StringBuffer strBuf = new StringBuffer(1256);
                boolean moveFilesToVersion = (Boolean.valueOf((String) contentObjectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION))).booleanValue();
                String documentId = (String) contentObjectMap.get(DomainConstants.SELECT_ID);


                Map lockCheckinStatusMap = CommonDocument.getLockAndCheckinIconStatus(context, contentObjectMap);
                boolean isAnyFileLockedByContext = (boolean) lockCheckinStatusMap.get("isAnyFileLockedByContext");


                //For getting the count of files
                HashMap filemap = new HashMap();
                filemap.put(CommonDocument.SELECT_MOVE_FILES_TO_VERSION, contentObjectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION));
                // Start MSF
                StringList activeVersionFileList = (StringList) contentObjectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
                if (null == activeVersionFileList) {
                    activeVersionFileList = new StringList();
                }
                filemap.put(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION, activeVersionFileList);
                // End MSF
                filemap.put(CommonDocument.SELECT_FILE_NAME, contentObjectMap.get(CommonDocument.SELECT_FILE_NAME));
                // Start MSF
                StringList activeVersionIDList = (StringList) contentObjectMap.get(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
                if (null == activeVersionIDList) {
                    activeVersionIDList = new StringList();
                }
                // End MSF
                fileCount = CommonDocument.getFileCount(context, filemap);

                vcInterface = (String) contentObjectMap.get(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
                vcDocument = "TRUE".equalsIgnoreCase(vcInterface) ? true : false;

                docType = (String) contentObjectMap.get(DomainConstants.SELECT_TYPE);

                if (!versionMap.containsKey(docType)) {
                    versionMap.put(docType, CommonDocument.checkVersionableType(context, docType));
                }

                String parentType = CommonDocument.getParentType(context, docType);
                if (CommonDocument.TYPE_DOCUMENTS.equals(parentType)) {
                    // show subscription link
                    if (FrameworkLicenseUtil.isCPFUser(context)) {
                        if (!isprinterFriendly) {
                            strBuf.append("<a href=\"javascript:showModalDialog('../components/emxSubscriptionDialog.jsp?objectId=");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                            strBuf.append("', '730', '450')\"><img border='0' src='../common/images/iconSmallSubscription.gif' alt=\"" + sTipSubscriptions + "\" title=\"" + sTipSubscriptions + "\"></img></a>&#160;");
                        } else {
                            strBuf.append("<img border='0' src='../common/images/iconSmallSubscription.gif' alt=\"" + sTipSubscriptions + "\" title=\"" + sTipSubscriptions + "\"></img>&#160;");
                        }
                    }
                    // add by chenyan 在文档对象的操作列添加预览按钮
                    //获取预览的HTML
                    String strShowAction = JF_DocumentService_mxJPO.getWidgit3DPlayUrl(context, new String[]{documentId});
                    if (UIUtil.isNotNullAndNotEmpty(strShowAction)){
                        strBuf.append(strShowAction);
                    }
                    //Can Download
                    if (CommonDocument.canDownload(context, contentObjectMap)) {
                        if (!isprinterFriendly) {
                            strBuf.append("<a href='javascript:callCheckout(\"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                            strBuf.append("\",\"download\", \"\", \"\",\"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, table));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, parentOID));
                            strBuf.append("\"");
                            strBuf.append(")'>");
                            strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
                            strBuf.append(sTipDownload);
                            strBuf.append("\" title=\"");
                            strBuf.append(sTipDownload);
                            strBuf.append("\"></img></a>&#160;");
                        } else {
                            strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
                            strBuf.append(sTipDownload);
                            strBuf.append("\"></img>&#160;");
                        }
                        // Changes for CLC start here..
                        //Show Download Icon for ClearCase Linked Objects
                        //DomainObject ccLinkedObject  = DomainObject.newInstance(context, documentId);

                        String isObjLinked = null;
                        if (linkAttrName != null && !linkAttrName.equals("")) {
                            DomainObject docObject = DomainObject.newInstance(context, documentId);
                            isObjLinked = docObject.getAttributeValue(context, linkAttrName);
                        }

                        if (isObjLinked != null && !isObjLinked.equals("")) {
                            if (isObjLinked.equalsIgnoreCase("True")) {
                                //show download icon for Linked Objects
                                strBuf.append("<a href='../servlet/MxCCCS/MxCCCommandsServlet.java?commandName=downloadallfiles&amp;objectId=");
                                strBuf.append(XSSUtil.encodeForURL(context, documentId));
                                strBuf.append("'>");
                                strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
                                strBuf.append(sTipDownload);
                                strBuf.append("\" title=\"");
                                strBuf.append(sTipDownload);
                                strBuf.append("\"></img></a>&#160;");
                            }
                        }
                    }
                    // Can Checkout
                    if (CommonDocument.canCheckout(context, contentObjectMap, false, ((Boolean) versionMap.get(docType)).booleanValue())) {
                        if (!isprinterFriendly) {

                            strBuf.append("<a href='javascript:callCheckout(\"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                            strBuf.append("\",\"checkout\", \"\", \"\",\"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, table));
                            strBuf.append("\", \"");
                            strBuf.append(parentOID);
                            strBuf.append("\"");
                            strBuf.append(")'>");
                            strBuf.append("<img border='0' src='../common/images/iconActionCheckOut.gif' alt=\"");
                            strBuf.append(sTipCheckout);
                            strBuf.append("\" title=\"");
                            strBuf.append(sTipCheckout);
                            strBuf.append("\"></img></a>&#160;");
                        } else {
                            strBuf.append("<img border='0' src='../common/images/iconActionCheckOut.gif' alt=\"");
                            strBuf.append(sTipCheckout);
                            strBuf.append("\"></img>&#160;");
                        }
                    } else {
                        strBuf.append("&#160;");
                    }
                    // Can Checkin
                    if ((CommonDocument.canCheckin(context, contentObjectMap) || VCDocument.canVCCheckin(context, contentObjectMap))) {
                        // Start MSF
                        isAdded = false;
                        for (int ii = 0; ii < activeVersionFileList.size(); ii++) {
                            if (!isAdded) {
                                isAdded = true;
                                msfFileFormatDetails += "{FileName: '" + XSSUtil.encodeForJavaScript(context, (String) activeVersionFileList.get(ii)) +
                                        "', VersionId: '" + XSSUtil.encodeForJavaScript(context, (String) activeVersionIDList.get(ii)) + "'}";
                            } else {
                                msfFileFormatDetails += ", {FileName: '" + XSSUtil.encodeForJavaScript(context, (String) activeVersionFileList.get(ii)) +
                                        "', VersionId: '" + XSSUtil.encodeForJavaScript(context, (String) activeVersionIDList.get(ii)) + "'}";
                            }
                        }
                        msfFileFormatDetails += "]";
                        // End MSF

                        // MSF
                        msfRequestData = "{RequestType: 'CheckIn', DocumentID: '" + documentId + "', " + msfFileFormatDetails + "}";
                        // MSF
                        vcFile = (Boolean.valueOf((String) contentObjectMap.get("vcfile"))).booleanValue();
                        if (!isprinterFriendly) {
                            if (isAnyFileLockedByContext) {
                                if (!vcDocument) {
                                    strBuf.append("<a href=\"javascript:processModalDialog(" + msfRequestData + "," + "'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                                    strBuf.append("&amp;customSortColumns="); //Added for Bug #371651 starts
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                                    strBuf.append("&amp;customSortDirections=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                                    strBuf.append("&amp;uiType=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                                    strBuf.append("&amp;table=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, table));                 //Added for Bug #371651 ends
                                    strBuf.append("&amp;showFormat=true&amp;showComments=required&amp;objectAction=update&amp;JPOName=emxTeamDocumentBase&amp;appDir=teamcentral&amp;appProcessPage=emxTeamPostCheckinProcess.jsp&amp;refreshTableContent=true&amp;updateTreeLabel=false',730,450);\">");
                                    strBuf.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\"");
                                    strBuf.append(sTipCheckin);
                                    strBuf.append("\" title=\"");
                                    strBuf.append(sTipCheckin);
                                    strBuf.append("\"></img></a>&#160;");
                                } else {
                                    if (vcFile) {
                                        strBuf.append("<a href=\"javascript:processModalDialog(" + msfRequestData + "," + "'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
                                        strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                                        strBuf.append("&amp;customSortColumns=");     //Added for Bug #371651 starts
                                        strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                                        strBuf.append("&amp;customSortDirections=");
                                        strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                                        strBuf.append("&amp;uiType=");
                                        strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                                        strBuf.append("&amp;table=");
                                        strBuf.append(XSSUtil.encodeForJavaScript(context, table));                 //Added for Bug #371651 ends
                                        strBuf.append("&amp;showFormat=false&amp;showComments=required&amp;objectAction=checkinVCFile&amp;allowFileNameChange=false&amp;noOfFiles=1&amp;JPOName=emxVCDocument&amp;methodName=checkinUpdate&amp;refreshTableContent=true', '730', '450');\">");
                                        strBuf.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\"");
                                        strBuf.append(sTipCheckin);
                                        strBuf.append("\" title=\"");
                                        strBuf.append(sTipCheckin);
                                        strBuf.append("\"></img></a>&#160;");
                                    } else {
                                        strBuf.append("<a href=\"javascript:processModalDialog(" + msfRequestData + "," + "'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
                                        strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                                        strBuf.append("&amp;customSortColumns=");         //Added for Bug #371651 starts
                                        strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                                        strBuf.append("&amp;customSortDirections=");
                                        strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                                        strBuf.append("&amp;uiType=");
                                        strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                                        strBuf.append("&amp;table=");
                                        strBuf.append(XSSUtil.encodeForJavaScript(context, table));                 //Added for Bug #371651 ends
                                        strBuf.append("&amp;override=false&amp;showFormat=false&amp;showComments=required&amp;objectAction=checkinVCFile&amp;allowFileNameChange=true&amp;noOfFiles=1&amp;JPOName=emxVCDocument&amp;methodName=checkinUpdate&amp;refreshTableContent=true', '730', '450');\">");
                                        strBuf.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\"");
                                        strBuf.append(sTipCheckin);
                                        strBuf.append("\" title=\"");
                                        strBuf.append(sTipCheckin);
                                        strBuf.append("\"></img></a>&#160;");
                                    }
                                }
                            }
                        } else {
                            if (isAnyFileLockedByContext) {
                                strBuf.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\"");
                                strBuf.append(sTipCheckin);
                                strBuf.append("\" title=\"");
                                strBuf.append(sTipCheckin);
                                strBuf.append("\"></img>&#160;");
                            }
                        }
                    }
                    // Can Add Files
                    if (CommonDocument.canAddFiles(context, contentObjectMap)) {
                        // MSF
                        msfRequestData = "{RequestType: 'AddFiles', DocumentID: '" + documentId + "'}";
                        // MSF
                        if (!isprinterFriendly) {
                            strBuf.append("<a href=\"javascript:processModalDialog(" + msfRequestData + "," + "'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                            strBuf.append("&amp;customSortColumns=");       //Added for Bug #371651 starts
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                            strBuf.append("&amp;customSortDirections=");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                            strBuf.append("&amp;uiType=");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                            strBuf.append("&amp;table=");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, table));                   //Added for Bug #371651 ends
                            strBuf.append("&amp;showFormat=true&amp;showDescription=required&amp;objectAction=checkin&amp;showTitle=true&amp;JPOName=emxTeamDocumentBase&amp;appDir=teamcentral&amp;appProcessPage=emxTeamPostCheckinProcess.jsp&amp;refreshTableContent=true&amp;updateTreeLabel=false', '730', '450')\">");
                            strBuf.append("<img border='0' src='../common/images/iconActionAppend.gif' alt=\"");
                            strBuf.append(sTipUpdate);
                            strBuf.append("\" title =\"");
                            strBuf.append(sTipUpdate);
                            strBuf.append("\"></img></a>&#160;");
                        } else {
                            strBuf.append("<img border='0' src='../common/images/iconActionAppend.gif' alt=\"");
                            strBuf.append(sTipUpdate);
                            strBuf.append("\" title=\"");
                            strBuf.append(sTipUpdate);
                            strBuf.append("\"></img>&#160;");
                        }
                    }
                    if (strBuf.length() == 0)
                        strBuf.append("&#160;");
                } else {
                    strBuf.append("&#160;");
                }
                vActions.add(strBuf.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            return vActions;
        }
    }
}
