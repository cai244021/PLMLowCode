import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dassault_systemes.enovia.unifiedchange.kernelutils.MqlCommand;
import com.matrixone.apps.common.InboxTask;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jdl.bosContext;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import matrix.db.*;
import matrix.util.StringList;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.matrixone.fcs.tools.FcsIndexGen.executor;

/**
 * @ClassName JF_FormalECRBubble_mxJPO
 * @Author: LIUJR
 * @CreateDate: 2025/12/29 16:32
 * @UpdateRemark:
 * @Version: 1.0
 * @Description:  ECR零件自动冒泡
 */
public class JF_FormalECRBubble_mxJPO implements DomainConstants,JF_PLMConstants_mxJPO{
    private static final Logger JF_LOGGER =  LoggerFactory.getLogger(JF_FormalECRBubble_mxJPO.class);
    private static final Logger ThreadLog = LoggerFactory.getLogger("MY_CUSTOM_LOGGER");
    private static final String  mql1 = "mod bus $1 owner $2 organization $3 project $4;";
    private static final String  mql = "mod bus $1 owner $2 organization $3 project $4 current $5;";
    private static final String  mql2 = "mod connection $1 owner $2 organization $3 project $4;";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a", Locale.US);

    /**
     * ecr审核状态中Route项目经理审批节点的action trigger
     * 进行是否冒泡校验  如果冒泡开关打开了，项目参与冒泡，项目目前没有ecr冒泡
     * 20260317 SIT这个ECR没生成ICO的会签任务。ICO总成是通过冒泡生成的  将会签生成放进来
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2026/1/21 10:09
     * @description
     */
    public static void partAutoBubbleWithFormalECR(Context context, String[] args) throws Exception{
        try {
            //1. 增加一个是否冒泡的全局开关--通过配置文件指定Bubbling.switch=on or off
            Boolean flag = Boolean.FALSE;
            String strSwitch = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"Bubbling.switch"});
            JF_LOGGER.info("Bubbling.switch:{}", strSwitch);
            JF_LOGGER.info("context:{}", context.getUser());
            if ("off".equalsIgnoreCase(strSwitch)) {
                flag = Boolean.TRUE;
            }
            String ecrId = args[0];
            JF_LOGGER.info("ecrId:{}", ecrId);
            //保存升版冒泡的Map   旧id=冒泡后的id
            DomainObject ecrObject = DomainObject.newInstance(context);
            ecrObject.setId(ecrId);
            String type = ecrObject.getInfo(context,DomainConstants.SELECT_TYPE);
            JF_LOGGER.info("type:{}",type);
            if("JFNewECR".equalsIgnoreCase(type)){// add by caipan 20260329 旧ECR不参与冒泡，但是需要创建会签任务
                //表示不能冒泡 需要创建会签任务
                JF_NewECRService_mxJPO jfNewECRServiceMxJPO = new JF_NewECRService_mxJPO();
                jfNewECRServiceMxJPO.createCountersignTaskAndECOInCountersign(context, args);
                return;
            }
            //获取ECR的所属项目
            String ecrProjectId = ecrObject.getInfo(context, "from[" + REL_JFChange2Project + "].to.id");
            JF_LOGGER.info("ECR的所属项目:{}", ecrProjectId);
            DomainObject projectObject = DomainObject.newInstance(context, ecrProjectId);
            //2. 项目是否参与冒泡:JF_JoinBubbling,如果该属性为Y，就代码当前项目参与冒泡，为N的情况就不参与冒泡，直接结束
            String strJoinBubbling = projectObject.getAttributeValue(context, Attr_JF_JoinBubbling);
            JF_LOGGER.info("项目是否参与冒泡:{}", strJoinBubbling);
            if ("N".equalsIgnoreCase(strJoinBubbling)) {
                flag = Boolean.TRUE;
            }
            //3. 判断当前ECR关联的项目属性JFBubblingFlag 是否为Y  是否可以冒泡
            String strJFBubblingFlag = projectObject.getAttributeValue(context, Attr_JFBubblingFlag);
            JF_LOGGER.info("项目是否可以冒泡:{}", strJFBubblingFlag);
            if ("N".equalsIgnoreCase(strJFBubblingFlag)) {
                flag = Boolean.TRUE;
            }
            if (flag) {
                //表示不能冒泡 需要创建会签任务
                JF_NewECRService_mxJPO jfNewECRServiceMxJPO = new JF_NewECRService_mxJPO();
                jfNewECRServiceMxJPO.createCountersignTaskAndECOInCountersign(context, args);
                return;
            }
//            JF_LOGGER.info("将项目设置为冒泡中！！！！！！！！！！！！");
//            //将项目设置为冒泡中
            projectObject.setAttributeValue(context, Attr_JFBubblingFlag, "N");
            projectObject.setAttributeValue(context, Attr_JF_BubblingECR, ecrObject.getInfo(context, DomainConstants.SELECT_NAME));
            JF_LOGGER.info("条件满足开始冒泡1111111111111111111111");
//            //条件满足开始冒泡
//            Job job = new Job("JF_FormalECRBubble", "partAutoBubbleWithFormalECRBegin", args);
//            job.setTitle("Project Space:" + projectObject.getInfo(context, SELECT_NAME) + "ECR: " + ecrObject.getInfo(context, SELECT_NAME) + "\u5f00\u59cb\u5192\u6ce1\u4e2d");
//            job.setDescription("Project Space:" + projectObject.getInfo(context, SELECT_NAME) + "ECR: " + ecrObject.getInfo(context, SELECT_NAME) + "\u5f00\u59cb\u5192\u6ce1\u4e2d");
//            job.createAndSubmit(context);
//            条件满足开始冒泡
            JF_Util_mxJPO.runAsync( context, new String[]{ecrId}, "JF_FormalECRBubble","partAutoBubbleWithFormalECRBegin");
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
    * 零件自动冒泡功能
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/12/29 16:45
    * @description
    */
    public static void partAutoBubbleWithFormalECRBegin(Context context, String[] args) throws Exception{
        String ecrProjectId = DomainConstants.EMPTY_STRING;
        try {
            ThreadLog.info("partAutoBubbleWithFormalECRBegin························");
            String password = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"admin_platform.internal.password"});
            JF_LOGGER.info("password：{}",password);
            String role = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"admin_platform.internal.role"});
            JF_LOGGER.info("password：{}",password);
            ContextUtil.pushContext(context, USER_Admin_Platform, password, "");
            context.resetRole(role);
            JF_LOGGER.info("context：{}",context.getUser());
            ContextUtil.startTransaction(context, true);
            String ecrId = args[0];
            ThreadLog.info("ecrId:{}", ecrId);
            //保存升版冒泡的Map   旧id=冒泡后的id
            Map<String, String> revBubbleMap = new HashMap<>();
            StringList bubbleAllList = new StringList();
            HashSet<String> bubbleSet = new HashSet<>();
            HashSet<String> bubbleGXSet = new HashSet<>();
            //记录本次任务中已经处理过的变更零件，避免多个整椅共用同一下级结构时重复向下遍历
            Set<String> processedPartIdSet = new HashSet<>();
            //记录本次任务中已经向上传播过的父件，避免同一父件被多个变更子件重复向上递归
            Set<String> propagatedParentIdSet = new HashSet<>();
            //记录没有符合条件上级的父件，重复遇到该父件时仍需按照当前下级补充根节点结果
            Set<String> rootParentIdSet = new HashSet<>();
            DomainObject ecrObject = DomainObject.newInstance(context);
            ecrObject.setId(ecrId);
            ecrObject.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_BubbleStartTime, LocalDateTime.now().format(inputFormatter));
            //获取ECR的所属项目
            ecrProjectId = ecrObject.getInfo(context, "from[" + REL_JFChange2Project + "].to.id");
            ThreadLog.info("ECR的所属项目:{}", ecrProjectId);
            DomainObject projectObject = DomainObject.newInstance(context);
            projectObject.setId(ecrProjectId);
            ThreadLog.info("冒泡条件以满足,进入冒泡程序中......");
            //获取ECR变更清单根节点
            StringList rootPartList = ecrObject.getInfoList(context, "from[" + REL_JFECRRelateRoot + "].to.id");
            //获取ECR JFRelateItem的零件及变更来源
            Map<String, String> partChangeSourceMap = new HashMap<>();
            Map<String, String> alreadyConnIdMap = new HashMap<>();
            StringList changeSourceRelSelectList = new StringList(SELECT_ATTR_JFCHANGESOURCE);
            changeSourceRelSelectList.add(SELECT_RELATIONSHIP_ID);
            ecrObject.getRelatedObjects(
                    context,
                    REL_JFRELATEITEM,
                    TYPE_VPMReference,
                    JF_Util_mxJPO.basicBolistSel(),
                    changeSourceRelSelectList,
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    0
            ).stream().forEach(m -> {
                Map map = (Map) m;
                String partId = UIUtil.getValue(map, SELECT_ID);
                partChangeSourceMap.put(partId, UIUtil.getValue(map, SELECT_ATTR_JFCHANGESOURCE));
                alreadyConnIdMap.put(partId, UIUtil.getValue(map, SELECT_RELATIONSHIP_ID));
            });
            JF_LOGGER.info("ECR的root节点:{}", rootPartList);
            //获取项目中的所有的所属件
            StringList psPartList = (StringList) projectObject.getRelatedObjects(
                    context,
                    rel_JFProject2RootPart,
                    TYPE_VPMReference,
                    JF_Util_mxJPO.basicBolistSel(),
                    JF_Util_mxJPO.basicRellistSel(),
                    false,
                    true,
                    (short) 1,
                    "",
                    "attribute[JF_BelongPart]==Y",
                    0
            ).stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
            //获取项目中的所有的供货件  保存逻辑id  后续冒泡的出来的件逻辑id一致
            StringList psZeroPartList = (StringList) projectObject.getRelatedObjects(
                    context,
                    rel_JFProject2RootPart,
                    TYPE_VPMReference,
                    StringList.create("logicalid"),
                    JF_Util_mxJPO.basicRellistSel(),
                    false,
                    true,
                    (short) 1,
                    "",
                    "attribute[JFZeroPart]==Y",
                    0
            ).stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, "logicalid");
            }).collect(Collectors.toCollection(StringList::new));
            JF_LOGGER.info("项目中的所有的所属件:{}", psPartList);
            JF_LOGGER.info("项目中的所有的供货件:{}", psZeroPartList);
            partAutoBubbleRecursion(context, rootPartList, psPartList, revBubbleMap, ecrId, bubbleSet, bubbleGXSet,
                    psZeroPartList, bubbleAllList, processedPartIdSet, propagatedParentIdSet, rootParentIdSet);
            JF_LOGGER.info("所有冒泡后变更结构的root件:{}", bubbleSet);
            JF_LOGGER.info("bubbleAllList:{}", bubbleAllList);
            JF_LOGGER.info("revBubbleMap:{}", revBubbleMap);
            ThreadLog.info("冒泡程序结束..关联关系开始....");
            DomainObject partObject = DomainObject.newInstance(context);
            String ReviseRelease = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.ReviseRelease", new String[]{});
            String beforePartId = EMPTY_STRING;
            String strId = EMPTY_STRING;
            String preLogicalid = EMPTY_STRING;
            DomainRelationship domainRelationship;
            //需要把根节点筛出来 进行ECR root变更结构关联
            StringList bubbleList = StringList.create(bubbleSet);
            for (int i = 0; i < bubbleList.size(); i++) {
                strId = bubbleList.get(i);
                if (UIUtil.isNullOrEmpty(strId) || "null".equalsIgnoreCase(strId)) {
                    continue;
                }
                partObject.setId(strId);
                preLogicalid = partObject.getInfo(context, "logicalid");
                for (Map.Entry<String, String> entry : revBubbleMap.entrySet()) {
                    if (Objects.equals(strId, entry.getValue())) {
                        beforePartId = entry.getKey();
                        break;
                    }
                }
                //是供货件 关联上ECR
                domainRelationship = ecrObject.addToObject(context, new RelationshipType(REL_JFECRRelateRootBubble), strId);
                domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_ECRID, ecrId);
                domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_BOMQuantity, "1");
                domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_BOMChangeDes, ReviseRelease);
                domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_ChangeBeforeRev, beforePartId);
                domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_BOMChangeQuantity, "0.0");
                domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_BOMBeforeQuantity, "1");
                if (psZeroPartList.contains(preLogicalid)) {
                    domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_freeState, "N");
                } else {
                    domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_freeState, "Y");
                }
            }
            //搭建冒泡后的数据与ECR关联item关系，并设置变更来源
            JF_LOGGER.info("bubbleAllList:{}", bubbleAllList);
            JF_LOGGER.info("partChangeSourceMap:{}", partChangeSourceMap);
            JF_LOGGER.info("alreadyConnIdMap:{}", alreadyConnIdMap);
            //拿出真正的变更结构数据
            StringList stringList = new StringList();
            if (!bubbleGXSet.isEmpty()) {
                for (String item : bubbleAllList) {
                    if (!bubbleGXSet.contains(item)) {
                        stringList.add(item);
                    }
                }
            } else {
                stringList.addAll(bubbleAllList);
            }
            if (!stringList.isEmpty()) {
                connBubblePartToECRRelateItem(context, ecrId, stringList, partChangeSourceMap, alreadyConnIdMap);
            }
            ThreadLog.info("冒泡程序结束..关联关系结束....");
            ecrObject.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_BubbleEndTime, LocalDateTime.now().format(inputFormatter));
            ContextUtil.commitTransaction(context);
            if (!revBubbleMap.isEmpty()) {
                //ECR冒泡后发送替换邮件
                StringList oldIdlist = new StringList();
                revBubbleMap.keySet().forEach(key ->
                        oldIdlist.add(key)
                );
                JF_LOGGER.info("oldIdlist:{}", oldIdlist);
                //自动撤回
                JF_FormalECRBubble_mxJPO jfFormalECRBubbleMxJPO = new JF_FormalECRBubble_mxJPO();
                jfFormalECRBubbleMxJPO.getReviewECRReject(context, oldIdlist, ecrId);
                //冒泡后发送邮件给项目经理可以开始审批了
                JF_LOGGER.info("冒泡后发送邮件给项目经理可以开始审批了!");
                sendEmailToProjectManagerForECR(context, new String[]{ecrProjectId, ecrObject.getInfo(context, SELECT_NAME)});
            }
            ThreadLog.info("发送邮件结束........");
        } catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.info(e.getMessage().toString());
            JF_LOGGER.info(Arrays.toString(e.getStackTrace()));
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        //重新提权限修改项目属性  放开项目冒泡程序
        try {
            ContextUtil.pushContext(context);
            JF_LOGGER.info("context user:{}",context.getUser());
            DomainObject domainObject = DomainObject.newInstance(context, ecrProjectId);
            domainObject.setAttributeValue(context, Attr_JFBubblingFlag, "Y");
            domainObject.setAttributeValue(context, Attr_JF_BubblingECR,"");
        }catch (Exception e) {
            JF_LOGGER.error("domainObject.setAttributeValue error:{}",e.getMessage());
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        //需要创建会签任务  add by ljr 20260317
        JF_NewECRService_mxJPO jfNewECRServiceMxJPO = new JF_NewECRService_mxJPO();
        jfNewECRServiceMxJPO.createCountersignTaskAndECOInCountersign(context, args);
    }

    public void test(Context context, String[] args) throws Exception{
        try {
            ContextUtil.pushContext(context, USER_Admin_Platform, "", "");
            context.resetRole("ctx::VPLMProjectLeader.RD Center.JFSeat");
            ContextUtil.startTransaction(context, true);
            String ecrId = args[0];
            //获取ECR JFRelateItem的零件及变更来源
            Map<String, String> partChangeSourceMap = new HashMap<>();
            Map<String, String> alreadyConnIdMap = new HashMap<>();
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(ecrId);
            StringList changeSourceRelSelectList = new StringList(SELECT_ATTR_JFCHANGESOURCE);
            changeSourceRelSelectList.add(SELECT_RELATIONSHIP_ID);
            domainObject.getRelatedObjects(
                    context,
                    REL_JFRELATEITEM,
                    TYPE_VPMReference,
                    JF_Util_mxJPO.basicBolistSel(),
                    changeSourceRelSelectList,
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    0
            ).stream().forEach(m -> {
                Map map = (Map) m;
                String partId = UIUtil.getValue(map, SELECT_ID);
                partChangeSourceMap.put(partId, UIUtil.getValue(map, SELECT_ATTR_JFCHANGESOURCE));
                alreadyConnIdMap.put(partId, UIUtil.getValue(map, SELECT_RELATIONSHIP_ID));
            });
            JF_LOGGER.info("partChangeSourceMap:{}", partChangeSourceMap);
            StringBuilder relWhereSb = new StringBuilder();
            relWhereSb.append(ecrId);
            relWhereSb.append("  matchlist  '");
            relWhereSb.append(SELECT_ATTRIBUTE_JF_ECRID);
            relWhereSb.append("'");
            relWhereSb.append(" ','");
            JF_LOGGER.info("relWhereSb:{}", relWhereSb.toString());
            StringList bubbleList = (StringList) domainObject.getRelatedObjects(
                    context,
                    REL_JFECRRelateRootBubble + "," + REL_JFECRRoot2ItemBubble,
                    TYPE_VPMReference,
                    JF_Util_mxJPO.basicBolistSel(),
                    StringList.create(SELECT_ATTRIBUTE_JF_ECRID),
                    false,
                    true,
                    (short) 0,
                    "",
                    relWhereSb.toString(),
                    0
            ).stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
            JF_LOGGER.info("bubbleList:{}", bubbleList);
            connBubblePartToECRRelateItem(context, ecrId, bubbleList, partChangeSourceMap, alreadyConnIdMap);
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
        }finally {
            ContextUtil.popContext(context);
        }
    }


    /**
    * 关联ECR与冒泡数据的RelateItem关系，并汇总冒泡件的变更来源
     * 一次查询ECR完整结构后在内存中建立子件到父件的映射，避免逐个零件重复向上查询
    * @param context
	* @param ecrId
	* @param bubbleList
	* @param partChangeSourceMap
    * @author LIUJR
    * @throws
    * @return void
    * @date 2026/1/15 14:05
    * @description
    */
    private static void connBubblePartToECRRelateItem(Context context, String ecrId, StringList bubbleList, Map<String, String> partChangeSourceMap, Map<String, String> alreadyConnIdMap) throws Exception{
        try {
            JF_LOGGER.info("bubbleList:{}", bubbleList);
            JF_LOGGER.info("partChangeSourceMap:{}", partChangeSourceMap);
            JF_LOGGER.info("alreadyConnIdMap:{}", alreadyConnIdMap);
            StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            reSelectList.add(SELECT_ATTRIBUTE_JF_ECRID);
            reSelectList.add(DomainRelationship.SELECT_FROM_ID);
            reSelectList.add(DomainRelationship.SELECT_TO_ID);
            StringBuilder relWhereSb = new StringBuilder();
            relWhereSb.append(ecrId);
            relWhereSb.append("  matchlist  '");
            relWhereSb.append(SELECT_ATTRIBUTE_JF_ECRID);
            relWhereSb.append("'");
            relWhereSb.append(" ','");
            DomainObject partObject = DomainObject.newInstance(context);
            //一次拿取ECR中的全部原始变更结构和冒泡结构
            partObject.setId(ecrId);
            MapList mapList = partObject.getRelatedObjects(context, REL_JFECRRelateRoot + "," + REL_JFECRRoot2Item + "," + REL_JFECRRelateRootBubble + "," + REL_JFECRRoot2ItemBubble, // relationship pattern
                    TYPE_VPMReference,                                    // object pattern
                    typeSelectList,                            // object selects
                    reSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 0,                                    // recursion level
                    "",                // object where clause
                    relWhereSb.toString(),
                    (short) 0
            );
            if (mapList.isEmpty()) {
                JF_LOGGER.info("ECR变更结构为空，不需要计算冒泡件变更来源，ECR ID:{}", ecrId);
                return;
            }
            connBubbleRelateItem(context, mapList, ecrId, new LinkedHashSet<>(bubbleList), partChangeSourceMap, alreadyConnIdMap);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
    * 根据一次查询得到的ECR结构在内存中汇总变更来源，并统一关联冒泡件
     * 每个零件的来源仅在结果发生变化时继续传递，公共结构不会重复查询父级
    * @param context
	* @param mapList ECR完整变更结构
	* @param ecrId  ECR id
	* @param bubblePartIdSet 冒泡零件集合
	* @param partChangeSourceMap 零件的变更来源
	* @param alreadyConnIdMap 已有关联关系ID
    * @author LIUJR
    * @throws
    * @return void
    * @date 2026/1/15 14:08
    * @description
    */
    private static void connBubbleRelateItem(Context context, MapList mapList, String ecrId,
                                             Set<String> bubblePartIdSet, Map<String, String> partChangeSourceMap,
                                             Map<String, String> alreadyConnIdMap) throws Exception{
        try {
            //建立子件到父件的内存映射；相同父子关系只保留一次，避免公共结构重复传播
            Map<String, Set<String>> parentIdsByChildId = new HashMap<>();
            for (Object item : mapList) {
                Map map = (Map) item;
                String parentId = UIUtil.getValue(map, DomainRelationship.SELECT_FROM_ID);
                String childId = UIUtil.getValue(map, DomainRelationship.SELECT_TO_ID);
                if (UIUtil.isNullOrEmpty(childId)) {
                    childId = UIUtil.getValue(map, SELECT_ID);
                }
                if (UIUtil.isNullOrEmpty(parentId) || UIUtil.isNullOrEmpty(childId) || ecrId.equalsIgnoreCase(parentId)) {
                    continue;
                }
                parentIdsByChildId.computeIfAbsent(childId, key -> new LinkedHashSet<>()).add(parentId);
            }

            //冒泡件的旧来源不参与本次重新汇总，来源统一由原始变更零件向上传递得到
            Map<String, String> aggregatedSourceMap = new HashMap<>();
            Deque<String> pendingPartQueue = new ArrayDeque<>();
            Set<String> pendingPartIdSet = new HashSet<>();
            for (Map.Entry<String, String> entry : partChangeSourceMap.entrySet()) {
                String partId = entry.getKey();
                String changeSource = entry.getValue();
                if (bubblePartIdSet.contains(partId) || UIUtil.isNullOrEmpty(changeSource)) {
                    continue;
                }
                aggregatedSourceMap.put(partId, changeSource);
                if (pendingPartIdSet.add(partId)) {
                    pendingPartQueue.offer(partId);
                }
            }

            //来源发生变化时才继续向父级传播；同一零件最多由单一来源升级为Both
            while (!pendingPartQueue.isEmpty()) {
                String childId = pendingPartQueue.poll();
                pendingPartIdSet.remove(childId);
                String childSource = aggregatedSourceMap.get(childId);
                Set<String> parentIdSet = parentIdsByChildId.get(childId);
                if (UIUtil.isNullOrEmpty(childSource) || parentIdSet == null || parentIdSet.isEmpty()) {
                    continue;
                }
                for (String parentId : parentIdSet) {
                    String oldSource = aggregatedSourceMap.get(parentId);
                    String mergedSource;
                    if (UIUtil.isNullOrEmpty(oldSource)) {
                        mergedSource = childSource;
                    } else if (oldSource.equalsIgnoreCase(childSource)) {
                        mergedSource = oldSource;
                    } else {
                        mergedSource = ATTR_JFCHANGESOURCE_RANGE_BOTH;
                    }
                    if (UIUtil.isNullOrEmpty(oldSource) || !oldSource.equalsIgnoreCase(mergedSource)) {
                        aggregatedSourceMap.put(parentId, mergedSource);
                        if (pendingPartIdSet.add(parentId)) {
                            pendingPartQueue.offer(parentId);
                        }
                    }
                }
            }

            JF_LOGGER.info("冒泡结构汇总后的变更来源:{}", aggregatedSourceMap);
            DomainObject bubblePartObject = DomainObject.newInstance(context);
            for (String bubblePartId : bubblePartIdSet) {
                String changeSource = aggregatedSourceMap.get(bubblePartId);
                if (UIUtil.isNullOrEmpty(changeSource)) {
                    changeSource = ATTR_JFCHANGESOURCE_RANGE_BOTH;
                }
                String relationId = alreadyConnIdMap.get(bubblePartId);
                if (UIUtil.isNotNullAndNotEmpty(relationId)) {
                    String oldSource = partChangeSourceMap.get(bubblePartId);
                    if (UIUtil.isNullOrEmpty(oldSource) || !oldSource.equalsIgnoreCase(changeSource)) {
                        DomainRelationship.newInstance(context, relationId).setAttributeValue(context, ATTR_JFCHANGESOURCE, changeSource);
                    }
                } else {
                    bubblePartObject.setId(bubblePartId);
                    DomainRelationship domainRelationship = bubblePartObject.addFromObject(context, new RelationshipType(REL_JFRELATEITEM), ecrId);
                    domainRelationship.setAttributeValue(context, ATTR_JFCHANGESOURCE, changeSource);
                    alreadyConnIdMap.put(bubblePartId, domainRelationship.getPhysicalId(context));
                }
                partChangeSourceMap.put(bubblePartId, changeSource);
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
    * 自动冒泡  找到需要冒泡得件 先校验 在冒泡
    * @param context
	* @param partList   变更数据集
	* @param psPartList ecr关联项目的所属件集 attribute[JF_BelongPart]==Y
	* @param revBubbleMap 冒泡过的件的Map  用于多个子级拥有同一个父级需要冒泡的情况 只冒泡一次  其他的直接拿取替换父级
	* @param ecrId ECRId  用于冒泡后设置零件属性和拿取变更数据中该ECR的子级结构
	* @param bubbleList 存储冒泡件 root节点
	* @param psZeroPartList 供货件列表
	* @param bubbleAllList 冒泡件列表
	* @param processedPartIdSet 已经处理过的变更零件ID
	* @param propagatedParentIdSet 已经向上传播过的父件ID
	* @param rootParentIdSet 没有符合条件上级的父件ID
    * @author LIUJR
    * @throws
    * @return void
    * @date 2026/1/4 13:23
    * @description
    */
    public static void partAutoBubbleRecursion(Context context, StringList partList, StringList psPartList, Map<String, String> revBubbleMap,
                                               String ecrId, HashSet<String> bubbleList, HashSet<String> bubbleGXSet,
                                               StringList psZeroPartList, StringList bubbleAllList, Set<String> processedPartIdSet,
                                               Set<String> propagatedParentIdSet, Set<String> rootParentIdSet) throws Exception {
        try {
            //获取ECR的根节点
            DomainObject partObject = DomainObject.newInstance(context);
            String partId = EMPTY_STRING;
            String partPreRevisionId = EMPTY_STRING;
            StringList parentList = new StringList();
            JF_LOGGER.info("当前参与校验可能冒泡的零件List:{}", partList);
            ThreadLog.info("开始遍历整椅零件冒泡.................");
            Boolean isBubble = Boolean.TRUE;
            StringList nextLevelPartList = new StringList();
            for (int i = 0; i < partList.size(); i++) {
                //变更零件id
                partId = partList.get(i);
                ThreadLog.info("整椅零件:{}......start........", partId);
                JF_LOGGER.info("当前参与校验可能冒泡的零件:{}", partId);
                //同一个零件可能同时出现在多个整椅结构中，本次任务只需要处理一次
                if (UIUtil.isNullOrEmpty(partId) || !processedPartIdSet.add(partId)) {
                    JF_LOGGER.info("当前变更零件已经处理，跳过重复结构:{}", partId);
                    continue;
                }
                /*
                 * 冒泡当前变更零件的条件：
                 *   1.当前零件属于当前ECR项目   不属于，继续找子级变更结构
                 *   2.当前零件有上一个发布大版本   没有，继续找子级变更结构
                 *   3.上一个发布大版本属于当前项目  不属于，继续找子级变更结构
                 *   4.上一个发布大版本有符合条件的直属父级 没有直属父级，继续找子级变更结构
                 * */
                isBubble = Boolean.TRUE;
                JF_LOGGER.info("项目.contains(当前零件):{}", psPartList.contains(partId));
                if (psPartList.contains(partId)) {
                    //零件的上一个发布大版本
                    partPreRevisionId = JF_Util_mxJPO.getLastReleasedMajoridExcludeOwner(context, partId);
                    JF_LOGGER.info("零件的上一个发布大版本:{}", partPreRevisionId);
                    JF_LOGGER.info("项目.contains(零件的上一个发布大版本):{}", psPartList.contains(partPreRevisionId));
                    if (UIUtil.isNotNullAndNotEmpty(partPreRevisionId) && psPartList.contains(partPreRevisionId)) {
                        //有上一个发布大版本并且属于ECR项目
                        //有上一个发布大版本， 获取上一个发布大版本的父级
                        partObject.setId(partPreRevisionId);
                        JF_LOGGER.info("零件校验通过，找上个发布版本的父级");
                        //获取零件的满足条件的直属父件  已经冒泡过，是最新发布版本，直属于ECR项目
                        parentList = getPartPreRevAndCheckResult(context, partObject, revBubbleMap, psPartList, bubbleAllList);
                        JF_LOGGER.info("找上个发布版本零件的满足条件的直属父件: {}", parentList);
                        if (parentList.isEmpty()) {
                            isBubble = Boolean.FALSE;
                        }
                    } else {
                        isBubble = Boolean.FALSE;
                    }
                } else {
                    isBubble = Boolean.FALSE;
                }
                ThreadLog.info("是否需要冒泡 isBubble: {}", isBubble);
                //判断是否可以冒泡 当零件有上一个发布版本，并且属于当前ECR项目，上一个发布版本父级有在ECR的项目中 需要向上冒泡
                partObject.setId(partId);
                if (isBubble) {
                    JF_LOGGER.info("开始冒泡 isBubble: {}", isBubble);
                    //冒泡当前层级了  拿到父级一直往上冒泡
                    partAutoBubble(context, partObject, parentList, revBubbleMap, partPreRevisionId, ecrId, psZeroPartList, bubbleAllList);
                    //冒泡后冒泡父级  不需要找上一个版本 只需要找只属于父级  递归冒泡
                    JF_LOGGER.info("开始冒泡父级 isBubble: {}", isBubble);
                    parentAutoBubble(context, partId, parentList, psPartList, revBubbleMap, ecrId, bubbleList, bubbleGXSet,
                            psZeroPartList, bubbleAllList, propagatedParentIdSet, rootParentIdSet);
                }
                //当向上冒泡不满足条件的时候 开始找到下一层级
                //冒泡后开始拿取下一层级的数据  如果是父级往上冒泡  就需要往下冒泡
                nextLevelPartList = getNextLevelPartList(context, partObject, ecrId);
                JF_LOGGER.info("冒泡后开始拿取下一层级的数据: {}", nextLevelPartList);
                if (!nextLevelPartList.isEmpty()) {
                    //开始下一层冒泡 递归
                    partAutoBubbleRecursion(context, nextLevelPartList, psPartList, revBubbleMap, ecrId, bubbleList, bubbleGXSet,
                            psZeroPartList, bubbleAllList, processedPartIdSet, propagatedParentIdSet, rootParentIdSet);
                }
                ThreadLog.info("整椅零件:{}......end........", partId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.info(e.getMessage().toString());
            throw e;
        }
    }

    /**
    * 冒泡后冒泡父级  不需要找上一个版本 只需要找只属于父级  递归冒泡
    * @param context
	* @param prePartId
	* @param partList
	* @param psPartList
	* @param revBubbleMap
	* @param ecrId
	* @param psZeroPartList 供货件列表
	* @param bubbleAllList 冒泡件
	* @param propagatedParentIdSet 已经向上传播过的父件ID
	* @param rootParentIdSet 没有符合条件上级的父件ID
    * @author LIUJR
    * @throws
    * @return void
    * @date 2026/1/4 14:11
    * @description
    */
    private static void parentAutoBubble(Context context, String prePartId, StringList partList, StringList psPartList,
                                         Map<String, String> revBubbleMap, String ecrId, HashSet<String> bubbleList,
                                         HashSet<String> bubbleGXSet, StringList psZeroPartList, StringList bubbleAllList,
                                         Set<String> propagatedParentIdSet, Set<String> rootParentIdSet) throws Exception{
        try {
            //获取ECR的根节点
            JF_LOGGER.info("parentAutoBubble..................");
            DomainObject partObject = DomainObject.newInstance(context);
            partObject.setId(prePartId);
            String preLogicalid = partObject.getInfo(context, "logicalid");
            JF_LOGGER.info("preLogicalid：{}", preLogicalid);
            String partId = EMPTY_STRING;
            StringList parentList = new StringList();
            Boolean isBubble = Boolean.TRUE;
            String bubbleId = EMPTY_STRING;
            JF_LOGGER.info("partList:{}", partList);
            for (int i = 0; i < partList.size(); i++) {
                //变更零件id
                partId = partList.get(i);
                bubbleId = revBubbleMap.get(partId);
                if (UIUtil.isNullOrEmpty(bubbleId)) {
                    JF_LOGGER.info("父件没有对应的冒泡版本，不再向上传播，父件ID:{}", partId);
                    continue;
                }
                //父件已经完成向上传播时不再重复查询父级；根节点仍按当前下级补充结果
                if (!propagatedParentIdSet.add(partId)) {
                    if (rootParentIdSet.contains(partId)) {
                        collectBubbleRootPart(context, prePartId, preLogicalid, partId, revBubbleMap,
                                bubbleList, bubbleGXSet, psZeroPartList);
                    }
                    JF_LOGGER.info("父件已经完成向上传播，跳过重复处理，父件ID:{}", partId);
                    continue;
                }
                /*
                 * 冒泡当前变更零件的条件：
                 *   1.当前零件属于当前ECR项目   不属于 终止
                 *   4.当前零件有符合条件的直属父级  无 终止
                 * */
                isBubble = Boolean.TRUE;
                JF_LOGGER.info("psPartList.contains(partId):{}", psPartList.contains(partId));
                if (psPartList.contains(partId)) {
                    partObject.setId(partId);
                    //获取零件的满足条件的直属父件  已经冒泡过，是最新发布版本，直属于ECR项目
                    JF_LOGGER.info("获取零件的满足条件的直属父件  已经冒泡过，是最新发布版本，直属于ECR项目");
                    parentList = getPartPreRevAndCheckResult(context, partObject, revBubbleMap, psPartList, bubbleAllList);
                    JF_LOGGER.info("需要冒泡或者替换的父级: {}", parentList);
                    if (parentList.isEmpty()) {
                        //root 节点拿取   如果当前节点不是GX 下级是供货件 当没有父件的时候不能把GX 放进去
                        JF_LOGGER.info("root 节点拿取   如果当前节点不是GX 下级是供货件 当没有父件的时候不能把GX 放进去: {}");
                        rootParentIdSet.add(partId);
                        collectBubbleRootPart(context, prePartId, preLogicalid, partId, revBubbleMap,
                                bubbleList, bubbleGXSet, psZeroPartList);
                        isBubble = Boolean.FALSE;
                    }
                } else {
                    isBubble = Boolean.FALSE;
                }
                JF_LOGGER.info("是否父级冒泡 isBubble:{}", isBubble);
                //判断是否可以冒泡 当零件有上一个发布版本，并且属于当前ECR项目，上一个发布版本父级有在ECR的项目中 需要向上冒泡
                partObject.setId(partId);
                if (!isBubble) {
                    continue;
                }
                JF_LOGGER.info("父级冒泡 revBubbleMap.get(partId):{}", revBubbleMap.get(partId));
                if (UIUtil.isNotNullAndNotEmpty(bubbleId)) {
                    partObject.setId(bubbleId);
                    //冒泡当前层级了  拿到父级一直往上冒泡
                    JF_LOGGER.info("冒泡当前层级了  拿到父级一直往上冒泡............");
                    partAutoBubble(context, partObject, parentList, revBubbleMap, partId, ecrId, psZeroPartList, bubbleAllList);
                }
                JF_LOGGER.info("拿到父级一直往上冒泡找父级............");
                //冒泡后冒泡父级  不需要找上一个版本 只需要找只属于父级
                parentAutoBubble(context, partId, parentList, psPartList, revBubbleMap, ecrId, bubbleList, bubbleGXSet,
                        psZeroPartList, bubbleAllList, propagatedParentIdSet, rootParentIdSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.info(e.getMessage().toString());
            throw e;
        }
    }

    /**
    * 查找当前零件在当前ECR中的下一级变更零件
    * @param context
	* @param partObject 变更零件
	* @param ecrId ECRid
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2025/12/30 9:12
    * @description
    */
    private static StringList getNextLevelPartList(Context context, DomainObject partObject, String ecrId) throws Exception{
        StringList rootItemList = new StringList();
        try {
            StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
            StringList typeSelectList = new StringList(SELECT_ID);
            typeSelectList.add(SELECT_REVISION);
            typeSelectList.add(SELECT_CURRENT);
            typeSelectList.add(SELECT_ATTR_JFConnectECR);
            reSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
            reSelectList.add(SELECT_ATTRIBUTE_JF_ECRID);
            reSelectList.add(SELECT_ATTRIBUTE_JF_ChangeBeforeRev);
            reSelectList.add(SELECT_ATTRIBUTE_JF_BOMChangeDes);
            reSelectList.add(SELECT_ATTRIBUTE_JF_BOMQuantity);
            //获取当前零件结构下的变更数据
            MapList maps = partObject.getRelatedObjects(
                    context,
                    REL_JFECRRoot2Item, // relationship pattern
                    TYPE_VPMReference,                                    // object pattern
                    typeSelectList,                            // object selects
                    reSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            rootItemList = (StringList) maps.stream().filter(m -> {
                Map map = (Map) m;
                String value = UIUtil.getValue(map, SELECT_ATTRIBUTE_JF_ECRID);
                //    1. ECR中变更零件为冻结并且版本不为AA版本的数据--
                //    也就是零件属性JF_VPMReference.JF_ConnectECR保存的ID 和当前ECR的ID一致----意味着当前零件是通过该ECR发布的变更数据可以冒泡
                String connectECR = UIUtil.getValue(map, SELECT_ATTR_JFConnectECR);
                if (value.contains(ecrId) && connectECR.equalsIgnoreCase(ecrId)) {
                    return true;
                } else {
                    return false;
                }
            }).map(m1-> {
                Map map1 = (Map) m1;
                return UIUtil.getValue(map1, SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
        }catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.info(e.getMessage().toString());
            throw e;
        }
        return rootItemList;
    }

    /**
    * 获取零件的满足条件的直属父件
     * 1. 判断父是否在升版冒泡Map中   是，后续冒泡直接拿取 不移除
     * 2. 父不在升版冒泡Map中， 判断当前父是否是最新发布版本  否，移除
     * 3. 判断是否是ECR项目中的所属件 不是 不需要冒泡移除
    * @param context
	* @param partObject
	* @param revBubbleMap
	* @param psPartList
	* @param bubbleAllList
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2026/1/4 13:40
    * @description
    */
    private static StringList getPartPreRevAndCheckResult(Context context, DomainObject partObject, Map<String, String> revBubbleMap, StringList psPartList, StringList bubbleAllList) throws Exception{
        StringList parentList = new StringList();
        String parentPartId = EMPTY_STRING;
        String lastReleasedMajorid = EMPTY_STRING;
        JF_LOGGER.info("获取零件的满足条件的直属父件............");
        try {
            //找到上一个发布大版本的发布所有父级
            parentList = partObject.getInfoList(context, "to[" + REL_Instance + "].from.id");
            JF_LOGGER.info("上一个发布大版本的发布所有父级:{}", parentList);
            if (parentList.isEmpty()) {
                return parentList;
            }
            /*找到所有发布的父级后进行判断  筛选出需要冒泡的父级
             * 判断父是否在升版冒泡Map中
             *    1. 是，后续冒泡直接拿取
             *    2. 否，判断当前父是否是最新发布版本
             */
            JF_LOGGER.info("找到所有发布的父级后进行判断....筛选出需要冒泡的父级........");
            JF_LOGGER.info("revBubbleMap:{}", revBubbleMap);
            DomainObject domainObject = DomainObject.newInstance(context);
            String current = EMPTY_STRING;
            StringList newParentList = new StringList();
            for (int i1 = 0; i1 < parentList.size(); i1++) {
                parentPartId = parentList.get(i1);
                domainObject.setId(parentPartId);
                current = domainObject.getInfo(context, SELECT_CURRENT);
                JF_LOGGER.info("current:{}", current);
                if (!"RELEASED".equalsIgnoreCase(current)) {
                    continue;
                }
                JF_LOGGER.info("是否已经冒泡:{}", revBubbleMap.containsKey(parentPartId));
                if (revBubbleMap.containsKey(parentPartId)) {
                    //上一个发布版本的父在升版冒泡Map中  后续冒泡直接拿取
                    newParentList.add(parentPartId);
                    continue;
                }//已经是冒泡件了 不允许再次冒泡
                if (bubbleAllList.contains(parentPartId)) {
                    continue;
                }
                // 上一个发布版本的父不在升版冒泡Map中否， 判断当前父是否是最新发布版本
//                lastReleasedMajorid = JF_Util_mxJPO.getLastReleasedMajorid(context, parentPartId);
                lastReleasedMajorid = JF_Util_mxJPO.getLastReleasedMajorid_new(context, parentPartId);
                JF_LOGGER.info("当前父的最新发布版本:{}", lastReleasedMajorid);
                if (!parentPartId.equalsIgnoreCase(lastReleasedMajorid)) {
                    //不是最新版本 不需要冒泡 移除
                    JF_LOGGER.info("不是最新版本 不需要冒泡 移除");
                    continue;
                }
                JF_LOGGER.info("判断是否是ECR项目中的所属件：{}", psPartList.contains(parentPartId));
                if (!psPartList.contains(parentPartId)) {
                    //判断是否是ECR项目中的所属件 不是 不需要冒泡移除
                    continue;
                }
                newParentList.add(parentPartId);
            }
            parentList = newParentList;
        }catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.info(e.getMessage().toString());
            throw e;
        }
        return parentList;
    }

    /**
    * 开始冒泡
     * 1. 判断上一个发布版本的父是否在升版冒泡Map中
     *        1. 是，使用setTo API替换旧版本，把当前变更数据替换上去
     *        2. 否，判断当前父是否是最新发布版本
     * 2. 是最新发布版本
     *   1. 调用升版代码进行升版，升版完成之后，使用setTo API替换旧版本，把当前变更数据替换上去，
     *          升版对象JF_VPMReference.JF_ConnectECR 设置为当前ECRID，状态直接设置到发布，版本根据规则生成流水码小版本，
     *          并将父级存储到升版冒泡Map中去， JF_VPMReferenceCost.JF_IsBubbling 为Y--标识下是冒泡出来的数据
     *   2. 升版注意： 升版需要切换到admin账号进行切换 role ctx::VPLMProjectLeader.RD Center.JFSeat
     *          (不然权限不够，升版完成之后进行 组织、协作区、人员的切换,包括3Dshape、XCADAssemblyRepReference 对象
     *          包括XCADAssemblyRepInstance、VPMRepInstance 关系的owner、 组织)，Instance关系的组织协作区是否需要转移，暂时先不管
     *   3. 父继续往上找父的父，如果符合以上规则，继续循环判断是否是项目所属件是否在Map中是否是最新发布版本，调用升版代码进行升版
    * @param context
	* @param partObject  当前变更零件
	* @param parentList   上一个发布大版本符合冒泡条件的直属父
	* @param revBubbleMap  冒泡集合
	* @param partPreRevisionId 当前变更零件的上一个发布大版本
	* @param ecrId  ECRId
	* @param psZeroPartList  供货件
	* @param bubbleAllList  冒泡件
    * @author LIUJR
    * @throws
    * @return void
    * @date 2026/1/4 13:54
    * @description
    */
    public static void partAutoBubble(Context context, DomainObject partObject, StringList parentList,  Map<String, String> revBubbleMap, String partPreRevisionId, String ecrId, StringList psZeroPartList, StringList bubbleAllList) throws Exception{
        StringList stringList = new StringList();
        try {
            String parentPartId = EMPTY_STRING;
            String parentBubbleId = EMPTY_STRING;
            String partType = EMPTY_STRING;
            String partBubbleType =  EMPTY_STRING;
            String quantity = EMPTY_STRING;
            String changeQuantity = EMPTY_STRING;
            String connId = EMPTY_STRING;
            DomainObject bubbleObject = DomainObject.newInstance(context);
            DomainObject domainObject = DomainObject.newInstance(context);
            MapList mapList = new MapList();
            MapList mapList1 = new MapList();
            JF_LOGGER.info("partAutoBubble...............");
            JF_LOGGER.info("parentList:{}", parentList);
            for (int i = 0; i < parentList.size(); i++) {
                parentPartId = parentList.get(i);
                domainObject.setId(parentPartId);
                JF_LOGGER.info("revBubbleMap.containsKey(parentPartId):{}", revBubbleMap.containsKey(parentPartId));
                if (revBubbleMap.containsKey(parentPartId)) {
                    //如果已经冒泡过 直接拿取 不需要升版了
                    parentBubbleId = revBubbleMap.get(parentPartId);
                } else {
                    //没有冒泡  需要将父级进行升版 并且修改属性、Owner、组织，协作区 包括下级实例或者数模
                    JF_LOGGER.info("冒泡 - 需要将父级进行升版 并且修改属性、Owner、组织，协作区 包括下级实例或者数模");
                    JF_LOGGER.info("parentPartId：{}", parentPartId);
                    parentBubbleId = revisePartBubbleRevision(context, parentPartId, ecrId);
                    //判断是否冒泡升版成功
                    JF_LOGGER.info("parentBubbleId：{}", parentBubbleId);
                    if (UIUtil.isNotNullAndNotEmpty(parentBubbleId)) {
                        //冒泡成功后将父级存储到升版冒泡Map中去
                        revBubbleMap.put(parentPartId, parentBubbleId);
                        bubbleAllList.add(parentBubbleId);
                    }
                }
                JF_LOGGER.info("冒泡后id:{}", parentBubbleId);
                JF_LOGGER.info("revBubbleMap:{}", revBubbleMap);
                partType = partObject.getAttributeValue(context, ATTR_JFPartType);
                if (UIUtil.isNotNullAndNotEmpty(parentBubbleId)) {
                    bubbleObject.setId(parentBubbleId);
                    partBubbleType = bubbleObject.getAttributeValue(context, ATTR_JFPartType);
                    stringList.add(parentBubbleId);
                    //拿取升版后的零件的下一层级的该件的上一版本的partPreRevisionId 的关系 替换为当前的这个件
                    JF_LOGGER.info("拿取升版后的零件的下一层级的该件的上一版本的partPreRevisionId..替换为当前的这个件...........");
                    mapList = bubbleObject.getRelatedObjects(
                            context,
                            REL_Instance,
                            TYPE_VPMReference,
                            JF_Util_mxJPO.basicBolistSel(),
                            JF_Util_mxJPO.basicRellistSel(),
                            false,
                            true,
                            (short) 1,
                            "id==" + partPreRevisionId,  //id等于上一版本id
                            "",
                            0
                    );
                    JF_LOGGER.info("mapList:{}", mapList);
                    mapList1 = domainObject.getRelatedObjects(
                            context,
                            REL_Instance,
                            TYPE_VPMReference,
                            JF_Util_mxJPO.basicBolistSel(),
                            JF_Util_mxJPO.basicRellistSel(),
                            false,
                            true,
                            (short) 1,
                            "id==" + partPreRevisionId,  //id等于上一版本id
                            "",
                            0
                    );
                    JF_LOGGER.info("mapList1:{}", mapList1);
                    //是，使用setTo API替换旧版本，把当前变更数据替换上去
                    quantity = String.valueOf(mapList.size());
                    changeQuantity = String.valueOf(mapList.size() - mapList1.size());
                    Map map = new HashMap();
                    for (int i1 = 0; i1 < mapList.size(); i1++) {
                        map = (Map) mapList.get(i1);
                        connId = UIUtil.getValue(map, DomainRelationship.SELECT_ID);
                        JF_LOGGER.info("使用setTo API替换旧版本，把当前变更数据替换上去:{}", connId);
                        DomainRelationship.setToObject(context, connId, partObject);
                        //如果当前变更的父级是供货件 并且是GC
                        //都是供货件  并且当前节点类型是GC, 关联的上一节点是GX,不搭建变更结构
                        JF_LOGGER.info("partBubbleType.equalsIgnoreCase(\"X\"):{}", partBubbleType.equalsIgnoreCase("X"));
                        JF_LOGGER.info("psZeroPartList.contains(partObject.getInfo(context, \"logicalid\"))", psZeroPartList.contains(partObject.getInfo(context, "logicalid")));
                        if (psZeroPartList.contains(partObject.getInfo(context, "logicalid")) && partBubbleType.equalsIgnoreCase("X")) {
                            //当存在GC GX都是供货件的时候 变更结构只能到GC
                            continue;
                        }
                        buildECRChangeStructure(context, parentBubbleId, partObject.getId(context), ecrId, partPreRevisionId, REL_JFECRRoot2ItemBubble, quantity, changeQuantity, String.valueOf(mapList1.size()));
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.info(e.getMessage().toString());
            throw e;
        }
    }

    /**
    *    1. 调用升版代码进行升版，升版完成之后，使用setTo API替换旧版本，把当前变更数据替换上去，
     *          升版对象JF_VPMReference.JF_ConnectECR 设置为当前ECRID，状态直接设置到发布，版本根据规则生成流水码小版本，
     *          并将父级存储到升版冒泡Map中去， JF_VPMReferenceCost.JF_IsBubbling 为Y--标识下是冒泡出来的数据
     *   2. 升版注意： 升版需要切换到admin账号进行切换 role ctx::VPLMProjectLeader.RD Center.JFSeat
     *          (不然权限不够，升版完成之后进行 组织、协作区、人员的切换,包括3Dshape、XCADAssemblyRepReference 对象
     *          包括XCADAssemblyRepInstance、VPMRepInstance 关系的owner、 组织)，Instance关系的组织协作区是否需要转移，暂时先不管
    * @param context
	* @param parentPartId    需要升版冒泡的父级
	* @param ecrId  ecrId
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2026/1/4 10:09
    * @description
    */
    private static String revisePartBubbleRevision(Context context, String parentPartId, String ecrId) throws Exception{
        String bubbleId = EMPTY_STRING;
        try {
            DomainObject domainObject = DomainObject.newInstance(context, parentPartId);
            domainObject.setId(parentPartId);
            String physicalid = domainObject.getInfo(context, "physicalid");
            String logicalid = domainObject.getInfo(context, "logicalid");
            String owner = domainObject.getInfo(context, SELECT_OWNER);
            String organization = domainObject.getInfo(context, DomainConstants.SELECT_ORGANIZATION);
            String project = domainObject.getInfo(context, DomainConstants.SELECT_PROJECT);
            JF_LOGGER.info("owner:{}", owner);
            JF_LOGGER.info("organization:{}", organization);
            JF_LOGGER.info("project:{}", project);
            JF_LOGGER.info("context:{}", context.getUser().toString());
            //切换context  为admin
            String majoredRevision = JF_Util_mxJPO.majorRevision(context, physicalid);
            JF_LOGGER.info("context:{}", context.getUser().toString());
            JF_LOGGER.info("majoredRevision:{}", majoredRevision);
            JSONObject jsonObject = JSONObject.parseObject(majoredRevision);
            JSONArray results = jsonObject.getJSONArray("results");
            JF_LOGGER.info("results:{}", results);
            for (int i = 0; i < results.size(); i++) {
                JSONObject jsonObject1 = results.getJSONObject(i);
                if (logicalid.equalsIgnoreCase(jsonObject1.getString("logicalid"))) {
                    bubbleId = jsonObject1.getString("physicalid");
                    break;
                }
            }
            JF_LOGGER.info("bubbleId:{}", bubbleId);
            if (UIUtil.isNotNullAndNotEmpty(bubbleId)) {
                domainObject.setId(bubbleId);
                bubbleId = domainObject.getInfo(context, SELECT_ID);
                JF_LOGGER.info("bubble   oooo Id:{}", bubbleId);
                Policy policy = domainObject.getPolicy(context);
                StateRequirementList stateRequirements = policy.getStateRequirements(context);
                //升版对象JF_VPMReference.JF_ConnectECR 设置为当前ECRID，状态直接设置到发布，版本根据规则生成流水码小版本，
                //并将父级存储到升版冒泡Map中去， JF_VPMReferenceCost.JF_IsBubbling 为Y--标识下是冒泡出来的数据 设置为发布
                domainObject.setAttributeValue(context, ATTR_JFConnectECR, ecrId);
                domainObject.setAttributeValue(context, Attr_JF_IsBubbling, "Y");
                MqlUtil.mqlCommand(context,false,false, mql,true, bubbleId, owner, organization, project, "RELEASED");
                //升版完成之后进行 组织、协作区、人员的切换,包括3Dshape、XCADAssemblyRepReference 对象
                //包括XCADAssemblyRepInstance from-、VPMRepInstance 关系的owner、 组织)，Instance关系的组织协作区是否需要转移，暂时先不管
                MapList mapList = domainObject.getRelatedObjects(context,
                        REL_VPMRepInstance + "," + REL_XCADAssemblyRepInstance,
                        QUERY_WILDCARD,
                        JF_Util_mxJPO.basicBolistSel(),
                        JF_Util_mxJPO.basicRellistSel(),
                        false,
                        true,
                        (short) 1,
                        "",
                        "",
                        0);
                JF_LOGGER.info("mapList:{}", mapList);
                for (int i = 0; i < mapList.size(); i++) {
                    Map map = (Map) mapList.get(i);
                    String oId = UIUtil.getValue(map, SELECT_ID);
                    String connId = UIUtil.getValue(map, DomainRelationship.SELECT_ID);
                    MqlUtil.mqlCommand(context,false,false, mql1,true, oId, owner, organization, project);
//                    MqlUtil.mqlCommand(context,false,false, mql,true, oId, owner, organization, project, "RELEASED");
                    JF_LOGGER.info("oId:{},connId:{} ", oId, connId);
                    MqlUtil.mqlCommand(context,false,false, mql2,true, connId, owner, organization, project);
                }
                JF_LOGGER.info("project:{}", project);
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return bubbleId;
    }

    /**
     * 获取零件的上一个发布大版本
     * @param context
     * @param partObject
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/12/29 16:51
     * @description
     */
    public static String getPartPreRevisionId(Context context, DomainObject partObject) throws Exception {
        String lastReleasedId = "";
        try {
            BusinessObjectList majorRevisionsBusObjList = partObject.getMajorRevisions(context);
            DomainObject doObj;
            String sCurrent;
            String sRev;
            if (majorRevisionsBusObjList.size() == 1) {
                lastReleasedId = EMPTY_STRING;
            } else {
                //
                String revision = partObject.getInfo(context, SELECT_REVISION);
                String[] split = revision.split("\\.");
                char ch = split[0].charAt(0);  //拿出大版本
                String major = String.valueOf((char)(ch - 1));  //大版本 上一个
                List<Map> mapList = new ArrayList<>();
                Map basicMap = new HashMap();
                StringList basicBolistSel = JF_Util_mxJPO.basicBolistSel();
                for (int i = 0; i < majorRevisionsBusObjList.size(); i++) {
                    doObj = (DomainObject) majorRevisionsBusObjList.get(i);
                    basicMap = doObj.getInfo(context, basicBolistSel);
                    sCurrent = UIUtil.getValue(basicMap, DomainObject.SELECT_CURRENT);
                    if (!"RELEASED".equals(sCurrent)) {
                        continue;
                    }
                    sRev = UIUtil.getValue(basicMap, DomainObject.SELECT_REVISION);
                    if (sRev.startsWith(major)) {
                        mapList.add(basicMap);
                    }
                }
                //开始比较出上一个版本的最新发布大版本
                if (!mapList.isEmpty()) {
                    lastReleasedId = getPartListSameRevMaxReleaseRevision(context, mapList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.info(e.getMessage().toString());
            throw e;
        }
        return lastReleasedId;
    }

    /**
     * 传入的零件件多个版本 返回其中最大的版本
     * @param context
     * @param mapList 零件的多个版本
     * @author LIUJR
     * @throws
     * @return java.util.Map
     * @date 2025/12/30 13:59
     * @description
     */
    public static String getPartListSameRevMaxReleaseRevision(Context context, List<Map> mapList) throws Exception {
        String partId = EMPTY_STRING;
        try {
            // 新正则：匹配 AA.2-001 这类格式
            Pattern revisionPattern = Pattern.compile("^([A-Z]+)\\.(\\d+)-(\\d{3})$");
            Map maxRevisionMap = mapList.stream()
                    .filter(map -> map != null && map.containsKey("revision"))
                    .filter(map -> {
                        String rev = (String) ((Map)map).get("revision");
                        return rev != null && revisionPattern.matcher(rev).matches();
                    })
                    .max((map1, map2) -> {
                        String rev1 = UIUtil.getValue(map1, "revision");
                        String rev2 = UIUtil.getValue(map2, "revision");

                        // 使用正则提取三部分（也可以用 split，但要考虑 - 的位置）
                        Matcher m1 = revisionPattern.matcher(rev1);
                        Matcher m2 = revisionPattern.matcher(rev2);
                        if (!m1.matches() || !m2.matches()) {
                            return 0; // 不应发生，因已过滤
                        }

                        String prefix1 = m1.group(1);
                        String prefix2 = m2.group(1);
                        int major1 = Integer.parseInt(m1.group(2));
                        int major2 = Integer.parseInt(m2.group(2));
                        int minor1 = Integer.parseInt(m1.group(3));
                        int minor2 = Integer.parseInt(m2.group(3));

                        int prefixComp = prefix1.compareTo(prefix2);
                        if (prefixComp != 0) return prefixComp;

                        int majorComp = Integer.compare(major1, major2);
                        if (majorComp != 0) return majorComp;

                        return Integer.compare(minor1, minor2);
                    })
                    .orElse(null);
            if (!maxRevisionMap.isEmpty()) {
                partId = UIUtil.getValue(maxRevisionMap, SELECT_ID);
            }
        }catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.info(e.getMessage().toString());
            throw e;
        }
        return partId;
    }

    /**
    * 搭建ECR变更结构
    * @param context
	* @param parentId  冒泡后父级id
	* @param childId  替换的子级id
	* @param ecrId ecrid
	* @param beforePartId 冒泡前父级id
	* @param quantity 数量
	* @param changeQuantity 变更数量
	* @param beforeQuantity 变更前数量
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2026/1/4 15:02
    * @description
    */
    public static void buildECRChangeStructure(Context context, String parentId, String childId, String ecrId, String beforePartId, String relType, String quantity, String changeQuantity, String beforeQuantity) throws Exception {
        try {
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(parentId);
            StringList basicRellistSel = JF_Util_mxJPO.basicRellistSel();
            StringList basicBolistSel = JF_Util_mxJPO.basicBolistSel();
            basicBolistSel.add(SELECT_REVISION);
            basicBolistSel.add(SELECT_CURRENT);
            basicBolistSel.add(SELECT_ATTR_JFConnectECR);
            basicRellistSel.add(SELECT_ATTRIBUTE_JF_ECRID);
            basicRellistSel.add(SELECT_ATTRIBUTE_JF_ChangeBeforeRev);
            basicRellistSel.add(SELECT_ATTRIBUTE_JF_BOMChangeDes);
            basicRellistSel.add(SELECT_ATTRIBUTE_JF_BOMQuantity);
            basicRellistSel.add(SELECT_ATTRIBUTE_JF_BOMChangeQuantity);
            //判断当前父级是否已经关联了子级
            MapList mapList = domainObject.getRelatedObjects(
                    context,
                    relType,
                    TYPE_VPMReference,
                    basicBolistSel,
                    basicRellistSel,
                    false,
                    true,
                    (short) 1,
                    "id==" + childId,  //id等于上一版本id
                    "",
                    0
            );
            if (mapList.isEmpty()) {
                //没有关联 就关联上
                String ReviseRelease = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.ReviseRelease", new String[]{});
                DomainRelationship domainRelationship = domainObject.addToObject(context, new RelationshipType(relType), childId);
                domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_ECRID, ecrId);
                domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_BOMQuantity, quantity);
                domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_BOMChangeDes, ReviseRelease);
                domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_ChangeBeforeRev, beforePartId);
                domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_BOMChangeQuantity, changeQuantity);
                domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_BOMBeforeQuantity, beforeQuantity);
                //当前的所有下级需要关联结构
            } else {
                Map map = (Map) mapList.get(0);
                String ecrID = UIUtil.getValue(map, SELECT_ATTRIBUTE_JF_ECRID);
                String connId = UIUtil.getValue(map, DomainRelationship.SELECT_ID);
                DomainRelationship domainRelationship = DomainRelationship.newInstance(context, connId);
                if (!ecrID.contains(ecrId)) {
                    domainRelationship.setAttributeValue(context, ATTRIBUTE_JF_ECRID, ecrID + "," + ecrId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.info(e.getMessage().toString());
            throw e;
        }
    }

    /**
    *冒泡后发送邮件给项目经理可以开始审批了
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2026/1/21 14:15
    * @description
    */
    public static void sendEmailToProjectManagerForECR(Context context, String[] args) throws Exception{
        try {
            String ecrProjectId = args[0];
            String ecrName = args[1];
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(ecrProjectId);
            String psDesc = domainObject.getInfo(context, SELECT_NAME) + "_" + domainObject.getDescription(context);
            String personId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, ecrProjectId, SELECT_ID, "", "attribute[Project Role]=='Project manager'");
            if(UIUtil.isNotNullAndNotEmpty(personId)) {
                domainObject.setId(personId);
                String emailAddress = domainObject.getAttributeValue(context, ATTRIBUTE_EMAIL_ADDRESS);
                //拿取邮件文档
                // 创建多部分消息体
                MimeMultipart multipart = new MimeMultipart(); // 默认混合模式
                //邮件内容的html模板部分
                BodyPart msgBodyPart = new MimeBodyPart();
                String mess = ComponentsUIUtil.getI18NString(context, "zh_CN", "emxComponents.ProjectManager.EmailMess", new String[]{});
                String mess1 = ComponentsUIUtil.getI18NString(context, "en", "emxComponents.ProjectManager.EmailMess", new String[]{});
                mess = String.format(mess, ecrName, psDesc);
                mess1 = String.format(mess1, ecrName, psDesc);
                msgBodyPart.setText(mess + "\n" + mess1);
                multipart.addBodyPart(msgBodyPart);
                Boolean aBoolean = JF_SendEmailUtils_mxJPO.SendEmail(context, emailAddress, "zh".equalsIgnoreCase("zh") ? "ECR冒泡完成通知 " : "ECR  Bubble Completion notification", multipart);
            }
        }catch (Exception e) {
            e.printStackTrace();
//            throw e;
        }
    }


    /**
     * @return void
     * @Author Liuxg
     * @Description
     * @Date 2026/1/13 8:50
     * @Param [context, args]
     **/
    public void getReviewECRReject(Context context, String[] args) throws Exception {
        Map pramap = JPO.unpackArgs(args);
        StringList oldidlist = (StringList) pramap.get("oldidlist");
        String ECRId = (String) pramap.get("ECRId");
        getReviewECRReject(context, oldidlist,ECRId);
    }


    /**
     * @return void
     * @Author Liuxg
     * @Description 根据冒泡升版后的ECR的旧数据，查询出来关联的其他审核中的ECR并驳回
     * @Date 2026/1/10 12:05
     * @Param [context, oldidlist]
     **/
    public void getReviewECRReject(Context context, StringList oldidlist,String ecrId) throws Exception {
        try {
            JF_LOGGER.info("getReviewECRReject start");
            JF_LOGGER.info("oldidlist:{}", oldidlist);
            JF_LOGGER.info("ecrId:{}", ecrId);
            Set<String> rejectEcrSet = new HashSet<String>();
            //遍历旧数据集合,查询出来的ECR需要去重
            DomainObject ecrObject = DomainObject.newInstance(context, ecrId);
            String ecrName = ecrObject.getInfo(context, DomainConstants.SELECT_NAME);
            DomainObject oldobj = DomainObject.newInstance(context);
            DomainObject object = DomainObject.newInstance(context);
            StringList ecrIdlist = new StringList();
            String ecrCurrent = DomainConstants.SELECT_CURRENT;
            //过滤出正在审核中的ECR
            for (String oldid : oldidlist) {
                oldobj.setId(oldid);
                ecrIdlist = oldobj.getInfoList(context, "to[JFRelateItem].from.id");
                for (String ecrid : ecrIdlist) {
                    object.setId(ecrid);
                    ecrCurrent = object.getCurrentState(context).getName();
                    if ("Review".equals(ecrCurrent)) {
                        rejectEcrSet.add(ecrid);
                    }
                }
            }
            StringList bul = JF_Util_mxJPO.basicBolistSel();
            bul.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            Map partmap = new HashMap();
            String LatestMajorid = DomainConstants.EMPTY_STRING;
            String LatestRev = DomainConstants.EMPTY_STRING;
            //旧数据详细信息  update by ljr  不需要单独查询 需要的时候 一起查询
//            Map oldpartMap = new HashMap();
//            for (String oldPartId : oldidlist) {
//                oldobj.setId(oldPartId);
//                partmap = oldobj.getInfo(context, bul);
//                LatestMajorid = JF_Util_mxJPO.getLatestMajorid_new(context, new String[]{oldPartId});
//                JF_LOGGER.info("LatestMajorid--->" + LatestMajorid);
//                object.setId(LatestMajorid);
//                 LatestRev = object.getRevision(context);
//                JF_LOGGER.info("LatestRev--->" + LatestRev);
//                partmap.put("LatestRev", LatestRev);
//                partmap.put("LatestECRName", ecrName);
//                oldpartMap.put(oldPartId, partmap);
//            }
            //需要处理的ECR和相关的零件信息
            StringList partIdList = new StringList();
            StringList parentIdList = new StringList();
            String partNumber = DomainConstants.EMPTY_STRING;
            String rev = DomainConstants.EMPTY_STRING;
            HashSet<String> intersection = new HashSet<>();
            //撤回信息
            StringBuffer msgPartsb = new StringBuffer();
            for (String ecrid : rejectEcrSet) {
                //查询每个ECR相关的已经升版过的零件号
                ecrObject.setId(ecrid);
                MapList ecrErrorPartlist = new MapList();
                partIdList = ecrObject.getInfoList(context, "from[JFRelateItem].to.id");
                for (String partId : partIdList) {
                    //Ecr关联的数据被包含在旧版本数据集合中
                    if (oldidlist.contains(partId)) {
                        //获取该零件的最新版本 及其基本信息
                        oldobj.setId(partId);
                        partmap = oldobj.getInfo(context, bul);
                        partNumber = (String) partmap.get("attribute[EnterpriseExtension.V_PartNumber]");
                        rev = (String) partmap.get(DomainConstants.SELECT_REVISION);
                        msgPartsb.append(partNumber).append("_").append(rev).append(",");
                        LatestMajorid = JF_Util_mxJPO.getLatestMajorid_new(context, new String[]{partId});
                        JF_LOGGER.info("LatestMajorid--->" + LatestMajorid);
                        object.setId(LatestMajorid);
                        LatestRev = object.getRevision(context);
                        JF_LOGGER.info("LatestRev--->" + LatestRev);
                        partmap.put("LatestRev", LatestRev);
                        partmap.put("LatestECRName", ecrName);
                        //查询出当前零件的父级
                        parentIdList = oldobj.getInfoList(context, "to[" + JF_PLMConstants_mxJPO.REL_Instance + "].from.id");
                        //拿出交集该零件在当前ECR中的父级
                        intersection = parentIdList.stream()
                                .filter(partIdList::contains)
                                .collect(Collectors.toCollection(HashSet::new));
                        parentIdList = StringList.create(intersection);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (String parentId : parentIdList) {
                            oldobj.setId(parentId);
                            stringBuilder.append(oldobj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER))
                                    .append("_").append(oldobj.getInfo(context, DomainConstants.SELECT_REVISION)).append("\n");
                        }
                        partmap.put("AffectedParentPart", stringBuilder.toString());
                        ecrErrorPartlist.add(partmap);
                    }
                }
//                errorEcrMap.put(ecrid,ecrErrorPartlist);
                //驳回审核中的ECR
                //设置ECR的属性JFAllowableReview为N，
                // 设置 xxx(所有的零件企业编码_版本号)零件不是最新发布版本，请撤回ECR、替换零件、重新发起ECR审核，保存到JFReviewMessage属性中
                //发送邮件通知ECRowner
//                for (int j = 0; j < ecrErrorPartlist.size(); j++) {
//                    partmap = (Map) ecrErrorPartlist.get(j);
//                    partNumber = (String) partmap.get("attribute[EnterpriseExtension.V_PartNumber]");
//                    rev = (String) partmap.get(DomainConstants.SELECT_REVISION);
//                    LatestRev = (String) partmap.get("LatestRev");
//                    msgPartsb.append(partNumber).append("_").append(rev).append(",");
//                }
                String msgPartStr = msgPartsb.toString();
                String errormsg = (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.ErrorECR.ErrorMsg", context.getLocale());
                rejectECR(context, ecrid);
                ecrObject.setAttributeValue(context, "JFAllowableReview", "N");
                ecrObject.setAttributeValue(context, "JFReviewMessage", msgPartStr + errormsg);
                //发送邮件通知ECROwner
                sendEmailForErrorEcr(context, ecrid, ecrErrorPartlist);
            }
            JF_LOGGER.info("getReviewECRReject end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendEmailForErrorEcr(Context context, String ecrid, MapList ecrErrorPartlist) throws Exception {

        //拿取邮件文档
        // 创建多部分消息体
        MimeMultipart multipart = new MimeMultipart(); // 默认混合模式
        //邮件内容的html模板部分
        BodyPart msgBodyPart = new MimeBodyPart();
        //邮件内容的html模板部分
        String html = JF_PublicMethodClass_mxJPO.getPageHTMLResourceFile(context, "ECRErrorPartEmails", "zh");
        org.jsoup.nodes.Document doc = Jsoup.parse(html);

        DomainObject EcrObj = DomainObject.newInstance(context, ecrid);
        String ecrOwner = EcrObj.getOwner(context).getName();
        String ecrName = EcrObj.getInfo(context, DomainConstants.SELECT_NAME);
        JF_LOGGER.info("ecrOwner--->" + ecrOwner);
        DomainObject personObject = PersonUtil.getPersonObject(context, ecrOwner);
        String sendEmails = personObject.getAttributeValue(context, DomainObject.ATTRIBUTE_EMAIL_ADDRESS);
//        String sendEmails = "liuxg@tecwin.com";

        StringList listAttr = new StringList();
        listAttr.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        listAttr.add(SELECT_REVISION);
        listAttr.add("LatestRev");
        listAttr.add("LatestECRName");
        listAttr.add("AffectedParentPart");
        doc.getElementById("projectTask").text(ecrName);

        //构建table
        for (int i = 0; i < ecrErrorPartlist.size(); i++) {
            Map errorPartmap = (Map) ecrErrorPartlist.get(i);
            JF_SendEmailUtils_mxJPO.writeTableData(context, "AllPartListTable", errorPartmap, doc, listAttr);
        }
        String htmlContent = doc.toString();
        msgBodyPart.setContent(htmlContent, "text/html;charset=utf-8");//html代码部分
        multipart.addBodyPart(msgBodyPart);
        Boolean aBoolean = JF_SendEmailUtils_mxJPO.SendEmail(context, sendEmails, "zh".equalsIgnoreCase("zh") ? "ECR撤回通知 " : "ECR Revocation notification", multipart);

    }

    public void rejectECR(Context context, String ecrid) throws Exception {
        //审核中的ECR如果有流程，获取当前流程
        String routeObjectId = (String) JPO.invoke(context, "JF_ECRProcess", new String[0], "getCurrentRoute", new String[]{ecrid}, String.class);

        DomainObject routeObj = DomainObject.newInstance(context, routeObjectId);
        StringList inboxTaskidList = routeObj.getInfoList(context, "to[Route Task].from.id");

        //获取任一审批中的任务，驳回
        String InboxTaskid = "";
        for (String inboxTaskid : inboxTaskidList) {
            InboxTask taskObj = (InboxTask) DomainObject.newInstance(context, inboxTaskid);
            String InboxCurrent = taskObj.getCurrentState(context).getName();
            JF_LOGGER.info("InboxCurrent:{} taskObj.getOwner(context).getName() : dd:{}", InboxCurrent, taskObj.getOwner(context).getName(), taskObj.getInfo(context, SELECT_OWNER));
            if (!"Complete".equalsIgnoreCase(InboxCurrent)) {
                InboxTaskid = inboxTaskid;
                break;
            }
        }

        //理论上不会为空，为空流程已经走完。
        if (UIUtil.isNotNullAndNotEmpty(InboxTaskid)) {
            rejectECRReviewTask(context, InboxTaskid, routeObjectId);
        }
    }


    public void rejectECRReviewTask(Context context, String taskId, String routeId) throws Exception {
        DomainObject taskpro = DomainObject.newInstance(context, taskId);
        DomainObject routeobj = DomainObject.newInstance(context, routeId);

        String taskowner = taskpro.getOwner(context).getName();

        StringList selectObjStmt = new StringList();
        selectObjStmt.add(DomainObject.SELECT_ID);
        selectObjStmt.add(DomainObject.SELECT_NAME);
        selectObjStmt.add(DomainConstants.SELECT_CURRENT);
        StringList selectRelStmt = new StringList();
        selectRelStmt.add(DomainRelationship.SELECT_ID);
        selectRelStmt.add("attribute[Route Node ID]");
        selectRelStmt.add("attribute[Route Sequence]");
        selectRelStmt.add("attribute[Comments]");
        selectRelStmt.add("attribute[Route Instructions]");

        String objWhere = "name == '" + taskowner + "'";
//            String objWhere = DomainObject.EMPTY_STRING;
        String relWhere = DomainObject.EMPTY_STRING;
        //oldNodeInfos信息用于设置
        MapList NodeInfos = routeobj.getRelatedObjects(context, // context
                "Route Node", // relationship pattern
                "Person", // object pattern
                selectObjStmt, // object selects
                selectRelStmt, // relationship selects
                Boolean.FALSE, // to direction
                Boolean.TRUE, // from direction
                (short) 1, // recursion level
                objWhere, // object where clause
                relWhere, // relationship where clause
                0);
        JF_LOGGER.info("taskowner--->" + taskowner);
        JF_LOGGER.info("NodeInfos--->" + NodeInfos);
        if (NodeInfos.size() > 0) {
            Map TaskInfos = (Map) NodeInfos.get(0);
            String Comments = "No";
            String connId = String.valueOf(TaskInfos.get(DomainRelationship.SELECT_ID));

            HashMap inboxTaskMap = new HashMap();
            inboxTaskMap.put("Approval Status", "Reject");
            inboxTaskMap.put("Task Comments Needed", "No");
            inboxTaskMap.put("Comments", "The system automatically rejected it because it contained an old release version.");
            taskpro.setAttributeValues(context, inboxTaskMap);
            try {
                ContextUtil.pushContext(context);
                taskpro.promote(context);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ContextUtil.popContext(context);
            }


            if (UIUtil.isNotNullAndNotEmpty(Comments)) {
                String MQLstmt = "modify bus " + taskId + " Comments '" + Comments + "'";
                String MQLret = MqlUtil.mqlCommand(context, MQLstmt, false);
                String MQLstmts = "modify connection " + connId + " Comments '" + Comments + "'";
                String MQLrets = MqlUtil.mqlCommand(context, MQLstmts, false);
            }
            String MQLstmtss = "modify connection " + connId + " 'Approval Status'" + " Reject";
            String MQLretss = MqlUtil.mqlCommand(context, MQLstmtss, false);

        }
    }


    public void TestEcr(Context context, String args[]) throws Exception {
        try {
            StringList idlist = new StringList();
            idlist.add("14585.59252.44744.25198");
            getReviewECRReject(context, idlist,"");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据没有符合条件上级的父件及其当前下级，补充冒泡根节点结果
     * 父件只向上传播一次，但同一根父件存在多个变更下级时，仍需保留原有的供货件和GX判断
     *
     * @param context 上下文
     * @param prePartId 当前父件的下级旧版本ID
     * @param preLogicalid 当前父件下级的逻辑ID
     * @param parentPartId 没有符合条件上级的父件ID
     * @param revBubbleMap 旧版本ID与冒泡版本ID的对应关系
     * @param bubbleList 冒泡根节点集合
     * @param bubbleGXSet GX冒泡节点集合
     * @param psZeroPartList 项目供货件逻辑ID集合
     * @author LIUJR
     * @throws Exception 获取零件属性失败
     * @return void
     * @date 2026/7/27
     */
    private static void collectBubbleRootPart(Context context, String prePartId, String preLogicalid, String parentPartId,
                                              Map<String, String> revBubbleMap, HashSet<String> bubbleList,
                                              HashSet<String> bubbleGXSet, StringList psZeroPartList) throws Exception {
        DomainObject parentPartObject = DomainObject.newInstance(context, parentPartId);
        String parentPartType = parentPartObject.getAttributeValue(context, ATTR_JFPartType);
        JF_LOGGER.info("根父件类型:{}, 下级是否为供货件:{}, 下级冒泡版本:{}",
                parentPartType, psZeroPartList.contains(preLogicalid), revBubbleMap.get(prePartId));
        if ("X".equalsIgnoreCase(parentPartType) && psZeroPartList.contains(preLogicalid)) {
            String prePartBubbleId = revBubbleMap.get(prePartId);
            if (UIUtil.isNotNullAndNotEmpty(prePartBubbleId)) {
                bubbleList.add(prePartBubbleId);
            }
            String parentBubbleId = revBubbleMap.get(parentPartId);
            if (UIUtil.isNotNullAndNotEmpty(parentBubbleId)) {
                bubbleGXSet.add(parentBubbleId);
            }
        } else {
            String parentBubbleId = revBubbleMap.get(parentPartId);
            if (UIUtil.isNotNullAndNotEmpty(parentBubbleId)) {
                bubbleList.add(parentBubbleId);
            }
        }
    }
}
