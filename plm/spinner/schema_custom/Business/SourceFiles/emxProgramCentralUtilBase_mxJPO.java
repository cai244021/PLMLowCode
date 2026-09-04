/*
**   emxProgramCentralUtilBase
**
**   Copyright (c) 1992-2020 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.1.2.1.2.2 Thu Dec  4 07:55:10 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.1.2.1.2.1 Thu Dec  4 01:53:17 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.1.2.1 Wed Oct 22 15:50:25 2008 przemek Experimental przemek $
*/

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;

import com.dassault_systemes.enovia.dpm.notification.NotificationBase;
import com.dassault_systemes.enovia.e6wv2.foundation.FoundationException;
import com.dassault_systemes.enovia.e6wv2.foundation.FoundationUtil;
import com.dassault_systemes.enovia.e6wv2.foundation.ServiceConstants.HttpMethodType;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxb.Dataobject;
import com.dassault_systemes.enovia.tskv2.ProjectSequence;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Issue;
import com.matrixone.apps.common.MemberRelationship;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.VCDocument;
import com.matrixone.apps.common.util.SubscriptionUtil;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkLicenseUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.Assessment;
import com.matrixone.apps.program.NotificationUtil;
import com.dassault_systemes.enovia.dpm.notification.*;


import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.Risk;
import com.matrixone.apps.program.Task;

import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.IconMail;
import matrix.db.JPO;
import matrix.db.MatrixWriter;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.StringList;
import matrix.util.StringResource;





/**
 * The <code>emxProgramCentralUtil</code> class contains static methods for sending mail.
 *
 * @version AEF 9.5.0.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxProgramCentralUtilBase_mxJPO
{
    /** Holds the base URL for notification messages. */
    protected static String _baseURL = "";

    /** Holds the agent name for notification messages. */
    protected static String _agentName = "";

    /** Holds the languages for notification messages. */
    protected static String _languages = "";

    /** Name of the bundle to use. */
    static final String _bundleName = "emxProgramCentralStringResource";

    /** Cache of the loaded properties. */
    static Map _bundles = new Hashtable();

    /** Directory name to store the migration log file */
    private String logDirectory = "";

    /** string resource constants used for framing email */
    protected static final String SUBJECT_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.Subject";
    protected static final String SUBJECT_STRING_RESOURCE_RISK = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.RiskSubject";
    protected static final String CRITICAL_ASSIGN_MESSAGE_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.CriticalAssignMessage";
    protected static final String ASSIGN_MESSAGE_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.AssignMessage";
    protected static final String REVOKE_SUBJECT_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyNewAssignees.RevokeSubject";
    protected static final String SUBJECT_STRING_COMPLETE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyNewAssignees.CompleteSubject";
    protected static final String ASSIGNED_BY_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.AssignedBy";
    protected static final String REVOKE_MESSAGE__STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyNewAssignees.RevokeMessage";
    protected static final String COLLAB_TASK_REVOKE_MESSAGE__STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyNewAssignees.CollabTaskRevokeMessage";
    protected static final String MESSAGE__COMPLETION_STRING = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyNewAssignees.CompletionMessage";
    protected static final String UNASSIGNED_BY_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.UnAssignedBy";
    protected static final String ASSIGNED_TO_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.AssignedTo";
    protected static final String DIMENSION_STRING_RESOURCE = "emxFramework.Range.Dimension";
    protected static final String TASK_LABEL_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.TaskLabel";
    protected static final String VVTASK_LABEL_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.VVTaskLabel";
    protected static final String TASK_LABEL_STRING_RESOURCE_RISK = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.RiskLabel";
    protected static final String DURATION_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.Duration";
    protected static final String STARTDATE_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.StartDate";
    protected static final String ENDDATE_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.EndDate";
    protected static final String PROJECT_LABEL_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.ProjectLabel";
    protected static final String AFFECTED_ITEM_LABEL_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.AffectedItemLabel";
    protected static final String CONTEXT_LABEL_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.ContextLabel";
    protected static final String DESCRIPTION_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.Description";
    protected static final String DASHBOARDURL_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.3DDashboardURL";
    protected static final String ENOVIAURL_STRING_RESOURCE = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.EnoviaURL";
    protected static final String SUBJECT_STRING_RESOURCE_OPPORTUNITY = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.OpportunitySubject";
    protected static final String TASK_LABEL_STRING_RESOURCE_OPPORTUNITY = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyMembers.OpportunityLabel";
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */
    public emxProgramCentralUtilBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        /*if (!context.isConnected())
            throw new Exception("not supported on desktop client");
        */
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception("must specify method on emxMailUtil invocation");
        }
        return 0;
    }

    /**
     * Set base URL string used in notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - url string
     * @return int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */
    public static int setBaseURL(Context context, String[] args)
        throws Exception
    {
        if (args != null && args.length > 0)
        {
            _baseURL = args[0];
        }
        return 0;
    }

    /**
     * Get base URL string used in notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return String containing url
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */
    public static String getBaseURL(Context context, String[] args)
        throws Exception
    {
        return _baseURL;
    }

    /**
     * Get base URL string used in notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */
    public static int getStreamBaseURL(Context context, String[] args)
        throws Exception
    {
        BufferedWriter writer = new BufferedWriter(new MatrixWriter(context));
        writer.write(_baseURL);
        writer.flush();
        return 0;
    }

    /**
     * Set agent name used in notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - name string
     * @return int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */
    public static int setAgentName(Context context, String[] args)
        throws Exception
    {
        if (args != null && args.length > 0)
        {
            _agentName = args[0];
        }
        return 0;
    }

    /**
     * Get agent name used in notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return String the name of the person to use in "from" field
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */
    public static String getAgentName(Context context, String[] args)
        throws Exception
    {
        return _agentName;
    }

    /**
     * Set languages used in notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - space-delimited list of locals
     * @return int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */
    public static int setLanguages(Context context, String[] args)
        throws Exception
    {
        if (args != null && args.length > 0)
        {
            _languages = args[0];
        }
        return 0;
    }

    /**
     * Get languages used in notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return String a list of space-delimited locales
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */
    public static String getLanguages(Context context, String[] args)
        throws Exception
    {
        return _languages;
    }

    /**
     * Sends an icon mail notification to a single specified user.
     * Appends a url to the message if an objectId is given.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - toUser - the user to notify
     *        1 - subject - the notification subject
     *        2 - message - the notification message
     *        3 - objectId - the id of the object to include in the notification url
     * @return int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     * @deprecated use sendNotificationToUser()
     * @since AEF 9.5.0.0
     */
    @Deprecated
    public static int sendMail(Context context, String[] args)
        throws Exception
    {
        if (args == null || args.length < 4)
        {
            throw (new IllegalArgumentException());
        }
        String toUser = args[0];
        String subject = args[1];
        String message = args[2];
        String objectId = args[3];
        String url = _baseURL;
        StringList toList = new StringList(1);
        toList.addElement(toUser);

        if (objectId != null && url.length() > 0)
        {
            url += "?objectId=";
            url += objectId;
            message += "\n\n";
            message += url;
        }

        sendMessage(context,
                    toList,
                    null,
                    null,
                    subject,
                    message,
                    null);
        return 0;
    }

    /**
     * This method sends an icon mail notification to the specified users.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *        toList - the list of users to notify
     *        ccList - the list of users to cc
     *        bccList - the list of users to bcc
     *        subject - the notification subject
     *        message - the notification message
     *        objectIdList - the ids of objects to send with the notification
     * @return int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */
    public static int sendMessage(Context context, String[] args)
        throws Exception
    {
        if (args == null || args.length < 1)
        {
            throw (new IllegalArgumentException());
        }
        Map map = (Map) JPO.unpackArgs(args);

        sendMessage(context,
                    (StringList) map.get("toList"),
                    (StringList) map.get("ccList"),
                    (StringList) map.get("bccList"),
                    (String) map.get("subject"),
                    (String) map.get("message"),
                    (StringList) map.get("objectIdList"));

        return 0;
    }

    /**
     * Sends an icon mail notification to the specified users.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *        toList - the list of users to notify
     *        ccList - the list of users to cc
     *        bccList - the list of users to bcc
     *        subjectKey - the notification subject key
     *        subjectKeys - an array of subject place holder keys
     *        subjectValues - an array of subject place holder values
     *        messageKey - the notification message key
     *        messageKeys - an array of message place holder keys
     *        messageValues - an array of message place holder values
     *        objectIdList - the ids of objects to send with the notification
     *        companyName - used for company-based messages
     * @return int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */
    public static int sendNotification(Context context, String[] args)
        throws Exception
    {
        if (args == null || args.length < 1)
        {
            throw (new IllegalArgumentException());
        }
        Map map = (Map) JPO.unpackArgs(args);

        sendNotification(context,
                    (StringList) map.get("toList"),
                    (StringList) map.get("ccList"),
                    (StringList) map.get("bccList"),
                    (String) map.get("subjectKey"),
                    (String[]) map.get("subjectKeys"),
                    (String[]) map.get("subjectValues"),
                    (String) map.get("messageKey"),
                    (String[]) map.get("messageKeys"),
                    (String[]) map.get("messageValues"),
                    (StringList) map.get("objectIdList"),
                    (String) map.get("companyName"));

        return 0;
    }

    /**
     * Sends an icon mail notification to a single specified user.
     * Appends a url to the message if an objectId is given.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        userList - a comma separated list of users to notify
     *        subjectKey - the notification subject key
     *        subjectSubCount - the number of key/value pairs for subject substitution
     *        subjectKey1 - the first subject key
     *        subjectValue1 - the first subject value
     *        messageKey - the notification message key
     *        messageSubCount - the number of key/value pairs for message substitution
     *        messageKey1 - the first message key
     *        messageValue1 - the first message value
     *        objectIdList - a comma separated list of objectids to include in the notification url
     *        companyName - used for company-based messages
     * @return int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */
    public static int sendNotificationToUser(Context context, String[] args)
        throws Exception
    {
        if (args == null || args.length < 3)
        {
            throw (new IllegalArgumentException());
        }
        int index = 0;
        StringTokenizer tokens = new StringTokenizer(args[index++], ",");
        StringList toList = new StringList();
        while (tokens.hasMoreTokens())
        {
            toList.addElement(tokens.nextToken().trim());
        }

        String subjectKey = args[index++];
        int subCount = Integer.parseInt(args[index++]);
        String[] subjectKeys = new String[subCount];
        String[] subjectValues = new String[subCount];
        if (args.length < 3+(subCount*2))
        {
            throw (new IllegalArgumentException());
        }
        for (int i=0; i < subCount ;i++)
        {
            subjectKeys[i] = args[index++];
            subjectValues[i] = args[index++];
        }

        String messageKey = args[index++];
        subCount = Integer.parseInt(args[index++]);
        String[] messageKeys = new String[subCount];
        String[] messageValues = new String[subCount];
        for (int i=0; i < subCount ;i++)
        {
            messageKeys[i] = args[index++];
            messageValues[i] = args[index++];
        }

        StringList objectIdList = null;
        if (args.length > index)
        {
            tokens = new StringTokenizer(args[index++], ",");
            objectIdList = new StringList();
            while (tokens.hasMoreTokens())
            {
                objectIdList.addElement(tokens.nextToken().trim());
            }
        }

        String companyName = null;
        if (args.length > index)
        {
            companyName = args[index];
        }

        sendNotification(context,
                         toList,
                         null,
                         null,
                         subjectKey,
                         subjectKeys,
                         subjectValues,
                         messageKey,
                         messageKeys,
                         messageValues,
                         objectIdList,
                         companyName);
        return 0;
    }

    /**
     * Returns a processed and translated message for a given key.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        messageKey - the message key
     *        messageSubCount - the number of key/value pairs for message substitution
     *        messageKey1 - the first message key
     *        messageValue1 - the first message value
     *        companyName - used for company-based messages
     * @return String the message
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */
    public static String getMessage(Context context, String[] args)
        throws Exception
    {
        if (args == null || args.length < 2)
        {
            throw (new IllegalArgumentException());
        }
        int index = 0;
        String messageKey = args[index++];
        int subCount = Integer.parseInt(args[index++]);
        String[] messageKeys = new String[subCount];
        String[] messageValues = new String[subCount];
        if (args.length < 2+(subCount*2))
        {
            throw (new IllegalArgumentException());
        }
        for (int i=0; i < subCount ;i++)
        {
            messageKeys[i] = args[index++];
            messageValues[i] = args[index++];
        }
        String companyName = null;
        if (args.length > index)
        {
            companyName = args[index];
        }

        String message = getMessage(context,
                         messageKey,
                         messageKeys,
                         messageValues,
                         companyName);

        BufferedWriter writer = new BufferedWriter(new MatrixWriter(context));
        writer.write(message);
        writer.flush();
        return message;
    }

    /**
     * Returns a processed and translated message for a given key.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param messageKey the notification message key
     * @param messageKeys an array of message place holder keys
     * @param messageValues an array of message place holder values
     * @param companyName used for company-based messages
     * @param locale: user locale
     * @return String the message
     * @throws Exception if the operation fails
     */
    protected static String getMessage(Context context,
                                     String messageKey,
                                     String[] messageKeys,
                                     String[] messageValues,
                                     String companyName, Locale locale)
        throws Exception
    {

        // Define the message from the given key.
        String messageValue = getString(
                messageKey,
                companyName,
                locale);
        String message = messageValue;

        // Substitute in values for any placeholders.
        if (messageKeys != null && messageKeys.length > 0 && messageValue != null)
        {
            message = StringResource.format(messageValue, messageKeys, messageValues);
        }
        return message;
    }
    /**
     * Returns a processed and translated message for a given key.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param messageKey the notification message key
     * @param messageKeys an array of message place holder keys
     * @param messageValues an array of message place holder values
     * @param companyName used for company-based messages
     * @return String the message
     * @throws Exception if the operation fails
     */
    public static String getMessage(Context context,
                                     String messageKey,
                                     String[] messageKeys,
                                     String[] messageValues,
                                     String companyName)
        throws Exception
    {
        return getMessage(context, messageKey, messageKeys, messageValues, companyName, getLocale(context));
    }

    /**
     * Sends an icon mail notification to the specified users.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param toList the list of users to notify
     * @param ccList the list of users to cc
     * @param bccList the list of users to bcc
     * @param subject the notification subject
     * @param message the notification message
     * @param objectIdList the ids of objects to send with the notification
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */
    protected static void sendMessage(Context context,
                                    StringList toList,
                                    StringList ccList,
                                    StringList bccList,
                                    String subject,
                                    String message,
                                    StringList objectIdList)
        throws Exception
    {
        // If there is no subject, then return without sending the notification.
        if (subject == null || "".equals(subject))
        {
            return;
        }

        // If the base URL and object id list are available,
        // then add urls to the end of the message.
        if ( (_baseURL != null && ! "".equals(_baseURL)) &&
             (objectIdList != null && objectIdList.size() != 0) )
        {
            // Prepare the message for adding urls.
            message += "\n";

            Iterator i = objectIdList.iterator();
            while (i.hasNext())
            {
                // Add the url to the end of the message.
                message += "\n" + _baseURL + "?objectId=" + (String) i.next();
            }
        }

        // Send the mail message.
        sendMail(context,
                 toList,
                 ccList,
                 bccList,
                 subject,
                 message,
                 objectIdList);
    }

    /**
     * Sends an icon mail notification to the specified users.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param toList the list of users to notify
     * @param ccList the list of users to cc
     * @param bccList the list of users to bcc
     * @param subjectKey the notification subject key
     * @param subjectKeys an array of subject place holder keys
     * @param subjectValues an array of subject place holder values
     * @param messageKey the notification message key
     * @param messageKeys an array of message place holder keys
     * @param messageValues an array of message place holder values
     * @param objectIdList the ids of objects to send with the notification
     * @param companyName used for company-based messages
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */
    protected static void sendNotification(Context context,
                                    StringList toList,
                                    StringList ccList,
                                    StringList bccList,
                                    String subjectKey,
                                    String[] subjectKeys,
                                    String[] subjectValues,
                                    String messageKey,
                                    String[] messageKeys,
                                    String[] messageValues,
                                    StringList objectIdList,
                                    String companyName)
        throws Exception
    {
        // Define the mail subject.
        String subject = getMessage(context,
                         subjectKey,
                         subjectKeys,
                         subjectValues,
                         companyName);

        // Define the mail message.
        String message = getMessage(context,
                         messageKey,
                         messageKeys,
                         messageValues,
                         companyName);

        // If the base URL and object id list are available,
        // then add urls to the end of the message.
        if ( (_baseURL != null && ! "".equals(_baseURL)) &&
             (objectIdList != null && objectIdList.size() != 0) )
        {
            // Prepare the message for adding urls.
            message += "\n";

            Iterator i = objectIdList.iterator();
            while (i.hasNext())
            {
                // Add the url to the end of the message.
                message += "\n" + _baseURL + "?objectId=" + (String) i.next();
            }
        }

        // Send the mail message.
        sendMail(context,
                 toList,
                 ccList,
                 bccList,
                 subject,
                 message,
                 objectIdList);
    }

    /**
     * Sends an icon mail notification to the specified users.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param toList the list of users to notify
     * @param ccList the list of users to cc
     * @param bccList the list of users to bcc
     * @param subject the notification subject
     * @param message the notification message
     * @param objectIdList the ids of objects to send with the notification
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */
    protected static void sendMail(Context context,
                                 StringList toList,
                                 StringList ccList,
                                 StringList bccList,
                                 String subject,
                                 String message,
                                 StringList objectIdList)
        throws Exception
    {
        // Create iconmail object.
        IconMail mail = new IconMail();
        mail.create(context);

        // Set the "to" list.
        mail.setToList(toList);

        // Set the "cc" list.
        if (ccList != null)
        {
            mail.setCcList(ccList);
        }

        // Set the "bcc" list.
        if (bccList != null)
        {
            mail.setBccList(bccList);
        }

        // Set the object list.  If the object id list is available,
        // then send the objects along with the notification.
        if (objectIdList != null && objectIdList.size() != 0)
        {
            BusinessObjectList bol =
                    new BusinessObjectList(objectIdList.size());

            Iterator i = objectIdList.iterator();
            while (i.hasNext())
            {
                String id = (String) i.next();
                BusinessObject bo = new BusinessObject(id);
                bo.open(context);
                bol.addElement(bo);
            }

            mail.setObjects(bol);
        }

        // Set the message.
        mail.setMessage(message);

        boolean isContextPushed = false;
        emxContextUtil_mxJPO utilityClass = new emxContextUtil_mxJPO(context, null);
        // Test if spoofing should be performed on the "from" field.
        if (_agentName != null && ! "".equals(_agentName))
        {
            try
            {
                // Push Notification Agent
                String[] pushArgs = {_agentName};
                utilityClass.pushContext(context, pushArgs);
                isContextPushed = true;
            }
            catch (Exception ex)
            {
            }
        }

        // Set the subject and send the iconmail.
        mail.send(context, subject);

        if (isContextPushed == true)
        {
            // Pop Notification Agent
            utilityClass.popContext(context, null);
        }
    }

    /**
     * Get a string for the specified key and company.
     *
     * @param key the key to use in the search
     * @param companyName the company to get the key for
     * @param locale used to identify the bundle to use
     * @return String matching the key
     * @since AEF 9.5.1.0
     */
    static public String getString(String key, String companyName, Locale locale)
    {

        /*// Get the appropriate resource bundle based on the locale.
        ResourceBundle bundle = (locale == null) ?
                                    (ResourceBundle) _bundles.get("default") :
                                    (ResourceBundle) _bundles.get(locale);

        // Create the bundle if necessary and store it in the bundle map.
        if (bundle == null)
        {
            //bundle = PropertyResourceBundle.getBundle(_bundleName, locale);
            bundle = ResourceBundle.getBundle(_bundleName, locale);

            if (locale == null)
            {
                _bundles.put("default", bundle);
            }
            else
            {
                _bundles.put(locale, bundle);
            }
        }

        // Get the string value from the bundle using the key.
        String value = null;
        try
        {
            if (companyName == null || companyName.length() == 0)
            {
                value = bundle.getString(key);
            }
            else
            {
                try
                {
                    value = bundle.getString(key + "." + companyName.replace(' ', '_'));
                }
                catch (Exception e)
                {
                    value = bundle.getString(key);
                }
            }
        }
        catch (Exception e)
        {
            value = key;
        }

        return value;*/
      String value = ProgramCentralConstants.EMPTY_STRING;
        try {
            value = EnoviaResourceBundle.getProperty(null, "emxProgramCentralStringResource",locale, key);
    } catch (Exception e) {
        value = key;
    }

        return value;
    }


    /**
     * Get locale for given context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @return the locale object
     * @since AEF 9.5.1.1
     */
    public static Locale getLocale(Context context)
    {
        // this is the country
        String strContry = "";
        // this is the string id of the localize tag, if there is one
        String strLanguage = "";

        // Get locale
        try
        {
            String result = context.getSession().getLanguage();

            StringTokenizer st1 = new StringTokenizer(result, "\n");
            String locale = st1.nextToken();

            int idxDash = locale.indexOf('-');
            int idxComma = locale.indexOf(',');
            int idxSemiColumn = locale.indexOf(';');
            if ((idxComma == -1) && (idxSemiColumn == -1) && (idxDash == -1))
            {
                strLanguage = locale;
            }
            else if (idxDash != -1)
            {
                boolean cont = true;
                if ((idxComma < idxDash) && (idxComma != -1))
                {
                    if ((idxSemiColumn == -1) || (idxComma < idxSemiColumn))
                    {
                        strLanguage = locale.substring(0, idxComma);
                        cont = false;
                    }
                }
                if ((cont) && (idxSemiColumn < idxDash) && (idxSemiColumn != -1) )
                {
                    if ((idxComma == -1) || (idxComma > idxSemiColumn))
                    {
                        strLanguage = locale.substring(0, idxSemiColumn);
                        cont = false;
                    }
                }
                else if (cont)
                {
                    boolean sec2 = true;
                    StringTokenizer st = new StringTokenizer(locale, "-");
                    if (st.hasMoreTokens()) {
                        strLanguage = st.nextToken();
                        if (st.hasMoreTokens()) {
                            strContry = st.nextToken();
                        } else {
                            sec2 = false;
                        }
                    } else {
                        sec2 = false;
                    }
                    int idx = strContry.indexOf(',');
                    if (idx != -1)
                    {
                        strContry = strContry.substring(0,idx);
                    }
                    idx = strContry.indexOf(';');
                    if (idx != -1)
                    {
                        strContry = strContry.substring(0,idx);
                    }
                    //if (!sec2) {
                        //System.out.println("MATRIX ERROR - LOCAL INFO CONTAINS WRONG DATA");
                    //}
                }
            }
            else
            {
                if ((idxComma != -1) && ((idxComma < idxSemiColumn) || (idxSemiColumn == -1)))
                {
                    strLanguage = locale.substring(0, idxComma);
                }
                else
                {
                    strLanguage = locale.substring(0, idxSemiColumn);
                }
            }
        }
        catch (Exception e)
        {
            strLanguage = "en";
            strContry = "US";
        }

        // Get Resource bundle.
        Locale loc = new Locale(strLanguage, strContry);
        return loc;
    }

    /**
     * Conversion routine for dashboards from pre 10.5 PMC
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String Log Directory
     * @throws Exception if the operation fails.
     */
    public void migrateDashboards(Context context, String[] args)
        throws Exception
    {
        if (args.length == 0 )
        {
            throw new IllegalArgumentException();
        }

        logDirectory = args[0];

        try
        {
            ContextUtil.pushContext(context);
            ArrayList convertedDashboardList = new ArrayList();

            StringList objectSelects = new StringList(1);
            objectSelects.add(DomainObject.SELECT_NAME);

            //Get the list of Person objects in the database
            MapList mapList = DomainObject.findObjects(context, DomainObject.TYPE_PERSON, null, null, objectSelects);

            Iterator iterator = mapList.iterator();
            Map map = new HashMap();
            String dashBoardName = "";
            String userName = "";
            String cmd = "";
            String result = "";
            while(iterator.hasNext())
            {
                map = (Map)iterator.next();
                userName = (String)map.get(DomainObject.SELECT_NAME);
                boolean hasAdminObject    = true;

                try
                {

                    matrix.db.Person adminPerson = new matrix.db.Person(userName);
                    //Opening the context will throw error in case Admin Object does not exist
                    adminPerson.open(context);
                    adminPerson.close(context);
                }
                catch(Exception e)
                {
                    hasAdminObject = false;
                    convertedDashboardList.add("Conversion failed : " + userName + "'s Adminstration Object does not exist.");
                }

                if(hasAdminObject)
                {
                    //Get the list of Sets for this user
                    //PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:start
                    String sCommandStatement = "list Set user $1";
                    result =  MqlUtil.mqlCommand(context, sCommandStatement,userName);
                    //PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:End

                    StringTokenizer st = new StringTokenizer(result, "\n");

                    //PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:start
                    sCommandStatement = "set workspace user $1";
                    result =  MqlUtil.mqlCommand(context, sCommandStatement,userName);
                    //PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:End

                    //Prefix Sets beginning with "dashboard-" with a "."
                    while(st.hasMoreTokens())
                    {
                        dashBoardName = st.nextToken();
                        if(dashBoardName.startsWith("dashboard-"))
                        {
                            try
                            {
                              //PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:start
                                sCommandStatement = "copy set $1";
                                String sParam = dashBoardName + "."+ dashBoardName;
                                MqlUtil.mqlCommand(context, sCommandStatement,sParam);
                             //PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:End

                              //PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:start
                                sCommandStatement = "delete set $1";
                                MqlUtil.mqlCommand(context, sCommandStatement,dashBoardName);
                             //PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:End

                                convertedDashboardList.add("Conversion passed : " + userName + " : " + dashBoardName );
                            }
                            catch(Exception e)
                            {
                                convertedDashboardList.add("Conversion failed : " + userName + " : Could can not copy Dashboard \"" + dashBoardName + "\" as hidden, a set with the same name might exists, Or not able to delete the original dashboard after conversion.");
                            }
                        }
                    }
                 }
            }

            // check for all the Admin persons
            result = MqlUtil.mqlCommand(context, "list person"); //PRG:RG6:R213:Mql Injection:Static Mql:18-Oct-2011
            StringTokenizer stUser = new StringTokenizer(result, "\n");
            while(stUser.hasMoreTokens())
            {
                userName = stUser.nextToken();
                try
                {
                    //Creating person bean itself will throw error if Business Object does not exist
                    Person persObj = Person.getPerson(context, userName);
                }
                catch(Exception e)
                {
                    convertedDashboardList.add("Conversion failed : " + userName + "'s Business Object does not exist.");
                }
            }
            writeFile(convertedDashboardList);
        }
        catch(Exception e)
        {
          throw new FrameworkException(e);
        }
        finally
        {
          ContextUtil.popContext(context);
        }
    }


    /**
     * Writes the dashboard names which have been renamed to a log file
     * @param dashBoardNames the list of dashboards in old format
     * @throws Exception if the operation fails.
     */

    protected void writeFile(ArrayList dashBoardNames)
        throws Exception
    {
        java.io.File file = new java.io.File(logDirectory + "convertedDashboardList.log");
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
        for (int i=0; i < dashBoardNames.size(); i++)
        {
            fileWriter.write((String)dashBoardNames.get(i));
            fileWriter.newLine();
        }
        fileWriter.close();
    }

    /**
     * This method is used to Exclude the person having role Project User & External Project User
     * @param context the eMatrix <code>Context</code> object
     * @param args holds follwing arguments
     *            objectId- Object id of the document
     * @return StringList
     * @throws Exception if the operation fails
     */

    public StringList getExcludeOIDForTaskAssignee(Context context, String args[]) throws Exception
    {

        //get All Users
        MapList mapUserList = null;
        StringList select = new StringList();
        select.add(DomainConstants.SELECT_ID);
        select.add(DomainConstants.SELECT_NAME);
        mapUserList =DomainObject.findObjects(context,PropertyUtil.getSchemaProperty(context,"type_Person"),null,null,select);

        HashMap programMap        = (HashMap) JPO.unpackArgs(args);
        // Modified:12-May-09:nzf:R207:PRG:Bug:375205
        // Added additional check and logic for Risk aswell
        // Added:7-May-09:nzf:R207:PRG:Bug:372833
        HashMap requestMap        = (HashMap)programMap.get("RequestValuesMap");
        String  objectId          = (String) programMap.get("objectId");

        DomainObject dmoTask = DomainObject.newInstance(context,objectId);

        boolean isKindOfTask = dmoTask.isKindOf(context,DomainConstants.TYPE_TASK_MANAGEMENT);

        StringList slProjMembers = new StringList();
        StringList slProjMembersNames = new StringList();
        String strProjectId = "";
        if(isKindOfTask){
            strProjectId = Task.getParentProject(context,dmoTask);
        }else{
            StringList slBusSelects = new StringList(2);
            slBusSelects.add(DomainConstants.SELECT_ID);

            MapList mlProjectList = dmoTask.getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_RISK,
                    DomainConstants.TYPE_PROJECT_MANAGEMENT,
                    slBusSelects,
                    null,       // relationshipSelects
                    true,      // getTo
                    false,       // getFrom
                    (short) 1,  // recurseToLevel
                    null,       // objectWhere
                    null);      // relationshipWhere


            Map mpProjectInfo = null;
            for (Iterator itrFolderStructure = mlProjectList.iterator(); itrFolderStructure.hasNext();) {

                mpProjectInfo = (Map)itrFolderStructure.next();
                strProjectId = (String)mpProjectInfo.get(DomainConstants.SELECT_ID);

            }
            //Added:22-Mar-10:S3L:R2011:PRG:Bug:041299
            dmoTask = DomainObject.newInstance(context,strProjectId);
            isKindOfTask=dmoTask.isKindOf(context,DomainConstants.TYPE_TASK_MANAGEMENT);
            if(isKindOfTask)
            {
                 strProjectId = Task.getParentProject(context,dmoTask);
            }
            // End:R2011:PRG:Bug:041299
        }

        ProjectSpace psProject = new ProjectSpace();
        psProject.newInstance(context);
        psProject.setId(strProjectId);

        //User Object selectables
        StringList objectSelects = new StringList(1);
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);

        MapList mapProjectMembers = psProject.getMembers(
                context,        //Context
                objectSelects,  //Person selectables
                null,           //MemberRelationship selectable
                null,           // business object where clause
                null);          // relationship where clause

        Iterator itr1 = mapProjectMembers.iterator();

        while (itr1.hasNext()){
            Map map1 = (Map) itr1.next();
            slProjMembers.add((String) map1.get(DomainConstants.SELECT_ID));
            slProjMembersNames.add((String) map1.get(DomainConstants.SELECT_NAME));
        }
        // End:R207:PRG:Bug:372833

        StringList  slAllUserIds = new StringList();
        StringList  slAllUserNames = new StringList();

        for(int i=0;i<mapUserList.size();i++){

            Map mapUser = (Map) mapUserList.get(i);

            String strUserId = (String) mapUser.get(DomainConstants.SELECT_ID);
            String strUserName= (String) mapUser.get(DomainConstants.SELECT_NAME);

            slAllUserIds.add(strUserId);
            slAllUserNames.add(strUserName);

        }

        // Added:7-May-09:nzf:R207:PRG:Bug:372833
        String strProjectMemberId = "";
        for(int x=0;x<slProjMembers.size();x++){
            strProjectMemberId = (String)slProjMembers.get(x);
            if(slAllUserIds.contains(strProjectMemberId)){
                slAllUserIds.remove(strProjectMemberId);
            }
        }// End:R207:PRG:Bug:372833

        // End:R207:PRG:Bug:375205

        return slAllUserIds;
    }
// End:V6R2010:PRG Autonomy search

    /**
     * This method is executed to check Access Node visibility.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds follwing arguments
     *            objectId- Object id of the document
     *
     * @return boolean
     * @throws Exception if the operation fails
     * @since PC10.5.SP1
     */

    public boolean checkAccessNode(Context context, String[] args) throws Exception
    {
        boolean retVal=false;
        try
        {
            HashMap programMap        = (HashMap) JPO.unpackArgs(args);
            String  objectId          = (String) programMap.get("objectId");

            DomainObject domObject = DomainObject.newInstance(context, objectId);

            StringList busSelects = new StringList(1);
            busSelects.add(DomainConstants.SELECT_ID);

            Pattern typePattern = new Pattern (DomainConstants.TYPE_BUSINESS_GOAL);
            typePattern.addPattern(DomainConstants.TYPE_WORKSPACE_VAULT);
            typePattern.addPattern(DomainConstants.TYPE_TASK_MANAGEMENT);
            typePattern.addPattern(DomainConstants.TYPE_RISK);
            typePattern.addPattern(DomainConstants.TYPE_QUALITY);
            typePattern.addPattern(DomainConstants.TYPE_FINANCIAL_ITEM);
            typePattern.addPattern(Assessment.TYPE_ASSESSMENT);
            typePattern.addPattern(DomainConstants.TYPE_ROUTE);

            MapList objectList = domObject.getRelatedObjects(context,
                                  DomainConstants.QUERY_WILDCARD,
                                  typePattern.getPattern(),
                                  busSelects,
                                  null,
                                  true,
                                  true,
                                  (short) 1,
                                  null,
                                  null);

            if(objectList.size() > 0)
           {
               retVal = true;
           }

        } catch (Exception ex) {
//            ex.printStackTrace();   
            DebugUtil.debug("checkAccessNode "+ ex.getMessage());
            throw ex;
        }
        finally
        {
            return retVal;
        }
    }

//  Added:R207:PRG Integration of Common Meetings and Decisions

    /**
     * This Method is used to get Decision object from meeting of a selected object.
     * This method is used for table "PMCDecisionsRelatedDecisions"
     *
     * @param context The Matrix Context object
     * @param args Packed program and request maps for the table
     * @return MapList containing all table data
     * @throws MatrixException if operation fails
     *
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTablePMCDecisionsRelatedDecisionsData(Context context, String[] args) throws MatrixException
    {
        try {
            final String SELECT_RELATED_OBJECT_ID = "from["+DomainConstants.RELATIONSHIP_DECISION+"].to.id";
            final String SELECT_RELATED_OBJECT_NAME = "from["+DomainConstants.RELATIONSHIP_DECISION+"].to.name";
            final String SELECT_RELATED_OBJECT_TYPE = "from["+DomainConstants.RELATIONSHIP_DECISION+"].to.type";

            Map programMap      = (Map) JPO.unpackArgs(args);
            String  strParentId = (String) programMap.get("objectId");

            DomainObject dmoObject = DomainObject.newInstance(context, strParentId);

            String strRelationshipPattern = DomainConstants.RELATIONSHIP_MEETING_CONTEXT+","+DomainConstants.RELATIONSHIP_DECISION;
            String strTypePattern = DomainConstants.TYPE_MEETING+","+DomainConstants.TYPE_DECISION;

            StringList slBusSelect = new StringList();
            slBusSelect.add(DomainConstants.SELECT_ID);
            slBusSelect.add(DomainConstants.SELECT_TYPE);
            slBusSelect.add(SELECT_RELATED_OBJECT_ID);
            slBusSelect.add(SELECT_RELATED_OBJECT_NAME);
            slBusSelect.add(SELECT_RELATED_OBJECT_TYPE);

            StringList slRelSelect = new StringList();

            boolean getTo = true;
            boolean getFrom = true;
            short recurseToLevel = 2;
            String strBusWhere = "";
            String strRelWhere = "";

            MapList mlRelatedObjects = dmoObject.getRelatedObjects(context,
                    strRelationshipPattern, //pattern to match relationships
                    strTypePattern, //pattern to match types
                    slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    getTo, //get To relationships
                    getFrom, //get From relationships
                    recurseToLevel, //the number of levels to expand, 0 equals expand all.
                    strBusWhere, //where clause to apply to objects, can be empty ""
                    strRelWhere); //where clause to apply to relationship, can be empty ""

            Map mapRelatedObjectInfo = null;
            String strType = "";
            String strDecisionId = "";
            Object objRelatedParentId = null;
            StringList slUniqueDecisionList = new StringList();
            MapList mlDecisionObjectList = new MapList();

            for (Iterator itrRelatedObjects = mlRelatedObjects.iterator(); itrRelatedObjects.hasNext();)
            {
                mapRelatedObjectInfo = (Map) itrRelatedObjects.next();
                strType = (String)mapRelatedObjectInfo.get(DomainConstants.SELECT_TYPE);
                //if it is Meeting then remove from the list
                if(DomainConstants.TYPE_MEETING.equals(strType))
                {
                    continue;
                }

                //if decision directly attached to parent then remove from the list
                objRelatedParentId = mapRelatedObjectInfo.get(SELECT_RELATED_OBJECT_ID);
                if (objRelatedParentId == null || strParentId.equals(objRelatedParentId))
                {
                    continue;
                }

                //if decision is already existing then remove from the list
                strDecisionId = (String)mapRelatedObjectInfo.get(DomainConstants.SELECT_ID);
                if (slUniqueDecisionList.contains(strDecisionId))
                {
                    continue;
                }
                slUniqueDecisionList.add(strDecisionId);

                mlDecisionObjectList.add(mapRelatedObjectInfo);
            }
            return mlDecisionObjectList;
        }
        catch (FrameworkException fwe)
        {
//            fwe.printStackTrace();   
            DebugUtil.debug("getTablePMCDecisionsRelatedDecisionsData "+ fwe.getMessage());
            throw new MatrixException(fwe);
        }
        catch (Exception e)
        {
//            e.printStackTrace();   
            DebugUtil.debug("getTablePMCDecisionsRelatedDecisionsData 2"+ e.getMessage());
            throw new MatrixException(e);
        }
    }

    /**
     * This method is used to show the parent object column data in Decision related object table
     * This is used for column "ParentObjects" in table "PMCDecisionsRelatedDecisions"
     *
     * @param context The Matrix Context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws MatrixException if operation fails
     */
    public Vector getColumnParentObjectData (Context context, String[] args) throws MatrixException
    {
        try
        {
            final String SELECT_RELATED_OBJECT_ID   = "from["+DomainConstants.RELATIONSHIP_DECISION+"].to.id";
            final String SELECT_RELATED_OBJECT_NAME = "from["+DomainConstants.RELATIONSHIP_DECISION+"].to.name";
            final String SELECT_RELATED_OBJECT_TYPE = "from["+DomainConstants.RELATIONSHIP_DECISION+"].to.type";

            Map programMap = (Map) JPO.unpackArgs(args);
            MapList mlObjectList = (MapList) programMap.get("objectList");

            Vector vecColumnValues = new Vector(mlObjectList.size());

            Object objRelatedObjectIds = null;
            Object objRelatedObjectName = null;
            Object objRelatedObjectType = null;
            String strRelatedObjectId = "";
            String strRelatedObjectName = "";
            String strRelatedObjectType = "";
            StringList slRelatedObjectIdList = null;
            StringList slRelatedObjectNameList = null;
            StringList slRelatedObjectTypeList = null;
            StringList slHTMLAchors = null;
            String strHTMLAnchor = "";
            String strTypeSymName = "";
            String strCommonDirectory = EnoviaResourceBundle.getProperty(context, "eServiceSuiteFramework.CommonDirectory");
            String strTypeIcon = "";

            int nTotalRelatedObjects = 0;

            final String HTML_TEMPLATE = "<a href='javascript:showModalDialog(\"../common/emxTree.jsp?objectId=${OBJECTID}\", \"875\", \"550\", \"false\", \"popup\")' title=\"${NAME}\"><img src=\"../" + strCommonDirectory + "/images/${IMAGE}\" border=\"0\"/>${NAME}</a>";

            Map mapObjectInfo = null;
            for (Iterator itrObjectList = mlObjectList.iterator();itrObjectList.hasNext();)
            {
                mapObjectInfo = (Map) itrObjectList.next();

                slRelatedObjectIdList = new StringList();

                objRelatedObjectIds = mapObjectInfo.get(SELECT_RELATED_OBJECT_ID);
                objRelatedObjectName = mapObjectInfo.get(SELECT_RELATED_OBJECT_NAME);
                objRelatedObjectType = mapObjectInfo.get(SELECT_RELATED_OBJECT_TYPE);

                if (objRelatedObjectIds instanceof String)
                {
                    strRelatedObjectId = (String)objRelatedObjectIds;
                    strRelatedObjectName = (String)objRelatedObjectName;
                    strRelatedObjectType = (String)objRelatedObjectType;

                    // Find type icon
                    strTypeSymName = FrameworkUtil.getAliasForAdmin(context, "Type", strRelatedObjectType, true);
                    try{
                        strTypeIcon = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon." + strTypeSymName);
                    }catch(Exception e){
                        strTypeIcon  = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon.defaultType");
                    }

                    strHTMLAnchor = FrameworkUtil.findAndReplace(HTML_TEMPLATE, "${OBJECTID}", strRelatedObjectId);
                    strHTMLAnchor = FrameworkUtil.findAndReplace(strHTMLAnchor, "${NAME}", strRelatedObjectName);
                    strHTMLAnchor = FrameworkUtil.findAndReplace(strHTMLAnchor, "${IMAGE}", strTypeIcon);

                    vecColumnValues.add(strHTMLAnchor);
                }
                else if (objRelatedObjectIds instanceof StringList)
                {
                    slRelatedObjectIdList = (StringList)objRelatedObjectIds;
                    slRelatedObjectNameList = (StringList)objRelatedObjectName;
                    slRelatedObjectTypeList = (StringList)objRelatedObjectType;

                    nTotalRelatedObjects = slRelatedObjectIdList.size();
                    slHTMLAchors = new StringList(nTotalRelatedObjects);
                    for (int i = 0; i < nTotalRelatedObjects; i++) {
                        strRelatedObjectId = (String)slRelatedObjectIdList.get(i);
                        strRelatedObjectName =XSSUtil.encodeForHTML(context, (String)slRelatedObjectNameList.get(i));
                        strRelatedObjectType = (String)slRelatedObjectTypeList.get(i);

                        // Find type icon
                        strTypeSymName = FrameworkUtil.getAliasForAdmin(context, "Type", strRelatedObjectType, true);
                        try{
                            strTypeIcon = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon." + strTypeSymName);
                        }catch(Exception e){
                            strTypeIcon  = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon.defaultType");
                        }

                        strHTMLAnchor = FrameworkUtil.findAndReplace(HTML_TEMPLATE, "${OBJECTID}", strRelatedObjectId);
                        strHTMLAnchor = FrameworkUtil.findAndReplace(strHTMLAnchor, "${NAME}", strRelatedObjectName);
                        strHTMLAnchor = FrameworkUtil.findAndReplace(strHTMLAnchor, "${IMAGE}", strTypeIcon);

                        slHTMLAchors.add(strHTMLAnchor);
                    }

                    vecColumnValues.add(FrameworkUtil.join(slHTMLAchors, ","));
                }
                else {
                    vecColumnValues.add(DomainObject.EMPTY_STRING);
                }
            }
            return vecColumnValues;
        }
        catch (FrameworkException fwe)
        {
//            fwe.printStackTrace();   
            DebugUtil.debug("getColumnParentObjectData "+ fwe.getMessage());
            throw new MatrixException(fwe);
        }
        catch (Exception e)
        {
//            e.printStackTrace();   
            DebugUtil.debug("getColumnParentObjectData 2"+ e.getMessage());
            throw new MatrixException(e);
        }
    }

//  End:R207:PRG Integration of Common Meetings and Decisions
  // start: Added for Task calender feature
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getExcludeOIDForCalendar(Context context, String []args)throws Exception {
        //get All Calender
        MapList mapCalendarList = null;
        StringList select = new StringList();
        select.add(DomainConstants.SELECT_ID);
        select.add(DomainConstants.SELECT_NAME);
        mapCalendarList =DomainObject.findObjects(context,PropertyUtil.getSchemaProperty(context,"type_WorkCalendar"),null,null,select);
        StringList slExcludeId = new StringList();
        Iterator itrCalender = mapCalendarList.iterator();
        while(itrCalender.hasNext()){
            Map mapCalendar = (Map)itrCalender.next();
            Object oCalendarId = mapCalendar.get(DomainConstants.SELECT_ID);
            slExcludeId.add(String.valueOf(oCalendarId));
        }

        Person person = Person.getPerson(context);
        Company company = person.getCompany(context);
        String strCompanyCalendar = PropertyUtil.getSchemaProperty(context,"relationship_CompanyCalendar");
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_NAME);
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_CURRENT);


        MapList companyCalenders =  company.getRelatedObjects(
                                        context,                    // eMatrix context
                                        strCompanyCalendar,    // relationship pattern
                                        DomainConstants.QUERY_WILDCARD,             // object pattern
                                        objectSelects,              // object selects
                                        null,                       // relationship selects
                                        false,                      // to direction
                                        true,                       // from direction
                                        (short) 1,                  // recursion level
                                        null,                // object where clause
                                        DomainConstants.EMPTY_STRING,0);              // relationship where clause

       Iterator itrCalenderCompany = companyCalenders.iterator();
       while(itrCalenderCompany.hasNext()){
           Map mapCalendar = (Map)itrCalenderCompany.next();
           if(mapCalendar.get(DomainConstants.SELECT_CURRENT).equals("Active")){
               slExcludeId.remove(mapCalendar.get(DomainConstants.SELECT_ID));
           }
       }
       return slExcludeId;
    }
    // End: Added for Task calender feature

    /**
     *
     * This API to be used for table column to display linked person object with image icon.
     * It returns the Full name of Owner/Assignee/or any Person Object which can be selected on business object or relationship.
     * String provided in Expression field of Column will be used as a Key to fetch Person value which had been stored in object map against this key.
     * Value of Person Object must be stored in object map against this key only.(In Table or Expand Table method.)
     *
     *
     * @param context The Matrix Context object
     * @param args Packed program and request maps for the table
     * @return Vector of owners full name
     * @throws Exception if operation fails
     */

    public StringList getPersonFullName(Context context,String[] args) throws Exception
    {
        StringList vcPersonFullName = new StringList();
        try
        {
            HashMap programMap         = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map mapColumnMap = (Map)programMap.get("columnMap");

            //Code for Export bug fix for Owner Column IR-205151V6R2014
            Map paramListMap = (Map)programMap.get("paramList");
            String reportFormat = (String) paramListMap.get("reportFormat");
            String exportFormat = (String) paramListMap.get("exportFormat");

            String strBusExpression=(String)mapColumnMap.get("expression_businessobject");
            String strRelExpression=(String)mapColumnMap.get("expression_relationship");
                String strExpressionData= ProgramCentralConstants.EMPTY_STRING;

            if(null != strBusExpression && !"".equals(strBusExpression))
            {
                strExpressionData= strBusExpression;
            }
            else if(null != strRelExpression && !"".equals(strRelExpression))
            {
                strExpressionData =strRelExpression;
            }
            else if((null == strBusExpression && null == strRelExpression)|| ("" .equals(strBusExpression) && "".equals(strRelExpression)))
            {
                throw new MatrixException("Invalid Key in Expression  "+strBusExpression +" : "+strRelExpression);
            }

            Map mapObjects = null;
            String strObjectId = DomainConstants.EMPTY_STRING;
            DomainObject dmoObject = null;
            String  strOwnerLoginName = DomainConstants.EMPTY_STRING;
            String strLanguage = ProgramCentralUtil.getLocale(context).getLanguage();
            String strPersonFullName = DomainConstants.EMPTY_STRING;
            String strPersonId = DomainConstants.EMPTY_STRING;
            boolean isRoleType = false;
            boolean isGroupType = false;
            String imgRole = "<img src=\"../common/images/iconSmallRole.gif\" border=\"0\" id=\"\" title=\"\"></img>";
            String imgPerson ="<img src=\"../common/images/iconSmallPerson.gif\" border=\"0\" id=\"\" title=\"\"></img>";
            String imgGroup = "<img src=\"../common/images/iconSmallGroup.gif\" border=\"0\" id=\"\" title=\"\"></img>";

            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();)
            {
                StringBuffer strPersonFullNameBuffer = new StringBuffer();
                mapObjects = (Map) itrObjects.next();

                strOwnerLoginName = (String)mapObjects.get(strExpressionData);
                isRoleType = "true".equalsIgnoreCase((String)mapObjects.get(strExpressionData+".isarole"))?true:false;
                isGroupType = "true".equalsIgnoreCase((String)mapObjects.get(strExpressionData+".isagroup"))?true:false;

                if(ProgramCentralUtil.isNullString(strOwnerLoginName))
                {
                    strObjectId = (String)mapObjects.get(DomainConstants.SELECT_ID);
                    StringList selectList = new StringList();
                    selectList.add(strExpressionData);
                    selectList.add(strExpressionData+".isaperson");
                    selectList.add(strExpressionData+".isagroup");
                    selectList.add(strExpressionData+".isarole");
                    Map objectMap = dmoObject.newInstance(context,strObjectId).getInfo(context, selectList);
                    strOwnerLoginName = (String)objectMap.get(strExpressionData);
                    isRoleType = "true".equalsIgnoreCase((String)mapObjects.get(strExpressionData+".isarole"))?true:false;
                    isGroupType = "true".equalsIgnoreCase((String)mapObjects.get(strExpressionData+".isagroup"))?true:false;
                }

                if(isRoleType)
                {
                    strPersonFullName = i18nNow.getRoleI18NString(strOwnerLoginName,strLanguage);
                    strPersonFullNameBuffer.append(imgRole+" ");
                    strPersonFullNameBuffer.append(XSSUtil.encodeForHTML(context,strPersonFullName));
                }
                else if(isGroupType)
                {
                    strPersonFullName = i18nNow.getMXI18NString(strOwnerLoginName, "", strLanguage, "Group");
                    strPersonFullNameBuffer.append(imgGroup+" ");
                    strPersonFullNameBuffer.append(XSSUtil.encodeForHTML(context,strPersonFullName));
                }

                //Code for Export bug fix for Owner Column
                else if("CSV".equalsIgnoreCase(reportFormat) && "CSV".equalsIgnoreCase(exportFormat)){
                    strPersonFullName = PersonUtil.getFullName(context, strOwnerLoginName);
                    strPersonFullNameBuffer.append(XSSUtil.encodeForHTML(context,strPersonFullName));
                }
                else
                {
                    //This code is written to check for person object having admin object but no business object.
                    //PersonUtil.getPersonObjectID() is only giving value if business object exist.
                    String sCommandStatement = "temp query bus $1 $2 $3 select $4 dump $5";
                    String result =  MqlUtil.mqlCommand(context, sCommandStatement,DomainConstants.TYPE_PERSON,strOwnerLoginName,"*","id","|");
                    StringList slResult = FrameworkUtil.splitString(result, "|");
                    if(ProgramCentralUtil.isNotNullString(result) && slResult.size()>0)
                    {
                        strPersonId = (String)slResult.get(3);
                        strPersonFullName = PersonUtil.getFullName(context, strOwnerLoginName);
                        strPersonFullNameBuffer.append(imgPerson);
                        strPersonFullNameBuffer.append("<a href='../common/emxTree.jsp?objectId=");
                        strPersonFullNameBuffer.append(strPersonId).append("'>");
                        strPersonFullNameBuffer.append(XSSUtil.encodeForXML(context,strPersonFullName));
                        strPersonFullNameBuffer.append("</a>");
                    }
                    else
                    {
                        strPersonFullNameBuffer.append(XSSUtil.encodeForHTML(context,strOwnerLoginName));
                    }
                }
                vcPersonFullName.add(strPersonFullNameBuffer.toString());
            }
        }
        catch (Exception ex)
        {
            throw new MatrixException(ex);
        }
        return vcPersonFullName;
    }

    /**
     * This trigger get the old object from that it get associated publish subscribe object
     * then get the associated Event objects from which it fetch the person objects and finally
     * get the revised object's id and push the old object's subscription events to the revised
     * object for those person objects.
     *
     * @author RG6
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            : object id - Old ObjectId before revising the Object
     *            : newRev    - Latest revision of the object which is created
     * @return int : int based on success or failure of the trigger
     *              0 - Success
     *              1 - Failure
     * @throws MatrixException
     *             if the operation fails
     * @since PRG R210
     */
    public static int triggerLinkSubscriptionsToLatestRevision(Context context,
            String[] args) throws MatrixException {
        try {
            ContextUtil.startTransaction(context, true);

            // Check method arguments
            if (context == null || args == null) {
                throw new IllegalArgumentException();
            }

            String strObjectId = args[0]; // object id of previous revision
            String strNewRev = args[1];

            if (strObjectId == null || "".equalsIgnoreCase(strObjectId)) {
                throw new IllegalArgumentException();
            }

            if (strNewRev == null || "".equalsIgnoreCase(strNewRev)) {
                throw new IllegalArgumentException();
            }

            SubscriptionUtil sbUtilObj = new SubscriptionUtil();
            String strLanguage = context.getSession().getLanguage();
            String strLatestRevObjId = ""; // new object id of revised object
            int count = 0;
            String SELECT_NEXT_ID = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Subscriptions.Select.Next.Id");
            StringList slBusSel = new StringList();
            slBusSel.add(SELECT_NEXT_ID);
            DomainObject dmoOldObject = DomainObject.newInstance(context,strObjectId);

            Map dObjInfoMap = dmoOldObject.getInfo(context, slBusSel);
            // get the new revision's object id
            String nextId = (String)dObjInfoMap.get(SELECT_NEXT_ID);

            strLatestRevObjId = nextId;  // new object ID

            //get subscribed events list for the old object
            Map objEventMap = SubscriptionUtil.getObjectSubscribedEventsMap(context, strObjectId);
            Map subscriptionMap = new HashMap();  // subscription map containing both subscribed and pushed events
            if(objEventMap != null && objEventMap.size()>0){
                Map SubscribedMap = (Map)objEventMap.get("Subscription Map");
                Map pushedSubMap =  (Map)objEventMap.get("Pushed Subscription Map");
                subscriptionMap.putAll(SubscribedMap);
                subscriptionMap.putAll(pushedSubMap);
            }

            Set eventKeySet = subscriptionMap.keySet();  // iterate over the events
            StringList slPersonNotificationList = new StringList();
            StringList slPersonlist = new StringList();
            for(Iterator itEventNames = eventKeySet.iterator();itEventNames.hasNext();){
                String eventName = (String)itEventNames.next();
                //get associated person object list for the subscribed event.
                slPersonlist =sbUtilObj.getSubscribers(context, strObjectId, (HashMap)subscriptionMap.get(eventName),"object",true);
                // array containing person names
                String[] strArrayObjPerson = new String[slPersonlist.size()];
                count = 0;  //reset the counter
                while(count<slPersonlist.size()){
                    String strPersonName = (String)slPersonlist.get(count); // list content both name and notification type separated by "|"
                    slPersonNotificationList.clear();
                    slPersonNotificationList = FrameworkUtil.split(strPersonName, "|");
                    String strActualPerosnName = (String)slPersonNotificationList.get(0);
                    String strPersonObjID = PersonUtil.getPersonObjectID(context, strActualPerosnName); // find the person object id
                    strArrayObjPerson[count] = strPersonObjID;
                    count++;
                }
                // push the subscription from old object,to the revised object for each event
                SubscriptionUtil.createPushSubscription(context, strLatestRevObjId, eventName, strArrayObjPerson, strLanguage);
            }

        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw new MatrixException(e);
        }
        ContextUtil.commitTransaction(context);

        return 0;
    }

    /**
     * This method is used get formatted value of currency as per context locale and display currency symbol or ISO4217 code as per locale input
     * @param context the eMatrix <code>Context</code> object
     * @param strCurrency
     *                  Currency value such as Dollar, Yuan Renimimbi, Yen, Euro, Pound.
     *                  If the currency value is different than system defined then need
     *                  to add ISO 4217 value in the properties file.
     * @param nStdCost
     *                  Cost value which needs to be formatted.
     * @return String
     *                  which contains formatted value of currency as per locale.
     *
     * @throws MatrixException
     */
    public static String getFormattedCurrencyValue(Context context, String strCurrency,double nStdCost) throws MatrixException
    {
        return getFormattedCurrencyValue(context, context.getLocale(),strCurrency, nStdCost);
    }

    /**
     * This method is used get formatted value of currency as per given locale and display currency symbol or ISO4217 code as per locale input
     * @param context the eMatrix <code>Context</code> object
     * @param localeObj
     *                  LocaleObj of Browser or defined
     * @param strCurrency
     *                  Currency value such as Dollar, Yuan Renimimbi, Yen, Euro, Pound.
     *                  If the currency value is different than system defined then need
     *                  to add ISO 4217 value in the properties file.
     * @param nStdCost
     *                  Cost value which needs to be formatted.
     * @return String
     *                  which contains formatted value of currency as per locale.
     *
     * @throws MatrixException
     */
    public static String getFormattedCurrencyValue(Context context, Locale localeObj, String strCurrency,double nStdCost) throws MatrixException
    {
        try {
            if(null==localeObj)
            {
                localeObj = context.getLocale();
            }
        NumberFormat format = NumberFormat.getCurrencyInstance(localeObj);
        String strDispCurrency = strCurrency;
        if(strCurrency.trim().contains(" "))
        {
            strDispCurrency = strCurrency.replace(" ", "_");
        }
        String strCurrencyCode = EnoviaResourceBundle.getProperty(context,"emxProgramCentral.CurrencyCode.ISO4217."+strDispCurrency);
        Currency currency = Currency.getInstance(strCurrencyCode);
        format.setCurrency(currency);
            //Currently fractional setting are according to java default.
            //if someone wants to change fractional setting to user default then following needs to be uncomment.
            //format.setMaximumFractionDigits(5);
        if (format instanceof DecimalFormat) {
            DecimalFormat decimalFormat = (DecimalFormat) format;
            decimalFormat.setNegativePrefix("(" + decimalFormat.getPositivePrefix());
            decimalFormat.setNegativeSuffix(")");
        }
        String strFormattedCost = format.format(nStdCost);
        return strFormattedCost;
    }
        catch (FrameworkException e)
    {
            throw new MatrixException(e);
    }
    }

    /**
     * This method is used get formatted value of currency as per context locale into number.
     * @param context the eMatrix <code>Context</code> object
     * @param strCostValue
     *                  Cost value which needs to be formatted.
     * @return BigDecimal
     *                  which contains formatted value of currency in number as per locale.
     *
     * @throws MatrixException
     */
    public static BigDecimal getNormalizedCurrencyValue(Context context,String strCostValue) throws MatrixException
    {
        return ProgramCentralUtil.getNormalizedCurrencyValue(context,context.getLocale(), strCostValue);
    }
    /**
     * This method is used get formatted value of currency as per given locale into number.
     * @param context the eMatrix <code>Context</code> object
     * @param localeObj
     *                  LocaleObj of Browser or defined
     * @param strCostValue
     *                  Cost value which needs to be formatted.
     * @return BigDecimal
     *                  which contains formatted value of currency in number as per locale.
     *
     * @throws MatrixException
     */
    public static BigDecimal getNormalizedCurrencyValue(Context context, Locale locale, String strCostValue) throws MatrixException
    {
        return ProgramCentralUtil.getNormalizedCurrencyValue(context,locale, strCostValue);
    }

    /**
     * This method is used get formatted value of number as per context locale.
     * @param context the eMatrix <code>Context</code> object
     * @param number
     *                  Number which needs to be formatted.
     * @return String
     *                  which contains formatted value of number as per locale.
     *
     * @throws MatrixException
     */
    public static String getFormattedNumberValue(Context context,double number) throws MatrixException
    {
        return getFormattedNumberValue(context, context.getLocale(), number);
    }
    /**
     * This method is used get formatted value of number as per given locale.
     * @param context the eMatrix <code>Context</code> object
     * @param localeObj
     *                  LocaleObj of Browser or defined
     * @param number
     *                  Number which needs to be formatted.
     * @return String
     *                  which contains formatted value of number as per locale.
     *
     * @throws MatrixException
     */
    public static String getFormattedNumberValue(Context context, Locale localeObj, double number) throws MatrixException
    {
        try {
            if(null==localeObj)
            {
                localeObj = context.getLocale();
            }
            NumberFormat format = NumberFormat.getNumberInstance(localeObj);
            String strFormattedCost = format.format(number);
            return strFormattedCost;
        }
        catch (Exception e)
        {
            throw new MatrixException(e);
        }
    }
    /**
     * This method is used to get currency symbol of given currency
     * @param context the eMatrix <code>Context</code> object
     * @param strCurrency
     *                  Currency value which needs to be formatted.
     * @return String
     *                  which contains formatted value of currency as per locale.
     *
     * @throws MatrixException
     */
    public static String getCurrencySymbol(Context context,String strCurrency) throws MatrixException
    {
        return getCurrencySymbol(context, context.getLocale(), strCurrency);
    }
    /**
     * This method is used to get currency symbol of given currency
     * @param context the eMatrix <code>Context</code> object
     * @param localeObj
     *                  LocaleObj of Browser or defined
     * @param nStdCost
     *                  Cost value which needs to be formatted.
     * @return String
     *                  which contains formatted value of currency as per locale.
     *
     * @throws MatrixException
     */
    public static String getCurrencySymbol(Context context, Locale localeObj,String strCurrency) throws MatrixException
    {
        try
        {
            return com.matrixone.apps.program.Currency.getCurrencySymbol(context, localeObj, strCurrency);
    }
        catch (Exception e)
    {
        throw new MatrixException(e);
    }
    }
    /**
     * Returns the user's preferred currency. Incase no currency is set then an error message will be shown.
     * @param context the ENOVIA <code>Context</code> user.
     * @param strNoticeKey the string error message key.
     * @return the currency set by user in preferences page.
     * @throws MatrixException if operation fails.
     */
    public static String getUserPreferenceCurrency(Context context, String strNoticeKey)
    throws MatrixException{
        try
        {
            String sLanguage = context.getSession().getLanguage();
            PersonUtil contextUser =  new PersonUtil();
            String strPreferredCurrency = contextUser.getCurrency(context);
            if(ProgramCentralUtil.isNotNullString(strPreferredCurrency) &&
                    !strPreferredCurrency.equals("As Entered")
                    && !strPreferredCurrency.equals("Unassigned")){
                return strPreferredCurrency;
            }
            else{
                String strTotalLabel = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                        strNoticeKey,sLanguage);
                throw new MatrixException(strTotalLabel);
            }
        }
        catch (MatrixException e){
            throw e;
        }
        catch (Exception e){
            throw new MatrixException(e);
        }
    }

    public static String getParentProject(Context context,String[] args) throws Exception{
        String prjId = "";
        try{
            String taskId = args[0];
            if(ProgramCentralUtil.isNullString(taskId)){
                throw new Exception();
            }

            DomainObject domObj = DomainObject.newInstance(context);
            domObj.setId(taskId);
            prjId = Task.getParentProject(context, domObj);
        }
        catch(Exception e){
//            e.printStackTrace();   
            DebugUtil.debug("getParentProject "+ e.getMessage());
            throw new MatrixException(e);
        }
        BufferedWriter writer = new BufferedWriter(new MatrixWriter(context));
        writer.write(prjId);
        writer.flush();
        return prjId;
    }


    //Added : H1A : IR-148090V6R2013x : 08/06/2012
    /**
     * This method is used to Exclude Document objects which have the is Versioned Object attribute set to true.
     * @param context the eMatrix <code>Context</code> object
     * @param args  arguments
     * @return StringList  List of document ids to be excluded
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getExcludeOIDForVersionedDocuments(Context context, String args[]) throws Exception
    {
        MapList mapDocumentList = null;
        StringList select = new StringList();
        select.add(DomainConstants.SELECT_ID);
        select.add(DomainConstants.SELECT_NAME);
        select.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
        String whereExp = "attribute["+CommonDocument.ATTRIBUTE_IS_VERSION_OBJECT+"] == True";

        //String strTypePattern =CommonDocument.TYPE_DOCUMENTS+","+PropertyUtil.getSchemaProperty("type_DOCUMENTCLASSIFICATION")+","+DomainConstants.TYPE_ECO+","+DomainConstants.TYPE_ECR+","+DomainConstants.TYPE_PART+","+DomainConstants.TYPE_PRODUCTLINE+","+PropertyUtil.getSchemaProperty("type_ExternalDeliverable");
        String strTypePattern =CommonDocument.TYPE_DOCUMENTS;
        mapDocumentList =DomainObject.findObjects(context,strTypePattern,null,whereExp,select);
        StringList  slVersionedDocumentIds = new StringList();
        int size = mapDocumentList.size();
        for(int i=0; i<size; i++)
        {
            Map mapDocumentObj = (Map) mapDocumentList.get(i);
            String strId = (String) mapDocumentObj.get(CommonDocument.SELECT_ID);
            slVersionedDocumentIds.add(strId);
        }

        return slVersionedDocumentIds;
    }

    /**
     * Exclude OID happens depending upon user role pass to emxFullSearch parameter arguments "field=TYPES=Person:USERROLE="
     * @param context The matrix context object
     * @param returns StringList containing Person whicn not in any Organization
     * @param args The arguments, it contains programMap
     * @throws Exception if operation fails
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getexcludeOIDforPersonSearch(Context context, String[] args)throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strRoles = (String)programMap.get("field");
            strRoles = strRoles.substring(22, strRoles.length());//"TYPES=Person:USERROLE="
            String[] saRoles = strRoles.split(",");
            StringList slPersonNames = new StringList();
            String strPersonRole = "";
            StringList slPersonIds = new StringList();
            StringList slPersonRoles = new StringList();
            slPersonRoles.add(ProgramCentralConstants.ROLE_PROJECT_LEAD);
            slPersonRoles.add(ProgramCentralConstants.ROLE_EXTERNAL_PROJECT_LEAD);
            slPersonRoles.add(ProgramCentralConstants.ROLE_PROJECT_USER);
            slPersonRoles.add(ProgramCentralConstants.ROLE_EXTERNAL_PROJECT_USER);
            slPersonRoles.add(ProgramCentralConstants.ROLE_VPLM_VIEWER);
            slPersonRoles.add(ProgramCentralConstants.ROLE_VPLM_PROJECT_LEADER);
            slPersonRoles.add(ProgramCentralConstants.ROLE_VPLM_ADMIN);
            slPersonRoles.add(ProgramCentralConstants.ROLE_VPLM_PROJECT_OWNER);

            for(int i = 0; i<saRoles.length;i++)
            {
                if(slPersonRoles.contains(saRoles[i]))
                {
                    strPersonRole = saRoles[i];
                    String strPersonName ="";
                    String strPersonId ="";
                    StringList slpersonOrg = new StringList();
                    int nLength;
                    slPersonNames = PersonUtil.getPersonFromRole(context, strPersonRole);
                    for(int nCount=0;nCount<slPersonNames.size();nCount++)
                    {
                        strPersonName=(String)slPersonNames.get(nCount);
                        //This method "PersonUtil.getMemberOrganizations()" is used to check if the person is associated with any organisation/company
                        //If the person gets deleted then the object remains as an Admin object which we can see in "Business Modeler",
                        //So it will give exception for method PersonUtil.getPersonObjectID()
                        strPersonId = PersonUtil.getPersonObjectID(context, strPersonName);
                        DomainObject domPerson = new DomainObject(strPersonId);
                        slpersonOrg = domPerson.getInfoList(context, "to["+DomainConstants.RELATIONSHIP_EMPLOYEE+"].from.id");
                        nLength = slpersonOrg.size();
                        if(nLength==0)
                        {
                              slPersonIds.add(strPersonId);
                            }
                }
            }
            }
            return slPersonIds;
        }
        catch (Exception e)
        {
//            e.printStackTrace();   
            DebugUtil.debug("getexcludeOIDforPersonSearch "+ e.getMessage());
            throw e;
        }
    }

    /**
     * Include OID program that list Project Members to assign to a task along with Project Role selection
     * @param context The matrix context object
     * @param returns StringList containing Person whicn not in any Organization
     * @param args The arguments, it contains programMap
     * @throws Exception if operation fails
     */
    @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList getIncludeOIDforProjectMemberPersonSearch(Context context, String[] args)throws Exception {
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strProjectId = (String)programMap.get("objectId");
            String strProjectRole = (String)programMap.get("SelectedProjectRole");

            StringList projectMemberSelects = new StringList(2);
            projectMemberSelects.add(DomainConstants.SELECT_NAME);
            projectMemberSelects.add(DomainConstants.SELECT_ID);

            StringList projectInfo = new StringList(3);
            projectInfo.add(ProgramCentralConstants.SELECT_TYPE);
            projectInfo.add(ProgramCentralConstants.SELECT_TASK_PROJECT_ID);

            ProjectSpace project = (ProjectSpace) DomainObject.newInstance(context,
                    DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
            project.setId(strProjectId);
            Map projectMap = project.getInfo(context, projectInfo);

            StringList slProjectSpace = ProgramCentralUtil.getSubTypesList(context, DomainConstants.TYPE_PROJECT_SPACE);

            //if user selects task object and invokes new task creation form
            if(!slProjectSpace.contains(projectMap.get(DomainConstants.SELECT_TYPE)))
                strProjectId = (String)projectMap.get(ProgramCentralConstants.SELECT_TASK_PROJECT_ID);
            project.setId(strProjectId);

            StringList relSelects = new StringList(2);
            relSelects.add(MemberRelationship.SELECT_PROJECT_ROLE);
            relSelects.add(MemberRelationship.SELECT_PROJECT_ACCESS);

            String relWhere = DomainConstants.EMPTY_STRING;
            if(ProgramCentralUtil.isNotNullString(strProjectRole))
                relWhere = "attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"] == \""+strProjectRole+"\"";
            else
                relWhere = DomainConstants.EMPTY_STRING;

            MapList membersList = project.getMembers(context, projectMemberSelects, relSelects, null, relWhere);
            Map member = new HashMap();
            StringList slPersonIds = new StringList();
            for(int i=0; i< membersList.size(); i++){
                member = (Map)membersList.get(i);
                Object oMemberId = member.get(DomainConstants.SELECT_ID);
                slPersonIds.add(String.valueOf(oMemberId));
            }
            return slPersonIds;
        }
        catch (Exception e)
        {
//            e.printStackTrace();   
            DebugUtil.debug("getIncludeOIDforProjectMemberPersonSearch "+ e.getMessage());
            throw e;
        }
    }

    /**
     * Method to check if LBC (Library Central) is installed
     *
     * @param context
     * @param args
     * @return true - if LBC is installed
     *         false - if LBC is not Installed
     * @exception throws FrameworkException
     * @since R215
     */
     public boolean isLBCInstalled(Context context,String args[]) throws FrameworkException
     {
         return  FrameworkUtil.isSuiteRegistered(context,"appVersionLibraryCentral",false,null,null);
     }

     /**Gets i18n value for passed key along with placeholders in keys.
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *       msgKey: key to translate
      *       Key   : Array of placeholder keys
      *       Value : Array of placeholder values
      * @throws MatrixException if operation fails
      */
     public String geti18nString(Context context, String[] args) throws MatrixException
     {
         String sReturnValue = "";

         try {
             Map mParamMap = (Map) JPO.unpackArgs(args);
             String sPromoteMsg = (String) mParamMap.get("msgKey");
             String sKey[] = (String[]) mParamMap.get("placeHolderKeys");
             String sValue[] = (String[]) mParamMap.get("placeHolderValues");

             sReturnValue = getMessage(context, sPromoteMsg, sKey, sValue, null);
         } catch (Exception e) {
             throw new MatrixException(e);
         }
         return sReturnValue;
     }

/**
     * This method is called to get "Previous Project State" attribute of "Project Space".
     * @param context
     *        context object which is used while fetching data related application.
     *
     * @param args
     *        Holds input argument.
     *
     * @throws Exception
     *         Exception can be thrown in case of method fails to execute.
     */
    public String getPreviousState(Context context,String[] args) throws Exception {
        String previousState    =   DomainConstants.EMPTY_STRING;
         Map programMap =  JPO.unpackArgs(args);
        String projectId = (String)programMap.get(ProgramCentralConstants.SELECT_ID);
         ProjectSpace projectObj = (ProjectSpace)DomainObject.newInstance(context,
                 DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.TYPE_PROGRAM);
        projectObj.setId(projectId);

         previousState  = projectObj.getAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_PREVIOUS_STATE);
         if (previousState.contains(ProgramCentralConstants.COMMA)) {
             String[] stateArray    =   previousState.split(ProgramCentralConstants.COMMA);
             previousState  =   stateArray[0];
         }
         return previousState;
    }

    /**
     * This method is called to get Previous policy of the ProjectSpace.
     * @param context
     *        context object which is used while fetching data related application.
     *
     * @param args
     *        Holds input argument.
     *
     * @throws Exception
     *         Exception can be thrown in case of method fails to execute.
     */

    public String getPreviousPolicy(Context context,String[] args) throws Exception {
        String previousPolicy   =   DomainConstants.EMPTY_STRING;
         Map programMap = JPO.unpackArgs(args);
        String projectId = (String)programMap.get(ProgramCentralConstants.SELECT_ID);

         ProjectSpace projectObj = (ProjectSpace)DomainObject.newInstance(context,
                 DomainConstants.TYPE_PROJECT_SPACE,    DomainConstants.TYPE_PROGRAM);
        projectObj.setId(projectId);

         previousPolicy = projectObj.getAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_PREVIOUS_STATE);
         if (previousPolicy.contains(ProgramCentralConstants.COMMA)) {
             String[] stateArray    =   previousPolicy.split(ProgramCentralConstants.COMMA);
            previousPolicy  =   stateArray[1];
         } else {
             previousPolicy =   DomainConstants.POLICY_PROJECT_SPACE;
         }
         return previousPolicy;
       }

    /**
     * This method sets the value "Previous Project State" attribute of "Project Space" to "Previous state","Previous policy".
     * @param context
     *        context object which is used while fetching data related application.
     *
     * @param args
     *        Holds input argument.
     *
     * @throws Exception
     *         Exception can be thrown in case of method fails to execute.
     */
    public void setPreviousState(Context context,String[] args) throws Exception {
         Map programMap = JPO.unpackArgs(args);
         String projectId = (String)programMap.get(ProgramCentralConstants.SELECT_ID);
         String state = (String)programMap.get(ProgramCentralConstants.SELECT_CURRENT);
         String policy = (String)programMap.get(ProgramCentralConstants.SELECT_POLICY);

         ProjectSpace projectObj = (ProjectSpace)
                        DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.TYPE_PROGRAM);

         projectObj.setId(projectId);

         String previousPolicy  =   DomainConstants.EMPTY_STRING;
         String previousState   =   projectObj.getAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_PREVIOUS_STATE);
         String previousStateAndPolicy  =   projectObj.EMPTY_STRING;

         if (previousState.contains(ProgramCentralConstants.COMMA)) {
             String[] stateArray    =   previousState.split(ProgramCentralConstants.COMMA);
             previousPolicy =   stateArray[1];
         } else {
             previousPolicy =   DomainConstants.POLICY_PROJECT_SPACE;
         }

         if (ProgramCentralUtil.isNotNullString(policy)) {
             previousStateAndPolicy =   state + ProgramCentralConstants.COMMA + policy;
         } else {
             previousStateAndPolicy =   state + ProgramCentralConstants.COMMA + previousPolicy;
         }
        projectObj.setAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_PREVIOUS_STATE,previousStateAndPolicy);
    }


    /**
     *
     * It reloads the SB for portal command with updated values and mainntains the edit mode.
     *
     * @param context The ENOVIA <code>Context</code> object.
     * @param args holds information about objects.
     * @return map contains script with refresh logic.
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map postProcessPortalCmdSBRefresh(Context context, String[] args) throws Exception {

        HashMap hashMap = new HashMap();

        Map programMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        String currentFrameName = XSSUtil.encodeForJavaScript(context, (String)requestMap.get("portalCmdName"));


        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("main:function() {");
        sb.append("var topFrame = findFrame(getTopWindow(), \"detailsDisplay\");");
        sb.append("var cmdURL = topFrame.location.href;");
        //sb.append("cmdURL += \"&mode=edit\";");
        sb.append("topFrame.location.href = cmdURL;");
        sb.append("}}");

        hashMap.put("Action","execScript");
        hashMap.put("Message", sb.toString());

//        hashMap.put("Action", "refresh");
        return hashMap;
    }

    /**
     *
     * Inherit POV and SOV access for any object when the relationship is being created.
     *
     * @param context The ENOVIA <code>Context</code> object.
     * @param args holds information about objects.
     * @throws Exception
     */
    public boolean PRGCreateAccessInheritance(Context context, String[] args) throws Exception
    {
       String fromId = args[0];
       String toId = args[1];
       String command = "";
       boolean contextPushed = false;

       try{
           ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
           contextPushed = true;
           DomainObject domChildObj = DomainObject.newInstance(context,fromId);
           if(domChildObj.isKindOf(context,DomainConstants.TYPE_PROJECT_SPACE))
           {
               command = "modify bus " + toId + " add access bus " + fromId;
           }
           else if(domChildObj.isKindOf(context,DomainConstants.TYPE_WEEKLY_TIMESHEET))
           {
               command = "modify bus " + fromId + " add access bus " + toId;
           }

           MqlUtil.mqlCommand(context, command);

          return true;
       }catch(Exception e){
          throw e;
       }
       finally
       {
           if(contextPushed)
           {
               ContextUtil.popContext(context);
           }
       }

    }

    /**
     *
     * Inherit POV and SOV access for any object when the relationship is being created.
     *
     * @param context The ENOVIA <code>Context</code> object.
     * @param args holds information about objects.
     * @throws Exception
     */
    public boolean setAccessInheritance(Context context, String[] args) throws Exception
    {
       String fromId = args[0];  // id of from object
       String toId = args[1];  // id of to object
       String onlyIncludeToType = args[2]; // only inherit access to this Type on the To side of the relationship
       String accessBits = args[3];  // access bits we want to inherit from the parent

       String command = "";
       boolean contextPushed = false;

       try{
           boolean bOKToInheritAccess = false;
           String TYPE_THREAD = PropertyUtil.getSchemaProperty("type_Thread" );
           String SELECT_KINDOF_THREAD = "type.kindof[" + TYPE_THREAD+ "]";
           StringList slBusSelect = new StringList();
           slBusSelect.add(DomainConstants.SELECT_ID);
           slBusSelect.add(DomainConstants.SELECT_TYPE);
           Map mapParent = null;
           String strParentid = DomainConstants.EMPTY_STRING;
           DomainObject domParentObject = DomainObject.newInstance(context);

           // if the To side is a Document and it has Specific Access Type then return
           DomainObject toObj = DomainObject.newInstance(context, toId);
           String sAccessType = toObj.getInfo(context, ProgramCentralConstants.SELECT_ATTRIBUTE_ACCESS_TYPE);
           if (sAccessType.equalsIgnoreCase("Specific"))
           {
               return true;
           }

            // Get information about the From side object
           StringList strFromList = new StringList();
           strFromList.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
           strFromList.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_CONCEPT);
           strFromList.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
           strFromList.add(SELECT_KINDOF_THREAD);

           DomainObject fromObj = DomainObject.newInstance(context,fromId);
           Map mpObjectInfo = fromObj.getInfo(context,strFromList);
           String bisFromProjectSpaceType = (String)mpObjectInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
           String bisFromProjectConceptType = (String)mpObjectInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_CONCEPT);
           String bisFromTaskManagementType = (String)mpObjectInfo.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
           String bisFromThreadType = (String)mpObjectInfo.get(SELECT_KINDOF_THREAD);


            // if in the context of PS, PC or PT
           if (bisFromProjectSpaceType.equalsIgnoreCase("true") || bisFromProjectConceptType.equalsIgnoreCase("true") || bisFromTaskManagementType.equalsIgnoreCase("true"))
           {
               bOKToInheritAccess = true;
           }
           else if (bisFromThreadType.equalsIgnoreCase("true"))
           {
               String strRelatioshipPattern = DomainConstants.RELATIONSHIP_THREAD + "," + DomainConstants.RELATIONSHIP_SUBTASK;

               MapList mlParent = fromObj.getRelatedObjects(context,     // context.
                        strRelatioshipPattern,                              // rel filter.
                        DomainConstants.QUERY_WILDCARD,                     // type filter
                        slBusSelect,                                        // business object selectables.
                        null,                                               // relationship selectables.
                        true,                                               // expand to direction.
                        false,                                              // expand from direction.
                        (short) 0,                                          // level
                        null,                                               // object where clause
                        null,                                               // relationship where clause
                        0);                                                 //limit

                for (Iterator itrParent = mlParent.iterator(); itrParent.hasNext();)
                {
                    mapParent = (Map) itrParent.next();
                    if(mapParent != null)
                    {
                        strParentid = (String)mapParent.get(DomainConstants.SELECT_ID);
                        domParentObject.setId(strParentid);

                        // if in the context of PS, PC or PT then restamp
                        if(domParentObject.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE) || domParentObject.isKindOf(context, DomainConstants.TYPE_PROJECT_TEMPLATE)
                            || domParentObject.isKindOf(context, DomainConstants.TYPE_PROJECT_CONCEPT))
                        {
                            bOKToInheritAccess = true;
                            break;
                       }
                    }
               }
           }


           if (bOKToInheritAccess)
           {
              ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
              contextPushed = true;

              if (onlyIncludeToType != null && (! "".equalsIgnoreCase(onlyIncludeToType)))
              {
                  String typeToInclude = PropertyUtil.getSchemaProperty(context, onlyIncludeToType);

                  DomainObject domChildObj = DomainObject.newInstance(context,toId);
                  // onlyIncludeToType is used to specify that we only want this to type to inherit access for the relationship being created
                  if(domChildObj.isKindOf(context, typeToInclude))
                  {
                     command = "modify bus " + toId + " add access bus " + fromId + " for 'Inherited Access' as " +  accessBits;
                  }
              }
              else
              {
                  command = "modify bus " + toId + " add access bus " + fromId + " for 'Inherited Access' as " +  accessBits;
              }

              if(command.trim().length() != 0)
                 MqlUtil.mqlCommand(context, command);
           }
           return true;
       }catch(Exception e){
          throw e;
       }
       finally
       {
           if(contextPushed)
           {
               ContextUtil.popContext(context);
           }
       }

    }

    /**
     *
     * Trigger method to set the flag allowing Document created in the context of a Project Space to be re-stamped
     *
     * @param context The ENOVIA <code>Context</code> object.
     * @param args holds information about objects.
     * @throws Exception
     */
    public void triggerSetAllowPOVStamping(Context context, String[] args) throws Exception
    {
        String strDisableTrigger = context.getCustomData("disableTriggerForDocumentImportOrCreateScenario");
        if(UIUtil.isNotNullAndNotEmpty(strDisableTrigger)){
            return;
        }
        String objectId = args[0];

        // System.out.println("RVW::Entering triggerSetAllowPOVStamping for objectId = " + objectId);
        try {
            PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", "true", true);
        }
        catch (Exception e){
            PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", "false", true);
        }
        finally {
            // System.out.println("RVW::Exiting triggerSetAllowPOVStamping MX_ALLOW_POV_STAMPING = " + PropertyUtil.getRPEValue(context, "MX_ALLOW_POV_STAMPING", false));
        }
    }

    /**
    *
    * Change the POV of the child to the POV of the parent.
    *
    * @param context The ENOVIA <code>Context</code> object.
    * @param args holds information about objects.
    * @throws Exception
    */
    private  static String RELATIONSHIP_PROJECT_ASSESSMENT = null;
    private  static String RELATIONSHIP_REFERENCE_DOCUMENT = null;
    private  static String TYPE_MEETING = null;
    private  static String TYPE_DECISION = null;
    private  static String TYPE_DISCUSSION = null;
    private  static String TYPE_MESSAGE = null;
    private  static String TYPE_ISSUE = null;
    private  static String TYPE_DOCUMENTS = null;
    private  static String TYPE_PART_SPECIFICATION =null;
    static {
         RELATIONSHIP_PROJECT_ASSESSMENT = PropertyUtil.getSchemaProperty("relationship_ProjectAssessment");
         RELATIONSHIP_REFERENCE_DOCUMENT = PropertyUtil.getSchemaProperty("relationship_ReferenceDocument");
         TYPE_MEETING = PropertyUtil.getSchemaProperty("type_Meeting" );
         TYPE_DECISION = PropertyUtil.getSchemaProperty("type_Decision" );
         TYPE_DISCUSSION = PropertyUtil.getSchemaProperty("type_Thread" );
         TYPE_MESSAGE = PropertyUtil.getSchemaProperty("type_Message" );
         TYPE_ISSUE = PropertyUtil.getSchemaProperty("type_Issue" );
         TYPE_DOCUMENTS = PropertyUtil.getSchemaProperty("type_DOCUMENTS");
         TYPE_PART_SPECIFICATION = PropertyUtil.getSchemaProperty("type_PartSpecification");
    }
   public boolean SetPOVToParentPOV(Context context, String[] args) throws Exception
   {


       String createOperation = PropertyUtil.getGlobalRPEValue(context, "IGNORE_CREATE_TRIGGER");
       if("true".equalsIgnoreCase(createOperation)) {
           return true;
       }

      String fromId = args[0];  // id of from object

      // Had to make this variable final in order for it to be accessed in the inner Callable class (see below)
      //
      final String toId = args[1];  // id of to object
      final String relType =args[2];
      String isToRel = "";
      String isFromRel = "";
      if(args.length>=3){
        isToRel = args[3];
      }
      if(args.length>=4){
        isFromRel = args[4];
      }
      if(!("true".equalsIgnoreCase(isFromRel) || "true".equalsIgnoreCase(isToRel))){
      String command = "";
      try{

         String SELECT_PROJECT = "project";
         String SELECT_POLICY = "policy";
         Map mapParent = null;
         String strParentid = DomainConstants.EMPTY_STRING;
         StringList slBusSelect = new StringList();
         slBusSelect.add(DomainConstants.SELECT_ID);
         slBusSelect.add(DomainConstants.SELECT_TYPE);
         slBusSelect.add(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
         slBusSelect.add(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
         slBusSelect.add(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
         MapList mlParent = new MapList();

         String strFromOrganization = DomainConstants.EMPTY_STRING;
         String strFromProject = DomainConstants.EMPTY_STRING;
         String strToOrganization = DomainConstants.EMPTY_STRING;
         String strToProject = DomainConstants.EMPTY_STRING;
         String bisToProxyPolicy = DomainConstants.EMPTY_STRING;

         String bisFromProjectSpaceType = DomainConstants.EMPTY_STRING;
         String bisFromProjectConceptType = DomainConstants.EMPTY_STRING;
         String bisFromTaskManagementType = DomainConstants.EMPTY_STRING;
         String bisToIssueType = DomainConstants.EMPTY_STRING;
         String bisToMeetingType = DomainConstants.EMPTY_STRING;
         String bisToDecisionType = DomainConstants.EMPTY_STRING;
         String bisToDiscussionType = DomainConstants.EMPTY_STRING;
         String bisToMessageType = DomainConstants.EMPTY_STRING;
         String bisToDocumentType = DomainConstants.EMPTY_STRING;
         String bisToWorkspaceVaultType = DomainConstants.EMPTY_STRING;
         String SELECT_KINDOF_ISSUE = "type.kindof[" + TYPE_ISSUE+ "]";

         String SELECT_KINDOF_MEETING = "type.kindof[" + TYPE_MEETING+ "]";
         String SELECT_KINDOF_WORKSPACE_VAULT  =  "type.kindof["+DomainConstants.TYPE_WORKSPACE_VAULT+"]";
         String SELECT_KINDOF_DECISION = "type.kindof[" + TYPE_DECISION+ "]";
         String SELECT_KINDOF_DISCUSSION = "type.kindof[" + TYPE_DISCUSSION+ "]";
         String SELECT_KINDOF_MESSAGE = "type.kindof[" + TYPE_MESSAGE+ "]";


         String SELECT_KINDOF_DOCUMENTS = "type.kindof[" + TYPE_DOCUMENTS+ "]";

         //  IR-715553-3DEXPERIENCER2020x
         //

         String SELECT_KINDOF_PART_SPECIFICATION = "type.kindof[" + TYPE_PART_SPECIFICATION + "]";

         // Get information about the From side object
         StringList strFromList = new StringList();
         strFromList.add(DomainConstants.SELECT_ORGANIZATION);
         strFromList.add(SELECT_PROJECT);
         strFromList.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
         strFromList.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_CONCEPT);
         strFromList.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);

         DomainObject fromObj = DomainObject.newInstance(context,fromId);
         Map mpObjectInfo = fromObj.getInfo(context,strFromList);

         strFromOrganization = (String)mpObjectInfo.get(DomainConstants.SELECT_ORGANIZATION);
         strFromProject = (String)mpObjectInfo.get(SELECT_PROJECT);
         bisFromProjectSpaceType = (String)mpObjectInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
         bisFromProjectConceptType = (String)mpObjectInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_CONCEPT);
         bisFromTaskManagementType = (String)mpObjectInfo.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);

         // Get information about the To side object
         StringList strToList = new StringList();
         strToList.add(DomainConstants.SELECT_ORGANIZATION);
         strToList.add(SELECT_PROJECT);
         strToList.add(SELECT_POLICY);
         strToList.add(SELECT_KINDOF_ISSUE);
         strToList.add(SELECT_KINDOF_MEETING);
         strToList.add(SELECT_KINDOF_WORKSPACE_VAULT);
         strToList.add(SELECT_KINDOF_DECISION);
         strToList.add(SELECT_KINDOF_DISCUSSION);
         strToList.add(SELECT_KINDOF_DOCUMENTS);
         strToList.add(SELECT_KINDOF_MESSAGE);
         strToList.add(DomainConstants.SELECT_HAS_MODIFY_ACCESS);
         strToList.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);

         //  IR-715553-3DEXPERIENCER2020x
         //
         strToList.add(SELECT_KINDOF_PART_SPECIFICATION);

         DomainObject toObj = DomainObject.newInstance(context,toId);
         mpObjectInfo = toObj.getInfo(context,strToList);
         strToOrganization = (String)mpObjectInfo.get(DomainConstants.SELECT_ORGANIZATION);
         strToProject = (String)mpObjectInfo.get(SELECT_PROJECT);
         bisToProxyPolicy = (String)mpObjectInfo.get(SELECT_POLICY);
         bisToIssueType = (String)mpObjectInfo.get(SELECT_KINDOF_ISSUE);
         bisToMeetingType = (String)mpObjectInfo.get(SELECT_KINDOF_MEETING);
         bisToWorkspaceVaultType = (String)mpObjectInfo.get(SELECT_KINDOF_WORKSPACE_VAULT);
         bisToDecisionType = (String)mpObjectInfo.get(SELECT_KINDOF_DECISION);
         bisToDiscussionType = (String)mpObjectInfo.get(SELECT_KINDOF_DISCUSSION);
         bisToMessageType = (String)mpObjectInfo.get(SELECT_KINDOF_MESSAGE);
         String bisToProjectSpaceType = (String)mpObjectInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);

         bisToDocumentType = (String)mpObjectInfo.get(SELECT_KINDOF_DOCUMENTS);
         String requirementSpecPolicy = "Requirement Specification";
         String accessToDocTypeOwner = "Full";
         String drawingPrintPolicy = "Drawing Print";

         // IR-715553-3DEXPERIENCER2020x
         //
         String bisToPartSpecificationType = (String)mpObjectInfo.get(SELECT_KINDOF_PART_SPECIFICATION);

         // if project or org are blank or project is GLOBAL we don't want to change POV
         if((strFromOrganization.equals("")) || (strFromProject.equals("")) || (strFromProject.equals("GLOBAL")))
             return true;

         // if we are connecting a project as a subproject we don't wan't to change the POV of the sub project
         if(bisToProjectSpaceType.equalsIgnoreCase("true"))
             return true;

        // IR-715553-3DEXPERIENCER2020x
        // if we are connecting a Part Specification, we don't want to change the POV
        //
        if (bisToPartSpecificationType.equalsIgnoreCase("true") | (bisToProxyPolicy.equalsIgnoreCase(drawingPrintPolicy)))
            return true;

         // we do not need this for Proxy Items
         String POLICY_PROXYITEM = "ProxyItem";
         if(bisToProxyPolicy.equalsIgnoreCase(POLICY_PROXYITEM))
             return true;

         // check to see if the Project or Organization of the child object is different than its parent object
         if((!strFromOrganization.equals(strToOrganization)) || (!strFromProject.equals(strToProject)))
         {
            if (bisToDocumentType.equalsIgnoreCase("true"))  // if To object isKindof Document object
            {
                // check RPE variable to see if we are coming from a create and connect and not just a connect
                String allowPOVStamping = PropertyUtil.getRPEValue(context, "MX_ALLOW_POV_STAMPING", false);
                if(allowPOVStamping!= null && "true".equals(allowPOVStamping)) // if we are creating this new Document restamp
                {
                    String strRelatioshipPattern = DomainConstants.RELATIONSHIP_TASK_DELIVERABLE + "," + DomainConstants.RELATIONSHIP_SUBTASK + "," + DomainConstants.RELATIONSHIP_PROJECT_VAULTS + "," + DomainConstants.RELATIONSHIP_SUB_VAULTS + "," + DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2 + "," + RELATIONSHIP_PROJECT_ASSESSMENT + "," + RELATIONSHIP_REFERENCE_DOCUMENT + "," + DomainConstants.RELATIONSHIP_RISK;

                    try{
                        ContextUtil.pushContext(context);
                        // let only restanp if this Document is getting created in the context of a PS, PC or PT
                        mlParent = toObj.getRelatedObjects(context,                 // context.
                                strRelatioshipPattern,                              // rel filter.
                                DomainConstants.QUERY_WILDCARD,                     // type filter
                                slBusSelect,                                        // business object selectables.
                                null,                                               // relationship selectables.
                                true,                                               // expand to direction.
                                false,                                              // expand from direction.
                                (short) 0,                                          // level
                                null,                                               // object where clause
                                null,                                               // relationship where clause
                                0);                                                 //limit
                    }catch(FrameworkException e){
//                        e.printStackTrace();   
                        DebugUtil.debug("SetPOVToParentPOV "+ e.getMessage());
                    }
                    finally{
                        ContextUtil.popContext(context);
                    }
                    DomainObject domParentObject = DomainObject.newInstance(context);
                    String personId = "";

                       try {
                        personId = PersonUtil.getPersonObjectID(context);
                       }
                       catch (Exception e){
                          // we fail the above call to getPersonObjectID becasue meetings already set context to UserAgent
                          // we only need the personID for Documents so just ignore the error
                       }
                    for (Iterator itrParent = mlParent.iterator(); itrParent.hasNext();)
                    {
                        mapParent = (Map) itrParent.next();
                        if(mapParent != null)
                        {
                            strParentid = (String)mapParent.get(DomainConstants.SELECT_ID);
                            domParentObject.setId(strParentid);
                            String isProjectSpace = (String)mapParent.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
                            String isProjectConcept = (String)mapParent.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
                            String isProjectTemplate = (String)mapParent.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);

                            // if in the context of PS, PC or PT then restamp
                            if("TRUE".equalsIgnoreCase(isProjectSpace) || "TRUE".equalsIgnoreCase(isProjectConcept) || "TRUE".equalsIgnoreCase(isProjectTemplate))
                            {
                                /* give the owner of the Document "full" access before restamping it and *"Leader" access if it is a "Reuirement Specification" Type of *Document,as "Leader" is access bit in DomainAccess xml for "Req Spec"*/
                               accessToDocTypeOwner = bisToProxyPolicy.equalsIgnoreCase(requirementSpecPolicy)?"Leader":"Full";

                                DomainAccess.createObjectOwnership(context, toId, personId, accessToDocTypeOwner, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                                //by cyl 20250715 to端为文档的不去修改组织合作区
//                                toObj.setPrimaryOwnership(context, strFromProject,strFromOrganization);
                                break;
                            }
                        }
                    }
                }
            } else if(DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2.equalsIgnoreCase(relType) ||         DomainConstants.RELATIONSHIP_TASK_DELIVERABLE.equalsIgnoreCase(relType) || RELATIONSHIP_REFERENCE_DOCUMENT.equalsIgnoreCase(relType)){
                //Do Nothing. No need to re-stamp.
            }
            // if an Issue DON'T restamp IR-712540
            else if (bisToIssueType.equalsIgnoreCase("true"))
            {
                //Do Nothing. No need to re-stamp.
            }
            // if an a Meeting make sure we only blank if created in Project Space or Project Concept of Task Management
            else if (bisToMeetingType.equalsIgnoreCase("true"))
            {
               if (bisFromProjectSpaceType.equalsIgnoreCase("true") || bisFromProjectConceptType.equalsIgnoreCase("true") || bisFromTaskManagementType.equalsIgnoreCase("true"))
               {
                   toObj.setPrimaryOwnership(context, strFromProject,strFromOrganization);
               }
            }
            // only change POV if Folder is in context of PS or PC or PT
            else if (bisToWorkspaceVaultType.equalsIgnoreCase("true"))
            {
                String strRelatioshipPattern = DomainConstants.RELATIONSHIP_PROJECT_VAULTS + "," + DomainConstants.RELATIONSHIP_SUB_VAULTS;

                try{
                    ContextUtil.pushContext(context);
                    // let only restanp if this Document is getting created in the context of a PS, PC or PT
                    mlParent = toObj.getRelatedObjects(context,     // context.
                            strRelatioshipPattern,                              // rel filter.
                            DomainConstants.QUERY_WILDCARD,                     // type filter
                            slBusSelect,                                        // business object selectables.
                            null,                                               // relationship selectables.
                            true,                                               // expand to direction.
                            false,                                              // expand from direction.
                            (short) 0,                                          // level
                            null,                                               // object where clause
                            null,                                               // relationship where clause
                            0);                                                 //limit
                } catch(FrameworkException e){
//                    e.printStackTrace();   
                    DebugUtil.debug("SetPOVToParentPOV 2"+ e.getMessage());
                }finally{
                    ContextUtil.popContext(context);
                }
                DomainObject domParentObject = DomainObject.newInstance(context);
                for (Iterator itrParent = mlParent.iterator(); itrParent.hasNext();)
                {
                    mapParent = (Map) itrParent.next();
                    if(mapParent != null)
                    {
                        strParentid = (String)mapParent.get(DomainConstants.SELECT_ID);
                        domParentObject.setId(strParentid);

                        String isProjectSpace = (String)mapParent.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
                        String isProjectConcept = (String)mapParent.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
                        String isProjectTemplate = (String)mapParent.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);

                        // if in the context of PS, PC or PT then restamp
                        if("TRUE".equalsIgnoreCase(isProjectSpace) || "TRUE".equalsIgnoreCase(isProjectConcept) || "TRUE".equalsIgnoreCase(isProjectTemplate))
                        {
                            toObj.setPrimaryOwnership(context, strFromProject,strFromOrganization);
                            break;
                        }
                    }
                }
            }
            // if an Decision make sure we only blank if created in Project Space or Project Concept of Task Management
            else if (bisToDecisionType.equalsIgnoreCase("true"))
            {
               if (bisFromProjectSpaceType.equalsIgnoreCase("true") || bisFromProjectConceptType.equalsIgnoreCase("true") || bisFromTaskManagementType.equalsIgnoreCase("true"))
               {
                   // check RPE variable to see if we are coming from a create and connect and not just a connect
                   String allowPOVStamping = PropertyUtil.getRPEValue(context, "MX_ALLOW_POV_STAMPING", false);
                   if(allowPOVStamping!= null && "true".equals(allowPOVStamping)) // if we are creating this new Decision restamp
                   {
                       toObj.setPrimaryOwnership(context, strFromProject,strFromOrganization);
                   }
               }
            }
            // if a Discussion make sure we only blank if created in Project Space or Project Concept of Task Management
            else if (bisToDiscussionType.equalsIgnoreCase("true"))
            {
               if (bisFromProjectSpaceType.equalsIgnoreCase("true") || bisFromProjectConceptType.equalsIgnoreCase("true") || bisFromTaskManagementType.equalsIgnoreCase("true"))
               {
                    toObj.setPrimaryOwnership(context, strFromProject,strFromOrganization);
               }
            }
            else if (bisToMessageType.equalsIgnoreCase("true"))
            {
                String strRelatioshipPattern = DomainConstants.RELATIONSHIP_MESSAGE + "," + DomainConstants.RELATIONSHIP_THREAD;

                try{
                    ContextUtil.pushContext(context);
                    // let only restanp if this Document is getting created in the context of a PS, PC or PT
                    mlParent = toObj.getRelatedObjects(context,     // context.
                            strRelatioshipPattern,                              // rel filter.
                            DomainConstants.QUERY_WILDCARD,                     // type filter
                            slBusSelect,                                        // business object selectables.
                            null,                                               // relationship selectables.
                            true,                                               // expand to direction.
                            false,                                              // expand from direction.
                            (short) 0,                                          // level
                            null,                                               // object where clause
                            null,                                               // relationship where clause
                            0);                                                 //limit
                } catch(FrameworkException e){
//                    e.printStackTrace();   
                    DebugUtil.debug("SetPOVToParentPOV 3"+ e.getMessage());

                }finally{
                    ContextUtil.popContext(context);
                }
                DomainObject domParentObject = DomainObject.newInstance(context);
                String personId = "";


                   try {
                    personId = PersonUtil.getPersonObjectID(context);
                   }
                   catch (Exception e){
                      // we fail the above call to getPersonObjectID becasue meetings already set context to UserAgent
                      // we only need the personID for Documents so just ignore the error
                   }
                for (Iterator itrParent = mlParent.iterator(); itrParent.hasNext();)
                {
                    mapParent = (Map) itrParent.next();
                    if(mapParent != null)
                    {
                        strParentid = (String)mapParent.get(DomainConstants.SELECT_ID);
                        domParentObject.setId(strParentid);

                        String isProjectSpace = (String)mapParent.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
                        String isProjectConcept = (String)mapParent.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
                        String isProjectTemplate = (String)mapParent.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);

                        // if in the context of PS, PC or PT then restamp
                        if("TRUE".equalsIgnoreCase(isProjectSpace) || "TRUE".equalsIgnoreCase(isProjectConcept) || "TRUE".equalsIgnoreCase(isProjectTemplate))
                        {
                            // give the owner of the Message full access before restamping it
                            DomainAccess.createObjectOwnership(context, toId, personId, "Full", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                            toObj.setPrimaryOwnership(context, strFromProject,strFromOrganization);
                            break;
                        }
                    }
                }
            }
            else
            {
                if(bisToProjectSpaceType.equalsIgnoreCase("true")){
                String hasModifyAcc = (String) mpObjectInfo.get(DomainConstants.SELECT_HAS_MODIFY_ACCESS);
                if(Boolean.valueOf(hasModifyAcc)){
                toObj.setPrimaryOwnership(context, strFromProject,strFromOrganization);
            }
                }else{
                    toObj.setPrimaryOwnership(context, strFromProject,strFromOrganization);
                }
            }
         }
      }catch(Exception e){
         throw e;
      }
      finally
      {
          PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", "false", true); // clear out the RPE variable
      }
    }
     return true;
   }

    /**
     * Returns Task Assignee full names. This is an indexing method.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public String getFullName(Context context, String[] args) throws Exception {
        StringList fullNames = new StringList();
        String fullName = DomainConstants.EMPTY_STRING;
        String usernames = args[0];
        char VALUE_SEPARATOR_CHAR = 0x07;
        StringList slUsernames = FrameworkUtil.split(usernames, String.valueOf(VALUE_SEPARATOR_CHAR));

        for (Iterator iterator = slUsernames.iterator(); iterator.hasNext();) {
            String username = (String) iterator.next();
            fullName = PersonUtil.getFullName(context, username);
            fullNames.add(fullName);
        }
        String result = FrameworkUtil.join(fullNames, String.valueOf(VALUE_SEPARATOR_CHAR));
        return result;
    }

    /**
     * Returns Task co-owners full names. This is an indexing method.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public String getCoOwners(Context context, String[] args) throws Exception {
        StringList fullNames = new StringList();
        String ownerships = args[0];
        char VALUE_SEPARATOR_CHAR = 0x07;
        StringList ownershipsList = FrameworkUtil.split(ownerships, String.valueOf(VALUE_SEPARATOR_CHAR));

        for (String ownership : ownershipsList) {
            if (ownership.endsWith("|co-owner")) {
                StringList tokens = FrameworkUtil.split(ownership, "|");
                String username = tokens.get(1);
                username = username.substring(0, username.length() -4); //removing _PRJ
                String fullName = PersonUtil.getFullName(context, username);
                fullNames.add(fullName);
            }
        }

        String result = FrameworkUtil.join(fullNames, String.valueOf(VALUE_SEPARATOR_CHAR));
        return result;
    }

    /**
     * This method is called from trigger "AssigneeNotification" to send email to an assignee of a task.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args
     * @return void
     * @throws Exception if the operation fails
     */

    public void sendTaskMail(Context context, String[] args) throws Exception {
        String sCommandStatement = "get env $1";

        String iEvent = MqlUtil.mqlCommand(context, sCommandStatement, "EVENT");
        iEvent = iEvent.toLowerCase();
        String iTaskObjectId = MqlUtil.mqlCommand(context, sCommandStatement, "TOOBJECTID");
        // Assignee Information
        String sAssigneeName = MqlUtil.mqlCommand(context, sCommandStatement, "FROMNAME");
        StringList iAssigneeList = new StringList(sAssigneeName);

        HashMap argMap = new HashMap();
        argMap.put(ProgramCentralConstants.ASSIGNEE_LIST, iAssigneeList);
        argMap.put(ProgramCentralConstants.TASK_OBJECTID, iTaskObjectId);
        argMap.put(ProgramCentralConstants.EVENT, iEvent);

        String[] argsList = JPO.packArgs(argMap);
        this.sendTaskMailHelper(context, argsList);
    }

    /**
     * This method is used to get task information
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sObjectId: task object id
     * @param isRisk: is kind of Risk or not
     * @return Map object: task name, type, startdate, enddate, duration, duration units, criticality,
     *                      physicalid, summary, policy, all states, projectid, project name, task description, context name
     * @throws Exception if the operation fails
     */

    protected Map getObjectMap(Context context, String sObjectId, String isRiskManagement) throws Exception {


        StringList selectList = new StringList();
        selectList.add(DomainConstants.SELECT_NAME);
        selectList.add(DomainConstants.SELECT_TYPE);
        selectList.add(DomainConstants.SELECT_CURRENT);
        selectList.add(DomainConstants.SELECT_POLICY);
        selectList.add(ProgramCentralConstants.SELECT_PHYSICALID);
        selectList.add(ProgramCentralConstants.SELECT_TASK_STATES);

        if("False".equalsIgnoreCase(isRiskManagement)){

        selectList.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
        selectList.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
        selectList.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_DURATION);
        selectList.add(ProgramCentralConstants.SELECT_TASK_ESTIMATED_DURATION_INPUTUNIT);
        selectList.add(ProgramCentralConstants.SELECT_CRITICAL_TASK);
        selectList.add(ProgramCentralConstants.SELECT_SUMMARY);
        selectList.add(ProgramCentralConstants.SELECT_PROJECT_ID);
        selectList.add(ProgramCentralConstants.SELECT_PROJECT_NAME);
        selectList.add(ProgramCentralConstants.SELECT_TASK_DESCRIPTION);
        selectList.add(ProgramCentralConstants.SELECT_CONTEXT_NAME);
        selectList.add(ProgramCentralConstants.SELECT_PROJECT_REVISION);
        selectList.add(ProgramCentralConstants.SELECT_PROJECT_TYPE);
        }else{
            selectList.add("attribute[Estimated Start Date].value");
            selectList.add("attribute[Estimated End Date].value");
        }

        DomainObject dmObject = new DomainObject(sObjectId);
        return dmObject.getInfo(context, selectList);
    }

    protected static String getURL_Simulia(Context context, String[] args) {
        try {
         String sPhysicalId = args[0];
         String sLanguage = args[1];
         String sURLString = "";

            sURLString = FoundationUtil.generate3DDashboardURL(context, "SIMTRCP_AP", sPhysicalId);
            return sURLString;
        } catch (Exception ex) {
//            ex.printStackTrace();   
            DebugUtil.debug("getObjectMap "+ ex.getMessage());
            System.out.println(ex.getMessage());
        }
        return null;
    }


    // expects assigneename list, event and objectid of task
    public void sendTaskMailHelper(Context context, String[] args) throws Exception {
        boolean contextPushed = false;
        try {
            Map argsMap = (HashMap) JPO.unpackArgs(args);
            StringList assigneeList = (StringList) argsMap.get(ProgramCentralConstants.ASSIGNEE_LIST);
            String sObjectId = (String) argsMap.get(ProgramCentralConstants.TASK_OBJECTID);
            String sEvent = (String) argsMap.get(ProgramCentralConstants.EVENT);
            String sAssignedOrUnAssignedBy = PersonUtil.getFullName(context, context.getUser());
            if(sEvent != null && sEvent.equalsIgnoreCase("complete")){
                ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                contextPushed = true;
            }

            DomainObject dmoObject = new DomainObject(sObjectId);
            StringList selectable = new StringList(2);
            selectable.add(ProgramCentralConstants.SELECT_IS_RISK);
            selectable.add(ProgramCentralConstants.SELECT_IS_KINDOF_RISKMANAGEMENT);
            Map objInfo = dmoObject.getInfo(context, selectable);
            String isRisk =(String)objInfo.get(ProgramCentralConstants.SELECT_IS_RISK);
            String isRiskManagement = (String) objInfo.get(ProgramCentralConstants.SELECT_IS_KINDOF_RISKMANAGEMENT);

            // get task map object will info: task name, type, startdate, enddate, duration, duration units, criticality,
            // physicalid, summary, policy, all states, projectid, project name, task description, context name
            Map objectMap = getObjectMap(context, sObjectId, isRiskManagement);
            // read the object map
            String sPolicy = (String) objectMap.get(DomainConstants.SELECT_POLICY);
            // Get the Assign State Name.
            String sCommandStatement = "print policy $1 select $2 dump";
            String sAssignPropVal = MqlUtil.mqlCommand(context, sCommandStatement, sPolicy, "property[state_Assign].value");

            StringList lStates = (StringList) objectMap.get("state");
            String sTaskCurrent = (String) objectMap.get(DomainConstants.SELECT_CURRENT);

            // Get Parents state position.
            int iAssignPos = lStates.indexOf(sAssignPropVal);
            // get index of current task pos.
            int iTaskPos = lStates.indexOf(sTaskCurrent);

            // RULE-1: if the task is not assigned or at later stage then no email will be sent.
            if (iTaskPos < iAssignPos) {
                return;
            }

            final String sTaskName = (String) objectMap.get(DomainConstants.SELECT_NAME);
            String sTaskType = (String) objectMap.get(DomainConstants.SELECT_TYPE);

            boolean isVVTest = false;
            if ("vv_test_execution".equalsIgnoreCase(sTaskType)) {
                isVVTest = true;
            }


            String sTaskEstStartDate = DomainConstants.EMPTY_STRING;
            String sTaskEstFinishDate = DomainConstants.EMPTY_STRING;
            String sTaskEstDuration = DomainConstants.EMPTY_STRING;
            String sTaskEstDurationUnit = DomainConstants.EMPTY_STRING;
            String sCritikTask = DomainConstants.EMPTY_STRING;
            String sSummary = DomainConstants.EMPTY_STRING;
            String sPrjId = DomainConstants.EMPTY_STRING;
            String sPrjName = DomainConstants.EMPTY_STRING;
            String sPrjNameEncoded =DomainConstants.EMPTY_STRING;
            String sDescription = DomainConstants.EMPTY_STRING;
            String sContextName = DomainConstants.EMPTY_STRING;
            String sPrjRev = DomainConstants.EMPTY_STRING;
            String sPrjType = DomainConstants.EMPTY_STRING;

            if("False".equalsIgnoreCase(isRiskManagement)){
            //For Task
             sTaskEstStartDate = (String) objectMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
             sTaskEstFinishDate = (String) objectMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

             sTaskEstDuration = (String) objectMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_DURATION);
              sTaskEstDurationUnit = (String) objectMap.get(ProgramCentralConstants.SELECT_TASK_ESTIMATED_DURATION_INPUTUNIT);
            if("h".equalsIgnoreCase(sTaskEstDurationUnit)){
                double duration = Task.parseToDouble(sTaskEstDuration);
                duration  = duration * 8.0;
                sTaskEstDuration = Double.toString(duration);
            }
              sCritikTask = (String) objectMap.get(ProgramCentralConstants.SELECT_CRITICAL_TASK);
              sSummary = (String) objectMap.get(ProgramCentralConstants.SELECT_SUMMARY);
              sPrjId = (String) objectMap.get(ProgramCentralConstants.SELECT_PROJECT_ID);
              sPrjName = (String) objectMap.get(ProgramCentralConstants.SELECT_PROJECT_NAME);
              sDescription = (String) objectMap.get(ProgramCentralConstants.SELECT_TASK_DESCRIPTION);
              sContextName = (String) objectMap.get(ProgramCentralConstants.SELECT_CONTEXT_NAME);
              sPrjRev = (String) objectMap.get(ProgramCentralConstants.SELECT_PROJECT_REVISION);
              sPrjType = (String) objectMap.get(ProgramCentralConstants.SELECT_PROJECT_TYPE);

            // RULE-2: if Type Part Quality Plan found then no email sent.

            // Find all business objects related to a task, send notification if connected to any PMC type
            StringList sList = new StringList();
            sList.add(DomainConstants.SELECT_TYPE);

              MapList mlProjectList = dmoObject.getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_SUBTASK,
                    null,
                    sList,
                    null,       // relationshipSelects
                    true,       // getTo
                    false,      // getFrom
                    (short) 0,  // recurseToLevel
                    null,       // objectWhere
                    null);      // relationshipWhere

            for (int iProjectIndx = 0; iProjectIndx < mlProjectList.size(); iProjectIndx++) {
                Map map = (Map) mlProjectList.get(iProjectIndx);
                String type = (String) map.get(DomainConstants.SELECT_TYPE);
                if (type.equals(DomainConstants.TYPE_PART_QUALITY_PLAN)) {
                    return;
                }
            }
            }else{
                //for Risk
                sTaskEstStartDate = (String) objectMap.get("attribute[Estimated Start Date].value");
                sTaskEstFinishDate = (String) objectMap.get("attribute[Estimated End Date].value");
                Risk risk = (Risk)DomainObject.newInstance(context,DomainConstants.TYPE_RISK,DomainConstants.PROGRAM);
                risk.setId(sObjectId);
                sPrjName = (String)risk.getAffectedItemsNames(context,true);
            }

            // For Both
            String sPhysicalId = (String) objectMap.get(ProgramCentralConstants.SELECT_PHYSICALID);

            // Set the notification subject
            String sSubjectKey = null;
            String sEmailContentTitle = null;
            String sAssignedOrUnAssignedResourceString = null;
            if (sEvent != null && sEvent.equalsIgnoreCase("create")) {
                if("False".equalsIgnoreCase(isRiskManagement)){
                sSubjectKey = SUBJECT_STRING_RESOURCE;
                }else{
                    if("True".equalsIgnoreCase(isRisk)){
                        sSubjectKey = SUBJECT_STRING_RESOURCE_RISK;}
                    else{
                        sSubjectKey = SUBJECT_STRING_RESOURCE_OPPORTUNITY;
                    }
                }

                if (sCritikTask.equalsIgnoreCase("TRUE")) {
                    sEmailContentTitle = CRITICAL_ASSIGN_MESSAGE_STRING_RESOURCE;
                } else {
                    sEmailContentTitle = ASSIGN_MESSAGE_STRING_RESOURCE;
                }
                sAssignedOrUnAssignedResourceString = ASSIGNED_BY_STRING_RESOURCE;
            } else if("complete".equalsIgnoreCase(sEvent)){
                sSubjectKey = SUBJECT_STRING_COMPLETE;
                sEmailContentTitle = MESSAGE__COMPLETION_STRING;
                sAssignedOrUnAssignedResourceString = ASSIGNED_TO_STRING_RESOURCE;
            } else {
                sSubjectKey = REVOKE_SUBJECT_STRING_RESOURCE;
                sAssignedOrUnAssignedResourceString = UNASSIGNED_BY_STRING_RESOURCE;
            }
            Set<String> assigneeNameSet = new HashSet<String>();
            for (int iAssigneeListIndx = 0; iAssigneeListIndx < assigneeList.size(); iAssigneeListIndx++) {

                String sAssigneeName = (String) assigneeList.get(iAssigneeListIndx);

                String mqlResult = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 select $4 dump", "*", sAssigneeName, "*","type");
                System.out.println("sAssigneeName... "+sAssigneeName);
                System.out.println("mqlResult... "+mqlResult);
                if(ProgramCentralUtil.isNotNullString(mqlResult)){

                    String[] resultArray = mqlResult.split(",");
                    if(resultArray.length >=3 && DomainConstants.TYPE_PERSON.equalsIgnoreCase(resultArray[0])) {

                // RULE 3: Don't send any notification if user preferences are set to false for notification (or person is inactive)
                if(!this.isNotify(context, sAssigneeName)) {
                    continue;
                }
                        assigneeNameSet.add(sAssigneeName);
                } else {
                        //If UserGroup is assigned or unassigned, iterate over the members and send notification.
                        String userGroupInfo =
                                MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 select $4 dump", "*", sAssigneeName, "*","physicalid");

                        String[] userGroupInfoArray = userGroupInfo.split(",");
                        if(resultArray.length >=4) {
                            String userGrouphysicalId = userGroupInfoArray[3];
                            List<Map<String, String>> userGroupMemberMapList = ProgramCentralUtil.getUserGroupMemberList(context,userGrouphysicalId);

                            for(Map<String,String> userGroupMemberMap : userGroupMemberMapList) {

                                sAssigneeName = userGroupMemberMap.get("name");
                                assigneeNameSet.add(sAssigneeName);


                            }
                        }
                    }
                }
            }
            if(assigneeNameSet != null && !assigneeNameSet.isEmpty()) {
                System.out.println("assigneeNameSet.size()... " + assigneeNameSet.size());
                for( String  sAssigneeName : assigneeNameSet) {
                    System.out.println("Email to sAssigneeName... " + sAssigneeName);
                                sendNotificationAssignee(context, sAssigneeName, contextPushed, isRiskManagement, sTaskEstStartDate,
                                        sTaskEstFinishDate, sTaskEstDuration, sTaskEstDurationUnit,
                            sPrjType, sPrjName, sPrjRev, sSubjectKey, sEmailContentTitle, isVVTest,
                                        isRiskManagement, sEvent, sAssignedOrUnAssignedBy,
                                        sAssignedOrUnAssignedResourceString, sPrjId, sPrjNameEncoded,
                                        sContextName, sDescription, sPhysicalId, sTaskName, sObjectId, sSummary, sTaskType);
                            }
                        }
        } catch (Exception ex) {
//            ex.printStackTrace();   
            DebugUtil.debug("sendTaskMailHelper "+ ex.getMessage());
            throw ex;
        }finally{
            if(contextPushed){
                ContextUtil.popContext(context);
                contextPushed=false;
            }
        }
    }
    private void sendNotificationAssignee(Context context,String sAssigneeName,
            boolean contextPushed,String isRiskManagement,
            String sTaskEstStartDate, String sTaskEstFinishDate, String sTaskEstDuration,
            String sTaskEstDurationUnit,
            String sPrjType, String sPrjName, String sPrjRev,
            String sSubjectKey,String sEmailContentTitle,
            boolean isVVTest,String isRisk, String sEvent,
            String sAssignedOrUnAssignedBy,String sAssignedOrUnAssignedResourceString,
            String sPrjId,String sPrjNameEncoded,String sContextName,
            String sDescription,String sPhysicalId, String sTaskName,
            String sObjectId,String sSummary, String sTaskType) throws Exception {

        System.out.println("sendNotificationAssignee... "+sAssigneeName);

                boolean hasAccess = false;
                ContextUtil.pushContext(context, sAssigneeName,DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                contextPushed = true;

                if("False".equalsIgnoreCase(isRiskManagement)){
                    //need to change type from below query for Risk Affected Items, it can be Task as well.
                    String output = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 select $4", sPrjType, sPrjName, sPrjRev, "current.access");
                    if(ProgramCentralUtil.isNotNullString(output)){
                        hasAccess = true;
                    }
                }else{
                    hasAccess = true;
                }

                String language = PropertyUtil.getAdminProperty(context, PersonUtil.personAdminType, sAssigneeName, PersonUtil.PREFERENCE_ICON_MAIL_LANGUAGE);
                Locale sAssigneeLocale = new Locale(language);
                sTaskEstStartDate = this.dateProcessing(context, sTaskEstStartDate,sAssigneeLocale);
                sTaskEstFinishDate = this.dateProcessing(context, sTaskEstFinishDate,sAssigneeLocale);
                //get message expects: String messageKey, String[] messageKeys, String[] messageValues, String companyName
                String sSubject = ProgramCentralConstants.EMPTY_STRING;
                if("False".equalsIgnoreCase(isRiskManagement)){
                    String sKey[] = {"ProjectName"};
                    String sValue[] = {sPrjName};
                    if(ProgramCentralUtil.isNullString(sPrjName)){
                sValue[0] = sTaskName;    // In case of collab task "ProjectName" is null hence replaced it by TaskName
                    }
                    sSubject = this.getMessage(context, sSubjectKey, sKey, sValue, null,sAssigneeLocale);
                }else{
                    sSubject = this.getMessage(context, sSubjectKey, null, null, null, sAssigneeLocale);
                }
                String sDurationPropertyKey = DIMENSION_STRING_RESOURCE + "." + sTaskEstDurationUnit;
                String sTaskEstDurationUnitResource = EnoviaResourceBundle.getProperty(context, "Framework", sDurationPropertyKey, sAssigneeLocale);

                // getting the units(days/hours) for duration, if we dont have sTaskEstDurationUnitResource then unit be whatever it is in backend
                if (sTaskEstDurationUnitResource.equals(sDurationPropertyKey)) {
                    sTaskEstDurationUnitResource = sTaskEstDurationUnit;
                }

                String sNotificationTextHtml = this.getMessage(context, sEmailContentTitle, null, null, null, sAssigneeLocale);
                String sNotificationText ="";

                //For Task/Risk Name hyperlink
                if(!REVOKE_SUBJECT_STRING_RESOURCE.equalsIgnoreCase(sSubjectKey)){
                    String objeNameURL = NotificationUtil.appendBaseUrl(context, new StringList(sObjectId), "");
                    if (isVVTest) {
                        String[] sArgs = { sPhysicalId, language };
                        objeNameURL = getURL_Simulia(context, sArgs);
//                      System.out.println("Simulia URL: " + objeNameURL);
                    }
                objeNameURL =objeNameURL.replace("\n", "");

                if(context.getUser() != PropertyUtil.getSchemaProperty(context, "person_UserAgent")){
                sTaskName ="<a href="+XSSUtil.encodeForHTML(context, objeNameURL)+" target=\"_blank\" >" + sTaskName +"</a>";
                }else{
                    if(contextPushed){
                    ContextUtil.popContext(context);
                        contextPushed = false;
                    }
                sTaskName ="<a href="+XSSUtil.encodeForHTML(context, objeNameURL)+" target=\"_blank\" >" + sTaskName +"</a>";
                    ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                    contextPushed = true;
                }
                }
                String[] messageKeysForTskLabel = { "TaskName" };
                String[] messageValuesForTskLabel = { sTaskName };
                String sTaskLabel = DomainConstants.EMPTY_STRING;
                String parentLabel = DomainConstants.EMPTY_STRING;

                if("False".equalsIgnoreCase(isRiskManagement)){
                    if (isVVTest) {
                        sTaskLabel= this.getMessage(context, VVTASK_LABEL_STRING_RESOURCE, messageKeysForTskLabel, messageValuesForTskLabel, null, sAssigneeLocale);
                        parentLabel = PROJECT_LABEL_STRING_RESOURCE;
                    } else {
                    sTaskLabel= this.getMessage(context, TASK_LABEL_STRING_RESOURCE, messageKeysForTskLabel, messageValuesForTskLabel, null, sAssigneeLocale);
                    parentLabel = PROJECT_LABEL_STRING_RESOURCE;
                    }
                }else{
                    if("True".equalsIgnoreCase(isRisk)){
                        sTaskLabel= this.getMessage(context, TASK_LABEL_STRING_RESOURCE_RISK, messageKeysForTskLabel, messageValuesForTskLabel, null, sAssigneeLocale);}
                    else{
                        sTaskLabel= this.getMessage(context, TASK_LABEL_STRING_RESOURCE_OPPORTUNITY, messageKeysForTskLabel, messageValuesForTskLabel, null, sAssigneeLocale);}
                    parentLabel = AFFECTED_ITEM_LABEL_STRING_RESOURCE;
                }
                sNotificationTextHtml = sNotificationTextHtml + "<br><br>" + sTaskLabel;
                sNotificationTextHtml = sNotificationTextHtml + "<br><br> ";

                if (sEvent != null && sEvent.equalsIgnoreCase("create") || sEvent.equalsIgnoreCase("complete")) {

                    if (sTaskEstDuration != null && !sTaskEstDuration.isEmpty()) {
                        String[] messageKeysForDuration = { "TaskEstDuration", "TaskEstDurationUnit" };
                        String[] messageValuesForDuration = { sTaskEstDuration, sTaskEstDurationUnitResource };
                        String sDurationMessage = this.getMessage(context, DURATION_STRING_RESOURCE, messageKeysForDuration, messageValuesForDuration, null, sAssigneeLocale);
                        sNotificationTextHtml = sNotificationTextHtml + "<br><br>" + sDurationMessage;
                    }
                    if (sTaskEstStartDate != null && !sTaskEstStartDate.isEmpty()) {
                        String[] messageKeysForStartDate = { "TaskEstStartDate" };
                        String[] messageValuesForStartDate = { sTaskEstStartDate };
                        String startDateMessage = this.getMessage(context, STARTDATE_STRING_RESOURCE, messageKeysForStartDate, messageValuesForStartDate, null, sAssigneeLocale);
                        sNotificationTextHtml = sNotificationTextHtml + "<br><br>" + startDateMessage;
                    }
                    if (sTaskEstFinishDate != null && !sTaskEstFinishDate.isEmpty()) {
                        String[] messageKeysForFinishDate = { "TaskEstFinishDate" };
                        String[] messageValuesForFinishDate = { sTaskEstFinishDate };
                        String finishDateMessage = this.getMessage(context, ENDDATE_STRING_RESOURCE, messageKeysForFinishDate, messageValuesForFinishDate, null, sAssigneeLocale);
                        sNotificationTextHtml = sNotificationTextHtml + "<br><br>" + finishDateMessage;
                    }
                }

                // add the assigner information
                if (sAssignedOrUnAssignedBy != null && !sAssignedOrUnAssignedBy.isEmpty()) {
                    String[] messageKeysForOwner = { "AssignedOrUnAssignedBy" };
                    String[] messageValuesForOwner = { sAssignedOrUnAssignedBy };
                    String sOwnerMessage = this.getMessage(context, sAssignedOrUnAssignedResourceString, messageKeysForOwner, messageValuesForOwner, null, sAssigneeLocale);
                    sNotificationTextHtml = sNotificationTextHtml + "<br><br>" + sOwnerMessage;
                }

                // Check if context/project availability
                if (sPrjName != null && !sPrjName.isEmpty()) {
                    if (sEvent != null && sEvent.equalsIgnoreCase("create") || sEvent.equalsIgnoreCase("complete")) {
                        if(hasAccess){
                            String sPrjNameURL = NotificationUtil.appendBaseUrl(context, new StringList(sPrjId), "");
                            sPrjNameURL =sPrjNameURL.replace("\n", "");
                            if(context.getUser() != PropertyUtil.getSchemaProperty(context, "person_UserAgent")){
                                sPrjNameEncoded ="<a href="+XSSUtil.encodeForHTML(context, sPrjNameURL)+" target=\"_blank\" >" + sPrjName +"</a>";
                            }else{
                                if(contextPushed){
                                ContextUtil.popContext(context);
                                contextPushed = false;
                                }
                                sPrjNameEncoded ="<a href="+XSSUtil.encodeForHTML(context, sPrjNameURL)+" target=\"_blank\" >" + sPrjName +"</a>";
                                ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                                contextPushed = true;
                            }
                        }
                    }
                    String[] messageKeysForProject = { "ProjectName" };
                    sPrjNameEncoded = ProgramCentralUtil.isNotNullString(sPrjNameEncoded) ? sPrjNameEncoded :sPrjName ;
                    String[] messageValuesForProject = { sPrjNameEncoded };
                    String sProjectLabelMessage = this.getMessage(context, parentLabel, messageKeysForProject, messageValuesForProject, null, sAssigneeLocale);
                    sNotificationTextHtml = sNotificationTextHtml + "<br><br>" + sProjectLabelMessage;
                    sNotificationTextHtml = sNotificationTextHtml + "<br><br> ";

                } else if (sContextName != null && !sContextName.isEmpty()) {
                    String[] messageKeysForContext = { "ContextName" };
                    String[] messageValuesForContext = { sContextName };
                    String sContextLabelMessage = this.getMessage(context, CONTEXT_LABEL_STRING_RESOURCE, messageKeysForContext, messageValuesForContext, null, sAssigneeLocale);
                    sNotificationTextHtml = sNotificationTextHtml + "<br><br>" + sContextLabelMessage;
                }
                // add task description to the message
                if (sDescription != null && !sDescription.isEmpty()) {
                    String[] messageKeysForDescription = { "Description" };
                    String[] messageValuesForDescription = { sDescription };
                    String sDescriptionMessage = this.getMessage(context, DESCRIPTION_STRING_RESOURCE, messageKeysForDescription, messageValuesForDescription, null, sAssigneeLocale);
                    sNotificationTextHtml = sNotificationTextHtml + "<br><br>" + sDescriptionMessage;
                }
                //Donot remove space in "<br><br> " else it will break icon mail message.
                sNotificationText = sNotificationTextHtml+"<br><br> ";
                String[] sArgsArr = { sPhysicalId, language };

                String s3DDURLString = this.get3DDashboardURLwithHTMLFormatting(context, sArgsArr);

                // send 3DDashboard url in all cases
                if (s3DDURLString != null && !s3DDURLString.isEmpty() && sEvent != null && sEvent.equalsIgnoreCase("create") && sSummary.equalsIgnoreCase("false") && sTaskType.equalsIgnoreCase(DomainConstants.TYPE_TASK)) {
                    sNotificationTextHtml = sNotificationTextHtml + s3DDURLString;
                }

            String sEnoviaURLString = this.getEnoviaURLwithHTMLFormatting(context, sArgsArr);
                // send enovia url only if there is project attached
                if (sEnoviaURLString != null && !sEnoviaURLString.isEmpty() && ProgramCentralUtil.isNotNullString(sPrjName) && sEvent != null && sEvent.equalsIgnoreCase("create") || sEvent.equalsIgnoreCase("complete")) {
                    sNotificationTextHtml = sNotificationTextHtml + sEnoviaURLString;
                }

                try {
                   if(REVOKE_SUBJECT_STRING_RESOURCE.equalsIgnoreCase(sSubjectKey)){
                        if(ProgramCentralUtil.isNotNullString(sPrjName)) {
                        String[] mKeys = {"TaskName","sAssignedOrUnAssignedBy","ProjectName"};
                        String[] mValues = {sTaskName,sAssignedOrUnAssignedBy,sPrjName};
                        sNotificationText= this.getMessage(context, REVOKE_MESSAGE__STRING_RESOURCE , mKeys, mValues, null, sAssigneeLocale);
                        }else {
                            String[] mKeys = {"TaskName","sAssignedOrUnAssignedBy"};
                            String[] mValues = {sTaskName,sAssignedOrUnAssignedBy};
                            sNotificationText= this.getMessage(context, COLLAB_TASK_REVOKE_MESSAGE__STRING_RESOURCE , mKeys, mValues, null, sAssigneeLocale);
                        }

                        sNotificationTextHtml = sNotificationText;
                    }else {
                        // URL generation
                        String URL = NotificationUtil.appendBaseUrl(context, new StringList(sObjectId), "");
                        URL =URL.replace("\n", "");
                        sNotificationText =sNotificationText+URL;
                        if(contextPushed){
                            ContextUtil.popContext(context);
                            contextPushed=false;
                        }
                    }
                    //send Email notification & iconMail notification
                    NotificationUtil.sendNotification(context, new StringList(sAssigneeName), null, null, sSubject, sNotificationText,sNotificationTextHtml, null,false);

                } catch (Exception ex) {
                    DebugUtil.debug("Error creating mail object for " + sAssigneeName);
                }
    }

    protected boolean isNotify(Context context, String sAssigneeName) throws FrameworkException {
        String sCommandStatement = "print person $1 select $2 $3 dump";
        String sResult = MqlUtil.mqlCommand(context, sCommandStatement, sAssigneeName, "active", "property[preference_AssignedTaskNotificationOn].value");
        return (sResult.equalsIgnoreCase("true") || sResult.equalsIgnoreCase("true,true"));
    }

    protected String dateProcessing(Context context, String date, Locale locale) throws Exception {
        String returnString = ProgramCentralConstants.EMPTY_STRING;
        TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
        double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
        double clientTZOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();
        int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
        DateFormat format = DateFormat.getDateTimeInstance(iDateFormat, iDateFormat, locale);
        returnString                       = eMatrixDateFormat.getFormattedDisplayDate(date, clientTZOffset,locale);
        return returnString;
    }

    public String getEnoviaURLwithHTMLFormatting(Context context, String[] args) {

        String sPhysicalId = args[0];
        String sLanguage = args[1];
        String sURLString = "";
        try {
            // Changed per call with RVW
            sURLString = com.dassault_systemes.enovia.dpm.notification.NotificationUtil.generateEnoviaURL(context, "ENOPRPR_AP", sPhysicalId);
          //sURLString = FoundationUtil.generateEnoviaURL(context, "ENOPRPR_AP", sPhysicalId);
            //sURLString = FoundationUtil.generateEnoviaURL(context, sPhysicalId);
            if (sURLString != null && !sURLString.isEmpty()) {
                String[] messageKeys = { "url" };
                String[] messageValues = { sURLString };
                return "<br><br>" + this.getMessage(context, ENOVIAURL_STRING_RESOURCE, messageKeys, messageValues, null, new Locale(sLanguage));
            } else {
                return sURLString;
            }
        } catch (Exception ex) {
//            ex.printStackTrace();   
            DebugUtil.debug("getEnoviaURLwithHTMLFormatting "+ ex.getMessage());
        }
        return null;
    }

    public String get3DDashboardURLwithHTMLFormatting(Context context, String[] args) {
        String sPhysicalId = args[0];
        String sLanguage = args[1];
        String sURLString = "";
        String collabTaskLicense = "ENOTASK_AP";
        try {
            // RVW: Comment out ENOTASP_AP logic for IR-1156528-3DEXPERIENCER2024x, as packaging change was never made
            //IR-956660-3DEXPERIENCER2022x: License change for Collaborative Task for on premise
            // if(!UINavigatorUtil.isCloud(context)) {
            //  collabTaskLicense = "ENOTASP_AP";
            // }
            sURLString = FoundationUtil.generate3DDashboardURL(context,collabTaskLicense, sPhysicalId);
            if (sURLString != null && !sURLString.isEmpty()) {
                if(!UINavigatorUtil.isCloud(context)) {
                    sURLString+="&x3dPlatformId=OnPremise";
                }
                String[] messageKeys = { "url" };
                String[] messageValues = { sURLString };
                return "<br><br>" + this.getMessage(context, DASHBOARDURL_STRING_RESOURCE, messageKeys, messageValues, null, new Locale(sLanguage));
            } else {
                return sURLString;
            }
        } catch (Exception ex) {
//            ex.printStackTrace();   
            DebugUtil.debug("get3DDashboardURLwithHTMLFormatting "+ ex.getMessage());
        }
        return null;
    }

    /**
     *
     * @param context
     * @param args
     * @return Project Ids which are already connected to Program Object and need to be excluded from Search results.
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getExcludeOIDForProjectSearchInProgram(Context context, String []args)throws Exception {

        StringList slExcludeId = new StringList();
        Map programMap                  = (HashMap) JPO.unpackArgs(args);
        String objectId                 = (String)programMap.get("objectId");

        DomainObject progObj = new DomainObject(objectId);
        StringList select = new StringList();
        select.add(DomainConstants.SELECT_ID);

        MapList projectList =  progObj.getRelatedObjects(
                context,                    // eMatrix context
                DomainRelationship.RELATIONSHIP_PROGRAM_PROJECT,    // relationship pattern
                DomainConstants.TYPE_PROJECT_SPACE,             // object pattern
                select,              // object selects
                null,                       // relationship selects
                false,                      // to direction
                true,                       // from direction
                (short) 1,                  // recursion level
                null,                // object where clause
                DomainConstants.EMPTY_STRING,0);              // relationship where clause

        Iterator itrProjectList = projectList.iterator();
        while(itrProjectList.hasNext()){
            Map projectMap = (Map)itrProjectList.next();
            slExcludeId.add((String) projectMap.get(DomainConstants.SELECT_ID));
        }
        return slExcludeId;
}

    /**
     * It returns Project, Project Template or Project Concept name with or without hyperlink
     * as per access of logged in user.
     * @param context the ENOVIA <code>Context</code> object.
     * @param args request arguments
     * @return projectName
     * @throws Exception if operation fails.
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public String getProjectName(Context context, String[] args) throws MatrixException {

        StringBuilder projectInfoBuilder = new StringBuilder();
        String prjName              = DomainObject.EMPTY_STRING;
        String projectId            = DomainObject.EMPTY_STRING;
        boolean hasAccess           = true;

        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) programMap.get("requestMap");
            String objectId = (String) requestMap.get("objectId");

            if(ProgramCentralUtil.isNotNullString(objectId)){

                String SELECT_TASK_PROJECT_ACCESS = ProgramCentralConstants.SELECT_PROJECT_OBJECT + ".current.access[read]";

                DomainObject taskObj = DomainObject.newInstance(context, objectId);
                String accessValue = taskObj.getInfo(context, SELECT_TASK_PROJECT_ACCESS);

                if(accessValue == null || accessValue.isEmpty()){
                    hasAccess = false;
                }


                try{
                    StringList selectList = new StringList(2);
                    selectList.add(ProgramCentralConstants.SELECT_PROJECT_NAME);
                    selectList.add(ProgramCentralConstants.SELECT_PROJECT_ID);

                    ProgramCentralUtil.pushUserContext(context);

                    Map<String,String> taskInfoMap = taskObj.getInfo(context, selectList);

                    prjName =  taskInfoMap.get(ProgramCentralConstants.SELECT_PROJECT_NAME);
                    projectId = taskInfoMap.get(ProgramCentralConstants.SELECT_PROJECT_ID);

                }finally{
                    ProgramCentralUtil.popUserContext(context);
                }

                if(hasAccess){
                    String url = "../common/emxNavigator.jsp?objectId="+projectId;
                    projectInfoBuilder.append("<a  href=\""+url);
                    projectInfoBuilder.append("\" target=\"_blank\" title=\""+prjName);
                    projectInfoBuilder.append("\">");
                    projectInfoBuilder.append(prjName);
                    projectInfoBuilder.append("</a>");
                }else{
                    projectInfoBuilder.append(prjName);
                }
            }

        } catch (Exception e) {
//            e.printStackTrace();   
            DebugUtil.debug("getProjectName "+ e.getMessage());
        }

        return projectInfoBuilder.toString();
    }

    /**
     *
     * @param context
     * @param args
     * @return
     * @throws MatrixException
     */

    static protected final String POLICY_PROXYITEM = "ProxyItem";
    static protected final String SELECT_PROXY_TENANT  = "attribute[ProxyItem.Proxy_Tenant].value";
    static protected final String SELECT_PROXY_SERVICE = "attribute[ProxyItem.Proxy_Service].value";
    static protected final String SELECT_PROXY_MODIFIED= "attribute[ProxyItem.Proxy_Modified].value";
    static protected final String SELECT_PROXY_STATE   = "attribute[ProxyItem.Proxy_State].value";
    static protected final String SELECT_PROXY_ID      = "attribute[ProxyItem.Proxy_Id].value";
    static protected final String SELECT_PROXY_TITLE   = "attribute[ProxyItem.Proxy_Title].value";
    static protected final String SELECT_PROXY_URL     = "attribute[ProxyItem.Proxy_URL].value";

    static protected final String SELECT_PHYSICALID = "physicalid";
    static protected final String SELECT_PROXYITEM  = "interface[ProxyItem]";


    @com.matrixone.apps.framework.ui.ProgramCallable
    public String getXPPProjectName(Context context, String[] args) throws MatrixException {

        StringBuilder projectInfoBuilder = new StringBuilder();
        String xppName          = DomainObject.EMPTY_STRING;
        String xppURL           = DomainObject.EMPTY_STRING;
        boolean hasAccess       = true;

        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) programMap.get("requestMap");
            String objectId = (String) requestMap.get("objectId");

            if(ProgramCentralUtil.isNotNullString(objectId)){

                String SELECT_TASK_PROJECT_ACCESS = ProgramCentralConstants.SELECT_PROJECT_OBJECT + ".current.access[read]";

                DomainObject taskObj = DomainObject.newInstance(context, objectId);
                String accessValue = taskObj.getInfo(context, SELECT_TASK_PROJECT_ACCESS);

                if(accessValue == null || accessValue.isEmpty()){
                    hasAccess = false;
                }


                try{
                    StringList selectList = new StringList(2);
                    selectList.add(SELECT_PROXY_URL);
                    selectList.add(SELECT_PROXY_TITLE);

                    ProgramCentralUtil.pushUserContext(context);

                    Map<String,String> taskInfoMap = taskObj.getInfo(context, selectList);

                    xppName =  taskInfoMap.get(SELECT_PROXY_TITLE);
                    xppURL = taskInfoMap.get(SELECT_PROXY_URL);

                }finally{
                    ProgramCentralUtil.popUserContext(context);
                }

            if (ProgramCentralUtil.isNotNullString(xppURL)) {

                    if(hasAccess){
                        projectInfoBuilder.append("<a  href=\""+ XSSUtil.encodeForURL(context, xppURL));
                        projectInfoBuilder.append("\" target=\"_blank\" title=\""+xppName);
                        projectInfoBuilder.append("\">");
                        projectInfoBuilder.append(xppName);
                        projectInfoBuilder.append("</a>");
                    }else{
                        projectInfoBuilder.append(xppName);
                    }
                }
            }


        } catch (Exception e) {
            System.out.println (e.getMessage());
//            e.printStackTrace();   
            DebugUtil.debug("getXPPProjectName "+ e.getMessage());
        }

        return projectInfoBuilder.toString();
    }

    /**
     * It returns Affected Items names with or without hyperlink
     * as per access of logged in user.
     * @param context the ENOVIA <code>Context</code> object.
     * @param args request arguments
     * @return Affected Item name
     * @throws Exception if operation fails.
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public String getAffectedItem(Context context, String[] args) throws MatrixException {
        String affectedItems = DomainConstants.EMPTY_STRING;
         try {
             Map programMap = (Map) JPO.unpackArgs(args);
             Map requestMap = (Map) programMap.get("requestMap");
             String objectId = (String) requestMap.get("objectId");

            Risk risk = (Risk)DomainObject.newInstance(context,DomainConstants.TYPE_RISK,DomainConstants.PROGRAM);
            risk.setId(objectId);
            affectedItems = (String)risk.getAffectedItemsNames(context,false);
         }catch(Exception ex){
            throw new MatrixException(ex);
                }finally {
                    return affectedItems.toString();
                }
    }

    @com.matrixone.apps.framework.ui.ProgramCallable
    public String getContextItem(Context context, String[] args) throws MatrixException {
        String contextItems = DomainConstants.EMPTY_STRING;
         try {
             Map programMap = (Map) JPO.unpackArgs(args);
             Map requestMap = (Map) programMap.get("requestMap");
             String objectId = (String) requestMap.get("objectId");

            Risk risk = (Risk)DomainObject.newInstance(context,DomainConstants.TYPE_RISK,DomainConstants.PROGRAM);
            risk.setId(objectId);
//          contextItems = (String)risk.getAffectedItemsNames(context,false);
            contextItems = (String)risk.getContextItemsNames(context,false);
         }catch(Exception ex){
            throw new MatrixException(ex);
                }finally {
                    return contextItems.toString();
                }
    }

    /**
     * Remove inherited access for any object when the relationship is being deleted.
     * Also removes ownership is there exist.
     * @param context the eMatrix <code>Context</code> object
     * @param args - contains trigger parameters.
     * @return
     * @throws Exception
     */
    public static boolean clearInheritedAccess(Context context, String[] args) throws Exception {

       String fromId = args[0];  // id of from object
       String toId = args[1];  // id of to object
       String onlyIncludeToType = args[2]; // only remove access to this Type on the To side of the relationship
       String accessBits = args[3];  // access bits we want to remove from the parent

       String command = DomainConstants.EMPTY_STRING;
       boolean contextPushed = false;
       try{
           boolean oKToRemoveAccess = false;
           DomainObject toObj = DomainObject.newInstance(context, toId);

           // Get information about the From side object
           StringList strFromList = new StringList();
           strFromList.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
           strFromList.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_CONCEPT);
           strFromList.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);

           DomainObject fromObj = DomainObject.newInstance(context,fromId);
           Map mpObjectInfo = fromObj.getInfo(context,strFromList);
           String bisFromProjectSpaceType = (String)mpObjectInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
           String bisFromProjectConceptType = (String)mpObjectInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_CONCEPT);
           String bisFromTaskManagementType = (String)mpObjectInfo.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);

           // if in the context of PS, PC or PT
           if (bisFromProjectSpaceType.equalsIgnoreCase("true") || bisFromProjectConceptType.equalsIgnoreCase("true") || bisFromTaskManagementType.equalsIgnoreCase("true"))
           {
               oKToRemoveAccess = true;
           }
           if (oKToRemoveAccess) {

               if (onlyIncludeToType != null && (! "".equalsIgnoreCase(onlyIncludeToType))){

                   String typeToInclude = PropertyUtil.getSchemaProperty(context, onlyIncludeToType);

                   //Check to see if it is kind of type_DOCUMENTS
                   if(toObj.isKindOf(context, typeToInclude)){

                       String comment = DomainAccess.COMMENT_MULTIPLE_OWNERSHIP;
                       if("TRUE".equalsIgnoreCase(bisFromTaskManagementType)){
                           //while deleting ownership, need to pass same comment as used in creating ownership
                           comment = "Adding ownership for TaskDeliverable relationship";
                       }

                       //delete Object Ownership if exist.
                       DomainAccess.deleteObjectOwnership(context, toId, fromId, comment);

                       //check if there is inherited access is present or not...
                       String sResult = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump",toId,"access["+fromId+"|Inherited Access]");

                       if("TRUE".equalsIgnoreCase(sResult)){
                           command = "modify bus " + toId + " remove access bus " + fromId + " for 'Inherited Access' as " +  accessBits;
                       }else{
                           return true;
                       }
                   }
               }
               else if(toObj.isKindOf(context,ProgramCentralConstants.TYPE_ISSUE)){   //Need to remove ownership from Issue..
                       DomainAccess.deleteObjectOwnership(context, toId, fromId, "");
                   }

               if(!command.isEmpty())
                   MqlUtil.mqlCommand(context, command);
           }

           return true;
       }catch(Exception e){
           throw e;
       }
    }

    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map postProcessRefresh (Context context, String[] args) throws Exception
    {
        Map <String,String> returnMap = new HashMap<String,String>(1);
        returnMap.put("Action","refresh");
        return returnMap;
    }

  /**
 * Excludes the items that are already being governed.
 * @param context the ENOVIA <code>Context</code> user.
 * @param args Jpo argument array
 * @return an exclusion list of object ids that are connected to some governing entity through Governing Project relationship.
 * @throws MatrixException
 */
 @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
public StringList excludeAlreadyGovernedItems(Context context, String[]args) throws MatrixException {
  try {
    return ProgramCentralUtil.excludeAlreadyGovernedItems(context);
  } catch (Exception e) {
    throw new MatrixException(e);
  }
}


 protected final static String SELECT_IS_KIND_OF_PROXYITEM = "interface.kindof[ProxyItem]";
 protected final static String  PROXYITEM     = "interface[ProxyItem]";
 protected final static String  PROXY_ID      = "attribute[ProxyItem.Proxy_Id]";
 protected final static String  PROXY_TITLE   = "attribute[ProxyItem.Proxy_Title]";
 protected final static String  PROXY_SERVICE = "attribute[ProxyItem.Proxy_Service]";
 protected final static String  PROXY_TENANT  = "attribute[ProxyItem.Proxy_Tenant]";


   /**
    *  Get Vector of Strings for Document Action Icons
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of html code to
    *        construct the Actions Column.
    *  @throws Exception if the operation fails
    *
    *  @since Common 10.5
    *  @grade 0
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
     public static Vector getDocumentActions(Context context, String[] args)
        throws Exception
      {
        Vector vActions = new Vector();
        try
        {
            // Start MSF
            String msfRequestData = "";
            String msfFileFormatDetails = "";
            boolean isAdded = false;
            // End MSF
            HashMap programMap = (HashMap) JPO.unpackArgs(args);

            MapList objectList = (MapList)programMap.get("objectList");
            if(objectList.size() <= 0){
                return vActions;
            }
            Map paramList      = (Map)programMap.get("paramList");
            String uiType = (String)paramList.get("uiType");
            String parentOID = (String)paramList.get("parentOID");
            String customSortColumns = (String)paramList.get("customSortColumns");
            String customSortDirections = (String)paramList.get("customSortDirections");
            String table = (String)paramList.get("table");
            if(objectList == null || objectList.size() <= 0)
            {
               return vActions;
            }

            boolean isprinterFriendly = false;
            if(paramList.get("reportFormat") != null)
            {
            isprinterFriendly = true;
            }

            String languageStr = (String)context.getSession().getLanguage();
            Locale strLocale = context.getLocale();
            String sTipDownload = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipDownload");
            String sTipCheckout = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipCheckout");
            String sTipCheckin  = EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipCheckin");
            String sTipUpdate   = EnoviaResourceBundle.getProperty(context,   "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipAddFiles");
            String sTipSubscriptions   = EnoviaResourceBundle.getProperty(context,   "emxComponentsStringResource", strLocale,"emxComponents.Command.Subscriptions");

            Map objectMap = null;
            if(objectList != null && objectList.size() > 0)
            {
                objectMap = (Map)objectList.get(0);
            }

            Iterator objectListItr = null;
            if ( objectMap != null
                && objectMap.containsKey(CommonDocument.SELECT_TYPE)
                && objectMap.containsKey(CommonDocument.SELECT_SUSPEND_VERSIONING)
                && objectMap.containsKey(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS)
                && objectMap.containsKey(CommonDocument.SELECT_HAS_CHECKIN_ACCESS)
                && objectMap.containsKey(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION)
                && objectMap.containsKey(CommonDocument.SELECT_FILE_NAME)
                && objectMap.containsKey(CommonDocument.SELECT_MOVE_FILES_TO_VERSION)
                && objectMap.containsKey(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT)
                && objectMap.containsKey(SELECT_IS_KIND_OF_PROXYITEM)
                && objectMap.containsKey("vcfile")
                && objectMap.containsKey("vcmodule")
                && objectMap.containsKey(CommonDocument.SELECT_ACTIVE_FILE_LOCKED)
                && objectMap.containsKey(CommonDocument.SELECT_ACTIVE_FILE_LOCKER)
                && objectMap.containsKey(CommonDocument.SELECT_HAS_TOCONNECT_ACCESS)
                && objectMap.containsKey(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID)
                && objectMap.containsKey(CommonDocument.SELECT_OWNER)
                && objectMap.containsKey(CommonDocument.SELECT_LOCKED)
                && objectMap.containsKey(CommonDocument.SELECT_LOCKER)
                && objectMap.containsKey(PROXYITEM)
                && objectMap.containsKey(PROXY_ID)
                && objectMap.containsKey(PROXY_TITLE)
                && objectMap.containsKey(PROXY_SERVICE)
                && objectMap.containsKey(PROXY_TENANT) ) {

                    objectListItr = objectList.iterator();
            } else {
            StringList selectTypeStmts = new StringList(30);
            selectTypeStmts.add(DomainConstants.SELECT_ID);
            selectTypeStmts.add(DomainConstants.SELECT_TYPE);
            selectTypeStmts.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
            selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
            selectTypeStmts.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
            selectTypeStmts.add(CommonDocument.SELECT_FILE_NAME);
            selectTypeStmts.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
            selectTypeStmts.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
            selectTypeStmts.add(SELECT_IS_KIND_OF_PROXYITEM);
            selectTypeStmts.add("vcfile");
            selectTypeStmts.add("vcmodule");
            selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
            selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
            selectTypeStmts.add(CommonDocument.SELECT_HAS_TOCONNECT_ACCESS);
            selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
            selectTypeStmts.add(CommonDocument.SELECT_OWNER);
            selectTypeStmts.add(CommonDocument.SELECT_LOCKED);
            selectTypeStmts.add(CommonDocument.SELECT_LOCKER);
            selectTypeStmts.add(CommonDocument.SELECT_CURRENT);

            selectTypeStmts.add(PROXYITEM);
            selectTypeStmts.add(PROXY_ID);
            selectTypeStmts.add(PROXY_TITLE);
            selectTypeStmts.add(PROXY_SERVICE);
            selectTypeStmts.add(PROXY_TENANT);


            selectTypeStmts.add(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);



            //Getting all the content ids
            String oidsArray[] = new String[objectList.size()];
            for (int i = 0; i < objectList.size(); i++)
            {
               try
               {
                   oidsArray[i] = (String)((HashMap)objectList.get(i)).get("id");
               } catch (Exception ex)
               {
                   oidsArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
               }
            }

            MapList objList = DomainObject.getInfo(context, oidsArray, selectTypeStmts);

                objectListItr = objList.iterator();
            }


            HashMap versionMap = new HashMap();
            String linkAttrName = PropertyUtil.getSchemaProperty(context,"attribute_MxCCIsObjectLinked");
            while(objectListItr.hasNext())
            {
                // Start MSF
                msfFileFormatDetails = "MSFFileFormatDetails:[";
                // End MSF
                Map contentObjectMap = (Map)objectListItr.next();
                int fileCount = 0;
                String vcInterface = "";
                boolean vcDocument = false;
                boolean vcFile = false;
                String docType = "";
                StringBuffer strBuf = new StringBuffer(1256);
                boolean moveFilesToVersion = (Boolean.valueOf((String) contentObjectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION))).booleanValue();
                String documentId = (String)contentObjectMap.get(DomainConstants.SELECT_ID);


                Map lockCheckinStatusMap = CommonDocument.getLockAndCheckinIconStatus(context, contentObjectMap);
                boolean isAnyFileLockedByContext = (boolean)lockCheckinStatusMap.get("isAnyFileLockedByContext");

                String proxyItem      = (String)contentObjectMap.get(PROXYITEM);
                String proxyID        = (String)contentObjectMap.get(PROXY_ID);
                String proxyTitle     = (String)contentObjectMap.get(PROXY_TITLE);
                String proxyService   = (String)contentObjectMap.get(PROXY_SERVICE);
                String proxyTenant    = (String)contentObjectMap.get(PROXY_TENANT);
                String isWorkspaceVault    = (String)contentObjectMap.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);



                //For getting the count of files
                HashMap filemap = new HashMap();
                filemap.put(CommonDocument.SELECT_MOVE_FILES_TO_VERSION, contentObjectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION));
                // Start MSF
                StringList activeVersionFileList = (StringList) contentObjectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
                if (null == activeVersionFileList) {
                    activeVersionFileList = new StringList();
                }
                filemap.put(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION, activeVersionFileList);
                // End MSF
                filemap.put(CommonDocument.SELECT_FILE_NAME, contentObjectMap.get(CommonDocument.SELECT_FILE_NAME));
                // Start MSF
                StringList activeVersionIDList = (StringList) contentObjectMap.get(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
                if (null == activeVersionIDList) {
                    activeVersionIDList = new StringList();
                }
                // End MSF
                fileCount = CommonDocument.getFileCount(context,filemap);

                vcInterface = (String)contentObjectMap.get(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
                vcDocument = "TRUE".equalsIgnoreCase(vcInterface)?true:false;

                // PROXY DOC
                String proxyInterface = "";
                boolean proxyDocument = false;
                proxyInterface = (String)contentObjectMap.get(SELECT_IS_KIND_OF_PROXYITEM);
                proxyDocument = "TRUE".equalsIgnoreCase(proxyInterface)?true:false;

                docType    = (String)contentObjectMap.get(DomainConstants.SELECT_TYPE);

                if(!versionMap.containsKey(docType)){
                    versionMap.put(docType, CommonDocument.checkVersionableType(context, docType));
                }

                String parentType = CommonDocument.getParentType(context, docType);
                if (CommonDocument.TYPE_DOCUMENTS.equals(parentType))
                {
                    // show subscription link
                    if (FrameworkLicenseUtil.isCPFUser(context) && ! proxyDocument)
                    {
                        if(!isprinterFriendly)
                        {
                            strBuf.append("<a href=\"javascript:showModalDialog('../components/emxSubscriptionDialog.jsp?objectId=");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                            strBuf.append("', '730', '450')\"><img border='0' src='../common/images/iconSmallSubscription.gif' alt=\""+sTipSubscriptions+"\" title=\""+sTipSubscriptions+"\"></img></a>&#160;");
                        } else {
                            strBuf.append("<img border='0' src='../common/images/iconSmallSubscription.gif' alt=\""+sTipSubscriptions+"\" title=\""+sTipSubscriptions+"\"></img>&#160;");
                        }
                    }
                    //Can Download
                    if(CommonDocument.canDownload(context, contentObjectMap))
                    {
                        if (!isprinterFriendly)
                        {
                            strBuf.append("<a href='javascript:callCheckout(\"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                            strBuf.append("\",\"download\", \"\", \"\",\"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, table));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, parentOID));
                            strBuf.append("\"");
                            strBuf.append(")'>");
                            strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
                            strBuf.append(sTipDownload);
                            strBuf.append("\" title=\"");
                            strBuf.append(sTipDownload);
                            strBuf.append("\"></img></a>&#160;");
                        } else {
                            strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
                            strBuf.append(sTipDownload);
                            strBuf.append("\"></img>&#160;");
                        }
                        // Changes for CLC start here..
                        //Show Download Icon for ClearCase Linked Objects
                        //DomainObject ccLinkedObject  = DomainObject.newInstance(context, documentId);

                        String isObjLinked = null;
                        if(linkAttrName!=null && !linkAttrName.equals(""))
                        {
                            DomainObject docObject = DomainObject.newInstance(context,documentId);
                            isObjLinked = docObject.getAttributeValue(context,linkAttrName);
                        }

                        if(isObjLinked!=null && !isObjLinked.equals(""))
                        {
                            if(isObjLinked.equalsIgnoreCase("True"))
                            {
                                //show download icon for Linked Objects
                                strBuf.append("<a href='../servlet/MxCCCS/MxCCCommandsServlet.java?commandName=downloadallfiles&amp;objectId=");
                                strBuf.append(XSSUtil.encodeForURL(context, documentId));
                                strBuf.append("'>");
                                strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
                                strBuf.append(sTipDownload);
                                strBuf.append("\" title=\"");
                                strBuf.append(sTipDownload);
                                strBuf.append("\"></img></a>&#160;");
                            }
                        }
                     //////////////////////////////////////////////////////////////
                    // Proxy Download  - DISABLED
                    // any supporting code would go into ENOProgramManagementBase\CNext\webroot\programcentral\emxProgramCentralUIFormValidation.js
                    } else if (proxyDocument  && false) {

                        if (!isprinterFriendly)
                        {
                            strBuf.append("<a href='javascript:download3DDrive(\"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                            strBuf.append("\"");
                            strBuf.append(")'>");
                        strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
                        strBuf.append(sTipDownload);
                        strBuf.append("\" title=\"");
                        strBuf.append(sTipDownload);
                        strBuf.append("\"></img></a>&#160;");
                        } else {
                            strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
                            strBuf.append(sTipDownload);
                            strBuf.append("\"></img>&#160;");
                        }


                    }
                    //////////////////////////////////////////////////////////////////
                    // Can Checkout
                    if(CommonDocument.canCheckout(context, contentObjectMap,false, ((Boolean) versionMap.get(docType)).booleanValue()))
                    {
                        if(!isprinterFriendly)
                        {

                            strBuf.append("<a href='javascript:callCheckout(\"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                            strBuf.append("\",\"checkout\", \"\", \"\",\"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, table));
                            strBuf.append("\", \"");
                            strBuf.append(parentOID);
                            strBuf.append("\"");
                            strBuf.append(")'>");
                            strBuf.append("<img border='0' src='../common/images/iconActionCheckOut.gif' alt=\"");
                            strBuf.append(sTipCheckout);
                            strBuf.append("\" title=\"");
                            strBuf.append(sTipCheckout);
                            strBuf.append("\"></img></a>&#160;");
                        } else {
                            strBuf.append("<img border='0' src='../common/images/iconActionCheckOut.gif' alt=\"");
                            strBuf.append(sTipCheckout);
                            strBuf.append("\"></img>&#160;");
                       }
                    } else {
                        strBuf.append("&#160;");
                    }
                    // Can Checkin
                    if((CommonDocument.canCheckin(context, contentObjectMap) || VCDocument.canVCCheckin(context, contentObjectMap)))
                    {
                        // Start MSF
                        isAdded = false;
                        for(int ii =0; ii< activeVersionFileList.size(); ii++){
                            if(!isAdded) {
                                isAdded = true;
                                msfFileFormatDetails += "{FileName: '" + XSSUtil.encodeForJavaScript(context, (String)activeVersionFileList.get(ii)) +
                                "', VersionId: '" + XSSUtil.encodeForJavaScript(context, (String)activeVersionIDList.get(ii)) + "'}";
                            }
                            else {
                                msfFileFormatDetails += ", {FileName: '" + XSSUtil.encodeForJavaScript(context, (String)activeVersionFileList.get(ii)) +
                                "', VersionId: '" + XSSUtil.encodeForJavaScript(context, (String)activeVersionIDList.get(ii)) + "'}";
                            }
                        }
                        msfFileFormatDetails += "]";
                        // End MSF

                        // MSF
                        msfRequestData = "{RequestType: 'CheckIn', DocumentID: '" + documentId + "', " + msfFileFormatDetails + "}";
                        // MSF
                        vcFile =(Boolean.valueOf((String) contentObjectMap.get("vcfile"))).booleanValue();
                        if(!isprinterFriendly)
                        {
                            if (isAnyFileLockedByContext) {
                            if( !vcDocument )
                            {
                                strBuf.append("<a href=\"javascript:processModalDialog(" + msfRequestData + "," + "'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
                                strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                                strBuf.append("&amp;customSortColumns="); //Added for Bug #371651 starts
                                strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                                strBuf.append("&amp;customSortDirections=");
                                strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                                strBuf.append("&amp;uiType=");
                                strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                                strBuf.append("&amp;table=");
                                strBuf.append(XSSUtil.encodeForJavaScript(context, table));                 //Added for Bug #371651 ends
                                strBuf.append("&amp;showFormat=true&amp;showComments=required&amp;objectAction=update&amp;JPOName=emxTeamDocumentBase&amp;appDir=teamcentral&amp;appProcessPage=emxTeamPostCheckinProcess.jsp&amp;refreshTableContent=true&amp;updateTreeLabel=false',730,450);\">");
                                strBuf.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\"");
                                strBuf.append(sTipCheckin);
                                strBuf.append("\" title=\"");
                                strBuf.append(sTipCheckin);
                                strBuf.append("\"></img></a>&#160;");
                            } else {
                                if(vcFile)
                                {
                                    strBuf.append("<a href=\"javascript:processModalDialog(" + msfRequestData + "," + "'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                                    strBuf.append("&amp;customSortColumns=");     //Added for Bug #371651 starts
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                                    strBuf.append("&amp;customSortDirections=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                                    strBuf.append("&amp;uiType=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                                    strBuf.append("&amp;table=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, table));                 //Added for Bug #371651 ends
                                    strBuf.append("&amp;showFormat=false&amp;showComments=required&amp;objectAction=checkinVCFile&amp;allowFileNameChange=false&amp;noOfFiles=1&amp;JPOName=emxVCDocument&amp;methodName=checkinUpdate&amp;refreshTableContent=true', '730', '450');\">");
                                    strBuf.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\"");
                                    strBuf.append(sTipCheckin);
                                    strBuf.append("\" title=\"");
                                    strBuf.append(sTipCheckin);
                                    strBuf.append("\"></img></a>&#160;");
                                } else {
                                    strBuf.append("<a href=\"javascript:processModalDialog(" + msfRequestData + "," + "'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                                    strBuf.append("&amp;customSortColumns=");         //Added for Bug #371651 starts
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                                    strBuf.append("&amp;customSortDirections=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                                    strBuf.append("&amp;uiType=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                                    strBuf.append("&amp;table=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, table));                 //Added for Bug #371651 ends
                                    strBuf.append("&amp;override=false&amp;showFormat=false&amp;showComments=required&amp;objectAction=checkinVCFile&amp;allowFileNameChange=true&amp;noOfFiles=1&amp;JPOName=emxVCDocument&amp;methodName=checkinUpdate&amp;refreshTableContent=true', '730', '450');\">");
                                    strBuf.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\"");
                                    strBuf.append(sTipCheckin);
                                    strBuf.append("\" title=\"");
                                    strBuf.append(sTipCheckin);
                                    strBuf.append("\"></img></a>&#160;");
                                }
                            }
                        }
                        } else {
                            if (isAnyFileLockedByContext) {
                            strBuf.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\"");
                            strBuf.append(sTipCheckin);
                            strBuf.append("\" title=\"");
                            strBuf.append(sTipCheckin);
                            strBuf.append("\"></img>&#160;");
                        }
                    }
                    }
                    // Can Add Files
                    if(CommonDocument.canAddFiles(context, contentObjectMap))
                    {
                        // MSF
                        msfRequestData = "{RequestType: 'AddFiles', DocumentID: '" + documentId + "'}";
                        // MSF
                        if(!isprinterFriendly)
                        {
                            strBuf.append("<a href=\"javascript:processModalDialog(" + msfRequestData + "," + "'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                            strBuf.append("&amp;customSortColumns=");       //Added for Bug #371651 starts
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                            strBuf.append("&amp;customSortDirections=");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                            strBuf.append("&amp;uiType=");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                            strBuf.append("&amp;table=");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, table));                   //Added for Bug #371651 ends
                            strBuf.append("&amp;showFormat=true&amp;showDescription=required&amp;objectAction=checkin&amp;showTitle=true&amp;JPOName=emxTeamDocumentBase&amp;appDir=teamcentral&amp;appProcessPage=emxTeamPostCheckinProcess.jsp&amp;refreshTableContent=true&amp;updateTreeLabel=false', '730', '450')\">");
                            strBuf.append("<img border='0' src='../common/images/iconActionAppend.gif' alt=\"");
                            strBuf.append(sTipUpdate);
                            strBuf.append("\" title =\"");
                            strBuf.append(sTipUpdate);
                            strBuf.append("\"></img></a>&#160;");
                        } else {
                            strBuf.append("<img border='0' src='../common/images/iconActionAppend.gif' alt=\"");
                            strBuf.append(sTipUpdate);
                            strBuf.append("\" title=\"");
                            strBuf.append(sTipUpdate);
                            strBuf.append("\"></img>&#160;");
                        }
                    }
                    if (strBuf.length() == 0)
                        strBuf.append("&#160;");
                } else if("true".equalsIgnoreCase(isWorkspaceVault))
                {

                    if(!isprinterFriendly){
                        strBuf.append("<a href=\"javascript:showModalDialog('../common/emxForm.jsp?form=PMCWorkspaceVaultSubscriptionForm&amp;formHeader=emxProgramCentral.FolderSubscription.Header&amp;mode=edit&amp;postProcessJPO=emxProjectFolder:createSubscriptionProcess&amp;HelpMarker=emxhelpfoldersubscribe&amp;suiteKey=ProgramCentral&amp;StringResourceFileId=emxProgramCentralStringResource&amp;SuiteDirectory=programcentral&amp;submitAction=doNothing&amp;objectId="); //Modified PRG:RG6:R212:2 Jun 2011:
                        strBuf.append(documentId);
                        strBuf.append("', '730', '450')\"><img border='0' src='../common/images/iconSmallSubscription.gif' alt=\""+sTipSubscriptions+"\" title=\""+sTipSubscriptions+"\"></img></a>&#160;");
                } else {
                        strBuf.append("<a href=\"#\" ><img border='0' src='../common/images/iconSmallSubscription.gif' alt=\""+sTipSubscriptions+"\" title=\""+sTipSubscriptions+"\"></img></a>&#160;"); //Modified PRG:RG6:R212:2 Jun 2011:
                    }

                }

                else {
                    strBuf.append("&#160;");
                }
                vActions.add(strBuf.toString());
            }
        } catch(Exception ex){
            System.out.println(ex.getMessage());
//            ex.printStackTrace();   
            DebugUtil.debug("getDocumentActions "+ ex.getMessage());
            throw ex;
        }
        finally
        {
            return vActions;
        }
    }


/**
 * Retrieve the name column for PMCDocumentSummary Table to accommodate
 * proxy objects as Task Deliverables and Reference Documents
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Vector getNameColumnData (Context context, String[] args) throws Exception
    {
        Map programMap      = (Map)JPO.unpackArgs(args);
        MapList objectList  = (MapList) programMap.get("objectList");
        HashMap paramList   = (HashMap) programMap.get("paramList");
        String exportFormat = (String)paramList.get("exportFormat");
        String invokeFrom   = (String)programMap.get("invokeFrom"); //Added for ODT

        boolean isPrinterFriendly = false;
        String strPrinterFriendly = (String)paramList.get("reportFormat");
        if ( strPrinterFriendly != null ) {
            isPrinterFriendly = true;
        }

        int size = objectList.size();
        String[] strObjectIds = new String[size];
        for (int i = 0; i < size; i++) {
            Map mapObject = (Map) objectList.get(i);
            String deliverableId = (String) mapObject.get(DomainObject.SELECT_ID);
            strObjectIds[i] = deliverableId;
        }

        String SELECT_PARENT_TYPE = "to[Data Vaults].from.type";
        StringList slBusSelect = new StringList(12);
        slBusSelect.add(DomainConstants.SELECT_ID);
        slBusSelect.add(DomainConstants.SELECT_TYPE);
        slBusSelect.add(DomainConstants.SELECT_NAME);
        slBusSelect.add(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
        slBusSelect.add(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);
        slBusSelect.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        slBusSelect.add(SELECT_PARENT_TYPE);
//      slBusSelect.add(DomainConstants.SELECT_CURRENT);
//      slBusSelect.add(DomainConstants.SELECT_POLICY);
//      slBusSelect.add(DomainConstants.SELECT_DESCRIPTION);

        slBusSelect.add(PROXYITEM);
        slBusSelect.add(PROXY_ID);
        slBusSelect.add(PROXY_TITLE);
        slBusSelect.add(PROXY_SERVICE);
        slBusSelect.add(PROXY_TENANT);


        Map<String,StringList> derivativeMap =
                ProgramCentralUtil.getDerivativeTypeListFromUtilCache(context,ProgramCentralConstants.TYPE_PROJECT_SPACE, ProgramCentralConstants.TYPE_PROJECT_CONCEPT, ProgramCentralConstants.TYPE_PROJECT_TEMPLATE);


        StringList subTypeList = new StringList();
        subTypeList.addAll(derivativeMap.get(ProgramCentralConstants.TYPE_PROJECT_SPACE));
        subTypeList.addAll(derivativeMap.get(ProgramCentralConstants.TYPE_PROJECT_CONCEPT));
        subTypeList.addAll(derivativeMap.get(ProgramCentralConstants.TYPE_PROJECT_TEMPLATE));


        Map mapTaskInfo = new LinkedHashMap();
        BusinessObjectWithSelectList objectWithSelectList = null;
        if("TestCase".equalsIgnoreCase(invokeFrom)) { //Added for ODT
            objectWithSelectList = ProgramCentralUtil.getObjectWithSelectList(context, strObjectIds, slBusSelect,true);
        }else {
            objectWithSelectList = ProgramCentralUtil.getObjectWithSelectList(context, strObjectIds, slBusSelect);
        }

        for (BusinessObjectWithSelectItr objectWithSelectItr = new BusinessObjectWithSelectItr(objectWithSelectList); objectWithSelectItr.next();) {
            BusinessObjectWithSelect objectWithSelect = objectWithSelectItr.obj();

            Map mapTask = new HashMap();
            for (Iterator itrSelectables = slBusSelect.iterator(); itrSelectables.hasNext();) {
                String strSelectable = (String)itrSelectables.next();
                mapTask.put(strSelectable, objectWithSelect.getSelectData(strSelectable));
            }
            mapTaskInfo.put(objectWithSelect.getSelectData(DomainConstants.SELECT_ID), mapTask);
        }

        Vector columnValues = new Vector(objectList.size());
        String url3DSpace  = "emxTree.jsp?mode=insert";
        String urlExternal = "emxTree.jsp?mode=insert";

        String strGridActive = "false";
       /* try {
            strGridActive = EnoviaResourceBundle.getProperty(context, "emxFramework.Activate.GridView");
        } catch (FrameworkException e) {
            strGridActive = "false";
        }*/
        if ("true".equalsIgnoreCase(strGridActive)) {
            url3DSpace  = "../../common/emxTree.jsp?mode=insert";
            urlExternal = "../../common/emxTree.jsp?mode=insert";
        }

        Iterator objectIdItr = objectList.iterator();
        while(objectIdItr.hasNext()){

            String deliverableId = (String)((Map)objectIdItr.next()).get(ProgramCentralConstants.SELECT_ID);

            Map objectInfo = (Map)mapTaskInfo.get(deliverableId);

            String strName = (String)objectInfo.get(DomainConstants.SELECT_NAME);
 //         String strDescription = (String)objectInfo.get(DomainConstants.SELECT_DESCRIPTION);
 //         String sState         = (String)objectInfo.get(DomainConstants.SELECT_CURRENT);
 //         String taskPolicy     = (String)objectInfo.get(DomainConstants.SELECT_POLICY);
            String taskObjType    = (String)objectInfo.get(DomainConstants.SELECT_TYPE);
            String proxyItem      = (String)objectInfo.get(PROXYITEM);
            String proxyID        = (String)objectInfo.get(PROXY_ID);
            String proxyTitle     = (String)objectInfo.get(PROXY_TITLE);
            String proxyService   = (String)objectInfo.get(PROXY_SERVICE);
            String proxyTenant    = (String)objectInfo.get(PROXY_TENANT);

            String isKindOfWorkspace    = (String)objectInfo.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
            String fromType    = (String)objectInfo.get(SELECT_PARENT_TYPE);

            String isKindOfControlledFolder    = (String)objectInfo.get(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);
            String title    = (String)objectInfo.get(DomainConstants.SELECT_ATTRIBUTE_TITLE);
            if("TRUE".equalsIgnoreCase(isKindOfControlledFolder) || "TRUE".equalsIgnoreCase(isKindOfWorkspace)) {
                strName = title;
            }

            String strDisplayName = strName;

            boolean isProxyType = false;
            if("Document Proxy".equalsIgnoreCase(taskObjType) || "TRUE".equalsIgnoreCase(proxyItem)){
                urlExternal =  build3DDriveURL (context, "X3DDRIV_AP", proxyID, proxyTenant, proxyService);
                isProxyType = true;
                strDisplayName = proxyService;
            }

            StringBuilder bufLink  = new StringBuilder();
            StringBuilder bufUrl = new StringBuilder();

            if(ProgramCentralUtil.isNullString(exportFormat)) {
                strName = XSSUtil.encodeForXML(context, strName);
                strDisplayName = XSSUtil.encodeForXML(context, strDisplayName);
            }

            // https://vdevpril889am.ux.dsone.3ds.com/3DSpace/common/emxTree.jsp?mode=insert&
            // &emxSuiteDirectory=components&portalCmdName=PMCDeliverable&relId=33728.33638.43661.52394&
            // parentOID=33728.33638.26417.7408&suiteKey=Components&objectId=33728.33638.34297.1271&jsTreeID=undefined
            if( !isProxyType ){
                bufUrl.append(url3DSpace);
                bufUrl.append("&");
                bufUrl.append("objectId="+deliverableId );

                if("TRUE".equalsIgnoreCase(isKindOfWorkspace) && ProgramCentralUtil.isNotNullString(fromType) && subTypeList.contains(fromType)) {
                    bufUrl.append("&emxSuiteDirectory=programcentral");
                }


                bufLink.append("<a  href=\""+XSSUtil.encodeForXML(context,bufUrl.toString()));
                bufLink.append("\" title=\""+strName+"\">");
                bufLink.append(strDisplayName);
           } else {
               if (strDisplayName.equalsIgnoreCase("onedrive"))
                   strDisplayName = "OneDrive";
               bufLink.append("<a  href=\""+XSSUtil.encodeForXML(context,urlExternal));
               bufLink.append("\" target=\"_blank\" title=\""+strDisplayName+"\">");
               bufLink.append(strDisplayName);
            }
            bufLink.append("</a>");

            columnValues.add(bufLink.toString());
        }
         return columnValues;
    }

/*
    Foundation utility generates the following
     https://vdevpril533am.ux.dsone.3ds.com:444/3DDashboard/#app:X3DDRIV_AP/content:id=13F45081AF370000C194195C30130100

    Base Unencoded
    https://eu1-215dsi0708-ifwe.3dexperience.3ds.com/#app:X3DDRIV_AP/content:granted-DSEXT001=true&firstlyLoading=false&saved-fragment=

    Remaining URL
    URL decoded      drives/DSEXT001/mimetype/text%2Fdirectory/inode/13F45081AF370000C194195C30130100/preview
    URL Encoded      drives%2FDSEXT001%2Fmimetype%2Ftext%252Fdirectory%2Finode%2F13F45081AF370000C194195C30130100%2Fpreview
*/

    protected static String build3DDriveURL(final Context context, final String appName, final String objectId, final String proxyTenant, String proxyService)  {
        String finalURL = null;
        StringBuffer  sbURL = new StringBuffer();
        StringBuffer  sbTail = new StringBuffer();

        try {
            final String dashBoardUrl = System.getenv("DASHBOARD_URL");
            if (objectId == null) {
                finalURL = dashBoardUrl + "#app:" + appName;
                sbURL.append(finalURL);

            } else {

//                sbTail.append("drives/");
//                sbTail.append(proxyTenant);
//                sbTail.append("/mimetype/text%2Fdirectory/inode/");
//                sbTail.append(objectId);
//                sbTail.append("/preview");

                if ("google2".equalsIgnoreCase(proxyService))
                    proxyService = "google";
                sbTail.append("/content:driveId=");
                sbTail.append(proxyService);
                sbTail.append("&contentId=");
                sbTail.append(objectId);
                sbTail.append("&contentType=file");

                sbURL.append(dashBoardUrl);
                sbURL.append("#app:");
                sbURL.append(appName);
//                sbURL.append("/content:granted-");
//                sbURL.append(proxyTenant);
//                sbURL.append("=true&firstlyLoading=false&saved-fragment=");
                sbURL.append(XSSUtil.encodeForURL(sbTail.toString()));

                // finalURL = dashBoardUrl + "#app:" + appName + "/content:id=" + objectId;
                // final String tenant = context.getTenant();
                // if(tenant != null && !tenant.isEmpty()) {
                //  finalURL = finalURL + "&tenant=" + tenant;
                // }
            }
        } catch (final Exception ex) {
            System.out.println (sbURL.toString());
            System.out.println (ex.getMessage());
//            ex.printStackTrace();   
            DebugUtil.debug("build3DDriveURL "+ ex.getMessage());
        }
 //     System.out.println(sbURL.toString());
        return sbURL.toString();
    }

    /**
     * Creates command PMCProjectIssue dynamically from ContextIssueListPage.
     * @param context The eMatrix <code>Context</code> object.
     * @param args holds information about object.
     * @return command setting info map.
     * @throws Exception if operation fails.
     */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList createDynamicPMCProjectIssue(Context context, String[]args) throws Exception {

         MapList categoryMapList = new MapList();
         try {
              Map inputMap      = JPO.unpackArgs(args);
              Map requestMap    = (Map) inputMap.get("requestMap");
              String objectId   = (String) requestMap.get("objectId");

              UIMenu uiMenu = new UIMenu();

              if(ProgramCentralUtil.isNotNullString(objectId)){
                    Map contextIssueListPageCmdMap = ProgramCentralUtil.createDynamicCommand(context,"ContextIssueListPage",uiMenu,true);
                    String sHref = (String)contextIssueListPageCmdMap.get("href");

                    if(sHref != null && sHref.contains("?program=")){
                        sHref = FrameworkUtil.findAndReplace(sHref,
                                "program=emxCommonIssue:getActiveIssues,emxCommonIssue:getAllContextIssues,emxCommonIssue:getClosedIssues",
                                "program=emxProgramCentralUtil:getActiveIssues,emxProgramCentralUtil:getAllContextIssues,emxProgramCentralUtil:getClosedIssues");
                    }

                    contextIssueListPageCmdMap.put("href", sHref);
                    contextIssueListPageCmdMap.put("name", "PMCProjectIssue");
                    categoryMapList.add(contextIssueListPageCmdMap);
              }

        } catch (Exception e) {
            throw new MatrixException(e);
        }
          return categoryMapList;
     }

    /**
      * Method to get Active issues of project and it's sub-project.
      * @param context the eMatrix <code>Context</code> object
      * @param args - args contains a Map with the following entries.
      * @return MapList containing the Active issue id's of projects, tasks and sub-projects Issue.
      * @throws Exception
      */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getActiveIssues(Context context, String[] args) throws Exception {
        return getAllProjectIssues(context, args, "Active");
     }

     /**
      * Method to get Closed issues of project and it's sub-project.
      * @param context the eMatrix <code>Context</code> object
      * @param args - args contains a Map with the following entries.
      * @return MapList containing the Closed issue id's of projects, tasks and sub-projects Issue.
      * @throws Exception
      */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getClosedIssues(Context context, String[] args) throws Exception {
        return getAllProjectIssues(context, args, "Closed");
     }

     /**
      * Method to get all context issues of project and it's sub-project.
      * @param context the eMatrix <code>Context</code> object
      * @param args - args contains a Map with the following entries.
      * @return MapList containing the All issue id's of projects, tasks and sub-projects Issue.
      * @throws Exception
      */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getAllContextIssues(Context context, String[] args) throws Exception {
        return getAllProjectIssues(context, args, "All");
     }

     /**
      * Method to get all the project related issues including it's sub-project.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args - args contains a Map with the following entries.
      * @param Filter - get issues connected to task based on the filter "Active", "Closed", "All".
      * @return MapList containing the id's of projects, tasks and sub-projects Issue.
      * @throws Exception if the operation fails
      * @Author AMD5
      */
     protected MapList getAllProjectIssues(Context context, String[] args, String Filter) throws Exception
     {
        long start = System.currentTimeMillis();

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");

        StringList selectList = new StringList();
        selectList.add(ProgramCentralConstants.SELECT_PAL_PHYSICALID_FROM_PROJECT);

        BusinessObjectWithSelectList objectWithSelectList =
                 ProgramCentralUtil.getObjectWithSelectList(context, new String[]{objectId}, selectList,true);

        BusinessObjectWithSelect bws = objectWithSelectList.getElement(0);
        String palId = bws.getSelectData(ProgramCentralConstants.SELECT_PAL_PHYSICALID_FROM_PROJECT);

        ProjectSequence pal = new ProjectSequence(context, palId);
        Set<String> taskIds = new HashSet<>();

        //Get sub projects of master project
        List<Dataobject> subProjects = pal.getProjects(context);
        if(subProjects.size()>1){
            Set<String> allTaskIds = new HashSet<>();
            Set<String> subProjectPALID = new HashSet<>();

            //get tasks ids's of all the sub-projects
            taskIds = getSubProjectsTaskIds(context,subProjects, allTaskIds,subProjectPALID);
        }else{
            Map<String, Dataobject> projectSeqData = pal.getSequenceData(context);
            taskIds = projectSeqData.keySet();
        }

         //Initializing the return type
         MapList returnIssueList = new MapList();
         MapList issueList = new MapList();

        for (String taskPhysicalId : taskIds ){
            if("Active".equalsIgnoreCase(Filter)){
                issueList = new Issue().getAllIssues(context, taskPhysicalId, true, false);
            }else if("Closed".equalsIgnoreCase(Filter)){
                issueList = new Issue().getAllIssues(context, taskPhysicalId, false, true);
            }else if("All".equalsIgnoreCase(Filter)){
                issueList = new Issue().getAllIssues(context, taskPhysicalId, true, true);
            }

            returnIssueList.addAll(issueList);
        }
         System.out.println("Total time taken by getAllProjectIssues() API .............."+(System.currentTimeMillis()-start));

         return returnIssueList;
    }

     /**
      * Method to get all the sub-projects tasks
      *
      * @param context the eMatrix <code>Context</code> object
      * @param List<Dataobject> - sub-Project Dataobject.
      * @param taskIds - to hold all the taskIds of sub-projects task.
      * @return Set<String> containing the sub-projects taskIds.
      * @throws Exception if the operation fails
      * @Author AMD5
      */
    protected Set<String> getSubProjectsTaskIds(Context context, List<Dataobject> subProjects, Set<String> taskIds, Set<String> subProjectPALID) throws Exception {

         String subProjectIds[]     = new String[subProjects.size()];
         List<String> subProjIdList = new ArrayList<>();

         for (int i=0, size = subProjects.size(); i<size;i++) {
             subProjectIds[i] = subProjects.get(i).getId();
         }

         BusinessObjectWithSelectList objectWithSelectList =
                 ProgramCentralUtil.getObjectWithSelectList(context, subProjectIds, new StringList(ProgramCentralConstants.SELECT_PAL_PHYSICALID_FROM_PROJECT),true);

         for(int i = 0, size = objectWithSelectList.size(); i<size ; i++){
             BusinessObjectWithSelect bws = objectWithSelectList.get(i);
             String subProjectPalId   = bws.getSelectData(ProgramCentralConstants.SELECT_PAL_PHYSICALID_FROM_PROJECT);

             //to Avoid duplicate call in case of Subproject
             if(!subProjectPALID.contains(subProjectPalId)){
                 ProjectSequence ps = new ProjectSequence(context, subProjectPalId);
                 List<Dataobject> subProjectList = ps.getProjects(context);
                 Map<String, Dataobject> palSeqData = ps.getSequenceData(context);

                 taskIds.addAll(palSeqData.keySet());
                 subProjectPALID.add(subProjectPalId);

                 if(subProjectList.size() > 1){
                     Dataobject projectDataObj = palSeqData.get(subProjectIds[i]);
                     subProjectList.remove(projectDataObj);

                     //recursive call to get remaining SubProjects tasks Id's
                     getSubProjectsTaskIds(context, subProjectList, taskIds,subProjectPALID);
                 }
             }
         }

         return taskIds;
     }

}
