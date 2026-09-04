import com.dscn.plm.util.NioJDUtils;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Author CHENYAN
 * @Date 2023/11/21 14:46
 * @Description 该类为对 office中转pdf进行了封装
 */
public class OfficeUtils_mxJPO {

    //日志
    private static final Logger _logger =  LoggerFactory.getLogger(OfficeUtils_mxJPO.class);

    //请求头content_type
    private static final String CONTENT_TYPE_FORM = "multipart/form-data; boundary=";

    //请求超时时间
    private static RequestConfig defaultConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(300000).build();

    //默认pdf服务器地址，当从page中方读取失败后使用该地址
    private static  final  String defaultUrl = "http://172.16.20.244:8080/office2pdf/pdf/convert";

    /**
     * 该方法不做任何处理 返回请求的json结果转换为Map
     * @param  file 转换文件
     * @return map key  state 状态 成功为 success
     *                  msg    结果消息
     *                  file     文件base64编码字符串
     *                  filename  pdf文件名
     *                  filesize  文件大小
     * @author CHENYAN
     * @date 2023/11/21 14:55
     */
    public static Map office2Pdf(Context context,File file) throws Exception{
        _logger.info("----------------------office2Pdf begin---------------------------------");
        //获取转ppd服务器地址
        String url = getPDFConvertServerUrl(context);
        Map res = new HashMap<>();
        //判空
        if (file == null || !file.exists()){
            res.put("state", "error");
            res.put("msg", "转换失败，文件不存在。");
            return res;
        }
        if (context == null){
            res.put("state", "error");
            res.put("msg", "转换失败,context不能为空。");
            return res;
        }
        if (!checkFileIsOffice(file)){
            res.put("state", "error");
            res.put("msg", "转换失败,文件类型不支持请选择文件后缀为xlsx,doc,docx,ppt,pptx");
            return res;
        }
        //发送请求
//        String strPostRes = doPostFile(getOfficeServerUrl(context), file, fileName);
        String strPostRes = doPostFile(url, file);
        //处理结果
        if (UIUtil.isNotNullAndNotEmpty(strPostRes)) {
            JSONObject convertJson = new JSONObject(strPostRes);

            //状态
            String state = convertJson.getString("state");
            //文件base64字符串
            String strFileBase64 = convertJson.getString("file");
            String strMsg = convertJson.getString("msg");
            String strFileSize = convertJson.getString("filesize");
            String strFileName = convertJson.getString("filename");
            strFileName = URLDecoder.decode(strFileName, "UTF-8");
            res.put("state", state);
            res.put("msg", strMsg);
            res.put("file", strFileBase64);
            res.put("filename", strFileName);
            res.put("filesize", strFileSize);
        }else {
            res.put("state", "error");
            res.put("msg", "转换失败，文件或文件名不符合要求。");
        }
        _logger.info("----------------------office2Pdf end---------------------------------");
        return res;
    }

    /**
    *
    *@description 文件转换pdf返回转换pdf全路径
    *@param context
	*@param file 文件
	*@param targetDirPath 目标文件夹路径
    *@return java.util.String 返回转换pdf文件全路径
    *@throws
    *@author CHENYAN
    *@date 2023/11/21 16:30
    *
    */
    public static String office2Pdf(Context context,File file ,String targetDirPath) throws Exception{
        FileOutputStream fos = null;
        if (UIUtil.isNullOrEmpty(targetDirPath)){
            throw new RuntimeException("目标文件夹不能为空");
        }
        Map resMap = office2Pdf(context, file);
        String strState = (String)resMap.get("state");
        if ("success".equals(strState)){
           try {
               String strFileBase64 = (String)resMap.get("file");
               String strFileName = (String)resMap.get("filename");
               byte[] fileBytes = Base64.getDecoder().decode(strFileBase64);
               //检查目标文件夹path是否需要补全 转换文件是否已经重名
               String strNewFileFullPath = checkDirAndFile(targetDirPath, strFileName);
               fos = new FileOutputStream(strNewFileFullPath);
               fos.write(fileBytes);
               fos.flush();
               return strNewFileFullPath;
           }finally {
               if (fos != null){
                   fos.close();
               }
           }
        }else {
            throw new RuntimeException("请求失败");
        }
    }

    /**
    *
    *@description office文件转换为pdf请求
    *@param url pdf转换work服务
	*@param file 文件
    *@return java.lang.String  响应json字符串
    *@throws
    *@author CHENYAN
    *@date 2023/11/22 10:48
    *
    */

    public static String doPostFile(String url, File file) throws IOException {
        _logger.info("----------------------- doPostFile begin ------------------------------");
        _logger.info("url:"+url);
        String result = "";
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse httpResponse = null;
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(defaultConfig);

        try {
            // 设置请求头 boundary边界不可重复，重复会导致提交失败
            StringBuffer sb  = new StringBuffer(CONTENT_TYPE_FORM);
            //边界
            StringBuffer boundary = new StringBuffer();
            boundary.append("-------------------------");
            boundary.append(System.currentTimeMillis());
            sb.append(boundary);
            String fileName = URLEncoder.encode(file.getName(), "UTF-8");
            httpPost.setHeader("Content-Type", sb.toString());
            httpPost.setHeader("filename", fileName);
            // 创建MultipartEntityBuilder
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            // 设置字符编码
            builder.setCharset(StandardCharsets.UTF_8);
            // 模拟浏览器
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            // 设置边界
            builder.setBoundary(boundary.toString());
            // 设置multipart/form-data流文件
            builder.addPart("file", new FileBody(file));
            // application/octet-stream代表不知道是什么格式的文件
            builder.addBinaryBody("media", file, ContentType.create("application/octet-stream"), fileName);
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            httpResponse = httpClient.execute(httpPost);
            HttpEntity responseEntity = httpResponse.getEntity();
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                BufferedReader reader = null;
                try{
                    reader = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
                    StringBuffer buffer = new StringBuffer();
                    String str = "";
                    while (!StringUtils.isEmpty(str = reader.readLine())) {
                        buffer.append(str);
                    }
                    result = buffer.toString();
                }finally {
                    //关闭流
                    if (reader != null){
                        reader.close();
                    }
                }


            }
            httpClient.close();
            if (httpResponse != null) {
                httpResponse.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        }
        _logger.info("----------------------- doPostFile end ------------------------------");
        return result;
    }


    /**
    *
    *@description 检查目标文件路径是否存在 末尾是否需要加文件分隔符， 检查文件名是否已经存在
    *@param targetDirPath 目标文件夹
	*@param fileName 转换后文件名
    *@return java.lang.String 返回不存在文件全路径
    *@throws
    *@author CHENYAN
    *@date 2023/11/21 16:58
    *
    */

    public static String checkDirAndFile(String targetDirPath ,String fileName){
        File dir = new File(targetDirPath);
        //创建文件夹
        if (!dir.exists()) {
            dir.mkdirs();
        }
        StringBuffer sb = new StringBuffer(targetDirPath);
        if (!targetDirPath.endsWith(File.separator)){
            sb.append(File.separator);
        }
        StringBuffer tempFileFullPath = new StringBuffer(sb).append(fileName);
        File file = new File(tempFileFullPath.toString());
        if (file.exists()) {
            String strNewFileName = UUID.randomUUID().toString().replace("-", "");
             String strSubFileType = fileName.substring(fileName.lastIndexOf("."));
             sb.append(strNewFileName);
             sb.append(strSubFileType);
        }else {
            sb.append(fileName);
        }
        return sb.toString();
    }
    /**
    *
    *@description 获取转pdf服务器地址
    *@param context
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2023/12/16 20:53
    */
    public static String getPDFConvertServerUrl(Context context) throws Exception{
        Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
        _logger.info("page配置服务器地址:"+prop.get("PDFConvertServer.address"));
        return (prop.get("PDFConvertServer.address") == null || prop.get("PDFConvertServer.address").equals("")) ? defaultUrl :(String)prop.get("PDFConvertServer.address");
    }
    public static boolean checkFileIsOffice(File file){
        if (file == null || !file.exists()){
            return false;
        }
        String strFileName = file.getName();
        _logger.info("strFileName:"+strFileName);
        return  (strFileName.endsWith(".xlsx") || strFileName.endsWith(".doc") ||strFileName.endsWith(".docx") || strFileName.endsWith(".ppt") || strFileName.endsWith(".pptx")) ? true :false;
    }
}
