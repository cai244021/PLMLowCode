import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeAction;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.FileList;
import matrix.db.JPO;
import matrix.util.StringList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class CUS_OnlyOfficeOnlineAction_mxJPO {
    public static String  onlyType_desktop="desktop";
    public static String  onlyType_mobile="mobile";
    public static String  onlyType_embedded="embedded";
    public static String  action_fillForms="fillForms";
    public static String  mode_review="review";
    public static String  mode_blockContent="blockcontent";
    public static String  mode_comment="comment";
    public static String  mode_edit="edit";
    public static String  mode_view="view";
    public static String  mode_embedded="embedded";
    public static Logger _logger= Logger.getLogger("CUS_OnlyOfficeOnlineAction_mxJPO");
    public static SimpleDateFormat yyyy_mm_dd_format=new SimpleDateFormat("yyyy_mm_dd");
    public static  String relationship_Reference_Document="Reference Document";
    public static  String relationship_Change_Analysis="Change Analysis";
    public static Properties config_properties =new Properties();

    public static final String TYPE_VPMREFERENCE = "VPMReference";
    public static final String TYPE_DOCUMENT = "Document";
    public static final String TYPE_DRAWING = "Drawing";
    public static final String TYPE_Aer_ECRDocument = "Aer_ECRDocument";
    public static final String TYPE_Change_Analysis = "Change Analysis";
    public static final String TYPE_Change_Request = "Change Request";

    public CUS_OnlyOfficeOnlineAction_mxJPO(Context context, String[] strings) throws Exception {
//        Properties config_properties1 = this.config_properties;
//        System.out.println("config_properties>>>"+config_properties);
        this.config_properties = (Properties) JPO.invoke(context, "CUS_ConfigConstants", new String[]{}, "getPagePropertiesValue", new String[]{}, Properties.class);
        _logger.info("config_properties toString"+config_properties.toString());

    }





    /*
    * 将系统文件下载到onlyoffice映射的路径下
    */


    public MapList getDocOnLineServicePath(Context context , String[] args){
        String path="";
        MapList mlMaplist=new MapList();

        try{
            String docId=args[0];
            _logger.info("getDocOnLineServicePath >>>docId "+docId);
            DomainObject docObj=DomainObject.newInstance(context,docId);
            String action=args[1];
            if(UIUtil.isNullOrEmpty(action)){
                action=mode_view;
            }
             //action=mode_edit;
            String currentUser=context.getUser();
          boolean canShowEditButton=false;
          String docCurrent=  docObj.getInfo(context,DomainObject.SELECT_CURRENT);
            String docOwner=  docObj.getInfo(context,DomainObject.SELECT_OWNER);
           /* policy[Document Release].state = PRIVATE
            policy[Document Release].state = IN_WORK*/

            if("PRIVATE".equalsIgnoreCase(docCurrent)||"IN_WORK".equalsIgnoreCase(docCurrent)){
               // action=mode_edit;
                if(currentUser.contains("admin_")||currentUser.equalsIgnoreCase(docOwner)){
                    action=mode_edit;
                    canShowEditButton=true;
                }
            }

            //如果关联了正在审核状态的DR、ECR，不可以编辑
            if(action.equalsIgnoreCase(mode_edit)){
                StringList selList = new StringList();
                selList.add("type");
                selList.add("current");
                MapList changeList = docObj.getRelatedObjects(context,
                        "Reference Document", //pattern to match relationships
                        "JFDR,JFNewECR,JFECR", //pattern to match types
                        selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                        new StringList(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                        true, //get To relationships
                        false, //get From relationships
                        (short) 1, //the number of levels to expand, 0 equals expand all.
                        DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                        null, //where clause to apply to relationship, can be empty ""
                        (short) 0); //limit
                for(int i=0;i<changeList.size();i++) {
                    Map map = (Map) changeList.get(i);
                    String current = UIUtil.getValue(map, "current");
                    if ((current.equalsIgnoreCase("Submit") || current.equalsIgnoreCase("Review") || current.equalsIgnoreCase("Approve"))) {
                        action = mode_view;
                        canShowEditButton = false;
                        break;
                    }
                }
            }
//add by caipan  先设置默认全部是只读 20241112
          //  action=mode_view;
//end

            //FileList flList =docObj.getFiles(context);
            String docPhyId= docObj.getInfo(context,DomainObject.SELECT_PHYSICAL_ID);
            StringList versionSelectList = new StringList();
            versionSelectList.add(CommonDocument.SELECT_ID);
            versionSelectList.add(CommonDocument.SELECT_TITLE);
            versionSelectList.add("attribute[JF_PDFSourceFileId].value");
            MapList versionList = docObj.getRelatedObjects(context,
                    CommonDocument.RELATIONSHIP_LATEST_VERSION,
                    CommonDocument.TYPE_DOCUMENTS,
                    versionSelectList,
                    null,
                    false,
                    true,
                    (short) 1,
                    null,
                    null,
                    null,
                    null,
                    null);
            Set pdfSourceFileIdSet = new HashSet();
            Set hiddenSourceFileNameSet = new HashSet();
            for (int i = 0; i < versionList.size(); i++) {
                Map versionMap = (Map) versionList.get(i);
                String pdfSourceFileId = (String) versionMap.get("attribute[JF_PDFSourceFileId].value");
                if (UIUtil.isNotNullAndNotEmpty(pdfSourceFileId)) {
                    pdfSourceFileIdSet.add(pdfSourceFileId);
                }
            }
            for (int i = 0; i < versionList.size(); i++) {
                Map versionMap = (Map) versionList.get(i);
                String versionId = (String) versionMap.get(CommonDocument.SELECT_ID);
                String versionTitle = (String) versionMap.get(CommonDocument.SELECT_TITLE);
                String lowerVersionTitle = UIUtil.isNullOrEmpty(versionTitle) ? "" : versionTitle.toLowerCase();
                if (pdfSourceFileIdSet.contains(versionId)
                        && (lowerVersionTitle.endsWith(".ppt")
                        || lowerVersionTitle.endsWith(".pptx")
                        || lowerVersionTitle.endsWith(".pptm"))) {
                    hiddenSourceFileNameSet.add(versionTitle);
                }
            }
            _logger.info("hiddenSourceFileNameSet:: " + hiddenSourceFileNameSet);
            FileList allFileList =docObj.getFiles(context);
            Set pdfBaseFileNameSet = new HashSet();
            for (int i = 0; i <allFileList.size() ; i++) {
                matrix.db.File files=allFileList.get(i);
                String docName= files.getName();
                String lowerDocName = UIUtil.isNullOrEmpty(docName) ? "" : docName.toLowerCase();
                if(lowerDocName.endsWith(".pdf")){
                    pdfBaseFileNameSet.add(lowerDocName.substring(0, lowerDocName.lastIndexOf(".")));
                }
            }
            FileList needDownloadFileList =new FileList();
            StringList fileNameList=new StringList();
            String allowSuffix=config_properties.getProperty("allowSuffix");
            for (int i = 0; i <allFileList.size() ; i++) {
                matrix.db.File files=allFileList.get(i);
               String docName= files.getName();
               _logger.info("docName>>>"+docName);
                String suffix = docName.replaceAll(".*(\\..*)","$1");
                String lowerDocName = UIUtil.isNullOrEmpty(docName) ? "" : docName.toLowerCase();
                boolean isPptFile = lowerDocName.endsWith(".ppt") || lowerDocName.endsWith(".pptx") || lowerDocName.endsWith(".pptm");

               if(hiddenSourceFileNameSet.contains(docName)){
                   _logger.info("skip pdf source file in online preview: "+docName);
                   continue;
               }

               if(isPptFile && pdfBaseFileNameSet.contains(lowerDocName.substring(0, lowerDocName.lastIndexOf(".")))){
                   _logger.info("skip ppt file because same name pdf exists in online preview: "+docName);
                   continue;
               }

               if(allowSuffix.contains(suffix)){
                   fileNameList.add(docName);
                   needDownloadFileList.add(files);
               }

            }
            if(fileNameList.size()==0&&needDownloadFileList.size()==0){
                _logger.info("该文档对象 没有指定格式的文件下载");
                return mlMaplist;
            }

            //System.out.println("allFileList>>>>==="+allFileList);
//            StringList slFileInfoList = new StringList();
//
//            slFileInfoList.add(CommonDocument.SELECT_FILE_NAME);
//            slFileInfoList.add(CommonDocument.SELECT_FILE_FORMAT);
//            slFileInfoList.add(CommonDocument.SELECT_FILE_SIZE);
//
//            Map masterObjectMap = docObj.getInfo(context,slFileInfoList);
//            StringList fileNameList       = (StringList) masterObjectMap.get(CommonDocument.SELECT_FILE_NAME);

            // StringList fileSizeList   = (StringList) masterObjectMap.get(CommonDocument.SELECT_FILE_SIZE);
            //Date date=new Date();
            //后续缓存配置文件
            String downLoadLocalPath=config_properties.getProperty("DocOnlineLocalSavePath");
            //String serverUrl="http://10.10.30.142/wopi/files/";
            docObj.checkoutFiles(context, false, null,needDownloadFileList, downLoadLocalPath);
            String user=context.getUser();
            Map userMapInfo=getOnlyOfficeUserMap(user);
            String createJsonStr=getCreatedInfo(userMapInfo);
            if(userMapInfo!=null){
                user= (String) userMapInfo.get(onlyOffice_UserIdKey);
            }
            _logger.info("fileNameList:: "+fileNameList);
            String onlyOfficeViewKey=config_properties.getProperty("onlyOfficeViewKey");
            String DocOnlineLocalSavePath=config_properties.getProperty("DocOnlineLocalSavePath");

            //多个文件咋处理
            for (int i = 0; i < fileNameList.size(); i++) {
                String onlineLink="";
                 String localFileName=fileNameList.get(i);

                    String onlineDocFileName=docPhyId+"_"+ onlyOfficeViewKey+ "_" +localFileName;
                    String oldPath= DocOnlineLocalSavePath + localFileName;
                    String newPath=DocOnlineLocalSavePath + onlineDocFileName;
                    renameFile(oldPath,newPath);
                    //创建文件夹
                    createDocSameFolderAndAddJsonFile(onlineDocFileName,createJsonStr);

                   onlineLink=getOnlineLinkCase(onlyType_desktop,action,onlineDocFileName,user,null);
                    if(UIUtil.isNotNullAndNotEmpty(onlineLink)){
                        Map valMap=new HashMap();
                        valMap.put("onlineLink",onlineLink);//在线预览地址
                        valMap.put("file3deName",localFileName);
                        valMap.put("onlineDocFileName",onlineDocFileName);
                        valMap.put("canShowEditButton",canShowEditButton);
                        mlMaplist.add(valMap);
                    }
            }

        }catch (Exception exception){
            exception.printStackTrace();
        }
        _logger.info("return  path: "+mlMaplist);
        return mlMaplist;

    }





    //@1在线插入 http://10.10.30.167:9991/example/editor?type=desktop&mode=review&fileName=%E7%AC%AC%E4%B8%89%E6%96%B9%E9%82%AE%E7%AE%B1%E7%A8%8B%E5%BA%8F%E6%9B%B4%E6%94%B9tecwin%E6%95%99%E7%A8%8B(1).docx&userid=uid-0&lang=zh&directUrl=false
    //@2在线编辑 http://10.10.30.167:9991/example/editor?type=desktop&fileName=%E7%AC%AC%E4%B8%89%E6%96%B9%E9%82%AE%E7%AE%B1%E7%A8%8B%E5%BA%8F%E6%9B%B4%E6%94%B9tecwin%E6%95%99%E7%A8%8B(1).docx&userid=uid-0&lang=zh&directUrl=false
    //@3 @1+@2 的功能都可以修改，http://10.10.30.167:9991/example/editor?type=desktop&mode=blockcontent&fileName=%E7%AC%AC%E4%B8%89%E6%96%B9%E9%82%AE%E7%AE%B1%E7%A8%8B%E5%BA%8F%E6%9B%B4%E6%94%B9tecwin%E6%95%99%E7%A8%8B(1).docx&userid=uid-0&lang=zh&directUrl=false
    //@4在线批注 http://10.10.30.167:9991/example/editor?type=desktop&mode=comment&fileName=%E7%AC%AC%E4%B8%89%E6%96%B9%E9%82%AE%E7%AE%B1%E7%A8%8B%E5%BA%8F%E6%9B%B4%E6%94%B9tecwin%E6%95%99%E7%A8%8B(1).docx&userid=uid-0&lang=zh&directUrl=false
    //@5移动端修改 需要许可 http://10.10.30.167:9991/example/editor?type=mobile&mode=edit&fileName=%E7%AC%AC%E4%B8%89%E6%96%B9%E9%82%AE%E7%AE%B1%E7%A8%8B%E5%BA%8F%E6%9B%B4%E6%94%B9tecwin%E6%95%99%E7%A8%8B(1).docx&userid=uid-0&lang=zh&directUrl=false
    //@6切入浏览模式 没哟工具栏，http://10.10.30.167:9991/example/editor?type=embedded&mode=embedded&fileName=01.3DE%20R2023x%E5%AE%A2%E6%88%B7%E7%AB%AF%E5%AE%89%E8%A3%85%E6%89%8B%E5%86%8C%20-%20V1.1.docx&userid=uid-0&lang=zh&directUrl=false
    //@7浏览模式 有工具栏http://10.10.30.167:9991/example/editor?type=desktop&mode=view&fileName=%E7%AC%AC%E4%B8%89%E6%96%B9%E9%82%AE%E7%AE%B1%E7%A8%8B%E5%BA%8F%E6%9B%B4%E6%94%B9tecwin%E6%95%99%E7%A8%8B(1).docx&userid=uid-0&lang=zh&directUrl=false
    //@8移动端浏览模式 没哟工具栏http://10.10.30.167:9991/example/editor?type=mobile&mode=view&fileName=01.3DE%20R2023x%E5%AE%A2%E6%88%B7%E7%AB%AF%E5%AE%89%E8%A3%85%E6%89%8B%E5%86%8C%20-%20V1.1.docx&userid=uid-0&lang=zh&directUrl=false

    public String getOnlineLinkCase(String type,String mode,String fileName,String userId,String appendOther) throws  Exception{
        String onlineLink="";
        StringBuffer url=new StringBuffer();
        String onlyOfficeURL=config_properties.getProperty("onlyOfficeURL");
        url.append(onlyOfficeURL);
       // fileName= XSSUtil.encodeForHTML(context, fileName);
        fileName= URLEncoder.encode(fileName,"utf-8");
        url.append("fileName="+fileName);
        url.append("&type="+type);
        url.append("&action="+mode);
        if(UIUtil.isNullOrEmpty(userId)){
            userId="3";
        }
        url.append("&userid="+userId);
        url.append("&lang=zh");
        url.append("&directUrl=false");
        if (UIUtil.isNotNullAndNotEmpty(appendOther)){
            url.append("&"+appendOther);
        }
        onlineLink=url.toString();
        _logger.info("onlineLink "+onlineLink);
        return onlineLink;

    }

    private static final String appendingSuffix="-hist";
    private static final String addJsonFileName="createdInfo.json";
    // demo {"created":"2024-01-31 16:10:37","name":"Robert SHU","id":"4"}


    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public String getCurrentSystemDate(){
        String strDate="";
        try{
            Date now = new Date();
            strDate  = simpleDateFormat.format(now);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return strDate;
    }

    public boolean createDocSameFolderAndAddJsonFile(String docName,String appendJsonStr){
        boolean isOK=true;
        try{
          String   DocOnlineLocalSavePath=config_properties.getProperty("DocOnlineLocalSavePath");
            File file=new File(DocOnlineLocalSavePath + docName);
            if(file.exists()){
                String suffixPath=DocOnlineLocalSavePath + docName + appendingSuffix;
                File folderSuffix=new File(suffixPath);

                if(folderSuffix.exists()){

                }else{
                    folderSuffix.mkdirs();
                }
                String jsonFilePath=suffixPath+"/"+addJsonFileName;
                File newJsonFile=new File(jsonFilePath);
                boolean isNeedAdd=false;
                if(!newJsonFile.exists()){
                    boolean fileCreated = newJsonFile.createNewFile();
                    if (fileCreated) {
                        isNeedAdd=true;
                       _logger.info("文件已创建：" +jsonFilePath);
                    } else {
                        isNeedAdd=false;
                        _logger.info("无法创建文件：" + jsonFilePath);
                    }
                }
                //新建文件才会添加内容
                if(isNeedAdd){
                    try (FileWriter jsonFile = new FileWriter(jsonFilePath)) {
                        jsonFile.write(appendJsonStr);
                    } catch (Exception e) {
                        _logger.info("写入json文件异常：" + jsonFilePath);
                        e.printStackTrace();
                    }

                }


            }


        }catch (Exception ex){

        }
        return isOK;

    }

    /*
    * 指定路径下 去创建only在线json 文件信息
    * */
    public boolean createDocSameFolderAndAddJsonFile_inputPath(String docName,String appendJsonStr,String DocOnlineLocalSavePath){
        boolean isOK=true;
        try{
           // String   DocOnlineLocalSavePath=config_properties.getProperty("DocOnlineLocalSavePath");
            File file=new File(DocOnlineLocalSavePath + docName);
            if(file.exists()){
                String suffixPath=DocOnlineLocalSavePath + docName + appendingSuffix;
                File folderSuffix=new File(suffixPath);

                if(folderSuffix.exists()){

                }else{
                    folderSuffix.mkdirs();
                }
                String jsonFilePath=suffixPath+"/"+addJsonFileName;
                File newJsonFile=new File(jsonFilePath);
                boolean isNeedAdd=false;
                if(!newJsonFile.exists()){
                    boolean fileCreated = newJsonFile.createNewFile();
                    if (fileCreated) {
                        isNeedAdd=true;
                        _logger.info("文件已创建：" +jsonFilePath);
                    } else {
                        isNeedAdd=false;
                        _logger.info("无法创建文件：" + jsonFilePath);
                    }
                }
                //新建文件才会添加内容
                if(isNeedAdd){
                    try (FileWriter jsonFile = new FileWriter(jsonFilePath)) {
                        jsonFile.write(appendJsonStr);
                    } catch (Exception e) {
                        _logger.info("写入json文件异常：" + jsonFilePath);
                        e.printStackTrace();
                    }

                }


            }


        }catch (Exception ex){

        }
        return isOK;

    }


    /*   //demo
    {"created":"2024-01-31 16:10:37","name":"Robert SHU","id":"4"}
*/
    private static  final  String onlyOffice_UserIdKey="userid";
    private static  final  String onlyOffice_FileNameKey="fileName";
    private static  final  String onlyOffice_userNameKey="userName";
    private static  final  String onlyOffice_userEmailKey="userEmail";
  public String getCreatedInfo(Map userInfoMap){
        String jsoStr="";
        try{
           String currentDate= getCurrentSystemDate();

            if(userInfoMap!=null){
                jsoStr="{\"created\":\""+currentDate+"\",\"name\":\""+(String)userInfoMap.get(onlyOffice_userNameKey)+"\",\"id\":\""+(String)userInfoMap.get(onlyOffice_UserIdKey)+"\"}";
            }
        _logger.info("jsoStr >>>"+jsoStr);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return jsoStr;

    }

    /*
    * 根据txt文件 查找 系统用户对应onlyoffice 的userid;
    * */
    private  static final int LENGTHFLAG = 3;
    //onlyoffice 默认系统已存在用户个数 用于计算其他新增同步3de用户的UserId
    //private  static final int onlyExistUserNumber= 3;
    public Map getOnlyOfficeUserMap(String currentUser){
        Map userMap=new HashMap();
        String userId = "";
        if("User Agent".equalsIgnoreCase(currentUser)||"creator".equalsIgnoreCase(currentUser)){
            currentUser="admin_platform";
        }
        try{
            String userInfoPath=config_properties.getProperty("userInfoPath");
           // String userInfoPath= ${CLASS:Aer_ConfigConstants}.userInfoPath;
            _logger.info("userInfoPath : "+userInfoPath);
           // String filePath = "D:/APP/3deUserInfo.txt";
            File readUserFile = new File(userInfoPath);

            if (readUserFile.exists()) {
                try (

                        FileReader fileReader = new FileReader(userInfoPath);
                        BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                   // boolean isHasUser=false;
                    String person_split_Key=config_properties.getProperty("person_split_Key");
                    _logger.info("person_split_Key>>>"+person_split_Key);
                    String onlyExistUserNumberStr=config_properties.getProperty("onlyExistUserNumber");
                    _logger.info("onlyExistUserNumberStr>>>"+onlyExistUserNumberStr);
                    int existUserNub=0;
                    if(UIUtil.isNotNullAndNotEmpty(onlyExistUserNumberStr)){
                        existUserNub=Integer.valueOf(onlyExistUserNumberStr);
                    }
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                      //  _logger.info("line"+line);
                        if(line.contains(person_split_Key+currentUser+person_split_Key)){


                            _logger.info("当前 账号 找到对应only 信息"+line);
                            String[] userInfo = line.split(person_split_Key);
                            for (int i = 0; i <userInfo.length ; i++) {
                                _logger.info("userInfo "+userInfo[i]);
                            }

                            String userName = "";
                            String userEmail = "";
                            if (userInfo.length > LENGTHFLAG) {
                                userName = userInfo[2] + "(" + userInfo[1] + ")";
                                userEmail = userInfo[LENGTHFLAG];
                            }

                             userId = userInfo[0];

                             try{
                                 int uid=Integer.valueOf(userId);
                                  userId=(uid+existUserNub)+"";
                             }catch (Exception exxx){
                                 _logger.info("txt 文件格式不正确，转换异常");
                                 exxx.printStackTrace();
                             }

                            userMap.put(onlyOffice_UserIdKey,userId);
                            userMap.put(onlyOffice_userNameKey,userName);
                            userMap.put(onlyOffice_userEmailKey,userEmail);

                             break;
                        }

                    }
                }
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        //如果为空要不要 设置匿名用户
//        if(UIUtil.isNullOrEmpty(userId)){
//            userId= ${CLASS:Aer_ConfigConstants}.onlyOfficeUser;
//        }
        return userMap;

    }


    /*判断字符中是否有中文*/
    public static boolean containsChineseCharacters(String str) {
        if (str == null) {
            return false;
        }
        String regex = "[\u4e00-\u9fa5]+";
        return str.matches(".*" + regex + ".*");
    }

    /*
    * 重命名文件
    * */
    public boolean renameFile(String oldPath,String newPath) throws Exception{
            boolean isOK=true;
        try{

        File file = new File (oldPath);
        if (file.exists()) {
            boolean isRenamed = file.renameTo(new File(newPath));

            if (isRenamed) {
                _logger.info("文件已成功重命名！");

            } else {
                isOK=false;
                _logger.info("文件重命名失败！");
            }
        } else {
            isOK=false;
            _logger.info("指定的文件不存在！"+oldPath);
        }
    }catch (Exception ex){
            isOK=false;
            ex.printStackTrace();
        }
        return isOK;
}



/*
* 根据文档 id 及 route 及文件id +版本 信息去 获取批注文件
* 场景1 route名称 + Ca名称 +文档名称
* 场景2 route名称 + 文档名称
* */
    public MapList getDocMarkUpServicePath_old(Context context , String[] args){
        String path="";
        MapList mlMaplist=new MapList();

        try{

            //Map progMap=JPO.unpackArgs(args);

            String docId=args[0];
            String parentId=args[1];
            _logger.info("getDocMarkUpServicePath >>>docId "+docId);
            DomainObject docObj=DomainObject.newInstance(context,docId);

            DomainObject parObj=DomainObject.newInstance(context,parentId);
            String routeName=parObj.getName(context);
            String phyId=docObj.getInfo(context, DomainConstants.SELECT_PHYSICAL_ID);



            FileList allFileList =docObj.getFiles(context);
            FileList needDownloadFileList =new FileList();
            //3de文件名称，
           // StringList fileNameList=new StringList();

           // StringList fileMarkDocList=new StringList();

            String downLoadLocalPath=config_properties.getProperty("DocOnlineLocalSavePath");
            String currentUser=context.getUser();
            StringList showNameList=new StringList();
            //检查服务器上是否有对应批注文件存在
          for (int i = 0; i <allFileList.size() ; i++) {
                matrix.db.File files=allFileList.get(i);
                String docName= files.getName();
                _logger.info("docName>>>"+docName);
                //获取后缀
                String suffix = docName.replaceAll(".*(\\..*)","$1");
                if(suffix.contains("doc")||suffix.contains("xls")||suffix.contains("pdf")||suffix.contains("ppt")){

                    String markDocName=phyId+"_"+routeName+"_"+docName;
                    File newFile=new File(downLoadLocalPath+markDocName);
                    if(newFile.exists()){
                        _logger.info("服务器存在文件 >>"+markDocName);
                        showNameList.add(docName);
                    }else{

                        needDownloadFileList.add(files);
                       // needDownloadRenameFileSl.add(markDocName);
                    }

                }

            }
            docObj.checkoutFiles(context, false, null,needDownloadFileList, downLoadLocalPath);

            _logger.info("needDownloadFileList.size(); >> "+needDownloadFileList.size());
            //需要下载的原始文件进行批注
            for (int i = 0; i <needDownloadFileList.size() ; i++) {
                matrix.db.File files=needDownloadFileList.get(i);
                String docName= files.getName();
                String markDocName=phyId+"_"+routeName+"_"+docName;
                String oldPath=downLoadLocalPath+docName;
                String newPath=downLoadLocalPath+markDocName;
               boolean isOk= renameFile(oldPath,newPath);
               if(isOk){
                   showNameList.add(docName);
               }

            }
            //封装数据 页面展示
            String onlineLink="";
            _logger.info("showNameList >> "+showNameList);
            Map userInfoMap  =getOnlyOfficeUserMap(currentUser);
            if(userInfoMap!=null){
                currentUser= (String) userInfoMap.get(onlyOffice_UserIdKey);
            }
            String jsonStr=getCreatedInfo(userInfoMap);
            for (int i = 0; i <showNameList.size() ; i++) {
                String docName=showNameList.get(i);
                String markDocName=phyId+"_"+routeName+"_"+docName;

                createDocSameFolderAndAddJsonFile(markDocName,jsonStr);
                onlineLink=getOnlineLinkCase(onlyType_desktop,mode_comment,markDocName,currentUser,null);
                if(UIUtil.isNotNullAndNotEmpty(onlineLink)){
                    Map valMap=new HashMap();
                    valMap.put("onlineLink",onlineLink);//在线预览地址
                    valMap.put("file3deName",docName);
                    valMap.put("onlineDocFileName",markDocName);
                    mlMaplist.add(valMap);
                }

            }

        }catch (Exception exception){
            exception.printStackTrace();
        }
        _logger.info("return  path: "+mlMaplist);
        return mlMaplist;

    }

    /*
    * 查询流程批注文件
    * */
    public MapList getDocRouteMarkUPInfo(Context context , String[] args ,String action){
        String path="";
        MapList mlMaplist=new MapList();

        try{

            //Map progMap=JPO.unpackArgs(args);

            String docId=args[0];
            String parentId=args[1];
            _logger.info("getDocRouteMarkUPInfo >>>docId "+docId);
            DomainObject docObj=DomainObject.newInstance(context,docId);

            DomainObject parObj=DomainObject.newInstance(context,parentId);
            String routeName=parObj.getName(context);
            String phyId=docObj.getInfo(context, DomainConstants.SELECT_PHYSICAL_ID);

            MapList allDoc=getDocAllFiles(context,docObj,true);

            //3de文件名称，
            StringList fileNameList=new StringList();

            StringList fileMarkDocList=new StringList();
            String downLoadLocalPath=config_properties.getProperty("DocOnlineLocalSavePath");
            String currentUser=context.getUser();
            //前台显示名称
            StringList showNameList=new StringList();
            //跳转真实文件文件名称
            StringList actualFileList=new StringList();
            //ContextUtil.pushContext(context);
            String allowSuffix=config_properties.getProperty("allowSuffix");
            for (int i = 0; i <allDoc.size() ; i++) {
                Map valMap= (Map) allDoc.get(i);
                String checkOutId= (String) valMap.get("checkOutId");

                String docName= (String) valMap.get("fileName");
                String suffix = docName.replaceAll(".*(\\..*)","$1");
                String verId= (String) valMap.get("id");
                String ver= (String) valMap.get("Ver");
                //后续换成key
                _logger.info("suffix"+suffix);
                if(allowSuffix.contains(suffix)){
                    String markDocName=phyId+"_"+routeName+"_"+verId+"_Ver"+ver+"_"+docName;
                    File newFile=new File(downLoadLocalPath+markDocName);
                    //检查服务器上是否有对应批注文件存在 没有则下载
                    if(newFile.exists()){
                        _logger.info("服务器存在批注文件 >>"+markDocName);
                        showNameList.add(docName);
                        actualFileList.add(markDocName);
                    }else{
                        _logger.info("服务器bu存在批注文件 >>"+markDocName);
                    }

                }

            }
            // ContextUtil.popContext(context);

            //封装数据 页面展示
            String onlineLink="";
            _logger.info("showNameList >> "+showNameList);
            Map userInfoMap  =getOnlyOfficeUserMap(currentUser);
            if(userInfoMap!=null){
                currentUser= (String) userInfoMap.get(onlyOffice_UserIdKey);
            }
            String jsonStr=getCreatedInfo(userInfoMap);
            for (int i = 0; i <showNameList.size() ; i++) {
                String docName=showNameList.get(i);
                String markDocName=actualFileList.get(i);

                createDocSameFolderAndAddJsonFile(markDocName,jsonStr);
                onlineLink=getOnlineLinkCase(onlyType_desktop,action,markDocName,currentUser,null);
                if(UIUtil.isNotNullAndNotEmpty(onlineLink)){
                    Map valMap=new HashMap();
                    valMap.put("onlineLink",onlineLink);//在线预览地址
                    valMap.put("file3deName",docName);
                    valMap.put("onlineDocFileName",markDocName);
                    mlMaplist.add(valMap);
                }
            }

        }catch (Exception exception){
            exception.printStackTrace();
        }
        _logger.info("return  path: "+mlMaplist);
        return mlMaplist;

    }

    /*
     * 根据文档 id 及 route 及文件id +版本 信息去 获取批注文件
     * 场景1 route名称 + Ca名称 +文档名称
     * 场景2 route名称 + 文档名称
     * */
    public MapList getDocMarkUpServicePath(Context context , String[] args){
        String path="";
        MapList mlMaplist=new MapList();
        mlMaplist= getDocRouteMarkUPInfo(context,args,mode_comment);
        return mlMaplist;
    }


    /*
     *inbox 流程任务页面 预览按钮显示的也是批注文件
     *  */
    public MapList getDocObjViewServicesPath(Context context , String[] args){
        String path="";
        MapList mlMaplist=new MapList();
        mlMaplist= getDocRouteMarkUPInfo(context,args,mode_view);
        return mlMaplist;
    }



/*
*
* 获取文档下所有文件 包括version对象的所有版本文件
* */
    public MapList getDocAllFiles(Context context,DomainObject masterObject,boolean isGetLastNew){
        MapList docList=new MapList();
        try{
           String  masterId=masterObject.getInfo(context,DomainObject.SELECT_PHYSICAL_ID);
            // Version Object seletcs
            StringList versionSelectList = new StringList(9);
            versionSelectList.add(CommonDocument.SELECT_ID);
            versionSelectList.add(CommonDocument.SELECT_REVISION);
//            versionSelectList.add(CommonDocument.SELECT_DESCRIPTION);
//            versionSelectList.add(CommonDocument.SELECT_LOCKED);
//            versionSelectList.add(CommonDocument.SELECT_LOCKER);
//            versionSelectList.add(CommonDocument.SELECT_TITLE);
//            versionSelectList.add(CommonDocument.SELECT_FILE_NAME);
//            versionSelectList.add(CommonDocument.SELECT_FILE_FORMAT);
//            versionSelectList.add(CommonDocument.SELECT_FILE_MODIFIED);
//            versionSelectList.add(CommonDocument.SELECT_FILE_SIZE);
//            versionSelectList.add(CommonDocument.SELECT_OWNER);
//            versionSelectList.add(DomainConstants.SELECT_ORIGINATED);
//            versionSelectList.add(DomainConstants.SELECT_TYPE);
//            versionSelectList.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
//            versionSelectList.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
            MapList versionList = masterObject.getRelatedObjects(context,
                    CommonDocument.RELATIONSHIP_ACTIVE_VERSION,
                    CommonDocument.TYPE_DOCUMENTS,
                    versionSelectList,
                    null,
                    false,
                    true,
                    (short)1,
                    null,
                    null,
                    null,
                    null,
                    null);

            Iterator versionItr  = versionList.iterator();
            while(versionItr.hasNext()) {
                Map fileVersionMap = (Map) versionItr.next();
                String versionId= (String) fileVersionMap.get(CommonDocument.SELECT_ID);
                DomainObject doDoc=DomainObject.newInstance(context,versionId);

//               StringList verAllIds= doDoc.getInfoList(context,"majorids[].minorrevisions."+DomainObject.SELECT_PHYSICAL_ID+"" ,false);
//               StringList verAllTitle= doDoc.getInfoList(context,"majorids[].minorrevisions.attribute[Title]",true);

             String reAllIds=  MqlUtil.mqlCommand(context,"print bus "+versionId+" select "+"majorids[].minorrevisions."+DomainObject.SELECT_PHYSICAL_ID+" dump = " ,false);
             _logger.info("reAllIds>>>>>>"+reAllIds);
                String reAllTitle=  MqlUtil.mqlCommand(context,"print bus "+versionId+" select "+"majorids[].minorrevisions.attribute[Title] dump = " ,false);
                _logger.info("reAllTitle>>>>>>"+reAllTitle);
                //StringList verAllFormat= doDoc.getInfoList(context,"majorids[].minorrevisions.format");

                StringList verAllIds=new StringList();
                StringList verAllTitle=new StringList();
                int lastVerSize=1;
              if(!isGetLastNew){
                  //只取最新版 最后一个为最新
                  if(UIUtil.isNotNullAndNotEmpty(reAllIds)){

                      String ids[]=reAllIds.split("=");
                      verAllIds.add(ids[ids.length-1]);
                      lastVerSize=ids.length;
                      String title[]=reAllTitle.split("=");
                      verAllTitle.add(title[title.length-1]);

                  }

              }else{
                  if(UIUtil.isNotNullAndNotEmpty(reAllIds)){
                      String ids[]=reAllIds.split("=");
                      for (int i = 0; i <ids.length ; i++) {
                          verAllIds.add(ids[i]);
                      }
                      String title[]=reAllTitle.split("=");
                      for (int i = 0; i <title.length ; i++) {
                          verAllTitle.add(title[i]);
                      }

                  }

              }





/*

                majorids[E30A1829DB62000064706A980002E4D2].minorrevisions[1].attribute[Title].value = 沃飞文档模板示例.docx
                majorids[E30A1829DB62000064706A980002E4D2].minorrevisions[2].attribute[Title].value = 新建DOCX 文档.docx
                majorids[E30A1829DB62000064706A980002E4D2].minorrevisions[3].attribute[Title].value = ENXDISC_AP_Config.json
                majorids[E30A1829DB62000064706A980002E4D2].minorrevisions[4].attribute[Title].value = 沃飞二次部署事宜.docx

*/              //多版本
                int verSize=verAllIds.size();
                _logger.info("verSize "+verSize);
                _logger.info("verAllIds "+verAllIds);
                if(verSize>1) {

                    for (int i = 0; i < verAllIds.size(); i++) {

                        String verId=verAllIds.get(i);
                        Map newMap = new HashMap();
                        newMap.put("id", verId);
                        newMap.put("Ver", (i+1)+"");
                        newMap.put("checkOutId", verId);
                        newMap.put("masterPhysicalid", masterId);


                        if(i==verSize-1){
                         //最后一个version 文件对象的文件 挂在文档masterId对象上；
                       // newMap.put("id", masterId);
                        newMap.put("checkOutId", masterId);
                        newMap.put("Ver", verSize+"");
                        }

                        String fileName=verAllTitle.get(i);
                        newMap.put("fileName", fileName);
                        docList.add(newMap);
                    }
                }else{
                    //没有多版本则挂在 挂在文档masterId对象上；
                    String verIdStr = verAllIds.get(0);

                    Map newMap = new HashMap();
                    newMap.put("checkOutId", masterId);
                    newMap.put("id", verIdStr);
                    newMap.put("Ver", ""+lastVerSize);
                    String fileName=verAllTitle.get(0);
                    newMap.put("fileName", fileName);
                    newMap.put("checkOutId", masterId);
                    newMap.put("masterPhysicalid", masterId);
                    docList.add(newMap);
                }


            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        _logger.info("all doc "+docList);
        return  docList;
    }
    public static  String relationship_Object_Route="Object Route";
    public static  String type_Change_Action="Change Action";
    public static  String type_Change_Request="Change Request";
    public static  String type_Change_Order="Change Order";

    /*
    * 获取route关联的内容是ca 下的文件
    * */
    public StringList getRouteContentIsCAFileItems(Context context,String args[]){
        StringList docSLId=new StringList();
        try{
            String routeId=args[0];
            DomainObject doRoute=DomainObject.newInstance(context,routeId);
            StringList caSL=doRoute.getInfoList(context,"to["+relationship_Object_Route+"|from.type=='"+ type_Change_Action+"'].from.id",false);
            _logger.info("caSL>>>"+caSL);
            for (int i = 0; i <caSL.size() ; i++) {
                String caId=caSL.get(i);
                ChangeAction ca=new ChangeAction(caId);
              MapList reItem2= ca.getAffectedItems(context);
               System.out.println("reItem2>>>"+reItem2);
               /*MapList reItem2= ca.getProposedItems(context);
                _logger.info("reItem2>>>"+reItem2);*/
                for (int j = 0; j <reItem2.size() ; j++) {
                    Map mapVal= (Map) reItem2.get(j);
                    //String type= (String) mapVal.get(DomainObject.SELECT_TYPE);
                    String objId= (String) mapVal.get(DomainObject.SELECT_ID);
                    DomainObject doObj=DomainObject.newInstance(context,objId);
                     if(doObj.isKindOf(context,DomainObject.TYPE_DOCUMENT)){
                         docSLId.add(objId);
                     }
                }
            }
//            //下载
//            for (int i = 0; i <docSLId.size() ; i++) {
//
//            }

        }catch (Exception ex){

        }
        return docSLId;

    }
    //流程启动时候 触发流程批注文件的生成，关联的CA;
    public void triggerIsRouteStartCreateMarkFile(Context context,String[] args){
        try{
            String routeId=args[0];
            StringList caReDocSL=getRouteContentIsCAFileItems(context,args);
            if(caReDocSL.size()>0){
                for (int i = 0; i <caReDocSL.size() ; i++) {

                    MapList reML=checkOutDocFileByRoute(context,new String[]{caReDocSL.get(i),routeId});
                    _logger.info("下载 内容信息reML>>>"+reML);
                }

            }else {
                _logger.info("流程未关联ca 或关联了CA但的内容没有文档");
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    /*
    * 下载doc文件;
    * */
    public MapList checkOutDocFileByRoute(Context context,String[] args){
        MapList reML=new MapList();
        try{
            String docId=args[0];
            String routeId=args[1];
            DomainObject doDoc=DomainObject.newInstance(context,docId);

            MapList docFileML=getDocAllFiles(context,doDoc,false);
            if(docFileML.size()==0){
                return  reML;
            }
            DomainObject doRoute=DomainObject.newInstance(context,routeId);
            String routeName=doRoute.getInfo(context,DomainObject.SELECT_NAME);

            String currentUser=context.getUser();
            Map userInfoMap  =getOnlyOfficeUserMap(currentUser);
//            if(userInfoMap!=null){
//                currentUser= (String) userInfoMap.get(onlyOffice_UserIdKey);
//            }
            String jsonStr=getCreatedInfo(userInfoMap);
            String phyId=doDoc.getInfo(context,DomainObject.SELECT_PHYSICAL_ID);

            for (int i = 0; i <docFileML.size() ; i++) {
                Map valMap= (Map) docFileML.get(i);
                String checkOutId= (String) valMap.get("checkOutId");

                String docName= (String) valMap.get("fileName");
               // String suffix = docName.replaceAll(".*(\\..*)","$1");
                String verId= (String) valMap.get("id");
                String ver= (String) valMap.get("Ver");
                DomainObject doCheckObj=DomainObject.newInstance(context,checkOutId);
                FileList currentFileList=doCheckObj.getFiles(context);

                if(currentFileList.size()>1){
                    for (int j = 0; j <currentFileList.size() ; j++) {
                        matrix.db.File file=currentFileList.get(j);
                        if(docName.equals(file.getName())){
                            currentFileList=new FileList();
                            currentFileList.addAll(file);
                            break;
                        }
                    }
                }
                String downLoadLocalPath=config_properties.getProperty("DocOnlineLocalSavePath");

                doCheckObj.checkoutFiles(context, false, null,currentFileList, downLoadLocalPath);

                //判读是否下载成功 下载成功重命名
               File newFile=new File(downLoadLocalPath+docName);
                String markDocName=phyId+"_"+routeName+"_"+verId+"_Ver"+ver+"_"+docName;
                if(newFile.exists()){
                    String oldPath=downLoadLocalPath+docName;
                    String newPath=downLoadLocalPath+markDocName;
                    boolean isOk= renameFile(oldPath,newPath);
                    if(isOk){

                        //创建onlyOffice
                        createDocSameFolderAndAddJsonFile(markDocName,jsonStr);

                        _logger.info(" route启动 下载生成批注文件成功 >>markDocName "+markDocName);
                        Map okMap=new HashMap();
                        okMap.put("docName",docName);
                        okMap.put("markDocName",markDocName);
                        okMap.put("verId",verId);
                        okMap.put("ver",ver);
                        okMap.put("masterId",docId);
                        reML.add(okMap);

                    }
                }else{
                    _logger.info(" route启动 下载失败文件 >>docName "+docName);
                }

            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return reML;
    }

    //from[Reference Document].to.name


    /*
    * CR 推到审批中 生成IA编辑文件
    * 此需求变化，cr创建的时候 附件生成 及生成onlyoffice 批注文件
    * */
    public void triggerIsCrInApproveAutoCheckIAMarkFile(Context context,String[] args){
        try{
            String crId=args[0];
            DomainObject doCR=DomainObject.newInstance(context ,crId);
            //from[Reference Document].to.name
            StringList reDocSL=doCR.getInfoList(context,"from["+relationship_Reference_Document+"|to.current!='OBSOLETE'].to.id",false);
            if(reDocSL.size()==0){
                _logger.info("》》》未找到 附件");
                return;
            }
            String CRName=doCR.getInfo(context,DomainConstants.SELECT_NAME);
            //是否过滤手动上传 还是模板创建的文档?
            MapList allFileML=new MapList();
            String allowSuffix=config_properties.getProperty("allowSuffix");
             String downLoadLocalPath = config_properties.getProperty("DocOnlineLocalSavePath");

            String jsonStr= getOnlyOfficeCreateFileJson(context.getUser());
            for (int i = 0; i<reDocSL.size() ; i++) {
                String docId=reDocSL.get(i);
                DomainObject doDoc=DomainObject.newInstance(context,docId);
                //取文件最新版文档
                MapList docFileML=getDocAllFiles(context,doDoc,true);
                if(docFileML.size()>0){
                    //下载文件
                    for (int j = 0; j <docFileML.size() ; j++) {
                        Map valMap= (Map) docFileML.get(j);


                        String docName= (String) valMap.get("fileName");
                        String suffix = docName.replaceAll(".*(\\..*)","$1");

                        //允许下载文件类型
                        if(allowSuffix.contains(suffix)){
                            String verId= (String) valMap.get("id");
                            String ver= (String) valMap.get("Ver");
                            String checkOutId= (String) valMap.get("checkOutId");
                            String masterPhysicalid= (String) valMap.get("masterPhysicalid");
                            checkOutDocAction(context,checkOutId,docName,downLoadLocalPath);

                            //判读是否下载成功 下载成功重命名
                            File newFile=new File(downLoadLocalPath+docName);
                            String markDocName=CRName+"_"+masterPhysicalid+"_"+verId+"_V"+ver+"_"+docName;
                            if(newFile.exists()){
                                String oldPath=downLoadLocalPath+docName;
                                String newPath=downLoadLocalPath+markDocName;
                                boolean isOk= renameFile(oldPath,newPath);
                                if(isOk){

                                    //创建onlyOffice
                                    createDocSameFolderAndAddJsonFile(markDocName,jsonStr);

                                    _logger.info(" cr  下载生成批注文件成功 >>markDocName "+markDocName);
//                                    Map okMap=new HashMap();
//                                    okMap.put("docName",docName);
//                                    okMap.put("markDocName",markDocName);
//                                    okMap.put("verId",verId);
//                                    okMap.put("ver",ver);
//                                    okMap.put("masterId",docId);
//                                    reML.add(okMap);

                                }
                            }else{
                                _logger.info(" cr 下载批注文件失败文件 >>docName "+docName);
                            }

                        }

                    }


                }
            }



        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
    /*
    * 根据配置文件获取创建onlyOffice
    * */
    public String getOnlyOfficeCreateFileJson(String currentUser){
        String jsonStr="";
        try{
            Map userInfoMap  =getOnlyOfficeUserMap(currentUser);
            if(userInfoMap!=null){
                //userid
               // currentUser= (String) userInfoMap.get(onlyOffice_UserIdKey);
            }
             jsonStr=getCreatedInfo(userInfoMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        //String currentUser=context.getUser();

        return jsonStr;
    }


    /*
    *
    * */
    public FileList checkOutDocAction(Context context,String checkOutId,String docName,String downLoadLocalPath){
        boolean isOK=false;
        FileList currentFileList=new FileList();
        try{

                DomainObject doCheckObj = DomainObject.newInstance(context, checkOutId);
                 currentFileList = doCheckObj.getFiles(context);

                if (currentFileList.size() > 1) {
                    for (int j = 0; j < currentFileList.size(); j++) {
                        matrix.db.File file = currentFileList.get(j);
                        if (docName.equals(file.getName())) {
                            currentFileList = new FileList();
                            currentFileList.addAll(file);
                            break;
                        }
                    }
                }
                //后续定义一个保存批注文件的路径
               // String downLoadLocalPath = config_properties.getProperty("DocOnlineLocalSavePath");
            doCheckObj.checkoutFiles(context, false, null,currentFileList, downLoadLocalPath);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return currentFileList;

    }

    /*
    * 获取IA批注文件
    * */
    public MapList getIAMarkFile(Context context ,String args[]){
        MapList fileML=new MapList();
        try{
            String IAid=args[0];
            DomainObject doIA=DomainObject.newInstance(context,IAid);
//            from[Reference Document].to.name = DOC-32242828-0000735
//            from[Change Analysis].to.name = IA-0000159
//
            String action=mode_review;

             String CRId=doIA.getInfo(context,"to["+relationship_Change_Analysis+"].from.id");
             String iaCurrent=doIA.getInfo(context,"current");
             if(UIUtil.isNotNullAndNotEmpty(CRId)){
                 DomainObject doCr=DomainObject.newInstance(context,CRId);
                 String CRName=doCr.getInfo(context, DomainConstants.SELECT_NAME);

                 String currentCR=doCr.getInfo(context,DomainObject.SELECT_CURRENT);
                 if("In Approval".equals(currentCR)){
                     String currentUser=context.getUser();
                     String changeCoordinator=doCr.getInfo(context,"from["+relationship_Change_Coordinator+"].to.name");
                     if(currentUser.equals(changeCoordinator)){
                         action=mode_edit;
                     }
                 }


                 //IA只允许关联一个文件？多个怎么处理？ 过滤手动上传的文件？
                 StringList docIdSL=doIA.getInfoList(context,"from["+relationship_Reference_Document+"].to.id");
                //参考命名规则 String markDocName=CRName+"_"+masterPhysicalid+"_"+verId+"_Ver"+ver+"_"+docName;
                 String  FileNamingConvention=CRName;
                 for (int i = 0; i <docIdSL.size() ; i++) {
                     String docId=docIdSL.get(i);
                     MapList markFileML=getDocReMarkFileInfo(context,docId,action,FileNamingConvention);
                     if(markFileML.size()>0){
                         fileML.add(markFileML);
                     }
                 }
             }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return fileML;

    }


    /*
     * 获取CR 所有批注文件
     * */
    public MapList getCROrIAMarkFile(Context context ,String args[]){
        MapList fileML=new MapList();
        try{
            String crId=args[0];
            DomainObject doCR=DomainObject.newInstance(context,crId);
//            from[Reference Document].to.name = DOC-32242828-0000735
//            from[Change Analysis].to.name = IA-0000159
             String action=mode_review;
            String CRName=doCR.getInfo(context,DomainObject.SELECT_NAME);
            if(UIUtil.isNotNullAndNotEmpty(CRName)){
                    String currentCR=doCR.getInfo(context,DomainObject.SELECT_CURRENT);
                if("Approved".equals(currentCR)||"Complete".equals(currentCR)){
                    String currentUser=context.getUser();
                    String changeCoordinator=doCR.getInfo(context,"from["+relationship_Change_Coordinator+"].to.name");
                    if(currentUser.equals(changeCoordinator)){
                        action=mode_edit;
                    }
                }


                //IA只允许关联一个文件？多个怎么处理？ 过滤手动上传的文件？
                StringList docIdSL=doCR.getInfoList(context,"from["+relationship_Reference_Document+"].to.id");
                //参考命名规则 String markDocName=CRName+"_"+masterPhysicalid+"_"+verId+"_Ver"+ver+"_"+docName;
                String  FileNamingConvention=CRName;
                for (int i = 0; i <docIdSL.size() ; i++) {
                    String docId=docIdSL.get(i);
                    MapList markFileML=getDocReMarkFileInfo(context,docId,action,FileNamingConvention);
                    if(markFileML.size()>0){
                        fileML.add(markFileML);
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return fileML;
    }

    /*
     * 获取CR 指定文件的预览
     * */
    public MapList getCROrIAViewFileByDoc(Context context ,String args[]){
        MapList fileML=new MapList();
        try{
            String docId=args[0];
            String crId=args[1];
            DomainObject doCR=DomainObject.newInstance(context,crId);
//            from[Reference Document].to.name = DOC-32242828-0000735
//            from[Change Analysis].to.name = IA-0000159
//
            String CRName=doCR.getInfo(context,DomainObject.SELECT_NAME);
            if(UIUtil.isNotNullAndNotEmpty(CRName)){
                fileML=getDocReMarkFileInfo(context,docId,action_fillForms,CRName);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return fileML;
    }

    public static String relationship_Change_Coordinator="Change Coordinator";//变更协调人
    /*
     * 获取CR 指定文件的 所有批注文件
     * */
    public MapList getCROrIAMarkFileByDoc(Context context ,String args[]){
        MapList fileML=new MapList();
        try{
            String docId=args[0];
            String crId=args[1];
            DomainObject doCR=DomainObject.newInstance(context,crId);
//            from[Reference Document].to.name = DOC-32242828-0000735
//            from[Change Analysis].to.name = IA-0000159
//


            StringList selectInfo=new StringList();
            selectInfo.add(DomainObject.SELECT_NAME);
            selectInfo.add(DomainObject.SELECT_OWNER);
            selectInfo.add(DomainObject.SELECT_CURRENT);
            selectInfo.add("from["+relationship_Change_Coordinator+"].to.name");
            Map valMap=doCR.getInfo(context,selectInfo);
            String CRName= (String) valMap.get(DomainObject.SELECT_NAME);

            String crCurrent= (String) valMap.get(DomainObject.SELECT_CURRENT);
          String changeCoordinator= (String) valMap.get("from["+relationship_Change_Coordinator+"].to.name");
          _logger.info("9999"+changeCoordinator);
            String crOwner= (String) valMap.get(DomainObject.SELECT_OWNER);
//
          String currentUser=context.getUser();




    /*        policy[Request For Change].state = Approved
            policy[Request For Change].state = Complete
*/
            String action=action_fillForms;

            if(currentUser.equalsIgnoreCase(crOwner)&&"Prepare".equals(crCurrent)){
                action=mode_edit;
            }else  if(currentUser.equalsIgnoreCase(changeCoordinator)){
                action=mode_edit;
            }else {
                    action=mode_review;
            }

            if("Approved".equals(crCurrent)||"Complete".equals(crCurrent)){
                action=action_fillForms;
            }


            if(UIUtil.isNotNullAndNotEmpty(CRName)){
                     fileML=getDocReMarkFileInfo(context,docId,action,CRName);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return fileML;
    }



    /*
     * 根据文档对象 查询对应业务需求的批注文件
     *@ FileNamingConvention  文件命名规则 用于查找服务器上的批注文件
     * */
    public MapList getDocReMarkFileInfo(Context context , String docId ,String action,String FileNamingConvention){
        String path="";
        MapList mlMaplist=new MapList();

        try{

            //Map progMap=JPO.unpackArgs(args);

           // String docId=args[0];
            //String parentId=args[1];
            _logger.info("getDocReMarkFileInfo >>>docId "+docId);
            DomainObject docObj=DomainObject.newInstance(context,docId);

           // DomainObject parObj=DomainObject.newInstance(context,parentId);
            //String routeName=parObj.getName(context);
//            String phyId=docObj.getInfo(context, DomainConstants.SELECT_PHYSICAL_ID);
            //取所有版本的文件
            MapList allDoc=getDocAllFiles(context,docObj,true);

//            //3de文件名称，
//            StringList fileNameList=new StringList();
//            StringList fileMarkDocList=new StringList();

            String downLoadLocalPath=config_properties.getProperty("DocOnlineLocalSavePath");
            String currentUser=context.getUser();
            //前台显示名称
            StringList showNameList=new StringList();
            //跳转真实文件文件名称
            StringList actualFileList=new StringList();
            //ContextUtil.pushContext(context);
            String allowSuffix=config_properties.getProperty("allowSuffix");
            for (int i = 0; i <allDoc.size() ; i++) {
                Map valMap= (Map) allDoc.get(i);
//                String checkOutId= (String) valMap.get("checkOutId");

                String docName= (String) valMap.get("fileName");
                String suffix = docName.replaceAll(".*(\\..*)","$1");
                String verId= (String) valMap.get("id");
                String ver= (String) valMap.get("Ver");
                String masterPhysicalid= (String) valMap.get("masterPhysicalid");
                //后续换成key
                _logger.info("suffix"+suffix);
                if(allowSuffix.contains(suffix)){
                   // String markDocName=phyId+"_"+routeName+"_"+verId+"_Ver"+ver+"_"+docName;
                    String markDocName=FileNamingConvention+"_"+masterPhysicalid+"_"+verId+"_V"+ver+"_"+docName;
                    File newFile=new File(downLoadLocalPath+markDocName);
                    //检查服务器上是否有对应批注文件存在
                    if(newFile.exists()){
                        _logger.info("服务器存在批注文件 >>"+markDocName);
                        showNameList.add(docName);
                        actualFileList.add(markDocName);
                    }else{

                        _logger.info("服务器bu存在批注文件 >>"+markDocName);
                    }

                }

            }
            // ContextUtil.popContext(context);

            //封装数据 页面展示
            String onlineLink="";
            _logger.info("showNameList >> "+showNameList);
            Map userInfoMap  =getOnlyOfficeUserMap(currentUser);
            if(userInfoMap!=null){
                currentUser= (String) userInfoMap.get(onlyOffice_UserIdKey);
            }
            String jsonStr=getCreatedInfo(userInfoMap);
            for (int i = 0; i <showNameList.size() ; i++) {
                String docName=showNameList.get(i);
                String markDocName=actualFileList.get(i);

                createDocSameFolderAndAddJsonFile(markDocName,jsonStr);
                onlineLink=getOnlineLinkCase(onlyType_desktop,action,markDocName,currentUser,null);
                if(UIUtil.isNotNullAndNotEmpty(onlineLink)){
                    Map valMap=new HashMap();
                    valMap.put("onlineLink",onlineLink);//在线预览地址
                    valMap.put("file3deName",docName);
                    valMap.put("onlineDocFileName",markDocName);
                    mlMaplist.add(valMap);
                }
            }

        }catch (Exception exception){
            exception.printStackTrace();
        }
        _logger.info("return  path: "+mlMaplist);
        return mlMaplist;

    }




}
