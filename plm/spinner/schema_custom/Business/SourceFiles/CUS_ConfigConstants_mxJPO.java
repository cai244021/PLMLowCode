import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.Page;

import java.io.*;
import java.util.Properties;

/*
* 系统配置信息中心
* */
public class CUS_ConfigConstants_mxJPO {

    //定义系统客制化配置信息 page对象
    public  static final String configPageName = "JFJDConfig";
    //测试
    //public  static final String configPageName = "Test_CUS_ConfigPage.properties";

    public Properties CUS_ConfigConstants_mxJPO(Context context,String args[]){

        return  getPagePropertiesValue(context,new String[]{configPageName}) ;
    }
   // public static Properties config_properties =new Properties();
    public static Properties getPagePropertiesValue(Context context, String[] args) {
        String pageName=configPageName;
        if(args!=null&&args.length>0){
            String  inputPageName=args[0];
            System.out.println("input inputPageName>> :"+inputPageName);

           if (UIUtil.isNotNullAndNotEmpty(inputPageName)){
               pageName=inputPageName;
           }else{
               System.out.println("input pageName is null set default pageName>>"+pageName);
           }
        }else {
            System.out.println("default pageName>>"+pageName);
        }

        InputStream inputStream = null;
        Properties properties = new Properties();
        try {
            if (UIUtil.isNullOrEmpty(pageName) || UIUtil.isNullOrEmpty(pageName)) {
                return properties;
            }
            //读取page
            Page page = new Page(pageName);
            page.open(context);
            //获取page对象生成的输入流
            inputStream = page.getContentsAsStream(context);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));

            page.close(context);
            //用Properties读取输入流
            properties.load(br);
            //通过key获取对应的value
            inputStream.close();
            br.close();
        } catch (Exception e) {
            System.out.println(pageName +" 读取配置文件出错 请联系管理员!!!!");
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
       //System.out.println("getPagePropertiesValue >> "+properties);
        return properties;
    }



    public void testPage(Context context, String[] args) {
        try{
            Properties  properties=getPagePropertiesValue(context,args);
            String  allowSuffix= (String) properties.get("allowSuffix");
            String  onlyOfficeURL= (String) properties.get("onlyOfficeURL");
            System.out.println("onlyOfficeURL"+onlyOfficeURL);
            System.out.println("allowSuffix"+allowSuffix);
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public static String getPageValue(Context context, String pageName, String key) {
        String value = "";
        InputStream inputStream = null;
        try {
            if (UIUtil.isNullOrEmpty(pageName) || UIUtil.isNullOrEmpty(key)) {
                return value;
            }
            //读取page
            Page page = new Page(pageName);
            page.open(context);
            //获取page对象生成的输入流
            inputStream = page.getContentsAsStream(context);
            page.close(context);
            //用Properties读取输入流
            Properties properties = new Properties();
            properties.load(inputStream);
            //通过key获取对应的value
            value = properties.getProperty(key);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }

    /**
     * 获取项目路径
     * @return
     */
    public String getProjectPath(){
        String projectPath = "";
        String projectResourcePath = getClass().getResource("/").getFile().toString();
        File newFile=new File(projectResourcePath);
        projectPath=newFile.getParentFile().getParent()+"/";
        return projectPath;
    }

}
