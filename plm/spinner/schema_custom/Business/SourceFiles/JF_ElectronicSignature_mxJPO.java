
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.util.DocumentUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import com.nomagic.esi.common.a.M;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @ClassName JF_ElectronicSignature_mxJPO
 * @Author: LIUJR
 * @CreateDate: 2025/4/1 15:17
 * @UpdateRemark:
 * @Version: 1.0
 * @Description: 电子签名
 */
public class JF_ElectronicSignature_mxJPO {
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_ElectronicSignature_mxJPO.class);
    private static final String TEMPLATE_EXCEL_PATH = "jf_Signature/";
    private static final String FORMAT_PDF = "PDF";
    private static final Float CM = 28.3465f;
    private static final Float FT = 70f;
//    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年M月d日");
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static final String STRING_SIGN_TASK_PROPERTIES_ZH = "SignTaskProperties_zh.xml";
    private static final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a", Locale.US);

    private static final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    /**
    * 签名入口方法
    * @param context
	* @param args
        *  list (MapList)   需要签名的文件列表
                *  Map :  key / value
                *         docId / 文档对象id
                *         fileId / 文件id
                *         fileName / 文件名
        *  signMap (HashMap<String, String>)  签名信息
                *  key / value
                *  ownerName / owner的name
                *  ownerDate / owner提交的日期
                *  reviewName / 直线经理name
                *  reviewDate / 直线经理审批日期
                *  completeName / 整椅经理name
                *  completeDate / 整椅经理审批日期
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/4/2 10:14
    * @description
    */
    public static void electronicSignAssembly(Context context, String[] args) throws Exception{
        JF_LOGGER.info(" ----------------------------------- electronicSignAssembly start ----------------------------------- ");
        try {
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            JF_ElectronicSignature_mxJPO jfElectronicSignatureMxJPO = new JF_ElectronicSignature_mxJPO();
            HashMap<String, Object> paramsMap = (HashMap) JPO.unpackArgs(args);
            //需要签名的文件集合
            MapList mapList = (MapList) paramsMap.get("list");
            //是图纸文档(DOC),还是图纸(DRAW)
            String flag = (String) paramsMap.get("flag");
            //签字的名字 和 时间
            HashMap<String, String> signMap = (HashMap) paramsMap.get("signMap");
            DomainObject domainObject = DomainObject.newInstance(context);
            //todo 构造下载目录  需要放置配置文件中
//            String pdfPath = "/tmp/ljr/sign/down/";
            String pdfPath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"Download.Document.Path"});
//            String newPswPathBasic = "/tmp/ljr/sign/signature/";
            String newPswPathBasic = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"Signature.Document.Path"});
            //签名参数 构造
            MapList signatureMapList = new MapList();
            if ("DRAW".equalsIgnoreCase(flag)) {
                signatureMapList = jfElectronicSignatureMxJPO.electronicSignAssemblyParams(context, signMap);
            }
            //开始遍历电子签名文件列表
            Iterator iterator = mapList.iterator();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String docId = UIUtil.getValue(map, "docId");
                String fileName = UIUtil.getValue(map, "fileName");
                String newPswPath = newPswPathBasic + fileName;
                if ("DOC".equalsIgnoreCase(flag)) {
                    //图纸文档的电子签名
                    String fileFormat = UIUtil.getValue(map, "fileFormat");
                    String fileId = UIUtil.getValue(map, "fileId");
                    //文档的创建签名重新获取
                    String ownerName = UIUtil.getValue(map, "ownerName");
                    String ownerDate = UIUtil.getValue(map, "ownerDate");
                    signMap.put("ownerName", ownerName);
                    signMap.put("ownerDate", ownerDate);
                    JF_LOGGER.info("fileName:{}", fileName);
                    JF_LOGGER.info("docId:{}", docId);
                    signatureMapList = jfElectronicSignatureMxJPO.electronicSignAssemblyParams(context, signMap);
                    domainObject.setId(docId);
                    String downPath = jfElectronicSignatureMxJPO.checkoutFile(context, docId, fileFormat, fileName, pdfPath);
                    //签字
                    jfElectronicSignatureMxJPO.electronicSignature(context, signatureMapList, downPath, newPswPath);
                    //检入并升版
                    jfElectronicSignatureMxJPO.docReviseVersionCheckIn(context, docId, fileFormat, fileName, fileId, newPswPathBasic);
                } else if ("DRAW".equalsIgnoreCase(flag)){
                    //图纸的签名
                    domainObject.setId(docId);
                    //下载pdf文件到系统目录
                    jfUtilMxJPO.checkOutFile(context, domainObject, fileName, pdfPath);
                    //下载完开始签字
                    JF_LOGGER.info("signatureMapList:{}", signatureMapList.toString());
                    jfElectronicSignatureMxJPO.electronicSignature(context, signatureMapList, pdfPath + fileName, newPswPath);
                    //check in回去   0是成功
                    int i = jfUtilMxJPO.checkinParameter(context, docId, fileName, FORMAT_PDF, newPswPathBasic);
                    JF_LOGGER.info("i:{}", i);
                }
//                new File(pdfPath).delete();
            }
        }catch (Exception e) {
             e.printStackTrace();
             throw e;
        }
        JF_LOGGER.info(" ----------------------------------- electronicSignAssembly end ----------------------------------- ");
    }

    /**
    * 下载文件
    * @param context
	* @param docId  文档对象
	* @param format  格式
	* @param fileName 文件名
	* @param dirPath 下载地址
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/4/2 11:03
    * @description
    */
    public String checkoutFile(Context context, String docId, String format, String fileName, String dirPath) throws Exception {
        CommonDocument commonDocument = new CommonDocument();
        commonDocument.setId(docId);
        if (!dirPath.endsWith(File.separator)) {
            dirPath = dirPath + File.separator;
        }
        commonDocument.checkoutFile(context, false, format, fileName, dirPath);
        return dirPath + fileName;
    }

    /**
    * 文件升版 并check in
    * @param context
	* @param docId  文档对象
	* @param format 格式
	* @param fileName 文件名
	* @param fileId 文件id
	* @param dirPath 地址
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/4/2 11:10
    * @description
    */
    public void docReviseVersionCheckIn(Context context, String docId, String format, String fileName, String fileId, String dirPath) throws Exception {
        JF_LOGGER.info(" ----------------------------------- docReviseVersionCheckIn ----------------------------------- ");
        CommonDocument document = new CommonDocument();
        document.setId(docId);
        DomainObject fileObj = DomainObject.newInstance(context, fileId);
        try {
            //1.文件对象加锁
            boolean locked = fileObj.isLocked(context);
            if (!locked) {
                fileObj.lock(context);
            }
            //2.升版文件对象升版 如果不加锁 返回值为 文档的id
            Map attrMap = new HashMap();
            String newVerId = document.reviseVersion(context, fileName, fileName, attrMap);
            //3.检入文件
            document.checkinFile(context, true, true, DomainConstants.EMPTY_STRING, format, fileName, dirPath);
            JF_LOGGER.info("commonDocument.checkinFile : " + dirPath);
            String owner = document.getOwner(context).getName();
            MqlUtil.mqlCommand(context, "mod bus $1 owner $2", newVerId, owner);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info(" ----------------------------------- docReviseVersionCheckIn ----------------------------------- ");
    }


    /**
     * 电子签名组装参数
     * @param context
     * @param signMap  签名
     * @param signMap  owner签名 日期 坐标
     * @param signMap  直线经理签名 日期 坐标
     * @param signMap  整椅经理签名 日期 坐标
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2025/4/1 13:37
     * @description
     */
    public MapList electronicSignAssemblyParams(Context context, Map signMap) throws Exception{
        MapList signatureMapList = new MapList();
        try {
            //获取owner - line manager  - chair manager的first name   和时间 解析
            //owner的name - first name
            String ownerName = UIUtil.getValue(signMap, "ownerName");
            DomainObject ownerPerson = PersonUtil.getPersonObject(context, ownerName);
            ownerName = ownerPerson.getAttributeValue(context, DomainConstants.ATTRIBUTE_FIRST_NAME);
            //owner的date -  日期格式化 解析日期
            String ownerDateFormat = UIUtil.getValue(signMap, "ownerDate");
            LocalDateTime dateTime = LocalDateTime.parse(ownerDateFormat, inputFormatter);
            ownerDateFormat = dateTime.format(outputFormatter);
            //直线经理的name - first name
            String reviewName = UIUtil.getValue(signMap, "reviewName");
            if (UIUtil.isNotNullAndNotEmpty(reviewName)) {
                DomainObject reviewPerson = PersonUtil.getPersonObject(context, reviewName);
                reviewName = reviewPerson.getAttributeValue(context, DomainConstants.ATTRIBUTE_FIRST_NAME);
            }
            //owner的date -  日期格式化 解析日期
            String reviewDateFormat = UIUtil.getValue(signMap, "reviewDate");
            if (UIUtil.isNotNullAndNotEmpty(reviewDateFormat)) {
                LocalDateTime dateTime1 = LocalDateTime.parse(reviewDateFormat, inputFormatter);
                reviewDateFormat = dateTime1.format(outputFormatter);
            }
            //整椅经理的name - first name
            String completeName = UIUtil.getValue(signMap, "completeName");
            if (UIUtil.isNotNullAndNotEmpty(completeName)) {
                DomainObject completePerson = PersonUtil.getPersonObject(context, completeName);
                completeName = completePerson.getAttributeValue(context, DomainConstants.ATTRIBUTE_FIRST_NAME);
            }
            //owner的date -  日期格式化 解析日期
            String completeDateFormat = UIUtil.getValue(signMap, "completeDate");
            if (UIUtil.isNotNullAndNotEmpty(completeDateFormat)) {
                LocalDateTime dateTime2 = LocalDateTime.parse(completeDateFormat, inputFormatter);
                completeDateFormat = dateTime2.format(outputFormatter);
            }
            //获取签名位置及坐标单位  放置在page文件中，方便修改坐标
            JF_ElectronicSignature_mxJPO jfElectronicSignatureMxJPO = new JF_ElectronicSignature_mxJPO();
            NodeList configNodes = jfElectronicSignatureMxJPO.readSignaturePropertiesFile(context, STRING_SIGN_TASK_PROPERTIES_ZH, "signature");
            //解析签名坐标及组装参数 ： 签名 名称 日期  位置的坐标  坐标单位
            for (int j = 0; j < configNodes.getLength(); j++) {
                Element configElement = (Element) configNodes.item(j);
                String id = configElement.getAttribute("id");
                Float nameX = Float.valueOf(configElement.getAttribute("nameX"));   //姓名的X坐标
                Float nameY = Float.valueOf(configElement.getAttribute("nameY"));   //姓名的Y坐标
                Float dateX = Float.valueOf(configElement.getAttribute("dateX"));   //日期的X坐标
                Float dateY = Float.valueOf(configElement.getAttribute("dateY"));   //日期的Y坐标
                String unit = String.valueOf(configElement.getAttribute("unit"));     //坐标的单位
                //组装 这里可以混装,  owner ,直线经理, 整椅经理的位置可以靠坐标来识别了， 放置Map -> MapList
                HashMap<String, Object> map = new HashMap<>();
                map.put("nameX", nameX);
                map.put("nameY", nameY);
                map.put("dateX", dateX);
                map.put("dateY", dateY);
                map.put("unit", unit);
                if ("owner".equalsIgnoreCase(id)) {
                    map.put("date", ownerDateFormat);
                    map.put(DomainConstants.SELECT_NAME, ownerName);
                    signatureMapList.add(map);
                } else if ("review".equalsIgnoreCase(id)) {
                    map.put(DomainConstants.SELECT_NAME, reviewName);
                    map.put("date", reviewDateFormat);
                    signatureMapList.add(map);
                } else if ("complete".equalsIgnoreCase(id)) {
                    map.put(DomainConstants.SELECT_NAME, completeName);
                    map.put("date", completeDateFormat);
                    signatureMapList.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return signatureMapList;
    }

    /**
     * 拿取xml的签名位置
     * @param context
     * @param pageName
     * @author LIUJR
     * @throws
     * @return com.matrixone.apps.domain.util.MapList
     * @date 2024/7/31 11:15
     * @description
     */
    public NodeList readSignaturePropertiesFile(Context context, String pageName, String flag) throws Exception{
        Page pageAttributePopulation = new Page(pageName);
        pageAttributePopulation.open(context);
        String strProperties = pageAttributePopulation.getContents(context);
        pageAttributePopulation.close(context);
        String createTaskType = flag;
        NodeList configNodes = null;
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
                if (createTaskType.equals(configurationElement.getAttribute("id"))) {
                    // 找到匹配的 configuration, 获取它下的 config 元素
                    configNodes = configurationElement.getElementsByTagName("config");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return configNodes;
    }


    /**
    * 电子签名
    * @param context
	* @param signatureMapList   签名的name和date  坐标
	* @param pdfPath    签名文件的地址
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/4/1 15:27
    * @description
    */
    public void electronicSignature(Context context, MapList signatureMapList, String pdfPath, String newPswPath) throws Exception{
        //开始签名
        PdfReader reader = null;
        FileOutputStream fOut = null;
        PdfStamper stp = null;
        try {
            //读取文件
            reader = new PdfReader(pdfPath);
            fOut = new FileOutputStream(newPswPath);
            stp = new PdfStamper(reader, fOut);
            //总页数
            int numberOfPages = reader.getNumberOfPages();
            //签名数据
            for (int pageNum = 1; pageNum <= numberOfPages; pageNum++) {
                PdfContentByte pdfContentByte = stp.getOverContent(pageNum);
                pageSign(pdfContentByte, signatureMapList);
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            // 确保按正确顺序关闭资源
            if (stp != null) {
                try {
                    stp.close(); // 这会同时关闭reader和fOut
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // 如果stamper未创建成功，单独关闭其他资源
                if (reader != null) {
                    reader.close();
                }
                if (fOut != null) {
                    try {
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    /**
    * 页开始签名
    * @param pdfContentByte
	* @param signatureMapList 签名集合
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/4/2 15:31
    * @description
    */
    private void pageSign(PdfContentByte pdfContentByte, MapList signatureMapList) throws DocumentException, IOException {
        pdfContentByte.beginText();
        // 设置字体及字号
        pdfContentByte.setFontAndSize(getBaseFont(), 12);
        //开始拿取内容
        float cmToPoint = 1f;
        Iterator iterator = signatureMapList.iterator();
        while (iterator.hasNext()) {
            Map map = (Map) iterator.next();
            String owner = (String) map.get("name");
            String date = (String) map.get("date");
            String unit = (String) map.get("unit");
            //单位换算
            if ("cm".equalsIgnoreCase(unit)){
                cmToPoint = CM;
            }
            Float nameX = (Float) map.get("nameX");
            nameX *= cmToPoint;
            Float nameY = (Float) map.get("nameY");
            nameY *= cmToPoint;
            Float dateX = (Float) map.get("dateX");
            dateX *= cmToPoint;
            Float dateY = (Float) map.get("dateY");
            dateY *= cmToPoint;
            addDeptReview(nameX, nameY, pdfContentByte, owner);
            addDeptReview(dateX, dateY, pdfContentByte, date);
        }
        pdfContentByte.endText();
    }

    /**
    * 设置内容
    * @param x
	* @param y
	* @param pdfContentByte
	* @param content
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/4/2 15:30
    * @description
    */
    private void addDeptReview(float x, float y, PdfContentByte pdfContentByte, String content) {
        pdfContentByte.setColorFill(BaseColor.BLACK);
        // 设置水印位置和内容
        pdfContentByte.showTextAligned(com.lowagie.text.Element.ALIGN_LEFT, content, x, y, 0);
    }

    /**
    * 设置基础字体和字号
    * @param
    * @author LIUJR
    * @throws
    * @return com.lowagie.text.pdf.BaseFont
    * @date 2025/4/2 15:17
    * @description
    */
    private BaseFont getBaseFont() throws DocumentException, IOException {
//        BaseFont base = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
//        BaseFont base = BaseFont.createFont("STHeiti-Regular", "UniGB-UCS2-H", BaseFont.EMBEDDED);
//        BaseFont base = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
        // 使用支持中文的字体，如STSong-Light（宋体）
        BaseFont base = BaseFont.createFont(
                "STSong-Light",
                "UniGB-UCS2-H",
                BaseFont.EMBEDDED
        );

        return base;
    }
}
