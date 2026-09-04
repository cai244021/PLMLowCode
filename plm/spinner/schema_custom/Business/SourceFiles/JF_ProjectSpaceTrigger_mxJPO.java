import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dscn.plm.util.NioJDUtils;
import com.matrixone.apps.common.Task;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UIUtil;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @description: 和项目有关的代码
 * @param: null
 * @return:
 * @author JJS
 * @date:  16:11
 */
public class JF_ProjectSpaceTrigger_mxJPO {

    private static final Logger log = LoggerFactory.getLogger(JF_ProjectSpaceTrigger_mxJPO.class);

    private static final String STRING_FILE_SHARE_PATH = "dataOutSource.share.path";

    private static final String STRING_SEND_DATA = "SendData";
    /**
     * @Author Liuxg
     * @Description 创建项目后发送邮件通知
     * @Date 2025/10/17 7:59
     * @Param [context, args]
     * @return void
     **/
    public void CreateSengEmail(Context context,String[]args)throws Exception{
        String projectid=args[0];

        Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
        String linkAddress = prop.getProperty("JF.3dspace.JFUrl").trim();
        linkAddress += projectid;

        DomainObject projectobj=DomainObject.newInstance(context,projectid);
        String projectName =projectobj.getInfo(context, DomainConstants.SELECT_DESCRIPTION);
        String projectID =projectobj.getName(context);
        if(UIUtil.isNullOrEmpty(projectName)){
            projectName =projectobj.getDescription(context);
        }else if(UIUtil.isNullOrEmpty(projectName)){
            projectName =projectobj.getDescription();
        }

        log.info("CreateSengEmail---projectid--->"+projectid);
        log.info("CreateSengEmail---projectName--->"+projectName);

        MimeMultipart multipart = new MimeMultipart(); // 默认混合模式
        //邮件内容的html模板部分
        BodyPart msgBodyPart = new MimeBodyPart();
        //邮件内容的html模板部分
        String html = JF_PublicMethodClass_mxJPO.getPageHTMLResourceFile(context, "JFLibAdminExpertPersonEmail", "zh");
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        doc.getElementById("projectName").append(projectName);

        //设置链接地址
        Element address = doc.getElementById("Address");
        //链接href
        address.attr("href", linkAddress);
        //设置显示的值
        address.text( "点击打开项目:"+projectID);


        String htmlContent = doc.toString();
        msgBodyPart.setContent(htmlContent, "text/html;charset=utf-8");//html代码部分
        multipart.addBodyPart(msgBodyPart);

        String Address = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JFLibAdmin.IT.Email"});
        JF_SendEmailUtils_mxJPO.SendEmail(context,Address,"通知",multipart);

    }


    public void JFCreateProjectSendBI(Context context,String[]args)throws Exception{
        JPO.invoke(context, "JF_ProjectSpace",new String[]{} , "JFCreateProjectSendBI", args, void.class);
    }


    public void triggerJSdataProcessProgressModAction(Context context,String[]args)throws Exception{

        try {

            String OBJECTID=args[0];
            String ATTRNAME=args[1];
            String ATTRVALUE=args[2];
            String NEWATTRVALUE=args[3];
            String OLDATTRVALUE=args[4];


            log.info("triggerJSdataProcessProgressModAction---->");
            log.info("OBJECTID---->"+OBJECTID);
            log.info("ATTRNAME---->"+ATTRNAME);
            log.info("ATTRVALUE---->"+ATTRVALUE);
            log.info("NEWATTRVALUE---->"+NEWATTRVALUE);
            log.info("OLDATTRVALUE---->"+OLDATTRVALUE);

            //此处OBJECTID可能为空？

            if("CAAProcessingComplete".equalsIgnoreCase(NEWATTRVALUE)){
                DomainObject obj=DomainObject.newInstance(context,OBJECTID);
                String type=obj.getTypeName(context);
                if("JFPartList".equals(type)){
                    JPO.invoke(context,"JF_ProjectSpace",null,"getPartListinfoSendSRM", new String[]{OBJECTID});
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
