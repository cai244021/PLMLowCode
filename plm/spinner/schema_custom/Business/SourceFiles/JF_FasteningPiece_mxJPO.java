import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.PostProcessCallable;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.library.LibraryCentralConstants;
import com.matrixone.apps.library.LibraryUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Page;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @ClassName JF_FasteningPiece_mxJPO
 * @Author: LIUJR
 * @CreateDate: 2024/7/30 9:37
 * @UpdateRemark:
 * @Version: 1.0
 * @Description: 紧固件处理
 */
public class JF_FasteningPiece_mxJPO  implements LibraryCentralConstants{
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_FasteningPiece_mxJPO.class);
    private static final String STRING_MQL_ATTRIBUTE = "attribute[%s].value";
    private static final String STRING_MQL_RELATIONSHIP_FROM = "from[%s].to.%s";
    private static final String STRING_MQL_RELATIONSHIP_TO = "to[%s].from.%s";
    private static final String STRING_SIGN_TASK_PROPERTIES_ZH = "SignTaskProperties_zh.xml";
    private static final String STRING_FASTENER_ATTRIBUTE = "JF_VPMReference.JF_Material,JF_VPMReference.JF_Weight,Fastener Specifications,strengthgrade,Surface Treatment,Salt Spray Test Time,Beatle,IS Anti Loosening Adhesive,length,Step Height,Grip Range,Matching Hole Dameter,Inside Diameter,Outside Diameter,Match The Thickness of the Structure";
    private static final String STRING_ELETRIC_ATTRIBUTE = "EPolarity,PinN,Diam,Supplier,Waterproofness";
    private static final String ROLE_JF_STANDARD_ADMIN = "JfStandardAdmin";
    private static final String STATE_DRAFT = "Draft";
    private static final String STATE_IN_WORK = "IN_WORK";
    public static final String PROPNAME_START_DELIMITER  = "[";
    public static final String PROPNAME_END_DELIMITER    = "]";
    public static final String RESULT_DELIMITER          = " =";
    public static final String RANGE_START_DELIMITER     = "[";
    public static final String UOM_ASSOCIATEDWITHUOM     = "AssociatedWithUOM";
    public static final String DB_UNIT                   = "DB Unit";
    public static final String UOM_UNIT_LIST             = "DB UnitList";
    /**
    * 紧固件从冻结提升到发布
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/30 11:10
    * @description
    */
    public Boolean FasteningPiecePromote(Context context, String[] args) throws Exception{
        Boolean flag = Boolean.TRUE;
        try{
            Map paramMap = (Map) JPO.unpackArgs(args);
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            StringList inWorkCurrentList = (StringList) paramMap.get("inWorkCurrentList");
            StringList objectIdList = (StringList) paramMap.get("objectIdList");
            //将工作中得提升到冻结
            jfUtilMxJPO.promoteVPM(context, inWorkCurrentList, "ToFreeze");
            //先提交冻结再到发布
            //将所有得提升到发布
            jfUtilMxJPO.promoteVPM(context, objectIdList, "ToRelease");
        }catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
        }
        return flag;
    }

    /**
     * 标准件的属性必填校验，在标准件发布的时候校验---根据标准件入库来决定校验那些属性必填
     * 电器件 、 紧固件 、 发泡标准件 、 面套标准件、 公共辅料件
     *
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2024/9/11 17:40
     * @description
     */
    public String partsReleaseVerification(Context context, String[] args) throws Exception{
        String strMess = DomainConstants.EMPTY_STRING;
        try {
            Map paramMap = (Map) JPO.unpackArgs(args);
            MapList mapList = (MapList) paramMap.get("mapList");
            JF_DR_mxJPO jfDrMxJPO = new JF_DR_mxJPO();
            StringBuffer stringBuffer = new StringBuffer();
            DomainObject domainObject = DomainObject.newInstance(context);
            HashSet checkPSList = new HashSet(); //无项目的零件name
            HashSet checkDrwList = new HashSet(); //无图纸的零件name
            StringList messList = new StringList();
            for (int i = 0; i < mapList.size(); i++) {
                Map map = (Map) mapList.get(i);
                String strPartId = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                domainObject.setId(strPartId);
                Map partMap = domainObject.getInfo(context, StringList.create(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN,JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER,SELECT_NAME,SELECT_TYPE));
                String  strPartType = (String)partMap.get(SELECT_TYPE);
                //只能是物理产品才走此逻辑
                if (JF_PLMConstants_mxJPO.TYPE_VPMReference.equals(strPartType)){
                    String strTempMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.XENWidgetMess.CheckRequireAttr");
                    String  strDetailCN = (String)partMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_Detail_CN);
                    String  strPartNum = (String)partMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                    JF_LOGGER.info("strDetailCN:{}",strDetailCN);
                    JF_LOGGER.info("strPartId:{}",strPartId);
                    if (UIUtil.isNullOrEmpty(strPartNum)){
                        strPartNum = (String)partMap.get(SELECT_NAME);
                    }
                    StringList requireAttribute = new StringList();
                    if(UIUtil.isNullOrEmpty(strDetailCN)) {
                        requireAttribute.add("JF_VPMReference.JF_Detail_CN");
                    }else {
                        requireAttribute = jfDrMxJPO.getRequireAttribute(context, strPartId, strDetailCN, new StringList());
                        //校验客户零件号/DB信息属性
                        String projectId = new JF_Util_mxJPO().getPartBelongProject(context, new String[]{strPartId});
                        jfDrMxJPO.checkDRCustomerPartsDBRequireAttribute(context, strPartId, strDetailCN, requireAttribute, projectId);
                        if (requireAttribute.contains("connectdrw")) {
                            checkDrwList.add(strPartNum);
                            requireAttribute.remove("connectdrw");
                        }
                    }
                    JF_LOGGER.info("requireAttribute:{}",requireAttribute);
                    StringList nlsAttrList = new StringList(requireAttribute.size());
                    if (requireAttribute.size() > 0){
                        for (int j = 0; j < requireAttribute.size(); j++) {
                            nlsAttrList.add(EnoviaResourceBundle.getAttributeI18NString(context,requireAttribute.get(j),context.getLocale().getLanguage()));
                        }
                    }
                    if (nlsAttrList.size() > 0){
                        String strPartMess = strTempMess.replace("{0}",":" + nlsAttrList.join(",")).replace("{1}",  strPartNum);
                        JF_LOGGER.info("strPartMess:{}",strPartMess);
                        messList.add(strPartMess);
                    }
                    JF_LOGGER.info("messList:{}",messList);
                    String hasProjectSpace = domainObject.getInfo(context, "to[JFProject2RootPart]");
                    JF_LOGGER.info("hasProjectSpace:{}",hasProjectSpace);
                    if ("FALSE".equalsIgnoreCase(hasProjectSpace)) {
                        checkPSList.add(strPartNum);
                    }
                }
            }
            if (!messList.isEmpty()) {
                strMess = messList.join(";");
            }
            if (!checkPSList.isEmpty()) {
                //无项目的情况
                String mess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponent.Common.VPMNoPS");
                strMess = UIUtil.isNullOrEmpty(strMess) ? strMess : strMess + ";";
                stringBuffer.append(mess);
                stringBuffer.append(":");
                stringBuffer.append(StringList.create(checkPSList).join(","));
                strMess += stringBuffer.toString();
            }
            if (!checkDrwList.isEmpty()) {
                strMess = UIUtil.isNullOrEmpty(strMess) ? strMess : strMess + ";";
                String strTempMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.DRCheckVPM.DRW.MESS");
                strMess += strTempMess + ":" + StringList.create(checkDrwList).join(",");
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        if (UIUtil.isNullOrEmpty(strMess)) {
            strMess = "success";
        }
        JF_LOGGER.info("strMess:{}",strMess);
        return strMess;
    }

    /**
    * 标准件的属性必填校验，在标准件发布的时候校验---根据标准件入库来决定校验那些属性必填
     * 电器件 、 紧固件
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/9/11 17:40
    * @description
    */
    public String verifyPartAttributesBased(Context context, String[] args) {
        String strMess = DomainConstants.EMPTY_STRING;
        try {
            Map paramMap = (Map) JPO.unpackArgs(args);
            MapList mapList = (MapList) paramMap.get("mapList");
            DomainObject domainObject = DomainObject.newInstance(context);
            Map groupMap = (Map) mapList.stream().collect(Collectors.groupingBy(m -> {
                Map info = (Map) m;
                return info.get("attribute[JF_VPMReference.JF_PartType].value");
            }));
            JF_LOGGER.info("分组数据：{}",groupMap.toString());
            String fastenerMess = DomainConstants.EMPTY_STRING;
            HashSet checkPSList = new HashSet(); //无项目的零件name
            if (groupMap.containsKey("F")) {
                StringList fastenerIdList = new StringList();
                List fastenerList = (List) groupMap.get("F");
                Set fastenerIdSet = (Set) fastenerList.stream().map(m -> {
                    Map info = (Map) m;
                    return info.get(DomainConstants.SELECT_ID);
                }).collect(Collectors.toSet());
                fastenerIdList = StringList.create(fastenerIdSet);
                if (!fastenerIdList.isEmpty()) {
                    Map<String, Object> paramsMap = new HashMap<>();
                    paramsMap.put("objectIdList", fastenerIdList);
                    paramsMap.put("attributes", STRING_FASTENER_ATTRIBUTE);
                    paramsMap.put("configuration", "Fastener");
                    fastenerMess = verifyPartAttributesBasedOnIncomingType(context, JPO.packArgs(paramsMap));
                    //判断紧固件的是否有项目名称
                    for (int i = 0; i < fastenerIdList.size(); i++) {
                        domainObject.setId(fastenerIdList.get(i));
                        String hasProjectSpace = domainObject.getInfo(context, "to[JFProject2RootPart]");
                        if ("FALSE".equalsIgnoreCase(hasProjectSpace)) {
                            checkPSList.add(domainObject.getInfo(context, DomainConstants.SELECT_NAME));
                        }
                    }
                }
            }
            String electricMess = DomainConstants.EMPTY_STRING;
            if (groupMap.containsKey("E")) {
                StringList electricIdList = new StringList();
                List electricList = (List) groupMap.get("E");
                Set electricIdSet = (Set) electricList.stream().map(m -> {
                    Map info = (Map) m;
                    return info.get(DomainConstants.SELECT_ID);
                }).collect(Collectors.toSet());
                electricIdList = StringList.create(electricIdSet);
                if (!electricIdList.isEmpty()) {
                    Map<String, Object> paramsMap = new HashMap<>();
                    paramsMap.put("objectIdList", electricIdList);
                    paramsMap.put("attributes", STRING_ELETRIC_ATTRIBUTE);
                    paramsMap.put("configuration", "electric");
                    electricMess = verifyPartAttributesBasedOnIncomingType(context, JPO.packArgs(paramsMap));
                }
            }
            if (fastenerMess.isEmpty() && electricMess.isEmpty()) {
                strMess = "success";
            } else {
                if (UIUtil.isNotNullAndNotEmpty(fastenerMess)){
                    strMess = fastenerMess;
                }
                if (UIUtil.isNotNullAndNotEmpty(electricMess)) {
                    if (UIUtil.isNotNullAndNotEmpty(strMess)) {
                        strMess += ";";
                    }
                    strMess += electricMess;
                }
            }
            JF_LOGGER.info("strMess:{}", strMess.toString());
            JF_LOGGER.info("checkPSList:{}", checkPSList.toString());
            if (!checkPSList.isEmpty()) {
                //无项目的情况
                String mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Common.VPMNoPS", new String[]{});
                JF_LOGGER.info("mess:{}", mess.toString());
                mess += StringList.create(checkPSList).join(",");
                if (UIUtil.isNotNullAndNotEmpty(strMess)) {
                    strMess += ";" + mess;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        JF_LOGGER.info("strMess:{}", strMess.toString());
        return strMess;
    }

    /**
    * 标准件的属性必填校验，在标准件发布的时候校验---根据标准件入库来决定校验那些属性必填
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2024/9/4 13:36
    * @description
    */
    public static String verifyPartAttributesBasedOnIncomingType(Context context, String[] args) throws Exception{
        String strMess = DomainConstants.EMPTY_STRING;
        try {
            Map paramMap = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("paramMap：{}", paramMap);
            StringList strObjectIdList = (StringList) paramMap.get("objectIdList");
            String attributes = (String) paramMap.get("attributes");
            String configuration = (String) paramMap.get("configuration");
            StringList strBusSelectList = new StringList();
            String strClassTitle = String.format(STRING_MQL_RELATIONSHIP_TO, DomainRelationship.RELATIONSHIP_CLASSIFIED_ITEM, DomainConstants.SELECT_ATTRIBUTE_TITLE);
            strBusSelectList.add(strClassTitle);
            strBusSelectList.add(DomainConstants.SELECT_ID);
            strBusSelectList.add(DomainConstants.SELECT_NAME);
            strBusSelectList.add("attribute[JF_VPMReference.JF_ProcurementType].value");
            strBusSelectList.add("attribute[EnterpriseExtension.V_PartNumber].value");
            strBusSelectList.add("attribute[JF_VPMReference.JF_PartNameCN].value");
            String[] attributeArray = attributes.split(",");
            for (String strAttr : attributeArray) {
                strBusSelectList.add(String.format(STRING_MQL_ATTRIBUTE, strAttr));
            }
            strBusSelectList.add(String.format(STRING_MQL_ATTRIBUTE, "JF_VPMReference.JF_WeightTarget"));
            StringList allPartMessList = new StringList();
            MapList resultMapList = DomainObject.getInfo(context, strObjectIdList.toStringArray(), strBusSelectList);
            resultMapList.stream().forEach(m -> {
                Map map = (Map) m;
                String strPartMess = DomainConstants.EMPTY_STRING;
                String strClassTitleValue = UIUtil.getValue(map, strClassTitle);
                JF_LOGGER.info("@@@@@@@@@@@strClassTitleValue:{}" + strClassTitleValue);
                String strName = UIUtil.getValue(map, DomainConstants.SELECT_NAME);
                //拿去配置文件中的值
                String strAttribute = DomainConstants.EMPTY_STRING;
                try {
                    strAttribute = readPropertiesFile(context, STRING_SIGN_TASK_PROPERTIES_ZH, configuration, strClassTitleValue);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                JF_LOGGER.info("@@@@@@@@@@@strAttribute:{}" + strAttribute);
                StringList partAttrCheckList = new StringList();
                for (String strAttrName : strAttribute.split(",")) {
                    String strAttrValue = UIUtil.getValue(map, String.format(STRING_MQL_ATTRIBUTE, strAttrName));
                    if (UIUtil.isNullOrEmpty(strAttrValue) || ("JF_VPMReference.JF_Weight".equalsIgnoreCase(strAttrName) && "0.0".equalsIgnoreCase(strAttrValue))) {
                        //没有填写
                        try {
                            if ("JF_VPMReference.JF_Weight".equalsIgnoreCase(strAttrName)) {
                                //add by ljr 20250304  当参考重量为空 或者为0.0 的时候，判断实际重量是否为空或为0，如果是，就弹出报错
                                String strWeightTarget = UIUtil.getValue(map, String.format(STRING_MQL_ATTRIBUTE, "JF_VPMReference.JF_WeightTarget"));
                                if (UIUtil.isNullOrEmpty(strWeightTarget) || "0.0".equalsIgnoreCase(strWeightTarget) || "0".equalsIgnoreCase(strWeightTarget)) {
                                    partAttrCheckList.add(EnoviaResourceBundle.getAttributeI18NString(context, strAttrName, context.getLocale().toString()));
                                }
                            } else {
                                partAttrCheckList.add(EnoviaResourceBundle.getAttributeI18NString(context, strAttrName, context.getLocale().toString()));
                            }
                        } catch (MatrixException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                String procurementType = UIUtil.getValue(map, "attribute[JF_VPMReference.JF_ProcurementType].value");
                if (UIUtil.isNullOrEmpty(procurementType)) {
                    try {
                        partAttrCheckList.add(EnoviaResourceBundle.getAttributeI18NString(context, "JF_VPMReference.JF_ProcurementType", context.getLocale().toString()));
                    } catch (MatrixException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (!partAttrCheckList.isEmpty()) {
                    allPartMessList.add(strName + ":" + partAttrCheckList.join(","));
                }
            });
            if (!allPartMessList.isEmpty()) {
                strMess = allPartMessList.join(";");
            } else {
                strMess = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
        JF_LOGGER.info("@@@@@@@@@@@strMess:{}" + strMess);
        return strMess;
    }


    /**
    * 拿取xml的内容
    * @param context
	* @param pageName
	* @param strConfiguration
	* @param strConfig
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2024/9/4 15:11
    * @description
    */
    public static String readPropertiesFile(Context context, String pageName, String strConfiguration, String strConfig) throws Exception{
        String returnAttribute = DomainConstants.EMPTY_STRING;
        Page pageAttributePopulation = new Page(pageName);
        pageAttributePopulation.open(context);
        String strProperties = pageAttributePopulation.getContents(context);
        pageAttributePopulation.close(context);
        // 使用 ByteArrayInputStream 将字符串转为输入流
        try (InputStream inputStream = new ByteArrayInputStream(strProperties.getBytes("UTF-8"))) {
            // 创建 DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            // 获取所有 config 元素
            NodeList configurationNodes = document.getElementsByTagName("configuration");
            for (int i = 0; i < configurationNodes.getLength(); i++) {
                Element configurationElement = (Element) configurationNodes.item(i);
                System.out.println("configurationElement:" + configurationElement.toString());
                if (strConfiguration.equals(configurationElement.getAttribute("id")) || UIUtil.isNullOrEmpty(strConfiguration)) {
                    // 找到匹配的 configuration, 获取它下的 config 元素
                    NodeList commonNodes = configurationElement.getElementsByTagName("common");
                    if (commonNodes.getLength() > 0) {
                        Element commonElement = (Element) commonNodes.item(0);
                        returnAttribute = commonElement.getAttribute("required");
                    }
                    NodeList configNodes = configurationElement.getElementsByTagName("config");
                    for (int j = 0; j < configNodes.getLength(); j++) {
                        Element configElement = (Element) configNodes.item(j);
                        if (strConfig.equalsIgnoreCase(configElement.getAttribute("name"))) {
                            String required = configElement.getAttribute("required");
                            returnAttribute += returnAttribute.length() > 0 ? "," + required : required;
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnAttribute;
    }

    /**
    * 是紧固件、电器件的发布
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return int
    * @date 2024/9/11 17:42
    * @description
    */
    public int promoteHasClassLibraryCheck(Context context, String[] args) {
        int iReturn = 0;
        String strMess = DomainConstants.EMPTY_STRING;
        try{
            String objectId = args[0];
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String strPartType = domainObject.getAttributeValue(context, "JF_VPMReference.JF_PartType");
            if ("F".equalsIgnoreCase(strPartType)) {
                //是紧固件 必须入库发布
                String strClassId = String.format(STRING_MQL_RELATIONSHIP_TO, DomainRelationship.RELATIONSHIP_CLASSIFIED_ITEM, DomainConstants.SELECT_ID);
                StringList classLibraryList = domainObject.getInfoList(context, strClassId);
                String attributeValue = domainObject.getAttributeValue(context, "EnterpriseExtension.V_PartNumber");
                if (UIUtil.isNullOrEmpty(attributeValue)) {
                    attributeValue = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
                }
                if (classLibraryList.isEmpty()) {
                    //不允许发布
                    iReturn = 1;
                    strMess = attributeValue;
                    strMess += EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.FasteningPiecePromote.HasLibrary");
                }
            }
            if (!strMess.isEmpty()) {
                //紧固件必须入库才能发布
                JF_LOGGER.info("strMess:{}", strMess);
                emxContextUtil_mxJPO.mqlNotice(context, strMess);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return iReturn;
    }

    /**
    * this method provides Dynamic Columns Map to display Classification Attributes in Classified Items Page Dynamically
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.List
    * @date 2024/9/11 17:42
    * @description
    */
    public static List getClassificationAttributesColumns(Context context,String[] args) throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String classObjectId = (String)requestMap.get("objectId");
        DomainObject domainObject = DomainObject.newInstance(context, classObjectId);
        String strClassTitleValue = domainObject.getAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE);
        // 使用 ByteArrayInputStream 将字符串转为输入流
        Page pageAttributePopulation = new Page("PartAttributeProperties.xml");
        pageAttributePopulation.open(context);
        String strProperties = pageAttributePopulation.getContents(context);
        InputStream inputStream = new ByteArrayInputStream(strProperties.getBytes("UTF-8"));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputStream);
        JF_LOGGER.info("strClassTitleValue:{}", strClassTitleValue);
        StringList attrList = JF_DR_mxJPO.findPartAttributeWithXPath(document, "MandatoryAttributePart", strClassTitleValue);
        JF_LOGGER.info("attrList:{}", attrList);
//        String strAttribute = readPropertiesFile(context, STRING_SIGN_TASK_PROPERTIES_ZH, "", strClassTitleValue);
//        JF_LOGGER.info("strAttribute:{}", strAttribute);
        MapList classificationAttributes = getClassClassificationAttributes(context, classObjectId, false);
        String attr = DomainConstants.EMPTY_STRING;
        if (null != attrList){
            attr = attrList.join(",");
        }
        return getDynamicColumnsMapList(context, classificationAttributes, attr);
    }

    /**
     * method to get the MapList of column Maps for dynamic columns for the classification attributes
     * @param context
     * @param classificationAttributes - MapList containing Maps of attribute Details, Each Map Containing attribute details for
     *             keys   - "name", "type", "range"
     *
     * @return List of Column Maps
     * @throws FrameworkException
     */
    protected static List getDynamicColumnsMapList(Context context,MapList classificationAttributes, String strAttribute) throws Exception{
        //Define a new MapList to return.
        MapList columnMapList = new MapList();

        String strLanguage =  context.getSession().getLanguage();
        String classAttributesHeader = EnoviaResourceBundle.getProperty(context, "emxLibraryCentralStringResource", new Locale(strLanguage),"emxLibraryCentral.Common.ClassificationAttributes");

        // attributeAttributeGroupMap contains all the attribute group names to which each attribute belongs
        HashMap attributeAttributeGroupMap = new HashMap();

        for(int i=0;i<classificationAttributes.size();i++){
            HashMap attributeGroup = (HashMap)classificationAttributes.get(i);
            String attributeGroupName = (String)attributeGroup.get("name");
            boolean isVisibleAG = LibraryUtil.isInterfaceVisible(context, attributeGroupName);
            MapList attributes = (MapList)attributeGroup.get("attributes");
            for(int j=0;j<attributes.size();j++){
                HashMap attribute =  (HashMap)attributes.get(j);
                String attributeQualifiedName = (String)attribute.get("qualifiedName");
                String attributeName = (String)attribute.get("name");
                //20260723 update by ljr 库分类Table不再显示客户零件号、客户零件版本属性；
                if (JF_PLMConstants_mxJPO.ATTR_CustomerPartNumber.equals(attributeName)
                        || JF_PLMConstants_mxJPO.ATTR_CustomerPartRevision.equals(attributeName)) {
                    continue;
                }
                String valueType = (String)attribute.get("valuetype");
                HashMap colMap = new HashMap();
                HashMap settingsMap = new HashMap();
                // PSA11 set the auto filter to true for IR-442214-3DEXPERIENCER2018x.
                settingsMap.put("Auto Filter", "true");
                settingsMap.put("Field Type", "attribute");
                if(isVisibleAG) //bae3 hiddenAG
                    settingsMap.put("Group Header",attributeGroupName);
                else
                    settingsMap.put("Group Header", classAttributesHeader); //bae3 hiddenAG
                settingsMap.put("On Change Handler", "reloadDuplicateAttributes");
                settingsMap.put("Editable", "true");
                settingsMap.put("Registered Suite", "LibraryCentral");
                String adminName = UICache.getSymbolicName(context, attributeName, "attribute");
                settingsMap.put("Admin Type", adminName);
                //add by ljr
                if (strAttribute.contains(attributeName)) {
                    settingsMap.put("Required", "true");
                }
                //end

                //KDR2
                String resourceBundle = "emxLibraryCentral";

                // PSA11
                //String editReleasedObject = EnoviaResourceBundle.getProperty(context,resourceBundle, new Locale(strLanguage),"emxLibraryCentral.EditObject.EditReleasedObject");
                String editReleasedObject = EnoviaResourceBundle.getProperty(context, "emxLibraryCentral.EditObject.EditReleasedObject");
                // PSA11

                if(editReleasedObject.equalsIgnoreCase("true")){
                    settingsMap.put("Update Program", "emxLibraryCentralCommon");
                    settingsMap.put("Update Function", "updateAttributeValues");
                }
                //END

                //format date field
                String attributeType = (String)attribute.get("type");
                if(attributeType.equals("timestamp")){
                    settingsMap.put("format", "date");
                    settingsMap.put("Sort Type", "date");
                }

                //format numeric field
                if(attributeType.equals("integer") || attributeType.equals("real")){
                    colMap.put("sorttype", "numeric");

                    // changes added by DMA8 for IR-591119-3DEXPERIENCER2019x start...
                    // if value type is range value for integer and real types, setting validate method and update function
                    if(valueType.equals("rangeval")) {
                        settingsMap.put(SETTING_VALIDATE, "checkRangeValue");
                        settingsMap.put(SETTING_UPDATE_PROGRAM,"emxLibraryCentralCommon");
                        settingsMap.put(SETTING_UPDATE_FUNCTION,"UpdateRangeValueFunction");
                    }
                    // changes added by DMA8 for IR-591119-3DEXPERIENCER2019x end.
                }

                //if range is defined , display as combobox
                MapList range = (MapList)attribute.get("range");

                // changes added by DMA8 for IR-581915-3DEXPERIENCER2019x start...
                // if range is finite for integer and real types, setting input type to combobox else to textbox
                if(range != null){
                    boolean isFinite = false;
                    for(int k=0;k<range.size();k++){
                        HashMap rangeMap =  (HashMap)range.get(k);
                        String operator = (String)rangeMap.get("operator");
                        String value = (String)rangeMap.get("value");
                        if(operator.equals("=")){
                            isFinite = true;
                            break;
                        }
                    }
                    if(attributeType.equals(FORMAT_INTEGER)){
                        if(isFinite){
                            settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_COMBOBOX);
                        } else{
                            settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_TEXTBOX);
                        }
                    } else if(attributeType.equals(FORMAT_REAL)){
                        if(isFinite){
                            settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_COMBOBOX);
                        } else{
                            settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_TEXTBOX);
                        }
                    } else{
                        settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_COMBOBOX);
                    }
                }
                // changes added by DMA8 for IR-581915-3DEXPERIENCER2019x end.

                //format boolean field
                if(attributeType.equals("boolean")){
                    if(range == null){
                        settingsMap.put("Input Type", "combobox");
                        settingsMap.put("Range Program", "emxLibraryCentralClassificationAttributes");
                        settingsMap.put("Range Function", "getRangeValuesForBooleanAttributes");
                    }
                }

                String isMultiline=(String)attribute.get("multiline");
                if (BOOLEAN_TRUE.equalsIgnoreCase(isMultiline)) {
                    settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_TEXTAREA);
                }

                //apply UOM details (if Present)
                if(UOMUtil.isAssociatedWithDimension(context, attributeQualifiedName)) {
                    StringList listUOMunits = UOMUtil.getDimensionUnits(context, attributeQualifiedName);
                    String sDBunit = UOMUtil.getDBunit(context, attributeQualifiedName);
                    colMap.put(UOM_ASSOCIATEDWITHUOM, "true");
                    colMap.put(DB_UNIT, sDBunit);
                    colMap.put(UOM_UNIT_LIST, listUOMunits);
                }else {
                    colMap.put(UOM_ASSOCIATEDWITHUOM, "false");
                }

                colMap.put("name",attributeGroupName+ LibraryCentralConstants.IPC_DELIMITER_PIPE+attributeQualifiedName);

                String name = (String)attribute.get("name");
                String attributeProperty = "emxFramework.Attribute." + name.replaceAll(" ", "_");
                String attributeNameFromPropertiesFile = EnoviaResourceBundle.getProperty(context,
                        "emxFrameworkStringResource",new Locale(strLanguage),attributeProperty);
                if(attributeNameFromPropertiesFile.contains("emxFramework.Attribute.")){
                    attributeNameFromPropertiesFile = name;
                }
                colMap.put("label",attributeNameFromPropertiesFile);
                colMap.put("expression_businessobject","attribute["+attributeQualifiedName+"].value");
                colMap.put("settings",settingsMap);
                columnMapList.add(colMap);

                String attributeGroupNames = (String)attributeAttributeGroupMap.get(attributeQualifiedName);
                if(attributeGroupNames == null){
                    attributeAttributeGroupMap.put(attributeQualifiedName, attributeGroupName);
                }else{
                    attributeAttributeGroupMap.put(attributeQualifiedName, attributeGroupNames + "|" + attributeGroupName);
                }
            }
        }

        //update "AttributeGroups" and "Mass Update" settings
        // Mass update should be false for duplicate attributes
        // Attribute Groups information will be used for reloading the duplicate values
        Iterator itr = columnMapList.iterator();
        while(itr.hasNext()){
            HashMap colMap = (HashMap)itr.next();
            HashMap settingsMap = (HashMap)colMap.get("settings");
            String columnName = (String)colMap.get("name");
            String attributeGroupName = columnName.substring(0,columnName.indexOf(LibraryCentralConstants.IPC_DELIMITER_PIPE));
            String attributeName = columnName.substring(columnName.indexOf(LibraryCentralConstants.IPC_DELIMITER_PIPE)+LibraryCentralConstants.IPC_DELIMITER_PIPE.length());

            String allAttributeGroupsNames = (String)attributeAttributeGroupMap.get(attributeName);
            settingsMap.put("AttributeGroups",allAttributeGroupsNames);
            if(allAttributeGroupsNames.indexOf('|') == -1){
                allAttributeGroupsNames += "|";
            }
            if(!(allAttributeGroupsNames.startsWith(attributeGroupName+"|"))){
                //except for the First attribute group , add Mass Update = fasle setting
                settingsMap.put("Mass Update","false");
            }
        }

        return columnMapList;
    }



    /**
     * method to get the classification attributes of all the attribute groups connected to the class
     * @param context the eMatrix <code>Context</code> object
     * @param objectId - objectId of the class whose attributes are to be returned
     * @returns - a MapList of Attributes Groups, each map with following key-Value Pairs
    1) "name" - String containing AttributeGroup name
    2) "attributes" - a MapList of Attributes, each map with following key-Value Pairs
    1) "qualifiedName" - String containing qualified name of the Attribute
    2) "name"          - String containing name of the Attribute
    3) "type"          - String containing type of the Attribute
    4) "default"       - String containing default value of the Attribute
    5) "description"   - String containing description of the Attribute
    6) "maxlength"     - String containing maxlength of the Attribute
    7) "dimension"     - name of the dimension
    8) "range"         - StringList of range Values
    Note: no key-Value pair exists in this map if there is no value for that key
     */
    protected static MapList getClassClassificationAttributes(Context context, String objectId, Boolean hiddenAllowed)throws Exception{
        DomainObject classObj   = new DomainObject(objectId);
        StringList selectables  = new StringList();
        String attribute_mxsysInterface = "attribute["+ATTRIBUTE_MXSYS_INTERFACE+"].value";
        selectables.add(attribute_mxsysInterface);
        Map classInfo = classObj.getInfo(context, selectables);
        String mxsysInterface = (String)classInfo.get(attribute_mxsysInterface);
        if(UIUtil.isNullOrEmpty(mxsysInterface)){
            // if mxsysInterface is empty then there will be no classification attributes associated with this object
            return new MapList();
        }

        StringList slAttributeGroups = new StringList();

        //get all the AttributeGroups of this class using mxsysInterface
        String mqlQuery = "print interface $1 select $2";
        String sAllParentInterfaces = MqlUtil.mqlCommand(context, mqlQuery, mxsysInterface,"allparents.derived");
        // iterate the values and check for Classification Attribute Groups
        // and then form Attribute groups

        HashMap hmAllParentInterfaces = parseMqlOutput(context, sAllParentInterfaces);
        Set setAllParentInterfaces = hmAllParentInterfaces.keySet();
        Iterator itr = setAllParentInterfaces.iterator();
        while(itr.hasNext()){
            String parentInterfaceName   = (String)itr.next();
            HashMap tempParentInterface  = (HashMap)hmAllParentInterfaces.get(parentInterfaceName);
            if(tempParentInterface != null){
                String parentInterfaceDerived = (String)tempParentInterface.get("derived");
                if(!UIUtil.isNullOrEmpty(parentInterfaceDerived) && parentInterfaceDerived.equals("Classification Attribute Groups")){
                    slAttributeGroups.add(parentInterfaceName);
                }
            }
        }

        MapList attributeGroups = new MapList();
        selectables = new StringList();
        selectables.add("type");
        selectables.add("range");
        selectables.add("multiline");
        selectables.add("valuetype");
        selectables.add("default");
        selectables.add("description");
        selectables.add("maxlength");
        selectables.add("dimension");
        selectables.add("property[predicate].value");
        selectables.add("property[ManipulationUnit].value"); // FUN110056

        //for each attribute group
        for(int i=0;i< slAttributeGroups.size();i++){
            HashMap attibuteGroup = new HashMap();
            String attibuteGroupName = (String)slAttributeGroups.get(i);
            attibuteGroup.put("name", attibuteGroupName);
            MapList attributes = getAttributeGroupAttributesDetails(context, attibuteGroupName, selectables, hiddenAllowed);
            attibuteGroup.put("attributes", attributes);
            attributeGroups.add(attibuteGroup);
        }
        return attributeGroups;
    }

    /**
     * this method parse the mql Output of mutiple lines,
     *  each line is of the form
     *  property[propertyName].subProperty = result
     *     where
     *       property      - should be present
     *                     - should not contain characters [ ] . =
     *                     - property in all the lines should be same
     *       propertyName  - should be present
     *                     - may contain . or = characters
     *                     - should not contain characters [ ]
     *       subProperty   - should be present
     *                     - should not contain . or = characters
     *                     - can end with [i] ,where i is 0, 1, 2, 3 ...
     *       result        - may or may not present
     *
     * @param context the eMatrix <code>Context</code> object
     * @param output mql output to be parsed
     * @return a HashMap with following key value pair
     *            key   - propertyName
     *            value - HashMap with following key value pair
     *                      key   - subProperty
     *                      value - String result
     *
     * @throws Exception
     */
    protected static HashMap parseMqlOutput(Context context,String output) throws Exception{
        BufferedReader in = new BufferedReader(new StringReader(output));
        String resultLine;
        HashMap mqlResult = new HashMap();
        while((resultLine = in.readLine()) != null){
            String property = null;
            String propertyName = null;
            String subProperty = null;
            String result = null;

            try{
                //identify property propertyValue subProperty subPropertyValue  - start
                boolean hasRanges = false;
                int propNameStartDelimIndex = resultLine.indexOf(PROPNAME_START_DELIMITER);
                int resultDelimIndex        = resultLine.indexOf(RESULT_DELIMITER);

                property                    = resultLine.substring(0, propNameStartDelimIndex);

                int propNameEndDelimIndex   = resultLine.indexOf(PROPNAME_END_DELIMITER, propNameStartDelimIndex);
                propertyName                = resultLine.substring(propNameStartDelimIndex+1, propNameEndDelimIndex);

                String propertyAndValue     = property + PROPNAME_START_DELIMITER+propertyName+PROPNAME_END_DELIMITER;
                String remainingResultLine  = resultLine.substring(propertyAndValue.length());

                // if remaining result starts with .
                int rangeStartDelimIndex    = remainingResultLine.indexOf(RANGE_START_DELIMITER);
                resultDelimIndex            = remainingResultLine.indexOf(RESULT_DELIMITER);
                if((rangeStartDelimIndex != -1) && (rangeStartDelimIndex < resultDelimIndex)){
                    // if [ exists and comes before = , then anything before [ is the subProperty and subProperty contains range of results
                    subProperty = remainingResultLine.substring(1,rangeStartDelimIndex);
                    // bae3
                    if(subProperty.equals("property"))
                        subProperty = remainingResultLine.substring(rangeStartDelimIndex+1, remainingResultLine.indexOf(PROPNAME_END_DELIMITER));
                    else //bae3
                        hasRanges   = true;
                }else{
                    // else , anything Before = is the subProperty
                    subProperty = remainingResultLine.substring(1,resultDelimIndex);
                }

                result   = remainingResultLine.substring(resultDelimIndex+RESULT_DELIMITER.length());

                property = property.trim();
                result   = result.trim();

                //identify property propertyValue subProperty subPropertyValue  - end

                //start building HashMap
                HashMap hmPropertyName;
                StringList slSubProperty;

                hmPropertyName = (HashMap)mqlResult.get(propertyName);
                if(hmPropertyName == null){
                    hmPropertyName = new HashMap();
                    mqlResult.put(propertyName, hmPropertyName);
                }
                if(hasRanges){
                    slSubProperty = (StringList)hmPropertyName.get(subProperty);
                    if(slSubProperty == null){
                        slSubProperty = new StringList();
                        hmPropertyName.put(subProperty,slSubProperty);
                    }
                    slSubProperty.add(result);
                }else{
                    hmPropertyName.put(subProperty,result);
                }

            }catch(Exception e){
                // if there is exception during parsing a line , proceed to next line
            }
        }

        return mqlResult;
    }

    /**
     * Method to get the Details of all attributes in a Attribute Group
     * @param context the eMatrix <code>Context</code> object
     * @param agName - Attribute Group Name
     * @param selectables - StringList attribute selectables
     * @return MapList containing Maps of attribute Details, Each Map Contains
     *             key   - attribute's selectable, ex:type ,
     *             value - String or StringList Based on the Number of available values for the specified attribute's selectable
     *                     value will be MapList for the key "range" each map containing an operator and a value
     *             keys "name" and "qualifiedName" are included by default even though they are not in selectables
     */
    protected static MapList getAttributeGroupAttributesDetails(Context context,String agName,StringList selectables, Boolean hiddenAllowed)throws Exception{
        StringBuffer cmd = new StringBuffer("print interface \"$1\" select "); // Move select

        selectables.add("owner");
        selectables.add("hidden");

        String[] newArgs = new String[selectables.size()+1];
        newArgs[0] = agName;
        for(int i=0;i<selectables.size();i++){
            cmd.append("\"$"+(i+2)+"\" ");
            newArgs[i+1] = "attribute."+(String)selectables.get(i);
        }

        String result = MqlUtil.mqlCommand(context,cmd.toString(),true,newArgs);

        HashMap hmAllAttributeDetails = parseMqlOutput(context, result);
        MapList agAttributesDetails = new MapList();

        Set setAllAttributeDetails = hmAllAttributeDetails.keySet();
        Iterator itr = setAllAttributeDetails.iterator();
        while(itr.hasNext()){
            String attributeName = (String)itr.next();
            HashMap hmAttributeDetails = (HashMap)hmAllAttributeDetails.get(attributeName);
            if(hmAttributeDetails != null){
                String hidden = (String)hmAttributeDetails.get("hidden");
                if("true".equalsIgnoreCase(hidden) && !hiddenAllowed){
                    continue;
                }
                String owner = (String)hmAttributeDetails.get("owner");
                String qualifiedName = attributeName;
                if(UIUtil.isNotNullAndNotEmpty(owner)){
                    qualifiedName = owner+"."+attributeName;
                }
                hmAttributeDetails.put("qualifiedName", qualifiedName);
                hmAttributeDetails.put("name", attributeName);

                // convert range StringList to MapList
                StringList range = (StringList)hmAttributeDetails.get("range");
                if(range != null){
                    MapList mlRange = new MapList();
                    Iterator<String> rangeItr = range.iterator();
                    while(rangeItr.hasNext()){
                        String rangeItem = rangeItr.next();
                        int rangeValueIndex = rangeItem.indexOf(" ");
                        if(rangeValueIndex == -1){
                            rangeItem = rangeItem + " ";
                            rangeValueIndex = rangeItem.indexOf(" ");
                        }
                        String rangeOperator = rangeItem.substring(0, rangeValueIndex);
                        if(rangeOperator.equals("uses")){
                            rangeOperator = "program";
                            rangeValueIndex = rangeItem.indexOf(" ", rangeValueIndex+1);
                        }

                        String rangeValue = rangeItem.substring(rangeValueIndex+1);

                        HashMap hmRangeItem = new HashMap();
                        hmRangeItem.put("operator", rangeOperator);
                        hmRangeItem.put("value", rangeValue);

                        mlRange.add(hmRangeItem);
                    }
                    hmAttributeDetails.put("range", mlRange);
                }
                agAttributesDetails.add(hmAttributeDetails);
            }
        }

        return agAttributesDetails;
    }



    /**
    * 1. 首页“标准件发布”页签查询当前登录人负责的标准件申请单；
    *     1. 只查询JFSPartsApplication类型；
    *     2. 按owner等于当前登录人过滤；
    *     3. 返回列表表格需要的基础字段、标题、备注、创建时间；
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2026/6/26 15:20
    * @description 标准件申请单首页列表数据查询
    */
    public MapList getMySPartsApplicationList(Context context, String[] args) throws Exception {
        StringList selectList = JF_Util_mxJPO.basicBolistSel();
        selectList.add(DomainConstants.SELECT_ORIGINATED);
        selectList.add(DomainConstants.SELECT_DESCRIPTION);
        selectList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        String strWhere = JF_PublicMethodClass_mxJPO.buildStringInStrings("owner=='", context.getUser(), "'");
        MapList applicationList = DomainObject.findObjects(context,JF_PLMConstants_mxJPO.TYPE_JFS_PARTS_APPLICATION, "*", strWhere, selectList);
        applicationList.sort(DomainConstants.SELECT_ORIGINATED, "descending", "date");
        return applicationList;
    }

    /**
    * 1. 创建标准件申请单后，将创建页面“零件号”字段选择的零件关联到申请单；
    *     1. 从创建后置参数中获取新建申请单ID；
    *     2. 从SPartsPartOID获取多选零件ID；
    *     3. 校验申请单为草稿且当前人是owner；
    *     4. 校验零件为VPMReference、状态为工作中或冻结、未关联其他标准件申请单；
    *     5. 校验通过后建立JFSPartsApplication2VPMReference关系；
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2026/6/26 15:25
    * @description 标准件申请单创建后关联零件
    */
    @PostProcessCallable
    public void createSPartsApplicationPost(Context context, String[] args) throws Exception {
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            Map paramMap = (Map) programMap.get("paramMap");
            Map requestMap = (Map) programMap.get("requestMap");
            String objectIds = (String) requestMap.get("objectIdList");
            String newObjectId = (String) paramMap.get("newObjectId");
            ContextUtil.startTransaction(context, true);
            DomainObject object = DomainObject.newInstance(context, newObjectId);
            object.addRelatedObjects(context,new RelationshipType(JF_PLMConstants_mxJPO.REL_JFS_PARTS_APPLICATION_2_VPM), true, objectIds.split(","));
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
        } finally {
            ContextUtil.popContext(context);
        }
    }

    /**
    * 1. 零件号搜索框include过滤标准件申请单可选择的零件；
    *     1. 只返回VPMReference；
    *     2. 零件状态必须为工作中或冻结；
    *     3. 零件不能已关联任何标准件申请单，不限制申请单状态；
    *     4. 零件Owner必须是当前申请单Owner；
    *     5. 零件必须存在Classified Item入库关系；
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2026/6/26 15:30
    * @description 标准件申请单零件搜索过滤
    */
    public StringList includeSPartsApplicationParts(Context context, String[] args) {
        StringList returnList = new StringList();
        try {
            Map paramMap = JPO.unpackArgs(args);
            String strApplicationId = UIUtil.getValue(paramMap, "objectId");
            if (UIUtil.isNullOrEmpty(strApplicationId)) {
                return returnList;
            }
            DomainObject applicationObj = DomainObject.newInstance(context, strApplicationId);
            String strApplicationOwner = applicationObj.getInfo(context, DomainConstants.SELECT_OWNER);
            StringList selectList = new StringList();
            selectList.add(DomainConstants.SELECT_ID);
            selectList.add("to[" + JF_PLMConstants_mxJPO.REL_JFS_PARTS_APPLICATION_2_VPM + "]");
            selectList.add("to[Classified Item]");
            // update by LIUJR 20260708 添加零件时仅允许选择申请单Owner负责、状态为工作中/冻结、未关联发布申请单且已入库的零件。
            String strWhere = "(current==FROZEN||current==IN_WORK)&&owner=='" + strApplicationOwner + "'";
            MapList partMapList = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.TYPE_VPMReference, "*", strWhere, selectList);
            JF_LOGGER.info("partMapList:{}", partMapList.size());
            returnList = (StringList) partMapList.stream().filter(m -> {
               Map map = (Map) m;
               String strApplicationRel = UIUtil.getValue(map, "to[" + JF_PLMConstants_mxJPO.REL_JFS_PARTS_APPLICATION_2_VPM + "]");
               String strClassifiedRel = UIUtil.getValue(map, "to[Classified Item]");
               if ("FALSE".equalsIgnoreCase(strApplicationRel) && "TRUE".equalsIgnoreCase(strClassifiedRel)) {
                   return true;
               } else {
                   return false;
               }
           }).map(m1 -> {
               Map map1 = (Map) m1;
               return UIUtil.getValue(map1, DomainConstants.SELECT_ID);
           }).limit(10000)   //不能超过16384条数据
                    .collect(Collectors.toCollection(StringList::new));
            JF_LOGGER.info("returnList:{}", returnList.size());
        } catch (Exception e) {
            JF_LOGGER.info(Arrays.toString(e.getStackTrace()));
        }
        //判断是否是标准件
        return returnList;
    }

    /**
    * 1. 标准件申请单Tree中“零件清单”查询关联零件；
	    *     1. 根据当前申请单ID查询JFSPartsApplication2VPMReference关系；
	    *     2. 返回VPMReference对象基础字段和关系ID；
	    *     3. 表格使用既有JFVPMReferenceListTable展示；
	    *     4. 零件子级展开复用DR零件清单的JF_DR:getExpand方法，保持两个页面的展开行为一致；
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2026/6/26 15:35
    * @description 标准件申请单零件清单查询
    */
    public MapList getSPartsApplicationPartList(Context context, String[] args){
        MapList mapList = new MapList();
        try {
            Map paramMap = (Map) JPO.unpackArgs(args);
            String strApplicationId = UIUtil.getValue(paramMap, "objectId");
            StringList selectList = JF_Util_mxJPO.basicBolistSel();
            selectList.add(DomainConstants.SELECT_DESCRIPTION);
	            StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
	            DomainObject applicationObj = DomainObject.newInstance(context, strApplicationId);
	            //20260727 update by ljr 标准件申请单只查询直接关联的顶层零件，子级展开复用DR的JF_DR:getExpand方法；
	            mapList =  applicationObj.getRelatedObjects(context,
	                    JF_PLMConstants_mxJPO.REL_JFS_PARTS_APPLICATION_2_VPM,
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    selectList,
                    relSelectList,
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    (short) 0);
        } catch (Exception e) {
            JF_LOGGER.info(Arrays.toString(e.getStackTrace()));
        }
        return mapList;
    }

    /**
     * Draft promote check trigger   order 99
    * 标准件发布申请单从草稿提升到审批的时候，标准件属性校验
     * 对象：标准间发布申请单
     * 触发trigger： Draft promote check
     * 校验： 申请单中的所有标准件属性是否填写完整
     *       1. 按照现有JFFasteningPiecePromote按钮的校验校验属性必填的逻辑
     *       2. 标准件不校验是否关联2D/3D图纸；
     *       3. 必须有关联零件
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return int
    * @date 2026/6/29 16:12
    * @description
    */
    public int  checkPartsApplicationAttributes(Context context, String[] args){
        int iReturn = 0;
        try {
            String objectId = args[0];
            DomainObject object = DomainObject.newInstance(context, objectId);
            String strOwner = object.getInfo(context, DomainConstants.SELECT_OWNER);
            String strLineManagerId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, strOwner);
            if (UIUtil.isNullOrEmpty(strLineManagerId)) {
                String strTempMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.JFSPartsApplication.LineManagerNotFound");
                emxContextUtil_mxJPO.mqlNotice(context, strTempMess);
                return 1;
            }
            MapList mapList = object.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JFS_PARTS_APPLICATION_2_VPM + "," + JF_PLMConstants_mxJPO.REL_Instance,
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    JF_Util_mxJPO.basicBolistSel(),
                    JF_Util_mxJPO.basicRellistSel(),
                    false,
                    true,
                    (short) 0,
                    "current=='IN_WORK' || current=='FROZEN'",
                    "",
                    (short) 0);
            if (mapList.isEmpty()) {
                String strTempMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Application.PromoteMessage");
                emxContextUtil_mxJPO.mqlNotice(context, strTempMess);
                return 1;
            }
            StringList selectList = JF_Util_mxJPO.basicBolistSel();
            selectList.add("attribute[JF_VPMReference.JF_PartType]");
            selectList.add("attribute[JF_VPMReference.JF_PartSubType]");
            selectList.add("attribute[JF_VPMReference.JF_Detail_CN]");
            selectList.add("attribute[EnterpriseExtension.V_PartNumber]");
            selectList.add("to[Classified Item].from.attribute[Title]");
            JF_FasteningPiece_mxJPO jfFasteningPieceMxJPO = new JF_FasteningPiece_mxJPO();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mapList", mapList);
            paramMap.put("objectId", "");
            //方法新增图纸校验
            String strResultMess = jfFasteningPieceMxJPO.partsReleaseVerification(context, JPO.packArgs(paramMap));
            if (!"success".equalsIgnoreCase(strResultMess)) {
                emxContextUtil_mxJPO.mqlNotice(context, strResultMess);
                return 1;
            }
        }catch (Exception e) {
            JF_LOGGER.info(Arrays.toString(e.getStackTrace()));
            iReturn = 1;
        }
        return iReturn;
    }

    /**
    * Draft promote action trigger order 1
    * 标准件发布申请单从草稿提交审批时创建直线经理审批流程
    * 取申请单Owner的直线经理作为审批人，流程通过后推动申请单进入Finished
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2026/7/8 14:00
    * @description
    */
    public void createSPartsApplicationReviewRoute(Context context, String[] args) throws Exception {
        try {
            String objectId = args[0];
            DomainObject object = DomainObject.newInstance(context, objectId);
            String strOwner = object.getInfo(context, DomainConstants.SELECT_OWNER);
            String strLineManagerId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, strOwner);
            if (UIUtil.isNullOrEmpty(strLineManagerId)) {
                String strTempMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.JFSPartsApplication.LineManagerNotFound");
                emxContextUtil_mxJPO.mqlNotice(context, strTempMess);
                throw new Exception(strTempMess);
            }

            String strRouteTitle = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.JFSPartsApplication.RouteTitle");
            MapList approveList = new MapList();
            Map nReceiverMapOne = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strLineManagerId, strRouteTitle, "true", "1", "All");
            approveList.add(nReceiverMapOne);

            JF_Route_mxJPO jfRoute = new JF_Route_mxJPO(context, args);
            jfRoute.createAndStartRoute(context, approveList, objectId, "state_Review", "policy_JFSPartsApplication", strRouteTitle);
        } catch (Exception e) {
            JF_LOGGER.info(Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }

    /**
    * Review promote action trigger order 1
    * 标准件发布申请单审批通过后发布申请单关联的标准件零件
    * Review提升到Finished时发布关联VPMReference，发布失败则阻断申请单完成
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2026/7/8 15:00
    * @description
    */
    public void promoteSPartsApplicationPartToRelease(Context context, String[] args) throws Exception {
        String strMess = DomainConstants.EMPTY_STRING;
        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, Boolean.TRUE);
            String objectId = args[0];
            DomainObject object = DomainObject.newInstance(context, objectId);
            MapList mapList = object.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JFS_PARTS_APPLICATION_2_VPM + "," + JF_PLMConstants_mxJPO.REL_Instance,
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    JF_Util_mxJPO.basicBolistSel(),
                    JF_Util_mxJPO.basicRellistSel(),
                    false,
                    true,
                    (short) 0,
                    "current=='IN_WORK' || current=='FROZEN'",
                    "",
                    (short) 0);
            HashSet set = (HashSet) mapList.stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, DomainConstants.SELECT_ID);
            }).collect(Collectors.toCollection(HashSet::new));
            StringList partList = StringList.create(set);
            HashSet<String> toReleaseSet = new HashSet<>();
            String strCurrent = DomainConstants.EMPTY_STRING;
            String strPartId = DomainConstants.EMPTY_STRING;
            StringList inWorkCurrentList = new StringList();
            DomainObject partObject = DomainObject.newInstance(context);
            HashSet documentsSet = new HashSet();
            for (int i = 0; i < mapList.size(); i++) {
                Map map = (Map) mapList.get(i);
                strPartId = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                strCurrent = UIUtil.getValue(map, DomainConstants.SELECT_CURRENT);
                if (!"IN_WORK".equalsIgnoreCase(strCurrent) && !"FROZEN".equalsIgnoreCase(strCurrent)) {
                    continue;
                }
                if ("IN_WORK".equalsIgnoreCase(strCurrent)) {
                    inWorkCurrentList.add(strPartId);
                }
                partObject.setId(strPartId);
                MapList drwList = partObject.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_XCADBaseDependency,
                        JF_PLMConstants_mxJPO.TYPE_Drawing,
                        JF_Util_mxJPO.basicBolistSel(),
                        JF_Util_mxJPO.basicRellistSel(),
                        true,
                        false,
                        (short) 1,
                        "current!=RELEASED",
                        "",
                        0);
                MapList documentDrwMapList = partObject.getRelatedObjects(
                        context,
                        DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT,
                        DomainConstants.TYPE_DOCUMENT,
                        JF_Util_mxJPO.basicBolistSel(),
                        JF_Util_mxJPO.basicRellistSel(),
                        false,
                        true,
                        (short) 1,
                        "attribute[JF_DocumentType].value==Drawing",
                        "",
                        0);
                if (drwList.isEmpty() && documentDrwMapList.isEmpty()) {
                    continue;
                }
                for (int i1 = 0; i1 < drwList.size(); i1++) {
                    Map map1 = (Map) drwList.get(i1);
                    strCurrent = UIUtil.getValue(map1, DomainConstants.SELECT_CURRENT);
                    if (!"IN_WORK".equalsIgnoreCase(strCurrent) && !"FROZEN".equalsIgnoreCase(strCurrent)) {
                        continue;
                    }
                    if ("IN_WORK".equalsIgnoreCase(strCurrent)) {
                        inWorkCurrentList.add(UIUtil.getValue(map1, DomainConstants.SELECT_ID));
                    }
                    toReleaseSet.add(UIUtil.getValue(map1, DomainConstants.SELECT_ID));
                }
                for (int i1 = 0; i1 < documentDrwMapList.size(); i1++) {
                    Map map1 = (Map) documentDrwMapList.get(i1);
                    strCurrent = UIUtil.getValue(map1, DomainConstants.SELECT_CURRENT);
                    if (!"IN_WORK".equalsIgnoreCase(strCurrent) && !"FROZEN".equalsIgnoreCase(strCurrent)) {
                        continue;
                    }
                    if ("IN_WORK".equalsIgnoreCase(strCurrent)) {
                        inWorkCurrentList.add(UIUtil.getValue(map1, DomainConstants.SELECT_ID));
                    }
                    toReleaseSet.add(UIUtil.getValue(map1, DomainConstants.SELECT_ID));
                    documentsSet.add(UIUtil.getValue(map1,DomainConstants.SELECT_ID));
                }
            }
            JF_FasteningPiece_mxJPO jfFasteningPieceMxJPO = new JF_FasteningPiece_mxJPO();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("inWorkCurrentList", inWorkCurrentList);
            partList.addAll(toReleaseSet);
            paramMap.put("objectIdList", partList);
            Boolean flagSuccess = jfFasteningPieceMxJPO.FasteningPiecePromote(context, JPO.packArgs(paramMap));
            JF_NewECRProcess_mxJPO.partReferenceDocumentSignature(context, objectId, StringList.create(documentsSet));
            if (flagSuccess) {
                strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.FasteningPiecePromote.Successful");
            } else {
                strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.FasteningPiecePromote.failed");
                emxContextUtil_mxJPO.mqlNotice(context, strMess);
                throw new Exception(strMess);
            }
            emxContextUtil_mxJPO.mqlNotice(context, strMess);
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.info(Arrays.toString(e.getStackTrace()));
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            try {
                ContextUtil.popContext(context);
            } catch (FrameworkException e) {
                e.printStackTrace();
                JF_LOGGER.info(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    /**
     * 根据用户拥有的标准件工程师角色获取允许负责的标准件根分类ID。
     *
     * @param context
     * @param userName 用户名
     * @return StringList 允许负责的标准件根分类ID
     * @throws Exception
     * @author caipan by codex
     * @date 2026/8/25 16:00
     */
    public static StringList getAllowedStandardClassIdsByUser(Context context, String userName) throws Exception {
        StringList allowedClassIdList = new StringList();
        Vector assignments = PersonUtil.getAssignments(context, userName);
        JF_LOGGER.info("assignments:{}", assignments);
        for (Object assignmentObj : assignments) {
            String strAssignment = (String) assignmentObj;
            if (strAssignment.startsWith("ctx::")) {
                continue;
            }
            String strRoleConfigKey = DomainConstants.EMPTY_STRING;
            switch (strAssignment) {
                case "JfStandardAdmin":
                    strRoleConfigKey = "JfStandardAdmin.GeneralClassId";
                    break;
                case "JfStandardAdmin_Foaming":
                    strRoleConfigKey = "JfStandardAdmin_Foaming.GeneralClassId";
                    break;
                case "JfStandardAdmin_Trim":
                    strRoleConfigKey = "JfStandardAdmin_Trim.GeneralClassId";
                    break;
                case "JfStandardAdmin_Harness":
                    strRoleConfigKey = "JfStandardAdmin_Harness.GeneralClassId";
                    break;
                default:
                    break;
            }
            if (UIUtil.isNullOrEmpty(strRoleConfigKey)) {
                continue;
            }
            String strGeneralClassIds = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{strRoleConfigKey});
            if (UIUtil.isNotNullAndNotEmpty(strGeneralClassIds)) {
                StringList roleClassIdList = FrameworkUtil.split(strGeneralClassIds, ",");
                for (Object classIdObj : roleClassIdList) {
                    String classId = (String) classIdObj;
                    if (!allowedClassIdList.contains(classId)) {
                        allowedClassIdList.add(classId);
                    }
                }
            }
        }
        JF_LOGGER.info("allowedClassIdList:{}", allowedClassIdList);
        return allowedClassIdList;
    }

    /**
    * 草稿提交审核检查触发器，执行顺序1。
    * 标准件发布申请单提交审批前校验申请单零件的库归属。
    * 1. 同一标准件只能归属一个直属库节点；
    * 2. 根据Owner拥有的标准件工程师角色读取JFJDConfig中的GeneralClassId，校验零件入库路径是否包含允许的分类库ID。
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return int
    * @date 2026/7/22
    * @description
    */
    public int checkSPartsApplicationStandardPart(Context context, String[] args) throws Exception {
        int iReturn = 0;
        try {
            String objectId = args[0];
            DomainObject object = DomainObject.newInstance(context, objectId);
            String strOwner = object.getInfo(context, DomainConstants.SELECT_OWNER);
            //20260825 update by ljr 发布校验与零件分区共同复用标准件角色分类权限。
            StringList allowedClassIdList = getAllowedStandardClassIdsByUser(context, strOwner);
            StringList selectList = JF_Util_mxJPO.basicBolistSel();
            selectList.add("attribute[EnterpriseExtension.V_PartNumber]");
            selectList.add("to[Classified Item].from.id");
            MapList partMapList = object.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JFS_PARTS_APPLICATION_2_VPM + "," + JF_PLMConstants_mxJPO.REL_Instance,
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    selectList,
                    JF_Util_mxJPO.basicRellistSel(),
                    false,
                    true,
                    (short) 0,
                    "current=='IN_WORK' || current=='FROZEN'",
                    "",
                    (short) 0);
            if (partMapList.isEmpty()) {
                return iReturn;
            }
            JF_LOGGER.info("partMapList:{}", partMapList.size());
            StringList problemPartList = new StringList();
            StringList problemNoClassPartList = new StringList();
            StringList problemMultiClassPartList = new StringList();
            StringList classSelectList = new StringList(DomainConstants.SELECT_ID);
            DomainObject classObject = DomainObject.newInstance(context);
            for (Object partObj : partMapList) {
                Map partMap = (Map) partObj;
                String strRevision = UIUtil.getValue(partMap, DomainConstants.SELECT_REVISION);
                String strPartNumber = UIUtil.getValue(partMap, "attribute[EnterpriseExtension.V_PartNumber]");
                if (UIUtil.isNullOrEmpty(strPartNumber)) {
                    strPartNumber = UIUtil.getValue(partMap, DomainConstants.SELECT_NAME);
                }
                // 直接从零件的Classified Item关系取全部直属库节点，避免UIUtil.getValue只取到单个值。
                Object classIdValue = partMap.get("to[Classified Item].from.id");
                Set<String> classIdSet = new LinkedHashSet<>();
                if (classIdValue instanceof Collection) {
                    for (Object classIdObj : (Collection) classIdValue) {
                        String classId = String.valueOf(classIdObj);
                        if (UIUtil.isNotNullAndNotEmpty(classId)) {
                            classIdSet.add(classId);
                        }
                    }
                } else if (classIdValue != null) {
                    String classId = String.valueOf(classIdValue);
                    if (UIUtil.isNotNullAndNotEmpty(classId)) {
                        classIdSet.add(classId);
                    }
                }
                JF_LOGGER.info("classIdSet:{}", classIdSet);
                // 同一零件存在多个直属库节点时，单独汇总后阻止申请单提交。
                if (classIdSet.size() > 1) {
                    problemMultiClassPartList.add(strPartNumber + "_" + strRevision);
                    continue;
                }
                // 未配置Owner角色允许库时，保留原有逻辑：仅执行新增的多库归属校验。
                if (allowedClassIdList.isEmpty()) {
                    continue;
                }
                if (classIdSet.isEmpty()) {
                    problemNoClassPartList.add(strPartNumber + "_" + strRevision);
                    continue;
                }
                String strClassId = classIdSet.iterator().next();
                classObject.setId(strClassId);
                StringList parentClassIdList = (StringList)classObject.getRelatedObjects(context,
                        "Subclass",
                        "General Library,General Class",
                        classSelectList,
                        JF_Util_mxJPO.basicRellistSel(),
                        true,
                        false,
                        (short) 0,
                        "",
                        "",
                        (short) 0).stream().map(m -> {
                            Map map = (Map) m;
                            return UIUtil.getValue(map, DomainConstants.SELECT_ID);
                }).collect(Collectors.toCollection(StringList::new));
                JF_LOGGER.info("parentClassIdList:{}", parentClassIdList);
                parentClassIdList.add(strClassId);
                parentClassIdList.retainAll(allowedClassIdList);
                if (parentClassIdList.isEmpty()) {
                    problemPartList.add(strPartNumber + "_" + strRevision);
                }
            }
            JF_LOGGER.info("problemPartList:{}", problemPartList);
            JF_LOGGER.info("problemNoClassPartList:{}", problemNoClassPartList);
            JF_LOGGER.info("problemMultiClassPartList:{}", problemMultiClassPartList);
            StringBuilder strMessageBuilder = new StringBuilder();
            if (!problemMultiClassPartList.isEmpty()) {
                String strTempMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.JFSPartsApplication.MultiClassPartCheckMessage");
                strMessageBuilder.append(strTempMess).append(problemMultiClassPartList.join(","));
            }
            if (!problemNoClassPartList.isEmpty()) {
                String strTempMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.JFSPartsApplication.NoClassPartCheckMessage");
                if (strMessageBuilder.length() > 0) {
                    strMessageBuilder.append("\n");
                }
                strMessageBuilder.append(strTempMess).append(problemNoClassPartList.join(","));
            }
            if (!problemPartList.isEmpty()) {
                String strTempMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.JFSPartsApplication.StandardPartCheckMessage");
                if (strMessageBuilder.length() > 0) {
                    strMessageBuilder.append("\n");
                }
                strMessageBuilder.append(strTempMess).append(problemPartList.join(","));
            }
            if (strMessageBuilder.length() > 0) {
                emxContextUtil_mxJPO.mqlNotice(context, strMessageBuilder.toString());
                iReturn = 1;
            }
        } catch (Exception e) {
            JF_LOGGER.info(Arrays.toString(e.getStackTrace()));
            throw e;
        }
        return iReturn;
    }

    /**
     * 判断零件是否满足DR中标准件的发布状态要求
     * 零件属于标准件库且状态为工作中或冻结时返回false，其他情况返回true
     * @param context
     * @param partId
     * @author LIUJR
     * @throws Exception
     * @return boolean
     * @date 2026/7/10
     * @description
     */
    public static boolean checkStandardPartReleased(Context context, String partId) throws Exception {
        // 获取零件当前状态及直接入库节点。
        DomainObject partObject = DomainObject.newInstance(context, partId);
        String strCurrent = partObject.getInfo(context, DomainConstants.SELECT_CURRENT);
        String strClassId = partObject.getInfo(context, "to[Classified Item].from.id");
        if (UIUtil.isNullOrEmpty(strClassId)) {
            return true;
        }
        if (!("IN_WORK".equalsIgnoreCase(strCurrent) || "FROZEN".equalsIgnoreCase(strCurrent))) {
            return true;
        }
        // 获取当前环境配置的全部标准件根节点ID。
        String strStandardClassIds = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JfStandardPart.GeneralClassId"});
        StringList standardClassIdList = FrameworkUtil.split(strStandardClassIds, ",");
        //判断是否是标准件
        DomainObject classObject = DomainObject.newInstance(context, strClassId);
        StringList parentClassIdList = (StringList) classObject.getRelatedObjects(context,
                "Subclass",
                "General Library,General Class",
                JF_Util_mxJPO.basicBolistSel(),
                JF_Util_mxJPO.basicRellistSel(),
                true,
                false,
                (short) 0,
                "",
                "",
                (short) 0).stream().map(m -> {
            Map map = (Map) m;
            return UIUtil.getValue(map, DomainConstants.SELECT_ID);
        }).collect(Collectors.toCollection(StringList::new));
        parentClassIdList.add(strClassId);
        parentClassIdList.retainAll(standardClassIdList);
        // 有交集表示当前零件是未发布标准件，不允许DR提交。
        return parentClassIdList.isEmpty();
    }

    /**
    * 流程对象提交到审核的时候，增加Change Control 来控制编辑权限
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2026/7/21 14:53
    * @description
    */
    public void addChangeInterfacePart(Context context,String[] args) throws  Exception{
        JF_LOGGER.info("addChangeInterface start");
        String drId = args[0];
        DomainObject dr = DomainObject.newInstance(context,drId);
        StringList bosel = JF_Util_mxJPO.basicBolistSel();
        StringList relsel =JF_Util_mxJPO.basicRellistSel();
        String where = "current==IN_WORK||current==FROZEN";
        MapList VPMList = dr.getRelatedObjects(context,
                JF_PLMConstants_mxJPO.REL_JFS_PARTS_APPLICATION_2_VPM+","+JF_PLMConstants_mxJPO.REL_Instance,
                JF_PLMConstants_mxJPO.TYPE_VPMReference,
                bosel,
                relsel,
                false,
                true,
                (short) 0,
                where,
                "",
                0);
        DomainObject vpmObj= DomainObject.newInstance(context);
        for(int i=0; i<VPMList.size(); i++){
            Map mangerMap = (Map) VPMList.get(i);
            String id = UIUtil.getValue(mangerMap, DomainConstants.SELECT_ID);
            vpmObj.setId(id);
            StringList list = vpmObj.getInfoList(context, "interface");
            if(!list.contains("Change Control")) {
                MqlUtil.mqlCommand(context, false, "mod bus '" + id + "' add interface 'Change Control' ", true);
            }
        }
        JF_LOGGER.info("addChangeInterface end");
    }

    /**
    * 流程对象审核完成的时候，移除掉Change Control 来释放升版权限
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2026/7/21 15:05
    * @description
    */
    public static void removeChangeInterface(Context context,String[] args){
        try {
            JF_LOGGER.info("removeChangeInterface start");
            String drId = args[0];
            DomainObject dr = DomainObject.newInstance(context,drId);
            StringList bosel = JF_Util_mxJPO.basicBolistSel();
            StringList relsel =JF_Util_mxJPO.basicRellistSel();
            MapList VPMList = dr.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JFS_PARTS_APPLICATION_2_VPM+","+JF_PLMConstants_mxJPO.REL_Instance,
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    bosel,
                    relsel,
                    false,
                    true,
                    (short) 0,
                    "",
                    "",
                    0);
            DomainObject vpmObj= DomainObject.newInstance(context);
            for(int i=0; i<VPMList.size(); i++){
                Map mangerMap = (Map) VPMList.get(i);
                String id = UIUtil.getValue(mangerMap, DomainConstants.SELECT_ID);
                vpmObj.setId(id);
                StringList list = vpmObj.getInfoList(context, "interface");
                if(list.contains("Change Control")) {
                    MqlUtil.mqlCommand(context, false, "mod bus '" + id + "' remove interface 'Change Control' ", true);
                }
            }
            JF_LOGGER.info("removeChangeInterface end");
        } catch (FrameworkException e) {
            e.printStackTrace();
            JF_LOGGER.info(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * 校验标准件发布申请单附件操作权限。
     * 仅当申请单处于草稿状态且Owner为当前登录人时，允许上传、添加系统现有文档或移除附件。
     *
     * @param context 上下文
     * @param args 页面请求参数
     * @return boolean 有附件操作权限返回true，否则返回false
     * @throws Exception 获取申请单信息失败时抛出异常
     * @author LIUJR
     * @date 2026/7/22
     */
    public boolean isSPartsApplicationAttachmentEditable(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String strObjectId = UIUtil.getValue(paramMap, "objectId");
        if (UIUtil.isNullOrEmpty(strObjectId)) {
            return false;
        }
        DomainObject applicationObject = DomainObject.newInstance(context, strObjectId);
        StringList selectList = StringList.create(DomainConstants.SELECT_TYPE, DomainConstants.SELECT_CURRENT, DomainConstants.SELECT_OWNER);
        Map applicationInfo = applicationObject.getInfo(context, selectList);
        String strType = UIUtil.getValue(applicationInfo, DomainConstants.SELECT_TYPE);
        String strCurrent = UIUtil.getValue(applicationInfo, DomainConstants.SELECT_CURRENT);
        String strOwner = UIUtil.getValue(applicationInfo, DomainConstants.SELECT_OWNER);
        return JF_PLMConstants_mxJPO.TYPE_JFS_PARTS_APPLICATION.equals(strType)
                && "Draft".equalsIgnoreCase(strCurrent)
                && context.getUser().equalsIgnoreCase(strOwner);
    }

    /**
     * 标准件申请单发布完成后异步同步申请单零件。
     **
     * @param context 上下文
     * @param args trigger参数，args[0]为标准件申请单ID
     * @return int 0表示异步任务提交完成
     * @throws Exception 异常
     * @author caipan by codex
     * @date 2026/8/27
     */
    public int syncStandardPartListAfterRelease(Context context, String[] args) throws Exception {
        String applicationId = args != null && args.length > 0 ? args[0] : DomainConstants.EMPTY_STRING;
        if (UIUtil.isNullOrEmpty(applicationId)) {
            throw new Exception("标准件申请单ID为空，无法同步标准件申请单零件");
        }
        JF_Util_mxJPO.runAsyncWithJsonResult(context, new String[]{applicationId}, "JF_BIInterface",
                "syncStandardPartList");
        return 0;
    }

}
