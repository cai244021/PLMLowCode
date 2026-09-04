import com.matrixone.apps.domain.DomainObject;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;
import matrix.db.*;
import matrix.util.StringList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class JF_loginPassport_mxJPO {
    static final String kernelServlet = "/servlet/MatrixXMLServlet";
    /**
     * @description  本地登录3DE
     * 需要 URL 用户名 和密码
     * @author caipan
     * @param[1] args
     * @throws

     * @time 2023/8/9 19:05
     */
    public static void main(String[] args) throws Exception{
        String host ="https://r2024.v6.com/3dspace";
        String userName ="admin_platform";
        String userPass ="Aa123456";
        System.out.println(new Date());
        String ticket = getTicket(host, userName, userPass);
        System.out.println(new Date());
        System.out.println("login ticket = = = = = >>>>{}"+ticket);
        Context context = new Context(host + ticket);
        System.out.println(new Date());
        context.setUser(userName);
        context.setLocale(Locale.CHINESE);
        System.out.println(new Date());
        context.setRole("ctx::VPLMCreator.Company Name.Common Space");
        context.connect();
        System.out.println(context.getUser());
        DomainObject obj = DomainObject.newInstance(context);
        obj.setId("59002.37410.57446.31432");
        System.out.println(obj.getInfo(context, "current"));
    }


    public void testConnection(Context context,String[] args) throws Exception{
        {
            String host ="https://r2024.v6.com/3dspace";
            String userName ="admin_platform";
            String userPass ="Aa123456";
            System.out.println(new Date());
            String ticket = getTicket(host, userName, userPass);
            System.out.println(new Date());
            System.out.println("login ticket = = = = = >>>>{}"+ticket);
             context = new Context(host + ticket);
            System.out.println(new Date());
            context.setUser(userName);
            context.setLocale(Locale.CHINESE);
            System.out.println(new Date());
            context.setRole("ctx::VPLMCreator.Company Name.Common Space");
            context.connect();
            System.out.println(context.getUser());
            DomainObject obj = DomainObject.newInstance(context);
            obj.setId("59002.37410.57446.31432");
            System.out.println(obj.getInfo(context, "current"));

        }
    }
    public static void createDoc(Context context,String[] args) throws Exception{
        Map programMap = new HashMap();
        programMap.put("objectId", "75158E560BFE010064DA0498001C6B4B");//物理产品ID
        String[] doc = new String[1];
        doc[0]="75158E56C0FC010064DA091C000F6350"; //文档ID
        programMap.put("documentIds", doc);
        String[] methodargs = JPO.packArgs(programMap);
//        ContextUtil.pushContext(context);
        Vector hasDocEverAttached = (Vector) JPO.invoke(context, "VPLMDocument", null,"attachDocuments", methodargs, Vector.class);
        System.out.println(hasDocEverAttached+"=hasDocEverAttached");
//        ContextUtil.popContext(context);
    }
    /**
     * @description 得到对象的下一个状态
     * @author caipan
     * @param[1] context
     * @param[2] objId 对象的ID
     * @throws

     * @time 2023/8/14 14:24
     */
    public static String getNextState(Context context ,String objId) throws Exception{
        DomainObject obj = new DomainObject(objId);
        StringList selList = new StringList();
        selList.add("state");
        selList.add("current");
        Map map = obj.getInfo(context, selList);
        System.out.println(map.entrySet());
        StringList stateList = (StringList)map.get("state");
        String current = (String)map.get("current");
        String nextCurrent = "";
        if(stateList.contains(current)) {
            for (int i = 0; i < stateList.size(); i++) {
                if (current.equalsIgnoreCase(stateList.get(i)) && i < stateList.size() - 1) {
                    nextCurrent = stateList.get(i + 1);
                    break;
                }
            }
        }
        return nextCurrent;
    }

    static void setTrustManager(boolean useCertificates) throws Exception {
        SSLContext sc = SSLContext.getInstance("TLS");
        if (useCertificates) {
            sc.init(null, null, null);
        } else {
            sc.init(null, new TrustManager[] { new TrustAllTrustManager() }, null);
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    public static String getTicket(String host, String user, String password) throws Exception {
        setTrustManager(false);
        HttpURLConnection conTemp = getHttpConnection(host + "/servlet/MatrixXMLServlet", false);
        int respTemp = conTemp.getResponseCode();
        if (respTemp != 302) {
            if (respTemp == 400)
                throw new Exception("Error 400: Bad request");
            if (respTemp == 404)
                throw new Exception("Error 404: Not found");
            throw new Exception("Required CAS redirect not found");
        }
        String redirectUrl = conTemp.getHeaderField("Location");
        HttpURLConnection conCAS = getHttpConnection(redirectUrl, false);
        Map<String, String> cookiesCAS = getCASCookies(conCAS);
        StringBuilder cookies = new StringBuilder();
        String jSessionId = cookiesCAS.get("JSESSIONID");
        if (jSessionId != null)
            cookies.append("JSESSIONID=").append(jSessionId).append(";");
        String serverId = cookiesCAS.get("SERVERID");
        if (serverId != null)
            cookies.append("SERVERID=").append(serverId).append(";");
        String tenant = null;
        if (serverId != null && host.endsWith("enovia"))
            tenant = host.substring(host.indexOf("//") + 2, host.indexOf("-")).toUpperCase();
        String authParamsCAS = getAuthParams(conCAS);
        JSONObject jsonCAS = new JSONObject(authParamsCAS);
        String lt = (String)jsonCAS.get("lt");
        String loginUrlCAS = (String)jsonCAS.get("url");
        HttpURLConnection conCASLogin = getHttpConnection(loginUrlCAS, false);
        conCASLogin.setRequestProperty("Cookie", cookies.toString());
        conCASLogin.setRequestMethod("POST");
        conCASLogin.setDoOutput(true);
        Properties casUrlParamProperties = new Properties();
        casUrlParamProperties.put("lt", lt);
        casUrlParamProperties.put("username", user);
        casUrlParamProperties.put("password", password);
        String casUrlParams = encodeUrlParams(casUrlParamProperties);
        DataOutputStream wr = new DataOutputStream(conCASLogin.getOutputStream());
        try {
            wr.writeBytes(casUrlParams);
            wr.flush();
            wr.close();
        } catch (Throwable throwable) {
            try {
                wr.close();
            } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
            }
            throw throwable;
        }
        int respCodeCASLogin = conCASLogin.getResponseCode();
        if (respCodeCASLogin == 302) {
            String redirectUrlFromCASLogin = conCASLogin.getHeaderField("Location");

            if (redirectUrlFromCASLogin.contains("?ticket=")) {
                String ticket = redirectUrlFromCASLogin.substring(redirectUrlFromCASLogin.lastIndexOf("?ticket="));
                if (tenant != null)
                    ticket = ticket + "&tenant=" + ticket;
                return ticket;
            }
            throw new Exception("Required CAS Ticket not found");
        }
        String authParamsCASLogin = getAuthParams(conCASLogin);
        JSONObject jsonCASLogin = new JSONObject(authParamsCASLogin);
        JSONArray jsonArrayCASLogin = (JSONArray)jsonCASLogin.get("errorMsgs");
        String errorMsg = "";
        for (int i = 0; i < jsonArrayCASLogin.length(); i++) {
            JSONObject tmp = (JSONObject)jsonArrayCASLogin.get(i);
            errorMsg = errorMsg + errorMsg;
        }
        if (errorMsg.isEmpty())
            errorMsg = "Internal Server Error";
        throw new Exception(errorMsg);
    }

    private static HttpURLConnection getHttpConnection(String sUrl, boolean followRedirect) throws MalformedURLException, IOException, NoSuchAlgorithmException, KeyManagementException {
        URL url = new URL(sUrl);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setInstanceFollowRedirects(followRedirect);
        return con;
    }

    private static Map<String, String> getCASCookies(URLConnection con) {
        Map<String, String> cookies = new HashMap<>();
        Map<String, List<String>> headers = con.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String headerKey = entry.getKey();
            if (headerKey != null && headerKey.equalsIgnoreCase("Set-Cookie")) {
                for (String headerValue : entry.getValue()) {
                    if (headerValue == null)
                        continue;
                    String[] fields = headerValue.split(";\\s*");
                    String cookieValue = fields[0];
                    String[] a = cookieValue.split("=", 2);
                    cookies.put(a[0], a[1]);
                }
                break;
            }
        }
        return cookies;
    }

    private static String encodeUrlParams(Properties p) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        Enumeration<?> names = p.propertyNames();
        while (names.hasMoreElements()) {
            String name = (String)names.nextElement();
            String value = p.getProperty(name);
            sb.append("&").append(URLEncoder.encode(name, "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
        }
        return sb.delete(0, 1).toString();
    }

    private static String inputStreamToString(InputStream inputStream, Charset charset) throws IOException {
        StringWriter writer = new StringWriter();
        try {
            InputStreamReader reader = new InputStreamReader(inputStream, charset);
            try {
                reader.transferTo(writer);
                String str = writer.toString();
                reader.close();
                writer.close();
                return str;
            } catch (Throwable throwable) {
                try {
                    reader.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
                throw throwable;
            }
        } catch (Throwable throwable) {
            try {
                writer.close();
            } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
            }
            throw throwable;
        }
    }

    private static String getAuthParams(URLConnection con) throws ParserConfigurationException, SAXException, IOException {
        String authParams = null;
        String page = inputStreamToString(con.getInputStream(), StandardCharsets.UTF_8);
        page = page.replace("&egrave;", "").replace("&reg;", "").replace("sales to engineering.\">", "\"/>").replace("&egrave", "").replace("crossorigin>", "/>").replace("defer", " ");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(page)));
        NodeList nodes = doc.getElementsByTagName("script");
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == 1) {
                Element e = (Element)node;
                if (e.getAttribute("id").equals("configData") && e.getAttribute("type").equals("application/json")) {
                    authParams = e.getTextContent();
                    break;
                }
            }
        }
        return authParams;
    }

    public static class TrustAllTrustManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
    }


}
