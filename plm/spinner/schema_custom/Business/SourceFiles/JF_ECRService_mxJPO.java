import com.alibaba.fastjson.JSON;
import com.dscn.plm.util.NioJDUtils;
import com.google.gson.Gson;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.DocumentUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import ds.enovia.apps.msf.BusinessObjects.ExecuteJPO.args;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMultipart;
import javassist.compiler.ast.StringL;
import matrix.db.*;
import matrix.util.StringList;
import org.antlr.v4.runtime.misc.IntegerList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.formula.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.dassault_systemes.enovia.apps.materialcomposition.MATCConstants.STRING_NEW_VALUE;
import static com.dassault_systemes.product.common.services.utility.Value.VPMInstance;
import static com.matrixone.apps.domain.DomainConstants.*;


/**
 * @author CHENYAN
 * @version v1.0.0
 * 创建时间：2024/7/19 14:16
 * @description ECR 业务处理相关
 */
public class JF_ECRService_mxJPO {
    private static final Logger _logger = LoggerFactory.getLogger(JF_ECRService_mxJPO.class);
    //ECR 类型
    private static final String ATTR_JFECRTYPE = "JFECRType";
    private static final String SELECT_ATTR_JFECRTYPE = "attribute[" + ATTR_JFECRTYPE + "]";
    //是否平台件
    private static final String ATTR_JFISPLATFORMPART = "JFIsPlatformPart";
    private static final String ATTR_JFCHANGESOURCE = "JFChangeSource";
    private static final String ATTR_JFIsFollow = "JFIsFollow";
    private static final String SELECT_ATTR_JFCHANGESOURCE = "attribute[" + ATTR_JFCHANGESOURCE + "]";

    private static final String ATTR_JFProjectPhase = "JFProjectPhase";

    private static final String SELECT_ATTR_JFProjectPhase = "attribute[" + ATTR_JFProjectPhase + "]";

    private static final String SELECT_ATTR_JFISPLATFORMPART = "attribute[" + ATTR_JFISPLATFORMPART + "]";
    //影响因素
    private static final String ATTR_JFAFFECTEDFACTORY = "JFAffectedFactory";
    private static final String SELECT_ATTR_JFAFFECTEDFACTORY = "attribute[" + ATTR_JFAFFECTEDFACTORY + "]";

    //开发费工时变更（小时）
    private static final String ATTR_JFCHANGESDEVEEXPENSESMANHOURS = "JFChangesDeveExpensesManHours";
    private static final String SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS = "attribute[" + ATTR_JFCHANGESDEVEEXPENSESMANHOURS + "]";
    //开发费工时变更（小时）- 外部
    private static final String ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL = "JFChangesDeveExpensesManHoursExternal";
    private static final String SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL = "attribute[" + ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL + "]";
    //试验开发费变更（元）
    private static final String ATTR_JFCHANGESTRIALEXPENSESMANHOURS = "JFChangesTrialExpensesManHours";
    private static final String SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURS = "attribute[" + ATTR_JFCHANGESTRIALEXPENSESMANHOURS + "]";
    //试验费变更（元）-外部
    private static final String ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL = "JFChangesTrialExpensesManHoursExternal";
    private static final String SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL = "attribute[" + ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL + "]";
    //变更原因
    private static final String ATTR_JFCHANGERESON = "JFChangeReson";
    private static final String SELECT_ATTR_JFCHANGERESON = "attribute[" + ATTR_JFCHANGERESON + "]";
    private static final String TYPE_JFECR = "JFECR";
    private static final String TYPE_JFNewECR = "JFNewECR";
    private static final String TYPE_VPMREFERENCE = "VPMReference";
    //ECR和受影响关系
    private static final String REL_JFRELATEITEM = "JFRelateItem";
    private static final String SELECT_JFECR2PartPriceID = "to[JFECR2PartPrice].id";

    private static final String ATTR_JFQQID = "JFQQ";
    private static final String SELECT_ATTR_JFQQID = "attribute[" + ATTR_JFQQID + "]";
    private static final String ATTR_JFAssociatedOtherProjects = "JFAssociatedOtherProjects";

    private static final String SELECT_ATTR_JFAssociatedOtherProjects = "attribute[" + ATTR_JFAssociatedOtherProjects + "]";

    private static final String TEMPLATE_EXCEL_PATH = "jf_template/";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    // 定义输入日期的格式
    private static final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a", Locale.US);
    // 定义输出日期的格式
    private static final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日ahh:mm:ss", Locale.CHINA);

    private static final DateTimeFormatter outputFormatter1 = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA);

    private static final Map<String, String> ecpCurrentTranslationMap = new HashMap<>();

    static {
        // ecp状态初始化翻译映射
        ecpCurrentTranslationMap.put("Create", "草稿");
        ecpCurrentTranslationMap.put("Assign", "To Do");
        ecpCurrentTranslationMap.put("Active", "工作中");
        ecpCurrentTranslationMap.put("Review", "审批中");
        ecpCurrentTranslationMap.put("Complete", "已完成");
    }

    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取登录人相关的ECR
     * @author CHENYAN
     * @date 2024/7/22 9:35
     */
    public MapList getECRsByLoginUser(Context context, String[] args) throws Exception {
        _logger.info("-----------------------------------getECRsByLoginUser begin ---------------------------------------------");
        String strLoginUser = context.getUser();
        Vector assignments = PersonUtil.getAssignments(context, strLoginUser);
        _logger.info("assignments:{}", assignments);
        boolean isShowAll = false;
        if (assignments.contains(JF_PLMConstants_mxJPO.ROLE_ECRADMIN)) {
            isShowAll = true;
        }
        StringList typeSelectList = new StringList();
        typeSelectList.add(SELECT_ID);
        typeSelectList.add(SELECT_ATTR_JFECRTYPE);
        typeSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
        typeSelectList.add(SELECT_ATTR_JFISPLATFORMPART);
        typeSelectList.add(SELECT_ATTR_JFAFFECTEDFACTORY);
        typeSelectList.add(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS);
        typeSelectList.add(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL);
        typeSelectList.add(SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURS);
        typeSelectList.add(SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL);
        typeSelectList.add(SELECT_ATTR_JFCHANGERESON);
        typeSelectList.add(SELECT_DESCRIPTION);
        typeSelectList.add(SELECT_ATTR_JFECRTYPE);
        String strWhere = "";
        if (!isShowAll) {
            strWhere = JF_PublicMethodClass_mxJPO.buildStringInStrings("owner=='", strLoginUser, "'");
        }
        MapList res = DomainObject.findObjects(context, TYPE_JFECR + "," + TYPE_JFNewECR, "*", strWhere, typeSelectList);
        _logger.info("size :{}", res.size());
        _logger.info("-----------------------------------getECRsByLoginUser end ---------------------------------------------");
        return res;
    }

    /**
     * @param context
     * @param args
     * @return java.util.Map
     * @throws
     * @description 是否平台间 Range值重新排序
     * @author CHENYAN
     * @date 2024/7/22 17:21
     */
    public Map getJFIsPlatformPartRanges(Context context, String[] args) throws Exception {
        HashMap<Object, Object> res = new HashMap<>();
        StringList ranges = FrameworkUtil.getRanges(context, "JFIsPlatformPart");
        _logger.info("ranges:{}", ranges);
        StringList sortRanges = new StringList(ranges.size());
        if (ranges.contains("Yes")) {
            sortRanges.add("Yes");
            ranges.remove("Yes");
        }
        if (ranges.contains("No")) {
            sortRanges.add("No");
            ranges.remove("No");
        }
        if (ranges.size() > 0) {
            sortRanges.addAll(ranges);
        }
        StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, "JFIsPlatformPart", sortRanges, context.getLocale().toString());
        res.put("field_choices", sortRanges);
        res.put("field_display_choices", nlsRanges);
        _logger.info("ranges:{}", sortRanges);
        return res;
    }

    /**
     * @param context
     * @param args
     * @return boolean
     * @throws
     * @description 获取ECR中受影响对象中 ToolBar的访问权限
     * @author CHENYAN
     * @date 2024/7/23 18:02
     */
    public boolean getECRAffectedItemsToolBarAccess(Context context, String[] args) throws Exception {
        HashMap inputMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) inputMap.get("requestMap");
        String strObjectId = null;
        if (requestMap == null) {
            strObjectId = (String) inputMap.get("objectId");
        } else {
            strObjectId = (String) requestMap.get("objectId");
        }
        String strLoginUser = context.getUser();
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        StringList typeSelectList = new StringList();
        typeSelectList.add(SELECT_OWNER);
        typeSelectList.add(SELECT_CURRENT);
        Map ecrInfoMap = ecr.getInfo(context, typeSelectList);
        if ("Create".equals(ecrInfoMap.get(SELECT_CURRENT)) && strLoginUser.equals(ecrInfoMap.get(SELECT_OWNER))) {
            return true;
        }
        return false;
    }

    public String getSearchUrl(Context context, String[] args) {
        String strObjectId = args[0];
        String strParentId = args[1];
        StringBuffer sbUrl = new StringBuffer("../common/emxFullSearch.jsp?field=TYPES=type_VPMReference:CURRENT=policy_VPLM_SMB_Definition_MajorRev.state_Review,policy_VPLM_SMB_Definition.state_Review&showInitialResults=true&table=PMCGeneralSearchResults&selection=multiple");
        sbUrl.append("&objectId=");
        sbUrl.append(strObjectId);
        sbUrl.append("&parentId=");
        sbUrl.append(strParentId);
        sbUrl.append("&includeOIDprogram=JF_ECRService:filterAffectedItems");
        sbUrl.append("&hideHeader=true&cancelLabel=emxFramework.Command.Cancel");
        sbUrl.append("&submitURL=../common/JF_ECRPostProcessAddAffectedItems.jsp");
        return sbUrl.toString();
    }

    public StringList filterAffectedItems(Context context, String[] args) throws Exception {
//        ProjectSpace projectSpace = new ProjectSpace();
//        Map parameterMap = JPO.unpackArgs(args);
//        String strObjectId = (String) parameterMap.get("objectId");
//        String strMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("print bus '",strObjectId,"' select from[JFChange2Project].to.from[JFProject2RootPart|attribute[JFZeroPart]==Y&&to.type==VPMReference].to.id dump |");
//        String strRootPartIds = MqlUtil.mqlCommand(context,false , strMql, true);
//        _logger.info("strRootPartIds:{}",strRootPartIds);
//        StringList rootPartIdList = FrameworkUtil.split(strRootPartIds,"|");
        //过滤出没有关联ECR的VPMReference且最新版
        //  islast=true 无法过滤 VPMReference 中判断最新版是用属性判断
//        DomainObject rootPart = DomainObject.newInstance(context);
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(SELECT_CURRENT);
        typeSelectList.add("to[JFRelateItem]");
        typeSelectList.add("attribute[PLMReference.V_isLastVersion]");
//        MapList res = new MapList();
//        for (int i = 0; i < rootPartIdList.size(); i++) {
//            String strRootPartId = rootPartIdList.get(i);
//            rootPart.setId(strRootPartId);
//            MapList maps = rootPart.getRelatedObjects(context, VPMInstance, // relationship pattern
//                    TYPE_VPMREFERENCE,                                    // object pattern
//                    typeSelectList,                            // object selects
//                    JF_Util_mxJPO.basicRellistSel(), // relationship selects
//                    false,                                        // to direction
//                    true,                                        // from direction
//                    (short) 0,                                    // recursion level
//                    "",                // object where clause
//                    "",
//                    (short) 0);
//            res.addAll(maps);
//        }
//        MapList res = DomainObject.findObjects(context, TYPE_VPMREFERENCE, "*", "to[JFRelateItem]==FALSE", new StringList(SELECT_ID));
        MapList res = DomainObject.findObjects(context, TYPE_VPMREFERENCE, "*", "current=='FROZEN'&&to[JFRelateItem]==FALSE&&attribute[PLMReference.V_isLastVersion]==TRUE", new StringList(SELECT_ID));
        //ECR添加受影响对象，只能添加关联项目的数据
//        _logger.info("res:{}",res);
//        Set oidSet = (Set) res.stream().filter(m -> {
//            Map info = (Map) m;
//            String strCurrent = (String) info.get(SELECT_CURRENT);
//            String strConnECRFlag = (String) info.get("to[JFRelateItem]");
//            String strIsLast = (String) info.get("attribute[PLMReference.V_isLastVersion]");
//            boolean isPast = false;
//            if ("FROZEN".equals(strCurrent) && "FALSE".equalsIgnoreCase(strConnECRFlag) && "TRUE".equalsIgnoreCase(strIsLast)) {
//                isPast = true;
//            }
//            return isPast;
//        }).map(m -> {
//            Map info = (Map) m;
//            return info.get(SELECT_ID);
//        }).collect(Collectors.toSet());
        Set oidSet = (Set) res.stream().map(m -> {
            Map info = (Map) m;
            return info.get(SELECT_ID);
        }).collect(Collectors.toSet());
        return StringList.create(oidSet);
    }

    public StringList getJFChangeResourceEditAccess(Context context, String[] args) throws Exception {
        _logger.info("------------------------- getJFChangeResourceEditAccess begin ------------------------------------");
        Map paramMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) paramMap.get("requestMap");
        String strParentId = (String) requestMap.get("parentOID");
        StringList tableIdList = _getListOfKeys(args, SELECT_ID);
        DomainObject ecr = DomainObject.newInstance(context, strParentId);
        String current = ecr.getInfo(context, SELECT_CURRENT);
        String owner = ecr.getInfo(context, SELECT_OWNER);
        String loginUser = context.getUser();
        String strChangeResource = ecr.getInfo(context, SELECT_ATTR_JFCHANGESOURCE);
        StringList res = new StringList(tableIdList.size());
        Boolean isEdit = Boolean.FALSE;
        if ("Both".equals(strChangeResource) && "Create".equalsIgnoreCase(current)&&owner.equals(loginUser)) {
            isEdit = Boolean.TRUE;
        }
        for (int i = 0; i < tableIdList.size(); i++) {
            res.add(isEdit.toString());
        }
        _logger.info("res:{}", res);
        _logger.info("------------------------- getJFChangeResourceEditAccess end ------------------------------------");
        return res;
    }

    public StringList getJFIsFollowEditAccess(Context context, String[] args) throws Exception {
        _logger.info("------------------------- getJFChangeResourceEditAccess begin ------------------------------------");
        Map paramMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) paramMap.get("requestMap");
        String strParentId = (String) requestMap.get("parentOID");
        StringList tableIdList = _getListOfKeys(args, SELECT_ID);
        DomainObject ecr = DomainObject.newInstance(context, strParentId);
        String current = ecr.getInfo(context, SELECT_CURRENT);
        String owner = ecr.getInfo(context, SELECT_OWNER);
        String loginUser = context.getUser();
        StringList res = new StringList(tableIdList.size());
        Boolean isEdit = Boolean.FALSE;
        if ("Create".equalsIgnoreCase(current)&& owner.equalsIgnoreCase(loginUser)) {
            isEdit = Boolean.TRUE;
        }
        for (int i = 0; i < tableIdList.size(); i++) {
            res.add(isEdit.toString());
        }
        _logger.info("res:{}", res);
        _logger.info("------------------------- getJFChangeResourceEditAccess end ------------------------------------");
        return res;
    }

    /**
     * @param context
     * @param args
     * @return void
     * @throws
     * @description 修改变更来源属性
     * @author CHENYAN
     * @date 2024/7/25 15:24
     */
    public void updateJFChangeResource(Context context, String[] args) throws Exception {
        Map parameter = (Map) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) parameter.get("paramMap");
        String strRelId = (String) paramMap.get("relId");
        //修改value值
        String strNewValue = (String) paramMap.get("New Value");
        DomainRelationship rel = DomainRelationship.newInstance(context, strRelId);
        rel.setAttributeValue(context, ATTR_JFCHANGESOURCE, strNewValue);
    }

    public void updateJFIsFollow(Context context, String[] args) throws Exception {
        Map parameter = (Map) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) parameter.get("paramMap");
        String strRelId = (String) paramMap.get("relId");
        //修改value值
        String strNewValue = (String) paramMap.get("New Value");
        DomainRelationship rel = DomainRelationship.newInstance(context, strRelId);
        rel.setAttributeValue(context, ATTR_JFIsFollow, strNewValue);
    }

    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取受影响零件 Table数据
     * @author CHENYAN
     * @date 2024/7/25 15:24
     */
    public MapList getECRAffectedItems(Context context, String[] args) throws Exception {
        _logger.info("-----------------------------------getECRAffectedItems begin ---------------------------------------------");
        Map parameters = JPO.unpackArgs(args);
        String strObjectId = (String) parameters.get("objectId");
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        StringList typeSelectList = new StringList(SELECT_ID);
        reSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
        MapList maps = ecr.getRelatedObjects(context, REL_JFRELATEITEM, // relationship pattern
                TYPE_VPMREFERENCE,                                    // object pattern
                typeSelectList,                            // object selects
                reSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        _logger.info("-----------------------------------getECRAffectedItems end ---------------------------------------------");
        return maps;
    }

    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取受影响父件Table数据
     * @author CHENYAN
     * @date 2024/7/25 15:25
     */
    public MapList getECRParentAffectedItems(Context context, String[] args) throws Exception {
        _logger.info("-----------------------------------getECRParentAffectedItems begin ---------------------------------------------");
        Map parameters = JPO.unpackArgs(args);
        String strObjectId = (String) parameters.get("objectId");
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        StringList typeSelectList = new StringList(SELECT_ID);
        reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubIFReplace);
        reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubPartId);
        reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubConnectId);
        MapList maps = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_JFRelateItemParent, // relationship pattern
                TYPE_VPMREFERENCE,                                    // object pattern
                typeSelectList,                            // object selects
                reSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        _logger.info("-----------------------------------getECRParentAffectedItems end ---------------------------------------------");
        return maps;
    }

    /**
     * @param var0
     * @return matrix.util.StringList
     * @throws
     * @description 获取Table中request 中行中1 指定key value
     * @author CHENYAN
     * @date 2024/7/25 15:20
     */
    public static StringList _getListOfKeys(String[] var0, String strKey) throws Exception {
        StringList var1 = new StringList();
        Map var2 = (Map) JPO.unpackArgs(var0);
        MapList var3 = (MapList) var2.get("objectList");
        if (var3 != null) {
            int var4 = 0;

            for (int var5 = var3.size(); var4 < var5; ++var4) {
                Map var6 = (Map) var3.get(var4);
                if (null != var6 && var6.containsKey(strKey)) {
                    String var7 = (String) var6.get(strKey);
                    var1.add(var7);
                } else {
                    var1.add("");
                }
            }
        }
        return var1;
    }

    /**
     * @param context
     * @param args
     * @return java.util.Map
     * @throws
     * @description 是否平台间 Range值重新排序
     * @author CHENYAN
     * @date 2024/7/22 17:21
     */
    public Map getJFChangeResourceRanges(Context context, String[] args) throws Exception {
        _logger.info("-------------------------- getJFChangeResourceRanges begin ------------------------------------------");
        HashMap<Object, Object> res = new HashMap<>();
        StringList ranges = FrameworkUtil.getRanges(context, ATTR_JFCHANGESOURCE);
        StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, ATTR_JFCHANGESOURCE, ranges, context.getLocale().toString());
        res.put("field_choices", ranges);
        res.put("field_display_choices", nlsRanges);
        _logger.info("res:{}", res);
        _logger.info("-------------------------- getJFChangeResourceRanges end ------------------------------------------");
        return res;
    }

    /**
     * @param context
     * @param args
     * @return java.util.Map
     * @throws
     * @description 获取是否替换range值
     * @author CHENYAN
     * @date 2024/7/25 15:20
     */
    public Map getJFSubIFReplaceRanges(Context context, String[] args) throws Exception {
        _logger.info("-------------------------- getJFSubIFReplaceRanges begin ------------------------------------------");
        HashMap<Object, Object> res = new HashMap<>();
        StringList ranges = FrameworkUtil.getRanges(context, JF_PLMConstants_mxJPO.ATTR_JFSubIFReplace);
        StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, JF_PLMConstants_mxJPO.ATTR_JFSubIFReplace, ranges, context.getLocale().toString());
        res.put("field_choices", ranges);
        res.put("field_display_choices", nlsRanges);
        _logger.info("res:{}", res);
        _logger.info("-------------------------- getJFSubIFReplaceRanges end ------------------------------------------");
        return res;
    }

    /**
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @description 获取受影响零件变更来源
     * @author CHENYAN
     * @date 2024/7/25 15:20
     */
    public StringList getJFChangeResource(Context context, String[] args) throws Exception {
        _logger.info("------------------------------------------  getJFChangeResource  begin ------------------------------------------");
        Map paramsMap = (Map) JPO.unpackArgs(args);
        Map paramList = (Map) paramsMap.get("paramList");
        String strParentOID = (String) paramList.get("parentOID");
        Map columnMap = (Map) paramsMap.get("columnMap");
        String strColName = (String) columnMap.get("name");
        _logger.info("strColName:{}", strColName);
        StringList tableRIdList = _getListOfKeys(args, SELECT_RELATIONSHIP_ID);
        _logger.info("tableRIdList:{}", tableRIdList);
        StringList res = new StringList();
        StringList nlsRanges = new StringList();
        for (int i = 0; i < tableRIdList.size(); i++) {
            String strRID = tableRIdList.get(i);
            DomainRelationship rel = DomainRelationship.newInstance(context, strRID);
            String strChangeResource = "";
            String strTmpAttr = "";
            if ("JFIsFollow".equals(strColName)) {
                strTmpAttr = ATTR_JFIsFollow;
            } else if ("JFChangeResource".equals(strColName)) {
                strTmpAttr = ATTR_JFCHANGESOURCE;

            }
            strChangeResource = rel.getAttributeValue(context, strTmpAttr);
            nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, strTmpAttr, StringList.create(strChangeResource), context.getLocale().toString());
            if (nlsRanges.size() > 0) {
                res.add(nlsRanges.get(0));
            } else {
                res.add(strChangeResource);
            }
        }
        return res;
    }

    public void updateJFSubIFReplace(Context context, String[] args) throws Exception {
        Map parameter = (Map) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) parameter.get("paramMap");
        String strRelId = (String) paramMap.get("relId");
        try {
            ContextUtil.pushContext(context);
            //修改value值
            String strNewValue = (String) paramMap.get("New Value");
            DomainRelationship rel = DomainRelationship.newInstance(context, strRelId);
            rel.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JFSubIFReplace, strNewValue);
        } finally {
            ContextUtil.popContext(context);
        }

    }

    /**
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @description 获取受影响附件的是否替换值
     * @author CHENYAN
     * @date 2024/7/25 15:18
     */
    public StringList getJFSubIFReplaceValue(Context context, String[] args) throws Exception {
        _logger.info("------------------------------------------  getJFSubIFReplaceValue  begin ------------------------------------------");
        Map paramMap = (Map) JPO.unpackArgs(args);
        StringList tableRIdList = _getListOfKeys(args, SELECT_RELATIONSHIP_ID);
        _logger.info("tableRIdList:{}", tableRIdList);
        StringList res = new StringList();
        for (int i = 0; i < tableRIdList.size(); i++) {
            String strRID = tableRIdList.get(i);
            DomainRelationship rel = DomainRelationship.newInstance(context, strRID);
            String strChangeResource = rel.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JFSubIFReplace);
            StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, JF_PLMConstants_mxJPO.ATTR_JFSubIFReplace, StringList.create(strChangeResource), context.getLocale().toString());
            if (nlsRanges.size() > 0) {
                res.add(nlsRanges.get(0));
            } else {
                res.add(strChangeResource);
            }
        }
        return res;
    }

    /**
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @description 获取图纸状态和图纸
     * @author CHENYAN
     * @date 2024/7/25 15:23
     */
    public Vector getConnectionDrawingStatus(Context context, String[] args) throws Exception {
        _logger.info("------------------------------------------  getConnectionDrawingStatus  begin ------------------------------------------");
        Map paramsMap = (Map) JPO.unpackArgs(args);
        Map columnMap = (Map) paramsMap.get("columnMap");
        String strColName = (String) columnMap.get("name");
        StringList tableIdList = _getListOfKeys(args, SELECT_ID);
        _logger.info("tableIdList:{}", tableIdList);
        Vector res = new Vector();
        String strSelectKey = "";
        if ("ConnectionDrawing".equals(strColName)) {
            strSelectKey = "to[XCADBaseDependency].from.attribute[PLMEntity.V_Name]";
        } else if ("ConnectionDrawingStatus".equals(strColName)) {
            strSelectKey = "to[XCADBaseDependency].from.current";
        }
        for (int i = 0; i < tableIdList.size(); i++) {
            String strID = tableIdList.get(i);
            DomainObject obj = DomainObject.newInstance(context, strID);
            StringList toCurrentList = obj.getInfoList(context, strSelectKey);
            if (toCurrentList.size() >= 1) {
                StringBuffer sb = new StringBuffer();
                sb.append("<div>");
                for (int i1 = 0; i1 < toCurrentList.size(); i1++) {
                    String strToCurrent = toCurrentList.get(i1);
                    if ("ConnectionDrawingStatus".equals(strColName)) {
                        strToCurrent = EnoviaResourceBundle.getStateI18NString(context, "VPLM_SMB_Definition_MajorRev", strToCurrent, context.getLocale().toString());
                    }
                    sb.append(strToCurrent);
                    sb.append("<br/>");
                }
                sb.append("</div>");
                res.add(sb.toString());
            } else {
                res.add("");
            }
        }
        return res;
    }

    /**
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @description 获取制造件中变更来源的值
     * @author CHENYAN
     * @date 2024/7/25 15:23
     */
    public StringList getMakeTableChangeSource(Context context, String[] args) throws Exception {
        Map paramMap = (Map) JPO.unpackArgs(args);
        _logger.info("paramMap:{}", paramMap);
        StringList tableIdList = _getListOfKeys(args, SELECT_ID);
        _logger.info("tableIdList:{}", tableIdList);
        StringList res = new StringList();
        for (int i = 0; i < tableIdList.size(); i++) {
            res.add("11111");
        }
        return res;
    }

    /**
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @description 获取受影响子件零件中文名
     * @author CHENYAN
     * @date 2024/7/25 15:47
     */
    public StringList getSunPartNameCN(Context context, String[] args) throws Exception {
        _logger.info("------------------------------------------  getSunPartNameCN  begin ------------------------------------------");
        StringList tableIdList = _getListOfKeys(args, JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubPartId);
        _logger.info("tableIdList:{}", tableIdList);
        StringList res = new StringList();
        for (int i = 0; i < tableIdList.size(); i++) {
            String strID = tableIdList.get(i);
            String strAttrValue = "";
            if (exists(context, strID)) {
                DomainObject obj = DomainObject.newInstance(context, strID);
                strAttrValue = obj.getInfo(context, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
            }
            res.add(strAttrValue);
        }
        return res;
    }

    /**
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @description 获取受影响子件零件中文名
     * @author CHENYAN
     * @date 2024/7/25 15:47
     */
    public StringList getSunPartNumber(Context context, String[] args) throws Exception {
        _logger.info("------------------------------------------  getSunPartNumber  begin ------------------------------------------");
        StringList tableIdList = _getListOfKeys(args, JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubPartId);
        _logger.info("tableIdList:{}", tableIdList);
        StringList res = new StringList();
        for (int i = 0; i < tableIdList.size(); i++) {
            String strID = tableIdList.get(i);
            String strAttrValue = "";
            if (exists(context, strID)) {
                DomainObject obj = DomainObject.newInstance(context, strID);
                strAttrValue = obj.getInfo(context, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            }
            res.add(strAttrValue);
        }
        return res;
    }

    /**
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @description 获取受影响子件零件中文名
     * @author CHENYAN
     * @date 2024/7/25 15:47
     */
    public StringList getSunPartRevision(Context context, String[] args) throws Exception {
        _logger.info("------------------------------------------  getSunPartNumber  begin ------------------------------------------");
        StringList tableIdList = _getListOfKeys(args, JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubPartId);
        _logger.info("tableIdList:{}", tableIdList);
        StringList res = new StringList();
        for (int i = 0; i < tableIdList.size(); i++) {
            String strID = tableIdList.get(i);
            String strRevision = "";
            if (exists(context, strID)) {
                DomainObject obj = DomainObject.newInstance(context, strID);
                strRevision = obj.getInfo(context, SELECT_REVISION);
            }
            res.add(strRevision);
        }
        return res;
    }

    /**
     * @param context
     * @param args
     * @return int  0 ，1
     * @throws
     * @description ECR创建提升到定义时检查trigger
     * @author CHENYAN
     * @date 2024/7/25 15:22
     */
    public int checkECRHasPromoteAccess(Context context, String[] args) throws Exception {
        try {
            String strObjectId = args[0];
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
            StringList typeSelectList = new StringList(SELECT_ID);
            typeSelectList.add(SELECT_CURRENT);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPART_NUMBER);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_CADOrigin);
//            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_TO_INSTANCE_ATTR_V_CADOrigin);
//            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_TO_INSTANCE_ATTR_V_CADOrigin);
//            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_TO_INSTANCE_FROM_ID);
            //标识是总成还是part
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_USAGE);
            reSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
            reSelectList.add(SELECT_FROM_ID);
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubIFReplace);
            _logger.info("");
            MapList maps = ecr.getRelatedObjects(context, REL_JFRELATEITEM + "," + JF_PLMConstants_mxJPO.REL_JFRelateItemParent, // relationship pattern
                    TYPE_VPMREFERENCE,                                    // object pattern
                    typeSelectList,                            // object selects
                    reSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            if (maps.size() > 0) {
                //未维护 JF_VPMReference.JF_DirectBuy 属性 零件号集合
                //未维护 采购类型 零件号集合
                StringList notWritePartList = new StringList();
                Map<String, Set<String>> notContainPartMap = new HashMap<>();
                //分组
                Map groupMap = (Map) maps.stream().collect(Collectors.groupingBy(m -> {
                    Map info = (Map) m;
                    return info.get("relationship");
                }));
                _logger.info("groupMap:{}", groupMap);
                boolean isAlert = false;
                if (groupMap.containsKey(REL_JFRELATEITEM)) {
                    List affectedItemsList = (List) groupMap.get(REL_JFRELATEITEM);
                    Set affectedItemsIdSet = (Set) affectedItemsList.stream().map(m -> {
                        Map info = (Map) m;
                        return info.get(SELECT_ID);
                    }).collect(Collectors.toSet());
                    //add by 需要校验一级件的这两个属性是否维护 2.如果是子件需要校验一级件中的这两个属性是否维护
                    for (int i = 0; i < affectedItemsList.size(); i++) {
                        Map affectedItem = (Map) affectedItemsList.get(i);
                        String strDirectBuy = (String) affectedItem.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                        String strProcurementType = (String) affectedItem.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                        String strAffectedItemId = (String) affectedItem.get(SELECT_ID);
                        String strAffectedItemName = (String) affectedItem.get(SELECT_NAME);
                        String strPartNum = (String) affectedItem.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                        //未维护 JF_VPMReference.JF_DirectBuy
                        if (UIUtil.isNullOrEmpty(strDirectBuy) || UIUtil.isNullOrEmpty(strProcurementType)) {
                            isAlert = true;
                            notWritePartList.add(strPartNum);
                        }
//                        if (affectedItem.containsKey(JF_PLMConstants_mxJPO.SELECT_ATTR_V_CADOrigin)){
//                            String strFlagOnePart = (String) affectedItem.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_CADOrigin);
//                            Object strFromFlagOnePart = UIUtil.getValue(affectedItem,JF_PLMConstants_mxJPO.SELECT_TO_INSTANCE_ATTR_V_CADOrigin);
//
//                            //父件还是 CATIAV5 标识为一级件
//                            if ("CATIAV5".equals(strFlagOnePart) && "CATIAV5".equals(strFromFlagOnePart)){
//                                String strFromId = UIUtil.getValue(affectedItem,JF_PLMConstants_mxJPO.SELECT_TO_INSTANCE_FROM_ID);
//                                DomainObject fromObj = DomainObject.newInstance(context, strFromId);
//                                StringList fromTypeSelect = new StringList();
//                                fromTypeSelect.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
//                                fromTypeSelect.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
//                                fromTypeSelect.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
//                                Map fromObjInfo = fromObj.getInfo(context, fromTypeSelect);
//                                String strFromDirectBuy = (String) fromObjInfo.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
//                                String strFromProcurementType = (String) fromObjInfo.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
//                                String strFromPartNum = (String) fromObjInfo.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
//                                //未维护 JF_VPMReference.JF_DirectBuy
//                                if (UIUtil.isNullOrEmpty(strFromDirectBuy) || UIUtil.isNullOrEmpty(strFromDirectBuy)){
//                                    isAlert = true;
//                                    notWritePartList.add(strFromPartNum);
//                                }
//                            }
//                        }
                        //零件清单中如果存在装配，需要校验该装配实际关联的子件已经发布或装配下的未发布零件已经存在于该CR的零件清单中。
                        //装配和零件标识
                        String strV_Usage = (String) affectedItem.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_USAGE);
                        if (UIUtil.isNullOrEmpty(strV_Usage)) {
                            DomainObject usAgeBo = DomainObject.newInstance(context, strAffectedItemId);
                            StringList usAgeTypeSelectList = JF_Util_mxJPO.basicBolistSel();
                            usAgeTypeSelectList.add(SELECT_CURRENT);
                            usAgeTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPART_NUMBER);
                            usAgeTypeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                            //优化逻辑，只有是总成的时候才查询
                            MapList checkMapList = usAgeBo.getRelatedObjects(context, VPMInstance, // relationship pattern
                                    TYPE_VPMREFERENCE,                                    // object pattern
                                    usAgeTypeSelectList,                            // object selects
                                    JF_Util_mxJPO.basicRellistSel(), // relationship selects
                                    false,                                        // to direction
                                    true,                                        // from direction
                                    (short) 0,                                    // recursion level
                                    "",                // object where clause
                                    "",
                                    (short) 0);
//                            MapList checkMapList = checkMapValueContainByMapList(VPMInstanceList, SELECT_FROM_ID, strAffectedItemId);
                            if (null != checkMapList && checkMapList.size() > 0) {
                                //装配下存在零件
                                for (int i1 = 0; i1 < checkMapList.size(); i1++) {
                                    Map checkMap = (Map) checkMapList.get(i1);
                                    String strCurrent = (String) checkMap.get(SELECT_CURRENT);
                                    String strCheckId = (String) checkMap.get(SELECT_ID);
                                    String strSubPartNum = (String) checkMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                                    if (UIUtil.isNullOrEmpty(strSubPartNum)) {
                                        strSubPartNum = (String) checkMap.get(SELECT_NAME);
                                    }
                                    if ((!"RELEASED".equals(strCurrent)) && (!affectedItemsIdSet.contains(strCheckId))) {
                                        isAlert = true;
                                        //临时的零件名称
                                        String strTempName = "";
                                        if (UIUtil.isNullOrEmpty(strSubPartNum)) {
                                            strTempName = strSubPartNum;
                                        } else {
                                            strTempName = strSubPartNum;
                                        }
                                        if (notContainPartMap.containsKey(strPartNum)) {
                                            Set<String> alertSubParts = notContainPartMap.get(strPartNum);
//                                            _logger.info("alertSubParts:{} strPartNum {} strPartNum {} strTempName {}",alertSubParts,notContainPartMap,strPartNum,strTempName);
                                            alertSubParts.add(strTempName);
                                        } else {
                                            Set alertSubParts = new HashSet<String>();
                                            alertSubParts.add(strTempName);
                                            notContainPartMap.put(strPartNum, alertSubParts);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }

                }
                StringList parentNotMaintain = new StringList();
                if (groupMap.containsKey(JF_PLMConstants_mxJPO.REL_JFRelateItemParent)) {
                    //需要校验该零件是否关联受影响的父件，如果关联了父件，替换规则是否维护，不允许为空。否则不允许提交状态。
                    List parentItemList = (List) groupMap.get(JF_PLMConstants_mxJPO.REL_JFRelateItemParent);
                    for (int i = 0; i < parentItemList.size(); i++) {
                        Map parentItemMap = (Map) parentItemList.get(i);
                        String strIsReplace = (String) parentItemMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubIFReplace);
                        String strPartNum = (String) parentItemMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);

                        if (UIUtil.isNullOrEmpty(strIsReplace)) {
                            //没有维护
                            parentNotMaintain.add(strPartNum);
                            isAlert = true;
                        }
                    }
                }
                //校验直线经理是否存在
                String strLinkManager = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, "", context.getUser());
                if (UIUtil.isNullOrEmpty(strLinkManager)) {
                    String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.LineManagerIsNull");
                    emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                    return 1;
                }
                String strProjectSpaceId = ecr.getInfo(context, "from[JFChange2Project].to.id");
                if (UIUtil.isNotNullAndNotEmpty(strProjectSpaceId)) {
                    Set projectIdSet = new HashSet<String>();
                    projectIdSet.add(strProjectSpaceId);
                    if (!projectIdSet.isEmpty()) {
                        StringList projectSpaceList = StringList.create(projectIdSet);
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("projectName", projectSpaceList);
                        paramsMap.put("type", ecr.getTypeName(context));
                        paramsMap.put("ecrId", strObjectId);
                        int iRes = JF_SignTask_mxJPO.checkProjectPersonRole(context, JPO.packArgs(paramsMap));
                        if (iRes == 1) {
                            return iRes;
                        }
                    }
                } else {
                    String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.AffectedItemIsNull");
                    emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                    return 1;
                }
                //提示信息
                if (isAlert) {
                    _logger.info("notWritePartList:{}", notWritePartList);
                    _logger.info("notContainPartMap:{}", notContainPartMap);
                    _logger.info("parentNotMaintain:{}", parentNotMaintain);
                    if (notContainPartMap.size() > 0) {
                        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.SubItemStatusError");
                        StringBuilder sb = new StringBuilder();
                        for (Object oEntry : notContainPartMap.entrySet()) {
                            Map.Entry entry = (Map.Entry) oEntry;
                            String strKey = (String) entry.getKey();
                            Set subPart = (Set) entry.getValue();
                            String strMess1 = strMess.replace("$1", strKey).replace("$2", StringList.create(subPart).join(","));
                            sb.append(strMess1);
                            sb.append("\n");
                        }
//                        strMess = strMess.replace("{}",StringList.create(notContainPartList).join(","));
                        emxContextUtilBase_mxJPO.mqlWarning(context, sb.toString());
                    }
                    if (notWritePartList.size() > 0) {
                        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.NotWriteDirectBuy");
                        strMess = strMess.replace("{}", notWritePartList.join(","));
                        emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                    }
                    if (parentNotMaintain.size() > 0) {
                        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.ParentNotWriteError");
                        strMess = strMess.replace("{}", parentNotMaintain.join(","));
                        emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                    }
                    return 1;
                } else {
                    return 0;
                }
            } else {
                String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.AffectedItemIsNull");
                emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                return 1;
            }
        } catch (Exception e) {
            _logger.error(e.getMessage());
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @param list
     * @param strKey
     * @param strValue
     * @return java.util.Map
     * @throws
     * @description 检查Map集合中 指定key 是否包含指定Value
     * @author CHENYAN
     * @date 2024/7/25 13:45
     */
    public static MapList checkMapValueContainByMapList(List list, String strKey, String strValue) {
        MapList res = new MapList();
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                Map info = (Map) list.get(i);
                if (info.containsKey(strKey)) {
                    if (info.get(strKey).equals(strValue)) {
                        res.add(info);
                    }
                }
            }
        }
        return res;
    }

    /**
     * @param context
     * @param args
     * @return void
     * @throws
     * @description ECR 创建到提交创建流程
     * @author CHENYAN
     * @date 2024/7/29 10:19
     */
    public void createRouteInSubmit(Context context, String[] args) throws Exception {
        _logger.info("------------------------ createRouteInSubmit begin  -----------------------------------");
        String strObjectId = args[0];
        String strCurrent = args[1];
        String strNextState = args[2];
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strCurrent:{}", strCurrent);
        _logger.info("strNextState:{}", strNextState);
        StringList typeSelectList = new StringList(SELECT_ID);
        typeSelectList.add(SELECT_NAME);
        StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
//        MapList maps = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_ECR2PERSON , // relationship pattern
//                TYPE_PERSON,                                    // object pattern
//                typeSelectList,                            // object selects
//                reSelectList, // relationship selects
//                false,                                        // to direction
//                true,                                        // from direction
//                (short) 1,                                    // recursion level
//                "",                // object where clause
//                "",
//                (short) 0);
//        _logger.info("maps:{}",maps);
//        //经理集合
//        StringList managerList = new StringList();
//        //整椅集合
//        StringList devList = new StringList();
//        for (int i = 0; i < maps.size(); i++) {
//            Map person = (Map) maps.get(i);
//            String strRouteRole = (String) person.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFROUTENOTE_APPROVALAROLE);
//            String strPersonId = (String) person.get(SELECT_ID);
//            switch (strRouteRole){
//                case JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_BOTH :{
//                    //既是整椅又是经理
//                    managerList.add(strPersonId);
//                    devList.add(strPersonId);
//                    break;
//                }
//                case JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_MANGER :{
//                    //经理
//                    managerList.add(strPersonId);
//                    break;
//                }
//                case JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_DEV_ENGINEER:{
//                    //整椅
//                    devList.add(strPersonId);
//                    break;
//                }
//            }
//        }
//        if (devList.size() > 0) {
//            for (int i = 0; i < devList.size(); i++) {
//                String strPersonId = devList.get(i);
//                Map devMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPersonId,"ECR提交审核","true","1","All");
//                approveList.add(devMap);
//            }
//        }
        //此处修改为直线经理 mod by chenyan 2024/09/05
        String strLineMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.Review.LineManager");
        String strChairMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.Review.ChairManager");
        MapList approveList = new MapList();
        // id
        String strLinkManager = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, "", context.getUser());
        if (UIUtil.isNotNullAndNotEmpty(strLinkManager)) {
            Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strLinkManager, strLineMess, "true", "1", "All");
            approveList.add(managerMap);
        }
        //获取整椅经理
        String strProjectSpaceId = ecr.getInfo(context, "from[JFChange2Project].to.id");
        MapList chairManager = JF_PublicMethodClass_mxJPO.getProjectPersonByRoleName(context, strProjectSpaceId, "Chair manager", typeSelectList, reSelectList);
        if (chairManager.size() > 0) {
            for (int i = 0; i < chairManager.size(); i++) {
                Map person = (Map) chairManager.get(i);
                String strPersonId = (String) person.get(SELECT_ID);
                Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPersonId, strChairMess, "true", "2", "All");
                approveList.add(managerMap);
            }
        }
//        if (managerList.size() > 0) {
//            for (int i = 0; i < managerList.size(); i++) {
//                String strPersonId = managerList.get(i);
//                Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPersonId,"ECR提交审核","true","2","All");
//                approveList.add(managerMap);
//            }
//        }
        //创建流程
        JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
        String state = "state_Submit";//在哪个状态增加流程
        String policy = "policy_JFECR";//哪个Policy上面
        String routeDescription = "ECR提交审核";//流程描述
        String routeId = jf_route.createAndStartRoute(context, approveList, strObjectId, state, policy, routeDescription);
        _logger.info("------------------------ createRouteInSubmit end  -----------------------------------");
    }

    /**
     * @param context
     * @param args
     * @return boolean
     * @throws
     * @description 获取ECR 编辑Table的各个列的编辑权限
     * @author CHENYAN
     * @date 2024/7/30 13:47
     */
    public boolean getECRFormEditAccess(Context context, String[] args) throws Exception {
        Map formSettingMap = JPO.unpackArgs(args);
        Map fieldMap = (Map) formSettingMap.get("field");
        Map requestMap = (Map) formSettingMap.get("requestMap");
        String strFieldName = (String) fieldMap.get("name");
        String strObjectId = (String) requestMap.get("objectId");
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        StringList typeSelectList = new StringList();
        typeSelectList.add(SELECT_CURRENT);
        typeSelectList.add(SELECT_OWNER);
        typeSelectList.add("from[JFChange2Project].to.id");
        String strLoginUser = context.getUser();
        Map filedMap = ecr.getInfo(context, typeSelectList);
        String strECROwner = (String) filedMap.get(SELECT_OWNER);
        String strCurrent = (String) filedMap.get(SELECT_CURRENT);
        String strProjectId = (String) filedMap.get("from[JFChange2Project].to.id");
        boolean res = false;
        switch (strFieldName) {
            case "JFProjectName", "JFProjectPhase", "Title", "JFECRChangeType",
                    "JFChangeSource", "JFIsPlatformPart", "JFAffectsFactory", "JFChangeEventType",
                    "JFChangesDeveExpensesManHours", "JFChangesTrialExpensesManHours", "JFChangesDeveExpensesManHoursExternal", "JFChangesTrialExpensesManHoursExternal",
                    "JFChangeReson", "Description", "ManagerReview", "WCharDevEngineering", "JFAssociatedOtherProjects": {
                if (strECROwner.equals(strLoginUser) && ("Create".equals(strCurrent))) {
                    res = true;
                }
                break;
            }
            case "JFBreakpointMode", "JFECRType": {
                String strProjectManager = JF_Util_mxJPO.getProjectManager(context, new String[]{strProjectId});
                if (strProjectManager.equals(strLoginUser) && "Review".equals(strCurrent)) {
                    _logger.info("@@@@@@@@@@@@@@@@@@@@@");
                    res = true;
                }
                break;
            }
        }
        return res;
    }

    public String getECRFormFieldValue(Context context, String[] args) throws Exception {
        Map formSettingMap = JPO.unpackArgs(args);
        Map paramMap = (Map) formSettingMap.get("paramMap");
        Map fieldMap = (Map) formSettingMap.get("fieldMap");
        String strFieldName = (String) fieldMap.get("name");
        Map requestMap = (Map) formSettingMap.get("requestMap");
        String strMode = (String) requestMap.get("mode");
        if ("ManagerReview".equals(strFieldName)) {
            String strLoginUser = context.getUser();
            String strValue = (String) requestMap.get("objectId");
            DomainObject ecr = DomainObject.newInstance(context, strValue);
            StringList busSelectList = new StringList();
            busSelectList.add(SELECT_CURRENT);
            busSelectList.add(SELECT_OWNER);
            Map ecrInfo = ecr.getInfo(context, busSelectList);
            String strCurrent = (String) ecrInfo.get(SELECT_CURRENT);
            String strOwner = (String) ecrInfo.get(SELECT_OWNER);
            StringList typeSelectList = new StringList();
            typeSelectList.add(SELECT_NAME);
            typeSelectList.add(SELECT_ID);
            String strRelWhere = JF_PublicMethodClass_mxJPO.buildStringInStrings("(", JF_PLMConstants_mxJPO.SELECT_ATTR_JFROUTENOTE_APPROVALAROLE, "==", JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_MANGER, "||", JF_PLMConstants_mxJPO.SELECT_ATTR_JFROUTENOTE_APPROVALAROLE, "==", JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_BOTH, ")");
            _logger.info("strRelWhere:{}", strRelWhere);
            MapList maps = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_ECR2PERSON, // relationship pattern
                    TYPE_PERSON,                                    // object pattern
                    typeSelectList,                            // object selects
                    new StringList(SELECT_RELATIONSHIP_ID), // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    strRelWhere,
                    (short) 0);
            _logger.info("maps:{}", maps);
            if (maps.size() > 0) {
                Map person = (Map) maps.get(0);
                StringBuffer sb = new StringBuffer();
                String strName = (String) person.get(SELECT_NAME);
                String strPersonId = (String) person.get(SELECT_ID);
                //显示人员全名
                if (UIUtil.isNotNullAndNotEmpty(strName)) {
                    String strFullName = PersonUtil.getFullName(context, strName);
                    if (UIUtil.isNotNullAndNotEmpty(strMode)) {
                        if ("edit".equalsIgnoreCase(strMode)) {
                            if ((!"Create".equals(strCurrent)) || (!strLoginUser.equals(strOwner))) {
                                return strFullName;
                            }
                        } else if ("view".equalsIgnoreCase(strMode)) {
                            return strFullName;
                        }
                    }
                    String strHtml = JF_PublicMethodClass_mxJPO.buildFieldHtml(context, strMode, "ManagerReview", strPersonId, strFullName, "../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=policy_Person.state_Active&selection=single&submitURL=./JF_ECRSearchUtils.jsp&groupName=major_manager&includeOIDprogram=JF_PublicMethodClass:getUserGroupPersons", true);

                    return strHtml;
                }
                return strName;
            } else {
                String strHtml = JF_PublicMethodClass_mxJPO.buildFieldHtml(context, strMode, "ManagerReview", "", "", "../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=policy_Person.state_Active&selection=single&submitURL=./JF_ECRSearchUtils.jsp&groupName=major_manager&includeOIDprogram=JF_PublicMethodClass:getUserGroupPersons", true);
                return strHtml;
            }
        }
        return "";
    }

    /**
     * @param context
     * @param args
     * @return StringList 返回列编辑权限
     * @throws
     * @description 获取cost table 列的编辑权限
     * @author CHENYAN
     * @date 2024/7/29 10:17
     */
    public StringList getJFECRAffectedItemsCostTableEditAccess(Context context, String[] args) throws Exception {
        StringList res = new StringList();
        Map tableSettingMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) tableSettingMap.get("objectList");
        Map columnMap = (Map) tableSettingMap.get("columnMap");
        String strFieldName = (String) columnMap.get("name");
        Map requestMap = (Map) tableSettingMap.get("requestMap");
        String strECRId = (String) requestMap.get("objectId");
        _logger.info("size:{}", res.size());
        MapList roleList = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECR(context, new String[]{strECRId});
        _logger.info("roleList：{}", roleList);
        _logger.info("strFieldName：{}", strFieldName);
        try {
            res = getTableEditAccessInLoginRole(context, objectList, roleList, strECRId, new StringList(), strFieldName);
        } catch (Exception e) {
            e.printStackTrace();
            _logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        _logger.info("res:{}", res);
        return res;
    }

    /**
     * @param context
     * @param args
     * @return StringList 返回列编辑权限
     * @throws
     * @description 获取cost table 列的编辑权限
     * @author CHENYAN
     * @date 2024/7/29 10:17
     */
    public StringList getJFECRAffectedItemsMakeTableEditAccess(Context context, String[] args) throws Exception {
        StringList res = new StringList();
        Map tableSettingMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) tableSettingMap.get("objectList");
        Map columnMap = (Map) tableSettingMap.get("columnMap");
        String strFieldName = (String) columnMap.get("name");
        Map requestMap = (Map) tableSettingMap.get("requestMap");
        String strECRId = (String) requestMap.get("objectId");
        MapList roleList = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECR(context, new String[]{strECRId});
        _logger.info("roleList：{}", roleList);
        _logger.info("strFieldName：{}", strFieldName);
        //add by chenyan 2-25/04/14 新增APR 阶段时财务BP可以编辑整椅卷积价格
        if ("JFChangeWholeSeatPrice".equals(strFieldName) || "JFChangeWholeSeatPriceExternal".equals(strFieldName)) {
            DomainObject ecr = DomainObject.newInstance(context, strECRId);
            String strCurrent = ecr.getInfo(context, SELECT_CURRENT);
            boolean prrAccess = false;
            if ("APR".equals(strCurrent)) {
                for (int i = 0; i < roleList.size(); i++) {
                    Map roleMap = (Map) roleList.get(i);
                    String strRoleName = (String) roleMap.get("role");
                    if (JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_FinancialBP.equals(strRoleName)) {
                        prrAccess = true;
                        break;
                    }
                }
            }
            _logger.info("prrAccess:{}", prrAccess);
            StringList rootPartIdList = ecr.getInfoList(context, "from[JFECR2Manufacturing].to.id");
            _logger.info("strRootPartIdList:{}", rootPartIdList);
            //只有是ECR和整椅关系的整椅零件才能编辑
            for (int i = 0; i < objectList.size(); i++) {
                Boolean isEdit = Boolean.FALSE;
                if (prrAccess) {
                    Map partMap = (Map) objectList.get(i);
                    String strPartId = (String) partMap.get(SELECT_ID);
                    String strPartType = (String) partMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType);
                    if (rootPartIdList.contains(strPartId) && "C".equals(strPartType)) {
                        isEdit = Boolean.TRUE;
                    }
                }
                res.add(isEdit.toString());
            }
        } else {
            res = getMakeTableEditAccessInLoginRole(context, objectList, roleList, strECRId, new StringList(), strFieldName);
        }
        return res;
    }

    public static StringList getMakeTableEditAccessInLoginRole(Context context, MapList objectList, MapList roleList, String strECRId, StringList approveStateList, String strFieldName) throws Exception {
        StringList res = new StringList(objectList.size());
        //角色列表为空全返回false
        DomainObject ecr = DomainObject.newInstance(context, strECRId);
        StringList ecrBusSelectList = new StringList();
        ecrBusSelectList.add(SELECT_CURRENT);
        ecrBusSelectList.add(SELECT_OWNER);
        ecrBusSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
        Map ecrInfo = ecr.getInfo(context, ecrBusSelectList);
        String strCurrent = (String) ecrInfo.get(SELECT_CURRENT);
        String strChangeSource = (String) ecrInfo.get(SELECT_ATTR_JFCHANGESOURCE);
        if (null == roleList || roleList.size() == 0) {
            for (int i = 0; i < objectList.size(); i++) {
                res.add(Boolean.FALSE.toString());
            }
        }
        // 1.ECR的变更来源来控制权限
        //根据一级件进行分组
        //一级件保存集合 key id  value map
        Map oneLevelMap = new HashMap();
        Map<String, String> roleMap = new HashMap<>();
        objectList.stream().forEach(m -> {
            Map infoMap = (Map) m;
            String strRelationship = UIUtil.getValue(infoMap, "relationship");
            String strId = UIUtil.getValue(infoMap, SELECT_ID);
            _logger.info("strRelationship:{}", strRelationship);
            //如果是一级件下面零件需要判断关系属性ECRName是否保存的当前ECR信息 获取表格数据时已经过滤无需再次过滤
            if (JF_PLMConstants_mxJPO.REL_JFECR2Manufacturing.equals(strRelationship) || JF_PLMConstants_mxJPO.RELATIONSHIP_JFRootPart2OnePart.equals(strRelationship)) {
                oneLevelMap.put(strId, m);
            }
        });
        //  oneLevelMap 和 group 的key oneLevelMap 大于等于 group的key ，一个保存的是 一级件Map ，一个保存一级件下面子件
        // 遍历所有一级件
        for (Object entry : oneLevelMap.entrySet()) {
            Map.Entry entryMap = (Map.Entry) entry;
            String strKey = (String) entryMap.getKey();
            //匹配不到给默认值
            if (UIUtil.isNullOrEmpty(strChangeSource)) {
                _logger.info("---------------------------- 授予默认值 -------------------------------");
                strChangeSource = JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH;
            }
            _logger.info("strChangeSource:{} strCurrent:{}", strChangeSource, strCurrent);
            // 编辑权限控制 变更来源 + DirectBuy + 采购类型 + ECR 状态 + 会签任务状态
            boolean isEdit = getFieldEditAccessByChangeSource(strChangeSource, strFieldName);
            _logger.info("isEdit:{}", isEdit);
            //ECR状态下是否可编辑 会签任务是否可编辑 审核中已完成不可编辑
            boolean isEditInCurrent = getFieldIsEditAccessByFieldName_State_Role(strFieldName, strCurrent, roleList);
            _logger.info("isEditInCurrent:{}", isEditInCurrent);
            boolean hasRole = false;
            StringList editRoleList = getEditRoleInFieldName(strFieldName, "", "");
            _logger.info("editRoleList:{}", editRoleList);
            StringList roleNameList = (StringList) roleList.stream().map(m -> {
                Map roleInfo = (Map) m;
                return roleInfo.get("role");
            }).collect(Collectors.toCollection(StringList::new));
            if (editRoleList.size() > 0) {
                for (int i = 0; i < editRoleList.size(); i++) {
                    String strRole = editRoleList.get(i);
                    hasRole = roleNameList.contains(strRole);
                    break;
                }
            }
            if (isEdit && hasRole && isEditInCurrent) {
                // 通过状态判断是否有权限
                roleMap.put(strKey, Boolean.TRUE.toString());
            } else {
                roleMap.put(strKey, Boolean.FALSE.toString());
            }
        }
        _logger.info("roleMap:{}", roleMap);
        // add by chenyan 不同GC下存在同一个一级件 只设置第一个可编辑
        // 已经遍历过的id
        Set<String> forEachIdSet = new HashSet<>();
        for (int i = 0; i < objectList.size(); i++) {
            Map obj = (Map) objectList.get(i);
            String strId = (String) obj.get(SELECT_ID);
            if (forEachIdSet.contains(strId)) {
                res.add(Boolean.FALSE.toString());
                continue;
            }
            String strRelName = (String) obj.get("relationship");
            if (roleMap.containsKey(strId) && (JF_PLMConstants_mxJPO.REL_JFECR2Manufacturing.equals(strRelName) || JF_PLMConstants_mxJPO.RELATIONSHIP_JFRootPart2OnePart.equals(strRelName))) {
                res.add(roleMap.get(strId));
            } else {
                res.add(Boolean.FALSE.toString());
            }
            forEachIdSet.add(strId);
        }
        _logger.info("res:{}", res);
        return res;
    }


    /**
     * @param context
     * @param args
     * @return matrix.util.StringList
     * @throws
     * @description 构造ECR两个Table行数据
     * @author CHENYAN
     * @date 2024/8/8 13:39
     */
    public StringList getAffectedItemsCostTableField(Context context, String[] args) throws Exception {
        _logger.info("------------------------------------------  getAffectedItemsCostTableField  begin ------------------------------------------");
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map paramList = (Map) paramsMap.get("paramList");
            String strTableName = (String) paramList.get("selectedTable");
            String strParentOID = (String) paramList.get("parentOID");
            Map columnMap = (Map) paramsMap.get("columnMap");
            String strColName = (String) columnMap.get("name");
            _logger.info("strColName:{}", strColName);
            try {
                ContextUtil.pushContext(context);
                switch (strColName) {
                    case "JFChangeUnitPrice", "JFChangeMold",
                            "JFChangeUnitPriceExternal", "JFChangeMoldCostExternal",
                            "JFChangeUnitPriceCost", "JFChangeMoldCost",
                            "JFChangeUnitPriceCostExternal", "JFChangeMoldPriceCostExternal",
                            "JFStagnationOfSuppliersInternal", "JFStagnationOfSuppliersExternal",
                            "JFChangesTrialExpensesManHours", "JFChangesTrialExpensesManHoursExternal": {
                        StringList tableIdList = _getListOfKeys(args, SELECT_ID);
                        StringList res = new StringList(tableIdList.size());
                        for (int i = 0; i < tableIdList.size(); i++) {
                            String strObjectId = tableIdList.get(i);
                            DomainObject part = DomainObject.newInstance(context, strObjectId);
                            StringList relSelectList = new StringList(SELECT_RELATIONSHIP_ID);
                            String strFieldName = JF_PublicMethodClass_mxJPO.buildStringInStrings("attribute[", strColName, "]");
                            relSelectList.add(strFieldName);
                            MapList maps = part.getRelatedObjects(context, "JFECR2PartPrice", // relationship pattern
                                    TYPE_JFECR,                                    // object pattern
                                    new StringList(SELECT_ID),                            // object selects
                                    relSelectList, // relationship selects
                                    true,                                        // to direction
                                    false,                                        // from direction
                                    (short) 1,                                    // recursion level
                                    JF_PublicMethodClass_mxJPO.buildStringInStrings("id==", strParentOID),                // object where clause
                                    "",
                                    (short) 0);
                            if (maps.size() > 0) {
                                Map info = (Map) maps.get(0);
                                res.add(UIUtil.getValue(info, strFieldName));
                            } else {
                                res.add("");
                            }
                        }
                        return res;
                    }
                    case "JFChangeMan-hour", "JFChangeMan-hourExternal",
                            "JFChangeSeatCost", "JFChangeSeatCostExternal",
                            "JFChangeTargetPrice", "JFChangeTargetPriceExternal",
                            "JFChangeWholeSeatPrice", "JFChangeWholeSeatPriceExternal": {
                        StringList tableIdList = _getListOfKeys(args, SELECT_ID);
                        StringList res = new StringList(tableIdList.size());
                        for (int i = 0; i < tableIdList.size(); i++) {
                            String strObjectId = tableIdList.get(i);
                            DomainObject part = DomainObject.newInstance(context, strObjectId);
                            StringList relSelectList = new StringList(SELECT_RELATIONSHIP_ID);
                            String strFieldName = JF_PublicMethodClass_mxJPO.buildStringInStrings("attribute[", strColName, "]");
                            relSelectList.add(strFieldName);
                            MapList maps = part.getRelatedObjects(context, "JFECR2MakePartPrice", // relationship pattern
                                    TYPE_JFECR,                                    // object pattern
                                    new StringList(SELECT_ID),                            // object selects
                                    relSelectList, // relationship selects
                                    true,                                        // to direction
                                    false,                                        // from direction
                                    (short) 1,                                    // recursion level
                                    JF_PublicMethodClass_mxJPO.buildStringInStrings("id==", strParentOID),                // object where clause
                                    "",
                                    (short) 0);
                            if (maps.size() > 0) {
                                Map info = (Map) maps.get(0);
                                res.add(UIUtil.getValue(info, strFieldName));
                            } else {
                                res.add("");
                            }
                        }
                        return res;
                    }
                    case "JFChangeResource": {
                        StringList tableIdList = _getListOfKeys(args, SELECT_ID);
                        StringList res = new StringList(tableIdList.size());
//                        if ("JFECRCosting".equals(strTableName)) {
                        // add by chenyan 2025/03/03 修复用户自定义表格报错
                        if (strTableName.contains("JFECRCosting")) {
                            for (int i = 0; i < tableIdList.size(); i++) {
                                String strPartId = tableIdList.get(i);
                                DomainObject part = DomainObject.newInstance(context, strPartId);
                                StringList typeSelectList = new StringList(SELECT_ID);
                                StringList relSelectList = new StringList(SELECT_RELATIONSHIP_ID);
                                relSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
                                MapList maps = part.getRelatedObjects(context, "JFRelateItem", // relationship pattern
                                        TYPE_JFECR,                                    // object pattern
                                        typeSelectList,                            // object selects
                                        relSelectList, // relationship selects
                                        true,                                        // to direction
                                        false,                                        // from direction
                                        (short) 1,                                    // recursion level
                                        JF_PublicMethodClass_mxJPO.buildStringInStrings("id==", strParentOID),                // object where clause
                                        "",
                                        (short) 0);
                                String strChangeSource = "";
                                if (maps.size() > 0) {
                                    Map info = (Map) maps.get(0);
                                    strChangeSource = UIUtil.getValue(info, SELECT_ATTR_JFCHANGESOURCE);
                                } else {
                                    DomainObject ecr = DomainObject.newInstance(context, strParentOID);
                                    strChangeSource = ecr.getInfo(context, SELECT_ATTR_JFCHANGESOURCE);
                                    _logger.info("strChangeSource:{}", strChangeSource);
                                }
                                if (UIUtil.isNotNullAndNotEmpty(strChangeSource)) {
                                    StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, ATTR_JFCHANGESOURCE, new StringList(strChangeSource), context.getLocale().toString());
                                    if (nlsRanges.size() > 0) {
                                        res.add(nlsRanges.get(0));
                                    } else {
                                        res.add("");
                                    }
                                } else {
                                    res.add("");
                                }
                            }
                        } else {
                            DomainObject ecr = DomainObject.newInstance(context, strParentOID);
                            String strChangeSource = ecr.getInfo(context, SELECT_ATTR_JFCHANGESOURCE);
                            _logger.info("strChangeSource:{}", strChangeSource);
                            StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, ATTR_JFCHANGESOURCE, new StringList(strChangeSource), context.getLocale().toString());
                            for (int i = 0; i < tableIdList.size(); i++) {
                                if (nlsRanges.size() > 0) {
                                    res.add(nlsRanges.get(0));
                                } else {
                                    res.add("");
                                }
                            }
                        }
                        return res;
                    }
                    case "id": {
                        StringList tableIdList = _getListOfKeys(args, SELECT_ID);
                        return tableIdList;
                    }
                    case "id[connection]": {
                        StringList tableIdList = _getListOfKeys(args, SELECT_ID);
                        StringList res = new StringList(tableIdList.size());
                        String strRelType = "";
                        if ("JFECRCosting".equals(strTableName)) {
                            strRelType = "JFECR2PartPrice";
                        } else if ("JFECRController".equals(strTableName)) {
                            strRelType = "JFECR2MakePartPrice";
                        }
                        for (int i = 0; i < tableIdList.size(); i++) {
                            String strObjectId = tableIdList.get(i);
                            DomainObject part = DomainObject.newInstance(context, strObjectId);
                            StringList relSelectList = new StringList(SELECT_RELATIONSHIP_ID);
                            MapList maps = part.getRelatedObjects(context, strRelType, // relationship pattern
                                    TYPE_JFECR,                                    // object pattern
                                    new StringList(SELECT_ID),                            // object selects
                                    relSelectList, // relationship selects
                                    true,                                        // to direction
                                    false,                                        // from direction
                                    (short) 1,                                    // recursion level
                                    JF_PublicMethodClass_mxJPO.buildStringInStrings("id==", strParentOID),                // object where clause
                                    "",
                                    (short) 0);
                            //实际只会关联一个
                            if (maps != null & maps.size() > 0) {
                                Map relMap = (Map) maps.get(0);
                                String strRelId = (String) relMap.get(SELECT_RELATIONSHIP_ID);
                                res.add(strRelId);
                            } else {
                                res.add("");
                            }
                        }
                        return res;
                    }
                    case "revision": {
                        StringList tableRevisionList = _getListOfKeys(args, SELECT_REVISION);
                        return tableRevisionList;
                    }
                    case "level": {
                        StringList tableRevisionList = _getListOfKeys(args, SELECT_LEVEL);
                        return tableRevisionList;
                    }
                    case "PartNumber", "PartType", "PartNameCN", "PartNameEN", "ProcurementType", "DirectBuy": {
                        String strColSelectName = (String) columnMap.get("selectName");
                        String strAttributeName = (String) columnMap.get("attributeName");
                        StringList tableSelectList = _getListOfKeys(args, strColSelectName);
                        if (strColName.equals("PartType") || strColName.equals("ProcurementType")) {
                            for (int i = 0; i < tableSelectList.size(); i++) {
                                String strFieldValue = tableSelectList.get(i);
                                StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, strAttributeName, new StringList(strFieldValue), context.getLocale().toString());
                                _logger.info("nlsRanges:{}", nlsRanges);
                                tableSelectList.set(i, nlsRanges.get(0));
                            }
                        }
                        return tableSelectList;
                    }
                    case "OnePartQuantity": {
                        MapList tableDataList = (MapList) paramsMap.get("objectList");
                        StringList tableSelectList = new StringList(tableDataList.size());
                        for (int i = 0; i < tableDataList.size(); i++) {
                            Map rowData = (Map) tableDataList.get(i);
                            String strType = (String) rowData.get(SELECT_TYPE);
                            //标识零级件
                            String strHasZeroPart = (String) rowData.get("to[JFECR2Manufacturing]");
                            String strHasOnePart = (String) rowData.get("to[JFRootPart2OnePart]");
                            if (TYPE_VPMREFERENCE.equals(strType) && ("TRUE".equalsIgnoreCase(strHasZeroPart) || "TRUE".equalsIgnoreCase(strHasOnePart))) {
                                tableSelectList.add("1");
                            } else {
                                tableSelectList.add("");
                            }
                        }
                        return tableSelectList;
                    }
                }
            } finally {
                ContextUtil.popContext(context);
            }
            StringList emptyList = new StringList(_getListOfKeys(args, SELECT_ID).size());
            return emptyList;
        } catch (Exception e) {
            _logger.info(e.getMessage());
            throw e;
        }
    }

    /**
     * @param context
     * @param args
     * @return void
     * @throws
     * @description 修改ECRTable中属性方法
     * @author CHENYAN
     * @date 2024/8/8 13:40
     */

    public void updateAffectedItemsCostTableField(Context context, String[] args) throws Exception {
        _logger.info("------------------------------------------  updateAffectedItemsCostTableField  begin ------------------------------------------");
        Map paramsMap = (Map) JPO.unpackArgs(args);
        _logger.info("paramsMap:{}", paramsMap);
        Map columnMap = (Map) paramsMap.get(JF_PLMConstants_mxJPO.STRING_COLUMNMAP);
        Map requestMap = (Map) paramsMap.get("requestMap");
        String strTableName = (String) requestMap.get("selectedTable");
        String strParentOID = (String) requestMap.get("parentOID");
        String strAttrName = (String) columnMap.get(DomainConstants.SELECT_NAME);
        HashMap paramMap = (HashMap) paramsMap.get(JF_PLMConstants_mxJPO.STRING_PARAMMAP);
        String strObjectId = (String) paramMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
        String strNewValue = (String) paramMap.get(STRING_NEW_VALUE);
        _logger.info("strTableName:{}", strTableName);
        _logger.info("strParentOID:{}", strParentOID);
        _logger.info("strAttrName:{}", strAttrName);
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strNewValue:{}", strNewValue);
        String strBusWhere = "";
        String strToId = "";
        String strRelName = "";
        strBusWhere = JF_PublicMethodClass_mxJPO.buildStringInStrings("id==", strParentOID);
        //根据Table获取不同的关系
       /* if ("JFECRCosting".equals(strTableName)) {
            strRelName = "JFECR2PartPrice";
        } else if ("JFECRController".equals(strTableName)) {
            strRelName = "JFECR2MakePartPrice";
        }*/
        //根据Table获取不同的关系 修复Bug 如果自定义了table有增加自定义的名称
        if (strTableName.contains("JFECRCosting")) {
            strRelName = "JFECR2PartPrice";
        } else if (strTableName.contains("JFECRController")) {
            strRelName = "JFECR2MakePartPrice";
        }
        try {
            ContextUtil.pushContext(context);
            DomainObject part = DomainObject.newInstance(context, strObjectId);
            StringList typeSelectList = new StringList();
            _logger.info("strRelName:{}", strRelName);
            MapList maps = part.getRelatedObjects(context, strRelName, // relationship pattern
                    TYPE_JFECR,                                    // object pattern
                    typeSelectList,                            // object selects
                    new StringList(SELECT_RELATIONSHIP_ID), // relationship selects
                    true,                                        // to direction
                    false,                                        // from direction
                    (short) 1,                                    // recursion level
                    JF_PublicMethodClass_mxJPO.buildStringInStrings("id==", strParentOID),                // object where clause
                    "",
                    (short) 0);
            //判断零件和价格关系是否已经创建 如果新建则修改关系属性 ，如果没有需要新建
            _logger.info("maps:{}", maps);
            try {
                if (maps.size() > 0) {
                    Map ecrInfo = (Map) maps.get(0);
                    String strRelId = (String) ecrInfo.get(SELECT_RELATIONSHIP_ID);
                    ContextUtil.startTransaction(context, true);
                    DomainRelationship rel = DomainRelationship.newInstance(context, strRelId);
                    rel.setAttributeValue(context, strAttrName, strNewValue);
                    ContextUtil.commitTransaction(context);
                } else {
                    ContextUtil.startTransaction(context, true);
                    DomainRelationship rel = DomainRelationship.connect(context, DomainObject.newInstance(context, strParentOID), strRelName, part);
                    rel.setAttributeValue(context, strAttrName, strNewValue);
                    ContextUtil.commitTransaction(context);
                }
            } catch (Exception e) {
                ContextUtil.abortTransaction(context);
                _logger.error(e.getMessage());
                throw e;
            }

        } finally {
            ContextUtil.popContext(context);
        }
        return;
    }

    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取ECR 采购件 Table数据
     * @author CHENYAN
     * @date 2024/7/29 10:57
     */
    public MapList getECRBuyTableData(Context context, String[] args) throws Exception {
        _logger.info("----------------------------- getECRBuyTableData begin ------------------------------------------------");
        Map parameters = JPO.unpackArgs(args);
        String strObjectId = (String) parameters.get("objectId");
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        String strName = ecr.getInfo(context, SELECT_NAME);
        StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        StringList typeSelectList = new StringList(SELECT_ID);
        typeSelectList.add(SELECT_JFECR2PartPriceID);
        reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFECRName);
        MapList maps = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_JFECR2OnelevelPart + "," + JF_PLMConstants_mxJPO.REL_JFOneLevelPart2Part, // relationship pattern
                TYPE_VPMREFERENCE,                                    // object pattern
                typeSelectList,                            // object selects
                reSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 2,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        _logger.info("maps.size:{}", maps.size());
        MapList res = (MapList) maps.stream().filter(m -> {
            Map infoMap = (Map) m;
            String strRelationship = UIUtil.getValue(infoMap, "relationship");
            _logger.info("strRelationship:{}", strRelationship);
            //如果是一级件下面零件需要判断关系属性ECRName是否保存的当前ECR信息
            if (JF_PLMConstants_mxJPO.REL_JFOneLevelPart2Part.equals(strRelationship)) {
                String strECRName = UIUtil.getValue(infoMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFECRName);
                return strName.equals(strECRName);
            }
            return true;
        }).collect(Collectors.toCollection(MapList::new));
        _logger.info("----------------------------- getECRBuyTableData end ------------------------------------------------");
        return res;
    }


    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取ECR Cost Table数据
     * @author CHENYAN
     * @date 2024/7/29 10:57
     */
    public MapList getECRCostingTableData(Context context, String[] args) throws Exception {
        _logger.info("----------------------------- getECRCostingTableData begin ------------------------------------------------");
        Map parameters = JPO.unpackArgs(args);
        _logger.info("parameters:{}", parameters);
        String strObjectId = (String) parameters.get("objectId");
        _logger.info("strObjectId:{}", strObjectId);
        String strExpandLevel = (String) parameters.get("expandLevel");
        short nExpandLevel = ProgramCentralUtil.getExpandLevel(strExpandLevel);
        _logger.info("strExpandLevel:{}", strExpandLevel);
        _logger.info("nExpandLevel:{}", nExpandLevel);
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        String strName = ecr.getInfo(context, SELECT_NAME);
        StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        StringList typeSelectList = new StringList(SELECT_ID);
        typeSelectList.add(SELECT_JFECR2PartPriceID);
        typeSelectList.add(SELECT_REVISION);
        typeSelectList.add("to[JFRelateItem|from.id=='" + strObjectId + "'].attribute[JFChangeSource].value");
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFECRName);
        reSelectList.add(SELECT_FROM_ID);
        reSelectList.add(SELECT_RELATIONSHIP_ID);
        reSelectList.add(JF_PublicMethodClass_mxJPO.buildStringInStrings("from.", JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType));
        MapList maps = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_JFECR2OnelevelPart + "," + JF_PLMConstants_mxJPO.REL_JFOneLevelPart2Part, // relationship pattern
                TYPE_VPMREFERENCE,                                    // object pattern
                typeSelectList,                            // object selects
                reSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 2,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        _logger.info("maps.size:{}", maps.size());
        MapList res = (MapList) maps.stream().filter(m -> {
            Map infoMap = (Map) m;
            String strRelationship = UIUtil.getValue(infoMap, "relationship");
            _logger.info("strRelationship:{}", strRelationship);
            //如果是一级件下面零件需要判断关系属性ECRName是否保存的当前ECR信息
            if (JF_PLMConstants_mxJPO.REL_JFOneLevelPart2Part.equals(strRelationship)) {
                String strECRName = UIUtil.getValue(infoMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFECRName);
                return strName.equals(strECRName);
            }
            return true;
        }).collect(Collectors.toCollection(MapList::new));
        _logger.info("res.size:{}", res.size());
        _logger.info("----------------------------- getECRCostingTableData end ------------------------------------------------");
        return res;
    }

    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取制造件 Table数据
     * @author CHENYAN
     * @date 2024/8/8 11:52
     */

    public MapList getECRMakeTableData(Context context, String[] args) throws Exception {
        _logger.info("----------------------------- getECRCostingTableData begin ------------------------------------------------");
        Map parameters = JPO.unpackArgs(args);
        _logger.info("parameters:{}", parameters);
        String strObjectId = (String) parameters.get("objectId");
        _logger.info("strObjectId:{}", strObjectId);
        String strExpandLevel = (String) parameters.get("expandLevel");
        short nExpandLevel = ProgramCentralUtil.getExpandLevel(strExpandLevel);
        _logger.info("strExpandLevel:{}", strExpandLevel);
        _logger.info("nExpandLevel:{}", nExpandLevel);
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        String strName = ecr.getInfo(context, SELECT_NAME);
        StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        StringList typeSelectList = new StringList(SELECT_ID);
        typeSelectList.add(SELECT_TYPE);
        typeSelectList.add(SELECT_REVISION);
        typeSelectList.add(SELECT_CURRENT);
        typeSelectList.add("to[JFECR2Manufacturing]");
        typeSelectList.add("to[JFRootPart2OnePart]");
        typeSelectList.add(SELECT_JFECR2PartPriceID);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
        reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFECRName);
        reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFWholeChair);
        reSelectList.add(SELECT_FROM_ID);
        reSelectList.add(SELECT_RELATIONSHIP_ID);
        MapList maps = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_JFECR2Manufacturing + "," + JF_PLMConstants_mxJPO.REL_JFOneLevelPart2Part + "," + JF_PLMConstants_mxJPO.RELATIONSHIP_JFRootPart2OnePart, // relationship pattern
                TYPE_VPMREFERENCE,                                    // object pattern
                typeSelectList,                            // object selects
                reSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 2,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        _logger.info("maps.size:{}", maps.size());
        MapList res = (MapList) maps.stream().filter(m -> {
            Map infoMap = (Map) m;
            String strRelationship = UIUtil.getValue(infoMap, "relationship");
            _logger.info("strRelationship:{}", strRelationship);
            //如果是一级件下面零件需要判断关系属性ECRName是否保存的当前ECR信息
            if (JF_PLMConstants_mxJPO.REL_JFOneLevelPart2Part.equals(strRelationship) || JF_PLMConstants_mxJPO.RELATIONSHIP_JFRootPart2OnePart.equals(strRelationship)) {
                String strECRName = UIUtil.getValue(infoMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFECRName);
                return strName.equals(strECRName);
            }
            return true;
        }).collect(Collectors.toCollection(MapList::new));
        _logger.info("----------------------------- getECRCostingTableData end ------------------------------------------------");
        return res;
    }

    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取ECR Table中的expand数据
     * @author CHENYAN
     * @date 2024/8/6 10:21
     */

    public MapList getExpandData(Context context, String[] args) throws Exception {
        Map parameters = JPO.unpackArgs(args);
        _logger.info("parameters:{}", parameters);
        return new MapList();
    }

    /**
     * @param context
     * @param args
     * @return int
     * @throws
     * @description 校验审核任务维护信息，并在DA延期流程最后一个任务完成时累计延期次数
     * @author CHENYAN
     * @date 2024/7/29 17:45
     */
    @ProgramCallable
    public int checkECRInboxTaskMaintenanceInformation(Context context, String[] args) throws Exception {
        //拿取ECR所属的project
        Set<String> projectSet = new HashSet<>();
        //是否是ECR 状态Review 的项目经理审核任务
        boolean isECRReviewRoute = false;
        boolean isPop = false;
        String strPolicy = "";
        String strEcrId = "";
        try {
            String strLoginUser = context.getUser();
            _logger.info("strLoginUser：{}", strLoginUser);
            ContextUtil.pushContext(context);
            String strObjectId = args[0];
            _logger.info("strObjectId:{}", strObjectId);
            DomainObject inBoxTask = DomainObject.newInstance(context, strObjectId);
            //20260805 update by ljr 修正Inbox Task获取所属Route的关系方向
            String strRouteId = inBoxTask.getInfo(context, "from[Route Task].to.id");
            // add by chenyan 修复当审核任务驳回时也校验了必填属性 驳回时不需要校验
            String strApproveState = inBoxTask.getInfo(context, "attribute[Approval Status].value");
            String Title = inBoxTask.getInfo(context, "attribute[Title].value");
            _logger.info("strApproveState：{}", strApproveState);
            if ("Reject".equals(strApproveState) || "None".equalsIgnoreCase(strApproveState)) {
                return 0;
            }
            StringList typeSelectList = new StringList();
            typeSelectList.add(SELECT_ID);
            typeSelectList.add(SELECT_TYPE);
            typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
            typeSelectList.add(SELECT_ATTR_JFECRTYPE);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFBREAKPOINTMODE);
            typeSelectList.add("attribute[JFDAIsDelay]");
            typeSelectList.add("attribute[JFDADelayCount]");
            typeSelectList.add("from[JFChange2Project].to.id");
            typeSelectList.add("from[JFChange2Project].to.owner");
            StringList reSelectList = new StringList();
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_ROUTE_BASE_POLICY);
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_ROUTE_BASE_STATE);
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_ROUTE_BASE_PURPOSE);
            MapList maps = inBoxTask.getRelatedObjects(context, RELATIONSHIP_ROUTE_TASK + "," + RELATIONSHIP_OBJECT_ROUTE, // relationship pattern
                    TYPE_ROUTE + "," + TYPE_JFECR + "," + TYPE_JFNewECR + "," + JF_PLMConstants_mxJPO.TYPE_JFFormalECR+ "," + JF_PLMConstants_mxJPO.TYPE_JFPartList + "," + TYPE_DOCUMENT + "," + JF_PLMConstants_mxJPO.Type_JF_PCR + ",JFDA",                                    // object pattern
                    typeSelectList,                            // object selects
                    reSelectList, // relationship selects
                    true,                                        // to direction
                    true,                                        // from direction
                    (short) 2,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0,
                    false, //checkHidden
                    true, //preventDuplicates
                    (short) 0, //pageSize
                    null,
                    null,
                    null,
                    "end"); //end(返回叶子节点)
            _logger.info("maps:{}", maps);
            ContextUtil.popContext(context);
            isPop = true;
            Map groupMap = (Map) maps.stream().collect(Collectors.groupingBy(m -> {
                Map info = (Map) m;
                return info.get("relationship");
            }));
            _logger.info("groupMap:{}", groupMap);
            if (groupMap.containsKey(RELATIONSHIP_OBJECT_ROUTE)) {
                List ecrList = (List) groupMap.get(RELATIONSHIP_OBJECT_ROUTE);
                //理应只绑定一个ECR
                if (null != ecrList && ecrList.size() > 0) {
                    for (int i = 0; i < ecrList.size(); i++) {
                        Map ecrInfo = (Map) ecrList.get(i);
                        //  "end"); //end(返回叶子节点) 使用这个时候关系属性被OOTB拼接了end
                        String strRouteBindPolicy = (String) ecrInfo.get(JF_PublicMethodClass_mxJPO.buildStringInStrings("end", JF_PLMConstants_mxJPO.SELECT_ATTR_ROUTE_BASE_POLICY));
                        strPolicy = strRouteBindPolicy;
                        strEcrId = (String) ecrInfo.get(SELECT_ID);
                        String strRouteBindState = (String) ecrInfo.get(JF_PublicMethodClass_mxJPO.buildStringInStrings("end", JF_PLMConstants_mxJPO.SELECT_ATTR_ROUTE_BASE_STATE));
                        _logger.info("strRouteBindPolicy:{}", strRouteBindPolicy);
                        _logger.info("strRouteBindState:{}", strRouteBindState);
                        //ECRSubmit创建的流程
                        // add by chenyan 2025/05/08 新增新类型ECR校验
                        if (("policy_JFECR".equals(strRouteBindPolicy) || "policy_JFNewECR".equals(strRouteBindPolicy)) && "state_Review".equals(strRouteBindState)) {
                            //校验ECR中项目经理维护信息必填
                            String projectSpaceId = UIUtil.getValue(ecrInfo, "from[JFChange2Project].to.id");
                            //项目经理通过公共方法去获取 2025/04/01
                            String strProjectSpaceOwner = JF_Util_mxJPO.getProjectManager(context, new String[]{projectSpaceId});
                            //                            String strProjectSpaceOwner = UIUtil.getValue(ecrInfo, "from[JFChange2Project].to.owner");
                            if (strLoginUser.equals(strProjectSpaceOwner)) {
                                isECRReviewRoute = true;
                                _logger.info("isECRReviewRoute:{}", isECRReviewRoute);
                                projectSet.add(projectSpaceId);
                                // add by chenyan 2024/09/02 新增校验ECR中项目必须关联整椅
                                DomainObject project = DomainObject.newInstance(context, projectSpaceId);
                                String strIsConnRootPart = project.getInfo(context, "from[JFProject2RootPart]");
                                if (UIUtil.isNullOrEmpty(strIsConnRootPart) || (!"TRUE".equalsIgnoreCase(strIsConnRootPart))) {
                                    String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.ProjectIsNotConnRootPart");
                                    emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                                    _logger.info("isECRReviewRoute:{}", isECRReviewRoute);
                                    return 1;
                                }
                                String strJFECRType = (String) ecrInfo.get(SELECT_ATTR_JFECRTYPE);
                                String strJFECRBreakPoint = (String) ecrInfo.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFBREAKPOINTMODE);
                                if (UIUtil.isNullOrEmpty(strJFECRType) || UIUtil.isNullOrEmpty(strJFECRBreakPoint)) {
                                    String strTitle = (String) ecrInfo.get(SELECT_ATTRIBUTE_TITLE);
                                    String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.ManagerFiledIsNull");
                                    strMess = strMess.replace("{}", strTitle);
                                    emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                                    _logger.info("isECRReviewRoute:{}", isECRReviewRoute);
                                    return 1;
                                }
                            }
                        } else if ("policy_JFDA".equals(strRouteBindPolicy)
                                && "state_Implement".equals(strRouteBindState)
                                && "JFDA".equals(UIUtil.getValue(ecrInfo, SELECT_TYPE))
                                && "TRUE".equalsIgnoreCase(UIUtil.getValue(ecrInfo, "attribute[JFDAIsDelay]"))
                                && isFinalDAExtensionTask(context, strRouteId, strObjectId)) {
                            //20260804 update by ljr 延期流程最后一个审批任务完成时只累计一次延期次数
                            String delayCountValue = UIUtil.getValue(ecrInfo, "attribute[JFDADelayCount]");
                            int delayCount = UIUtil.isNullOrEmpty(delayCountValue)
                                    ? 0 : Integer.parseInt(delayCountValue);
                            if (delayCount < 2) {
                                boolean delayCountPush = false;
                                try {
                                    ContextUtil.pushContext(context);
                                    delayCountPush = true;
                                    DomainObject.newInstance(context, UIUtil.getValue(ecrInfo, SELECT_ID))
                                            .setAttributeValue(context, "JFDADelayCount",
                                                    String.valueOf(delayCount + 1));
                                } finally {
                                    if (delayCountPush) {
                                        ContextUtil.popContext(context);
                                    }
                                }
                            }
                        } else if ("policy_JFPartList".equals(strPolicy) && "state_Review".equals(strRouteBindState)) {
                            String strPartListId = (String) ecrInfo.get(SELECT_ID);
                            return JF_PartList_mxJPO.checkPartListPartRequireByPartListId(context, strPartListId, strLoginUser);
                        } else if ("policy_Document".equals(strPolicy) && "state_FROZEN".equals(strRouteBindState)) {//完成文档的时候，校验回执确认必填
                            //如果文档的Title是回执确认，就需要填写文档回执
                            String titleZh = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("zh_CN"), "emxComponents.DOCManger.ROUTE.DocReceiptConfirmation");
                            String titleen = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("en"), "emxComponents.DOCManger.ROUTE.DocReceiptConfirmation");
                            String ReceiptSpecialisten = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("en"), "emxComponents.DOCManger.ROUTE.ReceiptSpecialist");
                            String ReceiptSpecialistzh_CN = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("zh_CN"), "emxComponents.DOCManger.ROUTE.ReceiptSpecialist");
                            _logger.info("titleZh:{} titleCn:{} Title：{}", titleZh, titleen, Title);
                            if (titleen.equals(Title) || titleZh.equals(Title)) {
                                //判断改文档是否有文档回执
                                String docId = (String) ecrInfo.get(SELECT_ID);
                                //没有上传回执文件
                                if (!JF_DocumentTrigger_mxJPO.docHasReceiptConfirmation(context, docId, JF_PLMConstants_mxJPO.select_attr_JF_DocReceiptConfirmation)) {
                                    String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.Doc.DocReceiptConfirmation");
                                    emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                                    return 1;
                                }
                            }
                            if (ReceiptSpecialisten.equals(Title) || ReceiptSpecialistzh_CN.equals(Title)) {
                                //判断改文档是否有专家文档回执
                                String docId = (String) ecrInfo.get(SELECT_ID);
                                //没有上传回执文件
                                if (!JF_DocumentTrigger_mxJPO.docHasReceiptConfirmation(context, docId, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_DocSpecialistReceipt)) {
                                    String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.Doc.DocReceiptConfirmation");
                                    emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                                    return 1;
                                }
                            }

                        } else if ("policy_JF_PCR".equals(strPolicy) && "state_Verification".equals(strRouteBindState)) {
                            //校验在验证状态，PM审批的时候必须输入断点切换日期
                            String pmTitle_zh = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("zh_CN"), "emxComponents.DOCManger.ROUTE.ProjectManager");
                            String pmTitle_en = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("en"), "emxComponents.DOCManger.ROUTE.ProjectManager");
                            String lunchTitle_en = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("en"), "emxComponents.PCR.LaunchRouteDescription");
                            String lunchTitle_zn = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("zh_CN"), "emxComponents.PCR.LaunchRouteDescription");
                            //需要增加兼容 caipan 20260618  兼容旧的和新的任务设置,旧的只可以写死了
                          String pmTitle_zh_Old="SDT-\u9879\u76EE\u7ECF\u7406\u5BA1\u6279";
                          String pmTitle_en_Old="SDT-Project Manager Approval";
                          String lunchTitle_zn_Old="SDT-Launch\u7ECF\u7406\u5BA1\u6279";
                          String lunchTitle_en_Old="SDT-Launch Manager Review";
                            _logger.info("titleZh:{} titleCn:{} Title：{}", pmTitle_zh, pmTitle_en, Title);
                            String docId = (String) ecrInfo.get(SELECT_ID);
                            DomainObject pcrObj = DomainObject.newInstance(context,docId);
                            String phase = pcrObj.getAttributeValue(context,JF_PLMConstants_mxJPO.ATTR_JF_PCRProjectPhase);
                            if("Phase2".equals(phase)||"Phase3".equals(phase)) {
                                if (pmTitle_zh.equals(Title) || pmTitle_en.equals(Title)||pmTitle_zh_Old.equals(Title) || pmTitle_en_Old.equals(Title)) {
                                    //PCRID
                                    //PM审批的时候必须输入断点切换日期
                                    if (!JF_PCR_mxJPO.checkAttrHasValue(context, docId, JF_PLMConstants_mxJPO.attr_JF_BreakpointSwitchingDate)) {
                                        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PCR.ReviewMessage");
                                        emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                                        return 1;
                                    }
                                }
                            }else  if("Phase4".equals(phase)) {
                                if (lunchTitle_en.equals(Title) || lunchTitle_zn.equals(Title) || lunchTitle_en_Old.equals(Title) || lunchTitle_zn_Old.equals(Title)) {
                                    //PCRID
                                    //lunch审批的时候必须输入断点切换日期
                                    if (!JF_PCR_mxJPO.checkAttrHasValue(context, docId, JF_PLMConstants_mxJPO.attr_JF_BreakpointSwitchingDate)) {
                                        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PCR.ReviewMessage");
                                        emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                                        return 1;
                                    }
                                }
                            }
                        } else if ("policy_JF_PCR".equals(strPolicy) && "state_IN_Evaluation".equals(strRouteBindState)) {
                            //校验在评估流程的时候，评估结论必填
                            //PCRID
                            //校验在验证状态，PM审批的时候必须输入断点切换日期
                            String pmTitle_zh = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("zh_CN"), "emxComponents.DOCManger.ROUTE.ProjectManager");
                            String pmTitle_en = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("en"), "emxComponents.DOCManger.ROUTE.ProjectManager");
                            String lunchTitle_en = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("en"), "emxComponents.PCR.LaunchRouteDescription");
                            String lunchTitle_zn = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("zh_CN"), "emxComponents.PCR.LaunchRouteDescription");
                            _logger.info("titleZh:{} titleCn:{} Title：{}", pmTitle_zh, pmTitle_en, Title);
                            String docId = (String) ecrInfo.get(SELECT_ID);
                            //需要增加兼容 caipan 20260618  兼容旧的和新的任务设置,旧的只可以写死了
                            String pmTitle_zh_Old="SDT-\u9879\u76EE\u7ECF\u7406\u5BA1\u6279";
                            String pmTitle_en_Old="SDT-Project Manager Approval";
                            String lunchTitle_zn_Old="SDT-Launch\u7ECF\u7406\u5BA1\u6279";
                            String lunchTitle_en_Old="SDT-Launch Manager Review";
                            //PM审批的时候，评估意见
                            //获取任务Title
                            if (pmTitle_zh.equals(Title) || pmTitle_en.equals(Title)||pmTitle_zh_Old.equals(Title) || pmTitle_en_Old.equals(Title)) {
                            if (!JF_PCR_mxJPO.checkAttrHasValue(context, docId, JF_PLMConstants_mxJPO.Attr_JF_EvaluateMessage)) {
                                String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PCR.IN_EvaluationJF_EvaluateMessage");
                                emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                                return 1;
                            }
                            }
                            //Lunch审批的时候，评估结论必填
                            if (lunchTitle_en.equals(Title) || lunchTitle_zn.equals(Title) || lunchTitle_en_Old.equals(Title) || lunchTitle_zn_Old.equals(Title)) {
                                if (!JF_PCR_mxJPO.checkAttrHasValue(context, docId, JF_PLMConstants_mxJPO.ATTR_JF_EvaluationConclusion)) {
                                    String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PCR.IN_EvaluationMessage");
                                    emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
                                    return 1;
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            if (!isPop) {
                ContextUtil.popContext(context);
            }
        }
        //校验ECR需要用到项目角色在项目团队中是否都已经维护对应的人(10个角色标黄色的是必须有分配人) ，
        // 如果没有维护或维护的不全，抛出提示指导项目经理。则不能完成任务，需要增加校验。
        // 并校验SDT角色账号是否都是活动中
        //add by ljr 20240801
        if (isECRReviewRoute) {
            _logger.info("projectSet:{}", projectSet);
            if (!projectSet.isEmpty()) {
                _logger.info("projectSet:{}", projectSet);
                StringList projectSpaceList = StringList.create(projectSet);
                HashMap<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("projectName", projectSpaceList);
                paramsMap.put("type", strPolicy.split("_")[1]);
                paramsMap.put("ecrId", strEcrId);
                return JF_SignTask_mxJPO.checkProjectPersonRole(context, JPO.packArgs(paramsMap));
            }
        }
        //end
        return 0;
    }

    /**
     * 判断当前Inbox Task完成后DA延期Route是否全部完成
     **
     * @param context
     * @param routeId Route对象ID
     * @param inboxTaskId 当前Inbox Task对象ID
     * @return boolean 当前任务位于最后流程节点且同节点其他任务均完成时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/8/4
     */
    private boolean isFinalDAExtensionTask(Context context, String routeId, String inboxTaskId)
            throws Exception {
        if (UIUtil.isNullOrEmpty(routeId)) {
            return false;
        }

        DomainObject routeObject = DomainObject.newInstance(context, routeId);
        String currentRouteNodeAttribute = PropertyUtil.getSchemaProperty(context,
                "attribute_CurrentRouteNode");
        String currentRouteSequenceValue = routeObject.getAttributeValue(context,
                currentRouteNodeAttribute);
        if (UIUtil.isNullOrEmpty(currentRouteSequenceValue)) {
            return false;
        }
        String selectRouteSequence = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_SEQUENCE + "]";
        String selectRouteNodeId = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_NODE_ID + "]";
        StringList routeNodeRelSelects = StringList.create(selectRouteSequence, selectRouteNodeId);
        MapList routeNodeList = routeObject.getRelatedObjects(context,
                DomainObject.RELATIONSHIP_ROUTE_NODE,
                DomainConstants.QUERY_WILDCARD,
                new StringList(),
                routeNodeRelSelects,
                false,
                true,
                (short) 1,
                DomainConstants.EMPTY_STRING,
                DomainConstants.EMPTY_STRING,
                0);

        int currentSequence = Integer.parseInt(currentRouteSequenceValue);
        int maximumSequence = -1;
        Set<String> currentRouteNodeIds = new HashSet<>();
        for (Object routeNodeObject : routeNodeList) {
            Map routeNodeMap = (Map) routeNodeObject;
            String sequenceValue = UIUtil.getValue(routeNodeMap, selectRouteSequence);
            if (UIUtil.isNullOrEmpty(sequenceValue)) {
                continue;
            }
            int sequence = Integer.parseInt(sequenceValue);
            maximumSequence = Math.max(maximumSequence, sequence);
            if (currentSequence == sequence) {
                currentRouteNodeIds.add(UIUtil.getValue(routeNodeMap, selectRouteNodeId));
            }
        }
        _logger.info("DA extension routeId:{}, currentSequence:{}, maximumSequence:{}",
                routeId, currentSequence, maximumSequence);
        if (currentSequence != maximumSequence || currentRouteNodeIds.isEmpty()) {
            return false;
        }

        StringList taskSelects = StringList.create(SELECT_ID, SELECT_CURRENT, selectRouteNodeId);
        MapList taskList = routeObject.getRelatedObjects(context,
                RELATIONSHIP_ROUTE_TASK,
                TYPE_INBOX_TASK,
                taskSelects,
                null,
                true,
                false,
                (short) 1,
                DomainConstants.EMPTY_STRING,
                DomainConstants.EMPTY_STRING,
                0);
        for (Object taskObject : taskList) {
            Map taskMap = (Map) taskObject;
            if (currentRouteNodeIds.contains(UIUtil.getValue(taskMap, selectRouteNodeId))
                    && !inboxTaskId.equals(UIUtil.getValue(taskMap, SELECT_ID))
                    && !"Complete".equals(UIUtil.getValue(taskMap, SELECT_CURRENT))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param context
     * @param args
     * @return void
     * @throws
     * @description ECR 提交到审核创建流程
     * @author CHENYAN
     * @date 2024/7/29 10:19
     */
    public void createRouteInReview(Context context, String[] args) throws Exception {
        _logger.info("------------------------ createRouteInReview begin  -----------------------------------");
        String strObjectId = args[0];
        String strCurrent = args[1];
        String strNextState = args[2];
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strCurrent:{}", strCurrent);
        _logger.info("strNextState:{}", strNextState);
        MapList chairManagerPersonList = new MapList();
        String strProjectOwner = "";
        String strLoginUser = context.getUser();
        try {
            ContextUtil.pushContext(context);
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
//            strProjectOwner = ecr.getInfo(context,"from[JFChange2Project].to.owner");
            //获取项目经理角色，如果没有项目经理角色才获取项目owner
            strProjectOwner = JF_Util_mxJPO.getProjectManager(context, new String[]{ecr.getInfo(context, "from[JFChange2Project].to.id")});
            StringList typeSelectList = new StringList(SELECT_ID);
            typeSelectList.add(SELECT_NAME);
            //  整椅经理获取
            typeSelectList.add(SELECT_NAME);
            StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFROUTENOTE_APPROVALAROLE);
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFROUTEOBJECTSTATUS);
            chairManagerPersonList = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_ECR2PERSON, // relationship pattern
                    TYPE_PERSON,                                    // object pattern
                    typeSelectList,                            // object selects
                    reSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
        } finally {
            ContextUtil.popContext(context);
        }
        _logger.info("user :{}", strLoginUser);
        int pmIndex = 1;
        String strDepartmentMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.Review.DepartmentManager");
        String strProjectManagerMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.Review.ProjectManager");

        MapList approveList = new MapList();
        //可能会存在多个
        if (chairManagerPersonList.size() > 0) {
            for (int i = 0; i < chairManagerPersonList.size(); i++) {
                Map person = (Map) chairManagerPersonList.get(i);
                String strPersonId = (String) person.get(SELECT_ID);
                Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPersonId, strDepartmentMess, "true", "1", "All");
                approveList.add(managerMap);
            }
            pmIndex += 1;
        }
        //项目经理
        DomainObject PMObj = PersonUtil.getPersonObject(context, strProjectOwner);
        String strPMId = PMObj.getInfo(context, SELECT_ID);

        Map managerMap = JF_PublicMethodClass_mxJPO.getMapAnyOrAll(strPMId, strProjectManagerMess, "true", String.valueOf(pmIndex), "All");
        approveList.add(managerMap);
        //创建流程
        JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
        String state = "state_Review";//在哪个状态增加流程
        String policy = "policy_JFECR";//哪个Policy上面
        String routeDescription = "ECR提交审核";//流程描述
        String routeId = jf_route.createAndStartRoute(context, approveList, strObjectId, state, policy, routeDescription);
        _logger.info("------------------------ createRouteInReview end  -----------------------------------");
    }

    /**
     * @param context
     * @param args
     * @return boolean  true 可编辑 false 不可编辑
     * @throws
     * @description 获取ECRForm中编辑按钮的权限
     * @author CHENYAN
     * @date 2024/7/30 13:25
     */
    public boolean getECRFormHasEditCmdAccess(Context context, String[] args) throws Exception {
        _logger.info("------------------------------- getECRFormHasEditCmdAccess begin --------------------------------");
        Map formSettingMap = JPO.unpackArgs(args);
        String strObjectId = (String) formSettingMap.get("objectId");
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        String strLoinUser = context.getUser();
        StringList typeSelectList = new StringList();
        typeSelectList.add(SELECT_CURRENT);
        typeSelectList.add("from[JFChange2Project].to.id");
        typeSelectList.add(SELECT_OWNER);
        Map ecrInfo = ecr.getInfo(context, typeSelectList);
        String strCurrent = (String) ecrInfo.get(SELECT_CURRENT);
        if ("Create".equals(strCurrent)) {
            String strOwner = (String) ecrInfo.get(SELECT_OWNER);
            _logger.info("strOwner:{}", strOwner);
            _logger.info("strLoinUser:{}", strLoinUser);
            return strLoinUser.equals(strOwner);
        } else if ("Review".equals(strCurrent)) {
            String strProjectId = (String) ecrInfo.get("from[JFChange2Project].to.id");

            String strProjectManager = JF_Util_mxJPO.getProjectManager(context, new String[]{strProjectId});
            return strLoinUser.equals(strProjectManager) ? true : false;
        }
        return false;
    }

    /**
     * @param context
     * @param args
     * @return void
     * @throws
     * @description 处理ECR中编辑整椅开发工程师列
     * @author CHENYAN
     * @date 2024/8/9 0:31
     */
    public Map processEditECRJPO(Context context, String[] args) throws Exception {
        _logger.info("-----------------------------  processEditECRJPO begin ---------------------------------------");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        String strManagerReviewOID = (String) requestMap.get("ManagerReviewOID");
        String strManagerReview = (String) requestMap.get("ManagerReview");
        String strJSApprovePerson = (String) requestMap.get("JSApprovePerson");
        String strJFECRQQFileId = (String) requestMap.get("JFECRQQFileId");
        String strJFECRQQFileId2 = (String) requestMap.get("JFECRQQFileId2");
        String strAffectedProjectIds = (String) requestMap.get("JFAffectedProject");
        String strQQID = (String) requestMap.get("JFQQ");
        String strObjectId = (String) requestMap.get("objectId");
        Map res = new HashMap<>();
        //防止不做任何编辑点提交
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strJSApprovePerson:{}", strJSApprovePerson);
        _logger.info("strManagerReviewOID:{}", strManagerReviewOID);
        _logger.info("strQQID:{}", strQQID);
        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
//             if (UIUtil.isNotNullAndNotEmpty(strObjectId)){
//                 String[] split = strJSApprovePerson.split(",");
//                 DomainObject ecr = DomainObject.newInstance(context, strObjectId);
//                 StringList reSelectList = new StringList();
//                 reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFROUTENOTE_APPROVALAROLE);
//                 reSelectList.add(SELECT_RELATIONSHIP_ID);
//                 StringList typeSelectList = new StringList();
//                 typeSelectList.add(SELECT_ID);
//                 typeSelectList.add(SELECT_NAME);
//                 String strBusWhere = "";
//                 String strRelWhere = "";
//                 StringList resApproveIdList = StringList.create(split);
//                 HashMap<String, String> roleMap = new HashMap<>();
////                 //先整理整椅开发工程师
////                 for (int i = 0; i < resApproveIdList.size(); i++) {
////                     String strRoleValue = JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_DEV_ENGINEER;
////                     String strPersonId = resApproveIdList.get(i);
////                     if (strPersonId.equals(strManagerReviewOID)){
////                         strRoleValue = JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_BOTH;
////                     }else if (UIUtil.isNotNullAndNotEmpty(strManagerReview) && UIUtil.isNullOrEmpty(strManagerReviewOID)){
////                         roleMap.put(strManagerReviewOID,JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_MANGER);
////                     }
////                     roleMap.put(strPersonId,strRoleValue);
////                 }
//                 //如果roleMap中没有strManagerReviewOID 说明需要加上本身
////                 if (!roleMap.containsKey(strManagerReviewOID)){
////                     if (UIUtil.isNotNullAndNotEmpty(strManagerReviewOID)){
////                         roleMap.put(strManagerReviewOID,JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_MANGER);
////                     }
////                 }
//                 _logger.info("roleMap:{}",roleMap);
//                 //old person
////                 MapList maps = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_ECR2PERSON , // relationship pattern
////                         TYPE_PERSON,                                    // object pattern
////                         typeSelectList,                            // object selects
////                         reSelectList, // relationship selects
////                         false,                                        // to direction
////                         true,                                        // from direction
////                         (short) 1,                                    // recursion level
////                         strBusWhere,                // object where clause
////                         strRelWhere,
////                         (short) 0);
////                 _logger.info("maps:{}",maps);
////                 StringList delRelIdList = new StringList();
////                 StringList oldPersonIdList = new StringList();
////                 for (int i = 0; i < maps.size(); i++) {
////                     Map person = (Map) maps.get(i);
////                     String strPersonId = (String) person.get(SELECT_ID);
////                     String strRelId = (String) person.get(SELECT_RELATIONSHIP_ID);
////                     String strRoleName = (String) person.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFROUTENOTE_APPROVALAROLE);
////                     oldPersonIdList.add(strPersonId);
////                     if (roleMap.containsKey(strPersonId)){
////                         Map relAttrMap = new HashMap<>();
////                         DomainRelationship rel = DomainRelationship.newInstance(context,strRelId);
////                         relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE,roleMap.get(strPersonId));
////                         relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTEOBJECTSTATUS,"Create");
////                         rel.setAttributeValues(context,relAttrMap);
////                     }else {
////                         delRelIdList.add(strRelId);
////                     }
////                 }
//                //移除没有的人员
////                 DomainRelationship.disconnect(context,delRelIdList.toStringArray());
//                 //添加新的人员
////                 Set<String> personIdSet = roleMap.keySet();
////                 for (String strPersonId : personIdSet) {
////                     //旧的人员不包含新的人员
////                     if (!oldPersonIdList.contains(strPersonId)) {
////                         Map relAttrMap = new HashMap<>();
////                         DomainRelationship rel = DomainRelationship.connect(context,ecr , JF_PLMConstants_mxJPO.REL_ECR2PERSON, DomainObject.newInstance(context, strPersonId));
////                         relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE,roleMap.get(strPersonId));
////                         relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTEOBJECTSTATUS,"Create");
////                         rel.setAttributeValues(context,relAttrMap);
////                     }
////                 }
//
//             }
            //更新受影响项目
            if (UIUtil.isNotNullAndNotEmpty(strObjectId)) {
                StringList typeSelectList = new StringList();
                StringList reSelectList = new StringList();
                typeSelectList.add(SELECT_ID);
                typeSelectList.add(SELECT_NAME);
                reSelectList.add(SELECT_RELATIONSHIP_ID);
                DomainObject ecr = DomainObject.newInstance(context, strObjectId);
                String strCurrent = ecr.getInfo(context, SELECT_CURRENT);
                if ("Create".equals(strCurrent)) {
                    MapList maps = ecr.getRelatedObjects(context, "JFECR2AffectedProject", // relationship pattern
                            TYPE_PROJECT_SPACE,                                    // object pattern
                            typeSelectList,                            // object selects
                            reSelectList, // relationship selects
                            false,                                        // to direction
                            true,                                        // from direction
                            (short) 1,                                    // recursion level
                            "",                // object where clause
                            "",
                            (short) 0);
                    StringList disConnectIdList = new StringList();
                    Set newProjectSet = new HashSet<String>();
                    if (UIUtil.isNotNullAndNotEmpty(strAffectedProjectIds)) {
                        String[] affectedProject = strAffectedProjectIds.split(",");
                        StringList affectedProjectIdList = StringList.create(affectedProject);
                        if (maps.size() > 0) {
                            for (int i = 0; i < maps.size(); i++) {
                                Map affectedProjectMap = (Map) maps.get(i);
                                String strAffectedProjectId = (String) affectedProjectMap.get(SELECT_ID);
                                String strRelId = (String) affectedProjectMap.get(SELECT_RELATIONSHIP_ID);
                                if (affectedProjectIdList.contains(strAffectedProjectId)) {
                                    continue;
                                }
                                disConnectIdList.add(strRelId);
                            }
                        }
                        for (int i = 0; i < affectedProjectIdList.size(); i++) {
                            String strNewProjectId = affectedProjectIdList.get(i);
                            boolean isNewProject = true;
                            for (int j = 0; j < maps.size(); j++) {
                                Map affectedProjectMap = (Map) maps.get(j);
                                String strAffectedProjectId = (String) affectedProjectMap.get(SELECT_ID);
                                if (strAffectedProjectId.equals(strNewProjectId)) {
                                    isNewProject = false;
                                    break;
                                }
                            }
                            if (isNewProject) {
                                newProjectSet.add(strNewProjectId);
                            }
                        }
                    } else {
                        //全部断开
                        for (int i = 0; i < maps.size(); i++) {
                            Map affectedProjectMap = (Map) maps.get(i);
                            String strAffectedProjectId = (String) affectedProjectMap.get(SELECT_ID);
                            String strRelId = (String) affectedProjectMap.get(SELECT_RELATIONSHIP_ID);
                            disConnectIdList.add(strRelId);
                        }
                    }
                    //连接新受影响项目
                    if (newProjectSet.size() > 0) {
                        DomainRelationship.connect(context, ecr, "JFECR2AffectedProject", true, StringList.create(newProjectSet).toStringArray());
                    }
                    //断开移除受影响项目
                    if (disConnectIdList.size() > 0) {
                        DomainRelationship.disconnect(context, disConnectIdList.toStringArray());
                    }
                    //更新QQ附件
                    MapList docListMap = ecr.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                            TYPE_DOCUMENT,                                    // object pattern
                            typeSelectList,                            // object selects
                            reSelectList, // relationship selects
                            false,                                        // to direction
                            true,                                        // from direction
                            (short) 1,                                    // recursion level
                            "",                // object where clause
                            "attribute[Project Role]=='QQ'",
                            (short) 0);
                    //是否需要连接标识
                    boolean isConnectQQFile = true;
                    Set disDocIdSet = new HashSet<String>();
                    if (docListMap.size() > 0) {
                        for (int i = 0; i < docListMap.size(); i++) {
                            Map docMap = (Map) docListMap.get(i);
                            String strDocId = (String) docMap.get(SELECT_ID);
                            String strRelId = (String) docMap.get(SELECT_RELATIONSHIP_ID);
                            if (strDocId.equals(strJFECRQQFileId)) {
                                isConnectQQFile = false;
                            } else {
                                disDocIdSet.add(strRelId);
                            }
                        }
                    }
                    if (isConnectQQFile) {
                        if (UIUtil.isNotNullAndNotEmpty(strJFECRQQFileId)) {
                            DomainRelationship rel = DomainRelationship.connect(context, ecr, RELATIONSHIP_REFERENCE_DOCUMENT, DomainObject.newInstance(context, strJFECRQQFileId));
                            Map relAttrMap = new HashMap<>();
                            relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE, "QQ");
                            rel.setAttributeValues(context, relAttrMap);
                        }
                        //断开原有关系
                        _logger.info("disDocIdSet:{}", disDocIdSet);
                        if (disDocIdSet.size() > 0) {
                            DomainRelationship.disconnect(context, StringList.create(disDocIdSet).toStringArray());
                        }
                    }
                    //更新问题清单附件
                    MapList docListMap2 = ecr.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                            TYPE_DOCUMENT,                                    // object pattern
                            typeSelectList,                            // object selects
                            reSelectList, // relationship selects
                            false,                                        // to direction
                            true,                                        // from direction
                            (short) 1,                                    // recursion level
                            "",                // object where clause
                            "attribute[Project Role]=='Questions List'",
                            (short) 0);
                    //是否需要连接标识
                    boolean isConnectQQFile2 = true;
                    Set disDocIdSet2 = new HashSet<String>();
                    if (docListMap2.size() > 0) {
                        for (int i = 0; i < docListMap2.size(); i++) {
                            Map docMap = (Map) docListMap2.get(i);
                            String strDocId = (String) docMap.get(SELECT_ID);
                            String strRelId = (String) docMap.get(SELECT_RELATIONSHIP_ID);
                            if (strDocId.equals(strJFECRQQFileId2)) {
                                isConnectQQFile2 = false;
                            } else {
                                disDocIdSet2.add(strRelId);
                            }
                        }
                    }
                    if (isConnectQQFile2) {
                        if (UIUtil.isNotNullAndNotEmpty(strJFECRQQFileId2)) {
                            DomainRelationship rel = DomainRelationship.connect(context, ecr, RELATIONSHIP_REFERENCE_DOCUMENT, DomainObject.newInstance(context, strJFECRQQFileId2));
                            Map relAttrMap = new HashMap<>();
                            relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE, "Questions List");
                            rel.setAttributeValues(context, relAttrMap);
                        }
                        //断开原有关系
                        _logger.info("disDocIdSet:{}", disDocIdSet2);
                        if (disDocIdSet2.size() > 0) {
                            DomainRelationship.disconnect(context, StringList.create(disDocIdSet2).toStringArray());
                        }
                    }
                    ecr.setAttributeValue(context, ATTR_JFQQID, strQQID);
                }
            }
            ContextUtil.commitTransaction(context);
        } catch (FrameworkException e) {
            _logger.error(e.getMessage());
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        res.put("Action", "CONTINUE");
        _logger.info("-----------------------------  processEditECRJPO end ---------------------------------------");
        return res;
    }

    /**
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description 构造ECR 项目和变更事件 属性列  已失效
     * @author CHENYAN
     * @date 2024/7/31 9:42
     */
    @Deprecated
    @com.matrixone.apps.framework.ui.ProgramCallable
    public String buildECRProjectName(Context context, String[] args) throws Exception {
        String strLoginUser = context.getUser();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        _logger.info("programMap:{}", programMap);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        String strFieldName = (String) fieldMap.get("name");
        String strMode = (String) requestMap.get("mode");
        String strObjectId = (String) requestMap.get("objectId");
        String strLanguage = context.getSession().getLanguage();
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        StringList typeSelectList = new StringList();
        String strSearchUrl = "";
        if ("JFProjectName".equals(strFieldName)) {
            strSearchUrl = "../common/emxFullSearch.jsp?field=TYPES=type_ProjectSpace:CURRENT=policy_ProjectSpace.state_Active&cancelLabel=emxFramework.Common.Close&HelpMarker=emxhelpselectorganization&table=ENCAddExistingGeneralSearchResults&selection=single&showInitialResults=true&submitURL=./AEFSearchUtil.jsp";
            typeSelectList.add("from[JFChange2Project].to.name");
            typeSelectList.add("from[JFChange2Project].to.id");
        } else if ("JFChangeEventType".equals(strFieldName)) {
            strSearchUrl = "../common/emxFullSearch.jsp?field=TYPES=type_JFChangeEventType&cancelLabel=emxFramework.Common.Close&table=ENCAddExistingGeneralSearchResults&selection=single&submitURL=./AEFSearchUtil.jsp";
            typeSelectList.add("to[JFChangeEventECR].from.id");
            typeSelectList.add("to[JFChangeEventECR].from.name");
        }
        typeSelectList.add(SELECT_CURRENT);
        typeSelectList.add(SELECT_OWNER);
        Map ecrMap = ecr.getInfo(context, typeSelectList);
        String strBuildName = "";
        String strBuildId = "";
        if ("JFProjectName".equals(strFieldName)) {
            strBuildName = (String) ecrMap.get("from[JFChange2Project].to.name");
            strBuildId = (String) ecrMap.get("from[JFChange2Project].to.id");
        } else if ("JFChangeEventType".equals(strFieldName)) {
            strBuildId = (String) ecrMap.get("to[JFChangeEventECR].from.id");
            strBuildName = (String) ecrMap.get("to[JFChangeEventECR].from.name");
        }
        String strECROwner = (String) ecrMap.get(SELECT_OWNER);
        String strCurrent = (String) ecrMap.get(SELECT_CURRENT);
        boolean hasEditAccess = false;
        if (strECROwner.equals(strLoginUser) && ("Create".equals(strCurrent) || "Submit".equals(strCurrent))) {
            hasEditAccess = true;
        }
        return JF_PublicMethodClass_mxJPO.buildFieldHtml(context, strMode, strFieldName, strBuildId, strBuildName, strSearchUrl, hasEditAccess);
    }

    /**
     * @param context
     * @param args
     * @return void
     * @throws
     * @description 修改ECR FORM属性
     * @author CHENYAN
     * @date 2024/8/9 0:31
     */
    public void updateECRFormField(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map paramMap = (Map) programMap.get("paramMap");
        Map fieldMap = (Map) programMap.get("fieldMap");
        String strNewId = (String) paramMap.get("New OID");
        String strNewValue = (String) paramMap.get("New Value");
        String strFieldName = (String) fieldMap.get("name");
        String strObjectId = (String) paramMap.get("objectId");
        _logger.info("strNewId:{}", strNewId);
        _logger.info("strNewValue:{}", strNewValue);
        _logger.info("strFieldName:{}", strFieldName);
        _logger.info("strObjectId:{}", strObjectId);
        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            String current = ecr.getInfo(context, SELECT_CURRENT);
            if ("JFProjectName".equals(strFieldName)) {
                String strRelId = ecr.getInfo(context, "from[JFChange2Project].id");
                if (UIUtil.isNotNullAndNotEmpty(strRelId)) {
                    DomainRelationship.setToObject(context, strRelId, DomainObject.newInstance(context, strNewId));
                }
            } else if ("JFChangeEventType".equals(strFieldName)) {
                String strRelId = ecr.getInfo(context, "to[JFChangeEventECR].id");
                _logger.info("strRelId:{}", strRelId);
                if (UIUtil.isNotNullAndNotEmpty(strRelId)) {
                    if (UIUtil.isNotNullAndNotEmpty(strNewId)) {
                        DomainRelationship.setFromObject(context, strRelId, DomainObject.newInstance(context, strNewId));
                    } else {
                        //断开关系
                        DomainRelationship.disconnect(context, strRelId);
                    }
                } else {
                    DomainRelationship.connect(context, DomainObject.newInstance(context, strNewId), "JFChangeEventECR", ecr);
                }
            } else if ("JFChangesDeveExpensesManHours".equals(strFieldName) || "JFChangesTrialExpensesManHours".equals(strFieldName) || "JFAffectsFactory".equals(strFieldName)
                    || "JFChangesDeveExpensesManHoursExternal".equals(strFieldName) || "JFChangesTrialExpensesManHoursExternal".equals(strFieldName) || "JFAssociatedOtherProjects".equals(strFieldName)
            ) {
                //列名和属性名不一致
                if ("JFAffectsFactory".equals(strFieldName)) {
                    String[] strNewValues = (String[]) paramMap.get("New Values");
                    String strJFAffectsFactory = "";
                    Attribute attributeValues = ecr.getAttributeValues(context, ATTR_JFAFFECTEDFACTORY);
                    StringList newValueList = new StringList();
                    if (null != strNewValues && strNewValues.length > 0) {
                        newValueList = StringList.create(strNewValues);
                        _logger.info("newValueList:{}", newValueList);
                    }
                    _logger.info("newValueList:{}", newValueList.size());
                    if (newValueList.size() > 0) {
                        //为空时传入数组有空串
                        if (newValueList.size() == 1 && newValueList.contains("")) {
                            strJFAffectsFactory = "\"\"";
                        } else {
                            strJFAffectsFactory = newValueList.join(",");
                        }
                    } else {
                        strJFAffectsFactory = "\"\"";
                    }
                    _logger.info("strJFAffectsFactory:{}", strJFAffectsFactory);
                    if ("Create".equalsIgnoreCase(current)) {
                        String strMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("mod bus ", strObjectId, " ", ATTR_JFAFFECTEDFACTORY, " ", strJFAffectsFactory, ";");
                        _logger.info("strMql:{}", strMql);
                        MqlUtil.mqlCommand(context, strMql);
                    }
                    if ("Draft".equalsIgnoreCase(current)) {
                        String strMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("mod bus ", strObjectId, " ", ATTR_JFAFFECTEDFACTORY, " ", strJFAffectsFactory, ";");
                        _logger.info("strMql:{}", strMql);
                        MqlUtil.mqlCommand(context, strMql);
                    }
//                         ecr.setAttributeValue(context,"JFAffectedFactory",strJFAffectsFactory);
                } else {
                    String strCurrent = ecr.getInfo(context, SELECT_CURRENT);
                    if (!(
                            "Review".equals(strCurrent) &&
                                    ("JFChangesDeveExpensesManHoursExternal".equals(strFieldName) || "JFChangesDeveExpensesManHours".equals(strFieldName))
                    )) {
                        ecr.setAttributeValue(context, strFieldName, strNewValue);
                    }
                }
            } else if ("ManagerReview".equals(strFieldName)) {
                String strRelId = ecr.getInfo(context, "from[JFECR2Person].id");
                if (UIUtil.isNotNullAndNotEmpty(strRelId)) {
                    if (UIUtil.isNotNullAndNotEmpty(strNewId)) {
                        DomainRelationship.setToObject(context, strRelId, DomainObject.newInstance(context, strNewId));
                    } else {
                        DomainRelationship.disconnect(context, strRelId);
                    }
                } else {
                    if (UIUtil.isNotNullAndNotEmpty(strNewId)) {
                        DomainRelationship.connect(context, ecr, "JFECR2Person", DomainObject.newInstance(context, strNewId));
                    }
                }
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            _logger.error(e.getMessage());
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }

    }

    /**
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description 构造ECR中联动列
     * @author CHENYAN
     * @date 2024/8/9 0:30
     */
    public String buildECRFormLinkageAttributeHtml(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        String strFieldName = (String) fieldMap.get("name");
        String strMode = (String) requestMap.get("mode");
        String strObjectId = (String) requestMap.get("objectId");
        if ("view".equals(strMode)) {
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            if ("JFAffectsFactory".equals(strFieldName)) {
                Attribute attributeValues = ecr.getAttributeValues(context, ATTR_JFAFFECTEDFACTORY);
                StringList valueList = attributeValues.getValueList();
                StringList rangeNlsList = i18nNow.getAttrRangeI18NStringList("JFAffectedFactory", valueList, context.getSession().getLanguage());
                return rangeNlsList.join("<br/>");
            } else if ("JFChangesDeveExpensesManHours".equals(strFieldName)) {
                return ecr.getInfo(context, SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS);

            } else if ("JFChangesTrialExpensesManHours".equals(strFieldName)) {
                return ecr.getInfo(context, SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURS);

            } else if ("JFChangesDeveExpensesManHoursExternal".equals(strFieldName)) {
                return ecr.getInfo(context, SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL);

            } else if ("JFChangesTrialExpensesManHoursExternal".equals(strFieldName)) {
                return ecr.getInfo(context, SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL);
            }
        } else if ("edit".equals(strMode)) {
            boolean isReadOnly = false;
            boolean isRequire = false;
            String strFieldValue = "";
            String strFieldNls = "";
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            //判断是否可以编辑
            StringList ecrSelectList = new StringList();
            ecrSelectList.add(SELECT_CURRENT);
            ecrSelectList.add(SELECT_OWNER);
            Map ecrInfo = ecr.getInfo(context, ecrSelectList);
            String strCurrent = (String) ecrInfo.get(SELECT_CURRENT);
            String strOwner = (String) ecrInfo.get(SELECT_OWNER);
            String strLoginUser = context.getUser();
            if (strLoginUser.equals(strOwner) && ("Create".equals(strCurrent)||"Draft".equals(strCurrent))) {
                if ("JFAffectsFactory".equals(strFieldName)) {
                    StringList typeSelectList = new StringList();
                    typeSelectList.add(SELECT_ATTR_JFISPLATFORMPART);
                    typeSelectList.add(SELECT_ATTR_JFAFFECTEDFACTORY);
                    Map ecrMap = ecr.getInfo(context, typeSelectList);
                    String strIsPlatformPart = (String) ecrMap.get(SELECT_ATTR_JFISPLATFORMPART);
                    Attribute attributeValues = ecr.getAttributeValues(context, ATTR_JFAFFECTEDFACTORY);
                    StringList valueList = attributeValues.getValueList();
                    strFieldValue = valueList.join(",");
                    isReadOnly = "No".equals(strIsPlatformPart)||"N".equals(strIsPlatformPart);
                    isRequire = !isReadOnly;
                } else if ("JFChangesDeveExpensesManHours".equals(strFieldName)) {
                    StringList typeSelectList = new StringList();
                    typeSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
                    typeSelectList.add(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS);
                    Map ecrMap = ecr.getInfo(context, typeSelectList);
                    String strChangeSource = (String) ecrMap.get(SELECT_ATTR_JFCHANGESOURCE);
                    strFieldValue = (String) ecrMap.get(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS);
                    isReadOnly = "External Changes".equals(strChangeSource);
                    isRequire = !isReadOnly;
                } else if ("JFChangesTrialExpensesManHours".equals(strFieldName)) {
                    StringList typeSelectList = new StringList();
                    typeSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
                    typeSelectList.add(SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURS);
                    Map ecrMap = ecr.getInfo(context, typeSelectList);
                    String strChangeSource = (String) ecrMap.get(SELECT_ATTR_JFCHANGESOURCE);
                    strFieldValue = (String) ecrMap.get(SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURS);
                    isReadOnly = "External Changes".equals(strChangeSource);
                    isRequire = !isReadOnly;
                } else if ("JFChangesDeveExpensesManHoursExternal".equals(strFieldName)) {
                    StringList typeSelectList = new StringList();
                    typeSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
                    typeSelectList.add(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL);
                    Map ecrMap = ecr.getInfo(context, typeSelectList);
                    String strChangeSource = (String) ecrMap.get(SELECT_ATTR_JFCHANGESOURCE);
                    strFieldValue = (String) ecrMap.get(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL);
                    isReadOnly = "Internal Changes".equals(strChangeSource);
                    isRequire = !isReadOnly;
                } else if ("JFChangesTrialExpensesManHoursExternal".equals(strFieldName)) {
                    StringList typeSelectList = new StringList();
                    typeSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
                    typeSelectList.add(SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL);
                    Map ecrMap = ecr.getInfo(context, typeSelectList);
                    String strChangeSource = (String) ecrMap.get(SELECT_ATTR_JFCHANGESOURCE);
                    strFieldValue = (String) ecrMap.get(SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL);
                    isReadOnly = "Internal Changes".equals(strChangeSource);
                    isRequire = !isReadOnly;
                }
                strFieldNls = EnoviaResourceBundle.getAttributeI18NString(context, strFieldName, context.getLocale().toString());
                return buildFormFieldHtml(context, strFieldName, strFieldValue, strFieldNls, isReadOnly, isRequire);
            } else {
                if ("JFAffectsFactory".equals(strFieldName)) {
                    Attribute attributeValues = ecr.getAttributeValues(context, ATTR_JFAFFECTEDFACTORY);
                    StringList valueList = attributeValues.getValueList();
                    StringList rangeNlsList = i18nNow.getAttrRangeI18NStringList("JFAffectedFactory", valueList, context.getSession().getLanguage());
                    return rangeNlsList.join("<br/>");
                } else if ("JFChangesDeveExpensesManHours".equals(strFieldName)) {
                    return ecr.getInfo(context, SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS);
                } else if ("JFChangesTrialExpensesManHours".equals(strFieldName)) {
                    return ecr.getInfo(context, SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURS);

                } else if ("JFChangesDeveExpensesManHoursExternal".equals(strFieldName)) {
                    return ecr.getInfo(context, SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL);

                } else if ("JFChangesTrialExpensesManHoursExternal".equals(strFieldName)) {
                    return ecr.getInfo(context, SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL);
                }
            }
        }
        return "";
    }

    /**
     * @param strFieldName    列名称
     * @param strFieldValue   列展示值
     * @param strFieldNameNls 列翻译
     * @param isReadOnly      是否可读
     * @return java.lang.String
     * @throws
     * @description 构造FormHTML
     * @author CHENYAN
     * @date 2024/8/9 0:29
     */

    public static String buildFormFieldHtml(Context context, String strFieldName, String strFieldValue, String strFieldNameNls, boolean isReadOnly, boolean isRequire) throws Exception {
        StringBuffer sb = new StringBuffer();
        switch (strFieldName) {
            case "JFAffectsFactory", "JFChangesDeveExpensesManHours",
                    "JFChangesTrialExpensesManHours", "JFChangesDeveExpensesManHoursExternal",
                    "JFChangesTrialExpensesManHoursExternal": {
                _logger.info("strFieldName:{}", strFieldName);
                if ("JFAffectsFactory".equalsIgnoreCase(strFieldName)) {
                    //i18nNow.getRangeI18NString("CRC Part Category", changeNull(tempList.get(0).toString()), language); 属性 range值翻译
                    StringList list = new StringList();
                    try {
                        String[] split = strFieldValue.split(",");
                        StringList JFAffectsFactoryDBValue = StringList.create(split);
                        list = com.matrixone.apps.domain.util.mxAttr.getChoices(context, "JFAffectedFactory");
                        //移除空串
                        if (list.contains("")) {
                            list.remove("");
                        }
                        sb.append("<table>");
                        sb.append("<tbody>");
                        for (int i = 0; i < list.size(); i++) {
                            String strRange = list.get(i);
                            String strRangeNls = i18nNow.getRangeI18NString("JFAffectsFactory", strRange, context.getSession().getLanguage());
                            sb.append("<tr>");
                            sb.append("<td>");
                            if (JFAffectsFactoryDBValue.contains(strRange)) {
                                sb.append("<input id=\"" + strRange + "\" type=\"checkbox\" name=\"JFAffectsFactory\" value=\"" + strRange + "\"  checked>");
                            } else {
                                sb.append("<input id=\"" + strRange + "\" type=\"checkbox\" name=\"JFAffectsFactory\" value=\"" + strRange + "\">");
                            }
                            sb.append("</td>");
                            sb.append("<td>");
                            sb.append(strRangeNls);
                            sb.append("</td>");
                            sb.append("</tr>");
                        }
                        sb.append("</table>");
                        sb.append("</tbody>");
//                         sb.append("<input type=\"hidden\" id=\"");
//                         sb.append(strFieldName);
//                         sb.append("\"");
//                         sb.append(JF_PublicMethodClass_mxJPO.buildStringInStrings(" value=\"", strFieldValue, "\""));
//                         sb.append(">");
//                         sb.append("  <div id=\"app\">\n" +
//                                 "<el-select v-model=\"attrValue\"  @change=\"OnJFAffectedFactoryChange\" multiple placeholder=\"请选择\">\n" +
//                                         "    <el-option\n" +
//                                         "      v-for=\"item in options\"\n" +
//                                         "      :key=\"item.value\"\n" +
//                                         "      :label=\"item.label\"\n" +
//                                         "      :value=\"item.value\">\n" +
//                                         "    </el-option>\n" +
//                                         "  </el-select>"+
//                                 "  </div>");
//                         sb.append("   <script>\n" +
//                                 "    new Vue({\n" +
//                                 "      el: '#app',\n" +
//                                 "      data: function() {\n" +
//                                 "        return {"+buildVueData(context,"JFAffectedFactory",strFieldValue)+"}\n" +
//                                 "      },\n");
//                         sb.append(buildVueMethod());
//                                 sb.append("    })\n"+
//                                 "  </script>");
//                         buildVueMethod();
                    } catch (Exception e) {
                        _logger.info("list:{}", list);
                    }
                } else {
                    sb.append("<input type=\"hidden\" name='");
                    sb.append(JF_PublicMethodClass_mxJPO.buildStringInStrings(strFieldName, "fieldValue'"));
                    sb.append(JF_PublicMethodClass_mxJPO.buildStringInStrings("value=\"", strFieldValue, "\""));
                    sb.append(">");
                    sb.append("<input type=\"text\" name=\"");
                    sb.append(strFieldName);
                    sb.append("\"");
                    sb.append("id=\"");
                    sb.append(strFieldName);
                    sb.append("\"");
                    sb.append(" size=\"20\"");
                    sb.append(" title=\"");
                    sb.append(StringEscapeUtils.escapeHtml4(strFieldNameNls));
                    sb.append(JF_PublicMethodClass_mxJPO.buildStringInStrings(" \" value=\"", strFieldValue, "\" "));
                    if (isReadOnly) {
                        //style="background-color: rgb(235, 235, 228);"
                        sb.append(" readonly=\"readonly\" ");
                    }
                    sb.append(">");
                }
                //加载必填样式
                if ("JFChangesDeveExpensesManHours".equals(strFieldName) || "JFChangesDeveExpensesManHoursExternal".equals(strFieldName)) {
                    if (isRequire) {
                        sb.append("<script>");
                        sb.append("  var  dom = document.getElementById('");
                        String strParentName = "JFChangesDeveExpensesManHours";
//                         if ("JFChangesDeveExpensesManHours".equals(strFieldName)){
//                             strParentName = "JFChangesDeveExpensesManHours";
//                         }
                        String strDomId = JF_PublicMethodClass_mxJPO.buildStringInStrings("calc_", strParentName);
                        sb.append(strDomId);
                        sb.append("');");
                        sb.append("  if (dom){");
                        if ("JFChangesDeveExpensesManHours".equals(strFieldName)) {
                            sb.append("    dom.querySelector('td:first-child').className = \"createLabelRequired\";");
                        } else if ("JFChangesDeveExpensesManHoursExternal".equals(strFieldName)) {
                            sb.append("    dom.querySelector('td:nth-child(3)').className = \"createLabelRequired\";");
                        }
                        sb.append("  }");
                        sb.append("</script>");
                    }
                }
                //最后一个元素渲染完成后 window的onload添加校验函数js
                if ("JFChangesDeveExpensesManHoursExternal".equals(strFieldName)) {
                    sb.append("<script>");
                    sb.append(" // 使用 addEventListener 添加第三个 onload 函数\n" +
                            "    window.addEventListener('load', function addValidateFunOnloadHandler() {\n" +
                            "        document.getElementById('JFChangesDeveExpensesManHours').customValidate = checkInputIsNumber;\n" +
                            "        document.getElementById('JFChangesDeveExpensesManHoursExternal').customValidate = checkInputIsNumber;\n" +
                            "    }, false);");
                    sb.append("</script>");
                } else if ("JFAffectsFactory".equals(strFieldName)) {
                    if (isRequire) {
                        sb.append("<script>");
                        sb.append("  var  dom = document.getElementById('");
                        sb.append("calc_JFAffectsFactory");
                        sb.append("');");
                        sb.append("  if (dom){");
                        sb.append("    dom.querySelector('td:first-child').className = \"createLabelRequired\";");
                        sb.append("  }");
                        sb.append("</script>");
                    }
                    sb.append("<script>");
                    sb.append(" // 使用 addEventListener 添加第三个 onload 函数\n" +
                            "    window.addEventListener('load', function addJFAffectsFactoryValidateFunOnloadHandler() {\n" +
                            "        document.getElementsByName(\"JFAffectsFactory\")[0].customValidate = checkInputIsNullEdit;\n");
                    if (UIUtil.isNullOrEmpty(strFieldValue)) {
                        sb.append("emxFormSetFieldEditable(\"JFAffectsFactory\",false);");
                    }
                    sb.append("    }, false);");

                    sb.append("</script>");
                }

            }
        }
        return sb.toString();
    }

    /**
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description 构造整椅开发工程师列 选人多选并且人是必填
     * @author CHENYAN
     * @date 2024/8/2 13:28
     */
    @ProgramCallable
    public String buildMultipleChoicePersonHtml(Context context, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        //添加移除文字提示
        String strAddButton = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Button.AddWCharDevEngineering");
        String strRemoveButton = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Button.RemoveWCharDevEngineering");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strMode = (String) requestMap.get("mode");
        String strObjectId = (String) requestMap.get("objectId");
        _logger.info("strMode:{}", strMode);
        _logger.info("strObjectId:{}", strObjectId);
        String strLoginUser = context.getUser();
        StringList peronIdList = new StringList();
        StringList peronNameList = new StringList();
        if (UIUtil.isNotNullAndNotEmpty(strObjectId)) {
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            StringList busSelectList = new StringList();
            busSelectList.add(SELECT_CURRENT);
            busSelectList.add(SELECT_OWNER);
            Map ecrInfo = ecr.getInfo(context, busSelectList);
            String strCurrent = (String) ecrInfo.get(SELECT_CURRENT);
            String strOwner = (String) ecrInfo.get(SELECT_OWNER);
            StringList reSelectList = new StringList();
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFROUTENOTE_APPROVALAROLE);
            StringList typeSelectList = new StringList();
            typeSelectList.add(SELECT_ID);
            typeSelectList.add(SELECT_NAME);
            MapList maps = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_ECR2PERSON, // relationship pattern
                    TYPE_PERSON,                                    // object pattern
                    typeSelectList,                            // object selects
                    reSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);

            //过滤出整椅开发工程师
            MapList devPersonList = (MapList) maps.stream().filter(m -> {
                Map devPerson = (Map) m;
                return JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_DEV_ENGINEER.equals(devPerson.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFROUTENOTE_APPROVALAROLE))
                        || JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_BOTH.equals(devPerson.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFROUTENOTE_APPROVALAROLE));
            }).collect(Collectors.toCollection(MapList::new));
            if (null != devPersonList && devPersonList.size() > 0) {
                for (int i = 0; i < devPersonList.size(); i++) {
                    Map person = (Map) maps.get(i);
                    peronIdList.add((String) person.get(SELECT_ID));
                    String strName = (String) person.get(SELECT_NAME);
                    String strFullName = PersonUtil.getFullName(context, strName);
                    peronNameList.add(strFullName);
                }
            }
            //  "create".equalsIgnoreCase(strMode)
            // 整椅经理只有草稿、和创建人可以修改
            if (UIUtil.isNotNullAndNotEmpty(strMode)) {
                if ("edit".equalsIgnoreCase(strMode)) {
                    if ((!"Create".equals(strCurrent)) || (!strLoginUser.equals(strOwner))) {
                        return peronNameList.join(",");
                    }
                } else if ("view".equalsIgnoreCase(strMode)) {
                    return peronNameList.join(",");
                }
            }
            //XSSOK
            sb.append("<input type=\"hidden\" name=\"IsContributorFieldModified\" id=\"IsContributorFieldModified\" value=\"false\" readonly=\"readonly\" />");
            //XSSOK
            sb.append("<input type=\"hidden\" name=\"JSApprovePerson\" id=\"JSApprovePerson\" value=\"" + peronIdList.join(",") + "\" readonly=\"readonly\" />");

            sb.append("<table>");
            sb.append("<tr>");
            sb.append("<th rowspan=\"2\">");
            sb.append("<select name=\"Contributor\" style=\"width:200px\" multiple=\"multiple\">");
            if (peronNameList.size() > 0) {
                for (int i = 0; i < peronNameList.size(); i++) {
                    sb.append("<option value=\"" + peronIdList.get(i) + "\" >");
                    //XSSOK
                    String strPersonName = peronNameList.get(i);
                    strPersonName = StringEscapeUtils.escapeHtml4(strPersonName);
                    sb.append(strPersonName);
                    sb.append("</option>");
                }
            }
            sb.append("</select>");
            sb.append("</th>");
            sb.append("<td>");
            sb.append("<a href=\"javascript:addJSApprovePersonDEV()\">");
            sb.append("<img src=\"../common/images/iconStatusAdded.gif\" width=\"12\" height=\"12\" border=\"0\" />");
            sb.append("</a>");
            sb.append("<a href=\"javascript:addJSApprovePersonDEV()\">");
            //XSSOK
            sb.append(strAddButton);
            sb.append("</a>");
            //sb.append("</div>");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("<tr>");
            sb.append("<td>");
            sb.append("<a href=\"javascript:removeJSApprovePerson()\">");
            sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
            sb.append("</a>");
            sb.append("<a href=\"javascript:removeJSApprovePerson()\">");
            //XSSOK
            sb.append(strRemoveButton);
            sb.append("</a>");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</table>");
            sb.append("<script language=\"JavaScript\">");
            sb.append("function checkWCharDevEngineeringIsNotNull(){\n" +
                    "        const strContributorHidden = document.getElementById(\"JSApprovePerson\").value;\n" +
                    "        if (strContributorHidden){\n" +
                    "            return true;\n" +
                    "        } else{" +
                    "//拿到浏览器语言\n" +
                    "    var language = navigator.language || navigator.userLanguage;\n" +
                    "    var strMess = \"\";\n" +
                    "    if (language.includes(\"zh\") || language.includes(\"zh-CN\")) {\n" +
                    "        strMess = \"\\u5fc5\\u987b\\u8f93\\u5165\\u6709\\u6548\\u503c\\uff1a \\u6574\\u6905\\u5f00\\u53d1\\u5de5\\u7a0b\\u5e08\";\n" +
                    "    }else {\n" +
                    "        strMess = \"Valid value must be entered: Full chair development engineer\";\n" +
                    "    }" +
                    "alert(strMess)" +
                    "}\n" +
                    "        return false\n" +
                    "    }");
            sb.append(" // 使用 addEventListener 添加第三个 onload 函数\n" +
                    "    window.addEventListener('load', function thirdOnloadHandler() {\n" +
                    "        console.log(\"Third onload handler called.\");\n" +
                    "        document.getElementById('JSApprovePerson').customValidate = checkWCharDevEngineeringIsNotNull\n" +
                    "    }, false);");
            sb.append(" </script>");
        } else if ("create".equals(strMode)) {
            //XSSOK
            sb.append("<input type=\"hidden\" name=\"IsContributorFieldModified\" id=\"IsContributorFieldModified\" value=\"false\" readonly=\"readonly\" />");
            //XSSOK
            sb.append("<input type=\"hidden\" name=\"JSApprovePerson\" id=\"JSApprovePerson\" value=\"" + peronIdList.join(",") + "\" readonly=\"readonly\" />");

            sb.append("<table>");
            sb.append("<tr>");
            sb.append("<th rowspan=\"2\">");
            sb.append("<select name=\"Contributor\" style=\"width:200px\" multiple=\"multiple\">");
            if (peronNameList.size() > 0) {
                for (int i = 0; i < peronNameList.size(); i++) {
                    sb.append("<option value=\"" + peronIdList.get(i) + "\" >");
                    //XSSOK
                    String strPersonName = peronNameList.get(i);
                    strPersonName = StringEscapeUtils.escapeHtml4(strPersonName);
                    sb.append(strPersonName);
                    sb.append("</option>");
                }
            }
            sb.append("</select>");
            sb.append("</th>");
            sb.append("<td>");
            sb.append("<a href=\"javascript:addJSApprovePersonContributor()\">");
            sb.append("<img src=\"../common/images/iconStatusAdded.gif\" width=\"12\" height=\"12\" border=\"0\" />");
            sb.append("</a>");
            sb.append("<a href=\"javascript:addJSApprovePersonContributor()\">");
            //XSSOK
            sb.append(strAddButton);
            sb.append("</a>");
            //sb.append("</div>");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("<tr>");
            sb.append("<td>");
            sb.append("<a href=\"javascript:removeJSApprovePerson()\">");
            sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
            sb.append("</a>");
            sb.append("<a href=\"javascript:removeJSApprovePerson()\">");
            //XSSOK
            sb.append(strRemoveButton);
            sb.append("</a>");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</table>");
            sb.append("<script language=\"JavaScript\">");
            sb.append("function checkWCharDevEngineeringIsNotNull(){\n" +
                    "        const strContributorHidden = document.getElementById(\"JSApprovePerson\").value;\n" +
                    "        if (strContributorHidden){\n" +
                    "            return true;\n" +
                    "        } else{" +
                    "//拿到浏览器语言\n" +
                    "    var language = navigator.language || navigator.userLanguage;\n" +
                    "    var strMess = \"\";\n" +
                    "    if (language.includes(\"zh\") || language.includes(\"zh-CN\")) {\n" +
                    "        strMess = \"\\u5fc5\\u987b\\u8f93\\u5165\\u6709\\u6548\\u503c\\uff1a \\u6574\\u6905\\u5f00\\u53d1\\u5de5\\u7a0b\\u5e08\";\n" +
                    "    }else {\n" +
                    "        strMess = \"Valid value must be entered: Full chair development engineer\";\n" +
                    "    }" +
                    "alert(strMess)" +
                    "}\n" +
                    "        return false\n" +
                    "    }");
            sb.append(" // 使用 addEventListener 添加第三个 onload 函数\n" +
                    "    window.addEventListener('load', function thirdOnloadHandler() {\n" +
                    "        console.log(\"Third onload handler called.\");\n" +
                    "        document.getElementById('JSApprovePerson').customValidate = checkWCharDevEngineeringIsNotNull\n" +
                    "    }, false);");
            sb.append(" </script>");
        }
        return sb.toString();
    }

    /**
     * @param projectList
     * @return java.lang.String
     * @throws
     * @description 够着带链接的受影响项目
     * @author CHENYAN
     * @date 2024/9/6 16:18
     */
    public String buildLinkProjectHtml(MapList projectList) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < projectList.size(); i++) {
            Map project = (Map) projectList.get(i);
            String strProjectId = (String) project.get(SELECT_ID);
            String strProjectName = (String) project.get(SELECT_NAME);
            sb.append(" <a href=\"JavaScript:emxFormLinkClick(&quot;../common/emxTree.jsp?objectId=");
            sb.append(strProjectId);
            sb.append("&amp;relId=null&quot;, &quot;content&quot;, &quot;&quot;, &quot;&quot;, &quot;&quot;, &quot;");
            sb.append(strProjectName);
            sb.append("&quot;, &quot;&quot;, &quot;&quot;)\" class=\"object\">" + strProjectName + "</a>");
            if (i != (projectList.size() - 1)) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description 构建ECR受影响项目列
     * @author CHENYAN
     * @date 2024/9/6 16:17
     */

    public String buildMultipleChoiceProjectHtml(Context context, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        //添加移除文字提示
        String strAddButton = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Button.AddWCharDevEngineering");
        String strRemoveButton = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Button.RemoveWCharDevEngineering");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strMode = (String) requestMap.get("mode");
        String strObjectId = (String) requestMap.get("objectId");
        _logger.info("strMode:{}", strMode);
        _logger.info("strObjectId:{}", strObjectId);
        String strLoginUser = context.getUser();
        StringList projectIdList = new StringList();
        StringList projectNameList = new StringList();
        if (UIUtil.isNotNullAndNotEmpty(strObjectId)) {
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            StringList busSelectList = new StringList();
            busSelectList.add(SELECT_CURRENT);
            busSelectList.add(SELECT_OWNER);
            busSelectList.add(SELECT_ATTR_JFAssociatedOtherProjects);
            Map ecrInfo = ecr.getInfo(context, busSelectList);
            String strCurrent = (String) ecrInfo.get(SELECT_CURRENT);
            String strOwner = (String) ecrInfo.get(SELECT_OWNER);
            String strAssociatedOtherProjects = (String) ecrInfo.get(SELECT_ATTR_JFAssociatedOtherProjects);
            StringList reSelectList = new StringList();
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFROUTENOTE_APPROVALAROLE);
            StringList typeSelectList = new StringList();
            typeSelectList.add(SELECT_ID);
            typeSelectList.add(SELECT_NAME);
            MapList maps;
            try {
                ContextUtil.pushContext(context);
                maps = ecr.getRelatedObjects(context, "JFECR2AffectedProject", // relationship pattern
                        TYPE_PROJECT_SPACE,                                    // object pattern
                        typeSelectList,                            // object selects
                        reSelectList, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 1,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
            } finally {
                ContextUtil.popContext(context);
            }

            //  "create".equalsIgnoreCase(strMode)
            // 整椅经理只有草稿、和创建人可以修改
            if (UIUtil.isNotNullAndNotEmpty(strMode)) {
                if ("edit".equalsIgnoreCase(strMode)) {
                    if ((!"Create".equals(strCurrent)) || (!strLoginUser.equals(strOwner))) {
                        return buildLinkProjectHtml(maps);
                    }
                } else if ("view".equalsIgnoreCase(strMode)) {
                    return buildLinkProjectHtml(maps);
                }
            }

            for (int i = 0; i < maps.size(); i++) {
                Map project = (Map) maps.get(i);
                String strProjectId = (String) project.get(SELECT_ID);
                String strProjectName = (String) project.get(SELECT_NAME);
                projectIdList.add(strProjectId);
                projectNameList.add(strProjectName);
            }
            //XSSOK
            sb.append("<input type=\"hidden\" name=\"JFAffectedProjectFieldModified\" id=\"JFAffectedProjectFieldModified\" value=\"false\" readonly=\"readonly\" />");
            //XSSOK
            sb.append("<input type=\"hidden\" name=\"JFAffectedProject\" id=\"JFAffectedProject\" value=\"" + projectIdList.join(",") + "\" readonly=\"readonly\" />");

            sb.append("<table>");
            sb.append("<tr>");
            sb.append("<th rowspan=\"2\">");
            sb.append("<select name=\"JFAffectedProjectSelect\" style=\"width:200px\" multiple=\"multiple\">");
            if (maps.size() > 0) {
                for (int i = 0; i < projectIdList.size(); i++) {
                    sb.append("<option value=\"" + projectIdList.get(i) + "\" >");
                    //XSSOK
                    String strProjectName = projectNameList.get(i);
                    strProjectName = StringEscapeUtils.escapeHtml4(strProjectName);
                    sb.append(strProjectName);
                    sb.append("</option>");
                }
            }
            sb.append("</select>");
            sb.append("</th>");
            sb.append("<td>");
            sb.append("<a href=\"javascript:addJSAffectedProject()\">");
            sb.append("<img src=\"../common/images/iconStatusAdded.gif\" width=\"12\" height=\"12\" border=\"0\" />");
            sb.append("</a>");
            sb.append("<a href=\"javascript:addJSAffectedProject()\">");
            //XSSOK
            sb.append(strAddButton);
            sb.append("</a>");
            //sb.append("</div>");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("<tr>");
            sb.append("<td>");
            sb.append("<a href=\"javascript:removeJSAffectedProject()\">");
            sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
            sb.append("</a>");
            sb.append("<a href=\"javascript:removeJSAffectedProject()\">");
            //XSSOK
            sb.append(strRemoveButton);
            sb.append("</a>");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</table>");
            sb.append("<script language=\"JavaScript\">");
            sb.append("function checkJSAffectedProjectIsNotNull(){\n" +
                    "var strIsAssociatedOtherProject = emxFormGetValue(\"JFAssociatedOtherProjects\").current.actual;" +
                    "            if (\"N\" === strIsAssociatedOtherProject){" +
                    "return true;" +
                    "}" +
                    "        const strContributorHidden = document.getElementById(\"JFAffectedProject\").value;\n" +
                    "        if (strContributorHidden){\n" +
                    "            return true;\n" +
                    "        } else{" +
                    "//拿到浏览器语言\n" +
                    "    var language = navigator.language || navigator.userLanguage;\n" +
                    "    var strMess = \"\";\n" +
                    "    if (language.includes(\"zh\") || language.includes(\"zh-CN\")) {\n" +
                    "        strMess = \"\u5fc5\u987b\u8f93\u5165\u6709\u6548\u503c\uff1a \u53d7\u5f71\u54cd\u9879\u76ee\uff01\"" +
                    "    }else {\n" +
                    "        strMess = \"Valid value must be entered: Affected Projects\";\n" +
                    "    }" +
                    "alert(strMess)" +
                    "}\n" +
                    "        return false\n" +
                    "    }");
            sb.append(" // 使用 addEventListener 添加第三个 onload 函数\n" +
                    "    window.addEventListener('load', function thirdOnloadHandler() {\n" +
                    "        console.log(\"Third onload handler called.\");\n" +
                    "        document.getElementById('JFAffectedProject').customValidate = checkJSAffectedProjectIsNotNull\n" +
                    "    }, false);");
            sb.append(" </script>");

            if ("Y".equals(strAssociatedOtherProjects)) {
                sb.append("<script>");
                sb.append("  var  dom = document.getElementById('");
                String strDomId = "calc_JFAffectedProject";
                sb.append(strDomId);
                sb.append("');");
                sb.append("  if (dom){");
                sb.append("    dom.querySelector('td:first-child').className = \"createLabelRequired\";");
                sb.append("  }");
                sb.append("</script>");
            }
        } else if ("create".equals(strMode)) {
            //XSSOK
            sb.append("<input type=\"hidden\" name=\"JFAffectedProjectFieldModified\" id=\"JFAffectedProjectFieldModified\" value=\"false\" readonly=\"readonly\" />");
            //XSSOK
            sb.append("<input type=\"hidden\" name=\"JFAffectedProject\" id=\"JFAffectedProject\" value=\"" + projectIdList.join(",") + "\" readonly=\"readonly\" />");

            sb.append("<table>");
            sb.append("<tr>");
            sb.append("<th rowspan=\"2\">");
            sb.append("<select name=\"JFAffectedProjectSelect\" style=\"width:200px\" multiple=\"multiple\">");
            if (projectIdList.size() > 0) {
                for (int i = 0; i < projectNameList.size(); i++) {
                    sb.append("<option value=\"" + projectIdList.get(i) + "\" >");
                    //XSSOK
                    String strProjectName = projectNameList.get(i);
                    strProjectName = StringEscapeUtils.escapeHtml4(strProjectName);
                    sb.append(strProjectName);
                    sb.append("</option>");
                }
            }
            sb.append("</select>");
            sb.append("</th>");
            sb.append("<td>");
            sb.append("<a href=\"javascript:addJSAffectedProject()\">");
            sb.append("<img src=\"../common/images/iconStatusAdded.gif\" width=\"12\" height=\"12\" border=\"0\" />");
            sb.append("</a>");
            sb.append("<a href=\"javascript:addJSAffectedProject()\">");
            //XSSOK
            sb.append(strAddButton);
            sb.append("</a>");
            //sb.append("</div>");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("<tr>");
            sb.append("<td>");
            sb.append("<a href=\"javascript:removeJSAffectedProject()\">");
            sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
            sb.append("</a>");
            sb.append("<a href=\"javascript:removeJSAffectedProject()\">");
            //XSSOK
            sb.append(strRemoveButton);
            sb.append("</a>");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</table>");
            sb.append("<script language=\"JavaScript\">");
            sb.append("function checkJSAffectedProjectIsNotNull(){\n" +
                    "var strIsAssociatedOtherProject = emxFormGetValue(\"JFAssociatedOtherProjects\").current.actual;" +
                    "            if (\"N\" === strIsAssociatedOtherProject){" +
                    "return true;" +
                    "}" +
                    "        const strContributorHidden = document.getElementById(\"JFAffectedProject\").value;\n" +
                    "        if (strContributorHidden){\n" +
                    "            return true;\n" +
                    "        } else{" +
                    "//拿到浏览器语言\n" +
                    "    var language = navigator.language || navigator.userLanguage;\n" +
                    "    var strMess = \"\";\n" +
                    "    if (language.includes(\"zh\") || language.includes(\"zh-CN\")) {\n" +
                    "        strMess = \"\u5fc5\u987b\u8f93\u5165\u6709\u6548\u503c\uff1a \u53d7\u5f71\u54cd\u9879\u76ee\uff01\"" +
                    "    }else {\n" +
                    "        strMess = \"Valid value must be entered: Affected Projects\";\n" +
                    "    }" +
                    "alert(strMess)" +
                    "}\n" +
                    "        return false\n" +
                    "    }");
            sb.append(" // 使用 addEventListener 添加第三个 onload 函数\n" +
                    "    window.addEventListener('load', function thirdOnloadHandler() {\n" +
                    "        console.log(\"Third onload handler called.\");\n" +
                    "        document.getElementById('JFAffectedProject').customValidate = checkJSAffectedProjectIsNotNull\n" +
                    "    }, false);");
            sb.append(" </script>");
        }
        return sb.toString();
    }


    public void updateFieldInForm(Context context, String[] args) {

    }

    public StringList buildECRTableColHtml(Context context, String[] args) throws Exception {
        _logger.info("------------------------------------------  buildECRTableColHtml  begin ------------------------------------------");
        Map paramsMap = (Map) JPO.unpackArgs(args);
        _logger.info("paramsMap:{}", paramsMap);
        StringList tableIdList = _getListOfKeys(args, SELECT_JFECR2PartPriceID);
        _logger.info("tableIdList:{}", tableIdList);
        StringList res = new StringList();
        for (int i = 0; i < tableIdList.size(); i++) {
            String strID = tableIdList.get(i);
            if (UIUtil.isNotNullAndNotEmpty(strID)) {
                DomainRelationship rel = DomainRelationship.newInstance(context, strID);
                String strFieldValue = rel.getAttributeValue(context, "JFChangeUnitPrice");
                res.add(strFieldValue);
            } else {
                res.add("");
            }
        }
        return res;
    }


    /**
     * @param context
     * @param objectList
     * @param roleList
     * @param strECRId
     * @param approveStateList
     * @return matrix.util.StringList
     * @throws
     * @description 获取ECR中指定角色指定状态下的Table列编辑权限
     * @author CHENYAN
     * @date 2024/8/9 9:22
     */
    public static StringList getTableEditAccessInLoginRole(Context context, MapList objectList, MapList roleList, String strECRId, StringList approveStateList, String strFieldName) throws Exception {
        StringList res = new StringList(objectList.size());
        //角色列表为空全返回false
        DomainObject ecr = DomainObject.newInstance(context, strECRId);
        StringList ecrBusSelectList = new StringList();
        ecrBusSelectList.add(SELECT_CURRENT);
        ecrBusSelectList.add(SELECT_OWNER);
        ecrBusSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
        Map ecrInfo = ecr.getInfo(context, ecrBusSelectList);
        String strCurrent = (String) ecrInfo.get(SELECT_CURRENT);
        String strECRChangeSource = (String) ecrInfo.get(SELECT_ATTR_JFCHANGESOURCE);
        if (null == roleList || roleList.size() == 0) {
            for (int i = 0; i < objectList.size(); i++) {
                res.add(Boolean.FALSE.toString());
            }
        }
        // 一级件 的采购类型为 Buy、ICO 只有一级件有编辑权限 ，下面子件全不可编辑 编辑权限由下面的子件控制（存在Both 全都开发 ，内部开放内部 ，外部开发外部）
        //根据一级件进行分组
        //一级件保存集合 key id  value map
        Map oneLevelMap = new HashMap();
        Map<String, String> roleMap = new HashMap<>();
        Map group = (Map) objectList.stream().filter(m -> {
            Map infoMap = (Map) m;
            String strRelationship = UIUtil.getValue(infoMap, "relationship");
            String strId = UIUtil.getValue(infoMap, SELECT_ID);
            _logger.info("strRelationship:{}", strRelationship);
            //如果是一级件下面零件需要判断关系属性ECRName是否保存的当前ECR信息
            if (JF_PLMConstants_mxJPO.REL_JFECR2OnelevelPart.equals(strRelationship)) {
                oneLevelMap.put(strId, m);
            }
            return JF_PLMConstants_mxJPO.REL_JFOneLevelPart2Part.equals(strRelationship) ? true : false;
        }).collect(Collectors.groupingBy(m -> {
            Map infoMap = (Map) m;
            return infoMap.get(SELECT_FROM_ID);
        }));
        //获取到role name
        StringList roleNameList = (StringList) roleList.stream().map(m -> {
            Map roleInfo = (Map) m;
            return roleInfo.get("role");
        }).collect(Collectors.toCollection(StringList::new));
        //  oneLevelMap 和 group 的key oneLevelMap 大于等于 group的key ，一个保存的是 一级件Map ，一个保存一级件下面子件
        // 遍历所有一级件
        for (Object entry : oneLevelMap.entrySet()) {
            Map.Entry entryMap = (Map.Entry) entry;
            String strKey = (String) entryMap.getKey();
            Map onePart = (Map) entryMap.getValue();
            String strProcurementType = (String) onePart.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
            //采购类型为空走make的逻辑
            if (UIUtil.isNullOrEmpty(strProcurementType)) {
                strProcurementType = JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE;
            }
            String strDirectBuy = (String) onePart.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
            //默认值给Both
            String strChangeSource = "";
            if (group.containsKey(strKey)) {
                //一级件存在受影响对象 变更来源取子件
                List sunList = (List) group.get(strKey);
                Set changeSourceSet = (Set) sunList.stream().map(m -> {
                    Map sun = (Map) m;
                    return sun.get(JF_PLMConstants_mxJPO.TO_REL_JFRELATEITEM_JFChangeSource);
                }).collect(Collectors.toSet());
                if (changeSourceSet.contains(JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH)) {
                    strChangeSource = JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH;
                } else if (changeSourceSet.size() == 1) {
                    for (Object changeSource : changeSourceSet) {
                        String strChangeSourceTemp = (String) changeSource;
                        strChangeSource = strChangeSourceTemp;
                    }
                }
            } else {
                //一级件不存在受影响对象 变更来源取本身 如果本身没有的话取ECR
                String strChangeSourceTemp = (String) onePart.get(JF_PLMConstants_mxJPO.TO_REL_JFRELATEITEM_JFChangeSource);
                strChangeSource = strChangeSourceTemp;
            }
            //匹配不到给默认值
            if (UIUtil.isNullOrEmpty(strChangeSource)) {
                _logger.info("---------------------------- 授予默认值 -------------------------------");
                strChangeSource = strECRChangeSource;
            }
            _logger.info("strChangeSource:{} strCurrent:{} strDirectBuy:{} strProcurementType:{}", strChangeSource, strCurrent, strDirectBuy, strProcurementType);
            // 编辑权限控制 变更来源 + DirectBuy + 采购类型 + ECR 状态 + 会签任务状态
            boolean isEdit = getFieldEditAccessByChangeSource(strChangeSource, strFieldName);
            //ECR状态下是否可编辑 会签任务是否可编辑 审核中已完成不可编辑
            boolean isEditInCurrent = getFieldIsEditAccessByFieldName_State_Role(strFieldName, strCurrent, roleList);
            boolean hasRole = false;
            StringList editRoleList = getEditRoleInFieldName(strFieldName, strDirectBuy, strProcurementType);

            if (editRoleList.size() > 0) {
                for (int i = 0; i < editRoleList.size(); i++) {
                    String strRole = editRoleList.get(i);
                    hasRole = roleNameList.contains(strRole);
                    break;
                }
            }
            if (isEdit && hasRole && isEditInCurrent) {
                // 通过状态判断是否有权限
                roleMap.put(strKey, Boolean.TRUE.toString());
            } else {
                roleMap.put(strKey, Boolean.FALSE.toString());
            }
        }
        for (int i = 0; i < objectList.size(); i++) {
            Map obj = (Map) objectList.get(i);
            String strId = (String) obj.get(SELECT_ID);
            String strFromId = (String) obj.get(SELECT_FROM_ID);
            String strRelName = (String) obj.get("relationship");
            String strParentAttr = (String) obj.get(JF_PublicMethodClass_mxJPO.buildStringInStrings("from.", JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType));
            // 判断一级件属性是 Buy ICO 如果是 那么一级件才能编辑 采取判断能不能编辑
            // 判断一级件属性是 Make 一级件不能编辑 自件可以编辑 采取判断能不能编辑
            if (roleMap.containsKey(strId) && JF_PLMConstants_mxJPO.REL_JFECR2OnelevelPart.equals(strRelName)) {
                String strProcurementType = (String) obj.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                //采购类型为空走make的逻辑
                if (UIUtil.isNullOrEmpty(strProcurementType)) {
                    strProcurementType = JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE;
                }
                if ((JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType) ||
                        JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_ICO.equals(strProcurementType))) {
                    res.add(roleMap.get(strId));
                } else {
                    res.add(Boolean.FALSE.toString());
                }
            } else if (roleMap.containsKey(strFromId) && JF_PLMConstants_mxJPO.REL_JFOneLevelPart2Part.equals(strRelName)) {
                Map onePart = (Map) oneLevelMap.get(strFromId);
                //一级件 采购类型
                String strProcurementType = (String) onePart.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                //采购类型为空走make的逻辑
                if (UIUtil.isNullOrEmpty(strProcurementType)) {
                    strProcurementType = JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE;
                }
                //子件采购类型
                String strObjProcurementType = (String) obj.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                //采购类型为空走make的逻辑
                if (UIUtil.isNullOrEmpty(strObjProcurementType)) {
                    strObjProcurementType = JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE;
                }
                // add by chenyan 2024 一级件为Make时， 子件为Buy 或者ICO 可以编辑
                // add by chenyan 2024 一级件为Buy ICO时， 子件不可以编辑
                if (JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)) {
                    if (!JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE.equals(strObjProcurementType)) {
                        String strChangeSource = (String) obj.get(JF_PLMConstants_mxJPO.TO_REL_JFRELATEITEM_JFChangeSource);
                        String strDirectBuy = (String) obj.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
                        _logger.info("strChangeSource:{} strCurrent:{} strDirectBuy:{} strProcurementType:{}", strChangeSource, strCurrent, strDirectBuy, strProcurementType);
                        // 编辑权限控制 变更来源 + DirectBuy + 采购类型 + ECR 状态 + 会签任务状态
                        boolean isEdit = getFieldEditAccessByChangeSource(strChangeSource, strFieldName);
                        _logger.info("isEdit:{}", isEdit);
                        //ECR状态下是否可编辑 会签任务是否可编辑 审核中已完成不可编辑
                        _logger.info("strFieldName:{} strCurrent ：{}", strFieldName, strCurrent);
                        boolean isEditInCurrent = getFieldIsEditAccessByFieldName_State_Role(strFieldName, strCurrent, roleList);
                        _logger.info("isEditInCurrent:{}", isEditInCurrent);
                        boolean hasRole = false;
                        StringList editRoleList = getEditRoleInFieldName(strFieldName, strDirectBuy, strObjProcurementType);
                        _logger.info("roleNameList:{}", roleNameList);
                        if (editRoleList.size() > 0) {
                            for (int k = 0; k < editRoleList.size(); k++) {
                                String strRole = editRoleList.get(k);
                                hasRole = roleNameList.contains(strRole);
                                break;
                            }
                        }
                        _logger.info("editRoleList:{}", editRoleList);
                        if (isEdit && hasRole && isEditInCurrent) {
                            // 通过状态判断是否有权限
                            res.add(Boolean.TRUE.toString());
                        } else {
                            res.add(Boolean.FALSE.toString());
                        }
                    } else {
                        res.add(Boolean.FALSE.toString());
                    }
                } else {
                    res.add(Boolean.FALSE.toString());
                }
            } else {
                res.add(Boolean.FALSE.toString());
            }
        }
        _logger.info("res:{}", res);
        return res;
    }

    /**
     * @param strChangeSource
     * @param strFieldName
     * @return boolean
     * @throws
     * @description 根据变更来源获取部分属性内外部编辑权限
     * @author CHENYAN
     * @date 2024/11/26 14:10
     */

    public static boolean getFieldEditAccessByChangeSource(String strChangeSource, String strFieldName) {
        boolean res = false;
        // add by chenyan 2024/11/18 目标售价不分区内外部
        if ("JFChangeTargetPrice".equals(strFieldName)) {
            return true;
        }
        switch (strFieldName) {
            //内部 二则
            case "JFChangeUnitPrice", "JFChangeMold", "JFChangeUnitPriceCost", "JFChangeMoldCost",
                    "JFChangeMan-hour", "JFChangeSeatCost", "JFChangeTargetPrice", "JFStagnationOfSuppliersInternal",
                    "JFChangesTrialExpensesManHours": {
                if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource) || JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                    res = true;
                }
                break;
            }
            //外部 二则
            case "JFChangeUnitPriceExternal", "JFChangeMoldCostExternal", "JFChangeUnitPriceCostExternal", "JFChangeMoldPriceCostExternal",
                    "JFChangeMan-hourExternal", "JFChangeSeatCostExternal", "JFChangeTargetPriceExternal", "JFStagnationOfSuppliersExternal",
                    "JFChangesTrialExpensesManHoursExternal": {
                if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource) || JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                    res = true;
                }
                break;
            }
        }
        return res;
    }

    public static boolean getFieldIsEditAccessByFieldName_State_Role(String strFieldName, String strCurrent, MapList roleList) {
        boolean res = false;
        if (UIUtil.isNotNullAndNotEmpty(strFieldName) && UIUtil.isNotNullAndNotEmpty(strCurrent) && (null != roleList && roleList.size() > 0)) {
            for (int i = 0; i < roleList.size(); i++) {
                Map roleMap = (Map) roleList.get(i);
                String strRoleName = (String) roleMap.get("role");
                String strSignTaskCurrent = (String) roleMap.get(SELECT_CURRENT);
                if (JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_FinancialBP.equals(strRoleName)) {
                    if ("APR".equals(strCurrent) && ("Create".equals(strSignTaskCurrent) || "Assign".equals(strSignTaskCurrent) || "Active".equals(strSignTaskCurrent))) {
                        switch (strFieldName) {
                            case JF_PLMConstants_mxJPO.ATTR_JFChangeSeatCost,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeSeatCostExternal,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeTargetPrice,
//                                    JF_PLMConstants_mxJPO.ATTR_JFChangeTargetPriceExternal,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPrice,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeMold,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceExternal,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCostExternal: {
                                res = true;
                                return res;
                            }
                        }
                    }
                    // mod by chenyan 2024/09/15 商务经理会签状态不再填写采购件清单（转交给采购填写）
                } else if (JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_InternalSupplier.equals(strRoleName)) {
                    if (("Countersign".equals(strCurrent) || "APR".equals(strCurrent)) && ("Create".equals(strSignTaskCurrent) || "Assign".equals(strSignTaskCurrent) || "Active".equals(strSignTaskCurrent))) {
                        switch (strFieldName) {
                            case JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPrice,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeMold,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceExternal,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCostExternal,
                                    JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersInternal,
                                    JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersExternal,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHours,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHoursExternal: {
                                res = true;
                                return res;
                            }
                        }
                    }
                } else if (JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_PRR.equals(strRoleName)) {
                    if (("Countersign".equals(strCurrent) || "APR".equals(strCurrent)) && ("Create".equals(strSignTaskCurrent) || "Assign".equals(strSignTaskCurrent) || "Active".equals(strSignTaskCurrent))) {
                        switch (strFieldName) {
                            case JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPrice,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeMold,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceExternal,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCostExternal,
                                    JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersInternal,
                                    JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersExternal,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHours,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHoursExternal: {
                                res = true;
                                return res;
                            }
                        }
                    }
                } else if (JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_Costing.equals(strRoleName)) {
                    if (("Countersign".equals(strCurrent) || "APR".equals(strCurrent)) && ("Create".equals(strSignTaskCurrent) || "Assign".equals(strSignTaskCurrent) || "Active".equals(strSignTaskCurrent))) {
                        switch (strFieldName) {
                            case JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceCost,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCost,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceCostExternal,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeMoldPriceCostExternal: {
                                res = true;
                                return res;
                            }
                        }
                    }
                } else if (JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_AME.equals(strRoleName)) {
                    if (("Countersign".equals(strCurrent) || "APR".equals(strCurrent)) && ("Create".equals(strSignTaskCurrent) || "Assign".equals(strSignTaskCurrent) || "Active".equals(strSignTaskCurrent))) {
                        switch (strFieldName) {
                            case JF_PLMConstants_mxJPO.ATTR_JFChangeManHour,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeManHour_External: {
                                res = true;
                                return res;
                            }
                        }
                    }
                } else if (JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_AME.equals(strRoleName)) {
                    if (("Countersign".equals(strCurrent) || "APR".equals(strCurrent)) && ("Create".equals(strSignTaskCurrent) || "Assign".equals(strSignTaskCurrent) || "Active".equals(strSignTaskCurrent))) {
                        switch (strFieldName) {
                            case JF_PLMConstants_mxJPO.ATTR_JFChangeManHour,
                                    JF_PLMConstants_mxJPO.ATTR_JFChangeManHour_External: {
                                res = true;
                                return res;
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    /**
     * @param strTableName       table 名称
     * @param strDirectBuy       零件属性 Y N
     * @param strProcurementType 零件属性 make buy ICO
     * @param strChangeSource    内部 外部 二则
     * @param strCurrent         ECR 当前状态
     * @param roleMapList        roleMap集合
     * @return matrix.util.StringList  attribute[xxxx] 必填属性集合
     * @throws
     * @description 根据角色、ECR状态，DirectBuy 采购类型 ,变更来源
     * @author CHENYAN
     * @date 2024/8/9 10:43
     */
    public static StringList getRequireEditFieldInRole_State(String strTableName, String strDirectBuy, String strProcurementType, String strChangeSource, String strCurrent, MapList roleMapList) {
        StringList res = new StringList();
        for (int i = 0; i < roleMapList.size(); i++) {
            Map roleMap = (Map) roleMapList.get(i);
            String strRole = (String) roleMap.get("role");
            StringList requireFieldList = getRequireEditFieldInRoleAndLinkField(strCurrent, strRole, strDirectBuy, strProcurementType, strChangeSource, "");
            res.addAll(requireFieldList);
        }
        _logger.info("requireField res :{}", res);
        return res;
    }

    /**
     * @param strFieldName       属性名称
     * @param strDirectBuy       零件属性 Y N
     * @param strProcurementType 零件属性 make buy ICO
     * @return java.lang.String
     * @throws
     * @description 根据属性名称， directbuy 和采购类型 获取到到属性可以编辑的角色信息
     * @author CHENYAN
     * @date 2024/8/9 10:08
     */
    public static StringList getEditRoleInFieldName(String strFieldName, String strDirectBuy, String strProcurementType) {
        StringList roleList = new StringList();
        switch (strFieldName) {
            //模具费变更 单价变更
            case "JFChangeUnitPrice",
                    "JFChangeMold",
                    "JFChangeUnitPriceExternal",
                    "JFChangeMoldCostExternal",
                    JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersInternal,
                    JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersExternal,
                    JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHours,
                    JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHoursExternal: {
                //mod by chenyan 2024/09/15 商务经理填写属性转交给采购
                if ("Y".equals(strDirectBuy)) {
                    roleList.add(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_PRR);
                } else {
                    if (JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType)) {
                        roleList.add(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_PRR);
                    } else if (JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_ICO.equals(strProcurementType)) {
                        roleList.add(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_InternalSupplier);
                    }
                }
                break;
            }
            //单价变更（成本） 模具费变更（成本）
            case "JFChangeUnitPriceCost", "JFChangeMoldCost", "JFChangeUnitPriceCostExternal", "JFChangeMoldPriceCostExternal": {
                // mod by chenyan 当采购类型为ICO的时候不在需要Costing填写这四个属性
                if (JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType)) {
                    roleList.add(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_Costing);
                }
                break;
            }
            case "JFChangeMan-hour", "JFChangeMan-hourExternal": {
                roleList.add(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_AME);
                break;
            }
            case "JFChangeSeatCost", "JFChangeSeatCostExternal", "JFChangeTargetPrice", "JFChangeTargetPriceExternal": {
                roleList.add(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_FinancialBP);
                break;
            }
        }
        return roleList;
    }

    /**
     * @param strCurrent
     * @param strRole
     * @param strDirectBuy
     * @param strProcurementType
     * @param strChangeSource
     * @param strTableName
     * @return matrix.util.StringList
     * @throws
     * @description 根据角色和联动属性获取table中必填属性
     * @author CHENYAN
     * @date 2024/8/9 13:25
     */
    public static StringList getRequireEditFieldInRoleAndLinkField(String strCurrent, String strRole, String strDirectBuy, String strProcurementType, String strChangeSource, String strTableName) {
        StringList res = new StringList();
//        _logger.info("strCurrent:{} strRole:{} strDirectBuy:{} strProcurementType:{} strChangeSource:{}",strCurrent,strRole,strDirectBuy,strProcurementType,strChangeSource);
        switch (strRole) {
            //财务BP
            case JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_FinancialBP: {
                if (("Countersign".equals(strCurrent) || "APR".equals(strCurrent)) && "JFECRCosting".equals(strTableName)) {
                    // Costing 表格属性 采购件
                    if (JF_PLMConstants_mxJPO.ATTR_ATTR_JFDIRECT_BUY_RANGE_N.equals(strDirectBuy) && JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)) {
//                        if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)){
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPrice);
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMold);
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersInternal);
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHours);
//                        }else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)){
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceExternal);
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCostExternal);
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersExternal);
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHoursExternal);
//                        }else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)){
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPrice);
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMold);
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersInternal);
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHours);
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceExternal);
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCostExternal);
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersExternal);
//                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHoursExternal);
//                        }
                    }
                } else if ("APR".equals(strCurrent) && "JFECRController".equals(strTableName)) {
                    if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)) {
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeSeatCost);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeTargetPrice);
                    } else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)) {
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeSeatCostExternal);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeTargetPrice);
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeTargetPriceExternal);
                    } else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeSeatCost);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeTargetPrice);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeSeatCostExternal);
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeTargetPriceExternal);
                    }
                }
                break;
            }
            // mod by chenyan 商务代表必填属性转移给采购
            //商务代表
            case JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_BU: {
//                if(("Countersign".equals(strCurrent) || "APR".equals(strCurrent)) && JF_PLMConstants_mxJPO.ATTR_ATTR_JFDIRECT_BUY_RANGE_Y.equals(strDirectBuy)
//                        && (JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType) || JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_ICO.equals(strProcurementType))
//                        &&  "JFECRCosting".equals(strTableName)){
//                    if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)){
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPrice);
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMold);
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersInternal);
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHours);
//                    }else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)){
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceExternal);
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCostExternal);
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersExternal);
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHoursExternal);
//                    }else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)){
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPrice);
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMold);
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersInternal);
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHours);
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceExternal);
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCostExternal);
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersExternal);
//                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHoursExternal);
//                    }
//                }
                break;
            }
            //AME
            case JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_AME: {
                if (("Countersign".equals(strCurrent) || "APR".equals(strCurrent)) && "JFECRController".equals(strTableName)) {
                    if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)) {
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeManHour);
                    } else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)) {
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeManHour_External);
                    } else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeManHour);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeManHour_External);
                    }
                }
                break;
            }
            //Costing
            case JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_Costing: {
                //采购件table中属性
                if (("Countersign".equals(strCurrent) || "APR".equals(strCurrent)) && "JFECRCosting".equals(strTableName)) {
                    if (JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType)) {
                        if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)) {
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceCost);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCost);
                        } else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)) {
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceCostExternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMoldPriceCostExternal);
                        } else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceCost);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCost);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceCostExternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMoldPriceCostExternal);
                        }
                    }
                }
                break;
            }
            //采购代表
            case JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_PRR: {
                if (("Countersign".equals(strCurrent) || "APR".equals(strCurrent)) && "JFECRCosting".equals(strTableName)) {
                    if ((JF_PLMConstants_mxJPO.ATTR_ATTR_JFDIRECT_BUY_RANGE_Y.equals(strDirectBuy)||JF_PLMConstants_mxJPO.ATTR_ATTR_JFDIRECT_BUY_RANGE_consignment.equals(strDirectBuy)) && (JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType) || JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_ICO.equals(strProcurementType))) {
                        if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)) {
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPrice);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMold);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersInternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHours);
                        } else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)) {
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceExternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCostExternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersExternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHoursExternal);
                        } else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPrice);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMold);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersInternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHours);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceExternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCostExternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersExternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHoursExternal);
                        }
                    } else if (JF_PLMConstants_mxJPO.ATTR_ATTR_JFDIRECT_BUY_RANGE_N.equals(strDirectBuy) && JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType)) {
                        if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)) {
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPrice);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMold);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersInternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHours);
                        } else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)) {
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceExternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCostExternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersExternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHoursExternal);
                        } else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPrice);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMold);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersInternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHours);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceExternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCostExternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersExternal);
                            res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHoursExternal);
                        }
                    }

                }
                break;
            }
            case JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE_Range_InternalSupplier: {
                if (("Countersign".equals(strCurrent) || "APR".equals(strCurrent)) && JF_PLMConstants_mxJPO.ATTR_JF_ProcurementType_RANGE_ICO.equals(strProcurementType) && "JFECRCosting".equals(strTableName) && (!"Y".equals(strDirectBuy))) {
                    if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_InternalChanges.equals(strChangeSource)) {
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPrice);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMold);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersInternal);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHours);
                    } else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_ExternalChanges.equals(strChangeSource)) {
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceExternal);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCostExternal);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersExternal);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHoursExternal);
                    } else if (JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strChangeSource)) {
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPrice);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMold);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersInternal);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHours);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeUnitPriceExternal);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangeMoldCostExternal);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFStagnationOfSuppliersExternal);
                        res.add(JF_PLMConstants_mxJPO.ATTR_JFChangesTrialExpensesManHoursExternal);
                    }
                }
            }
        }
        return res;
    }

    /**
     * @param context
     * @param args
     * @return void
     * @throws
     * @description 当ECR中变更来源更改之后需要同步受影响项的变更来源
     * @author CHENYAN
     * @date 2024/8/23 9:47
     */
    public void setChangeSourceAgreementByECR(Context context, String[] args) throws Exception {
        String strObjectId = args[0];
        String strAttrName = args[1];
        String strAttrValue = args[2];
        String strAttrNewValue = args[3];
        //只有变更来源是否才处理并且变更来源不为Both
        if (ATTR_JFCHANGESOURCE.equals(strAttrName) && (!JF_PLMConstants_mxJPO.ATTR_JFCHANGESOURCE_RANGE_BOTH.equals(strAttrNewValue))) {
            try {
                ContextUtil.pushContext(context);
                ContextUtil.startTransaction(context, true);
                DomainObject ecr = DomainObject.newInstance(context, strObjectId);
                StringList reSelectList = new StringList();
                reSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
                reSelectList.add(SELECT_RELATIONSHIP_ID);
                reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubIFReplace);
                MapList maps = ecr.getRelatedObjects(context, REL_JFRELATEITEM, // relationship pattern
                        TYPE_VPMREFERENCE,                                    // object pattern
                        new StringList(SELECT_ID),                            // object selects
                        reSelectList, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 1,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
                for (int i = 0; i < maps.size(); i++) {
                    Map info = (Map) maps.get(i);
                    String strRelChangeSource = (String) info.get(SELECT_ATTR_JFCHANGESOURCE);
                    String strRelId = (String) info.get(SELECT_RELATIONSHIP_ID);
                    if (!strAttrNewValue.equals(strRelChangeSource)) {
                        DomainRelationship rel = DomainRelationship.newInstance(context, strRelId);
                        rel.setAttributeValue(context, ATTR_JFCHANGESOURCE, strAttrNewValue);
                    }
                }
                ContextUtil.commitTransaction(context);
            } catch (Exception e) {
                _logger.error(e.getMessage());
                ContextUtil.abortTransaction(context);
            } finally {
                ContextUtil.popContext(context);
            }
        }

    }

    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取ECR创建人上传的所有附件
     * @author CHENYAN
     * @date 2024/9/5 9:56
     */
    public MapList getECRFileByOwner(Context context, String[] args) throws Exception {
        Map parameters = JPO.unpackArgs(args);
        String strObjectId = (String) parameters.get("objectId");
        try {
            ContextUtil.pushContext(context);
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
            StringList typeSelectList = new StringList(SELECT_ID);
            MapList maps = ecr.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                    TYPE_DOCUMENT,                                    // object pattern
                    typeSelectList,                            // object selects
                    reSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "to.owner==from.owner&&(attribute[Project Role]==''|| attribute[Project Role]==QQ || attribute[Project Role]=='Questions List')",
                    (short) 0);
            return maps;
        } finally {
            ContextUtil.popContext(context);
        }
    }

    /**
     * @param context
     * @param args
     * @return int
     * @throws
     * @description 检查ECR是否上传附件
     * @author CHENYAN
     * @date 2024/11/26 14:09
     */
    public int checkECRHasDocument(Context context, String[] args) throws Exception {
        _logger.info("------------------------ checkECRHasDocument begin  -----------------------------------");
        String strObjectId = args[0];
        String strCurrent = args[1];
        String strNextState = args[2];
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strCurrent:{}", strCurrent);
        _logger.info("strNextState:{}", strNextState);
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        StringList typeSelectList = new StringList(SELECT_ID);
        int iRes = 0;
        if ("Create".equals(strCurrent) && "Submit".equals(strNextState)) {
            MapList maps = ecr.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                    TYPE_DOCUMENT,                                    // object pattern
                    typeSelectList,                            // object selects
                    reSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "to.owner==from.owner&&attribute[Project Role]==''",
                    (short) 0);
            if (maps.size() <= 0) {
                iRes = 1;
                String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECR.DocumentIsRequire");
                emxContextUtilBase_mxJPO.mqlWarning(context, strMess);
            }
        }
        _logger.info("------------------------ checkECRHasDocument end  -----------------------------------");
        return iRes;
    }

    /**
     * 检验   在ECR创建提升到工作中增加检查Trigger，检查受影响件的父件目前是否是被打了快照---需要快照
     *
     * @param context
     * @param args
     * @return int
     * @throws
     * @author LIUJR
     * @date 2025/3/28 13:45
     * @description
     */
    public int checkECRParentPartHasSnapshot(Context context, String[] args) throws Exception {
        _logger.info("------------------------ checkECRParentPartHasSnapshot begin  -----------------------------------");
        /*
        *  校验逻辑
            1. 通过ECR找到关联项目、和影响项目，在找到这些项目关联的冻结状态的关键快照
            2. 通过快照找到关联的物理产品，在把物理产品进行全展开
            3. 把多个项目找到的全展开的物理产品进行去重(根据ObjectId)，组装成一个集合
            4. 在看ECR关联的受影响父件，填写的是否替换为Y
                1. 企业编码为空的就取Name
                2. 如果为Y，查询该数据是否在刚组装的集合里面，如果存在就弹出提示:xxx(企业编码)零件的父零件xxx(企业编码)，
                   在xxx项目的xxx快照已经冻结,请把当前ECR的的受影响父件是否替换改成否
        * */
        String strObjectId = args[0];
        _logger.info("strObjectId:{}", strObjectId);
        int iRes = 0;
        try {
            ContextUtil.pushContext(context);
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
            StringList busSelectList = JF_Util_mxJPO.basicBolistSel();
            busSelectList.add(SELECT_DESCRIPTION);
            //通过ecr找到关联项目和影响项目，找到项目冻结的快照，再通过快照找到物理产品，全展开记录id(去重),组装为集合
            //ECR和受影响项目rel: JFECR2AffectedProject
            //ECR和项目rel: JFChange2Project
            MapList mapList = ecr.getRelatedObjects(
                    context,
                    "JFECR2AffectedProject,JFChange2Project", // relationship pattern
                    TYPE_PROJECT_SPACE,                                    // object pattern
                    busSelectList,                            // object selects
                    relSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0
            );
            DomainObject domainObject = DomainObject.newInstance(context);
            Iterator iterator = mapList.iterator();
            HashSet<String> allPartSet = new HashSet<>();
            HashMap<String, Map<String, String>> partMap = new HashMap<>();
            //在找到这些项目关联的冻结状态的关键快照
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String id = UIUtil.getValue(map, SELECT_ID);
                String projectName1 = UIUtil.getValue(map, SELECT_DESCRIPTION);
                String projectName2 = UIUtil.getValue(map, SELECT_NAME);
                String projectName = UIUtil.isNullOrEmpty(projectName1) ? projectName2 : projectName1;
                domainObject.setId(id);
                //获取项目下的快照：JFProject2Snapshot
                StringList infoList = domainObject.getInfoList(context, "from[JFProject2Snapshot].to.id");
                MapList info = DomainObject.getInfo(context, infoList.toStringArray(), busSelectList);
                for (int i = 0; i < info.size(); i++) {
                    Map map1 = (Map) info.get(i);
                    if ("FROZEN".equalsIgnoreCase(UIUtil.getValue(map1, SELECT_CURRENT))) {
                        domainObject.setId(UIUtil.getValue(map1, SELECT_ID));
                        String snapshotName = UIUtil.getValue(map1, SELECT_NAME);
                        //获取快照和物理产品的关系  JFSnapshot2VPMReference
                        //通过快照找到关联的物理产品
                        StringList partList = domainObject.getInfoList(context, "from[JFSnapshot2VPMReference].to.id");
                        //把多个项目找到的全展开的物理产品进行去重(根据ObjectId)，组装成一个集合
                        for (String partId : partList) {
                            domainObject.setId(partId);
                            allPartSet.add(partId);
                            //展开结构
                            MapList childPartList = domainObject.getRelatedObjects(
                                    context,
                                    JF_PLMConstants_mxJPO.REL_Instance,
                                    TYPE_VPMREFERENCE,
                                    busSelectList,
                                    relSelectList,
                                    false,
                                    true,
                                    (short) 0,
                                    "",
                                    "",
                                    0
                            );
                            HashMap<String, String> partItemMap = new HashMap<>();
                            partItemMap.put("projectName", projectName);
                            partItemMap.put("snapshotName", snapshotName);
                            partMap.put(partId, partItemMap);
                            StringList allParts = (StringList) childPartList.stream().map(m -> {
                                Map map2 = (Map) m;
                                String partId1 = UIUtil.getValue(map2, SELECT_ID);
                                HashMap map3 = new HashMap<String, String>();
                                map3.put("projectName", projectName);
                                map3.put("snapshotName", snapshotName);
                                partMap.put(partId1, map3);
                                return partId1;
                            }).collect(Collectors.toCollection(StringList::new));
                            allPartSet.addAll(allParts);
                        }
                    }
                }
            }
            StringList allPartList = StringList.create(allPartSet);
            //查询ECR关联的受影响父件  JFRelateItemParent
            relSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubPartId);
            relSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubIFReplace);
            relSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubConnectId);
            busSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            MapList relateItemParentList = ecr.getRelatedObjects(
                    context,
                    JF_PLMConstants_mxJPO.REL_JFRelateItemParent, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                    busSelectList,                            // object selects
                    relSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0
            );
            StringBuilder sb = new StringBuilder();
            String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.HasSnapshot");
            Iterator iterator1 = relateItemParentList.iterator();
            _logger.info("partMap:{}", partMap.toString());
            _logger.info("allPartList:{}", allPartList.toString());
            while (iterator1.hasNext()) {
                Map map = (Map) iterator1.next();
                String id = UIUtil.getValue(map, SELECT_ID);
                String subIFReplace = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubIFReplace);
                // 2. 如果为Y，查询该数据是否在刚组装的集合里面，如果存在就弹出提示:xxx(企业编码)零件的父零件xxx(企业编码)，
                // 在xxx项目的xxx快照已经冻结,请把当前ECR的的受影响父件是否替换改成否
                if ("Y".equalsIgnoreCase(subIFReplace) && allPartList.contains(id)) {
                    if (!partMap.containsKey(id)) {
                        continue;
                    }
                    //报错
                    String subPartId = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_JFSubPartId);
                    _logger.info("subPartId:{}", subPartId.toString());
                    domainObject.setId(subPartId);
                    String subName = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER);
                    subName = UIUtil.isNullOrEmpty(subName) ? domainObject.getInfo(context, SELECT_NAME) : subName;
                    String parentName = UIUtil.getValue(map, JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
                    _logger.info("id:{}", id);
                    Map<String, String> stringStringMap = partMap.get(id);
                    _logger.info("stringStringMap:{}", stringStringMap.toString());
                    sb.append(strMess.replace("$1", subName).replace("$2", parentName).replace("$3", stringStringMap.get("projectName")).replace("$4", stringStringMap.get("snapshotName")));
                    sb.append("\n");
                }
            }
            String mess = sb.toString();
            if (mess.length() > 0) {
                iRes = 1;
                emxContextUtilBase_mxJPO.mqlWarning(context, mess);
            }
            _logger.info("------------------------ checkECRParentPartHasSnapshot end  -----------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return iRes;
    }


    /**
     * @param context
     * @param args
     * @return java.lang.String
     * @throws
     * @description 构造ECR QQ 列
     * @author CHENYAN
     * @date 2024/3/2 11:06
     */
    public String buildECRQQHtml(Context context, String[] args) throws Exception {
        _logger.info("--------------------- buildECRQQHtml begin --------------------------");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        String strMode = (String) requestMap.get("mode");
        String strLoginUser = context.getUser();
        String strLang = context.getSession().getLanguage();
        String strFileUploadNls = ComponentsUtil.i18nStringNow("emxComponents.Common.JFECRQQFileUpload", strLang);
        String strChangeSource = "";
        String strIsPlatForm = "";
        String objectId = null;
        if (requestMap == null) {
            objectId = (String) programMap.get("objectId");
        } else {
            objectId = (String) requestMap.get("objectId");
        }
        StringBuffer sbDocDown = new StringBuffer();

        _logger.info("objectId:{}", objectId);
        if (UIUtil.isNotNullAndNotEmpty(objectId)) {
            DomainObject ECR = DomainObject.newInstance(context, objectId);
            StringList ECRSelectList = new StringList();
            ECRSelectList.add(SELECT_ATTR_JFQQID);
            ECRSelectList.add(SELECT_CURRENT);
            ECRSelectList.add(SELECT_OWNER);
            ECRSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
            ECRSelectList.add(SELECT_ATTR_JFISPLATFORMPART);
            Map ECRInfoMap = ECR.getInfo(context, ECRSelectList);
            String strQQID = (String) ECRInfoMap.get(SELECT_ATTR_JFQQID);
            String strCurrent = (String) ECRInfoMap.get(SELECT_CURRENT);
            String strOwner = (String) ECRInfoMap.get(SELECT_OWNER);
            strChangeSource = (String) ECRInfoMap.get(SELECT_ATTR_JFCHANGESOURCE);
            strIsPlatForm = (String) ECRInfoMap.get(SELECT_ATTR_JFISPLATFORMPART);
            boolean isEdit = strLoginUser.equals(strOwner) && "Create".equals(strCurrent) ? true : false;
            StringList selectTypeList = new StringList();
            selectTypeList.add(SELECT_ID);
            selectTypeList.add(SELECT_ATTRIBUTE_TITLE);
            selectTypeList.add(SELECT_TYPE);
            selectTypeList.add(SELECT_NAME);
            selectTypeList.add(SELECT_REVISION);
            MapList docList = ECR.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                    TYPE_DOCUMENT,                                    // object pattern
                    selectTypeList,                            // object selects
                    new StringList(SELECT_RELATIONSHIP_ID), // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "attribute[Project Role]=='QQ'",
                    (short) 0);
            StringList docIDList = (StringList) docList.stream().map(m -> {
                Map doc = (Map) m;
                return doc.get(SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
            sbDocDown.append("<table");
            sbDocDown.append("><tr>");
            sbDocDown.append("<td>");
            strQQID = StringEscapeUtils.escapeHtml4(strQQID);
            strFileUploadNls = StringEscapeUtils.escapeHtml4(strFileUploadNls);
            if ("view".equals(strMode)) {
                sbDocDown.append(JF_PublicMethodClass_mxJPO.buildStringInStrings("<span>", strQQID, "</span>&nbsp;"));
            } else if ("edit".equals(strMode)) {
                if (isEdit) {
                    sbDocDown.append("<input value=\"" + strQQID + "\" id=\"JFQQ\" name=\"JFQQ\" type=\"text\"  size=\"20\">");
                } else {
                    sbDocDown.append(JF_PublicMethodClass_mxJPO.buildStringInStrings("<span>", strQQID, "</span>&nbsp;"));
                }
            }
            sbDocDown.append("</td>");
            StringList QQFileIdList = new StringList();
            if (isEdit && "edit".equals(strMode)) {
                sbDocDown.append("<td id=\"fileTd\">");
                sbDocDown.append("<input  id=\"JFECRQQFileId\" name=\"JFECRQQFileId\" value=\"" + docIDList.join(",") + "\" type=\"hidden\">");
                sbDocDown.append("<input onclick=\"javascript:showModalDialog('../components/JF_ECRQQFilePreCheckin.jsp?objectAction=checkin&msfBypass=true&");
                sbDocDown.append("objectId=");
                sbDocDown.append("");
                sbDocDown.append("','730','450')\"" +
                        "        value=\"");
                sbDocDown.append(strFileUploadNls);
                sbDocDown.append("\" type=\"button\">");
                sbDocDown.append("</td>");
            }
            sbDocDown.append("<td id=\"JFReplace\">");
            //Show Counter Link
            for (int i = 0; i < docList.size(); i++) {
                sbDocDown.append("<div ");
                Map docMap = (Map) docList.get(i);
                Map docObjMap = new HashMap<>();
                String strFileId = (String) docMap.get(SELECT_ID);
                QQFileIdList.add(strFileId);
                sbDocDown.append("style='vertical-align:middle;padding-left:1px;cursor:pointer;display:inline-block' ");
                sbDocDown.append("onClick=\"javascript:callCheckout('").append(strFileId).append("',");
                sbDocDown.append("'download', '', '', 'null', 'null', 'structureBrowser', 'PMCPendingDeliverableSummary', 'null')\">");
                sbDocDown.append("<img style='vertical-align:middle;' src='../common/images/").append("iconSmallDocument.gif").append("'");
                sbDocDown.append(" title=\"");
                String strDocTitle = (String) docMap.get(SELECT_ATTRIBUTE_TITLE);
                strDocTitle = StringEscapeUtils.escapeHtml4(strDocTitle);
                sbDocDown.append(strDocTitle);
                sbDocDown.append("\" />");
                sbDocDown.append("</div>");
            }
            sbDocDown.append("</td>");
        } else {
            sbDocDown.append("<table");
            sbDocDown.append("><tr>");
            sbDocDown.append("<td>");
            sbDocDown.append("<input value=\"\" id=\"JFQQ\" name=\"JFQQ\" type=\"text\"  size=\"20\"/>");
            sbDocDown.append("</td>");
            sbDocDown.append("<td id=\"fileTd\">");
            sbDocDown.append("<input id=\"JFECRQQFileId\" name=\"JFECRQQFileId\" type=\"hidden\"/>");
            sbDocDown.append("<input onclick=\"javascript:showModalDialog('../components/JF_ECRQQFilePreCheckin.jsp?objectAction=checkin&amp;msfBypass=true&amp;objectId=;");
            sbDocDown.append("','730','450')\"" +
                    "  value=\"");
            _logger.info("strFileUploadNls:{}", strFileUploadNls);
            sbDocDown.append(strFileUploadNls);
            sbDocDown.append("\" type=\"button\"/>");
            sbDocDown.append("</td>");
            sbDocDown.append("<td id=\"JFReplace\">");
            sbDocDown.append("</td>");
        }
        sbDocDown.append("</tr></table>");

        //
        if ("edit".equals(strMode)) {
            if ("Both".equals(strChangeSource) || "External Changes".equals(strChangeSource)) {
                sbDocDown.append("<script>");
                sbDocDown.append("  var  dom = document.getElementById('");
                String strDomId = "calc_JFQQ";
                sbDocDown.append(strDomId);
                sbDocDown.append("');");
                sbDocDown.append("  if (dom){");
                sbDocDown.append("    dom.querySelector('td:first-child').className = \"createLabelRequired\";");
                sbDocDown.append("  }");
                sbDocDown.append("</script>");

            }
        } else if ("create".equals(strMode)) {
            sbDocDown.append("<script>");
            sbDocDown.append("  var  dom = document.getElementById('");
            String strDomId = "calc_JFQQ";
            sbDocDown.append(strDomId);
            sbDocDown.append("');");
            sbDocDown.append("  if (dom){");
            sbDocDown.append("    dom.querySelector('td:first-child').className = \"createLabelRequired\";");
            sbDocDown.append("  }");
            sbDocDown.append("</script>");
        }
        sbDocDown.append("<script language=\"JavaScript\">");
        sbDocDown.append("function  checkJFQQ1(){\n" +
                "    var strChangeSource = emxFormGetValue(\"JFChangeSource\").current.actual;\n" +
                "    if (strChangeSource === \"Both\" || strChangeSource === \"External Changes\"){\n" +
                "        const  domJFQQ = document.getElementById('JFQQ');\n" +
                "        const domJFQQFileId=  document.getElementById(\"JFECRQQFileId\");\n" +
                "        if ((!domJFQQ.value) || (!domJFQQFileId.value)){\n" +
//                "        if (!(domJFQQ.value && domJFQQFileId.value)){\n" +
                "            sendMess1(\"\u5fc5\u987b\u8f93\u5165\u6709\u6548\\u503c\uff1a QQ\u7f16\u53f7\u53ca\u9644\u4ef6\",\"Valid values must be entered: QQ ID and attachment\");\n" +
                "        return false ;\n" +
                "        }\n" +
                "    }\n" +
                "        return true ;\n" +
                "}");
        sbDocDown.append("function sendMess1(strCNMess,strENMess){\n" +
                "    var language = navigator.language || navigator.userLanguage;\n" +
                "    var strMess = \"\";\n" +
                "    if (language.includes(\"zh\") || language.includes(\"zh-CN\")) {\n" +
                "        strMess = strCNMess;\n" +
                "    }else {\n" +
                "        strMess = strENMess;\n" +
                "    }\n" +
                "    alert(strMess);\n" +
                "}");
        sbDocDown.append(" // 使用 addEventListener 添加第三个 onload 函数\n" +
                "    window.addEventListener('load', function JFQQOnloadHandler() {\n" +
                "        console.log(\"Third onload handler called.\");\n" +
                "        console.log(\"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@.\");\n" +
                "        document.getElementById('JFQQ').customValidate = checkJFQQ1\n" +
                "    }, false);");
        sbDocDown.append(" </script>");
        _logger.info("sbDocDown:{}", sbDocDown);
        //加载影响工厂样式
        if (!"view".equals(strMode)) {
            if (!"Yes".equals(strIsPlatForm)) {
                sbDocDown.append("<script language=\"JavaScript\">");
                if ("create".equals(strMode)) {
                    sbDocDown.append("    window.addEventListener('load', function JFJFAffectsFactoryOnloadHandler() {\n" +
                            " FormHandler.GetField(\"JFAffectsFactory\").HandlerField[0].customValidate = checkInputIsNull;" +
                            "emxFormSetFieldEditable(\"JFAffectsFactory\",false);" +
                            "    }, false);");
                }
                sbDocDown.append(" </script>");


            }
        }
        _logger.info("--------------------- buildECRQQHtml end --------------------------");
        return sbDocDown.toString();
    }

    /**
     * @param context
     * @param args
     * @return java.util.Map
     * key oids  value 12133.131|211.121 以 | 分割
     * key titles  value 文件名1|文件名2 以 | 分割
     * @throws
     * @description IPPAP上传会议附件
     * @author CHENYAN
     * @date 2024/3/2 11:44
     */
    public static Map uploadFile(Context context, String[] args) throws Exception {
        _logger.info("--------------------- uploadFile begin--------------------------");
        String strLang = context.getSession().getLanguage();
        Map<String, String> res = new HashMap<>();
        StringBuffer sbDocDown = new StringBuffer();
        try {
            //返回所有文档对象信息集合
            MapList resDocMapList = new MapList();
            Map paramMap = (Map) JPO.unpackArgs(args);
            List files = (List) paramMap.get("files");
            String strObjectId = (String) paramMap.get("objectId");
            _logger.info("strObjectId:{}", strObjectId);
            String sOSName = System.getProperty("os.name");
            String sFolder = sOSName.contains("Windows") ? JF_ECRRESTService_mxJPO.FolderWIN : JF_ECRRESTService_mxJPO.FolderUNIX;
            String separator = sOSName.contains("Windows") ? "\\" : "/";
            String sTmpDir = Environment.getValue(context, "TMPDIR");
            if (null != sTmpDir && !sTmpDir.trim().isEmpty()) {
                sFolder = sTmpDir;
                if (!sFolder.substring(sFolder.length() - 1).equals(separator))
                    sFolder = sFolder + separator;
            }
            Iterator iter = files.iterator();
            int index;
            String sFilename = "";
            java.io.File file = null;
            java.io.File outfile = null;
            //文档id集合
            StringList oids = new StringList();
            //标题集合
            StringList titles = new StringList();
            while (iter.hasNext()) {
                file = (java.io.File) iter.next();
                sFilename = file.getName();
                _logger.info(sFilename);
                if (sFilename.contains("/")) {
                    index = sFilename.lastIndexOf("/");
                    sFilename = sFilename.substring(index);
                }
                if (sFilename.contains("\\")) {
                    index = sFilename.lastIndexOf("\\");
                    sFilename = sFilename.substring(index + 1);
                }
                outfile = new java.io.File(sFolder + sFilename);
                FileInputStream fis = null;
                FileOutputStream fos = null;
                try {
                    fis = new FileInputStream(file);
                    fos = new FileOutputStream(outfile);
                    fos.write(fis.readAllBytes());
                    fos.flush();
                } finally {
                    if (fis != null) {
                        fis.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                }
                String sObjGeneratorName = UICache.getObjectGenerator(context, "type_Document", "");
                String sName = DomainObject.getAutoGeneratedName(context, sObjGeneratorName, "");
                String docPolicy = PropertyUtil.getSchemaProperty(EnoviaResourceBundle.getProperty(context, "emxFrameowrk.FileUpload.Default.Policy"));
                if (UIUtil.isNotNullAndNotEmpty(docPolicy)) {
                    CommonDocument cDoc = new CommonDocument();
                    Policy policy = new Policy(docPolicy);
                    String revision = policy.getFirstInSequence(context);
                    PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", "true", false);
                    cDoc.createObject(context, DomainObject.TYPE_DOCUMENT, sName, revision, docPolicy, context.getVault().getName());
                    cDoc.setAttributeValue(context, "Title", sFilename);
                    //cDoc.checkinFile(context, true, true, "", "generic", sFilename, sFolder);
                    String storeFromBL = null;
                    storeFromBL = DocumentUtil.getStoreFromBL(context, "Document");
                    cDoc.createVersion(context, sFilename, sFilename, null);
                    cDoc.checkinFile(context, true, true, "", "generic", storeFromBL, sFilename, sFolder);
                    oids.add(cDoc.getId(context));
                }
            }
            outfile.delete();
            Gson gson = new Gson();
            String strFileUploadNls = ComponentsUtil.i18nStringNow("emxComponents.Common.JFECRQQFileUpload", strLang);

            for (int i = 0; i < oids.size(); i++) {
                Map resDocMap = new HashMap<>();
                resDocMap.put("objectId", oids.get(i));
                resDocMapList.add(resDocMap);
                sbDocDown.append("<div ");
                sbDocDown.append("style='vertical-align:middle;padding-left:1px;cursor:pointer;display:inline-block' ");
                sbDocDown.append("onClick=\"javascript:callCheckout('").append(oids.get(i)).append("',");
                sbDocDown.append("'download', '', '', 'null', 'null', 'structureBrowser', 'PMCPendingDeliverableSummary', 'null')\">");
                sbDocDown.append("<img style='vertical-align:middle;' src='../common/images/").append("iconSmallDocument.gif").append("'");
                sbDocDown.append(" title=\"");
                String strEscape = StringEscapeUtils.escapeHtml4(sFilename);
                strEscape = unEscape(strEscape);
                sbDocDown.append(strEscape);
                sbDocDown.append("\" />");
                sbDocDown.append("</div>");
            }
            res.put("newId", oids.join(","));
            res.put("html", sbDocDown.toString());
        } catch (Exception e) {
            _logger.error(e.getMessage());
            throw e;
        }
        _logger.info("sbDocDown:{}", sbDocDown);
        _logger.info("--------------------- uploadFile end--------------------------");
        return res;
    }

    /**
     * @param strChar
     * @return void
     * @throws
     * @description 取消转义
     * @author CHENYAN
     * @date 2024/7/4 14:58
     */
    public static String unEscape(String strChar) {
        strChar = strChar.replaceAll("&mdash;", "—");
        return strChar;
    }

    /**
     * 构造Vue的
     *
     * @return
     */
    public static String buildVueData(Context context, String strAttrName, String dbAttrValue) throws Exception {
        MapList rangeValueRes = new MapList();
        StringList attrRangeList = com.matrixone.apps.domain.util.mxAttr.getChoices(context, strAttrName);
        for (int i = 0; i < attrRangeList.size(); i++) {
            HashMap<String, String> rangeMap = new HashMap<>();
            String strValue = attrRangeList.get(i);
            String strValueNls = i18nNow.getRangeI18NString(strAttrName, strValue, context.getSession().getLanguage());
            rangeMap.put("value", strValue);
            rangeMap.put("label", strValueNls);
            rangeValueRes.add(rangeMap);
        }
        Gson gson = new Gson();
        String strJson = JF_PublicMethodClass_mxJPO.buildStringInStrings("options :", gson.toJson(rangeValueRes), ",", "attrValue : ", gson.toJson(dbAttrValue.split(",")));
        _logger.info("strJson:{}", strJson);
        return strJson;
    }

    public static String buildVueMethod() {
        StringBuffer sb = new StringBuffer();
        sb.append("methods: {");
        sb.append("OnJFAffectedFactoryChange(value){ const dom = document.getElementById(\"JFAffectsFactory\");\n" +
                "    if (dom){\n" +
                "      dom.value = JSON.stringify(this.attrValue);\n" +
                "    }} ");

        sb.append("}");
        _logger.info("sb:{}", sb);
        return sb.toString();
    }

    public Map getJFAffectedFactoryRanges(Context context, String[] args) throws Exception {
        HashMap<Object, Object> res = new HashMap<>();
        StringList ranges = FrameworkUtil.getRanges(context, "JFAffectedFactory");
        if (ranges.contains("")) {
            ranges.remove("");
        }
        _logger.info("getJFAffectedFactoryRanges.ranges:{}", ranges);
        StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, "JFAffectedFactory", ranges, context.getLocale().toString());
        res.put("field_choices", ranges);
        res.put("field_display_choices", nlsRanges);
        return res;
    }


    /**
     * @param context
     * @param args
     * @return boolean
     * @throws
     * @description 获取ECR上传更新excel权限
     * @author CHENYAN
     * @date 2024/11/26 14:08
     */

    public boolean getUploadECRTableAccess(Context context, String[] args) throws Exception {
        _logger.info("-----------------------------------------getUploadECRTableAccess begin ----------------------------------------------------");
        boolean res = false;
        Map requestMap = JPO.unpackArgs(args);
        _logger.info("requestMap:{}", requestMap);
//        String strObjectId  = (String)requestMap.get("parentOID");
        String strObjectId = (String) requestMap.get("objectId");
        String strSelectedTable = (String) requestMap.get("table");
        _logger.info("strSelectedTable:{}", strSelectedTable);
        String strLoginUser = context.getUser();
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        Map ecrInfo = ecr.getInfo(context, StringList.create(SELECT_CURRENT, SELECT_TYPE));
        String strECRCurrent = (String) ecrInfo.get(SELECT_CURRENT);
        String strECRType = (String) ecrInfo.get(SELECT_TYPE);
        if (TYPE_JFECR.equals(strECRType) && ("Countersign".equals(strECRCurrent) || "APR".equals(strECRCurrent))) {
            //角色不为空的会签任务
            MapList loginUserSignTaskRole = new MapList();
            loginUserSignTaskRole = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECRAndUser(context, new String[]{strObjectId, strLoginUser});
            if (loginUserSignTaskRole.size() > 0) {
                for (int i = 0; i < loginUserSignTaskRole.size(); i++) {
                    Map signTask = (Map) loginUserSignTaskRole.get(i);
                    String strCurrent = (String) signTask.get("current");
                    _logger.info("strCurrent:{}", strCurrent);
                    if (!("Complete".equals(strCurrent) || "Review".equals(strCurrent))) {
                        String strRole = (String) signTask.get("role");
                        _logger.info("strRole:{}", strRole);
                        strRole = JF_ECRRESTService_mxJPO.getRoleKeyByValue(strRole);
                        _logger.info("strRole:{}", strRole);
                        if (strSelectedTable.contains("JFECRCosting")) {
                            if (JF_ECRRESTService_mxJPO.EDIT_ECRCosting_TABLE_ROLE_LIST.contains(strRole)) {
                                res = true;
                                break;
                            }
                        } else if (strSelectedTable.contains("JFECRController")) {
                            if (JF_ECRRESTService_mxJPO.EDIT_ECRController_TABLE_ROLE_LIST.contains(strRole)) {
                                res = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        _logger.info("res:{}", res);
        _logger.info("-----------------------------------------getUploadECRTableAccess end ----------------------------------------------------");
        return res;
    }

    /**
     * @param context
     * @param args
     * @return boolean
     * @throws
     * @description 获取ECR下载的权限
     * @author CHENYAN
     * @date 2024/11/26 14:08
     */
    public boolean getDownloadECRTableAccess(Context context, String[] args) throws Exception {
        boolean res = false;
        Map requestMap = JPO.unpackArgs(args);
//        String strObjectId  = (String)requestMap.get("parentOID");
        String strObjectId = (String) requestMap.get("objectId");
        String strSelectedTable = (String) requestMap.get("table");
        String strLoginUser = context.getUser();
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        Map ecrInfo = ecr.getInfo(context, StringList.create(SELECT_CURRENT, SELECT_TYPE));
        String strECRCurrent = (String) ecrInfo.get(SELECT_CURRENT);
        String strECRType = (String) ecrInfo.get(SELECT_TYPE);
        _logger.info("strSelectedTable:{}", strSelectedTable);
        if (TYPE_JFECR.equals(strECRType) && ("Countersign".equals(strECRCurrent) || "APR".equals(strECRCurrent) ||
                "Quotation".equals(strECRCurrent) || "Complete".equals(strECRCurrent))) {
            //角色不为空的会签任务
            MapList loginUserSignTaskRole = new MapList();
            loginUserSignTaskRole = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECRAndUser(context, new String[]{strObjectId, strLoginUser});
            if (loginUserSignTaskRole.size() > 0) {
                for (int i = 0; i < loginUserSignTaskRole.size(); i++) {
                    Map signTask = (Map) loginUserSignTaskRole.get(i);
                    String strCurrent = (String) signTask.get("current");
                    _logger.info("strCurrent:{}", strCurrent);
                    String strRole = (String) signTask.get("role");
                    strRole = JF_ECRRESTService_mxJPO.getRoleKeyByValue(strRole);
                    _logger.info("strRole:{}", strRole);
//                    if ("JFECRCosting".equals(strSelectedTable)) {
                    if (strSelectedTable.contains("JFECRCosting")) {
                        if (JF_ECRRESTService_mxJPO.EDIT_ECRCosting_TABLE_ROLE_LIST.contains(strRole)) {
                            res = true;
                            break;
                        }
//                    } else if ("JFECRController".equals(strSelectedTable)) {
                    } else if (strSelectedTable.contains("JFECRController")) {
                        if (JF_ECRRESTService_mxJPO.EDIT_ECRController_TABLE_ROLE_LIST.contains(strRole)) {
                            res = true;
                            break;
                        }
                    }
                }
            }
        }
        _logger.info("res:{}", res);
        return res;
    }


    /**
     * @param strType
     * @param strPrePath
     * @return java.lang.String
     * @throws
     * @description 获取模版excel全路径
     * @author CHENYAN
     * @date 2024/11/26 14:07
     */
    public static String getTemplatePath(String strType, String strPrePath) {
        String strTempExcelName = "";
        if ("JFECRCosting".equals(strType) || "JFNewECRCosting".equals(strType)) {
            strTempExcelName = "ECRCosting.xlsx";
        } else if ("JFECRController".equals(strType) || "JFNewECRController".equals(strType)) {
            strTempExcelName = "ECRController.xlsx";
        } else if ("CostChangeHistoryTemplate".equals(strType)) {
            strTempExcelName = "CostChangeHistoryTemplate.xlsx";
        } else if ("JFECRSignTaskTemplate".equalsIgnoreCase(strType)) {
            strTempExcelName = "ECRSignTaskTemplate.xlsx";
        } else if ("ExportCostTemplate".equalsIgnoreCase(strType)) {
            strTempExcelName = "ExportCostTemplate.xlsx";
        } else if ("JFDADaTaskTemplate".equalsIgnoreCase(strType)){
            strTempExcelName = "DADATaskTemplate.xlsx";
        } else {
            strTempExcelName = strType;
        }
        return JF_PublicMethodClass_mxJPO.buildStringInStrings(strPrePath, TEMPLATE_EXCEL_PATH, strTempExcelName);
    }

    /**
     * @param context
     * @param args
     * @return java.util.Map
     * @throws
     * @description ECR 自制件个采购件下载逻辑
     * @author CHENYAN
     * @date 2024/11/26 14:05
     */

    public Map ECRDownload(Context context, String[] args) throws Exception {
        Map res = new HashMap<String, String>();
        Map paramMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) paramMap.get("objectId");
        //选择的表格
        String strSelectTable = (String) paramMap.get("type");
        _logger.info("strSelectTable:{}", strSelectTable);
        //服务路径
        String strPath = (String) paramMap.get("path");
        //模板excel路径
        String strTemplatePath = getTemplatePath(strSelectTable, strPath);
        _logger.info("strTemplatePath:{}", strTemplatePath);
        InputStream inputStream = null;
        FileOutputStream fos = null;
        try {
            inputStream = new FileInputStream(strTemplatePath);
            Workbook workbook = WorkbookFactory.create(inputStream);
            //设置可编辑单元格背景色
            CellStyle cellStyle = workbook.createCellStyle();
            //设置背景色
            cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            //必须设置 否则背景色不生效
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            //设置单元格边框
            CellStyle cellBorderStyle = workbook.createCellStyle();
            cellBorderStyle.setBorderTop(BorderStyle.THIN);
            cellBorderStyle.setBorderBottom(BorderStyle.THIN);
            cellBorderStyle.setBorderLeft(BorderStyle.THIN);
            cellBorderStyle.setBorderRight(BorderStyle.THIN);
            //读取第一个sheet
            Sheet sheet = workbook.getSheetAt(0);

            //从第二行遍历sheet，并拿取其中每行的数据
            Row row = sheet.getRow(0);
            StringList strings = new StringList();
            Map excelTitleMap = new HashMap<String, Integer>();
            for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
                String strCell = row.getCell(i, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK) == null ? "" : getCellValue(row, i);
                if (UIUtil.isNotNullAndNotEmpty(strCell)) {
                    excelTitleMap.put(strCell, i);
                }
            }
            _logger.info("excelTitleMap:{}", excelTitleMap);
            HashMap<String, String> stringStringHashMap = new HashMap<>();
            stringStringHashMap.put("objectId", strObjectId);
            stringStringHashMap.put("expandLevel", "0");
            MapList ecrCostingTableData = new MapList();
            //costing 表格数据
            if ("JFECRCosting".equals(strSelectTable)) {
                ecrCostingTableData = getECRCostingTableData(context, JPO.packArgs(stringStringHashMap));
            } else if ("JFECRController".equals(strSelectTable)) {
                ecrCostingTableData = getECRMakeTableData(context, JPO.packArgs(stringStringHashMap));
            }
            _logger.info("ecrCostingTableData size :{}", ecrCostingTableData.size());
            MapList CostingMappingList = JF_PublicMethodClass_mxJPO.getECRTableMapping(context, strSelectTable);
            updateTableData(context, strSelectTable, strObjectId, ecrCostingTableData, CostingMappingList);
            _logger.info("ecrCostingTableData:{}", ecrCostingTableData);
            //编辑table 数据写入excel
            for (int i = 0; i < ecrCostingTableData.size(); i++) {
                Row createRow = sheet.createRow(i + 2);
                Map map = (Map) ecrCostingTableData.get(i);
                createRowValue(excelTitleMap, map, createRow, cellStyle, cellBorderStyle);
            }
            String strTmpPath = context.createWorkspace();
            strTmpPath = strTmpPath.endsWith("/") ? strTmpPath : JF_PublicMethodClass_mxJPO.buildStringInStrings(strTmpPath, "/");
            String strFileName = JF_PublicMethodClass_mxJPO.buildStringInStrings(String.valueOf(System.currentTimeMillis()), ".xlsx");
            String strFullPath = JF_PublicMethodClass_mxJPO.buildStringInStrings(strTmpPath, strFileName);
            _logger.info("strFullPath:{}", strFullPath);
            fos = new FileOutputStream(strFullPath);
            workbook.write(fos);
            res.put("path", strFullPath);
            res.put("filename", strFileName);
            res.put("code", "200");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
        return res;
    }

    public static String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        cell.setCellType(CellType.STRING);
        return String.valueOf(cell.getStringCellValue().trim());
    }

    public static String getDisplayValueByTitleKey(Context context, String strKey, Map dbMap, MapList xmlMappingMapList) {
        String strDisPlayValue = "";
        try {
            String strSelectValueKey = "";
            if (UIUtil.isNullOrEmpty(strKey) || null == xmlMappingMapList || xmlMappingMapList.size() == 0) {
                return strDisPlayValue;
            }
            Map keyValueMap = getTitleMapByMappingList(strKey, xmlMappingMapList);
            switch (strKey) {
                case "PartType", "ProcurementType", "JFChangeResource": {
                    String strKeyValue = (String) keyValueMap.get("value");
                    String strSelectKeyValue = (String) keyValueMap.get("selectValue");
                    String strDisplayKey = (String) dbMap.get(strSelectKeyValue);
                    if (UIUtil.isNullOrEmpty(strDisplayKey)) {
                        strDisplayKey = "Both";
                    }
                    String strNlsValue = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), JF_PublicMethodClass_mxJPO.buildStringInStrings("emxFramework.Range.", strKeyValue, ".", strDisplayKey));
                    _logger.info("strNlsValue:{}", strNlsValue);
                    strDisPlayValue = strNlsValue;
                    break;
                }
                default: {
                    String strSelectKeyValue = (String) keyValueMap.get("selectValue");
                    strDisPlayValue = (String) dbMap.get(strSelectKeyValue);
                }
            }
        } catch (Exception e) {
            _logger.error("e:{}", e.getMessage());
            _logger.error("获取ECR表格映射属性失败 key :{}", strKey);
        }
        //如果是特定的属性需要转换为国际化翻译
        return strDisPlayValue;
    }

    public static Map getTitleMapByMappingList(String strKey, MapList xmlMappingMapList) {
        for (int i = 0; i < xmlMappingMapList.size(); i++) {
            Map xmlMapping = (Map) xmlMappingMapList.get(i);
            String strXMLId = (String) xmlMapping.get("id");
            if (strXMLId.equals(strKey)) {
                return xmlMapping;
            }
        }
        return new HashMap();
    }

    /**
     * @param excelTitleMap
     * @param dbMap
     * @param createRow
     * @return org.apache.poi.ss.usermodel.Row
     * @throws
     * @description 创建每一行的单元格
     * @author CHENYAN
     * @date 2024/9/29 15:53
     */

    public Row createRowValue(Map excelTitleMap, Map dbMap, Row createRow, CellStyle cellStyle, CellStyle cellBorderStyle) throws Exception {
        for (Object entry : excelTitleMap.entrySet()) {
            Map.Entry cellKey = (Map.Entry) entry;
            //列的key
            String strCellKey = (String) cellKey.getKey();
            //列的下标
            Integer iCellIndex = (Integer) cellKey.getValue();
            Cell createCell = createRow.createCell(iCellIndex);
            createCell.setCellValue((String) dbMap.get(JF_PublicMethodClass_mxJPO.buildStringInStrings(strCellKey, "disPlayValue")));
            String strIsEdit = (String) dbMap.get(JF_PublicMethodClass_mxJPO.buildStringInStrings(strCellKey, "isEdit"));
            if (UIUtil.isNotNullAndNotEmpty(strIsEdit)) {
                if ("true".equalsIgnoreCase(strIsEdit)) {
                    createCell.setCellStyle(cellStyle);
                } else {
                    if (!("id".equals(strCellKey) || "id[connection]".equals(strCellKey))) {
                        createCell.setCellStyle(cellBorderStyle);
                    }
                }
            }
        }
        return createRow;
    }

    /**
     * @param context
     * @param strSelectedTable JFECRCosting 和 JFECRController
     * @param strECRId         ECR ID
     * @param tableDataList    表格数据
     * @return void
     * @throws
     * @description 更新表格Map的属性值和是否可编辑
     * @author CHENYAN
     * @date 2024/9/29 16:45
     */
    public static void updateTableData(Context context, String strSelectedTable, String strECRId, MapList tableDataList, MapList xmlMappingMapList) throws Exception {
        try {
            for (int i = 0; i < xmlMappingMapList.size(); i++) {
                //获取列 key
                Map xmlFieldMap = (Map) xmlMappingMapList.get(i);
                String strFieldKey = (String) xmlFieldMap.get("id");
                //获取属性key
                String strFieldSelectKey = (String) xmlFieldMap.get("selectValue");
                String strFieldValueKey = (String) xmlFieldMap.get("value");
                _logger.info("strFieldKey:{}", strFieldKey);
                //构造 参数 调用table中方法
                HashMap<Object, Object> paramsMap = new HashMap<>();
                HashMap<Object, Object> paramList = new HashMap<>();
                HashMap<Object, Object> columnMap = new HashMap<>();
                paramList.put("selectedTable", strSelectedTable);
                paramList.put("parentOID", strECRId);
                paramList.put("objectId", strECRId);
                columnMap.put("name", strFieldKey);
                columnMap.put("selectName", strFieldSelectKey);
                columnMap.put("attributeName", strFieldValueKey);
                paramsMap.put("paramList", paramList);
                paramsMap.put("requestMap", paramList);
                paramsMap.put("columnMap", columnMap);
                paramsMap.put("objectList", tableDataList);
                String strGetFieldValueMethodName = "";
                String strGetFieldAccessMethodName = "";
//                if ("JFECRCosting".equals(strSelectedTable)) {
                if (strSelectedTable.contains("JFECRCosting")) {
                    strGetFieldValueMethodName = "getAffectedItemsCostTableField";
                    strGetFieldAccessMethodName = "getJFECRAffectedItemsCostTableEditAccess";
//                } else if ("JFECRController".equals(strSelectedTable)) {
                } else if (strSelectedTable.contains("JFECRController")) {
                    strGetFieldValueMethodName = "getAffectedItemsCostTableField";
                    strGetFieldAccessMethodName = "getJFECRAffectedItemsMakeTableEditAccess";
                }
                //列显示信息
                StringList fieldValueList = JPO.invoke(context, "JF_ECRService", null, strGetFieldValueMethodName, JPO.packArgs(paramsMap), StringList.class);
                //列可编辑信息
                StringList fieldIsEditList = JPO.invoke(context, "JF_ECRService", null, strGetFieldAccessMethodName, JPO.packArgs(paramsMap), StringList.class);
                _logger.info("fieldValueList:{}", fieldValueList);
                _logger.info("fieldIsEditList:{}", fieldIsEditList);
                //更新 表格数据
                for (int i1 = 0; i1 < tableDataList.size(); i1++) {
                    Map rowData = (Map) tableDataList.get(i1);
                    rowData.put(JF_PublicMethodClass_mxJPO.buildStringInStrings(strFieldKey, "disPlayValue"), fieldValueList.get(i1));
                    rowData.put(JF_PublicMethodClass_mxJPO.buildStringInStrings(strFieldKey, "isEdit"), fieldIsEditList.get(i1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            _logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * 1.读取Excel中数据保存为MapList
     * 校验excel中修改数据是否为正实数 不符合要求直接返回
     * 2.获取该ECR关联的采购件、自制件清单数据
     * 3.遍历ECR关联的采购件、自制件清单数据，获取每一行可编辑的属性
     * 4.拿到可编辑的属性根据id去一一匹配更新属性 如果有关系直接更新 ，如果不存在关系需要新建
     */
    public Map readECRExcelAndUpdateData(Context context, String[] args) throws Exception {
        Map res = new HashMap<String, String>();
        Map paramMap = (Map) JPO.unpackArgs(args);
        List files = (List) paramMap.get("files");
        String strObjectId = (String) paramMap.get("objectId");
        String strSelectTable = (String) paramMap.get("type");
        java.io.File file = (java.io.File) files.get(0);
        String strMess = "";
        if (!file.exists()) {
            _logger.info("======================================文件不存在，请核对文件位置");
            strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRUploadNullError");
            res.put("code", "404");
            res.put("mess", strMess);
            return res;
        }
        InputStream inputStream = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputStream);
        //读取第一个sheet
        Sheet sheet = workbook.getSheetAt(0);
        //拿取sheet行信息
        int iFirstRowNum = sheet.getFirstRowNum();
        int iLastRowNum = sheet.getLastRowNum();
        if (iFirstRowNum == iLastRowNum) {
            _logger.info("======================================excel 内容错误");
            res.put("code", "404");
            strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRUploadContentError");
            res.put("mess", strMess);
            return res;
        }
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        String strLevel = "0";
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("objectId", strObjectId);
        stringStringHashMap.put("expandLevel", strLevel);
        //costing xml 映射
        MapList CostingMappingList = JF_PublicMethodClass_mxJPO.getECRTableMapping(context, strSelectTable);
        _logger.info("CostingMappingList:{}", CostingMappingList);
        //excel 标题和对应下标
        Map excelHeadInfoMap = getExcelHeadInfo(sheet);
        //校验excel数据
        Map checkMap = checkExcelData(sheet, excelHeadInfoMap, CostingMappingList);
        _logger.info("checkMap:{}", checkMap);
        boolean isCheckSuccess = (boolean) checkMap.get("checkResult");
        StringList errorRowIndex = (StringList) checkMap.get("errorRowIndex");
        if ((!isCheckSuccess) && errorRowIndex.size() > 0) {
            res.put("code", "404");
            strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRUploadCheckError");
            strMess = strMess.replace("{}", errorRowIndex.join(","));
            res.put("mess", strMess);
            return res;
        }
        MapList ecrCostingTableData = new MapList();
        //costing 表格数据
        if ("JFECRCosting".equals(strSelectTable)) {
            ecrCostingTableData = getECRCostingTableData(context, JPO.packArgs(stringStringHashMap));
        } else if ("JFECRController".equals(strSelectTable)) {
            ecrCostingTableData = getECRMakeTableData(context, JPO.packArgs(stringStringHashMap));
        }
        _logger.info("ecrCostingTableData:{}", ecrCostingTableData.size());
        //登录人会签角色
        MapList loginUserSignTaskRole = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECRAndUser(context, new String[]{strObjectId, context.getUser()});
        _logger.info("loginUserSignTaskRole:{}", loginUserSignTaskRole);
        //将role value 映射为 前端值 好直接调用之前方法
        Set roleNameSet = JF_ECRRESTService_mxJPO.getRoleKeyListByMapList(loginUserSignTaskRole);
        //获取excel中除标题数据
        List<Row> excelDataInfo = getExcelDataInfo(sheet);
        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            for (int i = 0; i < ecrCostingTableData.size(); i++) {
                Map tableMap = (Map) ecrCostingTableData.get(i);
                //获取可以编辑的属性
                StringList canAttributeNameList = JF_ECRRESTService_mxJPO.getEditAttributeRowInExcel(context, tableMap, ecrCostingTableData, strSelectTable, ecr, StringList.create(roleNameSet));
                //根据Map中的id 跟excel中的id 对应找到excel中修改的行,并获取修改的值转换为Map 进行更新
                updateECRTableData(context, tableMap, excelDataInfo, canAttributeNameList, CostingMappingList, excelHeadInfoMap, strObjectId, strSelectTable);
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            _logger.error("e:{}", e.getMessage());
            ContextUtil.abortTransaction(context);
            throw new RuntimeException(e);
        } finally {
            ContextUtil.popContext(context);
        }
        res.put("code", "200");
        strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRUploadSuccess");
        res.put("mess", strMess);
        return res;
    }

    /**
     * @param sheet
     * @return java.util.Map
     * @throws
     * @description 获取Excel中头部的信息
     * @author CHENYAN
     * @date 2024/10/9 16:51
     */

    public static Map getExcelHeadInfo(Sheet sheet) {
        Map headMap = new HashMap<String, String>();
        Row row = sheet.getRow(0);
        //拿取sheet行信息
        int iFirstRowNum = row.getFirstCellNum();
        int iLastRowNum = row.getLastCellNum();
        for (int i = iFirstRowNum; i < iLastRowNum; i++) {
            String strCell = row.getCell(i, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK) == null ? "" : getCellValue(row, i);
            if (UIUtil.isNotNullAndNotEmpty(strCell)) {
                headMap.put(strCell, i);
            }
        }
        return headMap;
    }

    /**
     * @param sheet
     * @return java.util.List<org.apache.poi.ss.usermodel.Row>
     * @throws
     * @description 获取excel中修改数据
     * @author CHENYAN
     * @date 2024/10/10 16:33
     */

    public static List<Row> getExcelDataInfo(Sheet sheet) {
        List<Row> rowList = new ArrayList<Row>();

        //拿取sheet最后行信息
        int iLastRowNum = sheet.getLastRowNum();
        //从第3行开始
        for (int i = 2; i <= iLastRowNum; i++) {
            rowList.add(sheet.getRow(i));
        }
        return rowList;
    }

    /**
     * @param headMap
     * @param xmlMapping
     * @param strRequiredAttribute
     * @return Map   key index  行下标
     * @return key id  表示
     * @return key value  属性名
     * @throws
     * @description 根据必填属性名称获取表格中行对应下标
     * @author CHENYAN
     * @date 2024/10/9 17:08
     */

    public static Map getRowIndexByRequiredAttribute(Map headMap, MapList xmlMapping, String strRequiredAttribute) {
        Map res = new HashMap<>();
        //防止属性和 xml中id不一致时使用
//        for (int i = 0; i < xmlMapping.size(); i++) {
//            Map xmlMap = (Map) xmlMapping.get(i);
//            String strFieldName = (String)xmlMap.get("id");
//            if (strFieldName.equals(strRequiredAttribute) && headMap.containsKey(strRequiredAttribute)){
//                int iRowIndex = (int) headMap.get(strRequiredAttribute);
//                res.put("index",iRowIndex);
//                res.put("id",strRequiredAttribute);
//                res.put("value",xmlMap.get("value"));
//            }
//        }
        if (headMap.containsKey(strRequiredAttribute)) {
            int iRowIndex = (int) headMap.get(strRequiredAttribute);
            res.put("index", iRowIndex);
            res.put("id", strRequiredAttribute);
        }
        return res;
    }

    /**
     * @param sheet      表格
     * @param headMap    表格第一行
     * @param xmlMapping 表格映射信息
     * @return void
     * @throws
     * @description 检查Excel中数据是否符合
     * @author CHENYAN
     * @date 2024/10/10 11:07
     */
    public static Map checkExcelData(Sheet sheet, Map headMap, MapList xmlMapping) {
        HashMap res = new HashMap<>();
        res.put("checkResult", true);
        int lastRowNum = sheet.getLastRowNum();
        //错误行统计
        StringList errorRowIndex = new StringList();
        //从第三行开始
        for (int i = 2; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            for (int i1 = 0; i1 < xmlMapping.size(); i1++) {
                Map xmlMap = (Map) xmlMapping.get(i1);
                //标识该属性是否可编辑
                String strIsEdit = (String) xmlMap.get("isEdit");
                String strFieldId = (String) xmlMap.get("id");
                //表格第一行信息中保存
                if (headMap.containsKey(strFieldId) && "true".equalsIgnoreCase(strIsEdit)) {
                    //拿到行下标
                    Integer rowIndex = (Integer) headMap.get(strFieldId);
                    if (rowIndex != null && rowIndex != -1) {
                        Cell cell = row.getCell(rowIndex);
                        //先保存为字符串
                        CellType cellType = cell.getCellType();
                        String strCellValue = "";
                        if (CellType.STRING.equals(cellType)) {
                            strCellValue = cell.getStringCellValue();
                        } else if (CellType.NUMERIC.equals(cellType)) {
                            strCellValue = String.valueOf(cell.getNumericCellValue());
                        }
                        if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                            try {
                                double dCellValue = Double.parseDouble(strCellValue);
                                // add by chenyan 解除不能输入负数校验 2025/05/19
//                                if (dCellValue < 0) {
//                                    _logger.error("strCellValue:{}", strCellValue);
//                                    errorRowIndex.add(String.valueOf(i + 1));
//                                    res.put("checkResult", false);
//                                }
                            } catch (NumberFormatException e) {
                                _logger.error("strCellValue:{}", strCellValue);
                                // 如果转换失败，说明不是有效的数字
                                errorRowIndex.add(String.valueOf(i + 1));
                                res.put("checkResult", false);
                            }
                        }
                    }
                }
            }
        }
        res.put("errorRowIndex", errorRowIndex);
        return res;
    }


    /**
     * @param context
     * @param dataMap             数据库数据
     * @param excelDataList       所有excel行
     * @param updateAttributeList 更新的属性集合
     * @param xmlMapping          属性映射
     * @param headMap             excel头信息
     * @param strECRId            ecr id
     * @param strTableName        excel 标识
     * @return void
     * @throws
     * @description 根据数据库中数据找到对应excel中数据更新
     * @author CHENYAN
     * @date 2024/11/26 14:00
     */
    public static void updateECRTableData(Context context, Map dataMap, List<Row> excelDataList, StringList updateAttributeList, MapList xmlMapping, Map headMap, String strECRId, String strTableName) throws Exception {
        if (updateAttributeList != null && updateAttributeList.size() > 0) {
            String strRelName = "";
            //根据Table获取不同的关系
            if ("JFECRCosting".equals(strTableName)) {
                strRelName = "JFECR2PartPrice";
            } else if ("JFECRController".equals(strTableName)) {
                strRelName = "JFECR2MakePartPrice";
            }
            List<Row> mappingDataList = getExcelRowDataByMap(dataMap, excelDataList, headMap, xmlMapping);
            _logger.info("mappingDataList:{}", mappingDataList);
            Map updateAttributeValueMap = new HashMap<String, String>();
            String strUpdateRelId = "";
            if (mappingDataList != null && mappingDataList.size() > 0) {
                for (int i = 0; i < mappingDataList.size(); i++) {
                    Row updateRow = mappingDataList.get(i);
                    for (int i1 = 0; i1 < updateAttributeList.size(); i1++) {
                        //获取更新的关系id
                        Map relMap = getRowIndexByRequiredAttribute(headMap, xmlMapping, SELECT_RELATIONSHIP_ID);
                        String strRelKey = (String) relMap.get("id");
                        if (UIUtil.isNotNullAndNotEmpty(strRelKey)) {
                            int iRelIndex = (int) relMap.get("index");
                            strUpdateRelId = getCellValue(updateRow, iRelIndex);
                        }
                        String strRequireAttributeName = updateAttributeList.get(i1);
                        Map requiredAttributeMap = getRowIndexByRequiredAttribute(headMap, xmlMapping, strRequireAttributeName);
                        String strAttributeName = (String) requiredAttributeMap.get("id");
                        //防止没有找到映射属性
                        if (UIUtil.isNotNullAndNotEmpty(strAttributeName)) {
                            int rowIndex = (int) requiredAttributeMap.get("index");
                            String strCellValue = getCellValue(updateRow, rowIndex);
                            _logger.info("strCellValue:{}", strCellValue);
                            //获取到要更新的属性值
                            if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                                //默认保留两位小数
                                String str2FCellValue = String.format("%.2f", Double.parseDouble(strCellValue));
                                updateAttributeValueMap.put(strAttributeName, str2FCellValue);
                            }
                        }
                    }
                }
                DomainRelationship rel;
                _logger.info("strUpdateRelId:{}", strUpdateRelId);
                //add by chenyan 2024/10/22 判断是否已经新建了如果已经新建了查询到关系id
                if (UIUtil.isNullOrEmpty(strUpdateRelId)) {
                    String strPartId = (String) dataMap.get(SELECT_ID);
                    strUpdateRelId = checkECRIsConnectPricePart(context, strECRId, strPartId, strRelName);
                    if (UIUtil.isNullOrEmpty(strUpdateRelId)) {
                        DomainObject part = DomainObject.newInstance(context, strPartId);
                        rel = DomainRelationship.connect(context, DomainObject.newInstance(context, strECRId), strRelName, part);
                    } else {
                        rel = DomainRelationship.newInstance(context, strUpdateRelId);
                    }

                } else {
                    rel = DomainRelationship.newInstance(context, strUpdateRelId);
                }
                _logger.info("updateAttributeValueMap:{}", updateAttributeValueMap);
                rel.setAttributeValues(context, updateAttributeValueMap);
            }
        }

    }

    /**
     * @param dataMap       数据库数据
     * @param excelDataList excel 所有行
     * @param headMap       头信息
     * @param xmlMapping    xml excel 映射
     * @return java.util.List<org.apache.poi.ss.usermodel.Row>
     * @throws
     * @description 根据数据库 id 过滤出excel中id相同行
     * @author CHENYAN
     * @date 2024/11/26 14:03
     */
    public static List<Row> getExcelRowDataByMap(Map dataMap, List<Row> excelDataList, Map headMap, MapList xmlMapping) {
        //获取ID的下标映射
        Map idIndexMap = getRowIndexByRequiredAttribute(headMap, xmlMapping, SELECT_ID);
        int iIndex = (int) idIndexMap.get("index");
        String strDataId = (String) dataMap.get(SELECT_ID);
        List<Row> filterMapList = (List) excelDataList.stream().filter(excelMap -> {
            Row excelMapData = (Row) excelMap;
            Cell idCell = excelMapData.getCell(iIndex);
            String strExcelDataId = (String) idCell.getStringCellValue();
            return strDataId.equals(strExcelDataId);
        }).collect(Collectors.toList());
        return filterMapList;
    }

    /**
     * @param context
     * @param strECRId
     * @param strPartId
     * @param strRelName
     * @return java.lang.String
     * @throws
     * @description 检查ECR和part是否已经关联关系id
     * @author CHENYAN
     * @date 2024/10/22 15:57
     */
    public static String checkECRIsConnectPricePart(Context context, String strECRId, String strPartId, String strRelName) throws Exception {
        Map parameterMap = new HashMap<>();
        parameterMap.put("fromId", strECRId);
        parameterMap.put("toId", strPartId);
        parameterMap.put("relName", strRelName);
        return JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(parameterMap));
    }

    /**
     * @param workbook
     * @param project
     * @return org.apache.poi.ss.usermodel.Workbook
     * @throws
     * @description 获取项目关联所有ECR
     * @author CHENYAN
     * @date 2024/10/30 13:34
     */
    public static Workbook getProjectAllECRByPId(Context context, Workbook workbook, DomainObject project, CellStyle cellBorderStyle) throws Exception {
        try {
            Map projectInfo = project.getInfo(context, StringList.create(SELECT_NAME, SELECT_NAME, SELECT_DESCRIPTION));
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
            typeSelectList.add(SELECT_ATTR_JFECRTYPE);
            typeSelectList.add(SELECT_ATTR_JFCHANGESOURCE);
            typeSelectList.add(SELECT_ATTR_JFISPLATFORMPART);
            typeSelectList.add(SELECT_ATTR_JFProjectPhase);
            typeSelectList.add(SELECT_ATTR_JFAFFECTEDFACTORY);
            typeSelectList.add(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS);
            typeSelectList.add(SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL);
            typeSelectList.add(SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURS);
            typeSelectList.add(SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL);
            typeSelectList.add(SELECT_ATTR_JFCHANGERESON);
            typeSelectList.add(SELECT_DESCRIPTION);
            typeSelectList.add(SELECT_ATTR_JFECRTYPE);
            typeSelectList.add(SELECT_OWNER);
            typeSelectList.add(SELECT_ORIGINATED);
            typeSelectList.add("to[JFChangeEventECR].from.name");
            typeSelectList.add("from[JFECR2CO].to.name");
            StringList reSelectList = JF_Util_mxJPO.basicRellistSel();
            MapList maps = project.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JFChange2Project, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_JFECR,                                    // object pattern
                    typeSelectList,                            // object selects
                    reSelectList, // relationship selects
                    true,                                        // to direction
                    false,                                        // from direction
                    (short) 1,                                    // recursion level
                    "current!=Create",                // object where clause
                    "",
                    (short) 0);
            _logger.info("maps:{}", maps);
            Sheet sheet = workbook.getSheetAt(0);
            //获取xml表格首页映射
            MapList costChangeHistoryMapping0 = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "CostChangeHistory_0");
            _logger.info("costChangeHistoryMapping0:{}", costChangeHistoryMapping0);
            for (int i = 1; i <= maps.size(); i++) {
                Row row = sheet.createRow(i);
                Map rowData = (Map) maps.get(i - 1);
                //往 mapdata中添加项目数据
                rowData.put("projectCode", projectInfo.get(SELECT_NAME));
                rowData.put("projectName", projectInfo.get(SELECT_DESCRIPTION));
                for (Object entry : rowData.entrySet()) {
                    Map.Entry entryMap = (Map.Entry) entry;
                    String strKey = (String) entryMap.getKey();
                    String strIndex = "";
                    String strCellValue = "";
                    //初始化单元格值 防止属性中存在range值多选的情况
                    Object oCellValue = entryMap.getValue();
                    boolean isMul = false;
                    if (oCellValue instanceof StringList) {
                        isMul = true;
                    } else {
                        strCellValue = (String) oCellValue;
                    }
                    //获取excel中对应下标
                    for (int i1 = 0; i1 < costChangeHistoryMapping0.size(); i1++) {
                        Map mappingMap = (Map) costChangeHistoryMapping0.get(i1);
                        String strMappingId = (String) mappingMap.get("id");
                        if (mappingMap.containsValue(strKey)) {
                            strIndex = (String) mappingMap.get("value");
                            //格式化处理
                            String strFormat = (String) mappingMap.get("format");
                            if (UIUtil.isNotNullAndNotEmpty(strFormat)) {
                                String strNlsKey = (String) mappingMap.get("nlsKey");
                                if ("date".equals(strFormat)) {

                                } else if ("range".equals(strFormat)) {
                                    if (isMul) {
                                        StringList mulValueList = (StringList) oCellValue;
                                        if (mulValueList.size() > 0) {
                                            StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, strNlsKey, mulValueList, context.getLocale().toString());
                                            strCellValue = nlsRanges.join("\n");
                                        }
                                    } else {
                                        if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                                            strCellValue = EnoviaResourceBundle.getRangeI18NString(context, strNlsKey, strCellValue, context.getLocale().getLanguage());
                                        }
                                    }
                                } else if ("policy".equals(strFormat)) {
                                    strCellValue = EnoviaResourceBundle.getStateI18NString(context, strNlsKey, strCellValue, context.getLocale().toString());
                                } else if ("user".equals(strFormat)) {
                                    strCellValue = PersonUtil.getFullName(context, strCellValue);
                                    _logger.info("strCellValue:{}", strCellValue);
                                }
                            }
                            break;
                        }
                    }

                    if (UIUtil.isNotNullAndNotEmpty(strIndex)) {
                        Cell cell = row.createCell(Integer.valueOf(strIndex));
                        cell.setCellValue(strCellValue);
                    }
                }
            }
//            setCellBoard(sheet,cellBorderStyle,2);
            return workbook;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param workbook
     * @param project
     * @return org.apache.poi.ss.usermodel.Workbook
     * @throws
     * @description 获取项目关联所有ECR的成本清单
     * @author CHENYAN
     * @date 2024/10/30 13:34
     */
    public static Workbook getProjectAllECRCostDataByPId(Context context, Workbook workbook, DomainObject project, CellStyle cellBorderStyle) throws Exception {
        try {
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
            //Launch_Manager
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFMaterialsFinishedProductsRework);
            //APR
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestment);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesDevelopment);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMould);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSumInventoryScrapAmount);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentExternal);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesDevelopmentExternal);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesMouldExternal);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFSumInventoryScrapAmountExternal);
            //AQE
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesFixtures);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfQuality);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfTrial);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesFixturesExternal);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfQualityExternal);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFCostOfTrialExternal);
            //AME
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsWholeChair);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFoaming);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFaceCovers);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsWholeChairExternal);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFoamingExternal);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangesInvestmentsFaceCoversExternal);
            StringList reSelectList = JF_Util_mxJPO.basicRellistSel();
            MapList maps = project.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_JFChange2Project, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_JFECR,                                    // object pattern
                    typeSelectList,                            // object selects
                    reSelectList, // relationship selects
                    true,                                        // to direction
                    false,                                        // from direction
                    (short) 1,                                    // recursion level
                    "current!=Create",                // object where clause
                    "",
                    (short) 0);
            _logger.info("maps:{}", maps);
            Sheet sheet = workbook.getSheetAt(1);
            //获取xml表格首页映射
            MapList costChangeHistoryMapping0 = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "CostChangeHistory_1");
            _logger.info("costChangeHistoryMapping1:{}", costChangeHistoryMapping0);
            for (int i = 2; i <= (maps.size() + 1); i++) {
                Row row = sheet.createRow(i);
                Map rowData = (Map) maps.get(i - 2);
                //往 mapdata中添加项目数据
                for (Object entry : rowData.entrySet()) {
                    Map.Entry entryMap = (Map.Entry) entry;
                    String strKey = (String) entryMap.getKey();
                    String strIndex = "";
                    String strCellValue = "";
                    //初始化单元格值 防止属性中存在range值多选的情况
                    Object oCellValue = entryMap.getValue();
                    boolean isMul = false;
                    if (oCellValue instanceof StringList) {
                        isMul = true;
                    } else {
                        strCellValue = (String) oCellValue;
                    }
                    //获取excel中对应下标
                    for (int i1 = 0; i1 < costChangeHistoryMapping0.size(); i1++) {
                        Map mappingMap = (Map) costChangeHistoryMapping0.get(i1);
                        if (mappingMap.containsValue(strKey)) {
                            strIndex = (String) mappingMap.get("value");
                            break;
                        }
                    }

                    if (UIUtil.isNotNullAndNotEmpty(strIndex)) {
                        Cell cell = row.createCell(Integer.valueOf(strIndex));
                        cell.setCellValue(strCellValue);
                    }
                }
            }
//            setCellBoard(sheet,cellBorderStyle,2);
            return workbook;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param context
     * @param workbook
     * @param project
     * @param cellBorderStyle
     * @return org.apache.poi.ss.usermodel.Workbook
     * @throws
     * @description 获取项目关联整椅非自制件的成本价格
     * @author CHENYAN
     * @date 2024/11/6 10:17
     */
    public static Workbook getProjectAllPartCostDataByPId(Context context, Workbook workbook, DomainObject project, CellStyle cellBorderStyle) throws Exception {
        try {
            //获取xml表格首页映射
            MapList costChangeHistoryMapping0 = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "CostChangeHistory_3");
            _logger.info("costChangeHistoryMapping3:{}", costChangeHistoryMapping0);
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType);
            typeSelectList.add(SELECT_CURRENT);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
            String strSelectParentProcurementType = JF_PublicMethodClass_mxJPO.buildStringInStrings("from.", JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
            StringList reSelectList = JF_Util_mxJPO.basicRellistSel();
            reSelectList.add(strSelectParentProcurementType);
            //该API只能返回一个 废除
//            StringList rootPartIdList = project.getInfoList(context, JF_PLMConstants_mxJPO.SELECT_PROJECT_ROOT_PART);
            StringList rootPartIdList = JF_ECRService_mxJPO.getProjectRootPartByProjectId(context, project);
            _logger.info("rootPartIdList:{}", rootPartIdList);
            StringList partSelectList = JF_Util_mxJPO.basicBolistSel();
            partSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_IsLastVersion);
            partSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            partSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType);
            partSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
            partSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
            partSelectList.add("to[JFRelateItem].from.name");
            partSelectList.add("to[JFRelateItem].from.current");
            partSelectList.add("to[JFRelateItem].from.modified");
            StringList priceSelectList = JF_Util_mxJPO.basicBolistSel();
            getECRPriceSelect(priceSelectList);
            MapList allPartMapList = new MapList();
            if (rootPartIdList.size() > 0) {
                for (int i = 0; i < rootPartIdList.size(); i++) {
                    String strRootPartId = rootPartIdList.get(i);
//                    StringList split = FrameworkUtil.split(strRootPartId, "=");
//                    strRootPartId = split.get(1).trim();
                    _logger.info("strRootPartId:{}", strRootPartId);
                    DomainObject rootPart = DomainObject.newInstance(context, strRootPartId);
                    //展开整椅件
                    MapList partMapList = rootPart.getRelatedObjects(context,
                            JF_PLMConstants_mxJPO.REL_Instance, // relationship pattern
                            JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                            typeSelectList,                            // object selects
                            reSelectList, // relationship selects
                            false,                                        // to direction
                            true,                                        // from direction
                            (short) 0,                                    // recursion level
                            "",                // object where clause
                            "",
                            (short) 0);
                    DomainObject part = DomainObject.newInstance(context);
                    MapList newPartMapList = (MapList) partMapList.stream().filter(m -> {
                        Map partInfo = (Map) m;
                        String strProcurementType = (String) partInfo.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                        String strParentProcurementType = (String) partInfo.get(strSelectParentProcurementType);
                        return (!"make".equals(strProcurementType)) && !("buy".equals(strParentProcurementType) || "ICO".equals(strParentProcurementType));
                    }).map(m -> {
                        //遍历过滤后的零件，查询出所有的版本 和关联ECR价格相关数据
                        Map partInfo = (Map) m;
                        String strPartId = (String) partInfo.get(SELECT_ID);
                        try {
                            part.setId(strPartId);
                            //获取零件所有版本
                            MapList revisionPartsInfo = part.getRevisionsInfo(context, partSelectList, StringList.create());
                            Map newPartMap = new HashMap<String, String>();
                            revisionPartsInfo.stream().forEach(oPartMap -> {
                                Map partMap = (Map) oPartMap;
                                String strIsLastRevision = (String) partMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_IsLastVersion);
                                //统计最新版零件和关联ECR
                                if ("TRUE".equals(strIsLastRevision)) {
                                    newPartMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER, (String) partMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER));
                                    newPartMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType, (String) partMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType));
                                    newPartMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN, (String) partMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN));
                                    newPartMap.put(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN, (String) partMap.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN));
                                    newPartMap.put(SELECT_REVISION, (String) partMap.get(SELECT_REVISION));
                                    newPartMap.put(SELECT_CURRENT, (String) partMap.get(SELECT_CURRENT));
                                    newPartMap.put("to[JFRelateItem].from.name", (String) partMap.get("to[JFRelateItem].from.name"));
                                    newPartMap.put("to[JFRelateItem].from.current", (String) partMap.get("to[JFRelateItem].from.current"));
                                    newPartMap.put("to[JFRelateItem].from.modified", (String) partMap.get("to[JFRelateItem].from.modified"));
                                }
                                //获取零件关联ECR价格可能会存在多个 一级件下面子件走不通ECR
                                String strRevisionPartId = (String) partMap.get(SELECT_ID);
                                try {
                                    DomainObject revisionPart = DomainObject.newInstance(context, strRevisionPartId);
                                    MapList priceMapList = revisionPart.getRelatedObjects(context,
                                            JF_PLMConstants_mxJPO.REL_JFECR2PartPrice, // relationship pattern
                                            TYPE_JFECR,                                    // object pattern
                                            JF_Util_mxJPO.basicBolistSel(),                            // object selects
                                            priceSelectList, // relationship selects
                                            true,                                        // to direction
                                            false,                                        // from direction
                                            (short) 1,                                    // recursion level
                                            "",                // object where clause
                                            "",
                                            (short) 0);
                                    for (int i1 = 0; i1 < priceMapList.size(); i1++) {
                                        Map priceMap = (Map) priceMapList.get(i1);
                                        //统计各个ECR的价格
                                        for (Object oEntry : priceMap.entrySet()) {
                                            Map.Entry entry = (Map.Entry) oEntry;
                                            String strKey = (String) entry.getKey();
                                            String strValue = (String) entry.getValue();
                                            if (UIUtil.isNotNullAndNotEmpty(strValue)) {
                                                //说明是价格属性
                                                if (strKey.contains("attribute[")) {
                                                    if (newPartMap.containsKey(strKey)) {
                                                        String strSumPrice = (String) newPartMap.get(strKey);
                                                        BigDecimal addBigDecimal = new BigDecimal(strValue);
                                                        BigDecimal sumBigDecimal = new BigDecimal(strSumPrice);
                                                        sumBigDecimal = sumBigDecimal.add(addBigDecimal);
                                                        //四舍五入两位小数
                                                        sumBigDecimal.setScale(2, RoundingMode.HALF_UP);
                                                        newPartMap.put(strKey, sumBigDecimal.toString());
                                                    } else {
                                                        newPartMap.put(strKey, strValue);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    _logger.info(e.getMessage());
                                }
                            });
                            return newPartMap;
                        } catch (Exception e) {
                            _logger.info(e.getMessage());
                        }
                        return new HashMap();
                    }).collect(Collectors.toCollection(MapList::new));
                    _logger.info("newPartMapList:{}", newPartMapList);
                    allPartMapList.addAll(newPartMapList);
                }
            }
            _logger.info("allPartMapList:{}", allPartMapList);
            Sheet sheet = workbook.getSheetAt(3);

            for (int i = 2; i <= allPartMapList.size() + 1; i++) {
                Row row = sheet.createRow(i);
                int rowIndex = i - 2;
                Map rowData = (Map) allPartMapList.get(rowIndex);
                // 往数据里面添加求和的列
                addSumKeyByMapData(rowData);
                //往 mapdata中添加项目数据
                for (Object entry : rowData.entrySet()) {
                    Map.Entry entryMap = (Map.Entry) entry;
                    String strKey = (String) entryMap.getKey();
                    String strIndex = "";
                    boolean isFun = false;
                    String strFunValue = "";
                    String strCellValue = (String) entryMap.getValue();
                    //获取excel中对应下标
                    for (int i1 = 0; i1 < costChangeHistoryMapping0.size(); i1++) {
                        Map mappingMap = (Map) costChangeHistoryMapping0.get(i1);
                        if (mappingMap.containsValue(strKey)) {
                            strIndex = (String) mappingMap.get("value");
                            //格式化处理
                            String strFormat = (String) mappingMap.get("format");
                            if (UIUtil.isNotNullAndNotEmpty(strFormat)) {
                                String strNlsKey = (String) mappingMap.get("nlsKey");
                                if ("date".equals(strFormat)) {
                                    if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                                        _logger.info("strCellValue:{}", strCellValue);
                                        //转换时间
                                        Date javaDate = eMatrixDateFormat.getJavaDate(strCellValue, context.getLocale());
                                        LocalDateTime localDateTime = javaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                                        strCellValue = formatter.format(localDateTime);
                                    }
                                } else if ("range".equals(strFormat)) {
                                    if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                                        strCellValue = EnoviaResourceBundle.getRangeI18NString(context, strNlsKey, strCellValue, context.getLocale().getLanguage());
                                    }
                                } else if ("policy".equals(strFormat)) {
                                    if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                                        strCellValue = EnoviaResourceBundle.getStateI18NString(context, strNlsKey, strCellValue, context.getLocale().toString());
                                    }
                                } else if ("user".equals(strFormat)) {
                                    strCellValue = PersonUtil.getFullName(context, strCellValue);
                                    _logger.info("strCellValue:{}", strCellValue);
                                } else if ("function".equals(strFormat)) {
                                    isFun = true;
                                    strFunValue = (String) mappingMap.get("funValue");
                                    _logger.info("strFunValue:{}", strFunValue);
                                }
                            }

                            break;
                        }
                    }
                    if (UIUtil.isNotNullAndNotEmpty(strIndex)) {
                        Cell cell = row.createCell(Integer.valueOf(strIndex));
                        if (isFun) {
                            // 将列下标转换为列字母
                            //String colLetter1 = new CellReference(0, Integer.parseInt(strIndex)).
                            //funValue="VALUE(|{11}|)+VALUE(|{17}|)"  以 | 切割 然后找到 以{ 开始 }结束 的字符串 获取中间下标值 转换为列字母 拼接行信息 组装为完整函数
                            StringTokenizer strTokenizer = new StringTokenizer(strFunValue, "|");
                            //公式拼接
                            StringBuffer sb = new StringBuffer();
                            while (strTokenizer.hasMoreTokens()) {
                                String strTmpFunValue = strTokenizer.nextToken();
                                if (strTmpFunValue.startsWith("{") && strTmpFunValue.endsWith("}")) {
                                    //获取中间列下标
                                    strTmpFunValue = strTmpFunValue.substring(1, strTmpFunValue.length() - 1);
                                    //转换为字母加行数 A1 C2 这种
                                    strTmpFunValue = new CellReference(i, Integer.parseInt(strTmpFunValue)).formatAsString();
                                }
                                sb.append(strTmpFunValue);
                            }
                            cell.setCellFormula(sb.toString());
                        } else {
                            cell.setCellValue(strCellValue);
                        }
                    }
                }
            }
//            setCellBoard(sheet,cellBorderStyle,2);
            return workbook;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param context
     * @param workbook
     * @param project
     * @param cellBorderStyle
     * @return org.apache.poi.ss.usermodel.Workbook
     * @throws
     * @description 获取项目下
     * @author CHENYAN
     * @date 2024/11/27 13:44
     */
    public static Workbook getProjectOnePartCostDataByPId(Context context, Workbook workbook, DomainObject project, CellStyle cellBorderStyle) throws Exception {
        try {
            //获取xml表格首页映射
            MapList costChangeHistoryMapping0 = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "CostChangeHistory_4");
            _logger.info("costChangeHistoryMapping4:{}", costChangeHistoryMapping0);
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType);
            typeSelectList.add(SELECT_CURRENT);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
            typeSelectList.add("to[JFRelateItem].from.name");
            typeSelectList.add("to[JFRelateItem]");
            typeSelectList.add("to[JFRelateItem].from.current");
            typeSelectList.add("to[JFRelateItem].from.modified");
            String strSelectParentProcurementType = JF_PublicMethodClass_mxJPO.buildStringInStrings("from.", JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
            StringList reSelectList = JF_Util_mxJPO.basicRellistSel();
            reSelectList.add(strSelectParentProcurementType);
            StringList rootPartIdList = JF_ECRService_mxJPO.getProjectRootPartByProjectId(context, project);
//            StringList rootPartIdList = project.getInfoList(context, JF_PLMConstants_mxJPO.SELECT_PROJECT_ROOT_PART);
            StringList priceSelectList = JF_Util_mxJPO.basicBolistSel();
            getECRPriceSelect(priceSelectList);
            MapList allPartMapList = new MapList();
            if (rootPartIdList.size() > 0) {
                for (int i = 0; i < rootPartIdList.size(); i++) {
                    String strRootPartId = rootPartIdList.get(i);
//                    StringList split = FrameworkUtil.split(strRootPartId, "=");
//                    strRootPartId = split.get(1).trim();
                    _logger.info("strRootPartId:{}", strRootPartId);
                    DomainObject rootPart = DomainObject.newInstance(context, strRootPartId);
                    //展开整椅件
                    MapList partMapList = rootPart.getRelatedObjects(context,
                            JF_PLMConstants_mxJPO.REL_Instance, // relationship pattern
                            JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                            typeSelectList,                            // object selects
                            reSelectList, // relationship selects
                            false,                                        // to direction
                            true,                                        // from direction
                            (short) 0,                                    // recursion level
                            "",                // object where clause
                            "",
                            (short) 0);
                    DomainObject part = DomainObject.newInstance(context);
                    MapList newPartMapList = (MapList) partMapList.stream().filter(m -> {
                        Map partInfo = (Map) m;
                        String strIsConnection = (String) partInfo.get("to[JFRelateItem]");
                        String strProcurementType = (String) partInfo.get(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
                        String strParentProcurementType = (String) partInfo.get(strSelectParentProcurementType);
                        return (!"make".equals(strProcurementType)) && (!("buy".equals(strParentProcurementType) || "ICO".equals(strParentProcurementType))) && ("TRUE".equalsIgnoreCase(strIsConnection));
                    }).map(m -> {
                        try {
                            Map partInfo = (Map) m;
                            String strPartId = (String) partInfo.get(SELECT_ID);
                            part.setId(strPartId);
                            MapList priceMapList = part.getRelatedObjects(context,
                                    JF_PLMConstants_mxJPO.REL_JFECR2PartPrice, // relationship pattern
                                    TYPE_JFECR,                                    // object pattern
                                    JF_Util_mxJPO.basicBolistSel(),                            // object selects
                                    priceSelectList, // relationship selects
                                    true,                                        // to direction
                                    false,                                        // from direction
                                    (short) 1,                                    // recursion level
                                    "",                // object where clause
                                    "",
                                    (short) 0);
                            for (int i1 = 0; i1 < priceMapList.size(); i1++) {
                                Map priceMap = (Map) priceMapList.get(i1);
                                //统计各个ECR的价格
                                for (Object oEntry : priceMap.entrySet()) {
                                    Map.Entry entry = (Map.Entry) oEntry;
                                    String strKey = (String) entry.getKey();
                                    String strValue = (String) entry.getValue();
                                    if (UIUtil.isNotNullAndNotEmpty(strValue)) {
                                        //说明是价格属性
                                        if (strKey.contains("attribute[")) {
                                            if (partInfo.containsKey(strKey)) {
                                                String strSumPrice = (String) partInfo.get(strKey);
                                                BigDecimal addBigDecimal = new BigDecimal(strValue);
                                                BigDecimal sumBigDecimal = new BigDecimal(strSumPrice);
                                                sumBigDecimal = sumBigDecimal.add(addBigDecimal);
                                                //四舍五入两位小数
                                                sumBigDecimal.setScale(2, RoundingMode.HALF_UP);
                                                partInfo.put(strKey, sumBigDecimal.toString());
                                            } else {
                                                partInfo.put(strKey, strValue);
                                            }
                                        }
                                    }
                                }
                            }
                            return partInfo;
                        } catch (Exception e) {
                            _logger.info(e.getMessage());
                        }
                        return new HashMap();
                    }).collect(Collectors.toCollection(MapList::new));
                    allPartMapList.addAll(newPartMapList);
                }
            }
            _logger.info("allPartMapList:{}", allPartMapList);
            Sheet sheet = workbook.getSheetAt(4);

            for (int i = 2; i <= allPartMapList.size() + 1; i++) {
                int rowIndex = i - 2;
                Row row = sheet.createRow(rowIndex);
                Map rowData = (Map) allPartMapList.get(rowIndex);
                //往 mapdata中添加项目数据
                for (Object entry : rowData.entrySet()) {
                    Map.Entry entryMap = (Map.Entry) entry;
                    String strKey = (String) entryMap.getKey();
                    String strIndex = "";
                    boolean isFun = false;
                    String strFunValue = "";
                    String strCellValue = (String) entryMap.getValue();
                    //获取excel中对应下标
                    for (int i1 = 0; i1 < costChangeHistoryMapping0.size(); i1++) {
                        Map mappingMap = (Map) costChangeHistoryMapping0.get(i1);
                        if (mappingMap.containsValue(strKey)) {
                            strIndex = (String) mappingMap.get("value");
                            //格式化处理
                            String strFormat = (String) mappingMap.get("format");
                            if (UIUtil.isNotNullAndNotEmpty(strFormat)) {
                                String strNlsKey = (String) mappingMap.get("nlsKey");
                                if ("date".equals(strFormat)) {
                                    if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                                        //转换时间
                                        Date javaDate = eMatrixDateFormat.getJavaDate(strCellValue, context.getLocale());
                                        LocalDateTime localDateTime = javaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                                        strCellValue = formatter.format(localDateTime);
                                    }
                                } else if ("range".equals(strFormat)) {
                                    if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                                        strCellValue = EnoviaResourceBundle.getRangeI18NString(context, strNlsKey, strCellValue, context.getLocale().getLanguage());
                                    }
                                } else if ("policy".equals(strFormat)) {
                                    if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                                        strCellValue = EnoviaResourceBundle.getStateI18NString(context, strNlsKey, strCellValue, context.getLocale().toString());
                                    }
                                } else if ("user".equals(strFormat)) {
                                    strCellValue = PersonUtil.getFullName(context, strCellValue);
                                    _logger.info("strCellValue:{}", strCellValue);
                                } else if ("function".equals(strFormat)) {
                                    isFun = true;
                                    strFunValue = (String) mappingMap.get("funValue");
                                    _logger.info("strFunValue:{}", strFunValue);
                                }
                            }

                            break;
                        }
                    }
                    if (UIUtil.isNotNullAndNotEmpty(strIndex)) {
                        Cell cell = row.createCell(Integer.valueOf(strIndex));
                        if (isFun) {
                            // 将列下标转换为列字母
                            //String colLetter1 = new CellReference(0, Integer.parseInt(strIndex)).
                            //funValue="VALUE(|{11}|)+VALUE(|{17}|)"  以 | 切割 然后找到 以{ 开始 }结束 的字符串 获取中间下标值 转换为列字母 拼接行信息 组装为完整函数
                            StringTokenizer strTokenizer = new StringTokenizer(strFunValue, "|");
                            //公式拼接
                            StringBuffer sb = new StringBuffer();
                            while (strTokenizer.hasMoreTokens()) {
                                String strTmpFunValue = strTokenizer.nextToken();
                                if (strTmpFunValue.startsWith("{") && strTmpFunValue.endsWith("}")) {
                                    //获取中间列下标
                                    strTmpFunValue = strTmpFunValue.substring(1, strTmpFunValue.length() - 1);
                                    //转换为字母加行数 A1 C2 这种
                                    strTmpFunValue = new CellReference(i, Integer.parseInt(strTmpFunValue)).formatAsString();
                                }
                                sb.append(strTmpFunValue);
                            }
                            cell.setCellFormula(sb.toString());
                        } else {
                            cell.setCellValue(strCellValue);
                        }
                    }
                }
            }
//            setCellBoard(sheet,cellBorderStyle,2);
            return workbook;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param context
     * @param workbook
     * @param project
     * @param cellBorderStyle
     * @return org.apache.poi.ss.usermodel.Workbook
     * @throws
     * @description 获取项目自制件数据
     * @author CHENYAN
     * @date 2024/11/27 13:46
     */
    public static Workbook getProjectOneMakePartDataByPId(Context context, Workbook workbook, DomainObject project, CellStyle cellBorderStyle) throws Exception {
        try {
            //获取xml表格首页映射
            MapList costChangeHistoryMapping0 = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "CostChangeHistory_2");
            _logger.info("costChangeHistoryMapping2:{}", costChangeHistoryMapping0);
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartType);
            typeSelectList.add(SELECT_CURRENT);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
            typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
            StringList reSelectList = JF_Util_mxJPO.basicRellistSel();
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeManHour);
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeManHour_External);
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeSeatCost);
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeSeatCostExternal);
            reSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFChangeTargetPrice);
            //获取项目关联所有ECR
            DomainObject ecr = DomainObject.newInstance(context);
            StringList ecrIdList = project.getInfoList(context, "to[JFChange2Project].from.id");
            String strLevel = "0";
            HashMap<String, String> stringStringHashMap = new HashMap<>();
            stringStringHashMap.put("expandLevel", strLevel);
            //实际写入excel中集合
            MapList realMakeMapList = new MapList();
            for (int i = 0; i < ecrIdList.size(); i++) {
                String strECRId = ecrIdList.get(i);
                ecr.setId(strECRId);
                Map ecrInfo = ecr.getInfo(context, StringList.create(SELECT_NAME, SELECT_CURRENT));
                stringStringHashMap.put("objectId", strECRId);
                // add by chenyan 2025/02/13 排除掉创建状态的ECR
                String strCurrent = ecr.getInfo(context, "current");
                if ("Create".equals(strCurrent)) {
                    continue;
                }
                //获取ECR自制件清单数据
                MapList makeTableMapList = JPO.invoke(context, "JF_ECRService", null, "getECRMakeTableData", JPO.packArgs(stringStringHashMap), MapList.class);
                //获取ECR中关联价格关系
                MapList priceMapList = ecr.getRelatedObjects(context,
                        JF_PLMConstants_mxJPO.REL_JFECR2MakePartPrice, // relationship pattern
                        JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                        typeSelectList,                            // object selects
                        reSelectList, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 1,                                    // recursion level
                        "current!=Create",                // object where clause
                        "",
                        (short) 0);
                for (int i1 = 0; i1 < makeTableMapList.size(); i1++) {
                    Map partInfo = (Map) makeTableMapList.get(i1);
                    partInfo.put("ECRName", ecrInfo.get(SELECT_NAME));
                    partInfo.put("ECRCurrent", ecrInfo.get(SELECT_CURRENT));
                    String strPartId = (String) partInfo.get(SELECT_ID);
                    for (int i2 = 0; i2 < priceMapList.size(); i2++) {
                        Map priceMap = (Map) priceMapList.get(i2);
                        String strPricePartId = (String) priceMap.get(SELECT_ID);
                        if (strPartId.equals(strPricePartId)) {
                            Set entry = priceMap.entrySet();
                            for (Object oEntry : entry) {
                                Map.Entry mEntry = (Map.Entry) oEntry;
                                String strKey = (String) mEntry.getKey();
                                String strValue = (String) mEntry.getValue();
                                //将价格属性加入到partInfo中
                                if (!partInfo.containsKey(strKey)) {
                                    partInfo.put(strKey, strValue);
                                }
                            }
                            break;
                        }
                    }
                    realMakeMapList.add(partInfo);
                }
            }
            _logger.info("realMakeMapList:{}", realMakeMapList);
            //excel写入
            Sheet sheet = workbook.getSheetAt(2);

            for (int i = 2; i <= realMakeMapList.size() + 1; i++) {
                Row row = sheet.createRow(i);
                int rowIndex = i - 2;
                Map rowData = (Map) realMakeMapList.get(rowIndex);
                //往 mapdata中添加项目数据
                for (Object entry : rowData.entrySet()) {
                    Map.Entry entryMap = (Map.Entry) entry;
                    String strKey = (String) entryMap.getKey();
                    String strIndex = "";
                    boolean isFun = false;
                    String strFunValue = "";
                    String strCellValue = (String) entryMap.getValue();
                    //获取excel中对应下标
                    for (int i1 = 0; i1 < costChangeHistoryMapping0.size(); i1++) {
                        Map mappingMap = (Map) costChangeHistoryMapping0.get(i1);
                        if (mappingMap.containsValue(strKey)) {
                            strIndex = (String) mappingMap.get("value");
                            //格式化处理
                            String strFormat = (String) mappingMap.get("format");
                            if (UIUtil.isNotNullAndNotEmpty(strFormat)) {
                                String strNlsKey = (String) mappingMap.get("nlsKey");
                                if ("date".equals(strFormat)) {
                                    if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                                        //转换时间
                                        Date javaDate = eMatrixDateFormat.getJavaDate(strCellValue, context.getLocale());
                                        LocalDateTime localDateTime = javaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                                        strCellValue = formatter.format(localDateTime);
                                    }
                                } else if ("range".equals(strFormat)) {
                                    if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                                        strCellValue = EnoviaResourceBundle.getRangeI18NString(context, strNlsKey, strCellValue, context.getLocale().getLanguage());
                                    }
                                } else if ("policy".equals(strFormat)) {
                                    if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                                        strCellValue = EnoviaResourceBundle.getStateI18NString(context, strNlsKey, strCellValue, context.getLocale().toString());
                                    }
                                } else if ("user".equals(strFormat)) {
                                    strCellValue = PersonUtil.getFullName(context, strCellValue);
                                    _logger.info("strCellValue:{}", strCellValue);
                                } else if ("function".equals(strFormat)) {
                                    isFun = true;
                                    strFunValue = (String) mappingMap.get("funValue");
                                    _logger.info("strFunValue:{}", strFunValue);
                                }
                            }

                            break;
                        }
                    }
                    if (UIUtil.isNotNullAndNotEmpty(strIndex)) {
                        Cell cell = row.createCell(Integer.valueOf(strIndex));
                        if (isFun) {
                            // 将列下标转换为列字母
                            //String colLetter1 = new CellReference(0, Integer.parseInt(strIndex)).
                            //funValue="VALUE(|{11}|)+VALUE(|{17}|)"  以 | 切割 然后找到 以{ 开始 }结束 的字符串 获取中间下标值 转换为列字母 拼接行信息 组装为完整函数
                            StringTokenizer strTokenizer = new StringTokenizer(strFunValue, "|");
                            //公式拼接
                            StringBuffer sb = new StringBuffer();
                            while (strTokenizer.hasMoreTokens()) {
                                String strTmpFunValue = strTokenizer.nextToken();
                                if (strTmpFunValue.startsWith("{") && strTmpFunValue.endsWith("}")) {
                                    //获取中间列下标
                                    strTmpFunValue = strTmpFunValue.substring(1, strTmpFunValue.length() - 1);
                                    //转换为字母加行数 A1 C2 这种
                                    strTmpFunValue = new CellReference(i, Integer.parseInt(strTmpFunValue)).formatAsString();
                                }
                                sb.append(strTmpFunValue);
                            }
                            cell.setCellFormula(sb.toString());
                        } else {
                            cell.setCellValue(strCellValue);
                        }
                    }
                }
            }
//            setCellBoard(sheet,cellBorderStyle,2);
            return workbook;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param context
     * @param args
     * @return java.util.Map
     * @throws
     * @description 项目成本下载
     * @author CHENYAN
     * @date 2024/11/28 14:01
     */
    public static Map projectCostingDownLoad(Context context, String[] args) throws Exception {
        Map res = new HashMap<>();
        String strProjectId = args[0];
        String strPath = args[1];
        _logger.info("strPath:{}", strPath);
        String strFullTemplatePath = getTemplatePath("CostChangeHistoryTemplate", strPath);
        _logger.info("strFullTemplatePath:{}", strFullTemplatePath);
        InputStream inputStream = new FileInputStream(strFullTemplatePath);
        Workbook workbook = WorkbookFactory.create(inputStream);
        DomainObject project = DomainObject.newInstance(context, strProjectId);
        String strProjectName = project.getInfo(context, "name");
        String strFileName = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECRFileName.CostHistory");
        //设置单元格边框
        CellStyle cellBorderStyle = workbook.createCellStyle();
        cellBorderStyle.setBorderTop(BorderStyle.THIN);
        cellBorderStyle.setBorderBottom(BorderStyle.THIN);
        cellBorderStyle.setBorderLeft(BorderStyle.THIN);
        cellBorderStyle.setBorderRight(BorderStyle.THIN);
        try {
//            ContextUtil.pushContext(context);
            workbook = getProjectAllECRByPId(context, workbook, project, cellBorderStyle);
            workbook = getProjectAllECRCostDataByPId(context, workbook, project, cellBorderStyle);
            workbook = getProjectOneMakePartDataByPId(context, workbook, project, cellBorderStyle);
            workbook = getProjectAllPartCostDataByPId(context, workbook, project, cellBorderStyle);
            workbook = getProjectOnePartCostDataByPId(context, workbook, project, cellBorderStyle);
        } finally {
//            ContextUtil.popContext(context);
        }
        //获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 将 LocalDateTime 对象转换成指定格式的字符串
        String strFormattedDate = now.format(formatter);
        res.put("file", workbook);
        res.put("fileName", JF_PublicMethodClass_mxJPO.buildStringInStrings("【", strProjectName, "】#", strFileName, "#【", strFormattedDate, "】.xlsx"));
        return res;
    }

    public void projectCostingDownLoadTime(Context context, String[] args) throws Exception {
        Map map = projectCostingDownLoad(context, args);
        Workbook workbook = (Workbook) map.get("file");
        String fileName = args[3] + System.currentTimeMillis() + ".xlsx";// (String)map.get("fileName");
        String path = args[2] + fileName;
        _logger.info("path:{}", path);
        try {
            // 创建文件输出流，指定保存路径（如"D:/output.xlsx"）
            FileOutputStream fileOut = new FileOutputStream(path);
            // 将Workbook写入文件流
            workbook.write(fileOut);
            // 关闭资源
            fileOut.close();
            workbook.close();
            System.out.println("文件保存成功！");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据给定的 Locale 获取城市相对于 GMT 的时区偏移量（以小时为单位）。
     *
     * @param locale 指定的 Locale
     * @return 城市相对于 GMT 的时区偏移量（以小时为单位）
     */
    public static int getCityGmtOffsetInHours(Locale locale) {
        // 获取时区
        TimeZone timeZone = getTimeZoneForLocale(locale);
        if (timeZone == null) {
            // 如果无法找到特定时区，使用默认时区
            timeZone = TimeZone.getDefault();
        }

        // 计算相对于 GMT 的时区偏移量（以小时为单位）
        int rawOffset = timeZone.getRawOffset();
        int offsetInHours = rawOffset / (60 * 60 * 1000);

        // 处理夏令时
        if (timeZone.inDaylightTime(new Date())) {
            int dstOffset = timeZone.getDSTSavings() / (60 * 60 * 1000);
            offsetInHours += dstOffset;
        }

        return offsetInHours;
    }

    /**
     * 根据 Locale 获取时区。
     *
     * @param locale 指定的 Locale
     * @return 对应的 TimeZone 对象
     */
    private static TimeZone getTimeZoneForLocale(Locale locale) {
        // 根据 Locale 获取时区 ID
        String timeZoneId = determineTimeZoneIdForLocale(locale);
        if (timeZoneId != null) {
            return TimeZone.getTimeZone(timeZoneId);
        }

        // 如果无法找到特定的时区，返回 null
        return null;
    }

    /**
     * 根据 Locale 确定时区 ID。
     *
     * @param locale 指定的 Locale
     * @return 时区 ID
     */
    private static String determineTimeZoneIdForLocale(Locale locale) {
        // 根据 Locale 的国家代码确定时区 ID
        switch (locale.getCountry()) {
            case "US":
                // 美国东海岸
                return "America/New_York";
            case "CN":
                // 中国北京
                return "Asia/Shanghai";
            case "IN":
                // 印度新德里
                return "Asia/Kolkata";
            case "AU":
                // 澳大利亚悉尼
                return "Australia/Sydney";
            case "DE":
                // 德国柏林
                return "Europe/Berlin";
            // 更多国家和地区
            default:
                // 如果无法确定特定时区，使用默认时区
                return TimeZone.getDefault().getID();
        }
    }

    public static StringList getECRPriceSelect(StringList typeSelectList) {
        typeSelectList.add("attribute[JFChangesTrialExpensesManHours]");
        typeSelectList.add("attribute[JFStagnationOfSuppliersInternal]");
        typeSelectList.add("attribute[JFChangeUnitPrice]");
        typeSelectList.add("attribute[JFChangeMold]");
        typeSelectList.add("attribute[JFChangeUnitPriceCost]");
        typeSelectList.add("attribute[JFChangeMoldCost]");
        typeSelectList.add("attribute[JFChangesTrialExpensesManHoursExternal]");
        typeSelectList.add("attribute[JFStagnationOfSuppliersExternal]");
        typeSelectList.add("attribute[JFChangeUnitPriceExternal]");
        typeSelectList.add("attribute[JFChangeMoldCostExternal]");
        typeSelectList.add("attribute[JFChangeUnitPriceCostExternal]");
        typeSelectList.add("attribute[JFChangeMoldPriceCostExternal]");
        return typeSelectList;
    }

    public static void addSumKeyByMapData(Map data) {
        data.put("sum(JFChangesTrialExpensesManHours)", "");
        data.put("sum(JFStagnationOfSuppliersInternal)", "");
        data.put("sum(JFChangeUnitPrice)", "");
        data.put("sum(JFChangeMold)", "");
        data.put("sum(JFChangeUnitPriceCost)", "");
        data.put("sum(JFChangeMoldCost)", "");
    }

    /**
     * @param sheet
     * @param cellStyle
     * @return void
     * @throws
     * @description 统一设置单元格边框
     * @author CHENYAN
     * @date 2024/11/8 11:18
     */
    public static void setCellBoard(Sheet sheet, CellStyle cellStyle, int startRow) {
        Row row = sheet.createRow(0);
        int lastRowNum = sheet.getLastRowNum();
        _logger.info("lastRowNum:{}", lastRowNum);
        short firstCellNum = row.getFirstCellNum();
        short lastCellNum = row.getLastCellNum();
        for (; startRow < lastRowNum; startRow++) {
            Row boardRow = sheet.getRow(startRow);
            for (; firstCellNum < lastCellNum; firstCellNum++) {
                Cell cell = boardRow.getCell(firstCellNum);
                if (isCellEmpty(cell)) {
                    cell = boardRow.createCell(firstCellNum);
                }
                cell.setCellStyle(cellStyle);
            }
        }

    }

    private static boolean isCellEmpty(Cell cell) {
        if (cell.getCellType() == CellType.BLANK) {
            return true;
        }
        if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * @param context
     * @param args
     * @return boolean
     * @throws
     * @description 仅财务部门的人员才能有权限导出“成本变更履历”；
     * @author CHENYAN
     * @date 2024/12/4 11:14
     */
    public boolean getDownLoadProjectECRInfoAccess(Context context, String[] args) throws Exception {
        _logger.info("-----------------------------------------getDownLoadProjectECRInfoAccess begin ----------------------------------------------------");
        String strLoginUserId = PersonUtil.getPersonObjectID(context);
        String userName = context.getUser();
        DomainObject loginUser = DomainObject.newInstance(context, strLoginUserId);
        Properties prop = NioJDUtils.readPageObject(context, "JFJDConfig");
        String strFinanceCode = prop.getProperty("JF_Business.Department.Finance");
        strFinanceCode = strFinanceCode.trim();
        _logger.info("strFinanceCode:{}", strFinanceCode);
        String strWhereExp = JF_PublicMethodClass_mxJPO.buildStringInStrings("name=='", strFinanceCode, "'");
        MapList maps = loginUser.getRelatedObjects(context, "Member", // relationship pattern
                "Department",                                    // object pattern
                JF_Util_mxJPO.basicBolistSel(),                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                true,                                        // to direction
                false,                                        // from direction
                (short) 1,                                    // recursion level
                strWhereExp,                // object where clause
                "",
                (short) 0);
        _logger.info("maps:{}", maps);
        _logger.info("-----------------------------------------getDownLoadProjectECRInfoAccess end ----------------------------------------------------");
        Map requestMap = new HashMap();
        requestMap.put("roleName", "JfITAdmin");
        requestMap.put("userName", userName);
        boolean flag = JF_Util_mxJPO.isIncludeRole(context, JPO.packArgs(requestMap));
        return (maps != null && maps.size() > 0) || flag;
    }

    public static StringList getProjectRootPartByProjectId(Context context, DomainObject project) throws Exception {
        MapList maps = project.getRelatedObjects(context, "JFProject2RootPart", // relationship pattern
                TYPE_VPMREFERENCE,                                    // object pattern
                JF_Util_mxJPO.basicBolistSel(),                            // object selects
                JF_Util_mxJPO.basicRellistSel(), // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "attribute[PLMReference.V_isLastVersion]==TRUE",                // object where clause
                "attribute[JFZeroPart]==Y",
                (short) 0);
        return (StringList) maps.stream().map(m -> {
            Map rootPart = (Map) m;
            return rootPart.get(SELECT_ID);
        }).collect(Collectors.toCollection(StringList::new));
    }

    public void disConnectECRAssociationObjByECRId(Context context, String[] args) throws Exception {
        _logger.info("-------------------------------- disConnectECRAssociationObjByECRId begin-----------------------------------------------------");
        Map argsMap = JPO.unpackArgs(args);
        Boolean isStartTran = (Boolean) argsMap.get("isStartTran");
        StringList ECRIdList = (StringList) argsMap.get("ECRIdList");
        if (ECRIdList != null && ECRIdList.size() > 0) {
            try {
                ContextUtil.pushContext(context);
                if (isStartTran) {
                    ContextUtil.startTransaction(context, true);
                }
                DomainObject ecr = DomainObject.newInstance(context);
                Set<String> relSet = new HashSet<>();
                Set<String> idSet = new HashSet<>();
                for (int i = 0; i < ECRIdList.size(); i++) {
                    String strECRId = ECRIdList.get(i);
                    //防止空字符串
                    if (UIUtil.isNotNullAndNotEmpty(strECRId)) {
                        ecr.setId(strECRId);
                        //获取所有的会签任务并断开连接
                        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
                        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
                        MapList taskList = ecr.getRelatedObjects(context, JF_PLMConstants_mxJPO.RELATIONSHIP_JF_ECR_TASK, // relationship pattern
                                JF_PLMConstants_mxJPO.TYPE_JS_SIGN_TASK,                                    // object pattern
                                typeSelectList,                            // object selects
                                relSelectList, // relationship selects
                                false,                                        // to direction
                                true,                                        // from direction
                                (short) 1,                                    // recursion level
                                "",                // object where clause
                                "",
                                (short) 0);
                        for (int i1 = 0; i1 < taskList.size(); i1++) {
                            Map taskMap = (Map) taskList.get(i1);
                            String strRelId = (String) taskMap.get(SELECT_RELATIONSHIP_ID);
                            String strTaskId = (String) taskMap.get(SELECT_ID);
                            relSet.add(strRelId);
                            idSet.add(strTaskId);
                        }
                        _logger.info("sign task :{}", taskList);
                        Map parameters = new HashMap();
                        parameters.put("objectId", strECRId);
                        parameters.put("expandLevel", "0");
                        String[] paramArgs = JPO.packArgs(parameters);
                        MapList costingTableData = getECRCostingTableData(context, paramArgs);
                        //采购件清单
                        for (int i1 = 0; i1 < costingTableData.size(); i1++) {
                            Map costingMap = (Map) costingTableData.get(i1);
                            String strRelId = (String) costingMap.get(SELECT_RELATIONSHIP_ID);
                            relSet.add(strRelId);
                        }
                        _logger.info("costingTableData :{}", costingTableData);

                        //制造件清单
                        MapList makeTableData = getECRMakeTableData(context, paramArgs);
                        for (int i1 = 0; i1 < makeTableData.size(); i1++) {
                            Map makeMap = (Map) makeTableData.get(i1);
                            String strRelId = (String) makeMap.get(SELECT_RELATIONSHIP_ID);
                            relSet.add(strRelId);
                        }
                        _logger.info("makeTableData :{}", makeTableData);

                        //获取制造件价格关系
                        MapList priceMapList = ecr.getRelatedObjects(context,
                                JF_PLMConstants_mxJPO.REL_JFECR2MakePartPrice + "," + JF_PLMConstants_mxJPO.REL_JFECR2PartPrice, // relationship pattern
                                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                                typeSelectList,                            // object selects
                                relSelectList, // relationship selects
                                false,                                        // to direction
                                true,                                        // from direction
                                (short) 1,                                    // recursion level
                                "",                // object where clause
                                "",
                                (short) 0);
                        for (int i1 = 0; i1 < priceMapList.size(); i1++) {
                            Map priceMap = (Map) priceMapList.get(i1);
                            String strRelId = (String) priceMap.get(SELECT_RELATIONSHIP_ID);
                            relSet.add(strRelId);
                        }
                        _logger.info("priceMapList :{}", priceMapList);

                        //获取ECR关联ECO
                        Map ECOMap = ecr.getInfo(context, StringList.create("from[JFECR2CO].id", "from[JFECR2CO].to.id"));
                        _logger.info("ECOMap :{}", ECOMap);

                        String strECORelId = (String) ECOMap.get("from[JFECR2CO].id");
                        if (UIUtil.isNotNullAndNotEmpty(strECORelId)) {
                            relSet.add(strECORelId);
                        }
                        String strECOId = (String) ECOMap.get("from[JFECR2CO].to.id");
                        if (UIUtil.isNotNullAndNotEmpty(strECOId)) {
                            idSet.add(strECOId);
                        }
                    }
                }
                //关闭trigger
                MqlUtil.mqlCommand(context, "trigger off;");
                //断开关系
                _logger.info("relSet:{}", relSet);
                if (relSet.size() > 0) {
                    DomainRelationship.disconnect(context, relSet.toArray(String[]::new));
                }
                _logger.info("idSet:{}", idSet);
                //删除对象
                if (idSet.size() > 0) {
                    DomainObject.deleteObjects(context, idSet.toArray(String[]::new));
                }
                //开启trigger
                MqlUtil.mqlCommand(context, "trigger on;");
                ContextUtil.commitTransaction(context);
            } catch (FrameworkException e) {
                if (isStartTran) {
                    ContextUtil.abortTransaction(context);
                }
                e.printStackTrace();
                throw e;
            } finally {
                ContextUtil.popContext(context);
            }
        }
        _logger.info("-------------------------------- disConnectECRAssociationObjByECRId end-----------------------------------------------------");

    }

    /**
     * @param context
     * @param args
     * @return void
     * @throws
     * @description 检查ECR相关联对象是否已经全部清除
     * @author CHENYAN
     * @date 2025/2/7 10:31
     */

    public Map checkECRAssociationObjByECRId(Context context, String[] args) throws Exception {
        Map res = new HashMap<>();
        Map argsMap = JPO.unpackArgs(args);
        StringList ECRIdList = (StringList) argsMap.get("ECRIdList");
        boolean isPass = false;
        StringBuilder sb = new StringBuilder();
        if (ECRIdList != null && ECRIdList.size() > 0) {
            try {
                ContextUtil.pushContext(context);
                DomainObject ecr = DomainObject.newInstance(context);
                for (int i = 0; i < ECRIdList.size(); i++) {
                    String strECRId = ECRIdList.get(i);
                    //防止空字符串
                    if (UIUtil.isNotNullAndNotEmpty(strECRId)) {
                        ecr.setId(strECRId);
                        //获取所有的会签任务并断开连接
                        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
                        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
                        Map parameters = new HashMap();
                        parameters.put("objectId", strECRId);
                        parameters.put("expandLevel", "0");
                        String[] paramArgs = JPO.packArgs(parameters);
                        //采购件清单
                        MapList costingTableData = getECRCostingTableData(context, paramArgs);
                        //制造件清单
                        MapList makeTableData = getECRMakeTableData(context, paramArgs);
                        if (costingTableData != null && costingTableData.size() > 0) {
                            sb.append("制造件清单");
                            sb.append("、");
                        }
                        if (makeTableData != null && makeTableData.size() > 0) {
                            sb.append("采购件清单");
                            sb.append("、");
                        }
                        //获取制造件价格关系
                        MapList priceMapList = ecr.getRelatedObjects(context,
                                JF_PLMConstants_mxJPO.REL_JFECR2MakePartPrice + "," + JF_PLMConstants_mxJPO.REL_JFECR2PartPrice, // relationship pattern
                                JF_PLMConstants_mxJPO.TYPE_VPMReference,                                    // object pattern
                                typeSelectList,                            // object selects
                                relSelectList, // relationship selects
                                false,                                        // to direction
                                true,                                        // from direction
                                (short) 1,                                    // recursion level
                                "",                // object where clause
                                "",
                                (short) 0);
                        if (priceMapList != null && priceMapList.size() > 0) {
                            sb.append("价格关系清单");
                            sb.append("、");
                        }
                        //获取ECR关联ECO
                        Map ECOMap = ecr.getInfo(context, StringList.create("from[JFECR2CO]", "from[JFECR2Task|to.type==JF_SignTask]"));
                        if ("TRUE".equalsIgnoreCase((String) ECOMap.get("from[JFECR2CO]"))) {
                            sb.append("ECO清单");
                            sb.append("、");
                        }
                        if ("TRUE".equalsIgnoreCase((String) ECOMap.get("from[JFECR2Task]"))) {
                            sb.append("会签任务清单");
                            sb.append("、");
                        }
                    }
                }
                if (sb.length() > 0) {
                    isPass = true;
                    String strMessContent = sb.substring(0, sb.length() - 1);
                    res.put("mess", strMessContent);
                }

            } catch (FrameworkException e) {
                throw e;
            } finally {
                ContextUtil.popContext(context);
            }
        }
        res.put("code", isPass);
        return res;
    }

    public boolean getECRAdminUtilCmdAccess(Context context, String[] args) {
        String strRole = context.getRole();
        boolean hasAccess = false;
        if ((!"ctx::VPLMAdmin.Company Name.Default".equals(strRole)) && strRole.endsWith("JFSeat")) {
            hasAccess = true;
        }
        return hasAccess;
    }

    public StringList getProjectTitle(Context context, String[] args) throws Exception {
        _logger.info("------------------------------------------  getProjectTitle  begin ------------------------------------------");
        StringList tableIdList = _getListOfKeys(args, SELECT_ID);
        StringList res = new StringList();
        try {
            ContextUtil.pushContext(context);
            for (int i = 0; i < tableIdList.size(); i++) {
                String strID = tableIdList.get(i);
                DomainObject obj = DomainObject.newInstance(context, strID);
                String strAttrValue = obj.getInfo(context, "from[JFChange2Project].to.description");
                res.add(strAttrValue);
            }
        } finally {
            ContextUtil.popContext(context);
        }
        return res;
    }

    /**
     * eco标签页面的项目描述
     *
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList getEcoProjectDescription(Context context, String[] args) throws Exception {
        _logger.info("------------------------------------------  getProjectTitle  begin ------------------------------------------");
        StringList tableIdList = _getListOfKeys(args, SELECT_ID);
        StringList res = new StringList();
        try {
            ContextUtil.pushContext(context);
            for (int i = 0; i < tableIdList.size(); i++) {
                String strID = tableIdList.get(i);
                DomainObject obj = DomainObject.newInstance(context, strID);
                String strAttrValue = obj.getInfo(context, "to[JFECR2CO].from.from[JFChange2Project].to[Project Space].description");
                res.add(strAttrValue);
            }
        } finally {
            ContextUtil.popContext(context);
        }
        return res;
    }


    /**
     * 后台导出 ECR / ECO  表单并发送邮件给指定的人员
     *
     * @param context
     * @param args
     * @return void
     * @throws
     * @author LIUJR
     * @date 17/06/2025 15:10
     * @description
     */
    public void ExportAndSendEmailsBackground(Context context, String[] args) {
        try {
            JF_ECRService_mxJPO jfEcrServiceMxJPO = new JF_ECRService_mxJPO();
            Map res = jfEcrServiceMxJPO.exportAllECRAndSignTaskList(context, args);
            String flag = (String) res.get("flag");
            if ("N".equalsIgnoreCase(flag)) {
                return;
            }
            String strFileName = (String) res.get("fileName");
            _logger.info("strFileName： {}", strFileName);
            //获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 将 LocalDateTime 对象转换成指定格式的字符串
            String strFormattedDate = now.format(formatter);
            strFileName = "ECR变更请求导出_" + strFormattedDate + ".xlsx";
            String path = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"export.ECR.Download"});
            path += File.separator + strFileName;
            _logger.info("path： {}", path);
            FileOutputStream fileOut = new FileOutputStream(path);
            Workbook workbook = (Workbook) res.get("file");
            // 将Workbook写入文件流
            workbook.write(fileOut);
            workbook.close();
            fileOut.close();
            MimeMultipart multipart = JF_SendEmailUtils_mxJPO.sendEmailWithAttachments(context, path);
            String strSubject = "ECR/ECO任务状态汇总信息";
            String toAddressee = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"export.ECR.SendEmail"});
            _logger.info("toAddressee:{}", toAddressee);
            //开始构造邮件
            Boolean aBoolean = JF_SendEmailUtils_mxJPO.SendEmail(context, toAddressee, strSubject, multipart);
//            _logger.info("邮件发送状态： {}", aBoolean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出ECR
     *
     * @param context
     * @param args
     * @return java.util.Map
     * @throws
     * @author LIUJR
     * @date 2025/2/20 15:14
     * @description
     */
    public Map exportAllECRAndSignTaskList(Context context, String[] args) {
        Map resMap = new HashMap();
        Boolean isPush = Boolean.FALSE;
        Locale locale = context.getLocale();
        try {
            ContextUtil.pushContext(context);
            isPush = Boolean.TRUE;
            //文件地址
            String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            _logger.info("classPath:{}", classPath);
            if (classPath.length() < 20) {
                classPath = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{"Space.WEBINFO.Path"});
            }
            String fileTemPath = classPath.substring(0, classPath.indexOf("WEB-INF"));
            String FilePath = getTemplatePath("JFECRSignTaskTemplate", fileTemPath);
            //打开文件
            InputStream inputStream = new FileInputStream(FilePath);
            Workbook workbook = WorkbookFactory.create(inputStream);
            //设置单元格边框
            CellStyle cellBorderStyle = workbook.createCellStyle();
            cellBorderStyle.setBorderTop(BorderStyle.THIN);
            cellBorderStyle.setBorderBottom(BorderStyle.THIN);
            cellBorderStyle.setBorderLeft(BorderStyle.THIN);
            cellBorderStyle.setBorderRight(BorderStyle.THIN);
            cellBorderStyle.setAlignment(HorizontalAlignment.CENTER); // 水平居中
            cellBorderStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
            //公共需要的数据
            MapList res = DomainObject.findObjects(context, TYPE_JFECR + "," + TYPE_JFNewECR, "*", "", new StringList(SELECT_ID));
            StringList allECRList = (StringList) res.stream().map(m -> {
                Map ercMap = (Map) m;
                return UIUtil.getValue(ercMap, SELECT_ID);
            }).collect(Collectors.toCollection(StringList::new));
            //保留ECR的会签任务 及其ECR信息
            Map infoMap = new HashMap();
            //ECR任务状态写入文档 sheet 1
            _logger.info("ECR任务状态写入文档 sheet 1 start");
            writeECRTaskStatusToFile(context, workbook, cellBorderStyle, allECRList, infoMap);
            _logger.info("ECR任务状态写入文档 sheet 1 end");
            //获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 将 LocalDateTime 对象转换成指定格式的字符串
            String strFormattedDate = now.format(formatter);
            //PLM再跑ECR数据写入文件 sheet 0 infoMap保留ECR 及其会签任务信息: key : ecrId  value: signTaskList
            _logger.info("PLM再跑ECR数据写入文件 sheet 0 start");
            writePLMProjectECRCountToFile(context, workbook, cellBorderStyle, strFormattedDate, infoMap, locale);
            _logger.info("PLM再跑ECR数据写入文件 sheet 0 end");
            //再跑ECO数据写入文件 sheet2
            _logger.info("PLM再跑ECO数据写入文件 sheet 2 start");
            writePLMProjectECOCountToFile(context, workbook, cellBorderStyle, strFormattedDate);
            _logger.info("PLM再跑ECO数据写入文件 sheet 2 end");
            //再跑ECO数据写入文件 sheet3
            _logger.info("PLM再跑ECO执行计划写入文件 sheet 3 start");
            writePLMProjectECPToFile(context, workbook, cellBorderStyle, strFormattedDate);
            _logger.info("PLM再跑ECO执行计划写入文件 sheet 3 end");
            String local = context.getLocale().toString();
            _logger.info("local:{}", local);
            local = "en".equalsIgnoreCase(local) ? "en" : "zh";
            _logger.info("local:{}", local);
            String fileName = ComponentsUIUtil.getI18NString(context, local, "emxComponents.ECRChange.ExportName", new String[]{});
            resMap.put("file", workbook);
            resMap.put("flag", "Y");
            //分中英文
            _logger.info("fileName:{}", fileName);
            resMap.put("fileName", JF_PublicMethodClass_mxJPO.buildStringInStrings(fileName, strFormattedDate, ".xlsx"));
            _logger.info("strFileName:{}", JF_PublicMethodClass_mxJPO.buildStringInStrings(fileName, strFormattedDate, ".xlsx"));
        } catch (Exception e) {
            e.printStackTrace();
            resMap.put("flag", "N");
            return resMap;
        } finally {
            if (isPush) {
                try {
                    ContextUtil.popContext(context);
                } catch (FrameworkException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        _logger.info("完成！！！！！！！！！！");
        return resMap;
    }

    /**
    * 国际化
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2025/12/13 23:11
    * @description
    */
    public static Map exportECRNls() {
       Map<String, String> translationMap = new HashMap<>();
       // ecp状态初始化翻译映射
       translationMap.put("JFECR_Create", "创建");
       translationMap.put("JFECR_Submit", "提交");
       translationMap.put("JFECR_Review", "审核");
       translationMap.put("JFECR_Countersign", "会签");
       translationMap.put("JFECR_APR", "APR");
       translationMap.put("JFECR_Quotation", "报价");
       translationMap.put("JFECR_Complete", "已完成");
       translationMap.put("JFNewECR_Create", "创建");
       translationMap.put("JFNewECR_Review", "审核");
       translationMap.put("JFNewECR_Countersign", "会签");
       translationMap.put("JFNewECR_APR", "APR");
       translationMap.put("JFNewECR_Quotation", "报价");
       translationMap.put("JFNewECR_Complete", "已完成");
       translationMap.put("JFECRType_Ordinary", "普通");
       translationMap.put("JFECRType_Urgent", "紧急");
       translationMap.put("JFChangeSource_Both", "两者");
       translationMap.put("JFChangeSource_External_Changes", "外部变更");
       translationMap.put("JFChangeSource_Internal_Changes", "内部变更");
       translationMap.put("JFChangeSource_", "");
       translationMap.put("Project_Task_Create","草稿");
       translationMap.put("Project_Task_Active","工作中");
       translationMap.put("Project_Task_Complete", "已完成");
       translationMap.put("Project_Task_Assign", "To Do");
       translationMap.put("Project_Task_Review", "审批中");
       return translationMap;
    }

    /**
     * 拿取ecr任务状态  并写放入导出文件中
     *
     * @param context
     * @param workbook
     * @param cellBorderStyl
     * @return org.apache.poi.ss.usermodel.Workbook
     * @throws
     * @author LIUJR
     * @date 2025/2/20 15:50
     * @description
     */
    public static Workbook writeECRTaskStatusToFile(Context context, Workbook workbook, CellStyle cellBorderStyl, StringList allECRList, Map infoMap) throws Exception {
        //拿取需要查询的信息
        Map map = getECRTaskStatusColumnsInfo();
        Map exportECRNlsMap = exportECRNls();
        Map<Integer, String> attributeMap = (Map<Integer, String>) map.get("attributeMap");
        Map<String, Integer> settingMap = (Map<String, Integer>) map.get("settingMap");
        IntegerList mergeList = (IntegerList) map.get("mergeList");
        StringList internationalizationList = (StringList) map.get("internationalizationList");
        IntegerList tranDateList = (IntegerList) map.get("tranDateList");
        //获取需要国际化的属性，进行存储
        Integer startCell = settingMap.get("startCell");
        Integer relStart = settingMap.get("relStart");
        Integer projectStart = settingMap.get("projectStart");
        Integer reStartCell = settingMap.get("reStartCell");
        Integer endCell = settingMap.get("endCell");
        Integer startRow = settingMap.get("startRow");
        Integer relSighTask = settingMap.get("relSighTask");
        //系统所有ECR  关联的会签任务   项目信息
        StringList busSelectList = new StringList();
        StringList relSelectList = new StringList();
        StringList relSighTaskList = new StringList();
        StringList projectSelectList = new StringList();
        for (int i = startCell; i <= endCell; i++) {
            if (i >= relStart && i < projectStart - 1) {
                if (i == relSighTask) {
                    relSighTaskList.add(attributeMap.get(i));
                } else {
                    relSelectList.add(attributeMap.get(i));
                }
                continue;
            }
            if (i >= projectStart && i < reStartCell) {
                projectSelectList.add(attributeMap.get(i));
                continue;
            }
            busSelectList.add(attributeMap.get(i));
        }
        busSelectList.add(SELECT_ID);
        busSelectList.add(SELECT_POLICY);
        busSelectList.add(SELECT_CURRENT);
        relSelectList.add(SELECT_CURRENT);
        relSelectList.add(SELECT_POLICY);
        relSelectList.add(SELECT_ID);
        relSelectList.add(SELECT_TYPE);
        MapList res = DomainObject.getInfo(context, allECRList.toStringArray(), busSelectList);
        //排序 按照名称
        res.addSortKey(SELECT_NAME, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
        res.sort();
        int rowNum = startRow;
        Sheet sheet = workbook.getSheetAt(1);
        CellRangeAddress cellRangeAddress;
        HashMap<String, String> internationalizationMap = new HashMap<>();
        for (int i = 0; i < res.size(); i++) {
            //遍历erc 组装excel
            Map ercMap = (Map) res.get(i);
            String objectId = UIUtil.getValue(ercMap, SELECT_ID);
            String current = UIUtil.getValue(ercMap, SELECT_CURRENT);
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            //获取erc的会签任务
            MapList signTaskMapList = domainObject.getRelatedObjects(context, JF_PLMConstants_mxJPO.RELATIONSHIP_JF_ECR_TASK, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_JS_SIGN_TASK + "," + JF_PLMConstants_mxJPO.TYPE_JF_CustomerTask,                                    // object pattern
                    relSelectList,                            // object selects
                    relSighTaskList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            //mod by ljr 20250626
            MapList signTaskMapList1 = domainObject.getRelatedObjects(context, JF_PLMConstants_mxJPO.RELATIONSHIP_JF_ECR_TASK, // relationship pattern
                    JF_PLMConstants_mxJPO.TYPE_JF_APRTask,                                    // object pattern
                    relSelectList,                            // object selects
                    relSighTaskList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            //判断 如果有APR任务的时候
            if ("APR".equalsIgnoreCase(current)) {
                //当ecr是APR状态的时候,   APR任务是Active，然后会签任务种有工作中的，将APR会签任务状态改成已驳回
                if (!signTaskMapList1.isEmpty()) {
                    Map map1 = (Map) signTaskMapList1.get(0);
                    String current1 = UIUtil.getValue(map1, SELECT_CURRENT);
                    if ("Active".equalsIgnoreCase(current1)) {
                        MapList mapList = (MapList) signTaskMapList.stream().filter(m -> {
                            Map map2 = (Map) m;
                            String current2 = UIUtil.getValue(map2, SELECT_CURRENT);
                            if ("Active".equalsIgnoreCase(current2)) {
                                return Boolean.TRUE;
                            } else {
                                return Boolean.FALSE;
                            }
                        }).collect(Collectors.toCollection(MapList::new));
                        if (!mapList.isEmpty()) {
                            //是驳回
                            map1.put(SELECT_CURRENT, "已驳回");
                        }
                    }
                    signTaskMapList.add(map1);
                }
            }
            //end
            infoMap.put(objectId, signTaskMapList);
            //获取erc的所属项目
            MapList projceMapList = domainObject.getRelatedObjects(context, JF_PLMConstants_mxJPO.REL_JFChange2Project, // relationship pattern
                    TYPE_PROJECT_SPACE,                                    // object pattern
                    projectSelectList,                            // object selects
                    new StringList(), // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            /*
             * 开始写入文件，一个erc为一次，如果erc为创建状态，只有一行
             * erc为会签状态，行数就是ecr的行数等于会签任务的个数，并且需要合并单元格
             * */
            int signRows = signTaskMapList.size() == 0 ? 1 : signTaskMapList.size();
            int ercRow = rowNum;
            Map currentMap = new HashMap<String, String>();
            Map attrNlsMap = new HashMap<String, String>();
            Map personMap = new HashMap<String, String>();
            for (int iRow = 0; iRow < signRows; iRow++) {
                //具体的行数 需要在加完所有的行后  循环的最后记得+1
                //创建行
                Row row = sheet.createRow(ercRow);
                //序号写入
                Cell cellItem = row.createCell(0, CellType.STRING);
                cellItem.setCellValue(String.valueOf(i + 1));
                cellItem.setCellStyle(cellBorderStyl);
                for (int j = startCell; j < endCell; j++) {
                    // 行信息写入
                    Cell cell = row.createCell(j, CellType.STRING);
                    //属性名称
                    String strAttrName = attributeMap.get(j);
                    Map conMap = new HashMap();
                    if (j < relStart || j >= reStartCell) {
                        //ecr基础属性
                        conMap = ercMap;
                    } else if (j < projectStart) {
                        //会签任务属性
                        if (!signTaskMapList.isEmpty() && j != projectStart - 1) {
                            conMap = (Map) signTaskMapList.get(iRow);
                        }
                    } else if (j < reStartCell) {
                        //项目属性
                        if (!projceMapList.isEmpty()) {
                            conMap = (Map) projceMapList.get(0);
                        }
                    }
                    if (conMap.isEmpty()) {
                        cell.setCellValue(EMPTY_STRING);
                    } else {
                        String strAttrValue = UIUtil.getValue(conMap, strAttrName);
                        //国际化 人名化
                        if (strAttrName.equalsIgnoreCase(SELECT_OWNER) || strAttrName.equalsIgnoreCase(SELECT_CURRENT) || internationalizationList.contains(strAttrName)) {
                            if (internationalizationMap.containsKey(strAttrName + "_" + strAttrValue)) {
                                strAttrValue = internationalizationMap.get(strAttrName + "_" + strAttrValue);
                            } else {
                                //不包含 重新放置
                                if (strAttrName.equalsIgnoreCase(SELECT_OWNER)) {
                                    if (personMap.containsKey(strAttrValue)) {
                                        strAttrValue = UIUtil.getValue(personMap, strAttrValue);
                                    } else {
                                        String fullName = PersonUtil.getFullName(context, strAttrValue);
                                        if (UIUtil.isNotNullAndNotEmpty(fullName)) {
                                            personMap.put(strAttrValue, fullName);
                                            strAttrValue = fullName;
                                        } else {
                                            personMap.put(strAttrValue, strAttrValue);
                                        }
                                    }

                                } else if (strAttrName.equalsIgnoreCase(SELECT_CURRENT)) {
                                    if ("已驳回".equalsIgnoreCase(strAttrValue)) {
                                        strAttrValue = strAttrValue;
                                    } else if (currentMap.containsKey("current" + "_" + strAttrValue)) {
                                        strAttrValue = UIUtil.getValue(currentMap, SELECT_CURRENT + "_" + strAttrValue);
                                    } else {
                                        //current名称
                                        String policy = UIUtil.getValue(conMap, SELECT_POLICY);
                                        policy = policy.replaceAll(" ", "_");
                                        if (exportECRNlsMap.containsKey(policy + "_" + strAttrValue)) {
                                            strAttrValue = (String) exportECRNlsMap.get(policy + "_" + strAttrValue);
                                        } else {
                                            strAttrValue = policy;
                                        }
//                                        strAttrValue = EnoviaResourceBundle.getStateI18NString(context, policy, strAttrValue, context.getLocale().toString());
                                        currentMap.put(SELECT_CURRENT + "_" + strAttrValue, strAttrValue);
                                    }
                                } else if (internationalizationList.contains(strAttrName)) {
                                    //属性range值国际化
                                    if (attrNlsMap.containsKey(strAttrName + "_" + strAttrValue)) {
                                        strAttrValue = UIUtil.getValue(attrNlsMap, strAttrName + "_" + strAttrValue);
                                    } else {
                                        String name = strAttrName.replaceAll("attribute\\[([^\\]]+)\\]", "$1");
                                        String strAttrReplace = strAttrValue.replace(" ", "_");
                                        if (exportECRNlsMap.containsKey(name + "_" + strAttrReplace)) {
                                            strAttrValue = (String) exportECRNlsMap.get(name + "_" + strAttrReplace);
                                        } else {
                                            strAttrValue = strAttrReplace;
                                        }
//                                        strAttrValue = EnoviaResourceBundle.getRangeI18NString(context, name, strAttrValue, context.getLocale().getLanguage());
                                        attrNlsMap.put(strAttrName + "_" + strAttrReplace, strAttrValue);
                                    }
                                }
                                internationalizationMap.put(strAttrName + "_" + strAttrValue, strAttrValue);
                            }
                        }
                        if (tranDateList.contains(j) && UIUtil.isNotNullAndNotEmpty(strAttrValue)) {
                            // 解析输入日期
                            LocalDateTime dateTime = LocalDateTime.parse(strAttrValue, inputFormatter);
                            // 格式化输出日期
                            strAttrValue = dateTime.format(outputFormatter);
                        }
                        cell.setCellValue(strAttrValue);
                    }
                    cell.setCellStyle(cellBorderStyl);
                }
                ercRow++;
            }
            //合并单元格  行数为：rowNum - ercRow 列为iMerge
            if (rowNum < ercRow - 1) {
                for (int iMerge = 0; iMerge < endCell; iMerge++) {
                    if (mergeList.contains(iMerge)) {
                        //需要合并
                        cellRangeAddress = new CellRangeAddress(rowNum, ercRow - 1, iMerge, iMerge); // 参数依次是：开始行，结束行，开始列，结束列
                        sheet.addMergedRegion(cellRangeAddress);
                    }
                }
            }
            //新增一个为下一个ecr做准备
            rowNum += signRows;
        }
        return workbook;
    }

    /**
     * 获取erc表格输出列
     *
     * @param
     * @return java.util.Map
     * @throws
     * @author LIUJR
     * @date 2025/2/20 14:13
     * @description
     */
    public static Map getECRTaskStatusColumnsInfo() {
        //sheet中的列对应的属性
        HashMap hashMap = new HashMap();
        Map<Integer, String> attributeMap = new HashMap<>();
        attributeMap.put(1, SELECT_NAME);
        attributeMap.put(2, SELECT_ATTRIBUTE_TITLE);
        attributeMap.put(3, SELECT_OWNER);
        attributeMap.put(4, SELECT_ORIGINATED);
        attributeMap.put(5, SELECT_CURRENT);
        attributeMap.put(6, SELECT_ATTRIBUTE_TITLE);
        attributeMap.put(7, SELECT_CURRENT);
        attributeMap.put(8, SELECT_OWNER);
        attributeMap.put(9, SELECT_ORIGINATED);
        attributeMap.put(10, "state[Review].actual");
        attributeMap.put(11, "state[Complete].actual");
        attributeMap.put(12, "");
        attributeMap.put(13, SELECT_NAME);
        attributeMap.put(14, SELECT_DESCRIPTION);
        attributeMap.put(15, "attribute[JFProjectPhase]");
        attributeMap.put(16, "attribute[JFECRType]");
        attributeMap.put(17, "attribute[JFChangeSource]");
        attributeMap.put(18, "attribute[JFChangeReson]");
        //参数
        HashMap<String, Integer> settingMap = new HashMap<>();
        settingMap.put("startCell", 1);
        settingMap.put("relStart", 6);
        settingMap.put("projectStart", 13);
        settingMap.put("reStartCell", 15);
        settingMap.put("endCell", 19);
        settingMap.put("startRow", 1);
        settingMap.put("relSighTask", 9);
        //需要国际化的属性
        StringList internationalizationList = (StringList) Stream.of("attribute[JFECRType]", "attribute[JFChangeSource]").collect(Collectors.toCollection(StringList::new));
        //需要合并的列
        Integer[] integers = Stream.of(0, 1, 2, 3, 4, 5, 13, 14, 15, 16, 17, 18).toArray(Integer[]::new);
        IntegerList integerList = new IntegerList();
        integerList.addAll(Arrays.asList(integers));
        //需要转换时间列
        Integer[] tranDates = Stream.of(4, 9, 10, 11).toArray(Integer[]::new);
        IntegerList tranDateList = new IntegerList();
        tranDateList.addAll(Arrays.asList(tranDates));

        hashMap.put("attributeMap", attributeMap);
        hashMap.put("settingMap", settingMap);
        hashMap.put("mergeList", integerList);
        hashMap.put("internationalizationList", internationalizationList);
        hashMap.put("tranDateList", tranDateList);
        return hashMap;
    }

    /**
     * 拿取PLM中所有项目下得ECR数据并汇总
     *
     * @param context
     * @param workbook
     * @param cellBorderStyl
     * @return org.apache.poi.ss.usermodel.Workbook
     * @throws
     * @author LIUJR
     * @date 2025/3/7 10:22
     * @description
     */
    public static Workbook writePLMProjectECRCountToFile(Context context, Workbook workbook, CellStyle cellBorderStyl, String strFormattedDate, Map infoMap, Locale locale) throws Exception {
        //ECR信息：拿取Map infoMap保留ECR 及其会签任务信息: key : ecrId  value: signTaskList
        //获取动态列的值
        Map projectECRColumnsInfo = getProjectECRColumnsInfo();
        //动态写入行的动态列配置
        Map<Integer, String> attributeMap = (Map<Integer, String>) projectECRColumnsInfo.get("attributeMap");
        //起始行，起始列，结束列配置
        Map<String, Integer> settingMap = (Map<String, Integer>) projectECRColumnsInfo.get("settingMap");
        Map<Integer, String> RowCountMap = (Map<Integer, String>) projectECRColumnsInfo.get("RowCountMap");
        Integer startCell = settingMap.get("startCell");  //起始列
        Integer endCell = settingMap.get("endCell");    //结束列
        Integer startRow = settingMap.get("startRow");  //开始行
        Integer statisticRow = settingMap.get("statisticRow");  //统计行
        //合并列配置
        IntegerList mergeList = (IntegerList) projectECRColumnsInfo.get("mergeList");
        //跳过的列配置
        IntegerList skipList = (IntegerList) projectECRColumnsInfo.get("skipList");
        //最后一行的统计公式
        Map<Integer, String> lastRowMap = (Map<Integer, String>) projectECRColumnsInfo.get("lastRowMap");

        //获取表格
        Sheet sheet = workbook.getSheetAt(0);
        //获取系统中的项目
        MapList resMapList = DomainObject.findObjects(context, TYPE_PROJECT_SPACE, "*", "", StringList.create(SELECT_NAME, SELECT_TYPE, SELECT_ID, SELECT_DESCRIPTION));
        MapList res = (MapList) resMapList.stream().filter(m -> {
            Map map = (Map) m;
            String type = UIUtil.getValue(map, SELECT_TYPE);
            if (TYPE_PROJECT_SPACE.equalsIgnoreCase(type)) {
                return true;
            } else {
                return false;

            }
        }).collect(Collectors.toCollection(MapList::new));
        //在合并行的上方插入项目数量的行  先插入 直接获取
        sheet.shiftRows(statisticRow, sheet.getLastRowNum(), res.size() - 1, true, false);
        /**
         * 通过查询项目数据查询出 每个项目下的数据，并装入写入集合
         * 遍历项目：
         *      查询ECR个数
         *      查询审批未到期的ECR个数(忽略)
         *      创建状态的ECR个数
         *      提交状态的ECR个数
         *      审批状态的ECR个数
         *      会签状态的ECR,各会签节点审批中的ECR个数,  AME未会签|AQE未会签|costing未会签|launch未会签|采购未会签|物流未会签
         *      财务APR审批中的ECR个数
         *      商业报价状态的ECR个数
         *      已完成状态的ECR个数
         */
        //存放数据容器
        MapList mapList = new MapList();
        //开始遍历项目
        for (int i = 0; i < res.size(); i++) {
            Map map = (Map) res.get(i);
            Map<Integer, Object> hashMap = new HashMap<>();
            String psId = UIUtil.getValue(map, SELECT_ID);
            String psName = UIUtil.getValue(map, SELECT_NAME);
            String psTitle = UIUtil.getValue(map, SELECT_DESCRIPTION);
            DomainObject projectObject = DomainObject.newInstance(context, psId);
            //获取项目下的ECR
            MapList ecrMapList = projectObject.getRelatedObjects(
                    context,
                    JF_PLMConstants_mxJPO.REL_JFChange2Project, // relationship pattern
                    TYPE_JFECR + "," + TYPE_JFNewECR,       // object pattern
                    StringList.create(SELECT_NAME, SELECT_ID, SELECT_CURRENT, SELECT_ORIGINATED),// object selects
                    new StringList(), // relationship selects
                    true,             // to direction
                    false,            // from direction
                    (short) 1,        // recursion level
                    "",               // object where clause
                    "",
                    (short) 0
            );
            hashMap.put(0, i + 1);
            hashMap.put(1, psName);
            hashMap.put(2, psTitle);
            int iCreate = 0;
            int iSubmit = 0;
            int iReview = 0;
            int iComplete = 0;
            //延期的ecr数量
            int iextension = 0;
            //以下几个数据需要拿取会签任务的状态，判断为审批中
            int iAME = 0;
            int iTAME = 0;
            int iFAME = 0;
            int iAQE = 0;
            int icosting = 0;
            int ilaunch = 0;
            int icaigou = 0;
            int iwuliu = 0;
            int iAPR = 0;
            //报价统计
            int iQuotation = 0;
            if (ecrMapList.isEmpty()) {
                //无ECR
                hashMap.put(3, 0);
            } else {
                hashMap.put(3, ecrMapList.size());
                //ecr根据状态分组
                Map groupMap = (Map) ecrMapList.stream().collect(Collectors.groupingBy(m -> {
                            Map info = (Map) m;
                            return info.get(SELECT_CURRENT);
                        })
                );
                //遍历分组 然后排序
                for (Object k : groupMap.keySet()) {
                    String key = String.valueOf(k);
                    List infoList = (List) groupMap.get(key);
                    //每个分组下的数据
                    MapList ecrGroupMapList = new MapList();
                    ecrGroupMapList.addAll(infoList);
                    Map<String, Integer> countMap = ecrApprovalTaskExtensionQuantity(context, ecrGroupMapList, key, infoMap);
                    _logger.info("countMap:{}", countMap);
                    //ecr的超期期限
                    if ("Create".equalsIgnoreCase(key)) {
                        iCreate = countMap.get(key);
                        iextension += iCreate;
                    }
                    if ("Submit".equalsIgnoreCase(key)) {
                        iSubmit = countMap.get(key);
                        iextension += iSubmit;
                    }
                    if ("Review".equalsIgnoreCase(key)) {
                        iReview = countMap.get(key);
                        iextension += iReview;
                    }
                    if ("Countersign".equalsIgnoreCase(key)) {
                        iAME = countMap.get("AME");
                        iTAME = countMap.get("TAME");
                        iFAME = countMap.get("FAME");
                        iAQE = countMap.get("AQE");
                        icosting = countMap.get("Costing");
                        ilaunch = countMap.get("Launch");
                        icaigou = countMap.get("caigou");
                        iwuliu = countMap.get("wuliu");
                        int quantity = countMap.get(key);
                        iextension += quantity;
                    }
                    if ("APR".equalsIgnoreCase(key)) {
                        iAPR = countMap.get(key);
                        iextension += iAPR;
                    }
                    if ("Quotation".equalsIgnoreCase(key)) {
                        iQuotation = countMap.get(key);
                        iextension += iQuotation;
                    }
                    if ("Complete".equalsIgnoreCase(key)) {
                        iComplete = countMap.get(key);
                    }
                }
            }
            hashMap.put(4, RowCountMap.get(4));  //公式
            hashMap.put(5, iextension);
            hashMap.put(6, RowCountMap.get(6));   //公式
            hashMap.put(7, attributeMap.get(7));
            hashMap.put(8, iCreate);
            hashMap.put(9, iSubmit);
            hashMap.put(10, iReview);
            hashMap.put(11, iAME);
            hashMap.put(12, iFAME);//add
            hashMap.put(13, iTAME);//add
            hashMap.put(14, iAQE);
            hashMap.put(15, icosting);
            hashMap.put(16, ilaunch);
            hashMap.put(17, icaigou);
            hashMap.put(18, iwuliu);
            hashMap.put(19, iAPR);
            hashMap.put(20, iQuotation);
            hashMap.put(21, iComplete);
            hashMap.put(22, " ");
            mapList.add(hashMap);
        }
        mapList.addSortKey(String.valueOf(0), ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);
        mapList.sort();
        int projectRow = startRow;
        _logger.info("写入中.....");
        //写入内容
        for (int iRow = 0; iRow < mapList.size(); iRow++) {
            //写入文件
            Row row = sheet.createRow(projectRow);
            Map map = (Map) mapList.get(iRow);
            _logger.info("map:{}", map);
            for (int cell = startCell; cell < endCell; cell++) {
                if (cell == 4 || cell == 6) {
                    //需要计算
                    continue;
                }
                Cell cellItem = row.createCell(cell, CellType.NUMERIC);
                if (cell == 1 || cell == 2 || cell == 22 || cell == 7) {
                    cellItem = row.createCell(cell, CellType.STRING);
                    cellItem.setCellValue(String.valueOf(map.get(cell)));
                } else if (cell == 3 || cell == 5 || cell == 21) {
                    cellItem = row.createCell(cell, CellType.NUMERIC);
                    cellItem.setCellValue((Integer) map.get(cell));
                } else {
                    if ((Integer) map.get(cell) == 0) {
                        cellItem = row.createCell(cell, CellType.BLANK);
                    } else {
                        cellItem = row.createCell(cell, CellType.NUMERIC);
                        cellItem.setCellValue((Integer) map.get(cell));
                    }
                }
                cellItem.setCellStyle(cellBorderStyl);
            }
            projectRow++;
            //开始计算每一行的值
            Cell cellItem = row.createCell(4, CellType.NUMERIC);
            String value = ((String) map.get(4)).replace("$", String.valueOf(projectRow));
            cellItem.setCellFormula(value); //公式
            cellItem.setCellStyle(cellBorderStyl);
            //开始计算每一行的值
            cellItem = row.createCell(6, CellType.NUMERIC);
            value = ((String) map.get(6)).replace("$", String.valueOf(projectRow));
            cellItem.setCellFormula(value); //公式
            cellItem.setCellStyle(getPercentageStyle(workbook));
        }
        CellRangeAddress cellRangeAddress;
        //合并
        for (int i = 0; i < mergeList.size(); i++) {
            int iMerge = mergeList.get(i);
            //需要合并
            cellRangeAddress = new CellRangeAddress(startRow, projectRow - 1, iMerge, iMerge); // 参数依次是：开始行，结束行，开始列，结束列
            sheet.addMergedRegion(cellRangeAddress);
        }
        //统计行设置
        Row row = sheet.getRow(projectRow);
        for (int cell = startCell; cell < endCell; cell++) {
            if (skipList.contains(cell)) {
                continue;
            }
            //统计行
            if (lastRowMap.containsKey(cell)) {
                String value = lastRowMap.get(cell).replace("$", String.valueOf(projectRow));
                Cell cellItem = row.getCell(cell);
                cellItem.setCellFormula(value); //公式
            }
        }
        //标题
        Cell cellTitle = sheet.getRow(0).getCell(0);
        String cellValue = cellTitle.getStringCellValue();
        cellTitle.setCellValue(cellValue.replace("$1", strFormattedDate));
        sheet.setForceFormulaRecalculation(true);
        return workbook;
    }

    /**
     * 根据erc 查看是否延期三天
     *
     * @param context
     * @param ecrGroupMapList
     * @param key
     * @return int
     * @throws
     * @author LIUJR
     * @date 2025/4/11 14:17
     * @description
     */
    public static Map<String, Integer> ecrApprovalTaskExtensionQuantity(Context context, MapList ecrGroupMapList, String key, Map infoMap) {
        int quantity = 0;
        Map<String, Integer> returnMap = new HashMap();
        try {
            //ecr的超期期限
            Iterator iterator = ecrGroupMapList.iterator();
            _logger.info("key:{}", key);
            _logger.info("ecrGroupMapList:{}", ecrGroupMapList);
            DomainObject domainObject = DomainObject.newInstance(context);
            switch (key) {
                case "Create": {
                    while (iterator.hasNext()) {
                        Map map = (Map) iterator.next();
                        String createDate = UIUtil.getValue(map, SELECT_ORIGINATED);
                        _logger.info("create, createDate:{}", createDate);
                        if (getDateTimeOverdue(createDate, Locale.US)) {
                            quantity++;
                        }
                        _logger.info("create, quantity:{}", quantity);
                    }
                    returnMap.put(key, quantity);
                    break;
                }
                case "Submit":
                case "Review": {
                    int iDM = 0;
                    int iPM = 0;
                    while (iterator.hasNext()) {
                        Map map = (Map) iterator.next();
                        String id = UIUtil.getValue(map, SELECT_ID);
                        domainObject.setId(id);
                        _logger.info("Submit,Review, id:{}", id);
                        String strResult = getECRInboxTask(context, domainObject);
                        if (UIUtil.isNotNullAndNotEmpty(strResult)) {
                            if ("PM".equalsIgnoreCase(strResult)) {
                                iPM++;
                            } else if ("DM".equalsIgnoreCase(strResult)) {
                                iDM++;
                            }
                        }
                        _logger.info("Submit,Review, quantity:{}", quantity);
                    }
                    if (returnMap.containsKey("Submit")) {
                        if (iDM != 0) {
                            Integer is = returnMap.get("Submit");
                            returnMap.put("Submit", is + iDM);
                        }
                    } else {
                        returnMap.put("Submit", iDM);
                    }
                    if ("Review".equalsIgnoreCase(key)) {
                        returnMap.put("Review", iPM);
                    }
                    break;
                }
                case "Countersign":
                case "Quotation": {
                    //会签状态
                    Integer iAME = 0;
                    Integer iFAME = 0;
                    Integer iTAME = 0;
                    Integer iAQE = 0;
                    Integer icosting = 0;
                    Integer ilaunch = 0;
                    Integer icaigou = 0;
                    Integer iwuliu = 0;
                    Integer iQuotation = 0;
                    while (iterator.hasNext()) {
                        Map ercMap = (Map) iterator.next();
                        String ecrId = UIUtil.getValue(ercMap, SELECT_ID);
                        //拿取ECR下的会签任务集合
                        MapList signTaskList = (MapList) infoMap.get(ecrId);
                        _logger.info("Countersign,APR, Quotation:ecrId{}", ecrId);
                        _logger.info("Countersign,APR, Quotation:signTaskList{}", signTaskList);
                        //拿取审批中的会签任务 的标题
                        StringList signTitleList = (StringList) signTaskList.stream().filter(m -> {
                            Map signTaskMap = (Map) m;
                            String strCurrent = UIUtil.getValue(signTaskMap, SELECT_CURRENT);
                            Boolean flag = Boolean.FALSE;
                            if ("Active".equalsIgnoreCase(strCurrent)) {
                                //未提交的任务
                                try {
                                    String id = UIUtil.getValue(signTaskMap, SELECT_ID);
                                    domainObject.setId(id);
                                    //获取会签任务与ECR得关联关系的时间
                                    String date = domainObject.getInfo(context, "to[JFECR2Task].originated");
                                    if (UIUtil.isNotNullAndNotEmpty(date) && getDateTimeOverdue(date, Locale.US)) {
                                        flag = Boolean.TRUE;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            return flag;
                        }).map(m -> {
                            Map signTaskMap = (Map) m;
                            String strTitle = (String) signTaskMap.get(SELECT_ATTRIBUTE_TITLE);
                            return strTitle;
                        }).collect(Collectors.toCollection(StringList::new));
                        _logger.info("Countersign,APR, Quotation:signTitleList{}", signTitleList);
                        if (signTitleList.isEmpty()) {
                            continue;
                        } else {
                            //统计延期数量
                            quantity++;
                        }
                        String signTitles = signTitleList.toString();
                        if (signTitles.contains("Professional Review(Foam AME)")) {
                            iFAME++;
                        }
                        if (signTitles.contains("Professional Review(Trim AME)")) {
                            iTAME++;
                        }
                        if (signTitles.contains("Professional Review(Final assembly AME)") || signTitles.contains("Professional Review(AME)")) {
                            iAME++;
                        }
                        if (signTitles.contains("AQE")) {
                            iAQE++;
                        }
                        if (signTitles.contains("Costing")) {
                            icosting++;
                        }
                        if (signTitles.contains("Launch")) {
                            ilaunch++;
                        }
                        if (signTitles.contains("采购")) {
                            icaigou++;
                        }
                        if (signTitles.contains("物流")) {
                            iwuliu++;
                        }
                        if (signTitles.contains("Quotation")) {
                            iQuotation++;
                        }
                    }
                    if ("Countersign".equalsIgnoreCase(key)) {
                        returnMap.put("AME", iAME);
                        returnMap.put("TAME", iTAME);
                        returnMap.put("FAME", iFAME);
                        returnMap.put("AQE", iAQE);
                        returnMap.put("Costing", icosting);
                        returnMap.put("Launch", ilaunch);
                        returnMap.put("caigou", icaigou);
                        returnMap.put("wuliu", iwuliu);
                        returnMap.put(key, quantity);
                    }

                    if ("Quotation".equalsIgnoreCase(key)) {
                        returnMap.put("Quotation", iQuotation);
                    }
                    break;
                }
                case "APR": {
                    Integer iAPR = 0;
                    while (iterator.hasNext()) {
                        Map ercMap = (Map) iterator.next();
                        String ecrId = UIUtil.getValue(ercMap, SELECT_ID);
                        String current = UIUtil.getValue(ercMap, SELECT_CURRENT);
                        if ("APR".equalsIgnoreCase(current)) {
                            //如果是APR需要判断   会签任务的状态是否是有一个及其以上在工作中
                            MapList signTaskList = (MapList) infoMap.get(ecrId);
                            //拿取审批中的会签任务 的标题
                            MapList signTitleMapList = (MapList) signTaskList.stream().filter(m -> {
                                Map signTaskMap = (Map) m;
                                String strCurrent = UIUtil.getValue(signTaskMap, SELECT_CURRENT);
                                String strType = UIUtil.getValue(signTaskMap, SELECT_TYPE);
                                if ("Active".equalsIgnoreCase(strCurrent) && JF_PLMConstants_mxJPO.TYPE_JF_APRTask.equalsIgnoreCase(strType)) {
                                    return Boolean.TRUE;
                                } else {
                                    return Boolean.FALSE;
                                }
                            }).collect(Collectors.toCollection(MapList::new));
                            _logger.info("signTitleMapList:{}", signTitleMapList);
                            if (signTitleMapList.size() != 1) {
                                //存在延期  不统计
                            } else {
                                Map signTaskMap = (Map) signTitleMapList.get(0);
                                String id = UIUtil.getValue(signTaskMap, SELECT_ID);
                                domainObject.setId(id);
                                //获取会签任务与ECR得关联关系的时间
                                String date = domainObject.getInfo(context, "to[JFECR2Task].originated");
                                if (UIUtil.isNotNullAndNotEmpty(date) && getDateTimeOverdue(date, Locale.US)) {
                                    quantity++;
                                    iAPR++;
                                }
                            }
                        }
                    }
                    if ("APR".equalsIgnoreCase(key)) {
                        returnMap.put("APR", iAPR);
                    }
                    break;
                }
                case "Complete": {
                    returnMap.put(key, ecrGroupMapList.size());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnMap;
    }


    /**
     * 获取对象的正在审批中的流程， 并判断审批时间是否延期
     *
     * @param context
     * @param domainObject
     * @return void
     * @throws
     * @author LIUJR
     * @date 2025/4/11 10:09
     * @description
     */
    public static String getECRInboxTask(Context context, DomainObject domainObject) throws Exception {
        StringList busSelectList = JF_Util_mxJPO.basicBolistSel();
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        busSelectList.add(DomainConstants.SELECT_ORIGINATED);
        MapList mapList = domainObject.getRelatedObjects(context,
                DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
                DomainConstants.TYPE_ROUTE,
                busSelectList,
                relSelectList,
                false,
                true,
                (short) 1,
                "current=='In Process'",
                "",
                0
        );
        _logger.info("Submit,Review, mapList:{}", mapList);
        if (mapList.isEmpty()) {
            return "";
        } else {
            Map map2 = (Map) mapList.get(0);
            String routeId = (String) map2.get(DomainConstants.SELECT_ID);
            DomainObject route = DomainObject.newInstance(context, routeId);
            busSelectList.add("state[Assigned].actual");
            MapList inboxTaskMapList = route.getRelatedObjects(context,
                    RELATIONSHIP_ROUTE_TASK,
                    TYPE_INBOX_TASK,
                    busSelectList,
                    relSelectList,
                    true,
                    false,
                    (short) 1,
                    "current=='Assigned'",
                    "",
                    0
            );
            if (inboxTaskMapList.isEmpty()) {
                return "";
            }
            String strLineMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.Review.LineManager");
            String strChairMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.Review.ChairManager");
            String strDepartmentMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.Review.DepartmentManager");
//            String strProjectManagerMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECR.Review.ProjectManager");
            String strProjectManagerMessZh = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", Locale.CHINA, "emxComponents.ECR.Review.ProjectManager");
            String strProjectManagerMessUs = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", Locale.US, "emxComponents.ECR.Review.ProjectManager");
            //需要增加兼容 liujr 20260624  兼容旧的和新的流程title设置
            String strProjectManagerMessZh_old = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", Locale.CHINA, "emxComponents.ECR.Review.ProjectManager_Old");
            String strProjectManagerMessUs_old = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", Locale.US, "emxComponents.ECR.Review.ProjectManager_Old");
            Map inboxTaskMap = (Map) inboxTaskMapList.get(0);
            String date = UIUtil.getValue(inboxTaskMap, "state[Assigned].actual");
            String title = UIUtil.getValue(inboxTaskMap, SELECT_ATTRIBUTE_TITLE);
            _logger.info("Submit,Review, date:{}", date);
            if (getDateTimeOverdue(date, Locale.US)) {
                //需要增加兼容 liujr 20260624  兼容旧的和新的流程title设置
                if (title.equalsIgnoreCase(strProjectManagerMessZh) || title.equalsIgnoreCase(strProjectManagerMessUs) || title.equalsIgnoreCase(strProjectManagerMessZh_old) || title.equalsIgnoreCase(strProjectManagerMessUs_old)) {
                    return "PM";
                } else {
                    return "DM";
                }
            } else {
                return "";
                //未超期
            }
        }
    }

    /**
     * 判断时间是否超期三天
     *
     * @param date
     * @return java.lang.Boolean
     * @throws
     * @author LIUJR
     * @date 2025/3/10 14:30
     * @description
     */
    public static Boolean getDateTimeOverdue(String date, Locale locale) throws ParseException {
        // 使用stream()和max()找出最新日期
        // 将日期字符串转换为LocalDate对象并排序，找到最新的日期
        // 解析日期
        // 定义日期格式（注意：M/d/yyyy对应4/11/2025这样的格式）
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy hh:mm:ss a");

        // 解析字符串为日期对象
        Date targetDate = sdf.parse(date);

        // 计算三天后的日期
        // 也可以计算天数差
        // 获取当前日期
        Date currentDate = new Date();
        long diffInMillis = currentDate.getTime() - targetDate.getTime();
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
        _logger.info("diffInDays:{}", diffInDays);
        // 比较日期
        if (diffInDays >= 3) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 项目的ecr的信息
     *
     * @param
     * @return java.util.Map
     * @throws
     * @author LIUJR
     * @date 2025/3/10 13:33
     * @description
     */
    public static Map getProjectECRColumnsInfo() {
        //sheet中的列对应的属性
        HashMap hashMap = new HashMap();
        Map<Integer, String> attributeMap = new HashMap<>();
        attributeMap.put(7, "各项目延期审批 ECR数量统计");
//
        Map<Integer, String> RowCountMap = new HashMap<>();
//        RowCountMap.put(6, "F$/D$");  //最后统计 需要其他的值
        RowCountMap.put(6, "IFERROR(F$/D$,IF(D$=0,0,1))");  //最后统计 需要其他的值
//        =IFERROR(D7/C7,IF(C7=0,0,1))
        RowCountMap.put(4, "D$-F$-V$");    //最后统计 需要其他的值

        Map<Integer, String> lastRowMap = new HashMap<>();
        lastRowMap.put(3, "SUM(D5:D$)");
//        lastRowMap.put(4, "D$-F$-T$");    //最后统计 需要其他的值
        lastRowMap.put(5, "SUM(F5:F$)");
//        lastRowMap.put(6, "F$/D$");  //最后统计 需要其他的值
        lastRowMap.put(8, "SUM(I5:J$)");
        lastRowMap.put(10, "SUM(K5:K$)");
        lastRowMap.put(11, "SUM(L5:L$)");
        lastRowMap.put(12, "SUM(M5:M$)");
        lastRowMap.put(13, "SUM(N5:N$)");
        lastRowMap.put(14, "SUM(O5:O$)");
        lastRowMap.put(15, "SUM(P5:P$)");
        lastRowMap.put(16, "SUM(Q5:Q$)");
        lastRowMap.put(17, "SUM(R5:R$)");
        lastRowMap.put(18, "SUM(S5:S$)");
        lastRowMap.put(19, "SUM(T5:T$)");
        lastRowMap.put(20, "SUM(U5:U$)");
        lastRowMap.put(21, "SUM(V5:V$)");
        //参数
        HashMap<String, Integer> settingMap = new HashMap<>();
        settingMap.put("startCell", 0);
        settingMap.put("endCell", 23);
        settingMap.put("startRow", 4);
        settingMap.put("statisticRow", 5);

        //需要合并的列
        Integer[] integers = Stream.of(7).toArray(Integer[]::new);
        IntegerList integerList = new IntegerList();
        integerList.addAll(Arrays.asList(integers));
        //需要跳过的列  4,5,6
        Integer[] skips = Stream.of(0, 1, 2, 7, 22).toArray(Integer[]::new);
        IntegerList skipList = new IntegerList();
        skipList.addAll(Arrays.asList(skips));

        hashMap.put("attributeMap", attributeMap);
        hashMap.put("settingMap", settingMap);
        hashMap.put("mergeList", integerList);
        hashMap.put("lastRowMap", lastRowMap);
        hashMap.put("skipList", skipList);
        hashMap.put("RowCountMap", RowCountMap);
        return hashMap;
    }


    /**
     * 统计ECO数据导出到excel的sheet2
     *
     * @param context
     * @param workbook
     * @param cellBorderStyle
     * @param strFormattedDate
     */
    public static void writePLMProjectECOCountToFile(Context context, Workbook workbook, CellStyle cellBorderStyle, String strFormattedDate) throws Exception {

        //获取动态列的值
        Map projectECOColumnsInfo = getProjectECOColumnsInfo();
        //动态写入行的动态列配置
        Map<Integer, String> attributeMap = (Map<Integer, String>) projectECOColumnsInfo.get("attributeMap");
        //起始行，起始列，结束列配置
        Map<String, Integer> settingMap = (Map<String, Integer>) projectECOColumnsInfo.get("settingMap");
        Integer startCell = settingMap.get("startCell");  //起始列
        Integer endCell = settingMap.get("endCell");    //结束列
        Integer startRow = settingMap.get("startRow");  //开始行
        Integer statisticRow = settingMap.get("statisticRow");  //统计行
        Map<Integer, String> RowCountMap = (Map<Integer, String>) projectECOColumnsInfo.get("RowCountMap");
        //合并列配置
        IntegerList mergeList = (IntegerList) projectECOColumnsInfo.get("mergeList");
        //跳过的列配置
        IntegerList skipList = (IntegerList) projectECOColumnsInfo.get("skipList");
        //最后一行的统计公式
        Map<Integer, String> lastRowMap = (Map<Integer, String>) projectECOColumnsInfo.get("lastRowMap");

        //获取表格
        Sheet sheet = workbook.getSheetAt(2);
        //获取系统中的项目
        MapList res = DomainObject.findObjects(context, TYPE_PROJECT_SPACE, "*", "", StringList.create(SELECT_NAME, SELECT_ID, SELECT_DESCRIPTION));
        //在合并行的上方插入项目数量的行  先插入 直接获取
        sheet.shiftRows(statisticRow, sheet.getLastRowNum(), res.size() - 1, true, false);
        //存放数据容器
        MapList mapList = new MapList();
        //开始遍历项目
        for (int i = 0; i < res.size(); i++) {
            Map map = (Map) res.get(i);
            Map<Integer, Object> hashMap = new HashMap<>();
            String psId = UIUtil.getValue(map, SELECT_ID);
            String psName = UIUtil.getValue(map, SELECT_NAME);
            String psTitle = UIUtil.getValue(map, SELECT_DESCRIPTION);
            DomainObject projectObject = DomainObject.newInstance(context, psId);
            //获取项目下的ECR
            MapList ecrMapList = projectObject.getRelatedObjects(
                    context,
                    JF_PLMConstants_mxJPO.REL_JFChange2Project, // relationship pattern
                    TYPE_JFECR + "," + TYPE_JFNewECR,       // object pattern
                    StringList.create(SELECT_NAME, SELECT_ID, SELECT_CURRENT, SELECT_ORIGINATED),// object selects
                    new StringList(), // relationship selects
                    true,             // to direction
                    false,            // from direction
                    (short) 1,        // recursion level
                    "",               // object where clause
                    "",
                    (short) 0
            );
            MapList ecoAllMapList = new MapList();
            for (Object o : ecrMapList) {
                Map ecrMap = (Map) o;
                String ecrId = UIUtil.getValue(ecrMap, "id");
                DomainObject ecrObject = DomainObject.newInstance(context, ecrId);
                //获取eco
                MapList ecoMapList = ecrObject.getRelatedObjects(
                        context,
                        JF_PLMConstants_mxJPO.REL_JFECR2CO, // relationship pattern
                        JF_PLMConstants_mxJPO.TYPE_JFECO,       // object pattern
                        StringList.create(SELECT_NAME, SELECT_ID, SELECT_CURRENT, SELECT_ORIGINATED),// object selects
                        new StringList(), // relationship selects
                        false,             // to direction
                        true,            // from direction
                        (short) 1,        // recursion level
                        "",               // object where clause
                        "",
                        (short) 0
                );
                ecoAllMapList.addAll(ecoMapList);
            }
            hashMap.put(0, i + 1);//序号
            hashMap.put(1, psName);//项目
            hashMap.put(2, psTitle);//项目名称
            int isInWork = 0;
            int isComplete = 0;
            int isSDTSeat = 0;
            int isSDTBP = 0;
            int isSDTCaiGou = 0;
            int isSDTSDQ = 0;
            int isSDTWuLiu = 0;
            int isSDTAME = 0;
            int isSDTFAME = 0;//add
            int isSDTTAME = 0;//add
            int isSDTLaunch = 0;
            int isSDTAQE = 0;
            int iextension = 0;
            int NonZeroCount = 0;
            if (CollectionUtils.isEmpty(ecoAllMapList)) {
                hashMap.put(3, 0);//eco数量
            } else {
                hashMap.put(3, ecoAllMapList.size());//eco数量
            }
            Map groupEcoMap = (Map) ecoAllMapList.stream().collect(Collectors.groupingBy(m -> {
                Map info = (Map) m;
                return UIUtil.getValue(info, "current");
            }));
            //遍历key
            for (Object k : groupEcoMap.keySet()) {
                String key = String.valueOf(k);
                List infoList = (List) groupEcoMap.get(key);
                //每个分组下的数据
                MapList ecrGroupMapList = new MapList();
                ecrGroupMapList.addAll(infoList);
                Map<String, Integer> countMap = ecoTaskExtensionQuantity(context, ecrGroupMapList, key);
                _logger.info("key:{}", key);
                _logger.info("countMap:{}", countMap);
                if ("In_Work".equals(key)) {
                    isInWork = countMap.get(key);
                    iextension += isInWork;
                }
                if ("ExecuteFeedback".equals(key)) {
                    isSDTSeat = countMap.get("Chair");
                    isSDTBP = countMap.get("Financial");
                    isSDTCaiGou = countMap.get("Purchasing");
                    isSDTSDQ = countMap.get("SQD");
                    isSDTWuLiu = countMap.get("Logistics");
                    isSDTAME = countMap.get("AME");
                    isSDTFAME = countMap.get("FAME");
                    isSDTTAME = countMap.get("TAME");
                    isSDTLaunch = countMap.get("Launch");
                    isSDTAQE = countMap.get("AQE");
                    NonZeroCount = countMap.get("NonZeroCount");
                    iextension += NonZeroCount;
                }
                if ("Complete".equals(key)) {
                    isComplete = countMap.get(key);
                }
            }
            hashMap.put(4, iextension);//ECO延期审批数量
            hashMap.put(5, RowCountMap.get(5));//公式
            hashMap.put(6, attributeMap.get(6));
            hashMap.put(7, isInWork);
            hashMap.put(8, isSDTSeat);
            hashMap.put(9, isSDTBP);
            hashMap.put(10, isSDTCaiGou);
            hashMap.put(11, isSDTSDQ);
            hashMap.put(12, isSDTWuLiu);
            hashMap.put(13, isSDTAME);
            hashMap.put(14, isSDTFAME);
            hashMap.put(15, isSDTTAME);
            hashMap.put(16, isSDTLaunch);
            hashMap.put(17, isSDTAQE);
            hashMap.put(18, isComplete);
            hashMap.put(19, " ");
            mapList.add(hashMap);
        }
        mapList.addSortKey(String.valueOf(0), ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);
        mapList.sort();
        int projectRow = startRow;
        _logger.info("writePLMProjectECOCountToFile==写入中.....");
        for (int iRow = 0; iRow < mapList.size(); iRow++) {
            //写入文件
            Row row = sheet.createRow(projectRow);
            Map map = (Map) mapList.get(iRow);
            for (int cell = startCell; cell < endCell; cell++) {
                if (cell == 5) {
                    //需要计算
                    continue;
                }
                Cell cellItem = row.createCell(cell, CellType.NUMERIC);
                if (cell == 1 || cell == 2 || cell == 6 || cell == 19) {
                    cellItem = row.createCell(cell, CellType.STRING);
                    cellItem.setCellValue(String.valueOf(map.get(cell)));
                } else if (cell == 3 || cell == 4 || cell == 18) {
                    cellItem = row.createCell(cell, CellType.NUMERIC);
                    cellItem.setCellValue((Integer) map.get(cell));
                } else {
                    if ((Integer) map.get(cell) == 0) {
                        cellItem = row.createCell(cell, CellType.BLANK);
                    } else {
                        cellItem = row.createCell(cell, CellType.NUMERIC);
                        cellItem.setCellValue((Integer) map.get(cell));
                    }
                }
                cellItem.setCellStyle(cellBorderStyle);
            }
            projectRow++;
            //计算第四列每一行的值
            Cell cellItem = row.createCell(5, CellType.NUMERIC);
            String value = ((String) map.get(5)).replace("$", String.valueOf(projectRow));
            cellItem.setCellFormula(value); //公式
            cellItem.setCellStyle(getPercentageStyle(workbook));
        }
        CellRangeAddress cellRangeAddress;
        //合并
        for (int i = 0; i < mergeList.size(); i++) {
            int iMerge = mergeList.get(i);
            //需要合并
            cellRangeAddress = new CellRangeAddress(startRow, projectRow - 1, iMerge, iMerge); // 参数依次是：开始行，结束行，开始列，结束列
            sheet.addMergedRegion(cellRangeAddress);
        }
        //统计行设置
        Row row = sheet.getRow(projectRow);
        for (int cell = startCell; cell < endCell; cell++) {
            if (skipList.contains(cell)) {
                continue;
            }
            //统计行
            if (lastRowMap.containsKey(cell)) {
                String value = lastRowMap.get(cell).replace("$", String.valueOf(projectRow));
                Cell cellItem = row.getCell(cell);
                cellItem.setCellFormula(value); //公式
            }
        }
        //标题
        Cell cellTitle = sheet.getRow(0).getCell(0);
        String cellValue = cellTitle.getStringCellValue();
        cellTitle.setCellValue(cellValue.replace("$1", strFormattedDate));
        sheet.setForceFormulaRecalculation(true);
    }

    public static CellStyle getPercentageStyle(Workbook workbook) {
        CellStyle cellBorderStyle = workbook.createCellStyle();
        cellBorderStyle.setBorderTop(BorderStyle.THIN);
        cellBorderStyle.setBorderBottom(BorderStyle.THIN);
        cellBorderStyle.setBorderLeft(BorderStyle.THIN);
        cellBorderStyle.setBorderRight(BorderStyle.THIN);
        cellBorderStyle.setAlignment(HorizontalAlignment.CENTER); // 水平居中
        cellBorderStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        cellBorderStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00%")); // 设置为百分比格式，保留两位小数
        return cellBorderStyle;
    }


    /**
     * ecoTask
     *
     * @param context
     * @param
     * @param key
     * @return
     */
    public static Map<String, Integer> ecoTaskExtensionQuantity(Context context, MapList ecoGroupMapList, String key) {
        int quantity = 0;
        Map<String, Integer> returnMap = new HashMap();
        try {

            Iterator iterator = ecoGroupMapList.iterator();
            _logger.info("key:{}", key);
            _logger.info("ecoGroupMapList:{}", ecoGroupMapList);
            DomainObject domainObject = DomainObject.newInstance(context);
            switch (key) {
                case "Create":
                case "In_Work":
                case "Complete": {
                    returnMap.put(key, ecoGroupMapList.size());
                    break;
                }
                case "ExecuteFeedback": {
                    //执行任务
                    Integer izuoyi = 0;
                    Integer icaiwu = 0;
                    Integer icaigou = 0;
                    Integer iSDQ = 0;
                    Integer iwuliu = 0;
                    Integer iAME = 0;
                    Integer iFAME = 0;
                    Integer iTAME = 0;
                    Integer ilaunch = 0;
                    Integer iAQE = 0;
                    Integer nonZeroCount = 0; // 新增计数器
                    while (iterator.hasNext()) {
                        Map ecoMap = (Map) iterator.next();
                        String ecoId = UIUtil.getValue(ecoMap, SELECT_ID);
                        //获取eco下的task执行计划
                        MapList ecoTaskList = getECOTask(context, ecoId);
                        _logger.info("ExecuteFeedback:ecoId{}", ecoId);
                        _logger.info("ExecuteFeedback:ecoTaskList{}", ecoTaskList);
                        //拿取ceoTask的角色信息
                        StringList ecoRoleList = (StringList) ecoTaskList.stream().filter(m -> {
                            Map ecoTaskMap = (Map) m;
                            String strCurrent = UIUtil.getValue(ecoTaskMap, SELECT_CURRENT);
                            Boolean flag = Boolean.FALSE;
                            if (!("Complete".equalsIgnoreCase(strCurrent) || "Review".equalsIgnoreCase(strCurrent))) {
                                //未完成
                                try {
                                    String id = UIUtil.getValue(ecoTaskMap, SELECT_ID);
                                    domainObject.setId(id);
                                    String date = domainObject.getInfo(context, "attribute[Task Estimated Finish Date]");
                                    _logger.info("ExecuteFeedback ====:比较是否预期{}", isExecutionOverdue(date));
                                    if (UIUtil.isNotNullAndNotEmpty(date) && isExecutionOverdue(date)) {
                                        _logger.info("ExecuteFeedback ====:执行计划逾期");
                                        flag = Boolean.TRUE;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            return flag;
                        }).map(m -> {
                            Map ecoTaskMap = (Map) m;
                            String Role = (String) ecoTaskMap.get("attribute[Project Role]");
                            return Role;
                        }).collect(Collectors.toCollection(StringList::new));
                        _logger.info("ExecuteFeedback ====:ecoRoleList{}", ecoRoleList);
                        if (ecoRoleList.isEmpty()) {
                            continue;
                        }
                        // 新增临时标志，用于检查是否找到任何角色
                        boolean foundRole = false;
                        for (String s : ecoRoleList) {
                            if ("Chair manager".equals(s)) {
                                izuoyi++;
                                foundRole = true;
                            } else if ("Financial BP".equals(s)) {
                                icaiwu++;
                                foundRole = true;
                            } else if ("Purchasing representative".equals(s)) {
                                icaigou++;
                                foundRole = true;
                            } else if ("SQD Representative".equals(s)) {
                                iSDQ++;
                                foundRole = true;
                            } else if ("Logistics representative".equals(s)) {
                                iwuliu++;
                                foundRole = true;
                            } else if ("Foam AME representative".equals(s)) {
                                iFAME++;
                                foundRole = true;
                            } else if ("Trim AME representative".equals(s)) {
                                iTAME++;
                                foundRole = true;
                            } else if ("AME representative".equals(s)) {
                                iAME++;
                                foundRole = true;
                            } else if ("Launch manager".equals(s)) {
                                ilaunch++;
                                foundRole = true;
                            } else if ("AQE representative/PQL".equals(s)) {
                                iAQE++;
                                foundRole = true;
                            }
                        }
                        // 如果找到任何角色，则增加计数
                        if (foundRole) {
                            nonZeroCount++;
                        }
                    }

                    returnMap.put("Chair", izuoyi);
                    returnMap.put("Financial", icaiwu);
                    returnMap.put("Purchasing", icaigou);
                    returnMap.put("SQD", iSDQ);
                    returnMap.put("Logistics", iwuliu);
                    returnMap.put("AME", iAME);
                    returnMap.put("FAME", iFAME);
                    returnMap.put("TAME", iTAME);
                    returnMap.put("Launch", ilaunch);
                    returnMap.put("AQE", iAQE);
                    returnMap.put("NonZeroCount", nonZeroCount);// 新增字段存储次数
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnMap;
    }

    public static MapList getECOTask(Context context, String ecrId) throws Exception {
        DomainObject domainObject = DomainObject.newInstance(context, ecrId);
        MapList ecoTaskMapList = domainObject.getRelatedObjects(
                context,
                "JFCO2ECOTask", // relationship pattern
                "JF_ECOTask",       // object pattern
                StringList.create(SELECT_NAME, SELECT_ID, SELECT_CURRENT, "attribute[Project Role]"),// object selects
                new StringList(), // relationship selects
                false,             // to direction
                true,            // from direction
                (short) 1,        // recursion level
                "",               // object where clause
                "",
                (short) 0
        );
        return ecoTaskMapList;
    }

    /**
     * eco信息
     *
     * @return
     */
    public static Map getProjectECOColumnsInfo() {
        //sheet中的列对应的属性
        HashMap hashMap = new HashMap();
        Map<Integer, String> attributeMap = new HashMap<>();
        attributeMap.put(6, "各项目延期审批 ECO数量统计");

        Map<Integer, String> RowCountMap = new HashMap<>();
        RowCountMap.put(5, "IFERROR(E$/D$,IF(D$=0,0,1))");  //最后统计 需要其他的值

        Map<Integer, String> lastRowMap = new HashMap<>();
        lastRowMap.put(3, "SUM(D5:D$)");
        lastRowMap.put(4, "SUM(E5:E$)");
//        lastRowMap.put(4, "SUM(D5:D$)");需要用到其他列 后面计算
        lastRowMap.put(7, "SUM(H5:H$)");
        lastRowMap.put(8, "SUM(I5:I$)");
        lastRowMap.put(9, "SUM(J5:J$)");
        lastRowMap.put(10, "SUM(K5:K$)");
        lastRowMap.put(11, "SUM(L5:L$)");
        lastRowMap.put(12, "SUM(M5:M$)");
        lastRowMap.put(13, "SUM(N5:N$)");
        lastRowMap.put(14, "SUM(O5:O$)");
        lastRowMap.put(15, "SUM(P5:P$)");
        lastRowMap.put(16, "SUM(Q5:Q$)");
        lastRowMap.put(17, "SUM(R5:R$)");
        lastRowMap.put(18, "SUM(S5:S$)");
        //参数
        HashMap<String, Integer> settingMap = new HashMap<>();
        settingMap.put("startCell", 0);
        settingMap.put("endCell", 20);
        settingMap.put("startRow", 4);
        settingMap.put("statisticRow", 5);

        //需要合并的列
        Integer[] integers = Stream.of(6).toArray(Integer[]::new);
        IntegerList integerList = new IntegerList();
        integerList.addAll(Arrays.asList(integers));
        //需要跳过的列  4,5,6
        Integer[] skips = Stream.of(0, 1, 2, 6, 19).toArray(Integer[]::new);
        IntegerList skipList = new IntegerList();
        skipList.addAll(Arrays.asList(skips));

        hashMap.put("attributeMap", attributeMap);
        hashMap.put("settingMap", settingMap);
        hashMap.put("mergeList", integerList);
        hashMap.put("lastRowMap", lastRowMap);
        hashMap.put("skipList", skipList);
        hashMap.put("RowCountMap", RowCountMap);
        return hashMap;
    }

    /**
     * 当前时间和执行计划预计完成时间比较
     * true是超过了预计完成时间 false反之
     *
     * @param expectedCompletionTime
     * @return
     */
    public static boolean isExecutionOverdue(String expectedCompletionTime) {
        // 定义日期时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        // 获取当前时间
        Date currentTime = new Date();
        try {
            // 将预计完成时间字符串解析为 Date 对象
            Date expectedTime = sdf.parse(expectedCompletionTime);
            // 比较当前时间和预计完成时间
            return currentTime.after(expectedTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // 解析异常时返回 false
        }
    }


    /*
     * @description:对象是否存在
     * @author: caipan
     * @date: 2025/4/17 13:46:29
     * @param: * @param[1] context
     * @param[2] id
     * @return:
     **/
    public boolean exists(Context context, String id) {
        try {
            BusinessObject bo = new BusinessObject(id);
            if (bo.exists(context)) {
                return true;
            }
        } catch (Exception e) {
            _logger.error("error:{}", e.getMessage());
        }
        return false;
    }

    /**
     * 导出eco执行计划到excel sheet3
     *
     * @param context
     * @param workbook
     * @param cellBorderStyle
     * @param strFormattedDate
     * @throws Exception
     */
    public static void writePLMProjectECPToFile(Context context, Workbook workbook, CellStyle cellBorderStyle, String strFormattedDate) throws Exception {
        MapList info = getEcp(context);
        //获取动态列的值
        Map projectECOColumnsInfo = getECPColumnsInfo();
        //动态写入行的动态列配置
        Map<Integer, String> attributeMap = (Map<Integer, String>) projectECOColumnsInfo.get("attributeMap");
        //起始行，起始列，结束列配置
        Map<String, Integer> settingMap = (Map<String, Integer>) projectECOColumnsInfo.get("settingMap");
        Integer startCell = settingMap.get("startCell");  //起始列
        Integer endCell = settingMap.get("endCell");    //结束列
        Integer startRow = settingMap.get("startRow");  //开始行

        Map<String, List<Integer>> roleMap = (Map<String, List<Integer>>) projectECOColumnsInfo.get("roleMap");

        //获取表格
        Sheet sheet = workbook.getSheetAt(3);
        int rowIndex = startRow;
        //遍历数据，写入数据
        for (int i = 0; i < info.size(); i++) {
            Row row = sheet.createRow(rowIndex);
            Map ecpMap = (Map) info.get(i);
            //跳过的列配置
            List<Integer> skipList = new ArrayList<>((List<Integer>) projectECOColumnsInfo.get("skipList"));
            String role = UIUtil.getValue(ecpMap, "attribute[Project Role]");
            if (UIUtil.isNotNullAndNotEmpty(role)) {
                List<Integer> list = roleMap.get(role);
                for (int i1 = 0; i1 < list.size(); i1++) {
                    int integer = list.get(i1);
                    Cell cell = row.createCell(integer, CellType.STRING);
                    String attrName = attributeMap.get(integer);
                    String attrValue = UIUtil.getValue(ecpMap, attrName);
                    if ("to[Assigned Tasks].from[Person].name".equals(attrName)) {
                        if (UIUtil.isNotNullAndNotEmpty(attrValue)) {
                            attrValue = attrValue.split("\u0007")[0];
                            attrValue = PersonUtil.getFullName(context, attrValue);
                        }
                    } else if ("attribute[Task Estimated Finish Date]".equals(attrName) || "attribute[JF_DAActualFinishTime]".equals(attrName)) {
                        if (UIUtil.isNotNullAndNotEmpty(attrValue)) {
                            // 解析输入日期
                            LocalDateTime dateTime = LocalDateTime.parse(attrValue, inputFormatter);
                            // 格式化输出日期
                            attrValue = dateTime.format(outputFormatter1);
                        }
                    }
                    cell.setCellValue(attrValue);
                    cell.setCellStyle(cellBorderStyle);
                    skipList.remove(Integer.valueOf(integer));
                }
            }
            _logger.info("skipList====:::{}", JSON.toJSONString(skipList));
            //序号写入
            Cell cellItem = row.createCell(0, CellType.STRING);
            cellItem.setCellValue(String.valueOf(i + 1));
            cellItem.setCellStyle(cellBorderStyle);
            for (int j = startCell; j <= endCell; j++) {
                if (skipList.contains(j)) {
                    Cell cell = row.createCell(j, CellType.STRING);
                    cell.setCellValue(EMPTY_STRING);
                    cell.setCellStyle(cellBorderStyle);
                    continue;
                }
                if (j < 7) {
                    Cell cell = row.createCell(j, CellType.STRING);
                    String attrName = attributeMap.get(j);
                    String attrValue = UIUtil.getValue(ecpMap, attrName);
                    if ("current".equals(attrName)) {
                        attrValue = englishToChinese(attrValue);
                    }
                    cell.setCellValue(attrValue);
                    cell.setCellStyle(cellBorderStyle);
                }
            }
            rowIndex++;
        }
    }


    /**
     * ecp列信息
     *
     * @return
     */
    public static Map getECPColumnsInfo() {
        //sheet中的列对应的属性
        HashMap hashMap = new HashMap();
        Map<Integer, String> attributeMap = new HashMap<>();
        attributeMap.put(1, "name");
        attributeMap.put(2, "attribute[Title]");
        attributeMap.put(3, "to[JFCO2ECOTask].from.name");
        attributeMap.put(4, "to[JFCO2ECOTask].from.to[JFECR2CO].from.from[JFChange2Project].to.name");
        attributeMap.put(5, "to[JFCO2ECOTask].from.to[JFECR2CO].from.from[JFChange2Project].to.description");
        attributeMap.put(6, "current");
        attributeMap.put(7, "attribute[Task Estimated Finish Date]");
        attributeMap.put(8, "attribute[JF_DAActualFinishTime]");
        attributeMap.put(9, "to[Assigned Tasks].from[Person].name");
        attributeMap.put(10, "attribute[Task Estimated Finish Date]");
        attributeMap.put(11, "attribute[JF_DAActualFinishTime]");
        attributeMap.put(12, "to[Assigned Tasks].from[Person].name");
        attributeMap.put(13, "attribute[Task Estimated Finish Date]");
        attributeMap.put(14, "attribute[JF_DAActualFinishTime]");
        attributeMap.put(15, "to[Assigned Tasks].from[Person].name");
        attributeMap.put(16, "attribute[Task Estimated Finish Date]");
        attributeMap.put(17, "attribute[JF_DAActualFinishTime]");
        attributeMap.put(18, "to[Assigned Tasks].from[Person].name");
        attributeMap.put(19, "attribute[Task Estimated Finish Date]");
        attributeMap.put(20, "attribute[JF_DAActualFinishTime]");
        attributeMap.put(21, "to[Assigned Tasks].from[Person].name");
        attributeMap.put(22, "attribute[Task Estimated Finish Date]");
        attributeMap.put(23, "attribute[JF_DAActualFinishTime]");
        attributeMap.put(24, "to[Assigned Tasks].from[Person].name");
        attributeMap.put(25, "attribute[Task Estimated Finish Date]");
        attributeMap.put(26, "attribute[JF_DAActualFinishTime]");
        attributeMap.put(27, "to[Assigned Tasks].from[Person].name");
        attributeMap.put(28, "attribute[Task Estimated Finish Date]");
        attributeMap.put(29, "attribute[JF_DAActualFinishTime]");
        attributeMap.put(30, "to[Assigned Tasks].from[Person].name");
        attributeMap.put(31, "attribute[Task Estimated Finish Date]");
        attributeMap.put(32, "attribute[JF_DAActualFinishTime]");
        attributeMap.put(33, "to[Assigned Tasks].from[Person].name");
        attributeMap.put(34, "attribute[Task Estimated Finish Date]");
        attributeMap.put(35, "attribute[JF_DAActualFinishTime]");
        attributeMap.put(36, "to[Assigned Tasks].from[Person].name");

        //参数
        HashMap<String, Integer> settingMap = new HashMap<>();
        settingMap.put("startCell", 1);
        settingMap.put("endCell", 37);
        settingMap.put("startRow", 4);


        //需要跳过的列  4,5,6
        Integer[] skips = Stream.of(7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37).toArray(Integer[]::new);
        List<Integer> skipList = new ArrayList<>();
        skipList.addAll(Arrays.asList(skips));

        HashMap<String, List<Integer>> roleMap = new HashMap<>();
        List<Integer> izuoyi = new ArrayList<>();
        izuoyi.addAll(Arrays.asList(Stream.of(7, 8, 9).toArray(Integer[]::new)));
        roleMap.put("Chair manager", izuoyi);

        List<Integer> icaiwu = new ArrayList<>();
        icaiwu.addAll(Arrays.asList(Stream.of(10, 11, 12).toArray(Integer[]::new)));
        roleMap.put("Financial BP", icaiwu);

        List<Integer> icaigou = new ArrayList<>();
        icaigou.addAll(Arrays.asList(Stream.of(13, 14, 15).toArray(Integer[]::new)));
        roleMap.put("Purchasing representative", icaigou);

        List<Integer> iSDQ = new ArrayList<>();
        iSDQ.addAll(Arrays.asList(Stream.of(16, 17, 18).toArray(Integer[]::new)));
        roleMap.put("SQD Representative", iSDQ);

        List<Integer> iwuliu = new ArrayList<>();
        iwuliu.addAll(Arrays.asList(Stream.of(19, 20, 21).toArray(Integer[]::new)));
        roleMap.put("Logistics representative", iwuliu);

        List<Integer> iAME = new ArrayList<>();
        iAME.addAll(Arrays.asList(Stream.of(22, 23, 24).toArray(Integer[]::new)));
        roleMap.put("AME representative", iAME);

        List<Integer> iFAME = new ArrayList<>();
        iFAME.addAll(Arrays.asList(Stream.of(25, 26, 27).toArray(Integer[]::new)));
        roleMap.put("Foam AME representative", iFAME);

        List<Integer> iTAME = new ArrayList<>();
        iTAME.addAll(Arrays.asList(Stream.of(28, 29, 30).toArray(Integer[]::new)));
        roleMap.put("Trim AME representative", iTAME);

        List<Integer> ilaunch = new ArrayList<>();
        ilaunch.addAll(Arrays.asList(Stream.of(31, 32, 33).toArray(Integer[]::new)));
        roleMap.put("Launch manager", ilaunch);

        List<Integer> iAQE = new ArrayList<>();
        iAQE.addAll(Arrays.asList(Stream.of(34, 35, 36).toArray(Integer[]::new)));
        roleMap.put("AQE representative/PQL", iAQE);

        hashMap.put("attributeMap", attributeMap);
        hashMap.put("settingMap", settingMap);
        hashMap.put("skipList", skipList);
        hashMap.put("roleMap", roleMap);
        return hashMap;
    }

    public static String englishToChinese(String english) {
        return ecpCurrentTranslationMap.getOrDefault(english, english); // 如果没有找到，返回原始英文
    }

    public static MapList getEcp(Context context) throws Exception {
        StringList strings = new StringList();
        strings.add("name");
        strings.add("to[JFCO2ECOTask].from.name");
        strings.add("to[JFCO2ECOTask].from.to[JFECR2CO].from.from[JFChange2Project].to.name");
        strings.add("to[JFCO2ECOTask].from.to[JFECR2CO].from.from[JFChange2Project].to.description");
        strings.add("current");
        strings.add("attribute[Title]");
        strings.add("attribute[Task Estimated Finish Date]");
        strings.add("attribute[JF_DAActualFinishTime]");
        strings.add("attribute[Project Role]");
        strings.add("to[Assigned Tasks].from[Person].name");
        MapList res = DomainObject.findObjects(context, "JF_ECOTask", "*", "", new StringList(SELECT_ID));
        StringList allEcpList = (StringList) res.stream().map(m -> {
            Map ercMap = (Map) m;
            return UIUtil.getValue(ercMap, SELECT_ID);
        }).collect(Collectors.toCollection(StringList::new));
        MapList info = DomainObject.getInfo(context, allEcpList.toStringArray(), strings);
        info.addSortKey(SELECT_NAME, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
        info.sort();
        _logger.info("info::::{}", JSON.toJSONString(info));
        return info;
    }

}
