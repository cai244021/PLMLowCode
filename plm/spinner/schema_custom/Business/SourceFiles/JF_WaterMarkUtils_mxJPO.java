import com.dassault_systemes.enovia.bps.widget.jaxb.Ui;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.*;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.File;
import matrix.db.FileList;
import matrix.util.StringList;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * WatermarkUtils
 * PDF添加水印工具类
 *
 * @author pan.cai
 */
public class JF_WaterMarkUtils_mxJPO {

    public static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_PublicMethodClass_mxJPO.class);
    public static final String BASIC_PDF_FILE_PATH = "/tmp/dataOurSource/";
    public static final String END_WITH_PDF = ".pdf";
    public static final String ATTRIBUTE_DOCUMENT_JF_PDFSOURCEFILEDID = "JF_PDFSourceFileId";
    public static final String DEFAULT_WATERMARK_TEXT = "JFSeat_";
    public static final String FILE_GENERIC = "generic";
    public static final String FILE_CODE_XLSX = ".xlsx";
    public static final String FILE_CODE_XLS = ".xls";
    public static final String STRING_MQL_ATTRIBUTE = "attribute[%s].value";
    public static final StringList END_WITH = new StringList();
    static {
        END_WITH.add(".doc");
        END_WITH.add(".docx");
        END_WITH.add(".ppt");
        END_WITH.add(".pptx");
    }


    public static void main(String[] args) throws Exception {
        FileInputStream fileInputStream = new FileInputStream("D:\\pdf\\file\\testpdf21.pdf");
        addWaterMark(DEFAULT_WATERMARK_TEXT, IOUtils.toByteArray(fileInputStream), Files.newOutputStream(Paths.get("D:\\pdf\\file\\word2618.pdf")));
    }

    /**
     * @description 给pdf文件添加水印
     * @author caipan
     * @param[1] waterMarkText 添加的文字
     * @param[2] filePathName 源文件的全路径
     * @param[3] processPathName 生成之后的全路径
     * @throws

     * @time 2024/6/19 11:29
     */
    public static  void addWaterMark(String waterMarkText, String filePathName, String processPathName) {
        try {
            FileInputStream fileInputStream = new FileInputStream(filePathName);
            addWaterMark(DEFAULT_WATERMARK_TEXT, IOUtils.toByteArray(fileInputStream), Files.newOutputStream(Paths.get(processPathName)));
        }catch (Exception e){

        }
    }


    /**
     * pdf添加水印
     *
     * @param waterMarkText  水印文字
     * @param pdfFileBytes   pdf
     * @param outputFilePath 输出流
     */
    public static void addWaterMark(String waterMarkText, byte[] pdfFileBytes, OutputStream outputFilePath) {
        try {
            // 原PDF文件
            PdfReader reader = new PdfReader(pdfFileBytes);
            // 输出的PDF文件内容
            PdfStamper stamper = new PdfStamper(reader, outputFilePath);
            // 字体 来源于 itext-asian jar包
//            BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", true);
            BaseFont baseFont = BaseFont.createFont();
            PdfGState gs = new PdfGState();
            // 设置透明度
            gs.setFillOpacity(0.1f);
            gs.setStrokeOpacity(0.1f);

            int totalPage = reader.getNumberOfPages() + 1;
            for (int i = 1; i < totalPage; i++) {
                // 内容上层
                PdfContentByte content = stamper.getOverContent(i);
                content.beginText();
                // 字体添加透明度
                content.setGState(gs);
                // 添加字体大小等
                content.setFontAndSize(baseFont, 20);
                // 添加范围
                for(int j=1;j<10;j++) {
                    content.showTextAligned(Element.ALIGN_BOTTOM, Optional.ofNullable(waterMarkText).orElse(""), j*50, j*100, 45);
                }
                content.endText();
            }
            // 关闭
            stamper.close();
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    /**
     * pdf添加水印  均匀分布
     *
     * @param strWaterMarkList  文件列表
     * @param waterMarkText  水印文字
     */
    public static void addWaterMarkUniformDistribution(Context context, StringList strWaterMarkList,  String waterMarkText) throws Exception{
        try {
            for (String strFile : strWaterMarkList) {
                FileInputStream fileInputStream = new FileInputStream(strFile);
                byte[] pdfFileBytes = IOUtils.toByteArray(fileInputStream);
                // 预检：是否是 PDF 文件？
                Boolean head = pdfFileBytes.length >= 5 && new String(pdfFileBytes, 0, 5).startsWith("%PDF");
                if (!head) {
                    continue;
                }
                PdfReader reader = null;
                try {
                    reader = new PdfReader(pdfFileBytes);
                    // 权限检查...
                    if ((reader.isEncrypted() && !reader.isOpenedWithFullPermissions())
                            || !reader.getAcroFields().getSignatureNames().isEmpty()) {
                        System.out.println("跳过（权限/签名限制）: " + strFile);
                        continue;
                    }
                    OutputStream outputFilePath = Files.newOutputStream(Paths.get(strFile));
                    // 输出的PDF文件内容
                    PdfStamper stamper = new PdfStamper(reader, outputFilePath);
                    // 字体 来源于 itext-asian jar包
//            BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", true);
                    BaseFont baseFont = BaseFont.createFont();
                    PdfGState gs = new PdfGState();
                    // 设置透明度
                    gs.setFillOpacity(0.1f);
                    gs.setStrokeOpacity(0.1f);
                    int totalPage = reader.getNumberOfPages() + 1;
                    for (int i = 1; i < totalPage; i++) {

                        // 均匀分布水印的优化实现
                        float pageWidth = reader.getPageSize(i).getWidth();
                        float pageHeight = reader.getPageSize(i).getHeight();

                        // 1. 计算合理的水印密度
                        int cols = (int) (pageWidth / 150); // 每150px一个水印
                        int rows = (int) (pageHeight / 150); // 每150px一个水印

                        // 2. 设置安全边距
                        float marginX = pageWidth * 0.1f; // 10%边距
                        float marginY = pageHeight * 0.1f;

                        // 3. 计算水印间距
                        float spacingX = (pageWidth - 2 * marginX) / (cols - 1);
                        float spacingY = (pageHeight - 2 * marginY) / (rows - 1);

                        // 内容上层
                        PdfContentByte content = stamper.getOverContent(i);
                        content.beginText();
                        // 字体添加透明度
                        content.setGState(gs);
                        // 添加字体大小等
                        content.setFontAndSize(baseFont, 20);

                        // 4. 添加交错水印
                        for (int row = 0; row < rows; row++) {
                            for (int col = 0; col < cols; col++) {
                                float x = marginX + col * spacingX;
                                float y = marginY + row * spacingY;

                                // 交错布局：奇数列下移半个间距
                                if (col % 2 == 1) {
                                    y += spacingY / 2;
                                }

                                // 5. 使用中心对齐+旋转（最佳实践）
                                content.showTextAligned(
                                        Element.ALIGN_CENTER,
                                        waterMarkText,
                                        x,
                                        y,
                                        45 // 45度旋转
                                );
                            }
                        }

//                    // 添加范围
//                    for(int j=1;j<10;j++) {
//                        content.showTextAligned(Element.ALIGN_BOTTOM, Optional.ofNullable(waterMarkText).orElse(""), j*100, j*50, 45);
//                    }
                        content.endText();
                    }
                    // 关闭
                    stamper.close();
                    reader.close();
                } catch (com.itextpdf.text.exceptions.InvalidPdfException e) {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("R = 6")) {
                        continue;
                    }
                } finally {
                    if (reader != null) reader.close();
                }
//                PdfReader.unethicalreading=true;//全局设置
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * 1.  checkOut files 数据外发申请单下的内容为文档的时候 调用此方法
     * 2. 技术文档外发时，将转为PDF格式文件并打水印"JFSeat_"+加创建者的账号；
     * 3. 技术文档word、PPT打水印在PDF文档上，Excel不打水印，拿取源文件上，其他文档不打水印
     * @param context
     * @param args
     *  args[0] 文档id
     *  args[1] 申请的名称
     * @author LIUJR
     * @throws
     * @return Map 返回下载的路径地址 所有文件的地址
     * @date 2024/7/18 9:08
     * @description
     */
    public static Map<String, Object> fileCheckout(Context context, String[] args) throws Exception {
        JF_LOGGER.info("#####################fileCheckout begin #################################");
        Map<String, Object> resultMap = new HashMap<>();
        StringList strDownFilePath = new StringList();
        try {
            ContextUtil.startTransaction(context,true);
            ContextUtil.pushContext(context);
            String objectId = args[0];
            String dataSourceName = args[1];
            String strDirPath = args[2] + java.io.File.separator;
            objectId = UIUtil.isNullOrEmpty(objectId) ? "" : objectId;
            dataSourceName = UIUtil.isNullOrEmpty(dataSourceName) ? "" : dataSourceName;
            if (UIUtil.isNullOrEmpty(objectId) || UIUtil.isNullOrEmpty(dataSourceName)) {
                return resultMap;
            }
            CommonDocument commonDocument = new CommonDocument();
            commonDocument.setId(objectId);
            //拿取文档对象下的文件对象
            MapList versionList = JF_PublicMethodClass_mxJPO.getDocumentFiles(context, objectId);
            Iterator iterator = versionList.iterator();
            FileList fileList = new FileList();
            StringList waterMarkList = new StringList();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                System.out.println("map: " + map.toString());
                String fileId = (String) map.get(CommonDocument.SELECT_ID);
                String fileName = (String) map.get(CommonDocument.SELECT_TITLE);
                String fileFormat = (String) map.get(CommonDocument.SELECT_FILE_FORMAT);
                File file = new File(fileName, fileFormat);
                String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase(); // 获取并转换为小写
                //.doc .docx .xls .xlsx .xlsm other
                System.out.println("fileName: " + fileName.toString());
                System.out.println("other: fileExtension=" + fileExtension);

                //文档对象下有pdf,先拿pdf
                if (fileName.endsWith(END_WITH_PDF)) {
                    System.out.println("pdf: fileExtension=" + fileExtension);
                    String strJFPDFSourceFileId = (String) map.get(String.format(STRING_MQL_ATTRIBUTE, ATTRIBUTE_DOCUMENT_JF_PDFSOURCEFILEDID));
                    //需要判断文件的源文件ID,是否是excel,当为excel的情况,不需要打印,需要单独存储
                    System.out.println("当前文件是pdf：physicalid="  + fileId);
                    if (UIUtil.isNullOrEmpty(strJFPDFSourceFileId)) {
                        //本身就是源文件
                        waterMarkList.add(strDirPath + fileName);
                        fileList.add(file);
                        strDownFilePath.add(strDirPath + fileName);
                    } else {
                        CommonDocument commonDocument1 = new CommonDocument();
                        commonDocument1.setId(strJFPDFSourceFileId);
                        String attributeValue = commonDocument1.getAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE);
                        //判断源文件是否是excel .xlsx文件
                        if (attributeValue.endsWith(FILE_CODE_XLSX) || attributeValue.endsWith(FILE_CODE_XLS)) {
                            ;
                        } else {
                            //为转换后的pdf文件,直接下载pdf,并要加水印
                            fileList.add(file);
                            waterMarkList.add(strDirPath + fileName);
                            strDownFilePath.add(strDirPath + fileName);
                        }
                    }
                } else if (!END_WITH.contains(fileExtension)){
                    //除开.doc,.docx,.xlsx,.ppt,这几个类型的文件 其他的文件
                    System.out.println("other: fileExtension=" + fileExtension);
                    fileList.add(file);
                    strDownFilePath.add(strDirPath + fileName);
                }
            }
            if (!fileList.isEmpty()) {
                commonDocument.checkoutFiles(context, false, FILE_GENERIC, fileList, strDirPath);
            }
            ContextUtil.commitTransaction(context);
            //返回结果
            resultMap.put("files", strDownFilePath);  //所有文件
            resultMap.put("waterMark", waterMarkList); //需要打水印的文件
            resultMap.put("path", strDirPath);
            System.out.println("resultMap:" + resultMap.toString());
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        JF_LOGGER.info("#####################fileCheckout end #################################");
        return resultMap;
    }

    /**
    * 传入文件列表加水印
    * @param context
	* @param strWaterMarkList
	* @param strOwner
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/7/22 10:41
    * @description
    */
    public static void addFilesWaterMark(Context context, StringList strWaterMarkList, String strOwner) {
        try {
            String strWaterMarkText = DEFAULT_WATERMARK_TEXT + strOwner;
            for (String strFile : strWaterMarkList) {
                FileInputStream fileInputStream = new FileInputStream(strFile);
                addWaterMark(strWaterMarkText, IOUtils.toByteArray(fileInputStream), Files.newOutputStream(Paths.get(strFile)));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
    * 文件打印水印
    * @param context
	* @param strWaterMarkList
	* @param strWaterMarkText
    * @author LIUJR
    * @throws
    * @return void
    * @date 09/06/2025 13:55
    * @description
    */
    public static void addFilesWaterMarkMess(Context context, StringList strWaterMarkList, String strWaterMarkText) {
        try {
            for (String strFile : strWaterMarkList) {
                FileInputStream fileInputStream = new FileInputStream(strFile);
                addWaterMark(strWaterMarkText, IOUtils.toByteArray(fileInputStream), Files.newOutputStream(Paths.get(strFile)));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }



    public static void testAddWaterMark(Context context, String[] args) throws Exception{
        String[] params = new String[2];
        params[0]= "14432.39000.30079.57867";
        params[1]= "DOS-0000001";
        Map<String, Object> map = fileCheckout(context, params);
        //下载的所有文件
        StringList files = (StringList) map.get("files");
        //下载的地址
        String path = (String) map.get("path");
        //需要打水印的pdf
        StringList waterMarkFiles = (StringList) map.get("waterMark");
        String owner = "zhangzt";
//        String[] split = waterMarkFiles.split(",");
        for (String strFile : waterMarkFiles) {
            FileInputStream fileInputStream = new FileInputStream(strFile);
            addWaterMark(DEFAULT_WATERMARK_TEXT + owner, IOUtils.toByteArray(fileInputStream), Files.newOutputStream(Paths.get(strFile)));
        }
    }

}