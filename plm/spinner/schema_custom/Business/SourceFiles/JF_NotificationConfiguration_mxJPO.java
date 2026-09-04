import com.dassault_systemes.enovia.dpm.notification.NotificationBase;
import com.dassault_systemes.enovia.dpm.notification.NotificationUtil;
import com.dassault_systemes.enovia.e6wv2.foundation.util.StringUtil;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MatrixWriter;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @ClassName JF_NotificationConfigurationTaskType_mxJPO
 * @Author: LIUJR
 * @CreateDate: 2024/8/20 13:32
 * @UpdateRemark:
 * @Version: 1.0
 * @Description: 通知场景
 */
public class JF_NotificationConfiguration_mxJPO {

    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_NotificationConfiguration_mxJPO.class);

    private static final String STRING_NOTIFICATION_CONFIGURATION_TASK_TYPE = "Notification.Configuration.TaskType";
    private static final String STRING_CHECK_TASK_TYPE = "CHECK_TASK_TYPE";

    private HashMap controlMap = new HashMap();
    protected static boolean isCloud = false;

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


    /**
     * 如果是如下Task类型JF_DATask、JF_APRTask、JF_CustomerTask、JF_SignTask、JF_ECOTask，
     * 在分派和Create的Promote trigger时候不发邮件通知
     * 这几个类型需要在Assign的Promote Action上发通知(把原来的Trigger移植到这个位置)
     * 如果是Task类型，保持和原来一样
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return int
     * @date 2024/8/20 13:34
     * @description
     */
    public int processNotification(Context context, String[] args) {
        try {
            JF_LOGGER.info("args:{}", Arrays.stream(args).toList());
            String strKey = "";
            if (!args[0].isEmpty()) {
                strKey = args[0];
            }
            String strParamsThree = args[2].isEmpty() ? "" : args[2];
            if (UIUtil.isNotNullAndNotEmpty(strParamsThree)) {
                System.out.println("getObjectEnv");
                ((HashMap)notifyMap.get(strKey)).putAll(JF_NotificationUtils_mxJPO.getObjectEnv(context, strParamsThree));
            } else {
                System.out.println("getTriggerEnv");
                ((HashMap)notifyMap.get(strKey)).putAll(JF_NotificationUtils_mxJPO.getTriggerEnv(context));
            }
            controlMap = (HashMap)notifyMap.get(strKey);
            controlMap.put("key", strKey);
            String strParamsTaskId =  (String)controlMap.get("physicalId");
            String strParamsTaskType = (String)controlMap.get("objtype");
            JF_LOGGER.info("strParamsTaskId:{}", strParamsTaskId);
            JF_LOGGER.info("strParamsTaskType:{}", strParamsTaskType);

            String strConfigTaskType = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{STRING_NOTIFICATION_CONFIGURATION_TASK_TYPE});
            JF_LOGGER.info("strConfigTaskType:{}", strConfigTaskType);
            String[] split = strConfigTaskType.split(",");
            String flagTaskType = "Task";
            for (String str: split) {
                if (str.equalsIgnoreCase(strParamsTaskType)) {
                    flagTaskType = "Cust";
                }
            }
            String strParamsFive = args[5].isEmpty() ? "" : args[5];
            String flagSpecial = "N";
            if (STRING_CHECK_TASK_TYPE.equalsIgnoreCase(strParamsFive)) {
                //当为需要校验的时候，就代表当前的trigger是在task的Assign: promote.action.trigger
                //当类型不为JF_DATask、JF_APRTask、JF_CustomerTask、JF_SignTask、JF_ECOTask时不需要发送通知
                if ("Task".equalsIgnoreCase(flagTaskType)) {
                    JF_LOGGER.info("提升工作中,当类型不为JF_DATask、JF_APRTask、JF_CustomerTask、JF_SignTask、JF_ECOTask时不需要发送通知");
                    return 0;
                } else {
                    JF_LOGGER.info("提升工作中,当类型为JF_DATask、JF_APRTask、JF_CustomerTask、JF_SignTask、JF_ECOTask时需要发送通知");
                    args[5] = "";
                    flagSpecial = "Y";
                }
            } else {
                //这种情况当类型不为JF_DATask、JF_APRTask、JF_CustomerTask、JF_SignTask、JF_ECOTask时需要发送通知
                //当类型为JF_DATask、JF_APRTask、JF_CustomerTask、JF_SignTask、JF_ECOTask时不需要发送通知
                if ("Cust".equalsIgnoreCase(flagTaskType)) {
                    JF_LOGGER.info("分派人或者创建,当类型为JF_DATask、JF_APRTask、JF_CustomerTask、JF_SignTask、JF_ECOTask时不需要发送通知");
                    return 0;
                }
                JF_LOGGER.info("分派人或者创建,当类型不为JF_DATask、JF_APRTask、JF_CustomerTask、JF_SignTask、JF_ECOTask时需要发送通知");
            }

            JF_LOGGER.info("发通知：{}", "");
            BufferedWriter var3 = new BufferedWriter(new MatrixWriter(context));
            var3.write("\nExecuting DPM Gen2 Notification\n\n");
            var3.write("   Starting Proces\n");
            var3.flush();
//            NotificationBase.processNotification(context, args);
            JF_NotificationUtils_mxJPO.processNotification(context, args);


            if ("Y".equalsIgnoreCase(flagSpecial)) {
                JF_LOGGER.info("提升工作中发邮件：{}", "");
                JF_LOGGER.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@2");
                String objectId = UIUtil.getValue(controlMap, "objectId");
                DomainObject domainObject = DomainObject.newInstance(context, objectId);
                String sProjectTaskLink = emxNotificationUtil_mxJPO.getObjectLinkHTML(context, UIUtil.getValue(controlMap,"objname"), objectId);

                Map payload = new HashMap();
                payload.put("subject", "emxFramework.Notification.NotifyTaskOwner.ProjectTaskInitiated");
                payload.put("message", "emxFramework.Notification.NotifyTaskOwner.BodyProjectTaskInitiated");
                String[] messageKeys = {"type", "name", "url", "taskName"};
                String[] messageValues = {UIUtil.getValue(controlMap,"objtype"), UIUtil.getValue(controlMap,"objname"), sProjectTaskLink,  UIUtil.getValue(controlMap,"objname")};
                payload.put("messageKeys", messageKeys);
                payload.put("messageValues", messageValues);

                payload.put("tableHeader", "emxFramework.Notification.NotifyTaskOwner.ProjectTaskInitiated.TableHeader");
                payload.put("tableRow",    "emxFramework.Notification.NotifyTaskOwner.ProjectTaskInitiated.TableData");
                String[] tableRowKeys = new String[]{"TaskType", "RouteName", "TaskName", "TaskOwner", "DueDate", "Comments"};
                payload.put("tableRowKeys", tableRowKeys);
                String[] tableRowValues = new String[]{UIUtil.getValue(controlMap,"objtype"), UIUtil.getValue(controlMap,"objname"), UIUtil.getValue(controlMap,"owner"), domainObject.getAttributeValue(context, "Task Estimated Finish Date"), domainObject.getDescription(context)};
                payload.put("tableRowValues", tableRowValues);

                StringList infoList = domainObject.getInfoList(context, "to[Assigned Tasks].from.name");
                if (!infoList.contains(UIUtil.getValue(controlMap,"owner"))) {
                    infoList.add(UIUtil.getValue(controlMap,"owner"));
                }
                payload.put("toList", infoList.toStringArray());
                emxNotificationUtil_mxJPO.objectNotification(context, objectId, "APPRouteTaskDelegatedEvent", payload,"both"); // using APPRouteTaskDelegatedEvent notification object, as we want to mention toList and ccList in payload explicitly
//                emxProgramCentralUtilBase_mxJPO.sendNotification(context, JPO.packArgs(map));
            }
        } catch (Exception var4) {
            System.out.println(var4.getLocalizedMessage());
            var4.printStackTrace();
        }
        return 0;
    }

    /**
    * 发邮件
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2024/9/2 17:19
    * @description
    */
    public void sendNotificationMessages(Context context, String[] args) {
        try {
            JF_LOGGER.info("发通知：{}", "");
            BufferedWriter var3 = new BufferedWriter(new MatrixWriter(context));
            var3.write("\nExecuting DPM Gen2 Notification\n\n");
            var3.write("   Starting Proces\n");
            var3.flush();
//            NotificationBase.processNotification(context, args);
            JF_NotificationUtils_mxJPO.processNotification(context, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static int sendNotificationToUser(Context paramContext, String[] paramArrayOfString) throws Exception {
        if (paramArrayOfString == null || paramArrayOfString.length < 3)
            throw new IllegalArgumentException();
        byte b1 = 0;
        StringTokenizer stringTokenizer = new StringTokenizer(paramArrayOfString[b1++], ",");
        StringList stringList1 = new StringList();
        while (stringTokenizer.hasMoreTokens())
            stringList1.addElement(stringTokenizer.nextToken().trim());
        String str1 = paramArrayOfString[b1++];
        int i = Integer.parseInt(paramArrayOfString[b1++]);
        String[] arrayOfString1 = new String[i];
        String[] arrayOfString2 = new String[i];
        if (paramArrayOfString.length < 3 + i * 2)
            throw new IllegalArgumentException();
        for (byte b2 = 0; b2 < i; b2++) {
            arrayOfString1[b2] = paramArrayOfString[b1++];
            arrayOfString2[b2] = paramArrayOfString[b1++];
        }
        String str2 = paramArrayOfString[b1++];
        i = Integer.parseInt(paramArrayOfString[b1++]);
        String[] arrayOfString3 = new String[i];
        String[] arrayOfString4 = new String[i];
        for (byte b3 = 0; b3 < i; b3++) {
            arrayOfString3[b3] = paramArrayOfString[b1++];
            arrayOfString4[b3] = paramArrayOfString[b1++];
        }
        StringList stringList2 = null;
        if (paramArrayOfString.length > b1) {
            stringTokenizer = new StringTokenizer(paramArrayOfString[b1++], ",");
            stringList2 = new StringList();
            while (stringTokenizer.hasMoreTokens())
                stringList2.addElement(stringTokenizer.nextToken().trim());
        }
        String str3 = null;
        if (paramArrayOfString.length > b1)
            str3 = paramArrayOfString[b1];
//        sendNotification(paramContext, stringList1, null, null, str1, arrayOfString1, arrayOfString2, str2, arrayOfString3, arrayOfString4, stringList2, str3);
        return 0;
    }
}
