import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import matrix.db.Context;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.matrixone.apps.domain.DomainConstants.*;

public class JF_ESOTaskReminder_mxJPO implements JF_PLMConstants_mxJPO {

    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_ESOTaskReminder_mxJPO.class);
    private static final String EMAIL_PAGE_NAME = "ESOTaskReminderEmail";
    private static final String EMAIL_LANGUAGE_ZH = "zh";
    private static final String EMAIL_SUBJECT = "\u0045\u0053\u004f\u4efb\u52a1\u002f\u5ba1\u6838\u8bb0\u5f55\u8d85\u671f\u6c47\u603b\u63d0\u9192";
    private static final String CONFIG_3DSPACE_URL = "JF.3dspace.JFUrl";
    private static final String ATTR_COLOR_IDENTIFICATION = "attribute[JF_ColorIdentification]";
    private static final String ATTR_REVIEW_COUNT = "attribute[JF_ReviewCount]";
    private static final String ATTR_REVIEW_RECORD_COUNT = "attribute[JF_ReviewCounte]";
    private static final String ATTR_PHASE_STATE = "attribute[JF_PhaseState]";
    private static final String SELECT_ASSIGNED_TASK_PERSON = "to[" + RELATIONSHIP_ASSIGNED_TASKS + "].from.name";
    private static final String REL_TASK_TO_REVIEW = "JFESOTask2ESOReview";
    private static final String TYPE_REVIEW_RECORD = "JFESOReview";
    private static final String ROLE_ESO_ADMIN = "JfESOAdmin";
    private static final String WHERE_UNFINISHED = "current!=Complete";
    private static final String SECTION_DUE10 = "DUE10";
    private static final String SECTION_DUE3 = "DUE3";
    private static final String SECTION_OVERDUE = "OVERDUE";
    private static final String SECTION_REVIEW_YELLOW = "REVIEW_YELLOW";
    private static final String SECTION_REVIEW_RED = "REVIEW_RED";
    private static final String COLOR_RED = "red";
    private static final String COLOR_YELLOW = "yellow";
    private static final String LINK_LABEL_TASK = "\u4efb\u52a1\u94fe\u63a5";
    private static final String PHASE_STATE_RED = "R";
    private static final String PHASE_STATE_YELLOW = "Y";
    private static final int DUE_REMINDER_10_WORKDAYS = 10;
    private static final int DUE_REMINDER_3_WORKDAYS = 3;
    private static final int RED_REVIEW_REMINDER_WORKDAYS = 3;
    private static final int YELLOW_REVIEW_REMINDER_WORKDAYS = 7;
    // 仅处理该日期及以后创建的ESO模块任务，避免历史任务触发邮件提醒。
    private static final LocalDate ESO_REMINDER_START_DATE = LocalDate.of(2026, 7, 10);
    private static final DateTimeFormatter OUTPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy\u5e74MM\u6708dd\u65e5");
    private static final DateTimeFormatter[] DATE_TIME_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a", Locale.US),
            DateTimeFormatter.ofPattern("M/d/yyyy h:mm a", Locale.US),
            DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss", Locale.US),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US),
            DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss", Locale.US)
    };
    private static final DateTimeFormatter[] DATE_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US),
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US),
            DateTimeFormatter.ofPattern("yyyy/M/d", Locale.US)
    };
    private static final String DEFAULT_DATE_TIME_PATTERN = "M/d/yyyy h:mm:ss a";

    /**
     * 1. 工作日早上8点由定时器统一调用，查询ESO任务和ESO审核记录提醒数据；
     * 2. 只处理JFESOTask类型的模块任务，不再展开模块任务下的子级任务；
     * 3. 处理模块任务到期前10个工作日、到期前3个工作日、到期未审核三类任务提醒；
     * 4. 处理最新签发表黄色第7个工作日、红色第3个工作日的审核记录提醒；
     * 5. 收件人优先取模块任务中非ESO审核员的分派人，无有效分派人时回退非ESO审核员的owner；
     * 6. 按模块任务责任人合并邮件，直线经理放入同一封邮件收件人，到期未审核额外按整椅经理合并发送；
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2026/6/16 15:28
     * @description ESO任务和审核记录超期汇总邮件提醒
     */
    public void sendESOTaskReminderEmail(Context context, String[] args) throws Exception {
        JF_LOGGER.info("sendESOTaskReminderEmail start");
        boolean isPush = false;
        try {
            // 后台定时器使用管理员上下文读取任务、人员、项目和邮件配置。
            ContextUtil.pushContext(context);
            isPush = true;

            // 获取当前运行日期，后续所有工作日计算统一使用这个日期。
            LocalDate today = LocalDate.now();
            // 获取3DSPACE对象详情页基础地址，后续用模块任务id拼接邮件链接。
            String linkPrefix = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{CONFIG_3DSPACE_URL});
            // 定义分派人邮件汇总Map，key为分派人账号。
            Map personMailMap = new LinkedHashMap();
            // 定义整椅经理邮件汇总Map，只用于到期未审核提醒。
            Map chairMailMap = new LinkedHashMap();
            // 定义项目显示名称缓存，同一个项目下多个ESO任务共用一次查询结果。
            Map projectNameMap = new LinkedHashMap();
            // 定义项目整椅经理缓存，同一个项目下多个ESO任务共用一次查询结果。
            Map chairManagerMap = new LinkedHashMap();
            // 缓存人员是否拥有ESO审核员角色，避免同一人员在多条模块任务中被重复查询角色。
            Map esoMemberCache = new LinkedHashMap();
            // 基于项目角色公共常量拼接整椅经理查询条件，本次运行中重复复用。
            String chairManagerWhere = "attribute[" + ATTR_ProjectRole + "]=='" + ATTR_PROJECTROLE_RANGE_Chairmanager + "'";

            // 定义ESO模块任务查询字段，分派人和owner随任务一次查询返回。
            StringList taskSelects = new StringList();
            taskSelects.add(SELECT_ID);
            taskSelects.add(SELECT_NAME);
            taskSelects.add(SELECT_OWNER);
            taskSelects.add(SELECT_ORIGINATED);
            taskSelects.add(SELECT_ATTR_Task_Estimated_Finish_Date);
            taskSelects.add(ATTR_COLOR_IDENTIFICATION);
            taskSelects.add(ATTR_REVIEW_COUNT);
            taskSelects.add(SELECT_ATTR_JF_ESOType);
            taskSelects.add(SELECT_ASSIGNED_TASK_PERSON);
            // 定义项目查询字段。
            StringList projectSelects = new StringList();
            projectSelects.add(SELECT_NAME);
            projectSelects.add(SELECT_DESCRIPTION);
            // 定义签发表查询字段。
            StringList reviewSelects = new StringList();
            reviewSelects.add(SELECT_ID);
            reviewSelects.add(SELECT_NAME);
            reviewSelects.add(SELECT_ORIGINATED);
            reviewSelects.add(ATTR_REVIEW_RECORD_COUNT);
            reviewSelects.add(ATTR_PHASE_STATE);
            // 定义空关系查询字段，关系属性不需要读取时统一复用。
            StringList emptyRelSelects = new StringList();

            // 只查询未完成的JFESOTask模块任务，不再查询模块下的普通Task子任务。
            MapList esoTaskList = DomainObject.findObjects(context, TYPE_JF_ESOTask, "*", WHERE_UNFINISHED, taskSelects);
            JF_LOGGER.info("sendESOTaskReminderEmail esoTaskList size:{}", esoTaskList.size());

            // 创建可复用的ESO模块任务对象，循环中重复设置id减少对象创建。
            DomainObject esoTaskObj = DomainObject.newInstance(context);
            //20260814 update by LIUJR 复用ESO公共方法获取任务所属门节点。
            JF_ESO_mxJPO esoService = new JF_ESO_mxJPO();
            // 创建可复用的项目对象，循环中根据项目id读取项目名称。
            DomainObject projectObj = DomainObject.newInstance(context);
            // 遍历每一个未完成ESO模块任务。
            for (int i = 0; i < esoTaskList.size(); i++) {
                // 获取当前ESO模块任务查询结果Map。
                Map esoTaskMap = (Map) esoTaskList.get(i);
                // 获取ESO模块任务id。
                String esoTaskId = UIUtil.getValue(esoTaskMap, SELECT_ID);
                // 获取ESO模块任务名称。
                String esoTaskName = UIUtil.getValue(esoTaskMap, SELECT_NAME);
                // 2026年7月10日之前创建的ESO模块任务属于历史数据，不参与本次任何邮件提醒。
                String originatedValue = UIUtil.getValue(esoTaskMap, SELECT_ORIGINATED);
                LocalDate originatedDate = parseLocalDate(originatedValue);
                if (originatedDate != null && originatedDate.isBefore(ESO_REMINDER_START_DATE)) {
                    JF_LOGGER.info("ESO task created before reminder start date, skip taskId:{}, originated:{}", esoTaskId, originatedValue);
                    continue;
                }
                // 获取ESO模块任务计划完成日期字符串。
                String finishDateValue = UIUtil.getValue(esoTaskMap, SELECT_ATTR_Task_Estimated_Finish_Date);
                // 将计划完成日期转换为LocalDate，方便按工作日计算。
                LocalDate finishDate = parseLocalDate(finishDateValue);
                // 没有计划完成日期的ESO模块任务无法判断提醒日期，直接跳过。
                if (finishDate == null) {
                    JF_LOGGER.info("ESO task finish date is empty or invalid, taskId:{}, finishDate:{}", esoTaskId, finishDateValue);
                    continue;
                }

                // 邮件段落标题使用项目描述(项目名称)，描述为空时回退项目名称。
                // 根据ESO任务id获取所属项目id。
                String projectId = JF_PublicProjectQuery_mxJPO.getProjectIdByTaskId(context, esoTaskId);
                // 初始化项目显示名称。
                String projectName = "";
                // 初始化项目整椅经理账号，到期未审核时复用。
                String chairManager = "";
                // 项目id存在时再读取项目对象信息。
                if (UIUtil.isNotNullAndNotEmpty(projectId)) {
                    // 项目已经查询过时，直接使用缓存中的项目显示名称和整椅经理。
                    if (projectNameMap.containsKey(projectId)) {
                        projectName = UIUtil.getValue(projectNameMap, projectId);
                        chairManager = UIUtil.getValue(chairManagerMap, projectId);
                    // 项目首次出现时，查询项目基础信息和整椅经理并写入缓存。
                    } else {
                        // 设置项目对象id。
                        projectObj.setId(projectId);
                        // 查询项目基础信息。
                        Map projectInfo = projectObj.getInfo(context, projectSelects);
                        // 获取项目描述，用于邮件标题前半部分。
                        String projectDesc = UIUtil.getValue(projectInfo, SELECT_DESCRIPTION);
                        // 获取项目名称，用于邮件标题括号内展示。
                        String projectRealName = UIUtil.getValue(projectInfo, SELECT_NAME);
                        // 项目描述和项目名称都存在时，按“项目Desc(Name)”格式展示。
                        if (UIUtil.isNotNullAndNotEmpty(projectDesc) && UIUtil.isNotNullAndNotEmpty(projectRealName)) {
                            projectName = projectDesc + "(" + projectRealName + ")";
                        // 项目描述为空时，使用项目名称兜底。
                        } else if (UIUtil.isNotNullAndNotEmpty(projectRealName)) {
                            projectName = projectRealName;
                        // 项目名称为空时，使用项目描述兜底。
                        } else {
                            projectName = projectDesc;
                        }
                        // 根据项目角色获取整椅经理账号，避免在模块任务循环中重复查询。
                        chairManager = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_NAME, "", chairManagerWhere);
                        // 缓存项目显示名称，后续同项目ESO任务直接复用。
                        projectNameMap.put(projectId, projectName);
                        // 缓存项目整椅经理，后续同项目ESO任务直接复用。
                        chairManagerMap.put(projectId, chairManager);
                    }
                }

                // 设置当前ESO模块任务id，用于查询该模块任务关联的签发表。
                esoTaskObj.setId(esoTaskId);
                // 收件人优先取模块任务的非ESO分派人；没有有效分派人时回退到非ESO的任务owner。
                StringList responsibleList = getReminderResponsibleList(context, esoTaskMap, esoMemberCache);
                // 分派人和owner均无有效非ESO人员时，该模块任务不发送任何提醒。
                if (responsibleList.isEmpty()) {
                    JF_LOGGER.info("ESO task has no valid reminder responsible person, taskId:{}", esoTaskId);
                    continue;
                }
                //20260814 update by LIUJR 五类提醒邮件统一展示任务所属门节点和TKO类型。
                Map firstPhaseInfo = esoService.getFirstPhaseByTask(context, esoTaskObj);
                String gateNode = UIUtil.getValue(firstPhaseInfo, SELECT_NAME);
                String esoType = UIUtil.getValue(esoTaskMap, SELECT_ATTR_JF_ESOType);

                // 到期前提醒只在模块任务距离计划完成日期恰好10或3个工作日时发送一次。
                // 计算今天到ESO模块任务计划完成日期之间的工作日数量。
                int remainingWorkdays = getWorkdayCount(today, finishDate);
                // 只处理到期前10个工作日和3个工作日两个提醒窗口。
                if (remainingWorkdays == DUE_REMINDER_10_WORKDAYS || remainingWorkdays == DUE_REMINDER_3_WORKDAYS) {
                    // 根据模块任务剩余工作日决定放入10天提醒区域还是3天提醒区域。
                    String section = remainingWorkdays == DUE_REMINDER_10_WORKDAYS ? SECTION_DUE10 : SECTION_DUE3;
                    // 邮件中只生成当前ESO模块任务记录，不展开其子级任务。
                    ReminderRecord record = new ReminderRecord();
                    record.projectName = projectName;
                    record.taskId = esoTaskId;
                    record.taskName = esoTaskName;
                    record.gateNode = gateNode;
                    record.esoType = esoType;
                    record.remainingDays = String.valueOf(remainingWorkdays);
                    record.linkAddress = linkPrefix + esoTaskId;
                    // 按有效责任人把当前模块任务提醒合并到待发送邮件Map。
                    for (int k = 0; k < responsibleList.size(); k++) {
                        addRecordToPersonMail(context, personMailMap, responsibleList.get(k), section, record, true);
                    }
                }

                // 模块任务已到期且颜色为空，认定为到期未审核。
                // 获取ESO模块任务颜色标识。
                String esoColor = UIUtil.getValue(esoTaskMap, ATTR_COLOR_IDENTIFICATION);
                // 判断ESO模块任务是否已到期并且颜色标识未设置。
                if (!finishDate.isAfter(today) && UIUtil.isNullOrEmpty(esoColor)) {
                    // 获取ESO模块任务当前审核次数。
                    String reviewCount = UIUtil.getValue(esoTaskMap, ATTR_REVIEW_COUNT);
                    // 默认邮件展示的下一次审核次数为1。
                    int nextReviewCount = 1;
                    // 审核次数为数字时，邮件中展示当前次数加1。
                    try {
                        nextReviewCount = Integer.parseInt(reviewCount) + 1;
                    } catch (Exception ignored) {
                    }
                    // 创建ESO模块任务的到期未审核邮件记录。
                    ReminderRecord record = new ReminderRecord();
                    record.projectName = projectName;
                    record.taskId = esoTaskId;
                    record.taskName = esoTaskName;
                    record.gateNode = gateNode;
                    record.esoType = esoType;
                    record.reviewResult = "R(\u672a\u5ba1\u6838)";
                    // 只在邮件中展示审核次数加1，不回写属性。
                    record.nextReviewCount = String.valueOf(nextReviewCount);
                    record.linkAddress = linkPrefix + esoTaskId;
                    // 到期未审核同时通知模块任务责任人及其直线经理。
                    for (int k = 0; k < responsibleList.size(); k++) {
                        addRecordToPersonMail(context, personMailMap, responsibleList.get(k), SECTION_OVERDUE, record, true);
                    }
                    // 整椅经理存在时，额外通知项目整椅经理。
                    if (UIUtil.isNotNullAndNotEmpty(chairManager)) {
                        addRecordToPersonMail(context, chairMailMap, chairManager, SECTION_OVERDUE, record, false);
                    }
                }

                // 红黄审核提醒只看ESO模块任务下最新签发表，最新规则为先比较审核次数，次数相同再比较创建时间。
                // 初始化最新签发表记录对象。
                ReviewRecord latestReview = null;
                // 查询ESO模块任务下的签发表记录。
                MapList reviewList = esoTaskObj.getRelatedObjects(context, REL_TASK_TO_REVIEW, TYPE_REVIEW_RECORD,
                        reviewSelects, emptyRelSelects, false, true, (short) 1, "", "", 0);
                // 没有签发表记录则无法做红黄审核提醒。
                if (reviewList == null || reviewList.isEmpty()) {
                    continue;
                }
                // 遍历签发表记录，挑选最新的一条。
                for (int j = 0; j < reviewList.size(); j++) {
                    // 获取当前签发表Map。
                    Map reviewMap = (Map) reviewList.get(j);
                    // 创建签发表比较对象。
                    ReviewRecord review = new ReviewRecord();
                    // 设置签发表id。
                    review.id = UIUtil.getValue(reviewMap, SELECT_ID);
                    // 设置签发表名称。
                    review.name = UIUtil.getValue(reviewMap, SELECT_NAME);
                    // 设置签发表颜色状态。
                    review.phaseState = UIUtil.getValue(reviewMap, ATTR_PHASE_STATE);
                    // 解析签发表创建时间。
                    review.originated = parseDateTime(UIUtil.getValue(reviewMap, SELECT_ORIGINATED));
                    // 解析审核次数，解析失败按0处理。
                    try {
                        review.count = Integer.parseInt(UIUtil.getValue(reviewMap, ATTR_REVIEW_RECORD_COUNT));
                    } catch (Exception ignored) {
                        review.count = 0;
                    }
                    // 取最大审核次数；次数相同取originated更晚的签发表。
                    if (latestReview == null || review.count > latestReview.count
                            || (review.count == latestReview.count && review.originated != null
                            && (latestReview.originated == null || review.originated.isAfter(latestReview.originated)))) {
                        latestReview = review;
                    }
                }
                // 没有可用签发表或签发表创建时间为空时，跳过红黄审核提醒。
                if (latestReview == null || latestReview.originated == null) {
                    continue;
                }

                // 签发表颜色用JF_PhaseState判断：Y为黄色第7个工作日提醒，R为红色第3个工作日提醒。
                // 初始化签发表颜色值。
                String reviewColor = "";
                // 初始化签发表创建后的提醒工作日。
                int reviewReminderDays = 0;
                // 黄色签发表创建后第7个工作日提醒。
                if (PHASE_STATE_YELLOW.equalsIgnoreCase(latestReview.phaseState)) {
                    reviewColor = COLOR_YELLOW;
                    reviewReminderDays = YELLOW_REVIEW_REMINDER_WORKDAYS;
                // 红色签发表创建后第3个工作日提醒。
                } else if (PHASE_STATE_RED.equalsIgnoreCase(latestReview.phaseState)) {
                    reviewColor = COLOR_RED;
                    reviewReminderDays = RED_REVIEW_REMINDER_WORKDAYS;
                }
                // 非红黄签发表或未到精确提醒工作日时跳过。
                if (UIUtil.isNullOrEmpty(reviewColor) || getWorkdayCount(latestReview.originated.toLocalDate(), today) != reviewReminderDays) {
                    continue;
                }

                // 签发表达到提醒日期后，邮件对象直接使用ESO模块任务。
                ReminderRecord record = new ReminderRecord();
                record.projectName = projectName;
                record.taskId = esoTaskId;
                record.taskName = esoTaskName;
                record.gateNode = gateNode;
                record.esoType = esoType;
                record.reviewName = latestReview.name;
                record.reviewResult = convertReviewResult(latestReview.phaseState);
                record.linkAddress = linkPrefix + esoTaskId;
                //add by LIUJR 20260616 红色和黄色审核记录使用不同邮件模板区域
                String reviewSection = COLOR_YELLOW.equalsIgnoreCase(reviewColor) ? SECTION_REVIEW_YELLOW : SECTION_REVIEW_RED;
                //end
                // 按模块任务责任人合并到红黄审核提醒区域。
                for (int k = 0; k < responsibleList.size(); k++) {
                    addRecordToPersonMail(context, personMailMap, responsibleList.get(k), reviewSection, record, true);
                }
            }

            // 读取邮件Page模板，本次任务分派人邮件和整椅经理邮件共用同一份模板。
            String template = JF_PublicMethodClass_mxJPO.getPageHTMLResourceFile(context, EMAIL_PAGE_NAME, EMAIL_LANGUAGE_ZH);
            // 生成本次发送统一使用的统计截止日期，避免不同邮件重复获取系统日期。
            String reportDate = today.format(OUTPUT_DATE_FORMATTER);
            // 同一个人的多条提醒在内存中汇总完成后再统一发送。
            sendReminderMail(context, personMailMap, template, reportDate);
            sendReminderMail(context, chairMailMap, template, reportDate);
        } catch (Exception e) {
            JF_LOGGER.error("sendESOTaskReminderEmail error", e);
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
            JF_LOGGER.info("sendESOTaskReminderEmail end");
        }
    }

    /**
     * 1. 按收件人合并发送ESO提醒邮件；
     * 2. 同一个收件人的10天提醒、3天提醒、到期未审核、黄色审核记录、红色审核记录统一合并到同一封邮件；
     * 3. 邮件正文从Page模板读取固定区域，再根据ESO模块任务分段填充table列表；
     * @param context
     * @param mailMap
     * @param template
     * @param reportDate
     * @author LIUJR
     * @throws
     * @return void
     * @date 2026/6/16 15:28
     * @description 发送ESO提醒汇总邮件
     */
    private static void sendReminderMail(Context context, Map mailMap, String template, String reportDate) throws Exception {
        // 遍历按人员合并好的邮件数据。
        for (Object key : mailMap.keySet()) {
            // 获取当前人员的邮件数据。
            MailData mailData = (MailData) mailMap.get(key);
            // 当前人员及相关收件人没有邮箱时跳过发送。
            if (mailData.emailSet.isEmpty()) {
                continue;
            }
            // 使用Jsoup解析HTML模板，方便替换指定节点。
            Document doc = Jsoup.parse(template);
            // 获取报表日期节点。
            Element dateEle = doc.getElementById("reportDate");
            // 设置邮件统计截止日期。
            if (dateEle != null) {
                dateEle.text(reportDate);
            }
            //add by LIUJR 20260616 邮件固定文案放入模板，代码只替换占位并填充table
            boolean hasContent = fillMailContent(doc, mailData);
            if (!hasContent) {
                continue;
            }
            //end
            // 创建邮件HTML正文BodyPart。
            BodyPart msgBodyPart = new MimeBodyPart();
            // 设置邮件正文编码，避免中文乱码。
            msgBodyPart.setContent(doc.toString(), "text/html;charset=utf-8");
            // 创建邮件复合消息体。
            MimeMultipart multipart = new MimeMultipart();
            // 将HTML正文放入消息体。
            multipart.addBodyPart(msgBodyPart);
            // 将去重后的邮箱集合转换为逗号分隔的收件人字符串。
            StringList toEmailList = new StringList();
            toEmailList.addAll(mailData.emailSet);
            JF_LOGGER.info("mailData.emailSet:{}", mailData.emailSet.toString());
            toEmailList.add("liujr@tecwin.com");
            toEmailList.add("chencb@tecwin.com");
            toEmailList.add("linh@tecwin.com");
            HashSet<String> hashSet = new HashSet<>();
            hashSet.addAll(toEmailList);
            toEmailList = StringList.create(hashSet);
            // 调用项目公共邮件工具发送提醒邮件。
            JF_SendEmailUtils_mxJPO.SendEmail(context, toEmailList.join(","), EMAIL_SUBJECT, multipart);
        }
    }

    /**
     * 1. 将某个ESO模块任务提醒数据放入指定人员的邮件汇总；
     * 2. 模块任务责任人邮件收件人包含本人和直线经理，整椅经理汇总邮件只发送给整椅经理本人；
     * 3. 责任人维度下按照提醒类型和ESO模块任务分组，后续用于生成分段table；
     * @param context
     * @param mailMap
     * @param personName
     * @param section
     * @param record
     * @param includeLineManager
     * @author LIUJR
     * @throws
     * @return void
     * @date 2026/6/16 15:28
     * @description 添加ESO提醒数据到人员邮件汇总
     */
    private static void addRecordToPersonMail(Context context, Map mailMap, String personName, String section, ReminderRecord record, boolean includeLineManager) throws Exception {
        // 收件人账号为空时不处理。
        if (UIUtil.isNullOrEmpty(personName)) {
            return;
        }
        // 根据人员账号获取已汇总的邮件数据。
        MailData mailData = (MailData) mailMap.get(personName);
        // 第一次遇到该人员时，初始化邮件数据和收件邮箱。
        if (mailData == null) {
            mailData = new MailData();
            // 获取人员本人邮箱。
            String email = JF_NotificationUtils_mxJPO.getPersonEmail(context, personName, null);
            // 人员邮箱存在时加入收件人集合。
            if (UIUtil.isNotNullAndNotEmpty(email)) {
                mailData.emailSet.add(email);
            }
            // 模块任务责任人提醒需要同时通知直线经理，整椅经理汇总邮件不需要。
            if (includeLineManager) {
                // 获取当前人员直线经理id。
                String lineManagerId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, personName);
                // 直线经理存在时继续获取邮箱。
                if (UIUtil.isNotNullAndNotEmpty(lineManagerId)) {
                    // 根据直线经理id获取邮箱。
                    String lineManagerEmail = JF_NotificationUtils_mxJPO.getPersonEmail(context, null, lineManagerId);
                    // 直线经理邮箱存在时加入同一封邮件收件人集合。
                    if (UIUtil.isNotNullAndNotEmpty(lineManagerEmail)) {
                        mailData.emailSet.add(lineManagerEmail);
                    }
                }
            }
            // 将初始化后的人员邮件数据放回汇总Map。
            mailMap.put(personName, mailData);
        }
        // 默认写入到期前10天提醒区域。
        Map targetMap = mailData.due10Map;
        // 到期前3天提醒写入3天区域。
        if (SECTION_DUE3.equals(section)) {
            targetMap = mailData.due3Map;
        // 到期未审核提醒写入到期未审核区域。
        } else if (SECTION_OVERDUE.equals(section)) {
            targetMap = mailData.overdueMap;
        // 黄色审核记录提醒写入黄色审核记录区域。
        } else if (SECTION_REVIEW_YELLOW.equals(section)) {
            targetMap = mailData.yellowReviewMap;
        // 红色审核记录提醒写入红色审核记录区域。
        } else if (SECTION_REVIEW_RED.equals(section)) {
            targetMap = mailData.redReviewMap;
        }
        // 模块任务分组key包含项目、任务名称和任务id，避免同名模块任务被合并。
        String taskKey = record.projectName + "|" + record.taskName + "|" + record.taskId;
        // 获取当前模块任务已有的提醒行。
        List list = (List) targetMap.get(taskKey);
        // 第一次出现该模块任务时初始化行列表。
        if (list == null) {
            list = new ArrayList();
            targetMap.put(taskKey, list);
        }
        // 将当前模块任务提醒行加入任务分组。
        list.add(record);
    }

    /**
     * 1. 从ESO模块任务查询结果中获取分派人，并排除拥有JfESOAdmin角色的ESO审核员；
     * 2. 模块任务没有分派人或排除后没有有效分派人时，回退获取任务owner；
     * 3. owner拥有JfESOAdmin角色时不加入收件人，最终返回空列表并跳过该模块任务；
     * 4. 角色判断结果按人员缓存，避免同一批任务中重复查询人员角色；
     * @param context
     * @param taskMap ESO模块任务查询结果
     * @param esoMemberCache 人员ESO角色判断缓存
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2026/8/3
     * @description 获取ESO模块任务邮件责任人
     */
    private static StringList getReminderResponsibleList(Context context, Map taskMap, Map esoMemberCache) throws Exception {
        // 定义最终返回的有效分派人列表。
        StringList responsibleList = new StringList();
        // 从模块任务Map中取分派人字段，可能是StringList、List或单个字符串。
        Object assignedObj = taskMap.get(SELECT_ASSIGNED_TASK_PERSON);
        // 统一转换为普通List，方便后续遍历。
        List assignedValues = new ArrayList();
        // 分派人字段为StringList时直接加入。
        if (assignedObj instanceof StringList) {
            assignedValues.addAll((StringList) assignedObj);
        // 分派人字段为List时直接加入。
        } else if (assignedObj instanceof List) {
            assignedValues.addAll((List) assignedObj);
        // 分派人字段为单值字符串时，按项目约定通过UIUtil从Map中取值。
        } else if (assignedObj != null && UIUtil.isNotNullAndNotEmpty(UIUtil.getValue(taskMap, SELECT_ASSIGNED_TASK_PERSON))) {
            assignedValues.add(UIUtil.getValue(taskMap, SELECT_ASSIGNED_TASK_PERSON));
        }
        // 遍历模块任务全部分派人。
        for (int i = 0; i < assignedValues.size(); i++) {
            // 获取分派人账号。
            String personName = String.valueOf(assignedValues.get(i)).trim();
            // 账号为空时跳过。
            if (UIUtil.isNullOrEmpty(personName)) {
                continue;
            }
            // 优先从缓存读取角色判断，首次遇到该人员时才查询系统角色。
            Boolean esoMember = (Boolean) esoMemberCache.get(personName);
            if (esoMember == null) {
                esoMember = Boolean.valueOf(PersonUtil.getAssignments(context, personName).contains(ROLE_ESO_ADMIN));
                esoMemberCache.put(personName, esoMember);
            }
            // 排除ESO审核员，并避免同一模块任务重复加入同一个人。
            if (!esoMember.booleanValue() && !responsibleList.contains(personName)) {
                responsibleList.add(personName);
            }
        }
        // 有效分派人存在时优先使用分派人，不再处理owner。
        if (!responsibleList.isEmpty()) {
            return responsibleList;
        }
        // 无有效分派人时回退到模块任务owner。
        String owner = UIUtil.getValue(taskMap, SELECT_OWNER);
        owner = owner == null ? "" : owner.trim();
        if (UIUtil.isNullOrEmpty(owner)) {
            return responsibleList;
        }
        // owner同样需要排除ESO审核员，并复用人员角色缓存。
        Boolean ownerIsEsoMember = (Boolean) esoMemberCache.get(owner);
        if (ownerIsEsoMember == null) {
            ownerIsEsoMember = Boolean.valueOf(PersonUtil.getAssignments(context, owner).contains(ROLE_ESO_ADMIN));
            esoMemberCache.put(owner, ownerIsEsoMember);
        }
        if (!ownerIsEsoMember.booleanValue()) {
            responsibleList.add(owner);
        }
        return responsibleList;
    }

    /**
     * 1. 计算两个日期之间的工作日数量，只按周一到周五；
     * 2. 开始日期不计入，结束日期计入；
     * 3. 用于判断到期前10/3工作日和签发表创建后7/3工作日；
     * @param startDate
     * @param endDate
     * @author LIUJR
     * @throws
     * @return int
     * @date 2026/6/16 15:28
     * @description 计算工作日间隔
     */
    private static int getWorkdayCount(LocalDate startDate, LocalDate endDate) {
        // 初始化工作日数量。
        int count = 0;
        // 任一日期为空或两日期相同，工作日间隔为0。
        if (startDate == null || endDate == null || startDate.equals(endDate)) {
            return count;
        }
        // 从开始日期向结束日期逐天移动。
        LocalDate date = startDate;
        // 正向计算：开始日期不计入，结束日期计入。
        if (startDate.isBefore(endDate)) {
            while (date.isBefore(endDate)) {
                // 先移动到下一天。
                date = date.plusDays(1);
                // 只有周一到周五计入工作日。
                if (isWorkday(date)) {
                    count++;
                }
            }
        // 反向计算：用于ESO模块任务计划日期早于今天时返回负数。
        } else {
            while (date.isAfter(endDate)) {
                // 当前日期是工作日时扣减一天。
                if (isWorkday(date)) {
                    count--;
                }
                // 移动到前一天。
                date = date.minusDays(1);
            }
        }
        // 返回工作日间隔。
        return count;
    }

    /**
     * 1. 从邮件模板中读取固定的五类提醒区域；
     * 2. 每个ESO模块任务有数据时复制一份对应区域，并替换项目名称、任务名称、审核次数等占位；
     * 3. 代码只填充table数据行，固定文案统一维护在HTML模板中；
     * @param doc
     * @param mailData
     * @author LIUJR
     * @throws
     * @return boolean
     * @date 2026/6/16 15:28
     * @description 填充ESO提醒邮件模板
     */
    private static boolean fillMailContent(Document doc, MailData mailData) {
        // 获取邮件正文容器。
        Element contentEle = doc.getElementById("content");
        // 正文容器不存在时，不发送空邮件。
        if (contentEle == null) {
            return false;
        }
        // 从content中读取五个固定模板区域，随后清空content只保留本次有数据的区域。
        Element due10SourceEle = doc.getElementById("due10SectionTemplate");
        Element due3SourceEle = doc.getElementById("due3SectionTemplate");
        Element overdueSourceEle = doc.getElementById("overdueSectionTemplate");
        Element yellowReviewSourceEle = doc.getElementById("yellowReviewSectionTemplate");
        Element redReviewSourceEle = doc.getElementById("redReviewSectionTemplate");
        // 清空content前先复制模板区域，避免清空后模板节点丢失。
        Element due10TemplateEle = due10SourceEle == null ? null : due10SourceEle.clone();
        Element due3TemplateEle = due3SourceEle == null ? null : due3SourceEle.clone();
        Element overdueTemplateEle = overdueSourceEle == null ? null : overdueSourceEle.clone();
        Element yellowReviewTemplateEle = yellowReviewSourceEle == null ? null : yellowReviewSourceEle.clone();
        Element redReviewTemplateEle = redReviewSourceEle == null ? null : redReviewSourceEle.clone();
        // 清空模板示例内容，后续只追加实际需要发送的区域。
        contentEle.empty();
        // 是否生成了至少一个提醒区域。
        boolean hasContent = false;
        // 按模板中固定的10天到期区域填充ESO模块任务行。
        hasContent = appendTemplateSection(contentEle, due10TemplateEle, mailData.due10Map, "DUE") || hasContent;
        // 按模板中固定的3天到期区域填充ESO模块任务行。
        hasContent = appendTemplateSection(contentEle, due3TemplateEle, mailData.due3Map, "DUE") || hasContent;
        // 按模板中固定的到期未审核区域填充ESO模块任务行。
        hasContent = appendTemplateSection(contentEle, overdueTemplateEle, mailData.overdueMap, "OVERDUE") || hasContent;
        // 按模板中固定的黄色审核记录区域填充ESO模块任务行。
        hasContent = appendTemplateSection(contentEle, yellowReviewTemplateEle, mailData.yellowReviewMap, "REVIEW") || hasContent;
        // 按模板中固定的红色审核记录区域填充ESO模块任务行。
        hasContent = appendTemplateSection(contentEle, redReviewTemplateEle, mailData.redReviewMap, "REVIEW") || hasContent;
        // 返回是否存在可发送的正文内容。
        return hasContent;
    }

    /**
     * 1. 根据某一类提醒数据复制HTML模板区域；
     * 2. 替换项目名称、ESO模块任务名称和审核次数占位；
     * 3. 根据区域类型填充不同table列；
     * @param contentEle
     * @param templateEle
     * @param sectionMap
     * @param rowType
     * @author LIUJR
     * @throws
     * @return boolean
     * @date 2026/6/16 15:28
     * @description 按提醒类型填充邮件区域
     */
    private static boolean appendTemplateSection(Element contentEle, Element templateEle, Map sectionMap, String rowType) {
        // 模板或数据为空时，不生成该类型提醒区域。
        if (templateEle == null || sectionMap == null || sectionMap.isEmpty()) {
            return false;
        }
        // 标记当前类型是否生成过区域。
        boolean hasContent = false;
        // 遍历每个ESO模块任务分组。
        for (Object key : sectionMap.keySet()) {
            // 获取该ESO模块任务的提醒行。
            List list = (List) sectionMap.get(key);
            // 当前ESO模块任务没有提醒行时跳过。
            if (list == null || list.isEmpty()) {
                continue;
            }
            // 取第一行数据作为ESO模块任务标题信息来源。
            ReminderRecord first = (ReminderRecord) list.get(0);
            // 复制模板区域，避免改动原始模板。
            Element sectionEle = templateEle.clone();
            // 删除模板id，避免最终邮件中出现重复id。
            sectionEle.removeAttr("id");
            // 替换项目显示名称。
            Element projectNameEle = sectionEle.getElementsByClass("projectName").first();
            if (projectNameEle != null) {
                projectNameEle.text(first.projectName);
            }
            // 替换ESO模块任务名称；HTML中的class名称保持兼容，无需同步修改Page模板。
            Element parentTaskNameEle = sectionEle.getElementsByClass("parentTaskName").first();
            if (parentTaskNameEle != null) {
                parentTaskNameEle.text(first.taskName);
            }
            // 替换到期未审核区域的下一次审核次数。
            Element nextReviewCountEle = sectionEle.getElementsByClass("nextReviewCount").first();
            if (nextReviewCountEle != null) {
                nextReviewCountEle.text(first.nextReviewCount);
            }
            // 获取当前区域表格体。
            Element tbody = sectionEle.getElementsByTag("tbody").first();
            // 表格体存在时填充ESO模块任务行。
            if (tbody != null) {
                // 构建当前ESO模块任务行。
                StringBuilder rowHtml = new StringBuilder();
                // 遍历ESO模块任务提醒记录。
                for (int i = 0; i < list.size(); i++) {
                    // 获取当前ESO模块任务提醒记录。
                    ReminderRecord record = (ReminderRecord) list.get(i);
                    // 生成任务链接，邮件中只显示“任务链接”。
                    String linkHtml = "<a href='" + StringEscapeUtils.escapeHtml4(record.linkAddress) + "' target='_blank'>" + LINK_LABEL_TASK + "</a>";
                    // 到期前提醒行：序号、任务名称、门节点、TKO类型、剩余天数、链接。
                    if ("DUE".equals(rowType)) {
                        rowHtml.append("<tr><td>").append(i + 1).append("</td><td>").append(StringEscapeUtils.escapeHtml4(record.taskName))
                                .append("</td><td class='gate-node-column'>").append(StringEscapeUtils.escapeHtml4(record.gateNode))
                                .append("</td><td>").append(StringEscapeUtils.escapeHtml4(record.esoType))
                                .append("</td><td>").append(StringEscapeUtils.escapeHtml4(record.remainingDays)).append("</td><td>").append(linkHtml).append("</td></tr>");
                    // 到期未审核提醒行：序号、任务名称、门节点、TKO类型、审核结果、链接。
                    } else if ("OVERDUE".equals(rowType)) {
                        rowHtml.append("<tr><td>").append(i + 1).append("</td><td>").append(StringEscapeUtils.escapeHtml4(record.taskName))
                                .append("</td><td class='gate-node-column'>").append(StringEscapeUtils.escapeHtml4(record.gateNode))
                                .append("</td><td>").append(StringEscapeUtils.escapeHtml4(record.esoType))
                                .append("</td><td>").append(StringEscapeUtils.escapeHtml4(record.reviewResult)).append("</td><td>").append(linkHtml).append("</td></tr>");
                    // 红黄审核记录提醒行：序号、任务名称、门节点、TKO类型、签发记录、审核结果、链接。
                    } else if ("REVIEW".equals(rowType)) {
                        rowHtml.append("<tr><td>").append(i + 1).append("</td><td>").append(StringEscapeUtils.escapeHtml4(record.taskName))
                                .append("</td><td class='gate-node-column'>").append(StringEscapeUtils.escapeHtml4(record.gateNode))
                                .append("</td><td>").append(StringEscapeUtils.escapeHtml4(record.esoType))
                                .append("</td><td>").append(StringEscapeUtils.escapeHtml4(record.reviewName)).append("</td><td>")
                                .append(StringEscapeUtils.escapeHtml4(record.reviewResult)).append("</td><td>").append(linkHtml).append("</td></tr>");
                    }
                }
                // 写入模板table的tbody。
                tbody.html(rowHtml.toString());
            }
            // 将填充后的区域追加到正文容器。
            contentEle.appendChild(sectionEle);
            // 标记当前类型存在可发送内容。
            hasContent = true;
        }
        // 返回当前类型是否生成过正文。
        return hasContent;
    }

    /**
     * 1. 判断日期是否为工作日；
     * 2. 当前需求只按周一到周五处理，不识别法定节假日或调休；
     * 3. 用于各类提醒工作日间隔计算，定时器是否执行由脚本配置控制；
     * @param date
     * @author LIUJR
     * @throws
     * @return boolean
     * @date 2026/6/16 15:28
     * @description 判断是否工作日
     */
    private static boolean isWorkday(LocalDate date) {
        // 非空且不是周六、周日，即视为工作日。
        return date != null && date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    /**
     * 1. 将ENOVIA日期字符串解析为LocalDate；
     * 2. 内部复用日期时间解析逻辑，解析成功后只取日期部分；
     * 3. 用于ESO模块任务计划结束日期的工作日判断；
     * @param value
     * @author LIUJR
     * @throws
     * @return java.time.LocalDate
     * @date 2026/6/16 15:28
     * @description 解析日期字符串
     */
    private static LocalDate parseLocalDate(String value) {
        // 先按日期时间解析。
        LocalDateTime dateTime = parseDateTime(value);
        // 解析失败返回null，解析成功只保留日期部分。
        return dateTime == null ? null : dateTime.toLocalDate();
    }

    /**
     * 1. 解析ENOVIA常见日期时间字符串；
     * 2. 支持英文AM/PM格式、24小时格式、yyyy-MM-dd格式、yyyy/MM/dd格式和纯日期格式；
     * 3. 解析失败返回null，由调用方决定跳过任务或使用回退值；
     * @param value
     * @author LIUJR
     * @throws
     * @return java.time.LocalDateTime
     * @date 2026/6/16 15:28
     * @description 解析日期时间字符串
     */
    private static LocalDateTime parseDateTime(String value) {
        // 空字符串不参与解析。
        if (UIUtil.isNullOrEmpty(value)) {
            return null;
        }
        // 去除日期字符串前后空格。
        String val = value.trim();
        // 依次尝试按日期时间格式解析。
        for (int i = 0; i < DATE_TIME_FORMATTERS.length; i++) {
            try {
                return LocalDateTime.parse(val, DATE_TIME_FORMATTERS[i]);
            } catch (Exception ignored) {
            }
        }
        // 依次尝试按纯日期格式解析，解析成功后补当天零点。
        for (int i = 0; i < DATE_FORMATTERS.length; i++) {
            try {
                return LocalDate.parse(val, DATE_FORMATTERS[i]).atStartOfDay();
            } catch (Exception ignored) {
            }
        }
        // 兼容旧代码常用SimpleDateFormat解析英文AM/PM格式。
        try {
            Date date = new SimpleDateFormat(DEFAULT_DATE_TIME_PATTERN, Locale.US).parse(val);
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        } catch (Exception ignored) {
        }
        // 所有格式都无法解析时返回null。
        return null;
    }

    /**
     * 将ESO任务颜色或签发表状态转换为邮件中的审核结果。
     *
     * @param value 任务颜色或签发表状态
     * @return java.lang.String R、Y、G或R(未审核)
     * @throws Exception 无
     * @author LIUJR
     * @date 2026/8/14
     * @description 统一五类ESO到期提醒邮件中的审核结果显示值。
     */
    private static String convertReviewResult(String value) {
        if (UIUtil.isNullOrEmpty(value)) {
            return "";
        }
        if ("RN".equalsIgnoreCase(value)) {
            return "R(\u672a\u5ba1\u6838)";
        }
        if (COLOR_RED.equalsIgnoreCase(value) || PHASE_STATE_RED.equalsIgnoreCase(value)) {
            return "R";
        }
        if (COLOR_YELLOW.equalsIgnoreCase(value) || PHASE_STATE_YELLOW.equalsIgnoreCase(value)) {
            return "Y";
        }
        if ("green".equalsIgnoreCase(value) || "G".equalsIgnoreCase(value)) {
            return "G";
        }
        return value;
    }

    private static class MailData {
        private Set emailSet = new LinkedHashSet();
        private Map due10Map = new LinkedHashMap();
        private Map due3Map = new LinkedHashMap();
        private Map overdueMap = new LinkedHashMap();
        private Map yellowReviewMap = new LinkedHashMap();
        private Map redReviewMap = new LinkedHashMap();
    }

    private static class ReminderRecord {
        private String projectName = "";
        private String taskId = "";
        private String taskName = "";
        private String gateNode = "";
        private String esoType = "";
        private String remainingDays = "";
        private String reviewName = "";
        private String reviewResult = "";
        private String nextReviewCount = "";
        private String linkAddress = "";
    }

    private static class ReviewRecord {
        private String id = "";
        private String name = "";
        private String phaseState = "";
        private int count = 0;
        private LocalDateTime originated = null;
    }
}
