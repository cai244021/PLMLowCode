import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dscn.plm.util.NioJDUtils;
import com.google.gson.Gson;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import com.nomagic.esi.common.a.M;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JF_DataInterface_mxJPO extends DomainObject {
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_DataInterface_mxJPO.class);
    private static final String TYPE_JF_DATASOURCE = "JFDataOutSource";
    private static final String ATTRIBUTE_JF_CODE = "JSCode";
    private static final String ATTRIBUTE_JF_SECRET = "JSSecret";
    private static final String ATTRIBUTE_JF_FILE_DOWNLOAD_ADDRESS = "JS_FileDownloadAddress";
    private static final String ATTRIBUTE_JF_CONNECT_PROJECT = "JSConnectProject";
    private static final String ATTRIBUTE_JF_DATA_PROCESS_PROGRESS = "JSdataProcessProgress";
    private static final String ATTRIBUTE_JF_REASON = "JSReason";
    private static final String RANGE_PROCESS_PROGRESS_ENOVIAPROCESS = "EnoviaProcess";  //Enovia内部处理
    private static final String RANGE_PROCESS_PROGRESS_PENDINGCAAPROCESS = "PendingCAAProcess";  //待CAA处理
    private static final String RANGE_PROCESS_PROGRESS_CURRENTCAAPROCESS = "CurrentCAAProcess";  //CAA正在处理
    private static final String RANGE_PROCESS_PROGRESS_CAAPROCESSINGCOMPLETE = "CAAProcessingComplete";  //CAA处理完成
    private static final String RANGE_PROCESS_PROGRESS_OUTSOURCEDATACOMPLETE = "OutsourceDataComplete";  //外发数据完成
    private static final String RANGE_PROCESS_PROGRESS_CAAPROCEFAILD = "CAAProcessingFailed";  //CAA处理失败
    private static final String RANGE_PROCESS_PROGRESS_SEND_EMAIL = "SentEmail";  //外发数据完成

    private static final String STRING_FILE_SHARE_PATH = "dataOutSource.share.path";
    private static final String STRING_MQL_ATTRIBUTE = "attribute[%s].value";
    private static final String STRING_MQL_RELATIONSHIP_FROM = "from[%s].to.%s";
    private static final String STRING_MQL_RELATIONSHIP_TO = "from[%s].to.%s";
    private static final String STRING_SEND_DATA = "SendData";



    /**
     * @description 传入MQL，返回查询结果
     * @author caipan
     * @param[1] context
     * @param[2] args
     * @throws
     * @time 2023/11/2 9:18
     */
    public JSONObject queryDate(Context context, String[] args) throws Exception{
      JSONObject result = new JSONObject();
      Map<Object,Object> map = JPO.unpackArgs(args);
        String mql = UIUtil.getValue(map, "mql");
        JF_LOGGER.info("mql:{}",mql);
      String resultStr = MqlUtil.mqlCommand(context, true, mql, true);
     StringList list = FrameworkUtil.split(resultStr, "\n");
      JSONObject returnObj = new JSONObject();
      returnObj.put("data",list);
      result = NioJDUtils.setSuccessmsg(returnObj);
      return result;
    }

    /**
    * @description:提供给Portal调用校验秘钥是否正确
    * @author: caipan
    * @date:
    * @param: * @param[1] context
    * @param[2] args
    * @return:
    */
    public JSONObject VerifyKeyTest(Context context,String[] args) throws Exception {
        JSONObject result = new JSONObject();
        Map<Object,Object> map = JPO.unpackArgs(args);
        String code = UIUtil.getValue(map, "code");
        String secret = UIUtil.getValue(map, "secret");
        //拿取到code和key后 去系统中找数据外发对象

        JSONObject returnObj = new JSONObject();
        JSONObject obj = new JSONObject();
        if(code.equalsIgnoreCase("ASDAFSDFSFSFD")&&secret.equalsIgnoreCase("232424324")) {
            JF_LOGGER.info("code:{} secret:{}",code,secret);
            Map fileMap = new HashMap();
            StringList fileList = new StringList();
            fileList.add("/data/2024620/SHJF00005_1.zip");
            fileList.add("/data/2024620/SHJF00005_2.zip");
            fileList.add("/data/2024620/SHJF00005_3.zip");
            obj.put("filePath", fileList);
            obj.put("SupplyNum","Supply-00001");
            obj.put("ProjectName","Suri");
            returnObj.put("data", obj);
            result = NioJDUtils.setSuccessmsg(returnObj);
        }else{
            result = NioJDUtils.setErrormsg(obj,"The key does not match");
        }
        return result;
    }


    /**
    * PLM和Portal数据外发接口
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return com.alibaba.fastjson.JSONObject
    * @date 2024/7/12 13:25
    * @description 用户点击链接打开Portal，输入提取码，触发提取码校验，并返回文件路径
    */
    public JSONObject VerifyKey(Context context,String[] args) throws Exception {
        JSONObject result = new JSONObject();
        Map<Object,Object> map = JPO.unpackArgs(args);
        Boolean isPush = Boolean.FALSE;
        try {
            String code = UIUtil.getValue(map, "code");
            String secret = UIUtil.getValue(map, "secret");
            //拿取到code和key后 去系统中找数据外发对象
            String strWhere = "attribute[" + ATTRIBUTE_JF_CODE + "].value=='" + code + "'&&attribute[" + ATTRIBUTE_JF_SECRET + "].value=='" + secret +"'";
            String objectId = JF_PublicMethodClass_mxJPO.findObject(context, TYPE_JF_DATASOURCE, strWhere);
            StringList fileList = new StringList();
            JSONObject returnObj = new JSONObject();
            JSONObject obj = new JSONObject();
            if (UIUtil.isNullOrEmpty(objectId)) {
                //没有对象  错误 返回为500
                result = setErrormsg(obj,"提取码不匹配！");
            } else {
                //有对象 需要拿取文件列表
                ContextUtil.pushContext(context);
                isPush = Boolean.TRUE;
                DomainObject domainObject = DomainObject.newInstance(context, objectId);
                String strBasicUrl = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{STRING_FILE_SHARE_PATH});
                JF_LOGGER.info("=================");
                StringList strSelectList = new StringList();
                strSelectList.add(DomainConstants.SELECT_NAME);
                strSelectList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_CONNECT_PROJECT));
                Map infoMap = domainObject.getInfo(context, strSelectList);
                String strName = (String) infoMap.get(DomainConstants.SELECT_NAME);
                String strConnectProjectId = (String) infoMap.get(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_CONNECT_PROJECT));
                String strConnectProjectName = DomainConstants.EMPTY_STRING;
                try {
                    strConnectProjectName = DomainObject.newInstance(context, strConnectProjectId).getInfo(context, DomainConstants.SELECT_NAME);
                } catch (Exception e) {
                    strConnectProjectName = "No Project";
                }
                strBasicUrl += strName + File.separator + STRING_SEND_DATA;
                File dirFile = new File(strBasicUrl);
                File[] files = dirFile.listFiles();
                if (files.length == 0) {
                    //如果没有文件
                    result = setErrormsg(obj,"无下载文件！");
                    return result;
                }
                String finalStrBasicUrl = File.separator + strName + File.separator + STRING_SEND_DATA;
                JF_LOGGER.info("files" + files.toString());
                JF_LOGGER.info("@@@@@@@@@@@@@@@@2");
                StringList filesList = Arrays.stream(files).map(f -> {
                    File file = (File) f;
                    String fileName = file.getName();
                    String FilePath = finalStrBasicUrl + File.separator + fileName;
                    return FilePath;
                }).collect(Collectors.toCollection(StringList::new));
                JF_LOGGER.info("filesList:" + filesList.toString());
                obj.put("filePath", filesList);
                obj.put("SupplyNum", strName);
                obj.put("ProjectName", strConnectProjectName);
                returnObj.put("data", obj);
                result = NioJDUtils.setSuccessmsg(returnObj);
                //设置发送发送状态属性：
                domainObject.setAttributeValue(context, ATTRIBUTE_JF_DATA_PROCESS_PROGRESS, RANGE_PROCESS_PROGRESS_OUTSOURCEDATACOMPLETE);
                //提升状态
                JF_LOGGER.info("#########33");
                if (!"OutsourceComplete".equalsIgnoreCase(domainObject.getInfo(context, DomainConstants.SELECT_CURRENT))) {
                    domainObject.promote(context);
                }
                MqlUtil.mqlCommand(context,"mod bus " + objectId + " add history 'Portal users download files!';");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        return result;
    }

    /**
    * PLM和CAA数据查询申请单接口
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/7/17 11:07
    * @description CAA传递处理申请单数量，PLM返回数据外发申请单的数模结构的json文件
    */
    public JSONObject structureJsonFile(Context context, String[] args) throws Exception {
        try {
            JSONObject resultJson = new JSONObject();
            Map<Object,Object> map = JPO.unpackArgs(args);
            //传递数量
            Integer amount = (Integer) map.get("amount");
//            short amount = 5;
            //查询结果集
            StringList strBusSelectsList = new StringList();
            strBusSelectsList.add(DomainConstants.SELECT_ID);
            strBusSelectsList.add(DomainConstants.SELECT_OWNER);
            strBusSelectsList.add(DomainConstants.SELECT_TYPE);
            strBusSelectsList.add(DomainConstants.SELECT_NAME);
            strBusSelectsList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
            strBusSelectsList.add(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_DATA_PROCESS_PROGRESS));
            String strWhere = String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_JF_DATA_PROCESS_PROGRESS) + "==" + RANGE_PROCESS_PROGRESS_PENDINGCAAPROCESS;
            MapList mapList = DomainObject.findObjects(
                    context,
                    TYPE_JF_DATASOURCE + "," + JF_PLMConstants_mxJPO.TYPE_JFPartList,   //type
                    DomainConstants.QUERY_WILDCARD, //name
                    DomainConstants.QUERY_WILDCARD, //revision
                    DomainConstants.QUERY_WILDCARD, //Owner Pattern
                    VaultUtil.getSearchVaultPattern(context), // Vault Pattern
                    strWhere, //where
                    null,  //Query
                    false, // Expand Type
                    strBusSelectsList, // select
                    amount.shortValue() //limit
            );
            JSONObject returnObj = new JSONObject();
            JSONObject obj = new JSONObject();
            JF_LOGGER.info("mapList1: " + mapList.toString());
            DomainObject domainObject = DomainObject.newInstance(context);
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add("physicalid");
            Iterator iterator = mapList.iterator();
            StringList supplyList = new StringList();
            StringList connIds = new StringList();
            StringList applyIds = new StringList();
            //update by ljr 兼容partList  下的零件的数据转换
            while (iterator.hasNext()) {
                Map map1 = (Map) iterator.next();
                String strName = (String) map1.get(DomainConstants.SELECT_NAME);
                String strId = (String) map1.get(DomainConstants.SELECT_ID);
                String strType = (String) map1.get(DomainConstants.SELECT_TYPE);
                if (JF_PLMConstants_mxJPO.TYPE_JFPartList.equalsIgnoreCase(strType)) {
                    domainObject.setId(strId);
                    MapList partListMapList = domainObject.getRelatedObjects(context,
                            JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, // relationship pattern
                            JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                            typeSelectList,                            // object selects
                            JF_Util_mxJPO.basicRellistSel(), // relationship selects
                            false,                                        // to direction
                            true,                                        // from direction
                            (short) 1,                                    // recursion level
                            "",                // object where clause
                            strWhere,
                            (short) 0
                    );
                    supplyList.addAll((StringList)partListMapList.stream().map(m -> {
                        Map map2 = (Map) m;
                        String physicalid = UIUtil.getValue(map2, "physicalid");
                        String connId = UIUtil.getValue(map2, DomainRelationship.SELECT_ID);
                        String name = strName + "_" + physicalid;
                        connIds.add(connId);
                        return name;
                    }).collect(Collectors.toCollection(StringList::new)));

                } else {
                    supplyList.add(strName);
                }
                applyIds.add(strId);
            }
            obj.put("ApplyNumber", supplyList);
            returnObj.put("data", obj);
            returnObj = NioJDUtils.setSuccessmsg(returnObj);
            JF_LOGGER.info("structureJsonFile--------returnObj:" + returnObj.toString());
            //将这几条数据设置为正在处理
            for (String id : applyIds) {
                domainObject.setId(id);
                domainObject.setAttributeValue(context, ATTRIBUTE_JF_DATA_PROCESS_PROGRESS, RANGE_PROCESS_PROGRESS_CURRENTCAAPROCESS);
            }
            DomainRelationship domainRelationship;
            for (String connId : connIds) {
                domainRelationship = DomainRelationship.newInstance(context, connId);
                domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_DATA_PROCESS_PROGRESS, RANGE_PROCESS_PROGRESS_CURRENTCAAPROCESS);
            }
            return returnObj;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
    * PLM和CAA数据数模转换完成接口
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/7/17 11:09
    * @description CAA完成外发数据申请单的数模转换，返回完成信息
    */
    public JSONObject conversionComplete(Context context, String[] args) throws Exception {
        try {
            JSONObject resultJson = new JSONObject();
            JSONObject returnObj = new JSONObject();
            Map<Object,Object> map = JPO.unpackArgs(args);
            JF_LOGGER.info("@@@@@@@@@@@@@@@map:{}", map);
            //获取返回值
            String strApplyNumber = (String) map.get("ApplyNumber");
            String partDir = strApplyNumber;
            String strStatus = (String)map.get("status");
            String strReason = (String)map.get("msg");
            String partPhysicalId = DomainConstants.EMPTY_STRING;
            //update by ljr 兼容partList
            if (strApplyNumber.contains("_")) {
                //partList
                String[] split = strApplyNumber.split("_");
                strApplyNumber  = split[0];
                partPhysicalId = split[1];
            }
            //找到数据外发单 或者partList
            String objectId = JF_PublicMethodClass_mxJPO.findObject(context, TYPE_JF_DATASOURCE + "," + JF_PLMConstants_mxJPO.TYPE_JFPartList, "name=='" + strApplyNumber + "'");
            if (UIUtil.isNullOrEmpty(objectId)) {
                resultJson = NioJDUtils.setSuccessmsg(returnObj);
                return resultJson;
            }
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add("physicalid");
            String type = domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
            Map mapListGroupingMap = new HashMap<String, Object>();
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
            int partSize = 0;
            if (JF_PLMConstants_mxJPO.TYPE_JFPartList.equalsIgnoreCase(type)) {
                MapList partListMapList = domainObject.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, // relationship pattern
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                        typeSelectList,                            // object selects
                        basicRellistSel, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 1,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0
                );
                partSize = partListMapList.size();
                //根据物理id分组
                mapListGroupingMap = JF_NewECRService_mxJPO.getMapListGroupingMap(context, partListMapList, "physicalid");
            }
            DomainRelationship domainRelationship;
            if ("false".equalsIgnoreCase(strStatus)) {
                //转换失败 删除申请单目录
                //将申请单的进度改为待CAA处理
                JF_LOGGER.info("转换失败，处理降级！！！！！！！！！！！！！");
                if (TYPE_JF_DATASOURCE.equalsIgnoreCase(type)) {
                    //如果找到设置属性
                    //更改数据清单处理进度 为 CAA处理失败
                    domainObject.setAttributeValue(context, ATTRIBUTE_JF_DATA_PROCESS_PROGRESS, RANGE_PROCESS_PROGRESS_CAAPROCEFAILD);
                    domainObject.setAttributeValue(context, ATTRIBUTE_JF_REASON, strReason);
                } else {
                    if (mapListGroupingMap.containsKey(partPhysicalId)) {
                        List relIdList = (List) mapListGroupingMap.get(partPhysicalId);
                        Map map1 = (Map) relIdList.get(0);
                        String connId = UIUtil.getValue(map1, DomainRelationship.SELECT_ID);
                        domainRelationship = DomainRelationship.newInstance(context, connId);
                        domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_DATA_PROCESS_PROGRESS, RANGE_PROCESS_PROGRESS_CAAPROCEFAILD);
                        domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_REASON, strReason);
                    }
                }
            } else {
                //转换成功   压缩  打包 发邮件
                JF_LOGGER.info("转换成功，开始压缩打包发邮件");
                String fileName = DomainConstants.EMPTY_STRING;
                if (TYPE_JF_DATASOURCE.equalsIgnoreCase(type)) {
                    //如果找到设置属性
                    fileName = domainObject.getAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE);
                    fileName = UIUtil.isNullOrEmpty(fileName) ? domainObject.getInfo(context, DomainConstants.SELECT_NAME) : fileName;
                    //更改数据清单处理进度 为 CAA处理完成
                    domainObject.setAttributeValue(context, ATTRIBUTE_JF_DATA_PROCESS_PROGRESS, RANGE_PROCESS_PROGRESS_CAAPROCESSINGCOMPLETE);
                    domainObject.setAttributeValue(context, ATTRIBUTE_JF_REASON, "");
                } else {
                    //零件
                    domainObject.setId(partPhysicalId);
                    fileName = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER);
                    if (mapListGroupingMap.containsKey(partPhysicalId)) {
                        List relIdList = (List) mapListGroupingMap.get(partPhysicalId);
                        Map map1 = (Map) relIdList.get(0);
                        String connId = UIUtil.getValue(map1, DomainRelationship.SELECT_ID);
                        domainRelationship = DomainRelationship.newInstance(context, connId);
                        domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_DATA_PROCESS_PROGRESS, RANGE_PROCESS_PROGRESS_CAAPROCESSINGCOMPLETE);
                        domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_REASON, "");
                    }
                    strApplyNumber = partDir;
                }
                //当没有数模和转换图纸的时候  下载完成
                //进行打包
                String strBasicUrl = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{STRING_FILE_SHARE_PATH});
                String dirOne = strBasicUrl + strApplyNumber + File.separator + "TransverterV5File";
                String dirTwo = strBasicUrl + strApplyNumber + File.separator + "OtherDoc";
                String fileSendData = strBasicUrl + strApplyNumber + File.separator + STRING_SEND_DATA;
                String[] directories = {dirOne, dirTwo};
                String outputDirectory = fileSendData;
                if (JF_PLMConstants_mxJPO.TYPE_JFPartList.equalsIgnoreCase(type)) {
                    //partList 打包
                    fileName = fileName.replaceAll("/", "|");
                    fileName = fileName.replaceAll("\\\\", "|");
                    JF_ZipCompressor_mxJPO.createSplitZip(directories, outputDirectory + File.separator + fileName);
//                    JF_ZipCompressor_mxJPO.createSplitZip(directories, outputDirectory + File.separator + fileName);
                    JF_LOGGER.info("转换成功, strApplyNumber:{}", strApplyNumber);
                } else {
                    //数据外发 打包发邮件
                    //todo 打包待处理
                    Boolean aBoolean = JF_DataOutSource_mxJPO.PackageCompressAndSendEmails(context, directories,
                            outputDirectory, fileName,
                            new String[]{objectId, domainObject.getAttributeValue(context, ATTRIBUTE_JF_CODE),
                                    domainObject.getAttributeValue(context, ATTRIBUTE_JF_SECRET)});
                    if (aBoolean) {
                        //更改数据清单处理进度 已发送邮件
                        domainObject.setAttributeValue(context, ATTRIBUTE_JF_DATA_PROCESS_PROGRESS, RANGE_PROCESS_PROGRESS_SEND_EMAIL);
                    }
                }
            }
            //检测partList中的零件是否转换完成
            if (JF_PLMConstants_mxJPO.TYPE_JFPartList.equalsIgnoreCase(type)) {
                JF_LOGGER.info("!!!!!!!!!!!!!!!!!!!!!!!!!");
                JF_LOGGER.info("!!!!!!!!!!!!!!!!!!!!!!!!!");
                JF_LOGGER.info("partSize:{}", partSize);
                domainObject.setId(objectId);
                MapList partListMapList = domainObject.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.rel_JFPartList2VPMReference, // relationship pattern
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                        typeSelectList,                            // object selects
                        basicRellistSel, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 1,                                    // recursion level
                        "",                // object where clause
                        JF_PLMConstants_mxJPO.SELECT_ATTR_JSdataProcessProgress + "=="
                                + JF_PLMConstants_mxJPO.RANGE_PROCESS_PROGRESS_CAAPROCESSINGCOMPLETE
                                + "||"
                                + JF_PLMConstants_mxJPO.SELECT_ATTR_JSdataProcessProgress + "=="
                                + JF_PLMConstants_mxJPO.RANGE_PROCESS_PROGRESS_CAAPROCEFAILD
                                + "||"
                                + JF_PLMConstants_mxJPO.SELECT_ATTR_JSdataProcessProgress + "=="
                                + JF_PLMConstants_mxJPO.RANGE_PROCESS_PROGRESS_OUTSOURCEDATACOMPLETE,
                        (short) 0
                );
                JF_LOGGER.info("partListMapList:{}", partListMapList);
                if (partListMapList.size() == partSize) {
                    //修改partList的属性
                    JF_LOGGER.info("修改partList的属性");
                    domainObject.setAttributeValue(context, ATTRIBUTE_JF_DATA_PROCESS_PROGRESS, RANGE_PROCESS_PROGRESS_CAAPROCESSINGCOMPLETE);
                }
            }
            resultJson = NioJDUtils.setSuccessmsg(returnObj);
            return resultJson;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
    * 返回错误信息
    * @param sResult
	* @param msg
    * @author LIUJR
    * @throws
    * @return com.alibaba.fastjson.JSONObject
    * @date 2024/8/13 13:35
    * @description
    */
    public static JSONObject setErrormsg(JSONObject sResult, String msg) {
        sResult.put("code", "500");
        sResult.put("msg", msg);
        sResult.put("status", "false");
        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        String methodName = "";
        if(stackTrace.length>1) {
            methodName = stackTrace[1].getMethodName();
        }
        JF_LOGGER.info(" end gate Service process method "+methodName+" end "+sResult.toString());
        return sResult;
    }


    public JSONObject queryProjectInfo(Context context, String[] args) throws Exception{
        JSONObject result = new JSONObject();
        result = JPO.invoke(context, "JF_ProjectSpace", null, "getProjectInfoInterface",  args, JSONObject.class);
        return result;
    }
    public JSONObject queryProjectTaskInfo(Context context, String[] args) throws Exception{
        JSONObject result = new JSONObject();
        result = JPO.invoke(context, "JF_ProjectSpace", null, "getProjectTaskInfoInterface",  args, JSONObject.class);
        return result;
    }

    public JSONObject syncMBOMToMDM(Context context, String[] args) throws Exception{
        JSONObject result = new JSONObject();
        result = JPO.invoke(context, "JF_ExportMBOM", null, "getMBOMInfoInterface",  args, JSONObject.class);
        return result;
    }

    public JSONObject queryProjectESOTaskInfo(Context context, String[] args) throws Exception{
        JSONObject result = new JSONObject();
        result = JPO.invoke(context, "JF_ProjectSpace", null, "getAllESOInfoByALLProject",  args, JSONObject.class);
        return result;
    }
    public JSONObject syncPurchaseInfoByBI(Context context, String[] args) throws Exception{
        JSONObject result = new JSONObject();
        result = JPO.invoke(context, "JF_ProjectSpace", null, "getPurchaseInfoByBI",  args, JSONObject.class);
        return result;
    }

    public JSONObject syncPartListToSRM(Context context, String[] args) throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String pratlistid = (String) programMap.get("ID");
        JSONObject result = new JSONObject();
        result = JPO.invoke(context, "JF_ProjectSpace", null, "getPartListinfoSendSRM",  new String[]{pratlistid,"command"}, JSONObject.class);
        //调用通知

        return result;
    }

    /**
     * @Author Liuxg
     * @Description CAA查询供货件接口
     * @Date 2025/12/24 11:55 
     * @Param [context, args]
     * @return com.alibaba.fastjson.JSONObject
    **/
    public JSONObject getGCList(Context context,String[] args)throws Exception{
        JSONObject result =new JSONObject();
        JSONArray allidlist=new JSONArray();
        try {
            long currentTime= System.currentTimeMillis();

            ContextUtil.pushContext(context);
//            LocalDate today = LocalDate.now();
//            LocalDateTime startOfDay = today.atStartOfDay();
//
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
//            String startStr = startOfDay.format(formatter);
//
//            JF_LOGGER.info("Start of Day: " + startStr);
//
            StringBuffer modtimebtr=new StringBuffer();
//            modtimebtr.append(startStr).append(" ").append("0:00:00 AM");


            LocalDateTime now = LocalDateTime.now();
            LocalDateTime pastTime = now.minusHours(7);
            String startStr = pastTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a"));
            JF_LOGGER.info("Start of Day: " + startStr);
            modtimebtr.append(startStr);

            StringList relList = JF_Util_mxJPO.basicRellistSel();
            StringList selList = JF_Util_mxJPO.basicBolistSel();

            selList.add(DomainConstants.SELECT_MODIFIED);
            selList.add("attribute[PLMEntity.PLM_ExternalID]");
            MapList mlProject = DomainObject.findObjects(context, "Project Space", // type filter
                    null, // vault filter
                    "", // where clause
                    selList);

            String modtime="'modified' >= '"+modtimebtr.toString()+"'&& (current == 'FROZEN'||current == 'RELEASED')";
//            String modtime="'modified' >= '12/23/2025 0:00:00 AM' && (current == 'FROZEN'||current == 'RELEASED')";
            JF_LOGGER.info("modtime: " + modtime);
            JF_LOGGER.info("mlProject size:{}",mlProject.size());

            for(int i=0;i<mlProject.size();i++){
                Map promap= (Map) mlProject.get(i);

                String proid= (String) promap.get(DomainConstants.SELECT_ID);
               // JF_LOGGER.info("proid:{}",proid);
                DomainObject projectObject=DomainObject.newInstance(context,proid);

                MapList partMapList = projectObject.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.rel_JFProject2RootPart, //pattern to match relationships
                        JF_PLMConstants_mxJPO.TYPE_VPMReference, //pattern to match types
                        selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                        relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                        false, //get To relationships
                        true, //get From relationships
                        (short) 1, //the number of levels to expand, 0 equals expand all.
                        modtime, //where clause to apply to objects, can be empty ""
                        "attribute[JFZeroPart]==Y", //where clause to apply to relationship, can be empty ""
                        (short) 0);//limit
                JF_LOGGER.info("partMapList:{}",partMapList.size());
//                StringList partList = (StringList) partMapList.stream().map(m -> {
//                    Map map = (Map) m;
//                    return UIUtil.getValue(map, SELECT_ID);
//                }).collect(Collectors.toCollection(StringList::new));

                for(int j=0;j<partMapList.size();j++){
                    Map map = (Map) partMapList.get(j);
                    String name= (String) map.get(DomainConstants.SELECT_NAME);
                    String id= (String) map.get(DomainConstants.SELECT_ID);
                    String rev= (String) map.get(DomainConstants.SELECT_REVISION);
                    String PLM_ExternalID= (String) map.get("attribute[PLMEntity.PLM_ExternalID]");
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("name",name);
                    jsonObject.put("id",id);
                    jsonObject.put("revision",rev);
                    jsonObject.put("PLM_ExternalID",PLM_ExternalID);

                    allidlist.add(jsonObject);
                }

//                allidlist.addAll(partList);
            }


            result.put("msg","Success");
            result.put("code","0000");
            result.put("status","true");
            result.put("data",allidlist);

        }catch (Exception e){
            e.printStackTrace();
            result.put("msg","Please contact the administrator Error message:The key does not match!");
            result.put("code","500");
            result.put("status","false");

        }finally {
            ContextUtil.popContext(context);
        }
        JF_LOGGER.info("result: " + result);
        return result;
    }


    public String removePCRExecuteTaskDoc(Context context,String []args)throws Exception{
        Gson gson = new Gson();
        Map reqMap = (Map) JPO.unpackArgs(args);
        Map resMap = new HashMap();

        //
        String objectId = (String) reqMap.get("objectId");
        StringList deleteIds=new StringList();
        deleteIds.add(objectId);
        String[] stringArray = deleteIds.toStringArray();

        DomainObject.deleteObjects(context, stringArray);

        resMap.put("code","200");
//        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Button.UpdateProjectSuccess");
        String strMess = "delete success";

        resMap.put("mess",strMess);
        String strResHtml = gson.toJson(resMap);
        return strResHtml;
    }


}
