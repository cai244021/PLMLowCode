import com.dassault_systemes.enovia.dpm.notification.NotificationBase;
import com.dassault_systemes.enovia.dpm.notification.NotificationUtil;
import com.dassault_systemes.enovia.e6wv2.foundation.FoundationException;
import com.dassault_systemes.enovia.e6wv2.foundation.FoundationUtil;
import com.dassault_systemes.enovia.e6wv2.foundation.util.StringUtil;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeAction;
import com.dassault_systemes.i3dx.client.notifications.NotificationClientUtil;
import com.dassault_systemes.i3dx.client.notifications.nomatrix.NotifConf;
import com.dscn.plm.util.NioJDUtils;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.common.TaskHolder;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.Task;
import com.matrixone.enovia.bps.notifications.NotificationService;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.json.*;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;

/**
 * @ClassName JF_NotificationUtils_mxJPO
 * @Author: LIUJR
 * @CreateDate: 2024/8/20 13:32
 * @UpdateRemark:
 * @Version: 1.0
 * @Description: 通知场景 OOTB的发送通知的工具
 */
public final class JF_NotificationUtils_mxJPO {
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_NotificationUtils_mxJPO.class);
    // 项目任务到期提醒只处理该日期及之后创建的任务。
    private static final LocalDate PROJECT_TASK_DUE_REMINDER_START_DATE = LocalDate.of(2026, 7, 1);

    private static JsonWriterFactory FACTORY_INSTANCE;

    protected static final String OBJECTTYPE = "objtype";

    protected static final String OBJECTNAME = "objname";

    protected static final String PROJECTNAME = "projectname";

    protected static final String USER = "user";

    protected static final String OWNER = "owner";

    protected static final String STATE = "state";

    protected static final String ASSIGNEE = "assignee";

    protected static final String OBJECTID = "objectId";

    protected static final String PHYSICALID = "physicalId";

    protected static final String KEY = "key";

    protected static final String NEXTSTATE = "nextstate";

    protected static final String PREVSTATE = "prevstate";

    protected static final String GROUPURI = "groupuri";

    protected static final String GROUPID = "groupid";

    protected static final String RELNAME = "relname";

    protected static final String SELECT_PHYSICALID = "physicalid";

    private static final String mqlGetEnv = "get env $1";
    protected static boolean isCloud = false;

    protected static final String TYPE_EXPERIMENT = "Experiment";

    protected static final String TYPE_PROJECT_BASELINE = "Project Baseline";

    private static final String SELECT_PROJECT_NAME_FROM_EXPERIMENT = "relationship[Experiment].from.name";

    private static final String SELECT_PROJECT_NAME_FROM_PROJECT_BASELINE = "relationship[Project Baseline].from.name";

    private static final String SELECT_ASSIGNEES_FROM_TASK = "relationship[Assigned Tasks].from.id";

    private static final String SELECT_GROUPURI_FROM_TASK = "relationship[Assigned Tasks].from.attribute[Group URI].value";

    private static final String RELATIONSHIP_ASSIGNED_CANDIDATE_TASKS = "Assigned Tasks Candidate";

    private static final String SELECT_CANDIDATE_ASSIGNEES_FROM_TASK = "relationship[Assigned Tasks Candidate].from.id";

    private static final String SELECT_CANDIDATE_GROUPURI_FROM_TASK = "relationship[Assigned Tasks Candidate].from.attribute[Group URI].value";

    private static final String SELECT_GROUP_URI = "attribute[" + DomainConstants.ATTRIBUTE_GROUPURI + "]";

    protected static final String CANDIDATEGROUPURI = "candidategroupuri";

    protected static final String CANDIDATEGROUPID = "candidategroupid";

    protected static final String CANDIDATEASSIGNEE = "candidateassignee";
    protected static boolean bCreateLink = true;
    protected long startTime = 0L;
    protected boolean LEGACY_MODE = true;

    public static HashMap controlMap = new HashMap();
    private static HashMap<String, HashMap<String, String>> notifyMap = new HashMap<String, HashMap<String, String>>() {
        {
            this.put("TASK_ASSIGN", new HashMap<String, String>() {
                {
                    this.put("CLASS", "com.dassault_systemes.enovia.dpm.notification.AssignmentNotification");
                    this.put("CHK_METHOD", "checkTaskAssign");
                    this.put("LEGACY_JPO", "emxProgramCentralUtil");
                    this.put("LEGACY_METHOD", "sendTaskMail");
                    this.put("DPM_NOTIFY_ID", "dpm.notifications.task.setting");
                    this.put("DPM_NOTIFY_MSG", "dpm.notifications.task.msg.assign");
                    this.put("SIMULIA_NOTIFY_ID", "sim.notifications.task.setting");
                    this.put("SIMULIA_NOTIFY_MSG", "sim.notifications.task.msg.assign");
                }
            });
            this.put("TASK_CREATE_PROMOTE", new HashMap<String, String>() {
                {
                    this.put("CLASS", "com.dassault_systemes.enovia.dpm.notification.AssignmentNotification");
                    this.put("CHK_METHOD", "checkTaskPromote");
                    this.put("LEGACY_JPO", "emxTaskBase");
                    this.put("LEGACY_METHOD", "notifyTaskAssignees");
                    this.put("DPM_NOTIFY_ID", "dpm.notifications.taskcreatetoassign.setting");
                    this.put("DPM_NOTIFY_MSG", "dpm.notifications.taskcreatetoassign.msg.promote");
                    this.put("SIMULIA_NOTIFY_ID", "sim.notifications.task.setting");
                    this.put("SIMULIA_NOTIFY_MSG", "sim.notifications.task.msg.assign");
                }
            });
            this.put("TASK_PROMOTE_ASSIGN_TO_ACTIVE", new HashMap<String, String>() {
                {
                    this.put("CLASS", "com.dassault_systemes.enovia.dpm.notification.AssignmentNotification");
                    this.put("CHK_METHOD", "checkTaskPromote");
                    this.put("DPM_NOTIFY_ID", "dpm.notifications.taskassigntoactive.setting");
                    this.put("DPM_NOTIFY_MSG", "dpm.notifications.taskassigntoactive.msg.promote");
                }
            });
        }
    };


    protected static long debug(String paramString, long paramLong) {
        if (paramLong == 0L)
            return 0L;
        long l = System.currentTimeMillis() - paramLong;
        NotificationBase.LOGGER.debug(paramString + " " + paramString);
        return l;
    }

    protected static HashMap<String, String> getTriggerEnv(Context paramContext) throws Exception {
        HashMap<Object, Object> hashMap = new HashMap<>();
        String str1 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "USER" });
        String str2 = null;
        String str3 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "TOTYPE" });
        String str4 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "TONAME" });
        String str5 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "TOOBJECTID" });
        String str6 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "TOPHYSICALID" });
        String str7 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "FROMNAME" });
        String str8 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "FROMPHYSICALID" });
        if (str7 != null && !str7.isEmpty() &&
                !str3.equalsIgnoreCase(DomainObject.TYPE_GROUP) && !str3.equalsIgnoreCase(ProgramCentralConstants.TYPE_GROUP_PROXY))
            try {
                String str = PersonUtil.getPersonObjectID(paramContext, str7);
            } catch (Exception exception) {
                exception.getMessage();
                str7 = "";
            }
        String str9 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "RELTYPE" });
        boolean bool = false;
        String str10 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "STATENAME" });
        String str11 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "NEXTSTATE" });
        String str12 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "OWNER" });
        String str13 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "POLICY" });
        if (str3 == null || str3.isEmpty())
            str3 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "TYPE" });
        if (str4 == null || str4.isEmpty())
            str4 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "NAME" });
        if (str5 == null || str5.isEmpty())
            str5 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "OBJECTID" });
        if (str6 == null || str6.isEmpty())
            str6 = MqlUtil.mqlCommand(paramContext, "get env $1", new String[] { "PHYSICALID" });
        if (ProgramCentralUtil.isNullString(str7) && ProgramCentralUtil.isNullString(str2)) {
            System.out.println("assigneeName and candidateAssigneeName are null or empty... ");
            bool = true;
            str7 = getAssigneesForObject(paramContext, str6);
            str2 = getAssigneesForObject(paramContext, str6, "Assigned Tasks Candidate");
            if (ProgramCentralUtil.isNullString(str7) && ProgramCentralUtil.isNullString(str2) && ProgramCentralUtil.isNotNullString(str8)) {
                System.out.println("key = UNASSIGN_TASK_CANDIDATE...");
                String str15 = "print bus $1 select $2 dump $3";
                String str16 = MqlUtil.mqlCommand(paramContext, str15, new String[] { str8, "type", "," });
                if ("Assigned Tasks Candidate".equalsIgnoreCase(str9) && (DomainConstants.TYPE_GROUP
                        .equalsIgnoreCase(str16) || ProgramCentralConstants.TYPE_GROUP_PROXY.equalsIgnoreCase(str16))) {
                    if (isCloud) {
                        String str = MqlUtil.mqlCommand(paramContext, str15, new String[] { str8, SELECT_GROUP_URI, "," });
                        str2 = str;
                    } else {
                        List<Map<String, String>> list = ProgramCentralUtil.getUserGroupMemberList(paramContext, str8);
                        if (list != null && !list.isEmpty()) {
                            Map map = list.get(0);
                            str2 = (String)map.get("name");
                            byte b;
                            int i;
                            for (b = 1, i = list.size(); b < i; b++) {
                                Map map1 = list.get(b);
                                String str = (String)map1.get("name");
                                str2 = str2 + "," + str2;
                            }
                        }
                    }
                } else {
                    str7 = MqlUtil.mqlCommand(paramContext, str15, new String[] { str8, "name", "," });
                }
                System.out.println("assigneeType :: " + str16);
            }
        } else if (!isCloud) {
            String str = null;
            if (ProgramCentralUtil.isNotNullString(str8)) {
                String str15 = "print bus $1 select $2 dump $3";
                str = MqlUtil.mqlCommand(paramContext, str15, new String[] { str8, "type", "," });
            }
            System.out.println("(!NotificationBase.isCloud) assigneeType :: " + str);
            if (DomainConstants.TYPE_GROUP.equalsIgnoreCase(str) || ProgramCentralConstants.TYPE_GROUP_PROXY.equalsIgnoreCase(str)) {
                System.out.println("INSIDE ELSE assigneeName... ");
                List<Map<String, String>> list = ProgramCentralUtil.getUserGroupMemberList(paramContext, str8);
                if (list != null && !list.isEmpty()) {
                    Map map = list.get(0);
                    str2 = (String)map.get("name");
                    byte b;
                    int i;
                    for (b = 1, i = list.size(); b < i; b++) {
                        Map map1 = list.get(b);
                        String str15 = (String)map1.get("name");
                        str2 = str2 + "," + str2;
                    }
                }
            }
        }
        System.out.println("assigneeName... " + str7);
        System.out.println("candidateAssigneeName... " + str2);
        System.out.println("relationshipName... " + str9);
        if (str10 == null || str10.isEmpty())
            str10 = getStateForObject(paramContext, str6);
        String str14 = (str6 == null) ? "" : getProjectForObject(paramContext, str6);
        System.out.println("physicalId... " + str6);
        if (bool) {
            hashMap.put("assignee", str7);
            hashMap.put("groupuri", str7);
            hashMap.put("candidateassignee", str2);
            hashMap.put("candidategroupuri", str2);
        } else if ("Assigned Tasks Candidate".equalsIgnoreCase(str9)) {
            hashMap.put("candidateassignee", str2);
            hashMap.put("candidategroupuri", str2);
        } else {
            hashMap.put("assignee", str7);
            hashMap.put("groupuri", str7);
        }
        hashMap.put("objtype", str3);
        hashMap.put("objname", str4);
        hashMap.put("objectId", str5);
        hashMap.put("physicalId", str6);
        hashMap.put("owner", str12);
        hashMap.put("projectname", str14);
        hashMap.put("state", str10);
        hashMap.put("nextstate", str11);
        if (!hashMap.containsKey("assignee"))
            hashMap.put("assignee", str7);
        for (Object str : hashMap.keySet())
            NotificationBase.LOGGER.debug("Env:\t" + str + "\tValue:\t" + (String)hashMap.get(str));
        System.out.println("controlMap in getTriggerEnv :: " + hashMap);
        return (HashMap)hashMap;
    }

    protected static HashMap<String, String> getObjectEnv(Context paramContext, String paramString) throws Exception {
        HashMap<Object, Object> hashMap = new HashMap<>();
        DomainObject domainObject = new DomainObject(paramString);
        StringList stringList1 = new StringList(8);
        stringList1.add("id");
        stringList1.add("type");
        stringList1.add("name");
        stringList1.add("owner");
        stringList1.add("policy");
        stringList1.add("current");
        stringList1.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
        Map map = domainObject.getInfo(paramContext, stringList1);
        String str1 = (String)map.get("type");
        String str2 = (String)map.get("name");
        String str3 = (String)map.get("id");
        String str4 = (String)map.get("owner");
        String str5 = (String)map.get("policy");
        String str6 = (String)map.get("current");
        String str7 = (String)map.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
        String str8 = getAssigneesForObject(paramContext, paramString);
        boolean bool = isCloud;
        System.out.println("isCloud in getObjectEnv :: " + bool);
        if ("true".equalsIgnoreCase(str7) && paramString != null && isCloud) {
            Map<String, String> map1 = getGroupURIAssigneeForObject(paramContext, paramString, str8, null);
            if (!map1.isEmpty()) {
                String str = map1.get("groupURI");
                if (!str.isEmpty() && str != null)
                    hashMap.put("groupuri", str);
                str8 = map1.get("assigneeName");
                hashMap.put("assignee", str8);
            }
            Map<String, String> map2 = getGroupURIAssigneeForObject(paramContext, paramString, str8, "Assigned Tasks Candidate");
            if (!map2.isEmpty()) {
                String str11 = map2.get("groupURI");
                if (!str11.isEmpty() && str11 != null)
                    hashMap.put("candidategroupuri", str11);
                String str12 = map2.get("assigneeName");
                hashMap.put("candidateassignee", str12);
            }
        }
        StringList stringList2 = getStates(paramContext, str5);
        int i = stringList2.indexOf(str6);
        int j = stringList2.size();
        String str9 = "";
        if (i == j - 1) {
            str9 = str6;
        } else {
            str9 = (i < j) ? (String)stringList2.get(i + 1) : str6;
        }
        String str10 = (paramString == null) ? "" : getProjectForObject(paramContext, paramString);
        if (str10.isEmpty())
            if ("Experiment".equalsIgnoreCase(str1)) {
                str10 = domainObject.getInfo(paramContext, "relationship[Experiment].from.name");
            } else if ("Project Baseline".equalsIgnoreCase(str1)) {
                str10 = domainObject.getInfo(paramContext, "relationship[Project Baseline].from.name");
            }
        hashMap.put("objtype", str1);
        hashMap.put("objname", str2);
        hashMap.put("objectId", str3);
        hashMap.put("physicalId", paramString);
        hashMap.put("owner", str4);
        hashMap.put("projectname", str10);
        hashMap.put("state", str6);
        hashMap.put("nextstate", str9);
        if (!hashMap.containsKey("assignee"))
            hashMap.put("assignee", str8);
        for (Object str : hashMap.keySet())
            NotificationBase.LOGGER.debug("Env:\t" + str + "\tValue:\t" + (String)hashMap.get(str));
        System.out.println("controlMap in getObjectEnv :: " + hashMap);
        return (HashMap)hashMap;
    }

    protected static String getNotificationURL(Context paramContext, long paramLong) {
        NotificationService notificationService = new NotificationService(paramContext);
        String str1 = notificationService.get3DNotificationsURL();
        String str2 = str1;
        if (str1 == null || str1.isEmpty()) {
            str1 = System.getenv("ODTServerNotificationsUrl");
            str2 = str1 + "/api/notify";
        }
        System.out.println(str2);
        return str2;
    }

    protected static String getFullName(Context paramContext) {
        String str = paramContext.getUser();
        try {
            str = PersonUtil.getFullName(paramContext, paramContext.getUser());
        } catch (Exception exception) {
            NotificationBase.LOGGER.debug("No FullName for: " + paramContext.getUser());
        }
        return str;
    }

    protected static String getFullName(Context paramContext, String paramString) {
        String str = paramString;
        try {
            str = PersonUtil.getFullName(paramContext, paramString.trim());
        } catch (Exception exception) {
            NotificationBase.LOGGER.debug("No FullName for: " + paramString);
        }
        return str;
    }

    protected static Map<String, String> getGroupURIAssigneeForObject(Context paramContext, String paramString1, String paramString2, String paramString3) throws Exception {
        System.out.println("for cloud test assigneeNames in getGroupURIForObject :: " + paramString2);
        HashMap<Object, Object> hashMap = new HashMap<>();
        StringList stringList1 = new StringList(2);
        if ("Assigned Tasks Candidate".equalsIgnoreCase(paramString3)) {
            stringList1.add("relationship[Assigned Tasks Candidate].from.id");
            stringList1.add("relationship[Assigned Tasks Candidate].from.attribute[Group URI].value");
        } else {
            stringList1.add("relationship[Assigned Tasks].from.id");
            stringList1.add("relationship[Assigned Tasks].from.attribute[Group URI].value");
        }
        String[] arrayOfString = { paramString1 };
        StringList stringList2 = new StringList();
        StringList stringList3 = new StringList();
        BusinessObjectWithSelectList businessObjectWithSelectList = BusinessObject.getSelectBusinessObjectData(paramContext, arrayOfString, stringList1);
        int i = businessObjectWithSelectList.size();
        if ("Assigned Tasks Candidate".equalsIgnoreCase(paramString3)) {
            for (byte b = 0; b < i; b++) {
                BusinessObjectWithSelect businessObjectWithSelect = (BusinessObjectWithSelect)businessObjectWithSelectList.getElement(b);
                stringList2 = businessObjectWithSelect.getSelectDataList("relationship[Assigned Tasks Candidate].from.id");
                stringList3 = businessObjectWithSelect.getSelectDataList("relationship[Assigned Tasks Candidate].from.attribute[Group URI].value");
            }
        } else {
            for (byte b = 0; b < i; b++) {
                BusinessObjectWithSelect businessObjectWithSelect = (BusinessObjectWithSelect)businessObjectWithSelectList.getElement(b);
                stringList2 = businessObjectWithSelect.getSelectDataList("relationship[Assigned Tasks].from.id");
                stringList3 = businessObjectWithSelect.getSelectDataList("relationship[Assigned Tasks].from.attribute[Group URI].value");
            }
        }
        if (stringList2 != null && !stringList2.isEmpty() && stringList3 != null &&
                !stringList3.isEmpty()) {
            String str = "";
            int j = stringList3.size();
            for (byte b = 0; b < j; b++) {
                String str1 = (String)stringList3.get(b);
                if (!str1.isEmpty())
                    str = (str.trim() == "") ? str1 : (str + "," + str);
            }
            hashMap.put("groupURI", str);
            if (paramString2 != null && !paramString2.isEmpty()) {
                List<String> list = null;
                MapList mapList1 = new MapList();
                MapList mapList2 = new MapList();
                if (paramString2.contains(",")) {
                    list = StringUtil.splitString(paramString2, ",");
                } else {
                    list.add(paramString2);
                }
                int k = stringList2.size();
                int m;
                for (m = 0; m < k; m++) {
                    String str2 = (String)stringList2.get(m);
                    MapList mapList = (MapList)ProgramCentralUtil.getUserGroupMemberList(paramContext, str2);
                    if (null != mapList && !mapList.isEmpty())
                        mapList2.addAll((Collection)mapList);
                }
                if (!mapList2.isEmpty()) {
                    m = mapList2.size();
                    for (byte b1 = 0; b1 < m; b1++) {
                        Map map = (Map)mapList2.get(b1);
                        String str2 = (String)map.get("name");
                        if (list.contains(str2))
                            list.remove(str2);
                    }
                }
                String str1 = String.join(",", (Iterable)list);
                paramString2 = str1;
                hashMap.put("assigneeName", paramString2);
            }
        }
        System.out.println("map at end of getGroupURIForObject :: " + hashMap);
        return (Map)hashMap;
    }

    protected static String getAssigneesForObject(Context paramContext, String paramString) throws Exception {
        return getAssigneesForObject(paramContext, paramString, null);
    }

    protected static String getAssigneesForObject(Context paramContext, String paramString1, String paramString2) throws Exception {
        System.out.println(" in getAssigneesForObject :: " + paramString2);
        StringBuffer stringBuffer = new StringBuffer();
        String str1 = new String();
        if (paramString2 == null || paramString2.isEmpty())
            paramString2 = DomainConstants.RELATIONSHIP_ASSIGNED_TASKS;
        if (paramString1 == null || paramString1.isEmpty())
            return "";
        StringList stringList = new StringList();
        stringList.add("name");
        stringList.add("id");
        stringList.add("attribute[" + DomainConstants.ATTRIBUTE_GROUPURI + "]");
        DomainObject domainObject = new DomainObject(paramString1);
        String str2 = domainObject.getInfo(paramContext, "type");
        if ("Experiment".equalsIgnoreCase(str2) || "Project Baseline".equalsIgnoreCase(str2)) {
            str1 = paramContext.getUser();
            return str1;
        }
        if ("risk".equalsIgnoreCase(str2) || "opportunity".equalsIgnoreCase(str2))
            paramString2 = "Assigned Risk";
        boolean bool = isCloud;
        System.out.println("isCloud in getAssigneesForObject :: " + bool);
        MapList mapList = new MapList();
        if (isCloud) {
            mapList = domainObject.getRelatedObjects(paramContext, paramString2, DomainConstants.TYPE_PERSON + "," + DomainConstants.TYPE_PERSON + "," + DomainConstants.TYPE_GROUP, stringList, null, true, false, (short)1, "", "", 0);
            System.out.println("mlAssignees if isCloud true :: " + mapList);
        } else {
            Task task = new Task();
            task.setId(paramString1);
            mapList = task.getAssignees(paramContext, stringList, null, "", paramString2, true);
            System.out.println("mlAssignees if isCloud false :: " + mapList);
        }
        for (byte b = 0; b < mapList.size(); b++) {
            Map map = (Map)mapList.get(b);
            String str = (String)map.get("name");
            if (bool && "Assigned Tasks Candidate".equalsIgnoreCase(paramString2))
                str = (String)map.get("attribute[" + DomainConstants.ATTRIBUTE_GROUPURI + "]");
            stringBuffer.append(str);
            if (b < mapList.size() - 1)
                stringBuffer.append(",");
        }
        if (stringBuffer.length() > 0)
            str1 = stringBuffer.toString();
        System.out.println("assignees in getAssigneesForObject :: " + str1);
        return str1;
    }

    protected static String getMitigatedRiskOwner(Context paramContext, String paramString) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        String str1 = new String();
        if (paramString == null || paramString.isEmpty())
            return "";
        StringList stringList = new StringList();
        stringList.add("name");
        stringList.add("owner");
        DomainObject domainObject = new DomainObject(paramString);
        String str2 = "Contributes To";
        MapList mapList = domainObject.getRelatedObjects(paramContext, str2, "Risk", stringList, null, true, false, (short)1, "", "", 0);
        for (byte b = 0; b < mapList.size(); b++) {
            Map map = (Map)mapList.get(b);
            stringBuffer.append((String)map.get("owner"));
            if (b < mapList.size() - 1)
                stringBuffer.append(",");
        }
        if (stringBuffer.length() > 0)
            str1 = stringBuffer.toString();
        return str1;
    }

    protected static boolean isValidType(Context paramContext, String paramString) throws Exception {
        boolean bool = false;
        String str1 = "print type $1 select derivative dump $2";
        String str2 = MqlUtil.mqlCommand(paramContext, str1, new String[] { DomainObject.TYPE_TASK_MANAGEMENT, "|" });
        StringList stringList = FrameworkUtil.split(str2, "|");
        String[] arrayOfString = { "Task", "Opportunity", "Risk", "Project Space", "VV_Test_Execution", "Experiment", "Project Baseline" };
        stringList.addAll(arrayOfString);
        if (paramString != null && !paramString.isEmpty() &&
                stringList.contains(paramString))
            bool = true;
        return bool;
    }

    protected static String getStateForObject(Context paramContext, String paramString) throws Exception {
        String str = new String();
        if (paramString == null || paramString.isEmpty())
            return "";
        DomainObject domainObject = new DomainObject(paramString);
        str = domainObject.getInfo(paramContext, "current");
        return str;
    }

    protected static StringList getStates(Context paramContext, String paramString) throws Exception {
        StringList stringList = new StringList();
        if (paramString != null && !paramString.isEmpty())
            try {
                String str = MqlUtil.mqlCommand(paramContext, "print policy $1 select $2 dump $3", new String[] { paramString, "state", "," });
                StringList stringList1 = FrameworkUtil.split(str, ",");
                for (String str1 : stringList1)
                    stringList.add(str1);
            } catch (MatrixException matrixException) {
                throw new FoundationException(matrixException);
            }
        return stringList;
    }

    protected static String getProjectForObject(Context paramContext, String paramString) throws Exception {
        String str = new String();
        if (paramString == null || paramString.isEmpty())
            return "";
        StringList stringList = new StringList();
        stringList.add("name");
        stringList.add("physicalid");
        DomainObject domainObject = new DomainObject(paramString);
        MapList mapList = domainObject.getRelatedObjects(paramContext, DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY, DomainConstants.TYPE_PROJECT_ACCESS_LIST, stringList, null, true, false, (short)1, "", "", 0);
        for (byte b = 0; b < mapList.size(); b++) {
            Map map = (Map)mapList.get(b);
            String str1 = (String)map.get("physicalid");
            if (str1 != null && !str1.isEmpty()) {
                DomainObject domainObject1 = new DomainObject(str1);
                MapList mapList1 = domainObject1.getRelatedObjects(paramContext, DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST, DomainConstants.TYPE_PROJECT_SPACE, stringList, null, false, true, (short)1, "", "", 0);
                for (byte b1 = 0; b1 < mapList1.size(); b1++) {
                    map = (Map)mapList1.get(b1);
                    str = (String)map.get("name");
                }
                if ("".equalsIgnoreCase(str) || str.isEmpty() || str == null)
                    str = domainObject1.getInfo(paramContext, "project");
            }
        }
        return str;
    }

    public static String getURL_Enovia(Context paramContext, String[] paramArrayOfString) {
        try {
            String str1 = paramArrayOfString[0];
            String str2 = paramArrayOfString[1];
            String str3 = "";
            str3 = generateEnoviaURL(paramContext, "ENOPRPR_AP", str1);
            return str3;
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println(exception.getMessage());
            NotificationBase.LOGGER.error(exception.getMessage());
            return null;
        }
    }

    public static String getURL_RiskWidget(Context paramContext, String[] paramArrayOfString) {
        try {
            String str1 = paramArrayOfString[0];
            String str2 = paramArrayOfString[1];
            String str3 = "";
            str3 = FoundationUtil.generate3DDashboardURL(paramContext, "ENORISK_AP", str1);
            return str3;
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println(exception.getMessage());
            NotificationBase.LOGGER.error(exception.getMessage());
            return null;
        }
    }

    public static String getURL_OpportunityWidget(Context paramContext, String[] paramArrayOfString) {
        try {
            String str1 = paramArrayOfString[0];
            String str2 = paramArrayOfString[1];
            String str3 = "";
            str3 = FoundationUtil.generate3DDashboardURL(paramContext, "ENXOPPY_AP", str1);
            return str3;
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println(exception.getMessage());
            NotificationBase.LOGGER.error(exception.getMessage());
            return null;
        }
    }

    public static String getURL_3DDashboard(Context paramContext, String[] paramArrayOfString) {
        try {
            String str1 = paramArrayOfString[0];
            String str2 = paramArrayOfString[1];
            String str3 = "";
            String str4 = "ENOTASK_AP";
            str3 = FoundationUtil.generate3DDashboardURL(paramContext, str4, str1);
            return str3;
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println(exception.getMessage());
            NotificationBase.LOGGER.error(exception.getMessage());
            return null;
        }
    }

    public static String getURL_Simulia(Context paramContext, String[] paramArrayOfString) {
        try {
            String str1 = paramArrayOfString[0];
            String str2 = paramArrayOfString[1];
            String str3 = "";
            str3 = FoundationUtil.generate3DDashboardURL(paramContext, "SIMTRCP_AP", str1);
            return str3;
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println(exception.getMessage());
            NotificationBase.LOGGER.error(exception.getMessage());
            return null;
        }
    }

    public static String generateEnoviaURL(Context paramContext, String paramString1, String paramString2) throws FoundationException {
        String str = null;
        Object object = null;
        try {
            NotificationService notificationService = new NotificationService(paramContext);
            String str1 = notificationService.get3DSpaceURL();
            String str2 = "physicalId";
            if (paramString1 == null) {
                str = str1 + "/common/emxNavigator.jsp?" + str1 + "=" + str2;
            } else {
                str = str1 + "/common/emxNavigator.jsp?" + str1 + "=" + str2 + "&appName=" + paramString2;
            }
            String str3 = paramContext.getTenant();
            if (str3 != null && !str3.isEmpty()) {
                if (!str1.toLowerCase().contains(str3.toLowerCase()))
                    str1 = str1.replace("https://", String.format("%s%s%s", new Object[] { "https://", str3.toLowerCase(), "-" }));
                str = str + "&tenant=" + str;
            }
        } catch (Exception exception) {
            str = null;
        }
        return str;
    }
    public static boolean processNotification(Context var0, String[] var1) throws Exception {
        String var2 = "";
        boolean var3 = true;
        boolean var4 = true;
        String var5 = "";
        String var6 = "";
        String var7 = "";
        var4 = JF_LOGGER.isDebugEnabled();
        isCloud = UINavigatorUtil.isCloud(var0);
        System.out.println("isCloud in processNotification :: " + isCloud);
        if (var1 != null && var1.length >= 2) {
            String[] var8 = var1;
            int var9 = var1.length;

            for(int var10 = 0; var10 < var9; ++var10) {
                String var11 = var8[var10];
                JF_LOGGER.debug("Trigger Input Arg: " + var11);
            }

            if (!var1[0].isEmpty()) {
                var2 = var1[0];
            }

            if (!var1[1].isEmpty()) {
                var3 = Boolean.parseBoolean(var1[1]);
            }

            if (var1.length >= 3 && !var1[2].isEmpty() && var1[2].length() > 20) {
                var5 = var1[2];
            }
        }

        if (var1.length >= 5 && !var1[3].isEmpty() && !var1[4].isEmpty()) {
            var6 = var1[3];
            var7 = var1[4];
        }

        if (var2.isEmpty()) {
            JF_LOGGER.debug("No notification type identified in the arguments - returning, no action");
            return false;
        } else {
            String var34 = (String)((HashMap)notifyMap.get(var2)).get("DISPATCH");
            Object var35 = new ArrayList();
            if (var34 != null && !var34.isEmpty()) {
                var35 = StringUtil.splitString(var34, ",");
            } else {
                ((List)var35).add(var2);
            }

            Iterator var36 = ((List)var35).iterator();

            while(true) {
                while(var36.hasNext()) {
                    var2 = (String)var36.next();
                    if (!var5.isEmpty()) {
                        System.out.println("getObjectEnv");
                        ((HashMap)notifyMap.get(var2)).putAll(getObjectEnv(var0, var5));
                    } else {
                        System.out.println("getTriggerEnv");
                        ((HashMap)notifyMap.get(var2)).putAll(getTriggerEnv(var0));
                    }

                    ((HashMap)notifyMap.get(var2)).put("user", getFullName(var0));
                    new HashMap();
                    HashMap var37 = (HashMap)notifyMap.get(var2);
                    String var12 = (String)var37.get("CLASS");
                    String var13 = (String)var37.get("CHK_METHOD");
                    Class var14 = Class.forName(var12);
                    Constructor var15 = var14.getConstructor(Context.class);
//                    JF_NotificationUtils_mxJPO var16 = (JF_NotificationUtils_mxJPO)var15.newInstance(var0);
//                    JF_LOGGER.debug("NOTIFICATION: " + var16.getClass().getName());
                    JF_NotificationUtils_mxJPO jfNotificationUtilsMxJPO1 = new JF_NotificationUtils_mxJPO();
                    controlMap = (HashMap)notifyMap.get(var2);
                    controlMap.put("key", var2);
//                    var16.setDebug(var0, var4);
//                    var16.setLegacy(var3);
                    String var17 = (String)controlMap.get("physicalId");
                    String var18 = (String)controlMap.get("objtype");
                    if (!isValidType(var0, var18)) {
                        JF_LOGGER.debug("NOTIFICATION: Bypassing object Type: " + var18);
                    } else {
                        String var19 = (String)controlMap.get("key");
                        String var20 = (String)controlMap.get("state");
                        String var21 = (String)controlMap.get("assignee");
                        String var22 = (String)controlMap.get("candidateassignee");
                        ArrayList var23 = new ArrayList();
                        var23.add("TASK_CREATE_PROMOTE");
                        var23.add("TASK_PROMOTE_ASSIGN_TO_ACTIVE");
                        var23.add("TASK_PROMOTE_ACTIVE_TO_REVIEW");
                        var23.add("TASK_PROMOTE_REVIEW_TO_COMPLETED");
                        var23.add("TASK_DEMOTE");
                        var23.add("TASK_START1");
                        var23.add("TASK_FINISH_2");
                        var23.add("TASK_ASSIGN");
                        var23.add("TASK_UNASSIGN");
                        var23.add("ASSIGN_TASK_CANDIDATE");
                        var23.add("UNASSIGN_TASK_CANDIDATE");
                        System.out.println("assignees :: " + var21);
                        System.out.println("candidateAssignees :: " + var22);
                        String var24;
                        String var25;
                        if (var23.contains(var19)) {
                            String var26;
                            if ((!"".equalsIgnoreCase(var6) || !var6.isEmpty()) && (!"".equalsIgnoreCase(var7) || !var7.isEmpty())) {
                                var24 = "emxFramework.State.Project_Task." + var7;
                                var25 = var0.getSession().getLanguage();
                                var7 = EnoviaResourceBundle.getProperty(var0, "Framework", var24, var25);
                                controlMap.put("prevstate", var7);
                                var26 = "emxFramework.State.Project_Task." + var6;
                                var6 = EnoviaResourceBundle.getProperty(var0, "Framework", var26, var25);
                                controlMap.put("nextstate", var6);
                            }

                            if (("TASK_PROMOTE_ASSIGN_TO_ACTIVE".equalsIgnoreCase(var19) || "TASK_PROMOTE_ACTIVE_TO_REVIEW".equalsIgnoreCase(var19)) && (var17 != null || !var17.isEmpty())) {
//                            if (("TASK_PROMOTE_ACTIVE_TO_REVIEW".equalsIgnoreCase(var19)) && (var17 != null || !var17.isEmpty())) {
                                var24 = (String)controlMap.get("owner");
                                if (!var24.isEmpty() || var24 != null) {
                                    if (!"".equalsIgnoreCase(var21) && !var21.isEmpty()) {
                                        if (!var21.contains(var24)) {
                                            var21 = var21 + "," + var24;
                                        }
                                    } else {
                                        var21 = var24;
                                    }

                                    controlMap.put("assignee", var21);
                                }
                            }

                            if (null != var21 && !var21.isEmpty()) {
                                String[] var38 = var21.split(",");
                                int var39 = var38.length;
                                if (var39 != 0) {
                                    var26 = "";

                                    for(int var27 = 0; var27 < var39; ++var27) {
                                        if (!var0.getUser().equalsIgnoreCase(var38[var27])) {
                                            if (!"".equalsIgnoreCase(var26) && !var26.isEmpty()) {
                                                var26 = var26 + "," + var38[var27];
                                            } else {
                                                var26 = var38[var27];
                                            }
                                        }
                                    }

                                    controlMap.put("assignee", var26);
                                }
                            }
                        }

                        if (var19.equalsIgnoreCase("RISK_MITIGATE_1")) {
                            var21 = getMitigatedRiskOwner(var0, var17);
                            controlMap.put("assignee", var21);
                        }

                        JF_NotificationUtils_mxJPO jfNotificationUtilsMxJPO = new JF_NotificationUtils_mxJPO();
                        System.out.println("controlMap in processNotification :: " + controlMap);
                        var24 = (String)controlMap.get("groupuri");
                        var25 = (String)controlMap.get("candidategroupuri");
                        if ((var21 == null || var21.isEmpty()) && (var22 == null || var22.isEmpty())) {
                            System.out.println("FUN117103 in processNotification");
                            if (!"TASK_FINISH_1".equalsIgnoreCase(var19) && !"TASK_START".equalsIgnoreCase(var19)) {
                                if (var24 == null || var25 == null) {
                                    continue;
                                }
                            } else {
                                jfNotificationUtilsMxJPO.sendLegacyNotification(var0, var1);
                            }
                        }

                        if ("RISK_ASSIGN".equalsIgnoreCase(var19) || "RISK_PROMOTE".equalsIgnoreCase(var19)) {
                            controlMap.put("DPM_NOTIFY_MSG", "dpm.notifications.risk.msg.assign");
                        }

                        if ("RISK_PROMOTE".equalsIgnoreCase(var19) && "Review".equalsIgnoreCase(var20)) {
                            controlMap.put("DPM_NOTIFY_MSG", "dpm.notifications.risk.msg.complete");
                        }

                        bCreateLink = true;
                        if (var19.equalsIgnoreCase("TASK_UNASSIGN") || var19.equalsIgnoreCase("UNASSIGN_TASK_CANDIDATE")) {
                            bCreateLink = false;
                        }

                        boolean var40 = false;

                        try {
                            Method var41 = jfNotificationUtilsMxJPO1.getClass().getDeclaredMethod(var13, Context.class, String.class);
                            var40 = (Boolean)var41.invoke(jfNotificationUtilsMxJPO1, var0, var17);
                            JF_LOGGER.debug("Notification check conditions: (" + var13 + ") " + String.valueOf(var40));
                        } catch (SecurityException var29) {
                            var29.getMessage();
                        } catch (NoSuchMethodException var30) {
                            var40 = true;
                        } catch (IllegalArgumentException var31) {
                            var40 = true;
                        } catch (IllegalAccessException var32) {
                            var32.getMessage();
                        } catch (InvocationTargetException var33) {
                            var33.getMessage();
                        }
                        if (var40) {
                            System.out.println("BEFORE call processNotificationJob");
                            JF_LOGGER.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                            JF_LOGGER.info("controlMap:{}", controlMap.toString());
                            JF_LOGGER.info("var1:{}", Arrays.stream(var1).toList());

                            if (!"TASK_CREATE_PROMOTE".equalsIgnoreCase(var19) && !"TASK_PROMOTE_ACTIVE_TO_REVIEW".equalsIgnoreCase(var19)) {
                                JF_LOGGER.info("@@@@@@@@@@@@@@@@@@@@@@@");
                                JF_LOGGER.info("controlMap:{}", controlMap.toString());
                                JF_LOGGER.info("var1:{}", Arrays.stream(var1).toList());

                                if (!"ASSIGN_TASK_CANDIDATE".equalsIgnoreCase(var19) && !"UNASSIGN_TASK_CANDIDATE".equalsIgnoreCase(var19)) {
                                    JF_LOGGER.info("1111111111111111111111111");
                                    jfNotificationUtilsMxJPO1.processNotificationJob(var0, var1);
                                } else {
                                    boolean var43 = true;
                                    JF_LOGGER.info("2222222222222222222222222");
                                    jfNotificationUtilsMxJPO1.processNotificationJob(var0, var1, var43);
                                }
                            } else {
                                JF_LOGGER.info("########################");
                                JF_LOGGER.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
                                JF_LOGGER.info("controlMap:{}", controlMap.toString());
                                JF_LOGGER.info("var1:{}", Arrays.stream(var1).toList());

                                jfNotificationUtilsMxJPO1.processNotificationJob(var0, var1);
                                JF_LOGGER.info("########################01");
                                String var42 = (String)controlMap.get("DPM_NOTIFY_MSG");
                                controlMap.put("DPM_NOTIFY_MSG", "dpm.notifications.candidate.taskcreatetoassigncandidate.msg.promote");
                                boolean var28 = true;
                                jfNotificationUtilsMxJPO1.processNotificationJob(var0, var1, var28);
                                controlMap.put("DPM_NOTIFY_MSG", var42);
                                JF_LOGGER.info("########################03");
                            }
                            JF_LOGGER.debug("Notification Dispatch Complete");
                        }
                    }
                }

                return true;
            }
        }
    }


    public boolean sendLegacyNotification(Context var1, String[] var2) {
        JF_LOGGER.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%55");
        JF_LOGGER.info("controlMap:{}", controlMap.toString() );
        String var3 = (String)this.controlMap.get("LEGACY_JPO");
        String var4 = (String)this.controlMap.get("LEGACY_METHOD");
        String[] var5 = new String[]{(String)this.controlMap.get("objectId"), (String)this.controlMap.get("state"), (String)this.controlMap.get("nextstate")};

        try {
            JF_LOGGER.info("sendLegacyNotification Started");
            if (null != var3 && !var3.isEmpty() && null != var4 && !var4.isEmpty()) {
                JPO.invoke(var1, var3, (String[])null, var4, var5);
            }

            JF_LOGGER.info("sendLegacyNotification Completed");
            return true;
        } catch (Exception var7) {
            System.out.println(var7.getLocalizedMessage());
            var7.printStackTrace();
            JF_LOGGER.error(var7.getLocalizedMessage());
            return false;
        }
    }

    protected void processNotificationJob(Context var1, String[] var2) {
        this.processNotificationJob(var1, var2, false);
    }

    protected void processNotificationJob(Context var1, String[] var2, boolean var3) {
        String var4;
        try {
            System.out.println("controlMap in processNotificationJob NEW :: " + this.controlMap);
            var4 = (String)this.controlMap.get("groupuri");
            String var5 = (String)this.controlMap.get("candidategroupuri");
            JF_LOGGER.info("3DNotification Started");
            JsonObject var6 = this.generate3DNotification(var1, false, var3);
            JF_LOGGER.info("jsonMsg for assignee :: {}" , var6);
            JsonArray var7 = (JsonArray)var6.get("items");
            JsonArray var8 = null;
            JsonObject var9;
            if (var7 != null && var7.size() > 0) {
                var9 = var7.getJsonObject(0);
                var8 = (JsonArray)var9.get("users_target");
            }

            JF_LOGGER.info("3DNotification User List: " + var8.toString());
            if (var8 != null && var8.size() > 0) {
                JF_LOGGER.info("3DNotification being Sent");
                JF_LOGGER.info("333333333333333333333333333333333333");

                this.send3DNotification(var1, var6);
            }

            if ((var4 != null || var5 != null) && isCloud) {
                var6 = this.generate3DNotification(var1, true, var3);
                JF_LOGGER.info("jsonMsg for group :: {}" , var6);
                var7 = (JsonArray)var6.get("items");
                var8 = null;
                if (var7 != null && var7.size() > 0) {
                    var9 = var7.getJsonObject(0);
                    var8 = (JsonArray)var9.get("users_groups");
                }

                JF_LOGGER.info("3DNotification User List for group: " + var8.toString());
                if (var8 != null && var8.size() > 0) {
                    JF_LOGGER.info("3DNotification being Sent");
                    JF_LOGGER.info("44444444444444444444444444444444444");
                    this.send3DNotification(var1, var6);
                }
            }

            JF_LOGGER.info("3DNotification Complete");
        } catch (Exception var11) {
            System.out.println(var11.getMessage());
            JF_LOGGER.info(var11.getMessage());
        }

        try {
            var4 = (String)this.controlMap.get("key");
            if ("TASK_START".equalsIgnoreCase(var4) || "TASK_FINISH_1".equalsIgnoreCase(var4) || !UINavigatorUtil.isCloud(var1) && this.getLegacyMode(var1)) {
                JF_LOGGER.info("Legacy Notification Started");
                JF_LOGGER.info("55555555555555555555555555555555555");
                this.sendLegacyNotification(var1, var2);
                JF_LOGGER.info("Legacy Notification Complete");
            }
        } catch (Exception var10) {
            System.out.println(var10.getMessage());
            JF_LOGGER.error(var10.getMessage());
        }

    }

    protected boolean getLegacyMode(Context var1) {
        return this.LEGACY_MODE;
    }


    protected JsonObject generate3DNotification(Context var1, boolean var2, boolean var3) throws Exception {
        JF_LOGGER.debug("generate3DNotification Started");
        JsonObject var4 = this.generateMessageBodyJSON(var1);
        JsonValue var5 = (JsonValue)var4.get("msg");
        if (var5.toString().contains("BYPASS")) {
            return null;
        } else {
            String var6 = var1.getTenant();
            if (var6 == null || var6.isEmpty()) {
                var6 = "OnPremise";
            }

            JsonObjectBuilder var7 = Json.createObjectBuilder();
            String var8 = var2 ? "users_groups" : "users_target";
            JsonArray var9 = var2 ? this.generateUserGroupListJSON(var1, var3) : this.generateUserListJSON(var1, var3);
            if (this.bCreateLink) {
                var7.add("items", Json.createArrayBuilder().add(Json.createObjectBuilder().add("type", "NOTIF_UI").add(var8, var9).add("platformID", var6).add("message", Json.createObjectBuilder().add("icon", this.generateIconJSON(var1)).add("nls", var4).add("actions", this.generateActionLink(var1)).build()).add("service", this.generateServiceJSON(var1)).build()).build()).build();
            } else {
                var7.add("items", Json.createArrayBuilder().add(Json.createObjectBuilder().add("type", "NOTIF_UI").add(var8, var9).add("platformID", var6).add("message", Json.createObjectBuilder().add("icon", this.generateIconJSON(var1)).add("nls", var4).build()).add("service", this.generateServiceJSON(var1)).build()).build()).build();
            }

            JsonObject var10 = var7.build();
            ByteArrayOutputStream var11 = new ByteArrayOutputStream();
            JsonWriter var12 = getPrettyJsonWriterFactory().createWriter(var11, Charset.forName("UTF-8"));
            var12.write(var10);
            var12.close();
            JF_LOGGER.debug("generateNotificationJSON");
            String var13 = "JSON:\n" + var11.toString();
            JF_NotificationUtils_mxJPO.debug(var13, this.startTime);
            return var10;
        }
    }

    private JsonArray generateActionLink(Context var1) throws Exception {
        this.getMessageLinkURL(var1);
        String var3 = this.getMessageLinkURI(var1);
        JsonArray var4 = Json.createArrayBuilder().add(Json.createObjectBuilder().add("type", "link").add("options", Json.createObjectBuilder().add("event", Json.createObjectBuilder().add("type", "url").add("options", Json.createObjectBuilder().add("service", "3DDashboard").add("uri", var3).add("type", "GET").build()).build()).build()).build()).build();
        JF_LOGGER.debug("generateActionLinkJSON");
        return var4;
    }

    protected String getMessageLinkURI(Context var1) {
        String var2 = "";

        try {
            String var3 = (String)this.controlMap.get("physicalId");
            String var4 = (String)this.controlMap.get("objtype");
            String var5 = "";
            if (!ProgramCentralConstants.TYPE_TASK.equalsIgnoreCase(var4) && !ProgramCentralConstants.TYPE_GATE.equalsIgnoreCase(var4) && !ProgramCentralConstants.TYPE_MILESTONE.equalsIgnoreCase(var4) && !ProgramCentralConstants.TYPE_PHASE.equalsIgnoreCase(var4)) {
                var5 = ProgramCentralConstants.TYPE_RISK.equalsIgnoreCase(var4) ? "ENORISK_AP" : (ProgramCentralConstants.TYPE_OPPORTUNITY.equalsIgnoreCase(var4) ? "ENXOPPY_AP" : "");
            } else {
                var5 = "ENOTASK_AP";
            }

            var2 = buildMessageActionURI(var1, var3, var5, var4);
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return var2;
    }

    private static String buildMessageActionURI(Context var0, String var1, String var2, String var3) throws Exception {
        String var4 = "/#app:" + var2 + "/content:X3DContentId=";
        String var5 = var0.getRole();
        String var6 = var0.getTenant();
        if (var6 == null || var6.isEmpty()) {
            var6 = "OnPremise";
        }

        JsonObject var7 = Json.createObjectBuilder().add("protocol", "3DXContent").add("version", "").add("source", var2).add("widgetId", "").add("data", Json.createObjectBuilder().add("items", Json.createArrayBuilder().add(Json.createObjectBuilder().add("envId", var6).add("serviceId", "3DSpace").add("objectId", var1).add("objectType", var3).add("SecurityContext", var5).build()).build()).build()).build();
        String var8 = var7.toString();
        var4 = var4 + URLEncoder.encode(var8, "UTF-8");
        return var4;
    }

    protected String getMessageLinkURL(Context var1) {
        String var2 = "";
        String var3 = (String)this.controlMap.get("objectId");
        String var4 = (String)this.controlMap.get("physicalId");
        String var5 = (String)this.controlMap.get("assignee");
        String var6 = (String)this.controlMap.get("objtype");

        try {
            String var7 = JF_NotificationUtils_mxJPO.getFullName(var1);
            String var8 = var1.getLocale().getLanguage();
            String[] var9 = new String[]{var4, var8};
            String var10 = NotificationUtil.getURL_3DDashboard(var1, var9);
            JF_LOGGER.debug("Dashboard URL: " + var10);
            String var11 = NotificationUtil.getURL_RiskWidget(var1, var9);
            JF_LOGGER.debug("Risk URL: " + var11);
            String var12 = NotificationUtil.getURL_Simulia(var1, var9);
            JF_LOGGER.debug("Simulia URL: " + var12);
            String var13 = NotificationUtil.getURL_Enovia(var1, var9);
            JF_LOGGER.debug("ENOVIA URL: " + var13);
            var2 = var13;
            if ("vv_test_execution".equalsIgnoreCase(var6)) {
                if (!UINavigatorUtil.isCloud(var1)) {
                    var12 = var12 + "&x3dPlatformId=OnPremise";
                }

                var2 = var12;
            }

            if (ProgramCentralConstants.TYPE_TASK.equalsIgnoreCase(var6) || ProgramCentralConstants.TYPE_GATE.equalsIgnoreCase(var6) || ProgramCentralConstants.TYPE_MILESTONE.equalsIgnoreCase(var6) || ProgramCentralConstants.TYPE_PHASE.equalsIgnoreCase(var6)) {
                if (!UINavigatorUtil.isCloud(var1)) {
                    var10 = var10 + "&x3dPlatformId=OnPremise";
                }

                var2 = var10;
            }

            if ("risk".equalsIgnoreCase(var6)) {
                if (!UINavigatorUtil.isCloud(var1)) {
                    var11 = var11 + "&x3dPlatformId=OnPremise";
                }

                var2 = var11;
            }

            if ("Experiment".equalsIgnoreCase(var6)) {
                var2 = var2 + "&headerLifecycle=hide";
            }
        } catch (Exception var14) {
            var14.printStackTrace();
            JF_LOGGER.error("URL Error: " + var14.getMessage());
        }

        return var2;
    }


    protected JsonObject generateIconJSON(Context var1) throws Exception {
        JsonObject var2 = Json.createObjectBuilder().add("type", "login").add("data", var1.getUser()).build();
        JF_LOGGER.debug("generateIconJSON");
        return var2;
    }

    private JsonObject generateServiceJSON(Context var1) throws Exception {
        JsonObject var2 = Json.createObjectBuilder().add("appID", "ENOPREX_AP").add("notificationID", (String)this.controlMap.get("DPM_NOTIFY_ID")).build();
        String var3 = (String)this.controlMap.get("objtype");
        if ("vv_test_execution".equalsIgnoreCase(var3)) {
            var2 = Json.createObjectBuilder().add("appID", "SIMTRCP_AP").add("notificationID", (String)this.controlMap.get("SIMULIA_NOTIFY_ID")).build();
        }

        JF_LOGGER.debug("generateServiceJSON");
        return var2;
    }

    private static JsonWriterFactory getPrettyJsonWriterFactory() {
        if (null == FACTORY_INSTANCE) {
            HashMap var0 = new HashMap(1);
            var0.put("jakarta.json.stream.JsonGenerator.prettyPrinting", true);
            FACTORY_INSTANCE = Json.createWriterFactory(var0);
        }

        return FACTORY_INSTANCE;
    }

    protected JsonArray generateUserListJSON(Context var1, boolean var2) {
        StringList var3 = this.getTargetUsers(var1, var2);
        JsonArrayBuilder var4 = Json.createArrayBuilder();
        if (var3 != null && !var3.isEmpty()) {
            Iterator var5 = var3.iterator();

            while(var5.hasNext()) {
                String var6 = (String)var5.next();
                if (var6 != null && !var6.isEmpty()) {
                    var4.add(var6);
                }
            }
        }

        JF_NotificationUtils_mxJPO.debug("generateUserListJSON:\t", this.startTime);
        return var4.build();
    }

    protected StringList getTargetUsers(Context var1, boolean var2) {
        StringList var3 = new StringList();
        String var4 = (String)this.controlMap.get("assignee");
        if (var2) {
            var4 = (String)this.controlMap.get("candidateassignee");
        }

        if (var4 != null) {
            String[] var5 = var4.split(",");

            for(int var6 = 0; var6 < var5.length; ++var6) {
                var3.add(var5[var6]);
            }
        }

        return var3;
    }



    protected JsonArray generateUserGroupListJSON(Context var1, boolean var2) {
        StringList var3 = this.getTargetUserGroupUsers(var1, var2);
        JsonArrayBuilder var4 = Json.createArrayBuilder();
        if (var3 != null && !var3.isEmpty()) {
            Iterator var5 = var3.iterator();

            while(var5.hasNext()) {
                String var6 = (String)var5.next();
                if (var6 != null && !var6.isEmpty()) {
                    var4.add(var6);
                }
            }
        }

        System.out.println("targetUserArray in generateUserGroupListJSON :: " + var4);
        JF_NotificationUtils_mxJPO.debug("generateUserGroupListJSON:\t", this.startTime);
        return var4.build();
    }

    protected StringList getTargetUserGroupUsers(Context var1, boolean var2) {
        StringList var3 = new StringList();
        String var4 = (String)this.controlMap.get("groupuri");
        if (var2) {
            var4 = (String)this.controlMap.get("candidategroupuri");
        }

        if (var4 != null) {
            String[] var5 = var4.split(",");

            for(int var6 = 0; var6 < var5.length; ++var6) {
                var3.add(var5[var6]);
            }
        }

        System.out.println("groupURIList in getTargetUserGroupUsers :: " + var3);
        return var3;
    }

    private final void printContext(Context var1, NotifConf var2) {
        String var3 = String.format("User Info:\n  %s\t%s\n  %s\t%s\n  %s\t%s\n   %s\t%d\n ", "context", var1.getUser(), "tenant", var1.getTenant(), "role", var1.getRole(), "notifytype", var2.getType());
        JF_NotificationUtils_mxJPO.debug(var3, this.startTime);
    }

    public static int getStatusCode(String var0) {
        boolean var1 = false;

        int var5;
        try {
            if (var0.charAt(0) == '[') {
                var0 = var0.substring(1, var0.length() - 1);
            }

            JsonReader var2 = Json.createReader(new StringReader(var0));
            JsonObject var3 = var2.readObject();
            var2.close();
            var5 = var3.getInt("statusCode");
        } catch (Exception var4) {
            var5 = 0;
        }

        return var5;
    }

    private final void send3DNotification(Context var1, JsonObject var2) {
        JF_NotificationUtils_mxJPO.debug("send3DNotification Start:\t", this.startTime);
        String var3 = "";
        String var4 = "";
        String var5 = "";

        try {
            String var6 = JF_NotificationUtils_mxJPO.getNotificationURL(var1, this.startTime);
            String var7 = "Notification URL: " + var6;
            JF_NotificationUtils_mxJPO.debug(var7, this.startTime);
            NotifConf var8 = NotifConf.getInstance();
            var8.init(var6, 60000);
            this.printContext(var1, var8);
            boolean var9 = false;
            if (var2 != null) {
                String var11;
                JsonObject var12;
                try {
                    JF_NotificationUtils_mxJPO.debug("Sending the notification:\t", this.startTime);
                    JF_NotificationUtils_mxJPO.debug(var2.toString(), this.startTime);
                    var3 = NotificationClientUtil.sendNotification(var2.toString(), var6, 60000);
                    System.out.println("Notification Response =" + var3);
                    boolean var10 = false;
                    int var17 = getStatusCode(var3);
                    System.out.println(String.format("'%s %d", "StatusCode =", var17));
                    if (var17 == 411 || var17 == 470 || var3.contains("notification.receive.error")) {
                        System.out.println("Processing Registration");
                        JsonObject var18;
                        if (var9) {
                            var18 = this.generate3DNotificationRegistration(var1, 1);
                            var12 = this.generate3DNotificationRegistration(var1, 2);
                            System.out.println("Registering Partial:");
                            System.out.println(var18.toString());
                            System.out.println(var12.toString());
                            var4 = NotificationClientUtil.sendNotification(var18.toString(), var6, 60000);
                            var4 = NotificationClientUtil.sendNotification(var12.toString(), var6, 60000);
                        } else {
                            var18 = this.generate3DNotificationRegistration(var1, 0);
                            System.out.println("Registering:");
                            System.out.println(var18.toString());
                            var4 = NotificationClientUtil.sendNotification(var18.toString(), var6, 60000);
                        }

                        var5 = "Notification CONFIG status:: " + var4;
                        System.out.println("outputMsg :: " + var5);
                        JF_NotificationUtils_mxJPO.debug(var5, this.startTime);
                        var3 = NotificationClientUtil.sendNotification(var2.toString(), var6, 60000);
                    }
                } catch (RuntimeException var14) {
                    var11 = var14.getMessage();
                    var5 = "Notification ERROR:: " + var11;
                    JF_NotificationUtils_mxJPO.debug(var5, this.startTime);
                    if (var9) {
                        var12 = this.generate3DNotificationRegistration(var1, 1);
                        JsonObject var13 = this.generate3DNotificationRegistration(var1, 2);
                        System.out.println("Registering Partial in catch ::");
                        System.out.println(var12.toString());
                        System.out.println(var13.toString());
                        var4 = NotificationClientUtil.sendNotification(var12.toString(), var6, 60000);
                        var4 = NotificationClientUtil.sendNotification(var13.toString(), var6, 60000);
                    } else {
                        var12 = this.generate3DNotificationRegistration(var1, 0);
                        System.out.println("Registering in catch:");
                        System.out.println(var12.toString());
                        var4 = NotificationClientUtil.sendNotification(var12.toString(), var6, 60000);
                    }

                    var5 = "Notification CONFIG status:: " + var4;
                    JF_NotificationUtils_mxJPO.debug(var5, this.startTime);
                    var3 = NotificationClientUtil.sendNotification(var2.toString(), var6, 60000);
                } catch (Exception var15) {
                    var11 = "Error in sending the notification";
                    System.out.println(var11);
                    System.out.println(var15.getMessage());
                    var15.printStackTrace();
                    JF_LOGGER.error(var11);
                    JF_LOGGER.error(var15.getMessage());
                }
            } else {
                JF_NotificationUtils_mxJPO.debug("Error: Composed Messsage returned NULL", this.startTime);
            }
        } catch (Exception var16) {
            System.out.println("Error : " + var16.getMessage());
            JF_LOGGER.error(var16.getMessage());
        }

        JF_NotificationUtils_mxJPO.debug("send3DNotification Complete:\t", this.startTime);
    }


    protected JsonObject generate3DNotificationRegistration(Context var1, int var2) throws Exception {
        String var3 = (String)this.controlMap.get("objtype");
        JsonObject var4 = this.getENOVIASettingsAndNLSJSONObject(var1, var2);
        if ("vv_test_execution".equalsIgnoreCase(var3)) {
            var4 = this.getSIMULIASettingsAndNLSJSONObject(var1, var2);
        }

        ByteArrayOutputStream var5 = new ByteArrayOutputStream();
        JsonWriter var6 = getPrettyJsonWriterFactory().createWriter(var5, Charset.forName("UTF-8"));
        var6.write(var4);
        var6.close();
        JF_LOGGER.debug("generateConfigurationJSON");
        String var7 = "JSON:\n" + var5.toString();
        JF_NotificationUtils_mxJPO.debug(var7, this.startTime);
        return var4;
    }

    public JsonObject getENOVIASettingsAndNLSJSONObject(Context var1, int var2) {
        JsonObject var3 = Json.createObjectBuilder().add("items", Json.createArrayBuilder().add(Json.createObjectBuilder().add("type", "PUSH_NOTIFICATIONID").add("appID", "ENOPREX_AP").add("notifications", this.getENOVIAConfig_SettingsJSONArray()).add("nls", this.getENOVIAConfig_NLSJSONArray(var1, var2)))).build();
        return var3;
    }

    public JsonObject getSIMULIASettingsAndNLSJSONObject(Context var1, int var2) {
        JsonObject var3 = Json.createObjectBuilder().add("items", Json.createArrayBuilder().add(Json.createObjectBuilder().add("type", "PUSH_NOTIFICATIONID").add("appID", "SIMTRCP_AP").add("notifications", this.getSIMULIAConfig_SettingsJSONArray()).add("nls", this.getSIMULIAConfig_NLSJSONArray(var1, var2)))).build();
        return var3;
    }

    private JsonArray getENOVIAConfig_SettingsJSONArray() {
        JsonArray var1 = Json.createArrayBuilder().add("dpm.notifications.taskcreatetoassign.setting").add("dpm.notifications.taskassigntoactive.setting").add("dpm.notifications.taskactivetoreview.setting").add("dpm.notifications.taskreviewtocomplete.setting").add("dpm.notifications.taskdemote.setting").add("dpm.notifications.dependenttask.setting").add("dpm.notifications.experiment.setting").add("dpm.notifications.task.setting").add("dpm.notifications.risk.setting").add("dpm.notifications.riskownership.setting").add("dpm.notifications.riskinformeduser.setting").add("dpm.notifications.candidate.task.setting").build();
        return var1;
    }

    private JsonArray getConfig_NLSJSONArray(Context var1, String var2, int var3, int var4) {
        String[] var5 = new String[9];
        String[] var6 = new String[]{"en", "de", "fr", "ja", "ko", "ru", "zh", "es", "it"};
        String[] var7 = new String[]{"en", "de", "fr", "ja", "ko"};
        String[] var8 = new String[]{"ru", "zh", "es", "it"};
        var5 = var6;
        if (var4 == 1) {
            var5 = var7;
        }

        if (var4 == 2) {
            var5 = var8;
        }

        JsonArrayBuilder var9 = Json.createArrayBuilder();

        for(int var10 = 0; var10 < var5.length; ++var10) {
            ResourceBundle var11 = EnoviaResourceBundle.getBundle(var1, var2, var5[var10]);
            Enumeration var12 = var11.getKeys();
            JsonObjectBuilder var13 = Json.createObjectBuilder();

            while(var12.hasMoreElements()) {
                String var14 = (String)var12.nextElement();
                if (var3 == 0 && var14.contains("dpm.notifications")) {
                    var13.add(var14, var11.getString(var14));
                }

                if (var3 == 1 && var14.contains("sim.notifications")) {
                    var13.add(var14, var11.getString(var14));
                }
            }

            var9.add(Json.createObjectBuilder().add("language", var5[var10]).add("values", var13));
        }

        return var9.build();
    }


    private JsonArray getSIMULIAConfig_SettingsJSONArray() {
        JsonArray var1 = Json.createArrayBuilder().add("sim.notifications.task.setting").build();
        return var1;
    }

    private JsonArray getENOVIAConfig_NLSJSONArray(Context var1, int var2) {
        JsonArray var3 = this.getConfig_NLSJSONArray(var1, "emxProgramCentralStringResource", 0, var2);
        return var3;
    }

    private JsonArray getSIMULIAConfig_NLSJSONArray(Context var1, int var2) {
        JsonArray var3 = this.getConfig_NLSJSONArray(var1, "emxProgramCentralStringResource", 1, var2);
        return var3;
    }

    protected JsonObject generateMessageBodyJSON(Context var1) {
        String var2 = (String)this.controlMap.get("objtype");
        String var3 = (String)this.controlMap.get("objname");
        String var4 = (String)this.controlMap.get("user");
        String var5 = (String)this.controlMap.get("projectname");
        String var6 = (String)this.controlMap.get("nextstate");
        String var7 = (String)this.controlMap.get("prevstate");
        if (var7 == null) {
            var7 = "";
        }

        if (var6 == null) {
            var6 = "";
        }

        if (var2 == null) {
            var2 = "(type)";
        }

        if (var3 == null) {
            var3 = "(name)";
        }

        if (var5 == null) {
            var5 = "";
        } else if (!var5.isEmpty() && !"Experiment".equalsIgnoreCase(var2) && !"Project Baseline".equalsIgnoreCase(var2)) {
            var5 = "(" + var5 + ")";
        }

        if (var4 == null || var4.isEmpty()) {
            var4 = var1.getUser();
        }

        String var8 = (String)this.controlMap.get("DPM_NOTIFY_MSG");
        if ("vv_test_execution".equalsIgnoreCase(var2)) {
            var8 = (String)this.controlMap.get("SIMULIA_NOTIFY_MSG");
            var2 = "V&V Test Execution Task";
        }

        String var9 = var1.getSession().getLanguage();

        try {
            var2 = EnoviaResourceBundle.getTypeI18NString(var1, var2, var9);
        } catch (MatrixException var11) {
            var11.printStackTrace();
        }

        JsonObject var10 = Json.createObjectBuilder().add("msg", var8).add("data", Json.createObjectBuilder().add("OBJECTTYPE", var2).add("OBJECTNAME", var3).add("PROJECTNAME", var5).add("USER", var4).add("FROM", var7).add("TO", var6).build()).build();
        JF_LOGGER.debug("generateMessageBodyJSON");
        return var10;
    }

    /*
     * @description:ECR会签状态的项目任务，状态是活动中---临时版本 废弃了
     * @author: caipan
     * @date: 2025/1/22 17:41:33
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void sendECREmail(Context context,String[] args) throws Exception{
        StringList list = JF_Util_mxJPO.basicBolistSel();
        list.add(DomainConstants.SELECT_OWNER);
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        String where = "current==Countersign";
       MapList mapList = DomainObject.findObjects(context, "JFECR", "*", where,list );
       DomainObject ecrObj = DomainObject.newInstance(context);

       for(int i=0;i<mapList.size();i++){
        Map temp = (Map)mapList.get(i);
        String ecrId = UIUtil.getValue(temp, DomainConstants.SELECT_ID);
           ecrObj.setId(ecrId);
           MapList taskIdList = ecrObj.getRelatedObjects(
                   context,
                   "JFECR2Task", // relationship pattern
                   "Task", // object pattern
                   list, // object selects
                   relList, // relationship selects
                   false, // to direction
                   true, // from direction
                   (short) 1, // recursion level
                   "current==Active", //object where clause
                   "", //relationship where clause
                   0
           );
           JF_LOGGER.info("ecrObj Name :{} taskList:{}",ecrObj.getName(),taskIdList);
           for(int j=0;j<taskIdList.size();j++){
               Map taskTemp = (Map)taskIdList.get(j);
               DomainObject person = DomainObject.newInstance(context,PersonUtil.getPersonObjectID(context, UIUtil.getValue(taskTemp, DomainConstants.SELECT_OWNER)));
               String email = person.getAttributeValue(context, "Email Address");
               if (UIUtil.isNotNullAndNotEmpty(email)){
                   //调用发邮件的代码
                   Map request = new HashMap();
                   request.put("objectId", ecrId);
                   request.put("ecrName", UIUtil.getValue(temp, DomainConstants.SELECT_NAME));
                   request.put("email", email);
                   JF_LOGGER.info("request:{}",request);
                  Boolean flag = JF_SendEmailUtils_mxJPO.sendECREmailToPortal(context, request);
                  JF_LOGGER.info("发送邮件:{}",flag);

               }
           }
       }
    }

    public void test(Context context,String[] args) throws Exception{
        Map request = new HashMap();
        request.put("objectId", "15554.60467.32192.21026");
        request.put("ecrName", "ECR-0000005");
        request.put("email", "caip@tecwin.com");
        JF_LOGGER.info("request:{}",request);
        Boolean flag = JF_SendEmailUtils_mxJPO.sendECREmailToPortal(context, request);
        JF_LOGGER.info("flag:{}",flag);

    }
 /*   public void test2(Context context,String[] args) throws Exception{
        Map request = new HashMap();
        request.put("objectId", "15554.60467.32192.21026");
        request.put("ecrName", "ECR-0000005");
        request.put("email", "caip@tecwin.com");
        JF_LOGGER.info("request:{}",request);
        Boolean flag = JF_SendEmailUtils_mxJPO.sendECREmailToPortalForTime(context, request);
        JF_LOGGER.info("flag:{}",flag);
                      taskMap.put("taskId",UIUtil.getValue(map, DomainConstants.SELECT_ID));
                taskMap.put("taskName",UIUtil.getValue(map, DomainConstants.SELECT_NAME));
                taskMap.put("taskTitle",UIUtil.getValue(map, DomainConstants.SELECT_ATTRIBUTE_TITLE));
                taskMap.put("connectTitle",UIUtil.getValue(temp, DomainConstants.SELECT_ATTRIBUTE_TITLE));
                taskMap.put("connectId",UIUtil.getValue(temp, DomainConstants.SELECT_ID));
                taskMap.put("connectName",UIUtil.getValue(temp, DomainConstants.SELECT_NAME));
                taskMap.put("owner",taskOwner);
                taskMap.put("RouteType","countersign");

    }
    /*
     * @description:ECR任务超期提醒 3天(72小时)  废弃
     * @author: caipan
     * @date: 2025/3/10 16:18:31
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void sendECROverdue(Context context,String[] args) throws Exception{
        JF_LOGGER.info("sendECROverdue start:{}",new Date());
        //查询ECR的审核任务
        //query connection relationship 'Route Task' where "from.current==Assigned && to.to[Object Route|from.type==JFECR] " select from.attribute[Title];
        String stateInboxTaskAssigned = PropertyUtil.getSchemaProperty(context,"policy", DomainObject.POLICY_INBOX_TASK,"state_Assigned");
        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        // 格式化日期
        String formattedDate = threeDaysAgo.format(formatter);
        String where = "current=="+stateInboxTaskAssigned+"&&originated<"+formattedDate+"";
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add("originated");
        MapList list = DomainObject.findObjects(context, DomainConstants.TYPE_INBOX_TASK, "*", where, selList);
        DomainObject task = DomainObject.newInstance(context);
        String includeType = NioJDUtils.getPageStr(context, "review.Task.NotificationType");
        Map temp=null;
        selList.add("to[Object Route].from.type");
        selList.add("to[Object Route].from.attribute[Title]");
        selList.add("to[Object Route].from.id");
        selList.add("to[Object Route].from.name");
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        MapList taskList = new MapList();
        //按照一个人只发一封邮件来合并
        Map<String,MapList> personMap = new HashMap<>();
        for(int i=0;i<list.size();i++){
            //找到Route关联的类型
            temp = (Map)list.get(i);
            String taskId = UIUtil.getValue(temp, DomainConstants.SELECT_ID);
            String taskName = UIUtil.getValue(temp, DomainConstants.SELECT_NAME);
            String taskTitle = UIUtil.getValue(temp, DomainConstants.SELECT_ATTRIBUTE_TITLE);
            String taskOwner = UIUtil.getValue(temp, DomainConstants.SELECT_OWNER);
            task.setId(taskId);
            MapList taskIdList = task.getRelatedObjects(
                    context,
                    DomainConstants.RELATIONSHIP_ROUTE_TASK, // relationship pattern
                    DomainConstants.TYPE_ROUTE, // object pattern
                    selList, // object selects
                    relList, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    "", //object where clause
                    "", //relationship where clause
                    0
            );
            for(int j=0;j<taskIdList.size();j++){
                temp = (Map)taskIdList.get(j);
                String findType = UIUtil.getValue(temp, "to[Object Route].from.type");
                String connectTitle = UIUtil.getValue(temp, "to[Object Route].from.attribute[Title]");
                String connectName = UIUtil.getValue(temp, "to[Object Route].from.name");
                String connectId = UIUtil.getValue(temp, "to[Object Route].from.id");
                if(includeType.contains(findType)&&UIUtil.isNotNullAndNotEmpty(findType)){//符合需要通知的类型要求
                    Map taskMap = new HashMap();
                    taskMap.put("taskId",taskId);
                    taskMap.put("taskName",taskName);
                    taskMap.put("taskTitle",taskTitle);
                    taskMap.put("connectTitle",connectTitle);
                    taskMap.put("connectId",connectId);
                    taskMap.put("connectName",connectName);
                    taskMap.put("owner",taskOwner);
                    taskMap.put("RouteType","review");//review 审核任务  countersign 会签任务  发送的消息不一样和获取的链接ID不一样
                    taskList.add(taskMap);
                    MapList signList = new MapList();
                    if(personMap.containsKey(taskOwner)){
                        signList.addAll(personMap.get(taskOwner));
                    }
                    signList.add(taskMap);
                    personMap.put(taskOwner,signList);
                    break;
                }
            }

        }
        //ECR会签任务
        MapList CountersignList= sendECREmailCountersign(context, formattedDate,personMap);
        DomainObject personObj = DomainObject.newInstance(context);
        int count=0;
        //任务owner
        for(Map.Entry<String,MapList> entry : personMap.entrySet()) {
            String username = entry.getKey();
            MapList emaillist = entry.getValue();
            personObj.setId(PersonUtil.getPersonObjectID(context, username));
            String email =  personObj.getAttributeValue(context, "Email Address");
            String firstName =  personObj.getAttributeValue(context, "First Name");
            personObj.setId(JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, username));
            String LineManageEmail =  personObj.getAttributeValue(context, "Email Address");
            //
            JF_LOGGER.info("发送邮件地址:{},{}",email,LineManageEmail);
//            email="caip@tecwin.com";
//            count=count+1;
//            if(count<=2) {
                Boolean flag = JF_SendEmailUtils_mxJPO.sendECREmailToPortalForTime(context, email,LineManageEmail, emaillist,firstName);
//            }

        }
        //任务的直线经理

        JF_LOGGER.info("sendECROverdue end:{}",new Date());

    }
    /*
     * @description:ECR会签状态的项目任务，状态是活动中 时间超过72个小时没有完成的
     * @author: caipan
     * @date: 2025/1/22 17:41:33
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public MapList sendECREmailCountersign(Context context,String formattedDate,Map<String,MapList> personMap) throws Exception{
        StringList list = JF_Util_mxJPO.basicBolistSel();
        list.add("originated");
        list.add(DomainConstants.SELECT_OWNER);
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        String where = "current==Countersign||current==APR||current==Quotation";
        String includeType = NioJDUtils.getPageStr(context, "review.Task.NotificationType");
        MapList mapList = DomainObject.findObjects(context, includeType, "*", where,list );
        DomainObject ecrObj = DomainObject.newInstance(context);
        MapList resultList = new MapList();
        for(int i=0;i<mapList.size();i++){
            Map temp = (Map)mapList.get(i);
            String ecrId = UIUtil.getValue(temp, DomainConstants.SELECT_ID);
            String ecrCurrent = UIUtil.getValue(temp, DomainConstants.SELECT_CURRENT);
            ecrObj.setId(ecrId);
             where = "current==Active&&originated<"+formattedDate+"";
            MapList taskIdList = ecrObj.getRelatedObjects(
                    context,
                    "JFECR2Task", // relationship pattern
                    "Task", // object pattern
                    list, // object selects
                    relList, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    where, //object where clause
                    "", //relationship where clause
                    0
            );
            for(int j=0;j<taskIdList.size();j++){
                Map map = (Map)taskIdList.get(j);
                Map taskMap = new HashMap();
                String taskOwner = UIUtil.getValue(map, DomainConstants.SELECT_OWNER);
                taskMap.put("taskId",UIUtil.getValue(map, DomainConstants.SELECT_ID));
                taskMap.put("taskName",UIUtil.getValue(map, DomainConstants.SELECT_NAME));
                taskMap.put("taskTitle",UIUtil.getValue(map, DomainConstants.SELECT_ATTRIBUTE_TITLE));
                taskMap.put("connectTitle",UIUtil.getValue(temp, DomainConstants.SELECT_ATTRIBUTE_TITLE));
                taskMap.put("connectId",UIUtil.getValue(temp, DomainConstants.SELECT_ID));
                taskMap.put("connectName",UIUtil.getValue(temp, DomainConstants.SELECT_NAME));
                taskMap.put("owner",taskOwner);
                taskMap.put("RouteType","countersign");

                //如果是APR任务，需要增加逻辑判断是否有工作中的会签任务(意味着存在驳回的会签任务，这种情况下APR任务不算延期)
                if(ecrCurrent.equals("APR")){
                    String taskType = UIUtil.getValue(map, DomainConstants.SELECT_TYPE);
                    if("JF_APRTask".equalsIgnoreCase(taskType)){
                        //判断该ECR下面是否有未审批的会签任务
                        if(isSignTaskInwork(context,taskIdList)){
                            continue;
                        }
                    }
                }
                MapList signList = new MapList();
                if(personMap.containsKey(taskOwner)){
                    signList.addAll(personMap.get(taskOwner));
                }
                signList.add(taskMap);
                personMap.put(taskOwner,signList);
                resultList.add(taskMap);
            }
        }
        return resultList;
    }


    /*
     * @description:图纸冻结、数模冻结、发布之后给项目成员发邮件通知
     * @author: caipan
     * @date: 2025/3/13 16:21:24
     * @param: * @param[1] context
     * @param[2] args
     * @return: 
     **/
    public void notifySTDMember(Context context,String[] args) throws Exception{
        JF_LOGGER.info("notifySTDMember start");
        String objectId = args[0];
        try{
            ContextUtil.pushContext(context);
            DomainObject obj = DomainObject.newInstance(context, objectId);
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            selList.add("from[" + JF_PLMConstants_mxJPO.REL_JFChange2Project + "].to.id");
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            Map attributeMap = obj.getInfo(context, selList);
            String type = UIUtil.getValue(attributeMap, DomainConstants.SELECT_TYPE);
            String projectId = UIUtil.getValue(attributeMap, "from[" + JF_PLMConstants_mxJPO.REL_JFChange2Project + "].to.id");
            //获取STD成员账号
            if (UIUtil.isNotNullAndNotEmpty(projectId)) {
                StringList stdList = getSTDEmail(context, projectId);
                if (stdList.size() > 0) {
                    JF_LOGGER.info("stdList:{}", stdList);
                    //获取关联数据对象
                    String relName = "";
                    if (JF_PLMConstants_mxJPO.TYPE_JFECR.equals(type)) {
                        relName = JF_PLMConstants_mxJPO.REL_JFRelateItem;
                    } else if ("JFDR".equals(type)) {
                        relName = JF_PLMConstants_mxJPO.REL_JFDR2VPMREFERENCE;
                    }
                    MapList connectList = new MapList();
                    selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
                    selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                    selList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_PLMEntity_V_Name);
                    if ("JF_DRW".equals(type)) {
                        ChangeAction changeActionObj = new ChangeAction();
                        changeActionObj.setId(objectId);
                        MapList mapList = changeActionObj.getAffectedItems(context);
                        DomainObject drwObj = DomainObject.newInstance(context);
                        for (int i = 0; i < mapList.size(); i++) {
                            Map map = (Map) mapList.get(i);
                            String itemId = (String) map.get(DomainObject.SELECT_ID);
                            drwObj.setId(itemId);
                            connectList.add(drwObj.getInfo(context, selList));
                        }
                    } else {
                        connectList = obj.getRelatedObjects(
                                context,
                                relName, // relationship pattern
                                "*", // object pattern
                                selList, // object selects
                                relList, // relationship selects
                                false, // to direction
                                true, // from direction
                                (short) 1, // recursion level
                                "", //object where clause
                                "", //relationship where clause
                                15
                        );
                    }
                    //批量发送邮件
                    JF_SendEmailUtils_mxJPO.sendEmailToPortalForStatus(context, stdList, connectList, obj, projectId);
                }
            }
            }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        JF_LOGGER.info("notifySTDMember end");
   }


   public StringList getSTDEmail(Context context,String projectId) throws Exception{
       //需要发邮件的项目角色
       JF_LOGGER.info("projectId:{}",projectId);
       StringList notifyList = new StringList();
       notifyList.add("Chair manager");
       notifyList.add("Launch manager");
       notifyList.add("AQE representative/PQL");
       notifyList.add("Logistics representative");
       notifyList.add("AME representative");
       notifyList.add("SQD Representative");
       notifyList.add("Purchasing representative");
        StringList selList = JF_Util_mxJPO.basicBolistSel();
       selList.add("attribute[Email Address]");
        StringList relList = JF_Util_mxJPO.basicBolistSel();
        relList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_PROJECT_ROLE);
        DomainObject projectObject = DomainObject.newInstance(context, projectId);
       MapList mapList = projectObject.getRelatedObjects(
               context,
               DomainRelationship.RELATIONSHIP_MEMBER,
               DomainConstants.TYPE_PERSON,
               selList,
               relList,
               false,
               true,
               (short) 1, // recursion level
               "", //object where clause
               "", //relationship where clause
               0
       );
       StringList emailList = new StringList();
    for (int i=0;i<mapList.size();i++){
        Map temp = (Map)mapList.get(i);
        String projectRole = UIUtil.getValue(temp, JF_PLMConstants_mxJPO.SELECT_ATTR_PROJECT_ROLE);
        String email = UIUtil.getValue(temp, "attribute[Email Address]");
        if(notifyList.contains(projectRole)){
            JF_LOGGER.info("email:{}",email);
//            email = "caip@tecwin.com";
            emailList.add(email);
        }
    }
    return emailList;
   }

    /*
     * @description:创建ECO的时候触发发邮件给ECO owner 也就是项目经理
     * @author: caipan
     * @date: 2025/3/21 10:19:55
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
   public  void sendECOEmail(Context context,String[] args) throws Exception{
       JF_LOGGER.info("sendECOEmail start");
        String ecoId = args[0];
        DomainObject ecoObj = DomainObject.newInstance(context,ecoId);
        String email = getPersonEmail(context, ecoObj.getInfo(context, DomainObject.SELECT_OWNER),null);
       JF_LOGGER.info("send Email:{}",email);
       //测试email
//            email = "caip@tecwin.com";
           String ecoName = ecoObj.getInfo(context, DomainConstants.SELECT_NAME);
           String ecrName = ecoObj.getInfo(context, "to[JFECR2CO].from.name");
           String ecrTitle = ecoObj.getInfo(context, "to[JFECR2CO].from.attribute[Title]");
           Map map = new HashMap();
           map.put("objectId", ecoId);
           map.put("ecoName", ecoName);
           map.put("ecrName", ecrName + "#" + ecrTitle);
           map.put("email", email);
           JF_SendEmailUtils_mxJPO.sendECOEmailToPortal(context, map);
       JF_LOGGER.info("sendECOEmail end");
   }
    /*
     * @description:超过24小时ECO状态没有提升到执行反馈的时候发邮件给数据owner和他的直线经理
     * @author: caipan
     * @date: 2025/3/21 13:53:16
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void sendECOEmailTime(Context context,String[] args) throws Exception{
        JF_LOGGER.info("sendECOEmail start");
        LocalDate threeDaysAgo = LocalDate.now().minusDays(1);
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        // 格式化日期
        String formattedDate = threeDaysAgo.format(formatter);
        String where = "(current==In_Work||current==Create)&&originated<"+formattedDate+"";
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add("originated");
        selList.add("to[JFECR2CO].from.name");
        selList.add("to[JFECR2CO].from.attribute[Title]");
        MapList list = DomainObject.findObjects(context, JF_PLMConstants_mxJPO.TYPE_JFECO, "*", where, selList);
        Map<String,MapList> personMap = new HashMap<>();
        for(int i=0;i<list.size();i++) {
            Map temp =(Map) list.get(i);

            String ecoName =  UIUtil.getValue(temp,DomainConstants.SELECT_NAME);
            String ecrName = UIUtil.getValue(temp,"to[JFECR2CO].from.name");
            String ecrTitle =  UIUtil.getValue(temp,"to[JFECR2CO].from.attribute[Title]");
            String ecoId =  UIUtil.getValue(temp,DomainConstants.SELECT_ID);
            String owner =  UIUtil.getValue(temp,DomainConstants.SELECT_OWNER);
            Map map = new HashMap();
            map.put("objectId", ecoId);
            map.put("ecoName", ecoName);
            if(UIUtil.isNotNullAndNotEmpty(ecrName)) {
                map.put("ecrName", ecrName + "#" + ecrTitle);
            }else{
                map.put("ecrName","no connect ECR");
            }
            MapList contentList = new MapList();
            if(personMap.containsKey(owner)){
                contentList.addAll(personMap.get(owner));
            }
            contentList.add(map);
            personMap.put(owner,contentList);
        }

        for(Map.Entry<String,MapList> entry : personMap.entrySet()) {
            String username = entry.getKey();
            MapList emaillist = entry.getValue();

            String lineManager = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, username);
            String lineManagerEmail =  getPersonEmail(context, null,lineManager);
            String email =  getPersonEmail(context, username,null);

            JF_LOGGER.info("发送邮件地址:{},{}",email,lineManagerEmail);
//            email="caip@tecwin.com";
//            count=count+1;
//            if(count<=2) {
            JF_SendEmailUtils_mxJPO.sendECOEmailToPortalForTime(context, emaillist,email,lineManagerEmail);
//            }

        }
        JF_LOGGER.info("sendECOEmail end");
    }
    /*
     * @description:返回人员邮箱:如果ObjectId 有值 就优先使用objectId 没有就使用 userName
     * @author: caipan
     * @date: 2025/3/21 15:04:08
     * @param: * @param[1] context
     * @param[2] userName
     * @param[3] objectId
     * @return:
     **/
    public static String getPersonEmail(Context context,String userName,String objectId)throws Exception{
        DomainObject personObj = DomainObject.newInstance(context);
        if(UIUtil.isNotNullAndNotEmpty(objectId)){
            personObj.setId(objectId);
        }else {
            personObj.setId(PersonUtil.getPersonObjectID(context, userName));
        }
        return personObj.getAttributeValue(context, "Email Address");
    }

    /**
     * @description:ECR任务超期提醒 3天(72小时),会签任务
     * @author: caipan
     * @date: 2025/3/10 16:18:31
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void sendECROverdue2(Context context,String[] args) throws Exception{
        JF_LOGGER.info("sendECROverdue start:{}",new Date());
        //查询ECR的审核任务
        //query connection relationship 'Route Task' where "from.current==Assigned && to.to[Object Route|from.type==JFECR] " select from.attribute[Title];
        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        // 格式化日期
        String formattedDate = threeDaysAgo.format(formatter);
        JF_LOGGER.info("formattedDate:{}",formattedDate);
        //按照一个人只发一封邮件来合并
        Map<String,MapList> personMap = new HashMap<>();
        //ECR会签任务
        sendECREmailCountersign(context, formattedDate,personMap);
        DomainObject personObj = DomainObject.newInstance(context);
        int count=0;
        //任务owner
        for(Map.Entry<String,MapList> entry : personMap.entrySet()) {
            String username = entry.getKey();
            MapList emaillist = entry.getValue();
            personObj.setId(PersonUtil.getPersonObjectID(context, username));
            String email =  personObj.getAttributeValue(context, "Email Address");
            String firstName =  personObj.getAttributeValue(context, "First Name");
            personObj.setId(JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, username));
            String LineManageEmail =  personObj.getAttributeValue(context, "Email Address");
            //部门总监
            StringList departmentHeadEmailList = new StringList();
            String departments=getDepartMentHead(context,username);   //如果有多个，用英文,号分隔
            if(UIUtil.isNotNullAndNotEmpty(departments)) {
                String[] split = departments.split(",");
                for(int i = 0; i < split.length; i++) {
                    personObj.setId((PersonUtil.getPersonObjectID(context, split[i])));
                    String emailAddress = personObj.getAttributeValue(context, "Email Address");
                    if (LineManageEmail.equalsIgnoreCase(emailAddress)) {
                        continue;
                    }
                    departmentHeadEmailList.add(emailAddress);
                }
            }
            //
            JF_LOGGER.info("发送邮件地址:{},{},{},departmentHeadEmail {}",email,LineManageEmail,departments,departmentHeadEmailList);
//            email="caip@tecwin.com";
//            count=count+1;
//            if(count<=2) {
            Boolean flag = JF_SendEmailUtils_mxJPO.sendECREmailToPortalForTime2(context, email,LineManageEmail,departmentHeadEmailList.join(","), emaillist,firstName);
//            }

        }
        //任务的直线经理

        JF_LOGGER.info("sendECROverdue end:{}",new Date());

    }
    /*
     * @description:获取该用户的部门总监的账号
     * @author: caipan
     * @date: 2025/8/19 14:22:38
     * @param: * @param[1] context
     * @param[2] userName
     * @return:
     **/
    public String getDepartMentHead(Context context,String userName) throws Exception{
        DomainObject personObj = DomainObject.newInstance(context);
        personObj.setId(PersonUtil.getPersonObjectID(context, userName));
        //获取部门
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add("description");
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        String departMentName = "";
        MapList list = personObj.getRelatedObjects(context,
                "Member", //pattern to match relationships
                "Department", //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                true, //get To relationships
                false, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 1); //limit
        if(list.size()>0){
            Map map = (Map)list.get(0);
            departMentName =UIUtil.getValue (map,"description");
        }
        return departMentName;
    }

    /**
     * @description:ECR任务超期提醒 72小时 审核任务
     * @author: caipan
     * @date: 2025/3/10 16:18:31
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void sendECROverdueReview(Context context,String[] args) throws Exception{
        JF_LOGGER.info("sendECROverdueReview start:{}",new Date());
        //查询ECR的审核任务
        //query connection relationship 'Route Task' where "from.current==Assigned && to.to[Object Route|from.type==JFECR] " select from.attribute[Title];
        String stateInboxTaskAssigned = PropertyUtil.getSchemaProperty(context,"policy", DomainObject.POLICY_INBOX_TASK,"state_Assigned");
        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        // 格式化日期
        String formattedDate = threeDaysAgo.format(formatter);
        JF_LOGGER.info("formattedDate:{}",formattedDate);
        String where = "current=="+stateInboxTaskAssigned+"&&originated<"+formattedDate+"";
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add("originated");
        MapList list = DomainObject.findObjects(context, DomainConstants.TYPE_INBOX_TASK, "*", where, selList);
        JF_LOGGER.info("list:{}",list.size());
        DomainObject task = DomainObject.newInstance(context);
        String includeType = NioJDUtils.getPageStr(context, "review.Task.NotificationType");
        Map temp=null;
        selList.add("to[Object Route].from.type");
        selList.add("to[Object Route].from.attribute[Title]");
        selList.add("to[Object Route].from.id");
        selList.add("to[Object Route].from.name");
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        MapList taskList = new MapList();
        //按照一个人只发一封邮件来合并
        Map<String,MapList> personMap = new HashMap<>();//普通审核节点
        Map<String,MapList> personReviewMap = new HashMap<>();//直线经理 整椅经理审核节点
        String strLineMess = "ECR整椅主管审核";//EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.Review.LineManager");
        String strChairMess = "ECR SDT-项目总工程师/PDL审核";//EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.Review.ChairManager");
        String strLineMess_EN = "Chair Supervisor Review";
        String strChairMess_EN ="SDT-Project Chief Engineer/PDL Review";
        //拿到配置的研发总监
        String RDHead = NioJDUtils.getPageStr(context, "review.Task.RDHead");
        //需要中英文都匹配
        for(int i=0;i<list.size();i++){
            //找到Route关联的类型
            temp = (Map)list.get(i);
            String taskId = UIUtil.getValue(temp, DomainConstants.SELECT_ID);
            String taskName = UIUtil.getValue(temp, DomainConstants.SELECT_NAME);
            String taskTitle = UIUtil.getValue(temp, DomainConstants.SELECT_ATTRIBUTE_TITLE);
            String taskOwner = UIUtil.getValue(temp, DomainConstants.SELECT_OWNER);
            String title = UIUtil.getValue(temp, DomainConstants.SELECT_ATTRIBUTE_TITLE);
            task.setId(taskId);
            MapList taskIdList = task.getRelatedObjects(
                    context,
                    DomainConstants.RELATIONSHIP_ROUTE_TASK, // relationship pattern
                    DomainConstants.TYPE_ROUTE, // object pattern
                    selList, // object selects
                    relList, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    "", //object where clause
                    "", //relationship where clause
                    0
            );
            boolean flag = title.equalsIgnoreCase(strLineMess)||title.equalsIgnoreCase(strChairMess)||title.equalsIgnoreCase(strLineMess_EN)||title.equalsIgnoreCase(strChairMess_EN);
            for(int j=0;j<taskIdList.size();j++){
                temp = (Map)taskIdList.get(j);
                String findType = UIUtil.getValue(temp, "to[Object Route].from.type");
                String connectTitle = UIUtil.getValue(temp, "to[Object Route].from.attribute[Title]");
                String connectName = UIUtil.getValue(temp, "to[Object Route].from.name");
                String connectId = UIUtil.getValue(temp, "to[Object Route].from.id");

                if(includeType.contains(findType)&&UIUtil.isNotNullAndNotEmpty(findType)){//符合需要通知的类型要求
                    JF_LOGGER.info("findType:{} {} title {}",findType,flag,title);
                    Map taskMap = new HashMap();
                    taskMap.put("taskId",taskId);
                    taskMap.put("taskName",taskName);
                    taskMap.put("taskTitle",taskTitle);
                    taskMap.put("connectTitle",connectTitle);
                    taskMap.put("connectId",connectId);
                    taskMap.put("connectName",connectName);
                    taskMap.put("owner",taskOwner);
                    taskMap.put("RouteType","review");//review 审核任务  countersign 会签任务  发送的消息不一样和获取的链接ID不一样
                    taskList.add(taskMap);
                    if(!flag) {
                        MapList signList = new MapList();
                        if (personMap.containsKey(taskOwner)) {
                            signList.addAll(personMap.get(taskOwner));
                        }
                        signList.add(taskMap);
                        personMap.put(taskOwner, signList);
                        break;
                    }else{
                        MapList signList = new MapList();
                        if (personReviewMap.containsKey(taskOwner)) {
                            signList.addAll(personReviewMap.get(taskOwner));
                        }
                        signList.add(taskMap);
                        personReviewMap.put(taskOwner, signList);
                        break;
                    }
                }
            }

        }
//        JF_LOGGER.info("personMap:{} personReviewMap {}",personMap,personReviewMap);
        DomainObject personObj = DomainObject.newInstance(context);
        int count=0;
        //任务owner  其他审核任务
        for(Map.Entry<String,MapList> entry : personMap.entrySet()) {
            String username = entry.getKey();
            MapList emaillist = entry.getValue();
            personObj.setId(PersonUtil.getPersonObjectID(context, username));
            String email =  personObj.getAttributeValue(context, "Email Address");
            String firstName =  personObj.getAttributeValue(context, "First Name");
            personObj.setId(JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, username));
            String LineManageEmail =  personObj.getAttributeValue(context, "Email Address");
            //
            JF_LOGGER.info("发送邮件地址:{},{}",email,LineManageEmail);
//            email="caip@tecwin.com";
//            LineManageEmail="caip@tecwin.com";
//            count=count+1;
//            if(count<=2) {
            Boolean flag = JF_SendEmailUtils_mxJPO.sendECREmailToPortalForTime(context, email,LineManageEmail, emaillist,firstName);
//            }

        }
        //任务的直线经理、整椅经理审核任务
        for(Map.Entry<String,MapList> entry : personReviewMap.entrySet()) {
            String username = entry.getKey();
            MapList emaillist = entry.getValue();
            personObj.setId(PersonUtil.getPersonObjectID(context, username));
            String email =  personObj.getAttributeValue(context, "Email Address");
            String firstName =  personObj.getAttributeValue(context, "First Name");
            personObj.setId(JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, username));
            String LineManageEmail =  personObj.getAttributeValue(context, "Email Address");
            //
            JF_LOGGER.info("personReviewMap发送邮件地址:{},{}",email,LineManageEmail);
//            email="caip@tecwin.com";
//            LineManageEmail="caip@tecwin.com";
//            count=count+1;
//            if(count<=2) {
            Boolean flag = JF_SendEmailUtils_mxJPO.sendECREmailToPortalForTime2(context, email,LineManageEmail,RDHead, emaillist,firstName);
//            }

        }
        JF_LOGGER.info("sendECROverdue end:{}",new Date());

    }

    /*
     * @description:ECO执行任务，任务状态到
     * @author: caipan
     * @date: 2025/8/28 10:56:29
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void sendECOTaskOverdue(Context context,String[] args) throws Exception{
        JF_LOGGER.info("sendECOTaskOverdue start:{}",new Date());
        //查询ECR的审核任务
        //query connection relationship 'Route Task' where "from.current==Assigned && to.to[Object Route|from.type==JFECR] " select from.attribute[Title];
        LocalDate threeDaysAgo = LocalDate.now().minusDays(1);
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        // 格式化日期
        String formattedDate = threeDaysAgo.format(formatter);
        //按照一个人只发一封邮件来合并
        Map<String,MapList> personMap = new HashMap<>();
        //ECR会签任务
        sendECOTask(context, formattedDate,personMap);
        DomainObject personObj = DomainObject.newInstance(context);
        int count=0;
        //任务owner
        for(Map.Entry<String,MapList> entry : personMap.entrySet()) {
            String username = entry.getKey();
            MapList emaillist = entry.getValue();
            personObj.setId(PersonUtil.getPersonObjectID(context, username));
            String email =  personObj.getAttributeValue(context, "Email Address");
            String firstName =  personObj.getAttributeValue(context, "First Name");
            personObj.setId(JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, username));
            String LineManageEmail =  personObj.getAttributeValue(context, "Email Address");
            //部门总监
//            String departmentHeadEmail = "";
//            String departmentHead=getDepartMentHead(context,username);
//            if(UIUtil.isNotNullAndNotEmpty(departmentHead)) {
//                personObj.setId((PersonUtil.getPersonObjectID(context, departmentHead)));
//                departmentHeadEmail =  personObj.getAttributeValue(context, "Email Address");
//            }
            //            email="caip@tecwin.com";
//            LineManageEmail="caip@tecwin.com";
//            departmentHeadEmail="caip@tecwin.com";
//            count=count+1;
//            if(count<=2) {
//            Boolean flag = JF_SendEmailUtils_mxJPO.sendTaskEmailToPortalForTime(context, email,LineManageEmail,departmentHeadEmail, emaillist,firstName);
//            }
            StringList departmentHeadEmailList = new StringList();
            String departments=getDepartMentHead(context,username);   //如果有多个，用英文,号分隔
            if(UIUtil.isNotNullAndNotEmpty(departments)) {
                String[] split = departments.split(",");
                for(int i = 0; i < split.length; i++) {
                    personObj.setId((PersonUtil.getPersonObjectID(context, split[i])));
                    String emailAddress = personObj.getAttributeValue(context, "Email Address");
                    if (LineManageEmail.equalsIgnoreCase(emailAddress)) {
                        continue;
                    }
                    departmentHeadEmailList.add(emailAddress);
                }
            }
            //
            JF_LOGGER.info("发送邮件地址:{},{},{},departmentHeadEmail {}",email,LineManageEmail,departments,departmentHeadEmailList);
            Boolean flag = JF_SendEmailUtils_mxJPO.sendECREmailToPortalForTime2(context, email,LineManageEmail,departmentHeadEmailList.join(","), emaillist,firstName);

        }
        //任务的直线经理

        JF_LOGGER.info("sendECOTaskOverdue end:{}",new Date());

    }
    /*
     * @description:获取ECO超时的执行任务
     * @author: caipan
     * @date: 2025/8/28 13:55:11
     * @param: * @param[1] context
     * @param[2] formattedDate
     * @param[3] personMap
     * @return:
     **/
    public MapList sendECOTask(Context context,String formattedDate,Map<String,MapList> personMap) throws Exception{
        StringList list = JF_Util_mxJPO.basicBolistSel();
        list.add("originated");
        list.add(DomainConstants.SELECT_OWNER);
        list.add(DomainConstants.SELECT_DESCRIPTION);
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        String where = "current==ExecuteFeedback";
        String includeType = "JFECO";
        MapList mapList = DomainObject.findObjects(context, includeType, "*", where,list );
        DomainObject ecrObj = DomainObject.newInstance(context);
        MapList resultList = new MapList();
        for(int i=0;i<mapList.size();i++){
            Map temp = (Map)mapList.get(i);
            String ecrId = UIUtil.getValue(temp, DomainConstants.SELECT_ID);
            ecrObj.setId(ecrId);
            where = "current==Active&&attribute[Task Estimated Finish Date]<"+formattedDate+"";
            MapList taskIdList = ecrObj.getRelatedObjects(
                    context,
                    "JFCO2ECOTask", // relationship pattern
                    "Task", // object pattern
                    list, // object selects
                    relList, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    where, //object where clause
                    "", //relationship where clause
                    0
            );
            for(int j=0;j<taskIdList.size();j++){
                Map map = (Map)taskIdList.get(j);
                Map taskMap = new HashMap();
                String taskOwner = UIUtil.getValue(map, DomainConstants.SELECT_OWNER);
                taskMap.put("taskId",UIUtil.getValue(map, DomainConstants.SELECT_ID));
                taskMap.put("taskName",UIUtil.getValue(map, DomainConstants.SELECT_NAME));
                taskMap.put("taskTitle",UIUtil.getValue(map, DomainConstants.SELECT_ATTRIBUTE_TITLE));
                taskMap.put("connectTitle",UIUtil.getValue(temp, DomainConstants.SELECT_DESCRIPTION));
                taskMap.put("connectId",UIUtil.getValue(temp, DomainConstants.SELECT_ID));
                taskMap.put("connectName",UIUtil.getValue(temp, DomainConstants.SELECT_NAME));
                taskMap.put("owner",taskOwner);
                taskMap.put("RouteType","countersign");
                MapList signList = new MapList();
                if(personMap.containsKey(taskOwner)){
                    signList.addAll(personMap.get(taskOwner));
                }
                signList.add(taskMap);
                personMap.put(taskOwner,signList);
                resultList.add(taskMap);
            }
        }
        return resultList;
    }




    /*
     * @description:获取DA超时的执行任务
     * @author: caipan
     * @date: 2025/8/28 13:55:11
     * @param: * @param[1] context
     * @param[2] formattedDate
     * @param[3] personMap
     * @return:
     **/
    public MapList sendDATask(Context context,String formattedDate,Map<String,MapList> personMap) throws Exception{
        StringList list = JF_Util_mxJPO.basicBolistSel();
        list.add("originated");
        list.add(DomainConstants.SELECT_OWNER);
        list.add(DomainConstants.SELECT_DESCRIPTION);
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        String where = "current==Implement";
        String includeType = "JFDA";
        MapList mapList = DomainObject.findObjects(context, includeType, "*", where,list );
        DomainObject ecrObj = DomainObject.newInstance(context);
        MapList resultList = new MapList();
        for(int i=0;i<mapList.size();i++){
            Map temp = (Map)mapList.get(i);
            String ecrId = UIUtil.getValue(temp, DomainConstants.SELECT_ID);
            ecrObj.setId(ecrId);
            where = "current==Active&&attribute[Task Estimated Finish Date]<"+formattedDate+"";
            MapList taskIdList = ecrObj.getRelatedObjects(
                    context,
                    "JFDA2JFDATask", // relationship pattern
                    "Task", // object pattern
                    list, // object selects
                    relList, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    where, //object where clause
                    "", //relationship where clause
                    0
            );
            for(int j=0;j<taskIdList.size();j++){
                Map map = (Map)taskIdList.get(j);
                Map taskMap = new HashMap();
                String taskOwner = UIUtil.getValue(map, DomainConstants.SELECT_OWNER);
                taskMap.put("taskId",UIUtil.getValue(map, DomainConstants.SELECT_ID));
                taskMap.put("taskName",UIUtil.getValue(map, DomainConstants.SELECT_NAME));
                taskMap.put("taskTitle",UIUtil.getValue(map, DomainConstants.SELECT_ATTRIBUTE_TITLE));
                taskMap.put("connectTitle",UIUtil.getValue(temp, DomainConstants.SELECT_ATTRIBUTE_TITLE));
                taskMap.put("connectId",UIUtil.getValue(temp, DomainConstants.SELECT_ID));
                taskMap.put("connectName",UIUtil.getValue(temp, DomainConstants.SELECT_NAME));
                taskMap.put("owner",taskOwner);
                taskMap.put("RouteType","countersign");
                MapList signList = new MapList();
                if(personMap.containsKey(taskOwner)){
                    signList.addAll(personMap.get(taskOwner));
                }
                signList.add(taskMap);
                personMap.put(taskOwner,signList);
                resultList.add(taskMap);
            }
        }
        return resultList;
    }

    /*
     * @description:DA执行任务，任务状态到
     * @author: caipan
     * @date: 2025/8/28 10:56:29
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public void sendDATaskOverdue(Context context,String[] args) throws Exception{
        JF_LOGGER.info("sendDATaskOverdue start:{}",new Date());
        //查询ECR的审核任务
        //query connection relationship 'Route Task' where "from.current==Assigned && to.to[Object Route|from.type==JFECR] " select from.attribute[Title];
        LocalDate threeDaysAgo = LocalDate.now().minusDays(1);
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        // 格式化日期
        String formattedDate = threeDaysAgo.format(formatter);
        //按照一个人只发一封邮件来合并
        Map<String,MapList> personMap = new HashMap<>();
        //ECR会签任务
        sendDATask(context, formattedDate,personMap);
        DomainObject personObj = DomainObject.newInstance(context);
        int count=0;
        //任务owner
        for(Map.Entry<String,MapList> entry : personMap.entrySet()) {
            String username = entry.getKey();
            MapList emaillist = entry.getValue();
            personObj.setId(PersonUtil.getPersonObjectID(context, username));
            String email =  personObj.getAttributeValue(context, "Email Address");
            String firstName =  personObj.getAttributeValue(context, "First Name");
            personObj.setId(JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, username));
            String LineManageEmail =  personObj.getAttributeValue(context, "Email Address");
            //部门总监
            StringList departmentHeadEmailList = new StringList();
            String departments=getDepartMentHead(context,username);   //如果有多个，用英文,号分隔
            if(UIUtil.isNotNullAndNotEmpty(departments)) {
                String[] split = departments.split(",");
                for(int i = 0; i < split.length; i++) {
                    personObj.setId((PersonUtil.getPersonObjectID(context, split[i])));
                    String emailAddress = personObj.getAttributeValue(context, "Email Address");
                    if (LineManageEmail.equalsIgnoreCase(emailAddress)) {
                        continue;
                    }
                    departmentHeadEmailList.add(emailAddress);
                }
            }
            //
            JF_LOGGER.info("发送邮件地址:{},{},{},departmentHeadEmail {}",email,LineManageEmail,departments,departmentHeadEmailList);
//            email="caip@tecwin.com";
//            count=count+1;
//            if(count<=2) {
            Boolean flag = JF_SendEmailUtils_mxJPO.sendECREmailToPortalForTime2(context, email,LineManageEmail,departmentHeadEmailList.join(","), emaillist,firstName);
//            }

        }
        //任务的直线经理

        JF_LOGGER.info("sendDATaskOverdue end:{}",new Date());

    }
    /*
     * @description:判断当前MapList是否有工作中状态的会签任务
     * @author: caipan
     * @date: 2025/10/13 11:34:41
     * @param: * @param[1] context
     * @param[2] taskList
     * @return:
     **/
    public boolean isSignTaskInwork(Context context,MapList taskList) throws Exception{
        boolean flag = false;
        Map map =null;
        for(int i=0;i<taskList.size();i++){
            map = (Map)taskList.get(i);
            String type = UIUtil.getValue(map,DomainConstants.SELECT_TYPE);
            String current = UIUtil.getValue(map,DomainConstants.SELECT_CURRENT);
            if("JF_SignTask".equalsIgnoreCase(type)&&"Active".equalsIgnoreCase(current)){
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 1. 从XML读取审批内容对象类型、审批节点国际化资源和超期工作日；
     * 2. 一次查询Assigned状态的Inbox Task及其Route关联内容对象，避免逐任务查询Route；
     * 3. 仅处理同时匹配对象类型和审批节点、且已超过配置工作日的任务；
     * 4. 同一审批人的多条超期任务合并到一封邮件，每天由定时器执行一次。
     * @param context
     * @param args 定时器调用参数
     * @author LIUJR
     * @throws Exception
     * @return void
     * @date 2026/7/30
     * @description 通用审批任务超期邮件提醒
     */
    public void sendApprovalTaskOverdueEmailReminder(Context context, String[] args) throws Exception {
        JF_LOGGER.info("sendApprovalTaskOverdueEmailReminder start:{}", new Date());
        try {
            MapList configList = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(
                    context,
                    "ApprovalTaskOverdueEmailReminder");
            if (configList == null || configList.isEmpty()) {
                return;
            }

            // 按审批内容对象类型归集规则；每条规则保存节点的多语言标题和工作日阈值。
            Map ruleMap = new LinkedHashMap();
            int minimumWorkdays = Integer.MAX_VALUE;
            for (Object configObj : configList) {
                Map configMap = (Map) configObj;
                String objectType = UIUtil.getValue(configMap, "objectType");
                String taskNodeBundle = UIUtil.getValue(configMap, "taskNodeBundle");
                String taskNodeKey = UIUtil.getValue(configMap, "taskNodeKey");
                String overdueWorkdaysValue = UIUtil.getValue(configMap, "overdueWorkdays");
                //天数判断
                int overdueWorkdays;
                try {
                    overdueWorkdays = Integer.parseInt(overdueWorkdaysValue.trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                if (overdueWorkdays < 0) {
                    continue;
                }
                //拿取国际化Title
                Set taskNodeTitleSet = new HashSet();
                taskNodeTitleSet.add(EnoviaResourceBundle.getProperty(context, taskNodeBundle, Locale.CHINA, taskNodeKey + ".CH"));
                taskNodeTitleSet.add(EnoviaResourceBundle.getProperty(context, taskNodeBundle, Locale.CHINA, taskNodeKey + ".EN"));

                Map rule = new HashMap();
                rule.put("taskNodeTitleSet", taskNodeTitleSet);
                rule.put("overdueWorkdays", overdueWorkdays);
                if (!ruleMap.containsKey(objectType)) {
                    ruleMap.put(objectType, new MapList());
                }
                ((MapList) ruleMap.get(objectType)).add(rule);
                minimumWorkdays = Math.min(minimumWorkdays, overdueWorkdays);
            }
            if (ruleMap.isEmpty()) {
                JF_LOGGER.warn("sendApprovalTaskOverdueEmailReminder has no valid rule");
                return;
            }
            JF_LOGGER.info("sendApprovalTaskOverdueEmailReminder ruleMap:{}", ruleMap);

            LocalDate today = LocalDate.now();
            String reportDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            // 使用平台日期时间格式生成截止时间，避免originated完整时间依赖隐式日期解析。
            String candidateDate = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US)
                    .format(Date.from(today.minusDays(minimumWorkdays).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
            String assignedState = PropertyUtil.getSchemaProperty(
                    context,
                    "policy",
                    DomainObject.POLICY_INBOX_TASK,
                    "state_Assigned");
            String taskWhere = "current==" + assignedState
                    + "&&originated<'" + candidateDate + "'"
                    + "&&from[" + DomainConstants.RELATIONSHIP_ROUTE_TASK + "].to.attribute["
                    + DomainConstants.ATTRIBUTE_ROUTE_STATUS + "]!=Stopped";
            JF_LOGGER.info("sendApprovalTaskOverdueEmailReminder taskWhere:{}", taskWhere);
            String contentSelectPrefix = "from[Route Task].to.to[Object Route].from.";
            String contentNameSelect = contentSelectPrefix + DomainConstants.SELECT_NAME;
            String contentTypeSelect = contentSelectPrefix + DomainConstants.SELECT_TYPE;
            String contentTitleSelect = contentSelectPrefix + DomainConstants.SELECT_ATTRIBUTE_TITLE;
            StringList taskSelectList = JF_Util_mxJPO.basicBolistSel();
            taskSelectList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
            taskSelectList.add("originated");
            taskSelectList.add(contentNameSelect);
            taskSelectList.add(contentTypeSelect);
            taskSelectList.add(contentTitleSelect);
            MapList taskList = DomainObject.findObjects(
                    context,
                    DomainConstants.TYPE_INBOX_TASK,
                    "*",
                    taskWhere,
                    taskSelectList);
            JF_LOGGER.info("sendApprovalTaskOverdueEmailReminder candidate task size:{}", taskList.size());

            String baseUrl = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"JF.3dspace.JFUrl"});
            Map personReminderMap = new LinkedHashMap();
            for (Object taskObj : taskList) {
                Map taskMap = (Map) taskObj;
                String contentType = UIUtil.getValue(taskMap, contentTypeSelect);
                if (!ruleMap.containsKey(contentType)) {
                    continue;
                }
                JF_LOGGER.info("contentType:{}", contentType);
                String taskTitle = UIUtil.getValue(taskMap, DomainConstants.SELECT_ATTRIBUTE_TITLE);
                LocalDate assignedDate = parseProjectTaskDueReminderDate(UIUtil.getValue(taskMap, "originated"));
                if (assignedDate == null) {
                    continue;
                }
                //超期天数
                int elapsedWorkdays = getProjectTaskDueReminderWorkdayDiff(assignedDate, today);
                JF_LOGGER.info("elapsedWorkdays:{}", elapsedWorkdays);
                boolean needReminder = false;
                MapList typeRuleList = (MapList) ruleMap.get(contentType);
                for (Object ruleObj : typeRuleList) {
                    Map rule = (Map) ruleObj;
                    Set taskNodeTitleSet = (Set) rule.get("taskNodeTitleSet");
                    int overdueWorkdays = (Integer) rule.get("overdueWorkdays");
                    JF_LOGGER.info("elapsedWorkdays > overdueWorkdays:{}", elapsedWorkdays > overdueWorkdays);
                    JF_LOGGER.info("taskNodeTitleSet.contains(taskTitle):{}", taskNodeTitleSet.contains(taskTitle));
                    if (taskNodeTitleSet.contains(taskTitle) && elapsedWorkdays > overdueWorkdays) {
                        needReminder = true;
                        break;
                    }
                }
                //是否需要发邮件
                if (!needReminder) {
                    continue;
                }
                //发送人
                String taskOwner = UIUtil.getValue(taskMap, DomainConstants.SELECT_OWNER);
                if (!personReminderMap.containsKey(taskOwner)) {
                    personReminderMap.put(taskOwner, new MapList());
                }
                Map record = new HashMap();
                String contentTitle = UIUtil.getValue(taskMap, contentTitleSelect);
                record.put("contentName", UIUtil.getValue(taskMap, contentNameSelect));
                record.put("contentTitle", UIUtil.isNotNullAndNotEmpty(contentTitle) ? contentTitle : UIUtil.getValue(taskMap, contentNameSelect));
                record.put("taskTitle", taskTitle);
                record.put("assignedDate", UIUtil.getValue(taskMap, "originated"));
                record.put("elapsedWorkdays", elapsedWorkdays);
                record.put("linkAddress", baseUrl + UIUtil.getValue(taskMap, DomainConstants.SELECT_ID));
                ((MapList) personReminderMap.get(taskOwner)).add(record);
            }
            JF_LOGGER.info("sendApprovalTaskOverdueEmailReminder personReminderMap:{}", personReminderMap);

            // 人员信息按审批人查询一次，避免同一审批人的每条任务重复查询邮箱。
            DomainObject personObject = DomainObject.newInstance(context);
            for (Object entryObj : personReminderMap.entrySet()) {
                Map.Entry entry = (Map.Entry) entryObj;
                String personName = (String) entry.getKey();
                MapList personTaskList = (MapList) entry.getValue();
                try {
                    personObject.setId(PersonUtil.getPersonObjectID(context, personName));
                    String email = personObject.getAttributeValue(context, "Email Address");
                    JF_LOGGER.info("email:{}", email);
                    Set emailSet = new HashSet();
                    emailSet.add(email);
                    emailSet.add("liujr@tecwin.com");
                    emailSet.add("chencb@tecwin.com");
                    emailSet.add("linh@tecwin.com");
                    if (emailSet == null || emailSet.isEmpty()) {
                        continue;
                    }
                    email = StringList.create(emailSet).join(",");
                    String fullName = PersonUtil.getFullName(context, personName);
                    if (UIUtil.isNullOrEmpty(email)) {
                        continue;
                    }
                    Boolean sendResult = JF_SendEmailUtils_mxJPO.sendApprovalTaskOverdueEmailReminder(
                            context,
                            email,
                            personTaskList,
                            UIUtil.isNotNullAndNotEmpty(fullName) ? fullName : personName,
                            reportDate);
                } catch (Exception e) {
                    // 单个审批人邮件失败不影响其他审批人的提醒。
                    JF_LOGGER.error("sendApprovalTaskOverdueEmailReminder send error, person:{}", personName, e);
                }
            }
        } catch (Exception e) {
            JF_LOGGER.error("sendApprovalTaskOverdueEmailReminder error", e);
            throw e;
        } finally {
            JF_LOGGER.info("sendApprovalTaskOverdueEmailReminder end:{}", new Date());
        }
    }

    /**
     * 1. 每天由定时器查询工作中项目下的未完成项目任务；
     * 2. 只处理2026年7月1日及之后创建的任务；
     * 3. 当前只启用到期前10个工作日提醒，3天及超期提醒预计2026年10月2日后恢复；
     * 4. 责任人优先取非ESO审核员的任务分派人，没有有效分派人时取任务owner；
     * 5. 同一责任人的任务按提醒类型拆分邮件，同一类型内的多项目任务合并展示。
     * @param context
     * @param args 定时器调用参数
     * @author LIUJR
     * @throws Exception
     * @return void
     * @date 2026/7/2 15:28
     * @description 项目任务到期和超期汇总提醒
     */
    public void sendProjectTaskDueReminder(Context context, String[] args) throws Exception {
        JF_LOGGER.info("sendProjectTaskDueReminder start:{}", new Date());
        try {
            // 当前日期作为所有任务到期判断的统一基准。
            LocalDate today = LocalDate.now();
            // 邮件开头展示的统计日期。
            String reportDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            JF_LOGGER.info("reportDate:{}", reportDate);
            // 读取PLM对象访问地址前缀，后续每一行任务都生成独立链接。
            Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
            String baseUrl = prop.getProperty("JF.3dspace.JFUrl").trim();
            // 按责任人和提醒类型分别汇总，避免不同升级级别的任务共用收件人。
            Map personMailMap = new LinkedHashMap();
            // 查询正在工作中的项目对象。
            StringList projectSelectList = JF_Util_mxJPO.basicBolistSel();
            projectSelectList.add(DomainConstants.SELECT_DESCRIPTION);
            projectSelectList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
            MapList projectList = DomainObject.findObjects(context, DomainConstants.TYPE_PROJECT_SPACE, "*", "current==Active&&type=='Project Space'", projectSelectList);
            JF_LOGGER.info("sendProjectTaskDueReminder active project size:{}", projectList.size());
            // 定义任务查询select，避免循环中重复创建。
            StringList taskSelectList = JF_Util_mxJPO.basicBolistSel();
            taskSelectList.add(DomainConstants.SELECT_OWNER);
            taskSelectList.add(DomainConstants.SELECT_DESCRIPTION);
            taskSelectList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
            taskSelectList.add(DomainConstants.SELECT_ORIGINATED);
            taskSelectList.add("attribute[Task Estimated Finish Date]");
            StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
            String taskWhere = "current!=Complete && type!='" + JF_PLMConstants_mxJPO.TYPE_JF_ESOTask + "'";
            DomainObject projectObject = DomainObject.newInstance(context);
            DomainObject taskObject = DomainObject.newInstance(context);
            JF_ESO_mxJPO esoService = new JF_ESO_mxJPO();
            // 遍历每个Active项目，分别查询项目WBS中的未完成Task。
            for (int i = 0; i < projectList.size(); i++) {
                Map projectMap = (Map) projectList.get(i);
                String projectId = UIUtil.getValue(projectMap, DomainConstants.SELECT_ID);
                String projectName = UIUtil.getValue(projectMap, DomainConstants.SELECT_NAME);
                String projectTitle = UIUtil.getValue(projectMap, DomainConstants.SELECT_ATTRIBUTE_TITLE);
                String projectDesc = UIUtil.getValue(projectMap, DomainConstants.SELECT_DESCRIPTION);
                String projectDisplayName = UIUtil.isNotNullAndNotEmpty(projectDesc) ? projectDesc + "(" + projectName + ")" : (UIUtil.isNotNullAndNotEmpty(projectTitle) ? projectTitle + "(" + projectName + ")" : projectName);
                projectObject.setId(projectId);
                JF_LOGGER.info("projectName:{}", projectName);
                MapList taskList = projectObject.getRelatedObjects(context,
                        DomainConstants.RELATIONSHIP_SUBTASK,
                        "*",
                        taskSelectList,
                        relSelectList,
                        false,
                        true,
                        (short) 0,
                        taskWhere,
                        DomainConstants.EMPTY_STRING,
                        0);
                // 遍历项目下所有未完成任务，按计划完成日期判断是否需要提醒。
                for (int j = 0; j < taskList.size(); j++) {
                    Map taskMap = (Map) taskList.get(j);
                    String taskId = UIUtil.getValue(taskMap, DomainConstants.SELECT_ID);
                    String taskType = UIUtil.getValue(taskMap, DomainConstants.SELECT_TYPE);
                    String originatedValue = UIUtil.getValue(taskMap, DomainConstants.SELECT_ORIGINATED);
                    LocalDate originatedDate = parseProjectTaskDueReminderDate(originatedValue);
                    if (!taskType.equalsIgnoreCase("Task")) {
                        continue;
                    }
                    // 创建日期为空、无法解析或早于2026年7月1日时，不参与到期提醒。
                    if (originatedDate == null || originatedDate.isBefore(PROJECT_TASK_DUE_REMINDER_START_DATE)) {
                        continue;
                    }
                    String finishDateValue = UIUtil.getValue(taskMap, "attribute[Task Estimated Finish Date]");
                    LocalDate finishDate = parseProjectTaskDueReminderDate(finishDateValue);
                    JF_LOGGER.info("finishDateValue:{}", finishDateValue);
                    // 没有计划完成时间或时间无法解析时无法判断提醒窗口，直接跳过。
                    if (finishDate == null) {
                        continue;
                    }
                    String finishDateDisplay = finishDate.format(DateTimeFormatter.ofPattern("M/d/yyyy"));
                    String reminderType = DomainConstants.EMPTY_STRING;
                    int workdayDiff = getProjectTaskDueReminderWorkdayDiff(today, finishDate);
                    JF_LOGGER.info("workdayDiff:{}", workdayDiff);
                    // 到期前10个工作日只在精确当天提醒。
                    if (workdayDiff == 10) {
                        reminderType = "due10";
                    }
                    // TODO 2026-10-02后恢复：3天及超期任务整类提醒，包括责任人和升级人员。
                    /*
                    else if (workdayDiff == 3) {
                        reminderType = "due3";
                    } else if (today.isAfter(finishDate)) {
                        reminderType = "overdue";
                    }
                    */
                    // 当前未命中已启用的10天提醒时，任务不进入邮件。
                    if (UIUtil.isNullOrEmpty(reminderType)) {
                        continue;
                    }
                    String owner = UIUtil.getValue(taskMap, DomainConstants.SELECT_OWNER);
                    // 按规则获取责任人：有效分派人优先，没有则使用owner。
                    StringList responsibleList = getProjectTaskDueReminderResponsibleList(context, taskId, owner);
                    // 邮件中的项目阶段使用任务所属的Phase。
                    taskObject.setId(taskId);
                    Map phaseInfo = esoService.getFirstPhaseByTask(context, taskObject);
                    String projectPhase = UIUtil.getValue(phaseInfo, DomainConstants.SELECT_NAME);
                    for (int k = 0; k < responsibleList.size(); k++) {
                        addProjectTaskDueReminderRecord(context, personMailMap, responsibleList.get(k), reminderType, projectId, projectDisplayName, taskMap, projectPhase, finishDateDisplay, baseUrl);
                    }
                }
            }
            // 汇总完成后按责任人和提醒类型逐封发送邮件。
            DomainObject personObject = DomainObject.newInstance(context);
            for (Object entryObj : personMailMap.entrySet()) {
                Map.Entry entry = (Map.Entry) entryObj;
                String personName = (String) entry.getKey();
                Map reminderMailMap = (Map) entry.getValue();
                String firstName = personName;
                try {
                    personObject.setId(PersonUtil.getPersonObjectID(context, personName));
                    String tempFirstName = personObject.getAttributeValue(context, "First Name");
                    if (UIUtil.isNotNullAndNotEmpty(tempFirstName)) {
                        firstName = tempFirstName;
                    }
                } catch (Exception e) {
                    JF_LOGGER.warn("sendProjectTaskDueReminder get firstName error person:{}", personName, e);
                }
                for (Object reminderEntryObj : reminderMailMap.entrySet()) {
                    Map.Entry reminderEntry = (Map.Entry) reminderEntryObj;
                    String reminderType = (String) reminderEntry.getKey();
                    Map mailData = (Map) reminderEntry.getValue();
                    Set emailSet = (Set) mailData.get("emailSet");
                    if (emailSet == null) {
                        continue;
                    }
                    emailSet.add("liujr@tecwin.com");
                    emailSet.add("chencb@tecwin.com");
                    emailSet.add("linh@tecwin.com");
                    if (emailSet.isEmpty()) {
                        continue;
                    }
                    String toEmail = joinProjectTaskDueReminderEmails(emailSet);
                    JF_LOGGER.info("sendProjectTaskDueReminder send person:{}, reminderType:{}, email:{}", personName, reminderType, toEmail);
                    JF_SendEmailUtils_mxJPO.sendProjectTaskDueReminderEmail(context, toEmail, mailData, firstName, reportDate);
                }
            }
        } catch (Exception e) {
            JF_LOGGER.error("sendProjectTaskDueReminder error", e);
            throw e;
        } finally {
            JF_LOGGER.info("sendProjectTaskDueReminder end:{}", new Date());
        }
    }

    /**
     * 1. 获取项目任务提醒责任人；
     * 2. 任务存在分派人时优先取分派人，并排除拥有JfESOAdmin角色的ESO审核员；
     * 3. 没有有效分派人时回退到任务owner。
     * @param context
     * @param taskId 任务对象id
     * @param owner 任务owner
     * @author LIUJR
     * @throws Exception
     * @return matrix.util.StringList
     * @date 2026/7/2 15:28
     * @description 获取项目任务提醒责任人
     */
    private StringList getProjectTaskDueReminderResponsibleList(Context context, String taskId, String owner) throws Exception {
        StringList responsibleList = new StringList();
        String assignees = getAssigneesForObject(context, taskId);
        if (UIUtil.isNotNullAndNotEmpty(assignees)) {
            assignees = assignees.replace(";", ",").replace("|", ",").replace("，", ",");
            String[] assigneeArray = assignees.split(",");
            for (int i = 0; i < assigneeArray.length; i++) {
                String personName = assigneeArray[i] == null ? DomainConstants.EMPTY_STRING : assigneeArray[i].trim();
                if (UIUtil.isNullOrEmpty(personName) || responsibleList.contains(personName)) {
                    continue;
                }
                if (!isProjectTaskDueReminderESOReviewer(context, personName)) {
                    responsibleList.add(personName);
                }
            }
        }
        if (responsibleList.isEmpty() && UIUtil.isNotNullAndNotEmpty(owner)) {
            responsibleList.add(owner);
        }
        return responsibleList;
    }

    /**
     * 1. 判断人员是否拥有ESO审核员角色；
     * 2. 分派人拥有JfESOAdmin时不作为项目任务提醒责任人；
     * 3. 判断异常时按非ESO审核员处理，避免异常人员影响整批提醒。
     * @param context
     * @param personName 人员账号
     * @author LIUJR
     * @throws Exception
     * @return boolean
     * @date 2026/7/2 15:28
     * @description 判断是否为ESO审核员
     */
    private boolean isProjectTaskDueReminderESOReviewer(Context context, String personName) throws Exception {
        try {
            return PersonUtil.getAssignments(context, personName).contains("JfESOAdmin");
        } catch (Exception e) {
            JF_LOGGER.warn("isProjectTaskDueReminderESOReviewer error person:{}", personName, e);
            return false;
        }
    }

    /**
     * 1. 将单条任务提醒加入责任人对应提醒类型的邮件汇总数据；
     * 2. 邮件数据先按责任人、提醒类型拆分，再按项目合并；
     * 3. 根据提醒类型同步补充直线经理和部门经理收件人。
     * @param context
     * @param personMailMap 责任人邮件汇总Map
     * @param responsible 责任人账号
     * @param reminderType 提醒类型
     * @param projectId 项目id
     * @param projectName 项目显示名称
     * @param taskMap 任务数据
     * @param projectPhase 任务所属项目阶段
     * @param finishDateDisplay 邮件展示的计划完成日期
     * @param baseUrl 对象链接前缀
     * @author LIUJR
     * @throws Exception
     * @return void
     * @date 2026/7/2 15:28
     * @description 添加项目任务提醒记录
     */
    private void addProjectTaskDueReminderRecord(Context context, Map personMailMap, String responsible, String reminderType, String projectId, String projectName, Map taskMap, String projectPhase, String finishDateDisplay, String baseUrl) throws Exception {
        Map reminderMailMap = (Map) personMailMap.get(responsible);
        if (reminderMailMap == null) {
            reminderMailMap = new LinkedHashMap();
            personMailMap.put(responsible, reminderMailMap);
        }
        Map mailData = (Map) reminderMailMap.get(reminderType);
        if (mailData == null) {
            mailData = new LinkedHashMap();
            mailData.put("emailSet", new LinkedHashSet());
            mailData.put("projectMap", new LinkedHashMap());
            reminderMailMap.put(reminderType, mailData);
        }
        Set emailSet = (Set) mailData.get("emailSet");
        addProjectTaskDueReminderEmailByPersonName(context, emailSet, responsible);
        if ("due3".equals(reminderType) || "overdue".equals(reminderType)) {
            String lineManagerId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, responsible);
            addProjectTaskDueReminderEmailByPersonId(context, emailSet, lineManagerId);
        }
        if ("overdue".equals(reminderType)) {
            String departmentPersons = getDepartMentHead(context, responsible);
            if (UIUtil.isNotNullAndNotEmpty(departmentPersons)) {
                departmentPersons = departmentPersons.replace(";", ",").replace("|", ",").replace("，", ",");
                String[] personArray = departmentPersons.split(",");
                for (int i = 0; i < personArray.length; i++) {
                    addProjectTaskDueReminderEmailByPersonName(context, emailSet, personArray[i] == null ? DomainConstants.EMPTY_STRING : personArray[i].trim());
                }
            }
        }
        Map projectDataMap = (Map) mailData.get("projectMap");
        Map projectData = (Map) projectDataMap.get(projectId);
        if (projectData == null) {
            projectData = new LinkedHashMap();
            projectData.put("projectName", projectName);
            projectData.put("due10", new MapList());
            projectData.put("due3", new MapList());
            projectData.put("overdue", new MapList());
            projectDataMap.put(projectId, projectData);
        }
        Map record = new LinkedHashMap();
        String taskId = UIUtil.getValue(taskMap, DomainConstants.SELECT_ID);
        record.put("taskId", taskId);
        record.put("taskName", UIUtil.getValue(taskMap, DomainConstants.SELECT_NAME));
        record.put("projectPhase", projectPhase);
        record.put("finishDate", finishDateDisplay);
        record.put("responsible", getProjectTaskDueReminderResponsibleFullName(context, responsible));
        record.put("linkAddress", baseUrl + taskId);
        ((MapList) projectData.get(reminderType)).add(record);
    }

    /**
     * 获取邮件表格中展示的责任人全名，人员资料异常或全名为空时回退到UID。
     */
    private String getProjectTaskDueReminderResponsibleFullName(Context context, String responsible) {
        try {
            String fullName = PersonUtil.getFullName(context, responsible);
            return UIUtil.isNotNullAndNotEmpty(fullName) ? fullName : responsible;
        } catch (Exception e) {
            JF_LOGGER.warn("getProjectTaskDueReminderResponsibleFullName error person:{}", responsible, e);
            return responsible;
        }
    }

    /**
     * 1. 根据人员账号获取邮箱并加入收件人集合；
     * 2. 收件人集合使用Set去重；
     * 3. 人员不存在或邮箱为空时跳过。
     * @param context
     * @param emailSet 收件人集合
     * @param personName 人员账号
     * @author LIUJR
     * @throws Exception
     * @return void
     * @date 2026/7/2 15:28
     * @description 添加人员邮箱
     */
    private void addProjectTaskDueReminderEmailByPersonName(Context context, Set emailSet, String personName) throws Exception {
        if (UIUtil.isNullOrEmpty(personName)) {
            return;
        }
        try {
            addProjectTaskDueReminderEmailByPersonId(context, emailSet, PersonUtil.getPersonObjectID(context, personName));
        } catch (Exception e) {
            JF_LOGGER.warn("addProjectTaskDueReminderEmailByPersonName error person:{}", personName, e);
        }
    }

    /**
     * 1. 根据人员id获取邮箱并加入收件人集合；
     * 2. 邮箱为空时跳过；
     * 3. 该方法用于责任人、直线经理和部门经理统一补充To收件人。
     * @param context
     * @param emailSet 收件人集合
     * @param personId 人员id
     * @author LIUJR
     * @throws Exception
     * @return void
     * @date 2026/7/2 15:28
     * @description 添加人员邮箱
     */
    private void addProjectTaskDueReminderEmailByPersonId(Context context, Set emailSet, String personId) throws Exception {
        if (UIUtil.isNullOrEmpty(personId)) {
            return;
        }
        DomainObject personObject = DomainObject.newInstance(context, personId);
        String email = personObject.getAttributeValue(context, "Email Address");
        if (UIUtil.isNotNullAndNotEmpty(email)) {
            emailSet.add(email);
        }
    }

    /**
     * 1. 将收件人集合拼接为邮件地址字符串；
     * 2. 邮箱之间使用英文逗号分隔；
     * 3. 空邮箱不参与拼接。
     * @param emailSet 收件人集合
     * @author LIUJR
     * @throws Exception
     * @return java.lang.String
     * @date 2026/7/2 15:28
     * @description 拼接邮件地址
     */
    private String joinProjectTaskDueReminderEmails(Set emailSet) throws Exception {
        StringBuilder builder = new StringBuilder();
        for (Object emailObj : emailSet) {
            String email = emailObj == null ? DomainConstants.EMPTY_STRING : emailObj.toString();
            if (UIUtil.isNullOrEmpty(email)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(email);
        }
        return builder.toString();
    }

    /**
     * 1. 计算两个日期之间的工作日差值；
     * 2. 结束日期晚于开始日期时返回正数，用于10天和3天到期前提醒；
     * 3. 结束日期早于开始日期时返回负数，用于判断超期天数。
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @author LIUJR
     * @throws Exception
     * @return int
     * @date 2026/7/2 15:28
     * @description 计算工作日差值
     */
    private int getProjectTaskDueReminderWorkdayDiff(LocalDate startDate, LocalDate endDate) throws Exception {
        int count = 0;
        LocalDate date = startDate;
        if (startDate == null || endDate == null || startDate.equals(endDate)) {
            return count;
        }
        if (startDate.isBefore(endDate)) {
            while (date.isBefore(endDate)) {
                date = date.plusDays(1);
                if (isProjectTaskDueReminderWorkday(date)) {
                    count++;
                }
            }
        } else {
            while (date.isAfter(endDate)) {
                if (isProjectTaskDueReminderWorkday(date)) {
                    count--;
                }
                date = date.minusDays(1);
            }
        }
        return count;
    }

    /**
     * 1. 判断日期是否为工作日；
     * 2. 当前口径只按周一到周五处理；
     * 3. 不识别法定节假日和调休。
     * @param date 日期
     * @author LIUJR
     * @throws Exception
     * @return boolean
     * @date 2026/7/2 15:28
     * @description 判断工作日
     */
    private boolean isProjectTaskDueReminderWorkday(LocalDate date) throws Exception {
        return date != null && date.getDayOfWeek().getValue() <= 5;
    }

    /**
     * 从指定日期向前计算工作日日期，周六和周日不计入天数。
     **
     * @param date 基准日期
     * @param workdays 向前计算的工作日数量
     * @return LocalDate 计算后的日期
     * @throws Exception
     * @author caipan
     * @date 2026/8/3 16:13
     */
    private LocalDate getDateBeforeWorkdays(LocalDate date, int workdays) throws Exception {
        LocalDate result = date;
        int elapsedWorkdays = 0;
        while (elapsedWorkdays < workdays) {
            result = result.minusDays(1);
            if (isProjectTaskDueReminderWorkday(result)) {
                elapsedWorkdays++;
            }
        }
        return result;
    }

    /**
     * 1. 将ENOVIA日期字符串解析为LocalDate；
     * 2. 兼容常见英文AM/PM、24小时制、年月日和纯日期格式；
     * 3. 解析失败时返回null，由调用方跳过该任务。
     * @param value 日期字符串
     * @author LIUJR
     * @throws Exception
     * @return java.time.LocalDate
     * @date 2026/7/2 15:28
     * @description 解析任务计划完成日期
     */
    private LocalDate parseProjectTaskDueReminderDate(String value) throws Exception {
        if (UIUtil.isNullOrEmpty(value)) {
            return null;
        }
        String val = value.trim();
        String[] dateTimePatterns = new String[]{"M/d/yyyy h:mm:ss a", "MM/dd/yyyy h:mm:ss a", "M/d/yyyy H:mm:ss", "MM/dd/yyyy H:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss"};
        for (int i = 0; i < dateTimePatterns.length; i++) {
            try {
                return LocalDateTime.parse(val, DateTimeFormatter.ofPattern(dateTimePatterns[i], Locale.US)).toLocalDate();
            } catch (Exception ignored) {
            }
        }
        String[] datePatterns = new String[]{"M/d/yyyy", "MM/dd/yyyy", "yyyy-MM-dd", "yyyy/MM/dd"};
        for (int i = 0; i < datePatterns.length; i++) {
            try {
                return LocalDate.parse(val, DateTimeFormatter.ofPattern(datePatterns[i], Locale.US));
            } catch (Exception ignored) {
            }
        }
        try {
            return new SimpleDateFormat("M/d/yyyy h:mm:ss a", Locale.US).parse(val).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 产品配置表完成客户经理审批并提升状态后，通知项目SDT成员及相关业务对象Owner  trigger
     *
     * @param context
     * @param args args[0]为产品配置表ID
     * @return void
     * @throws Exception 查询通知数据或发送邮件失败时抛出异常
     * @author LIUJR
     * @date 2026/7/30
     */
    public void sendProductConfigManagerCompleteNotification(Context context, String[] args) throws Exception {
        String productConfigId = args != null && args.length > 0 ? args[0] : DomainConstants.EMPTY_STRING;
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            return;
        }

        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            DomainObject productConfigObject = DomainObject.newInstance(context, productConfigId);
            StringList productConfigSelects = StringList.create(
                    "to[JFProject2ProductConfigTable].from.id",
                    "to[JFProject2ProductConfigTable].from.name",
                    "to[JFProject2ProductConfigTable].from.description",
                    "to[JFConfigTableRoute2ProductConfigTable].from.id");
            Map productConfigInfo = productConfigObject.getInfo(context, productConfigSelects);
            String projectId = UIUtil.getValue(
                    productConfigInfo,
                    "to[JFProject2ProductConfigTable].from.id");
            if (UIUtil.isNullOrEmpty(projectId)) {
                JF_LOGGER.warn("Product config project is empty, productConfigId:{}", productConfigId);
                return;
            }
            String projectName = UIUtil.getValue(
                    productConfigInfo,
                    "to[JFProject2ProductConfigTable].from.description");
            if (UIUtil.isNullOrEmpty(projectName)) {
                projectName = UIUtil.getValue(
                        productConfigInfo,
                        "to[JFProject2ProductConfigTable].from.name");
            }

            // 客户经理意见以已完成的JF_ProductConfigTask说明为准。
            StringList managerComments = productConfigObject.getInfoList(
                    context,
                    "from[JFProductConfigTable2ProductConfigTask].to.description");
            String businessManagerComment = managerComments.isEmpty()
                    ? DomainConstants.EMPTY_STRING
                    : String.valueOf(managerComments.get(managerComments.size() - 1));

            // 由产品配置表反查本次审批申请，再读取整椅经理Route Task审批意见。
            String routeApplyId = UIUtil.getValue(
                    productConfigInfo,
                    "to[JFConfigTableRoute2ProductConfigTable].from.id");
            String chairManagerComment = DomainConstants.EMPTY_STRING;
            StringList objectSelects = JF_Util_mxJPO.basicBolistSel();
            objectSelects.add(DomainConstants.SELECT_POLICY);
            MapList emailObjectList = new MapList();
            emailObjectList.add(productConfigObject.getInfo(context, objectSelects));
            DomainObject projectObject = DomainObject.newInstance(context, projectId);

            // 项目关系会随升版复制，只展示当前项目最新版本的DB件清单。
            emailObjectList.addAll(projectObject.getRelatedObjects(
                    context,
                    "JFProject2DBList",
                    "JFDBList",
                    objectSelects,
                    new StringList(),
                    false,
                    true,
                    (short) 1,
                    "islast==TRUE",
                    DomainConstants.EMPTY_STRING,
                    (short) 0));

            // 售后件清单必须取本次产品配置审批申请实际关联的对象。
            if (UIUtil.isNotNullAndNotEmpty(routeApplyId)) {
                DomainObject routeApplyObject = DomainObject.newInstance(context, routeApplyId);
                StringList chairComments = routeApplyObject.getInfoList(
                        context,
                        "from[" + DomainConstants.RELATIONSHIP_OBJECT_ROUTE
                                + "].to.to[" + DomainConstants.RELATIONSHIP_ROUTE_TASK
                                + "].from.attribute[" + DomainObject.ATTRIBUTE_COMMENTS + "]");
                chairManagerComment = chairComments.isEmpty()
                        ? DomainConstants.EMPTY_STRING
                        : String.valueOf(chairComments.get(chairComments.size() - 1));
                emailObjectList.addAll(routeApplyObject.getRelatedObjects(
                        context,
                        "JFConfigTableRoute2ServicePartsList",
                        "JFServicePartsList",
                        objectSelects,
                        new StringList(),
                        false,
                        true,
                        (short) 1,
                        DomainConstants.EMPTY_STRING,
                        DomainConstants.EMPTY_STRING,
                        (short) 0));
            }

            //20260804 update by ljr 项目收件人仅取Member关系Project Role不为空的SDT成员，以及相关业务对象Owner。
            Set<String> emailSet = new LinkedHashSet<>();
            emailSet.addAll(JF_PublicMethodClass_mxJPO.getProjectSDTPersons(context,projectId, "attribute[Email Address]"));
            JF_LOGGER.warn("emailSet:{}", emailSet);

            Set<String> ownerSet = new LinkedHashSet<>();
            for (Object objectObj : emailObjectList) {
                Map emailObjectMap = (Map) objectObj;
                String objectType = UIUtil.getValue(emailObjectMap, DomainConstants.SELECT_TYPE);
                // 20260831 新需求：客户经理审核完成邮件不通知售后件清单Owner。
                // 后续如需恢复通知，删除下面的if判断即可；售后件清单仍保留在邮件正文中。
                if ("JFServicePartsList".equals(objectType)) {
                    continue;
                }
                ownerSet.add(UIUtil.getValue(emailObjectMap, DomainConstants.SELECT_OWNER));
            }
            ownerSet.remove(DomainConstants.EMPTY_STRING);
            for (String owner : ownerSet) {
                String personId = PersonUtil.getPersonObjectID(context, owner);
                String email = DomainObject.newInstance(context, personId)
                        .getAttributeValue(context, "Email Address");
                if (UIUtil.isNotNullAndNotEmpty(email)) {
                    emailSet.add(email.trim());
                }
            }
            emailSet.remove(DomainConstants.EMPTY_STRING);
            emailSet.remove(null);
            if (emailSet.isEmpty()) {
                JF_LOGGER.warn("Product config notification recipient is empty, projectId:{}", projectId);
                return;
            }
            JF_LOGGER.warn("emailSet:{}", emailSet);
            Map mailData = new HashMap();
            mailData.put("chairManagerComment", chairManagerComment);
            mailData.put("businessManagerComment", businessManagerComment);
            mailData.put("projectName", projectName);
            mailData.put("baseUrl", JF_PublicMethodClass_mxJPO.getBasicUrl(
                    context,
                    new String[]{"JF.3dspace.JFUrl"}));
            mailData.put("objectList", emailObjectList);
            JF_SendEmailUtils_mxJPO.sendProductConfigManagerCompleteEmail(
                    context,
                    String.join(",", emailSet),
                    mailData);
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 项目P-2+3或P-3阶段超期时发送最新发布版缺失图纸零件提醒邮件
     **
     * @param context
     * @param args 定时任务参数
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/8/13 16:13
     */
    public void sendMissingDrawingsEmail(Context context, String[] args) throws Exception {
        boolean isPush = false;
        JF_LOGGER.info("sendMissingDrawingsEmail start");
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            Date currentDate = new Date();
            //20260903 update by caipan 排除已完成和已归档项目，并跨项目按零件Owner汇总邮件
            String projectWhere = "current!=" + ProgramCentralConstants.STATE_PROJECT_SPACE_COMPLETE
                    + "&&current!=" + ProgramCentralConstants.STATE_PROJECT_SPACE_ARCHIVE;
            StringList projectSelects = JF_Util_mxJPO.basicBolistSel();
            projectSelects.add(DomainConstants.SELECT_DESCRIPTION);
            MapList projectList = DomainObject.findObjects(context, DomainConstants.TYPE_PROJECT_SPACE,
                    DomainConstants.QUERY_WILDCARD, projectWhere, projectSelects);
//            JF_LOGGER.info("projectList:{}",projectList);
            StringList taskSelects = JF_Util_mxJPO.basicBolistSel();
            taskSelects.add(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            StringList candidatePartSelects = new StringList();
            candidatePartSelects.add(DomainConstants.SELECT_ID);
            candidatePartSelects.add(JF_PLMConstants_mxJPO.LOGICAL_ID);
            StringList partSelects = JF_Util_mxJPO.basicBolistSel();
            partSelects.add(DomainConstants.SELECT_OWNER);
            partSelects.add(JF_PLMConstants_mxJPO.LOGICAL_ID);
            partSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            partSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
            partSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
            partSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType);
            partSelects.add("attribute[" + JF_PLMConstants_mxJPO.ATTR_JF_Detail_CN + "]");
            partSelects.add("attribute[" + JF_PLMConstants_mxJPO.ATTR_JF_Detail_EN + "]");
            partSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
            partSelects.add("attribute[JF_VPMReference.JF_IsThereALackOfDrawings]");
            StringList partRelSelects = JF_Util_mxJPO.basicRellistSel();
            ProjectSpace projectObject = (ProjectSpace) DomainObject.newInstance(context,
                    DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
            Map<String, LinkedHashMap<String, Map>> ownerPartMap = new LinkedHashMap<>();
            Map<String, String> latestReleasedIdCache = new HashMap<>();
            for (Object projectItem : projectList) {
                Map projectMap = (Map) projectItem;
                String projectId = UIUtil.getValue(projectMap, DomainConstants.SELECT_ID);
                String projectName = UIUtil.getValue(projectMap, DomainConstants.SELECT_NAME);
                String projectDescription = UIUtil.getValue(projectMap, DomainConstants.SELECT_DESCRIPTION);
                try {
                    projectObject.setId(projectId);
                    MapList firstLevelTaskList = Task.getTasks(context, (TaskHolder) projectObject, 1,
                            taskSelects, new StringList());
//                    JF_LOGGER.info("firstLevelTaskList:{}",firstLevelTaskList);
                    if (!hasOverdueP23OrP3Phase(firstLevelTaskList, currentDate)) {
                        continue;
                    }
                    //20260903 update by caipan 先按已发布且缺图纸过滤，再按logicalid解析最新发布版并复核缺图纸属性
                    MapList candidatePartList = projectObject.getRelatedObjects(context,
                            JF_PLMConstants_mxJPO.rel_JFProject2RootPart,
                            JF_PLMConstants_mxJPO.TYPE_VPMReference,
                            candidatePartSelects,
                            partRelSelects,
                            false,
                            true,
                            (short) 1,
                            "current==RELEASED&&attribute[JF_VPMReference.JF_IsThereALackOfDrawings]==Yes",
                            "attribute[JF_BelongPart]==Y",
                            0);
                    if (candidatePartList.isEmpty()) {
                        continue;
                    }
                    Set<String> processedLogicalIdSet = new HashSet<>();
                    Set<String> latestReleasedIdSet = new LinkedHashSet<>();
                    for (Object candidatePartItem : candidatePartList) {
                        Map candidatePartMap = (Map) candidatePartItem;
                        String candidatePartId = UIUtil.getValue(candidatePartMap, DomainConstants.SELECT_ID);
                        String logicalId = UIUtil.getValue(candidatePartMap, JF_PLMConstants_mxJPO.LOGICAL_ID);
                        String logicalKey = UIUtil.isNullOrEmpty(logicalId) ? candidatePartId : logicalId;
                        if (UIUtil.isNullOrEmpty(logicalKey) || !processedLogicalIdSet.add(logicalKey)) {
                            continue;
                        }
                        String latestReleasedId;
                        if (latestReleasedIdCache.containsKey(logicalKey)) {
                            latestReleasedId = latestReleasedIdCache.get(logicalKey);
                        } else {
                            latestReleasedId = JF_Util_mxJPO.getLastReleasedMajorid(context, candidatePartId);
                            latestReleasedIdCache.put(logicalKey, latestReleasedId);
                        }
                        if (UIUtil.isNotNullAndNotEmpty(latestReleasedId)) {
                            latestReleasedIdSet.add(latestReleasedId);
                        }
                    }
                    if (latestReleasedIdSet.isEmpty()) {
                        continue;
                    }
                    MapList latestReleasedPartList = DomainObject.getInfo(context,
                            latestReleasedIdSet.toArray(new String[latestReleasedIdSet.size()]), partSelects);
                    MapList missingDrawingPartList = new MapList();
                    for (Object partItem : latestReleasedPartList) {
                        Map partMap = new HashMap((Map) partItem);
                        if (!"RELEASED".equals(UIUtil.getValue(partMap, DomainConstants.SELECT_CURRENT))
                                || !"Yes".equals(UIUtil.getValue(partMap,
                                "attribute[JF_VPMReference.JF_IsThereALackOfDrawings]"))) {
                            continue;
                        }
                        partMap.put("projectName", projectName);
                        missingDrawingPartList.add(partMap);
                        String owner = UIUtil.getValue(partMap, DomainConstants.SELECT_OWNER);
                        if (UIUtil.isNullOrEmpty(owner)) {
                            JF_LOGGER.warn("sendMissingDrawingsEmail skip part without owner, projectId:{}, partId:{}",
                                    projectId, UIUtil.getValue(partMap, DomainConstants.SELECT_ID));
                            continue;
                        }
                        LinkedHashMap<String, Map> ownerPartList = ownerPartMap.get(owner);
                        if (ownerPartList == null) {
                            ownerPartList = new LinkedHashMap<>();
                            ownerPartMap.put(owner, ownerPartList);
                        }
                        ownerPartList.put(UIUtil.getValue(partMap, DomainConstants.SELECT_ID), partMap);
                    }
                    if (missingDrawingPartList.isEmpty()) {
                        continue;
                    }
                    String chairManager = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId,
                            DomainConstants.SELECT_NAME, DomainConstants.EMPTY_STRING,
                            "attribute[Project Role]=='Chair manager'");
                    if (UIUtil.isNotNullAndNotEmpty(chairManager)) {
                        sendMissingDrawingsProjectEmail(context, projectName, projectDescription, chairManager,
                                missingDrawingPartList, true);
                    } else {
                        JF_LOGGER.warn("sendMissingDrawingsEmail chair manager is empty, projectId:{}", projectId);
                    }
                } catch (Exception e) {
                    JF_LOGGER.error("sendMissingDrawingsEmail process project error, projectId:{}", projectId, e);
                }
            }
            for (Map.Entry<String, LinkedHashMap<String, Map>> ownerEntry : ownerPartMap.entrySet()) {
                MapList ownerPartList = new MapList();
                ownerPartList.addAll(ownerEntry.getValue().values());
                try {
                    sendMissingDrawingsProjectEmail(context, DomainConstants.EMPTY_STRING,
                            DomainConstants.EMPTY_STRING, ownerEntry.getKey(), ownerPartList, false);
                } catch (Exception e) {
                    JF_LOGGER.error("sendMissingDrawingsEmail send owner email error, owner:{}",
                            ownerEntry.getKey(), e);
                }
            }
        } catch (Exception e) {
            JF_LOGGER.error("sendMissingDrawingsEmail error", e);
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        JF_LOGGER.info("sendMissingDrawingsEmail end");
    }

    /**
     * 判断项目第一层WBS是否存在已超期的P-2+3或P-3阶段
     **
     * @param firstLevelTaskList 项目第一层WBS
     * @param currentDate 当前时间
     * @return boolean 存在名称以P-2+3或P-3开头且计划完成时间早于当前时间的阶段时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/8/13 16:13
     */
    private boolean hasOverdueP23OrP3Phase(MapList firstLevelTaskList, Date currentDate) throws Exception {
        for (Object taskItem : firstLevelTaskList) {
            Map taskMap = (Map) taskItem;
            String phaseName = UIUtil.getValue(taskMap, DomainConstants.SELECT_NAME);
            //20260903 update by caipan 缺图纸提醒阶段由P-1调整为P-2+3或P-3
            if (!"Phase".equalsIgnoreCase(UIUtil.getValue(taskMap, DomainConstants.SELECT_TYPE))
                    || (!phaseName.startsWith("P-2+3") && !phaseName.startsWith("P-3"))) {
                continue;
            }
            String estimatedFinishDate = UIUtil.getValue(taskMap, Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            if (UIUtil.isNullOrEmpty(estimatedFinishDate)) {
                continue;
            }
            try {
                if (eMatrixDateFormat.getJavaDate(estimatedFinishDate).before(currentDate)) {
                    return true;
                }
            } catch (Exception e) {
                JF_LOGGER.warn("sendMissingDrawingsEmail skip invalid phase finish date, phaseId:{}",
                        UIUtil.getValue(taskMap, DomainConstants.SELECT_ID), e);
            }
        }
        return false;
    }

    /**
     * 向零件Owner或单个项目整椅经理发送缺失图纸提醒邮件
     **
     * @param context
     * @param projectName 整椅经理邮件对应的项目名称
     * @param projectDescription 整椅经理邮件对应的项目描述
     * @param recipient 收件人账号
     * @param partList 缺失图纸零件
     * @param isChairManager 是否为整椅经理汇总邮件
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/8/13 16:13
     */
    private void sendMissingDrawingsProjectEmail(Context context, String projectName, String projectDescription, String recipient,
                                                  MapList partList, boolean isChairManager) throws Exception {
        String email = getPersonEmail(context, recipient, null);
//        email="caip@tecwin.com";
        if (UIUtil.isNullOrEmpty(email)) {
            JF_LOGGER.warn("sendMissingDrawingsEmail recipient email is empty, projectName:{}, recipient:{}",
                    projectName, recipient);
            return;
        }
        org.jsoup.nodes.Document doc = Jsoup.parse(
                JF_PublicMethodClass_mxJPO.getPageHTMLResourceFile(context, "MissingDrawingsEmails", "zh"));
        //20260903 update by caipan Owner邮件跨项目汇总并隐藏顶部项目，整椅经理邮件保持按项目展示
        if (isChairManager) {
            doc.getElementById("projectName").text(projectName);
        } else if (doc.getElementById("projectInfo") != null) {
            doc.getElementById("projectInfo").remove();
        }
        doc.getElementById("recipientName").text(PersonUtil.getFullName(context, recipient));
        doc.getElementById("mailScope").text(isChairManager ? "以下为当前项目全部缺失图纸零件："
                : "以下为您负责的缺失图纸零件，请尽快补充图纸");
        StringList tableColumns = new StringList();
        tableColumns.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        tableColumns.add(DomainConstants.SELECT_REVISION);
        tableColumns.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
        tableColumns.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
        tableColumns.add("projectName");
        tableColumns.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType);
        tableColumns.add("attribute[" + JF_PLMConstants_mxJPO.ATTR_JF_Detail_CN + "]");
        tableColumns.add("attribute[" + JF_PLMConstants_mxJPO.ATTR_JF_Detail_EN + "]");
        tableColumns.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
        tableColumns.add("attribute[JF_VPMReference.JF_IsThereALackOfDrawings]");
        org.jsoup.nodes.Element tbody = doc.getElementById("missingDrawingsTbody");
        for (Object partItem : partList) {
            Map partMap = new HashMap((Map) partItem);
            if (UIUtil.isNullOrEmpty(UIUtil.getValue(partMap, "projectName"))) {
                partMap.put("projectName", projectName);
            }
            translateMissingDrawingsPartRange(context, partMap,
                    JF_PLMConstants_mxJPO.ATTR_JF_PartType,
                    JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType);
            translateMissingDrawingsPartRange(context, partMap,
                    JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType,
                    JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
            translateMissingDrawingsPartRange(context, partMap,
                    "JF_VPMReference.JF_IsThereALackOfDrawings",
                    "attribute[JF_VPMReference.JF_IsThereALackOfDrawings]");
            org.jsoup.nodes.Element tr = new org.jsoup.nodes.Element("tr");
            for (Object column : tableColumns) {
                String value = UIUtil.getValue(partMap, String.valueOf(column));
                tr.appendChild(JF_SendEmailUtils_mxJPO.createTd(UIUtil.isNullOrEmpty(value)
                        ? DomainConstants.EMPTY_STRING : value));
            }
            tbody.appendChild(tr);
        }
        BodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(doc.toString(), "text/html;charset=utf-8");
        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(bodyPart);
        String projectDisplayName = UIUtil.isNullOrEmpty(projectDescription)
                ? projectName : projectName + " " + projectDescription;
        String mailSubject = isChairManager ? projectDisplayName + "的零件缺失图纸提醒" : "零件缺失图纸提醒";
        Boolean sendResult = JF_SendEmailUtils_mxJPO.SendEmail(context, email, mailSubject, multipart);
        JF_LOGGER.info("sendMissingDrawingsEmail result:{}, projectName:{}, recipient:{}, partSize:{}",
                sendResult, projectName, recipient, partList.size());
    }

    /**
     * 将缺失图纸邮件中的零件Range属性转换为中文显示值
     **
     * @param context
     * @param partMap 零件查询数据
     * @param attributeName 属性名称
     * @param selectName 查询字段名称
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/8/13 16:13
     */
    private void translateMissingDrawingsPartRange(Context context, Map partMap, String attributeName,
                                                   String selectName) throws Exception {
        String rangeValue = UIUtil.getValue(partMap, selectName);
        if (UIUtil.isNotNullAndNotEmpty(rangeValue)) {
            partMap.put(selectName, EnoviaResourceBundle.getRangeI18NString(context, attributeName, rangeValue, "zh"));
        }
    }

    /**
     * 产品配置客户经理任务创建超过24小时后发送邮件提醒
     **
     * @param context
     * @param args 定时任务参数
     * @return void
     * @throws Exception
     * @author caipan by codex
     * @date 2026/9/1 16:13
     */
    public void sendProductConfigTaskOverdue(Context context, String[] args) throws Exception {
        JF_LOGGER.info("sendProductConfigTaskOverdue start:{}", new Date());
        long currentTime = System.currentTimeMillis();
        long twentyFourHours = 24L * 60L * 60L * 1000L;
        long fortyEightHours = 48L * 60L * 60L * 1000L;
        Map<String, MapList> ownerReminderMap = new LinkedHashMap<>();
        Map<String, MapList> directorReminderMap = new LinkedHashMap<>();

        String productConfigTaskType = PropertyUtil.getSchemaProperty(context, "type_JF_ProductConfigTask");
        if (UIUtil.isNullOrEmpty(productConfigTaskType)) {
            productConfigTaskType = "JF_ProductConfigTask";
        }
        String productConfigIdSelect = "to[JFProductConfigTable2ProductConfigTask].from.id";
        String productConfigNameSelect = "to[JFProductConfigTable2ProductConfigTask].from.name";
        String productConfigTitleSelect = "to[JFProductConfigTable2ProductConfigTask].from.attribute[Title]";
        StringList taskSelects = JF_Util_mxJPO.basicBolistSel();
        taskSelects.add(DomainConstants.SELECT_ORIGINATED);
        taskSelects.add(DomainConstants.SELECT_OWNER);
        taskSelects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        taskSelects.add(productConfigIdSelect);
        taskSelects.add(productConfigNameSelect);
        taskSelects.add(productConfigTitleSelect);
        MapList taskList = DomainObject.findObjects(context, productConfigTaskType, DomainConstants.QUERY_WILDCARD,
                "current==Active", taskSelects);

        for (Object taskItem : taskList) {
            Map taskInfo = (Map) taskItem;
            String taskId = UIUtil.getValue(taskInfo, DomainConstants.SELECT_ID);
            String originated = UIUtil.getValue(taskInfo, DomainConstants.SELECT_ORIGINATED);
            if (UIUtil.isNullOrEmpty(originated)) {
                JF_LOGGER.warn("sendProductConfigTaskOverdue skip task without originated, taskId:{}", taskId);
                continue;
            }
            long elapsedTime;
            try {
                elapsedTime = currentTime - eMatrixDateFormat.getJavaDate(originated).getTime();
            } catch (Exception e) {
                JF_LOGGER.warn("sendProductConfigTaskOverdue skip invalid originated, taskId:{}, originated:{}",
                        taskId, originated, e);
                continue;
            }
            if (elapsedTime <= twentyFourHours) {
                continue;
            }
            String taskOwner = UIUtil.getValue(taskInfo, DomainConstants.SELECT_OWNER);
            if (UIUtil.isNullOrEmpty(taskOwner)) {
                JF_LOGGER.warn("sendProductConfigTaskOverdue skip task without owner, taskId:{}", taskId);
                continue;
            }
            Map taskMap = new HashMap();
            taskMap.put("taskId", taskId);
            taskMap.put("taskName", UIUtil.getValue(taskInfo, DomainConstants.SELECT_NAME));
            taskMap.put("taskTitle", UIUtil.getValue(taskInfo, DomainConstants.SELECT_ATTRIBUTE_TITLE));
            taskMap.put("connectTitle", UIUtil.getValue(taskInfo, productConfigTitleSelect));
            taskMap.put("connectId", UIUtil.getValue(taskInfo, productConfigIdSelect));
            taskMap.put("connectName", UIUtil.getValue(taskInfo, productConfigNameSelect));
            taskMap.put("owner", taskOwner);
            taskMap.put("RouteType", "countersign");
            Map<String, MapList> reminderMap = elapsedTime > fortyEightHours ? directorReminderMap : ownerReminderMap;
            MapList ownerTaskList = reminderMap.get(taskOwner);
            if (ownerTaskList == null) {
                ownerTaskList = new MapList();
                reminderMap.put(taskOwner, ownerTaskList);
            }
            ownerTaskList.add(taskMap);
        }

        String businessDirectorEmails = "";
        if (!directorReminderMap.isEmpty()) {
            try {
                businessDirectorEmails = getBusinessDepartmentDirectorEmails(context);
            } catch (Exception e) {
                JF_LOGGER.error("sendProductConfigTaskOverdue get business director email error", e);
            }
        }
        List<Map<String, MapList>> reminderMaps = Arrays.asList(ownerReminderMap, directorReminderMap);
        for (int i = 0; i < reminderMaps.size(); i++) {
            String ccEmails = i == 1 ? businessDirectorEmails : "";
            for (Map.Entry<String, MapList> entry : reminderMaps.get(i).entrySet()) {
                String taskOwner = entry.getKey();
                try {
                    String personId = PersonUtil.getPersonObjectID(context, taskOwner);
                    DomainObject personObj = DomainObject.newInstance(context, personId);
                    String email = personObj.getAttributeValue(context, "Email Address");
//                    email="caip@tecwin.com";
                    if (UIUtil.isNullOrEmpty(email)) {
                        JF_LOGGER.warn("sendProductConfigTaskOverdue skip owner without email, owner:{}", taskOwner);
                        continue;
                    }
                    String firstName = personObj.getAttributeValue(context, "First Name");
                    if (UIUtil.isNullOrEmpty(firstName)) {
                        firstName = PersonUtil.getFullName(context, taskOwner);
                    }
                    Boolean sendResult = JF_SendEmailUtils_mxJPO.sendTaskEmailToPortalForTime(context, email,
                            ccEmails, "", entry.getValue(), firstName);
                    JF_LOGGER.info("sendProductConfigTaskOverdue result:{}, owner:{}, cc:{}, taskSize:{}",
                            sendResult, taskOwner, ccEmails, entry.getValue().size());
                } catch (Exception e) {
                    JF_LOGGER.error("sendProductConfigTaskOverdue send email error, owner:{}", taskOwner, e);
                }
            }
        }
        JF_LOGGER.info("sendProductConfigTaskOverdue end:{}", new Date());
    }

    /**
     * 根据配置的商务部门获取部门总监邮箱
     **
     * @param context
     * @return String 商务部门总监邮箱，多个邮箱以英文逗号分隔
     * @throws Exception
     * @author caipan by codex
     * @date 2026/9/1 16:13
     */
    private String getBusinessDepartmentDirectorEmails(Context context) throws Exception {
        String departmentName = NioJDUtils.getPageStr(context, "Department.Business.Name");
        if (UIUtil.isNullOrEmpty(departmentName)) {
            JF_LOGGER.warn("getBusinessDepartmentDirectorEmails Department.Business.Name is empty");
            return "";
        }
        StringList departmentSelects = JF_Util_mxJPO.basicBolistSel();
        departmentSelects.add(DomainConstants.SELECT_DESCRIPTION);
        String where = JF_PublicMethodClass_mxJPO.buildStringInStrings("name=='", departmentName, "'");
        MapList departmentList = DomainObject.findObjects(context, "Department", DomainConstants.QUERY_WILDCARD,
                where, departmentSelects);
        if (departmentList == null || departmentList.isEmpty()) {
            JF_LOGGER.warn("getBusinessDepartmentDirectorEmails department not found, departmentName:{}", departmentName);
            return "";
        }
        String directorNames = UIUtil.getValue((Map) departmentList.get(0), DomainConstants.SELECT_DESCRIPTION);
        if (UIUtil.isNullOrEmpty(directorNames)) {
            JF_LOGGER.warn("getBusinessDepartmentDirectorEmails department description is empty, departmentName:{}",
                    departmentName);
            return "";
        }
        Set<String> directorEmailSet = new LinkedHashSet<>();
        for (String directorNameValue : directorNames.split("[,，]")) {
            String directorName = directorNameValue.trim();
            if (UIUtil.isNullOrEmpty(directorName)) {
                continue;
            }
            try {
                String personId = PersonUtil.getPersonObjectID(context, directorName);
                if (UIUtil.isNullOrEmpty(personId)) {
                    JF_LOGGER.warn("getBusinessDepartmentDirectorEmails person not found, person:{}", directorName);
                    continue;
                }
                String email = DomainObject.newInstance(context, personId).getAttributeValue(context, "Email Address");
                if (UIUtil.isNotNullAndNotEmpty(email)) {
                    directorEmailSet.add(email);
                } else {
                    JF_LOGGER.warn("getBusinessDepartmentDirectorEmails person email is empty, person:{}", directorName);
                }
            } catch (Exception e) {
                JF_LOGGER.warn("getBusinessDepartmentDirectorEmails resolve person error, person:{}", directorName, e);
            }
        }
        return String.join(",", directorEmailSet);
    }

    /**
     * 全量刷新生产环境所有零件的是否缺失图纸字段 JF_VPMReference.JF_IsThereALackOfDrawings
     **
     * @param context
     * @param args
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/9/3 15:43
     */
    public void FullRefreshIsThereALackOfDrawings(Context context,String[] args) throws Exception {
        boolean isPush = false;
        int successCount = 0;
        int failureCount = 0;
        JF_LOGGER.info("FullRefreshIsThereALackOfDrawings start");
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            StringList partSelects = new StringList(DomainConstants.SELECT_ID);
            MapList partList = DomainObject.findObjects(
                    context,
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,
                    DomainConstants.QUERY_WILDCARD,
                    DomainConstants.EMPTY_STRING,
                    partSelects);
            JF_LOGGER.info("FullRefreshIsThereALackOfDrawings totalCount:{}", partList.size());
            JF_VPMReferenceEBOM_mxJPO ebomService = new JF_VPMReferenceEBOM_mxJPO();
            for (Object partItem : partList) {
                String partId = UIUtil.getValue((Map) partItem, DomainConstants.SELECT_ID);
                if (UIUtil.isNullOrEmpty(partId)) {
                    continue;
                }
                try {
                    ebomService.updatePartIsThereALackOfDrawings(context, partId);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    JF_LOGGER.error("FullRefreshIsThereALackOfDrawings refresh part error, partId:{}", partId, e);
                }
            }
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        JF_LOGGER.info("FullRefreshIsThereALackOfDrawings end, successCount:{}, failureCount:{}",
                successCount, failureCount);
        if (failureCount > 0) {
            throw new Exception("FullRefreshIsThereALackOfDrawings failed part count: " + failureCount);
        }
    }
}
