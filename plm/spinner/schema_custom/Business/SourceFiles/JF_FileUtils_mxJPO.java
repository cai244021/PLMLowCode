/*
 * Copyright (c) 1992-2020 Dassault Systemes.
 * All Rights Reserved.
 */

import com.google.gson.Gson;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.CommonImageConverterRemoteExec;
import com.matrixone.apps.common.util.DocumentUtil;
import com.matrixone.apps.common.util.ImageManagerUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class JF_FileUtils_mxJPO {
	//日志
	private static final Logger _logger =  LoggerFactory.getLogger("JF_FileUtils");
			public static String SELECT_FILE_NAME = "format.file.name";
			public static String SELECT_FILE_FORMAT = "format.file.format";
	public JF_FileUtils_mxJPO(Context context, String[] args) throws Exception {}

	// Image Column providing check in capabilities by Drag&Drop
	public Vector columnImage(Context context, String[] args) throws Exception {

		Vector vResult      = new Vector();
		Map paramMap        = (Map) JPO.unpackArgs(args);
		Map paramList       = (Map)paramMap.get("paramList");
		MapList mlObjects   = (MapList) paramMap.get("objectList");
		String sMCSURL      = emxUtil_mxJPO.getMCSURL(context, args);
		String sLang        = (String)paramList.get("languageStr");

		String sLabel        = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.DropImagesHere", sLang);
		String sLabelNoImage = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.NoImage", sLang);
		boolean useInPlaceManager = false;
		try{
			useInPlaceManager = "true".equalsIgnoreCase(EnoviaResourceBundle.getProperty(context, "emxFramework.InPlaceImageManager"));
		}catch(Exception e){
		}
		boolean excluded = false;
		StringList contentTypesList = new StringList();
		try{
			String excludedTypes = EnoviaResourceBundle.getProperty(context, "emxFramework.InPlaceImageManager.TypeExclusionList");
			contentTypesList = FrameworkUtil.split(excludedTypes, ",");
		}catch(Exception e){
		}
		if (mlObjects.size() > 0) {

			for (int i = 0; i < mlObjects.size(); i++) {

				StringBuilder sbResult  = new StringBuilder();
				Map mObject             = (Map) mlObjects.get(i);
				String sOID             = (String) mObject.get("id");
				String objectType = (String)mObject.get("type");
				String symbolicType = FrameworkUtil.getAliasForAdmin(context, "type", objectType, true);
				if(contentTypesList.indexOf(symbolicType) > -1)
				{
					excluded = true;
				}
				else{
					excluded = false;
				}
				DomainObject dObject    = new DomainObject(sOID);
				String sURLImage        = emxUtil_mxJPO.getPrimaryImageURL(context, args, sOID, "mxThumbnail Image", sMCSURL, "NoImageFound");
				Access access           = dObject.getAccessMask(context);
				Boolean bAccess         = access.hasFromConnectAccess();

				String sLevel           = (String) mObject.get("id[level]");
				String sFormId          = "formDrag"        + i + sLevel;
				String sDivId           = "divDrop"         + i + sLevel;
				String sDivIdImage      = "divDropImge"     + i + sLevel;

				if(sURLImage.equals("NoImageFound")) {
					if(bAccess && !excluded) {
						sbResult.append("<form class='dropAreaInColumn' id='").append(sFormId).append("' action=\"../common/emxExtendedPageHeaderFileUploadImage.jsp?objectId=").append(sOID).append("\"  method='post'  enctype='multipart/form-data'>");
						sbResult.append("<div id='").append(sDivId).append("' class='dropArea'");
						if(useInPlaceManager){
							sbResult.append(" onClick=\"launchImageManager('" + sOID + "');\"");
						}
						sbResult.append("       ondrop=\"ImageDropColumn(event, '").append(sFormId).append("', '").append(sDivId).append("', '").append(sLevel).append("')\" ");
						sbResult.append("   ondragover=\"ImageDragHover(event, '").append(sDivId).append("')\" ");
						sbResult.append("  ondragleave=\"ImageDragHover(event, '").append(sDivId).append("')\">");
						sbResult.append(sLabel);
						sbResult.append("   </div>");
						sbResult.append("</form>");
					} else {
						sbResult.append("<div class='dropArea' style='border:none;white-space:normal;'>");
						sbResult.append(sLabelNoImage);
						sbResult.append("</div>");
					}
				} else {
					if(bAccess && !excluded) {
						sbResult.append("<form class='dropAreaInColumn' id='").append(sFormId).append("' action=\"../common/emxExtendedPageHeaderFileUploadImage.jsp?objectId=").append(sOID).append("\"  method='post'  enctype='multipart/form-data'>");
						sbResult.append("<div id='").append(sDivId).append("' class='dropAreaWithImage' ");
						sbResult.append("        ondrop=\"ImageDropOnImageColumn(event, '").append(sFormId).append("', '").append(sDivId).append("', '").append(sDivIdImage).append("', '").append(sLevel).append("')\" ");
						sbResult.append("    ondragover=\"ImageDragHoverWithImage(event, '").append(sDivId).append("', '").append(sDivIdImage).append("')\" ");
						sbResult.append("   ondragleave=\"ImageDragHoverWithImage(event, '").append(sDivId).append("', '").append(sDivIdImage).append("')\">");
						sbResult.append("<img class='dropAreaImage' id='").append(sDivIdImage).append("' src='").append(sURLImage).append("' ");
						if(useInPlaceManager){
							sbResult.append(" onClick=\"launchImageManager('" + sOID + "');\"");
						}else{
							sbResult.append(" onClick=\"var posLeft=(screen.width/2)-(900/2);var posTop = (screen.height/2)-(650/2);");
							sbResult.append("window.open('../components/emxImageManager.jsp?isPopup=false&amp;toolbar=APPImageManagerToolBar&amp;header=emxComponents.Image.ImageManagerHeading&amp;HelpMarker=emxhelpimagesview&amp;");
							sbResult.append("objectId=").append(sOID).append("', '', 'height=650,width=850,top=' + posTop + ',left=' + posLeft + ',toolbar=no,directories=no,status=no,menubar=no;return false;')\"");
						}
						sbResult.append(" />");
						sbResult.append("   </div>");
						sbResult.append("</form>");
					} else {
						sbResult.append("<img class='dropAreaImage' id='").append(sDivIdImage).append("' src='").append(sURLImage).append("' ");
						if(!excluded){
							if(useInPlaceManager){
								sbResult.append(" onClick=\"launchImageManager('" + sOID + "');\"");
							}else{
								sbResult.append(" onClick=\"var posLeft=(screen.width/2)-(850/2);var posTop = (screen.height/2)-(650/2);");
								sbResult.append("window.open('../components/emxImageManager.jsp?isPopup=false&amp;toolbar=APPImageManagerToolBar&amp;header=emxComponents.Image.ImageManagerHeading&amp;HelpMarker=emxhelpimagesview&amp;");
								sbResult.append("objectId=").append(sOID).append("', '', 'height=650,width=850,top=' + posTop + ',left=' + posLeft + ',toolbar=no,directories=no,status=no,menubar=no;return false;')\"");
							}
						}
						sbResult.append(" />");
					}
				}
				vResult.add(sbResult.toString());
			}
		}

		return vResult;

	}
	@com.matrixone.apps.framework.ui.ProgramCallable
	public String checkInImage(Context context, String[] args) throws Exception {

		try {
			Map paramMap = (Map) JPO.unpackArgs(args);
			String sLanguage = (String) paramMap.get("language");
			String sOIDParent = (String) paramMap.get("objectId");
			List files = (List) paramMap.get("files");
			String sFolder = (String) paramMap.get("folder");
			String sMCSURL = (String) paramMap.get("MCSURL");
			String imageRelType = (String) paramMap.get("relationship");

			DomainObject imageHolder = new DomainObject();
			DomainObject doParent = new DomainObject(sOIDParent);
			String sOIDImageHolder = doParent.getInfo(context, "to["+ PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_relationship_ImageHolder) +"].from.id");

			Iterator iter = files.iterator();
			int index;
			String sFilename="";
			FileItem file = null;
			File outfile = null;
			ContextUtil.startTransaction(context, true);
			while (iter.hasNext())
			{
				file = (FileItem) iter.next();
				sFilename 		= file.getName();
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
				if(DomainConstants.RELATIONSHIP_IMAGES.equals(imageRelType)){
					String sName = DomainObject.getAutoGeneratedName(context, "type_Image", "");
					imageHolder.createObject(context, DomainConstants.TYPE_IMAGE, sName, "-", DomainConstants.POLICY_IMAGE, context.getVault().getName());
					imageHolder.addRelatedObject(context, new RelationshipType(DomainConstants.RELATIONSHIP_IMAGES), true, sOIDParent);
					sOIDImageHolder = imageHolder.getInfo(context, DomainConstants.SELECT_ID);
					String sHasPrimaryImage = doParent.getInfo(context, "from["+ DomainConstants.RELATIONSHIP_PRIMARY_IMAGE + "]");

					if(sHasPrimaryImage.equalsIgnoreCase("FALSE")) {
						imageHolder.addRelatedObject(context, new RelationshipType(DomainConstants.RELATIONSHIP_PRIMARY_IMAGE), true, sOIDParent);
					}
				} else {
					String typeImageHolder = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_ImageHolder);
					String relImageHolder = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_relationship_ImageHolder);
					String attrPrimayImage = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_attribute_PrimaryImage);

					if(sOIDImageHolder == null) {
						imageHolder = DomainObject.newInstance(context, typeImageHolder);
						imageHolder.createAndConnect(context, typeImageHolder, relImageHolder, doParent, false);
						imageHolder.setAttributeValue(context, attrPrimayImage, ImageManagerUtil.getPrimaryImageFileNameForImageManager(context, sFilename));
						sOIDImageHolder = imageHolder.getInfo(context, DomainConstants.SELECT_ID);
					} else {
						imageHolder = new DomainObject(sOIDImageHolder);
					}
				}

				imageHolder.checkinFile(context, true, true, "", "generic", sFilename, sFolder);

				String gotMcsURL = MqlUtil.mqlCommand(context, "get env global MCSURL");

				List lstFilesToTransform = new ArrayList();
				Hashtable htCCIHInfo = new Hashtable();

				htCCIHInfo.put("Oid"    , sOIDImageHolder   );
				htCCIHInfo.put("File"   , sFilename         );
				htCCIHInfo.put("Format" , "generic"         );

				lstFilesToTransform.add(htCCIHInfo);
				new CommonImageConverterRemoteExec().convertImageAndCheckinSameObject(context, gotMcsURL, lstFilesToTransform, null, ".jpg");
				outfile.delete();
			}

			ContextUtil.commitTransaction(context);
			return emxExtendedHeader_mxJPO.genHeaderImage(context, args, sOIDParent, sLanguage, sMCSURL,imageRelType, false , "true");
		}catch (Exception ex) {
			ContextUtil.abortTransaction(context);
			ex.printStackTrace();
			return "ERROR"+ex.getMessage();
		}
	}


	// Drop column
	public String checkinFile(Context context, String[] args) throws Exception {
		// add by chenyan 修改为返回文档id 和title href
		Gson gson = new Gson();
		//文档对象信息集合
		MapList fileList = new MapList();
		try{
			Map paramMap = (Map) JPO.unpackArgs(args);
			String sLanguage = (String) paramMap.get("language");
			String timeZone = (String) paramMap.get("timezone");
			String sOID = (String) paramMap.get("objectId");
			//标识文档的上传
			String strFileType = (String) paramMap.get("type");
			String strHostUrl  = (String) paramMap.get("hostUrl");
			String sRelType = (String) paramMap.get("relationship");
			String sFolder  = (String) paramMap.get("folder");
			List files = (List) paramMap.get("files");
			HashMap uploadParamsMap=new HashMap();
			String objectAction = (String) paramMap.get("objectAction");
			uploadParamsMap.put("objectAction", objectAction);
			uploadParamsMap.put("parentId", sOID);
			StringList ids=new StringList();
			String documentCommand  = (String) paramMap.get("documentCommand");
			DomainObject dObject    = new DomainObject(sOID);
			String strType     = PropertyUtil.getSchemaProperty(context,DomainObject.SYMBOLIC_type_DOCUMENTS);
			RelationshipType relTypes = new RelationshipType(sRelType);
			BusinessTypeList allowedFromTypes = relTypes.getFromTypes(context);
			//FD 2024 版本此API已经弃用
//			BusinessTypeList allowedFromTypes = relTypes.getFromTypes(context, true);
			StringList selectableList	=	new StringList();
			selectableList.add("type.kindof["+strType+"]");
			selectableList.add(DomainObject.SELECT_TYPE);
			Map objectInfo			=	dObject.getInfo(context,selectableList);
			String sIsDocument      = (String)objectInfo.get("type.kindof["+strType+"]");
			String typeName      = (String)objectInfo.get(DomainObject.SELECT_TYPE);
			BusinessType bType		= allowedFromTypes.find(typeName);
			if(bType == null){
				Locale locale = context.getLocale();
				String type1 = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",locale,"emxFramework.Type.Document");
				String key = "emxFramework.Type." + dObject.getTypeName().replace(' ', '_');
				String type2 = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",locale,key);
				String[] messageValues = new String[]{type1,type2};
				String msg = MessageUtil.getMessage(context, null, "emxComponents.DragAndDrop.WarningMessage",messageValues, null, locale, "emxComponentsStringResource");
				return "ERROR"+msg;
			}
			if(UIUtil.isNullOrEmpty(sRelType)){
				sRelType = "Reference Document";
			}
			if( UIUtil.isNullOrEmpty(documentCommand)){
				documentCommand = "APPReferenceDocumentsTreeCategory";
			}
			Iterator iter = files.iterator();
			int index;
			String sFilename="";
			FileItem file = null;
			File outfile = null;
			StringBuffer errorMessage=new StringBuffer();
			String errorMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.CommonDocument.DocumentAlreadyExist");
			String dropFilesHere = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",context.getLocale(),"emxFramework.String.DropFilesHere");
			String checkInError=EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.CommonDocument.DocumentsAreNotLockedByUser");
			errorMessage.append(errorMsg);
			boolean fileCheckedOut=isFileCheckedOut(context, sOID, files);
			if(isDuplicateFile(context, sOID, files, errorMessage)){
				if(!fileCheckedOut)
					return "ERROR"+errorMessage+"@"+dropFilesHere;
			}
			ContextUtil.startTransaction(context, true);
			StringList lockedFiles = new StringList();
			while (iter.hasNext())
			{
				file = (FileItem) iter.next();
				sFilename 		= file.getName();
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

				if(sRelType.equals("Active Version") && sIsDocument.equalsIgnoreCase("TRUE")) {
					if(fileCheckedOut){

						CommonDocument  cDoc = new CommonDocument(sOID);
						StringList selects = new StringList(12);
						selects.add(SELECT_FILE_NAME);
						selects.add(SELECT_FILE_FORMAT);
						StringList selectsList = new StringList();
						selectsList.add("attribute[Title]");
						selectsList.add(DomainConstants.SELECT_LOCKER);
						MapList mlist = cDoc.getRelatedObjects(context,                       // context.
								"Active Version",  // rel filter.
								"Document",               // type filter.
								selectsList,                   // business selectables.
								null,                   // relationship selectables.
								false,                        // expand to direction.
								true,                         // expand from direction.
								(short) 1,                    // level
								"",                  // object where clause
								"",
								0);
					Iterator itr = mlist.iterator();
					while(itr.hasNext()){
						Map m = (Map)itr.next();
						String title = (String) m.get("attribute[Title]");
						String locker=(String)m.get("locker");
						//If user is try to upload file,which is locked by other user
						if(sFilename.equals(title)&&!context.getUser().equals(locker)){
							lockedFiles.add(title);
						}
					}
					if(!lockedFiles.contains(sFilename)){
						cDoc.lock(context);
						cDoc.reviseVersion(context, null,sFilename, null);
						//cDoc.checkinFile(context, true, true, "generic", null, sFilename, sFolder); // commented by L48
						String storeFromBL = null;
						storeFromBL = DocumentUtil.getStoreFromBL(context, "Document");
						cDoc.checkinFile(context, true, true, "", "generic", storeFromBL, sFilename, sFolder);
					}

					}else{
						CommonDocument cDoc = new CommonDocument(sOID);
						//cDoc.checkinFile(context, true, true, "", "generic", sFilename, sFolder);
						String storeFromBL = null;
						storeFromBL = DocumentUtil.getStoreFromBL(context, "Document");
						cDoc.createVersion(context, sFilename, sFilename, null);
						cDoc.checkinFile(context, true, true, "", "generic", storeFromBL, sFilename, sFolder);
					}

				} else {
					String sObjGeneratorName = UICache.getObjectGenerator(context, "type_Document", "");
					String sName = DomainObject.getAutoGeneratedName(context, sObjGeneratorName, "");
					String docPolicy = PropertyUtil.getSchemaProperty(EnoviaResourceBundle.getProperty(context, "emxFrameowrk.FileUpload.Default.Policy"));
					if (UIUtil.isNotNullAndNotEmpty(docPolicy)) {
						CommonDocument cDoc = new CommonDocument();
						Policy policy = new Policy(docPolicy);
						String revision = policy.getFirstInSequence(context);
						PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", "true", false);
						cDoc.createObject(context, DomainObject.TYPE_DOCUMENT, sName, revision, docPolicy, context.getVault().getName());
						DomainRelationship rel = cDoc.addRelatedObject(context, new RelationshipType(sRelType), true, sOID);
						rel.setAttributeValue(context,"Project Role",strFileType);
						String strRelPhysicalId = rel.getPhysicalId(context);
						cDoc.setAttributeValue(context, "Title", sFilename);
						//cDoc.checkinFile(context, true, true, "", "generic", sFilename, sFolder);
						String storeFromBL = null;
						storeFromBL = DocumentUtil.getStoreFromBL(context, "Document");
						cDoc.createVersion(context, sFilename, sFilename, null);
						cDoc.checkinFile(context, true, true, "", "generic", storeFromBL, sFilename, sFolder);


						ids.add(cDoc.getId(context));
						// add by chenyan 设置文档信息map
						Map docMapInfo = new HashMap<>();
						//跳转url
						StringBuffer sbHref = new StringBuffer();
						sbHref.append(strHostUrl);
						sbHref.append("/3dspace/common/emxTree.jsp?mode=insert&relId=null&parentOID=null&jsTreeID=null&objectId=");
						sbHref.append(cDoc.getId(context));
						sbHref.append("&suiteKey=Components&emxSuiteDirectory=components");
						//文档对象id
						docMapInfo.put("objectId",cDoc.getId(context));
						//关系id
						docMapInfo.put("relId",strRelPhysicalId);
						docMapInfo.put("href",sbHref.toString());
						String strEscape = StringEscapeUtils.escapeHtml4(sFilename);
//						strEscape = Idm_PPAPService_mxJPO.unEscape(strEscape);
						docMapInfo.put("title", strEscape);
						docMapInfo.put("delAccess","true");
						docMapInfo.put("flag",false);
						fileList.add(docMapInfo);
					}
				}

				outfile.delete();
			}
			HashMap params = new HashMap();
			params.put("uploadParamsMap",uploadParamsMap);
			params.put("objectIds",ids);
			String initargs[] = {};
			JPO.invoke(context, "emxTeamDocumentBase", initargs, "postCheckinDND", JPO.packArgs (params));
			ContextUtil.commitTransaction(context);
				//When User try to drag & drop file,locked by another user
			checkInError=checkInError+"\n"+lockedFiles;
			if(lockedFiles.size()!=0){
				return "ERROR"+checkInError+"*"+dropFilesHere;
			}
			emxExtendedHeader_mxJPO.genHeaderDocuments(context, sOID,sRelType, documentCommand, sLanguage, false,timeZone);
			return gson.toJson(fileList);
		}catch (Exception ex) {
			ContextUtil.abortTransaction(context);
			ex.printStackTrace();
			return "ERROR"+ex.getMessage();
		} finally {
			PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", "false", false);
		}
	}
	private boolean isDuplicateFile(Context context,String objectID,List checkinFiles,StringBuffer error) throws Exception{
		DomainObject domainObject = DomainObject.newInstance(context, objectID);
		if(!(domainObject instanceof CommonDocument)){
			return false;
		}
		StringList files = getFiles(context, domainObject);
		return isFilePresent(context, files, checkinFiles,error);
	}
	private StringList getFiles(Context context,DomainObject domainObject) throws Exception{
		CommonDocument object = (CommonDocument)domainObject;
		StringList selectList = new StringList();
		selectList.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
		selectList.add(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
		Map selectMap = object.getInfo(context, selectList);
		StringList selectFileList = new StringList();
		selectFileList.add(CommonDocument.SELECT_TITLE);
		selectFileList.add(CommonDocument.SELECT_LOCKER);
		return (StringList) selectMap.get(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
	}
	private boolean isFilePresent(Context context,StringList files,List checkinFiles,StringBuffer error) throws Exception{
		boolean isFileFound = false;
		if(files!=null && !files.isEmpty()){
			Iterator fileItr = files.iterator();
			while(fileItr.hasNext()){
				Iterator revItr = getRevisionFiles(context,(String)fileItr.next()).iterator();
				while(revItr.hasNext()){
					Map fileMap = (Map)revItr.next();
					if(fileFound(context,fileMap,checkinFiles,error)){
						isFileFound=true;
					}
				}
			}
		}
		return isFileFound;
	}
	private MapList getRevisionFiles(Context context,String fileId) throws Exception{
		DomainObject versionObj = DomainObject.newInstance(context, fileId);
		StringList selectFileList = new StringList();
		selectFileList.add(CommonDocument.SELECT_TITLE);
		selectFileList.add(CommonDocument.SELECT_LOCKER);
		selectFileList.add(CommonDocument.SELECT_LOCKED);

		return versionObj.getRevisionsInfo(context,selectFileList,new StringList());

	}
	private boolean fileFound(Context context,Map fileMap,List checkinFiles,StringBuffer error){
		String fileTitle = (String)fileMap.get(CommonDocument.SELECT_TITLE);
		Iterator chekingFileItr = checkinFiles.iterator();
		boolean isFileFound = false;
		while(chekingFileItr.hasNext()){
			FileItem file = (FileItem) chekingFileItr.next();
			String sFilename 		= file.getName();
			String[] sFilenamePath=sFilename.split("\\\\");
			sFilename = sFilenamePath[sFilenamePath.length-1].trim();
			if(sFilename.equalsIgnoreCase(fileTitle)){
					if((error.indexOf(sFilename)<0))
							error.append(" \n" + sFilename);
				isFileFound = true;
				break;
			}
		}
		return isFileFound;

	}

	private boolean	 isFileCheckedOut(Context context,String objectID,List checkinFiles) throws Exception{
		DomainObject domainObject = DomainObject.newInstance(context, objectID);
		if(!(domainObject instanceof CommonDocument)){
			return false;
		}
		StringList files = getFiles(context, domainObject);
		return isFilePresentandCheckedOut(context, files, checkinFiles);
	}

	private boolean isFilePresentandCheckedOut(Context context,StringList files,List checkinFiles) throws Exception{
		boolean fileCheckedOut = false;
		if(files!=null && !files.isEmpty()){
			Iterator fileItr = files.iterator();
			while(fileItr.hasNext()){
				Iterator revItr = getRevisionFiles(context,(String)fileItr.next()).iterator();
				while(revItr.hasNext()){
					Map fileMap = (Map)revItr.next();
					fileCheckedOut=fileFoundandCheckedout(context,fileMap,checkinFiles);
					if(fileCheckedOut)
						break;
				}
				if(fileCheckedOut)
						break;
			}
		}
		return fileCheckedOut;
	}

	private boolean fileFoundandCheckedout(Context context,Map fileMap,List checkinFiles){
		String fileTitle = (String)fileMap.get(CommonDocument.SELECT_TITLE);
		String fileLocked = (String)fileMap.get(CommonDocument.SELECT_LOCKED);
		boolean fileCheckedOut = false;

		Iterator chekingFileItr = checkinFiles.iterator();
		while(chekingFileItr.hasNext()){
			FileItem file = (FileItem) chekingFileItr.next();
			String sFilename 		= file.getName();
			String[] sFilenamePath=sFilename.split("\\\\");
			sFilename = sFilenamePath[sFilenamePath.length-1].trim();
			if(sFilename.equalsIgnoreCase(fileTitle)){
				if(fileLocked.equalsIgnoreCase("true"))
					fileCheckedOut=true;
				break;
			}
		}
		return fileCheckedOut;

	}

	/**
	*
	*@description 创建游离的文档对象
	*@param context
	*@param args
	*@return java.util.List<com.matrixone.apps.common.CommonDocument>
	*@throws
	*@author CHENYAN
	*@date 2025/8/20 15:49
	*/

	public static List<CommonDocument> createDocs(Context context, String[] args) throws Exception {
		Map argsMap = JPO.unpackArgs(args);
		List<File> files = (List<File>)argsMap.get("files");
		List<CommonDocument> docList = new ArrayList<>(files.size());
		String sObjGeneratorName = UICache.getObjectGenerator(context, "type_Document", "");
		String sName = DomainObject.getAutoGeneratedName(context, sObjGeneratorName, "");
		String docPolicy = PropertyUtil.getSchemaProperty(EnoviaResourceBundle.getProperty(context, "emxFrameowrk.FileUpload.Default.Policy"));
		for (int i = 0; i < files.size(); i++) {
			File file = files.get(i);
			String strFileName = file.getName();
			String strFilePath = file.getPath();
			if (UIUtil.isNotNullAndNotEmpty(docPolicy)) {
				CommonDocument cDoc = new CommonDocument();
				Policy policy = new Policy(docPolicy);
				String revision = policy.getFirstInSequence(context);
				PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", "true", false);
				cDoc.createObject(context, DomainObject.TYPE_DOCUMENT, sName, revision, docPolicy, context.getVault().getName());
				cDoc.setAttributeValue(context, "Title", strFileName);
				//cDoc.checkinFile(context, true, true, "", "generic", sFilename, sFolder);
				String storeFromBL = null;
				storeFromBL = DocumentUtil.getStoreFromBL(context, "Document");
				cDoc.createVersion(context, strFileName, strFileName, null);
				cDoc.checkinFile(context, true, true, "", "generic", storeFromBL, strFileName, strFilePath);

				docList.add(cDoc);
			}
		}
		return docList ;
	}
	/**
	*
	*@description 创建文档次方法不删除文件，请调用者自己删除
	*@param context
	*@param args
	*@return com.matrixone.apps.common.CommonDocument
	*@throws
	*@author CHENYAN
	*@date 2025/8/20 16:04
	*/
	public static CommonDocument createDoc(Context context, String[] args) throws Exception {
		Map argsMap = JPO.unpackArgs(args);
		File file = (File)argsMap.get("file");
		CommonDocument cDoc = null;
		String sObjGeneratorName = UICache.getObjectGenerator(context, "type_Document", "");
		String sName = DomainObject.getAutoGeneratedName(context, sObjGeneratorName, "");
		String docPolicy = PropertyUtil.getSchemaProperty(EnoviaResourceBundle.getProperty(context, "emxFrameowrk.FileUpload.Default.Policy"));
		String strFileName = file.getName();
		String strFilePath = file.getParent();

		if (UIUtil.isNotNullAndNotEmpty(docPolicy)) {
			cDoc = new CommonDocument();
			Policy policy = new Policy(docPolicy);
			String revision = policy.getFirstInSequence(context);
			PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", "true", false);
			cDoc.createObject(context, DomainObject.TYPE_DOCUMENT, sName, revision, docPolicy, context.getVault().getName());
			cDoc.setAttributeValue(context, "Title", strFileName);
			//cDoc.checkinFile(context, true, true, "", "generic", sFilename, sFolder);
			String storeFromBL = null;
			storeFromBL = DocumentUtil.getStoreFromBL(context, "Document");
			cDoc.createVersion(context, strFileName, strFileName, null);
			cDoc.checkinFile(context, true, true, "", "generic", storeFromBL, strFileName, strFilePath);
			
		}
		return cDoc ;
	}
}
