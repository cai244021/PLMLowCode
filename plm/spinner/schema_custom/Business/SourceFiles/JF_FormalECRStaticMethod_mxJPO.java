import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.util.StringList;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.matrixone.apps.domain.DomainConstants.*;

/**
 * @author CHENYAN
 * @version v1.0.0
 * 创建时间：2026/1/21 12:04
 * @description
 */
public class JF_FormalECRStaticMethod_mxJPO implements JF_PLMConstants_mxJPO {
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_FormalECRStaticMethod_mxJPO.class);

    //关联项目
    public static final String STRING_SELECT_CONNECT_PROJECT = "to[JFProject2RootPart|attribute[JF_BelongPart]=='Y'].from.name";
    public static final String STRING_SELECT_CONNECT_PROJECT_1 = "to[JFProject2RootPart].from.name";

    //关联ECR的变更来源
    public static final String STRING_SELECT_JFRELATEITEM_JFCHANGESOURCE = "to[JFRelateItem].attribute[JFChangeSource]";
    public static final String STRING_SELECT_JFECRCONECTIONPROJECT = "from[JFChange2Project].to.id";

    //零件关联项目
    public static final String STRING_SELECT_JF_LogisticsFeesWhere = "to[JFPS2VPM|%s].attribute[JF_LogisticsFees]";
    public static final String STRING_SELECT_JF_LogisticsFees = "to[JFPS2VPM].attribute[JF_LogisticsFees]";

    public static final String STRING_SELECT_JFPS2VPMRELID_Where = "to[JFPS2VPM|%s].id";



    //价格的TXO包名
    public static final String STRING_TXO_PRICE_PACKAGE_NAME = "JF_VPMReferenceCost.";
    public static final String STRING_ISEDIT = "isEdit";
    public static final String STRING_Row_Edit = "rowEdit";
    //默认权限
    public static final String STRING_ROLE_ALL =  "All";
    //表示是校验调用
    public static final String STRING_VERIFY_UNIT_PRICE =  "verifyUnitPrice";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // 定义数字格式化器，防止科学计数法或多余的小数位
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.##");

    public static final StringList EDIT_ECRCosting_TABLE_ROLE_LIST = new StringList();
    public static final StringList EDIT_ECRController_TABLE_ROLE_LIST = new StringList();
    static {
        //初始化可以下载和上传ECR表格清单角色
        EDIT_ECRCosting_TABLE_ROLE_LIST.add(ATTR_PROJECT_ROLE_Range_PRR);
        EDIT_ECRCosting_TABLE_ROLE_LIST.add(ATTR_PROJECT_ROLE_Range_Costing);
        EDIT_ECRCosting_TABLE_ROLE_LIST.add(ATTR_PROJECT_ROLE_Range_InternalSupplier);
//        EDIT_ECRCosting_TABLE_ROLE_LIST.add("APR");
        EDIT_ECRController_TABLE_ROLE_LIST.add(ATTR_PROJECT_ROLE_Range_FinancialBP);
        EDIT_ECRController_TABLE_ROLE_LIST.add(ATTR_PROJECT_ROLE_Range_AME);
        EDIT_ECRController_TABLE_ROLE_LIST.add(ATTR_PROJECT_ROLE_Range_Foam_AME);
        EDIT_ECRController_TABLE_ROLE_LIST.add(ATTR_PROJECT_ROLE_Range_Trim_AME);
        EDIT_ECRController_TABLE_ROLE_LIST.add(ATTR_PROJECT_ROLE_Range_PRR);
        EDIT_ECRController_TABLE_ROLE_LIST.add(ATTR_PROJECT_ROLE_Range_Costing);
    }
    /**
     *
     *@description 判断表格是否是采购件表格
     *@param strSelectTable
     *@return boolean
     *@throws
     *@author CHENYAN
     *@date 2026/1/15 18:00
     */
    public static  boolean checkIsBuyTable(String strSelectTable){
        if (UIUtil.isNotNullAndNotEmpty(strSelectTable) && strSelectTable.contains("JFFormalECRCosting")){
            return true;
        }
        return false;
    }
    /**
     *
     *@description 判断表格是否是自制件表格
     *@param strSelectTable
     *@return boolean
     *@throws
     *@author CHENYAN
     *@date 2026/1/15 18:00
     */
    public static  boolean checkIsMakeTable(String strSelectTable){
        if (UIUtil.isNotNullAndNotEmpty(strSelectTable) && strSelectTable.contains("JFFormalECRController")){
            return true;
        }
        return false;
    }
    /**
     *
     *@description 获取各个角色可以编辑单件成本的属性
     *@param strRoleName
     *@return matrix.util.StringList
     *@throws
     *@author CHENYAN
     *@date 2026/1/20 15:58
     */
    public static StringList getCanEditUnitPartPriceByRoleName(String strRoleName, boolean isBuyTable, boolean isMakeTable)  {
        JF_LOGGER.info("----------------------------- getCanEditUnitPartPriceByRoleName begin ------------------------------------");
        StringList canEditAttrList = new StringList();
        if (isBuyTable){
            switch (strRoleName){
                case ATTR_PROJECT_ROLE_Range_PRR,ATTR_PROJECT_ROLE_Range_InternalSupplier:
                {
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_UnitPrice);
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_CostMoldPrice);
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_StagnationPrice);
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_TrialExpensesPrice);
                    break;
                }
                case ATTR_PROJECT_ROLE_Range_Costing:
                {
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_MoldPrice);
                    break;
                } case STRING_ROLE_ALL:
                {
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_UnitPrice);
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_CostMoldPrice);
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_StagnationPrice);
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_TrialExpensesPrice);
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_MoldPrice);
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_Man_hour);
                    break;
                }
                default:{

                }
            }

        }
        if (isMakeTable) {
            switch (strRoleName){
                case ATTR_PROJECT_ROLE_Range_PRR ,ATTR_PROJECT_ROLE_Range_InternalSupplier:
                {
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_CostMoldPrice);

                    break;
                }
                case ATTR_PROJECT_ROLE_Range_AME:
                {
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_Man_hour);
                    break;
                }
                case ATTR_PROJECT_ROLE_Range_Foam_AME:
                {
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_Man_hour);
                    break;
                }
                case ATTR_PROJECT_ROLE_Range_Trim_AME:
                {
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_Man_hour);
                    break;
                }
                case ATTR_PROJECT_ROLE_Range_Costing:
                {
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_MoldPrice);
                    break;
                }
                case ATTR_PROJECT_ROLE_Range_FinancialBP:{
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_TargetPrice);
                    break;
                }

                case STRING_ROLE_ALL:
                {
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_MoldPrice);
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_CostMoldPrice);
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_TargetPrice);
                    canEditAttrList.add(ATTR_JF_VPMReferenceCost_JF_Man_hour);
                    break;
                }
                default:{

                }
            }

        }
        JF_LOGGER.info("----------------------------- getCanEditUnitPartPriceByRoleName end ------------------------------------");
        return canEditAttrList;
    }

    /**
    *
    *@description 获取当前登录人可以编辑的初始成本属性
    *@param roleList
	*@param isBuyTable
	*@param isMakeTable
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2026/2/5 10:30
    */
    public static StringList getCanEditUnitPartPriceByRoleNameList(MapList roleList, boolean isBuyTable, boolean isMakeTable)  {
        StringList res = new StringList();
        for (int i = 0; i < roleList.size(); i++) {
            Map roleMap = (Map)roleList.get(i);
            String strRoleName = (String)roleMap.get("role");
            StringList editAttrNameList = getCanEditUnitPartPriceByRoleName(strRoleName, isBuyTable, isMakeTable);
            res.addAll(editAttrNameList);
        }
        return res ;
    }


    /**
     *
     *@description 初始化零件表格属性值
     *@param context
     *@param sourceMap
     *@param canEditAttrList
     *@param roleList
     *@param isZeroPart
     *@return java.util.Map
     *@throws Exception
     *@author CHENYAN
     *@date 2026/1/15 18:00
     */
    public static Map initPartTableAttrValue(Context context, Map sourceMap, StringList canEditAttrList, MapList roleList,boolean isZeroPart) throws Exception {
        Set entrySet = sourceMap.entrySet();
        HashMap resMap = new HashMap<>();
        Map canEditMap = new HashMap<>();
        boolean canEdit = true;
        JF_LOGGER.info("canEditAttrList:{}",canEditAttrList);
        String strPartType = (String) sourceMap.get(SELECT_ATTR_JF_PartType);
        String strProcurementType = (String) sourceMap.get(SELECT_ATTR_JF_ProcurementType);
        resMap.put("partType", strPartType);
        //同步属性
        for (Object oEntry : entrySet) {
            Map.Entry entry = (Map.Entry) oEntry;
            String strKey = (String) entry.getKey();
            String strValue = (String) entry.getValue();
            //特殊处理
            if (SELECT_ATTR_JFConnectECR.equals(strKey)) {
                String strECRName = EMPTY_STRING;
                if (UIUtil.isNotNullAndNotEmpty(strValue)){
                    DomainObject ecr = DomainObject.newInstance(context, strValue);
                    strECRName = ecr.getInfo(context, SELECT_NAME);
                }
                resMap.put("ECRName", strECRName);
            } else if (STRING_SELECT_CONNECT_PROJECT_1.equals(strKey)) {
                resMap.put("project", strValue);
            } else if (SELECT_ATTR_V_PART_NUMBER.equals(strKey)) {
                resMap.put("partNumber", strValue);
            }
            if (strKey.startsWith(ATTR_PREFIX) && strKey.endsWith(ATTR_SUFFIX)) {
                String strKeyTemp = replaceAttrPreAndSub(strKey);
                strKey = replaceAttrPreAndSubAndPackage(strKey);
                //还需要判断角色和属性是否匹配 采购对应Buy ICO 对应ICO
                if (Objects.nonNull(canEditAttrList) && canEditAttrList.contains(strKeyTemp)) {
                    if (checkRoleAndPartType(roleList,strPartType,isZeroPart,strProcurementType)) {
                        canEditMap.put(strKey, canEdit);
                    }
                }
            }
            if (UIUtil.isNullOrEmpty(strValue)) {
                strValue = EMPTY_STRING;
            }
            resMap.put(strKey, strValue);
        }
        //标识可以编辑的属性
        resMap.put(STRING_ISEDIT, canEditMap);
        return resMap;
    }
    /**
     * @param strFullAttrName
     * @return java.lang.String
     * @throws
     * @description 去除属性前缀和后缀
     * @author CHENYAN
     * @date 2026/1/13 12:33
     */
    public static String replaceAttrPreAndSub(String strFullAttrName) {
        return strFullAttrName.replaceAll("attribute\\[|\\]", "");
    }

    /**
     * @param strFullAttrName
     * @return java.lang.String
     * @throws
     * @description
     * @author CHENYAN
     * @date 2026/1/13 12:33
     */
    public static String replaceAttrPreAndSubAndPackage(String strFullAttrName) {
        return strFullAttrName.replaceAll("attribute\\[|\\]|JF_VPMReferenceCost.", "");
    }
    /**
     *
     *@description 补全属性的包名
     *@param strAttrName
     *@return java.lang.String
     *@throws
     *@author CHENYAN
     *@date 2026/1/16 14:24
     */
    public static String complementPackageNameByAttrName(String strAttrName){
        if (UIUtil.isNotNullAndNotEmpty(strAttrName)){
            return JF_PublicMethodClass_mxJPO.buildStringInStrings(STRING_TXO_PRICE_PACKAGE_NAME, strAttrName);
        }
        return strAttrName;
    }
    public static String complementSelectAttrNameByAttrName(String strAttrName){
        if (UIUtil.isNotNullAndNotEmpty(strAttrName)){
            return JF_PublicMethodClass_mxJPO.buildStringInStrings(ATTR_PREFIX, strAttrName,ATTR_SUFFIX);
        }
        return strAttrName;
    }
    /**
     *
     *@description 根据表格类型和行编辑标识获取可编辑的角色列表
     *@param isBuyTable 是否采购件表格
     *@param isMakeTable 是否自制件表格
     *@param strRowEditFlag 行编辑标识 "1","2" 1标识自制件里面的角色 2标识采购件里面的采购代表和costing角色
     *@return StringList 可编辑的角色名称列表（strRoleName的值），如果不可编辑则返回空列表
     *@throws
     *@author CHENYAN
     *@date 2026/1/19
     */
    public static StringList getEditableRolesByRowEditFlag(boolean isBuyTable, boolean isMakeTable, String strRowEditFlag) {
        StringList editableRoles = new StringList();

        // 根据表格类型返回对应的角色列表
        if (isBuyTable) {
            // 采购件表格可编辑的角色
            editableRoles.add(ATTR_PROJECT_ROLE_Range_PRR);              // 采购代表
            editableRoles.add(ATTR_PROJECT_ROLE_Range_InternalSupplier); // 内部供应商
            editableRoles.add(ATTR_PROJECT_ROLE_Range_Costing);          // 成本
            // 可根据需要添加其他采购件相关的角色
        } else if (isMakeTable) {
            // 自制件表格可编辑的角色
            if ("1".equals(strRowEditFlag)) {
                editableRoles.add(ATTR_PROJECT_ROLE_Range_FinancialBP);      // 财务BP
                editableRoles.add(ATTR_PROJECT_ROLE_Range_AME);              // 整椅 AME
                editableRoles.add(ATTR_PROJECT_ROLE_Range_Foam_AME);         // 发泡 AME
                editableRoles.add(ATTR_PROJECT_ROLE_Range_Trim_AME);        // 面套 AME
                editableRoles.add(ATTR_PROJECT_ROLE_Range_Costing);          // 成本
            } else if ("2".equals(strRowEditFlag)) {
                editableRoles.add(ATTR_PROJECT_ROLE_Range_PRR);              // 采购代表
                editableRoles.add(ATTR_PROJECT_ROLE_Range_Costing);          // 成本
            }
        }
        return editableRoles;
    }
    /**
     *
     *@description 判断零件是否是指定项目的供货件
     *@param context
     *@param strPartId
     *@param strProjectId
     *@return boolean
     *@throws
     *@author CHENYAN
     *@date 2025/8/25 16:00
     */

    public static boolean checkPartIsZeroPart(Context context,String strPartId ,String strProjectId) throws Exception{
        String strSelectZeroPart = "print bus %s select to[JFProject2RootPart|from.id=='%s'&&attribute[JFZeroPart]=='Y'] dump";

        String strIsZeroPart = String.format(strSelectZeroPart, strPartId,strProjectId);
        //判断零件是否是指定项目供货件
        String strIsZeroPartRes = MqlUtil.mqlCommand(context, Boolean.FALSE, strIsZeroPart, Boolean.TRUE);
        strIsZeroPartRes = strIsZeroPartRes.trim();
        return "TRUE".equalsIgnoreCase(strIsZeroPartRes);
    }

    /**
     *
     *@description 获取表格的显示价格属性
     *@param typeSelectList  select 属性集合
     *@param isBuyTable 是否是采购表格
     *@param isMakeTable 是否是自制表格
     *@return matrix.util.StringList
     *@throws
     *@author CHENYAN
     *@date 2026/1/15 17:29
     */
    public static void getPriceAttrListByTable(StringList typeSelectList,boolean isBuyTable ,boolean isMakeTable) {
        if (isBuyTable){
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMold);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCost);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldCost);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldPriceCostExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHours);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHoursExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersInternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersExternal);
        }else if (isMakeTable){
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeManHour);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeManHourExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeSeatCost);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeTargetPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeSeatCostExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPriceExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMold);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldCost);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldPriceCostExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCost);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal);
        }
    }

    /**
     *
     *@description 获取可编辑属性列表（包含ECR状态和行编辑权限角色集合控制）
     *@param isBuyTable 是否采购件表格
     *@param isMakeTable 是否自制件表格
     *@param roleList 当前用户的角色列表
     *@param strProcurementType 采购类型
     *@param strPartType 零件类型
     *@param strDirectBuy 是否直接购买
     *@param strChangeSource 变更来源
     *@param strECRCurrent ECR的当前状态（如Create、Review、Complete等）
     *@param editableRoles 有行编辑权限的角色名称列表（StringList，存的是strRoleName的值，可为null或空表示不限制）
     *@return StringList 所有角色可编辑的属性列表（并集）
     *@return isZeroPart 是否是供货件
     *@throws
     *@author CHENYAN
     *@date 2026/1/19
     */
    public static StringList getRoleEditableFieldsList(boolean isBuyTable, boolean isMakeTable, MapList roleList,
                                                       String strProcurementType, String strPartType, String strDirectBuy, String strChangeSource,
                                                       String strECRCurrent, StringList editableRoles,boolean isZeroPart,boolean isVerifyUnitPrice) {

        StringList allRoleEditAttrList = new StringList();

        for (int i = 0; i < roleList.size(); i++) {
            Map roleMap = (Map) roleList.get(i);
            String strRoleName = (String) roleMap.get("role");
            String strTaskCurrent = (String) roleMap.get(SELECT_CURRENT);

            // 会签任务状态为审核和已完成直接跳过
            // Only Active sign tasks can edit.
            if (!"Active".equals(strTaskCurrent)) {
                continue;
            }
            // 检查角色是否在有行编辑权限的角色集合中
            if (Objects.isNull(editableRoles) || !editableRoles.contains(strRoleName)) {
                continue;
            }
            //ECR 在会签状态下角色不为APR可以编辑
            // APR reuses Countersign role-based editable fields.
            if (("Countersign".equals(strECRCurrent) || "APR".equals(strECRCurrent)) && (!ATTR_PROJECT_ROLE_Range_FinancialBP.equals(strRoleName))){
                // 获取当前角色可编辑的属性
                StringList roleEditAttrList = getEditableFieldsByRole(
                        isBuyTable, isMakeTable, strRoleName,
                        strProcurementType, strPartType, strDirectBuy, strChangeSource,isZeroPart
                );
                // 将该角色的可编辑属性添加到总列表
                allRoleEditAttrList.addAll(roleEditAttrList);
            }else if ("APR".equals(strECRCurrent) && isZeroPart){
                if (ATTR_PROJECT_ROLE_Range_FinancialBP.equals(strRoleName)){
                    // 获取当前角色可编辑的属性
                    StringList roleEditAttrList = getEditableFieldsByRole(
                            isBuyTable, isMakeTable, strRoleName,
                            strProcurementType, strPartType, strDirectBuy, strChangeSource,isZeroPart
                    );
                    // 将该角色的可编辑属性添加到总列表
                    allRoleEditAttrList.addAll(roleEditAttrList);
                }
            }
            //添加单件成本
            if (isVerifyUnitPrice){
                // 单件成本对应的角色编辑属性
//                StringList unitEditAttrList =  JF_FormalECRStaticMethod_mxJPO.getCanEditUnitPartPriceByRoleName(strRoleName, isBuyTable, isMakeTable);
//                allRoleEditAttrList.addAll(unitEditAttrList);
            }
        }

        return allRoleEditAttrList;
    }
    /**
     * 根据角色和条件获取可编辑字段列表
     * @param isBuyTable 是否采购件表
     * @param isMakeTable 是否自制件表
     * @param strRoleName 角色名称
     * @param strProcurementType 采购类型
     * @param strPartType 零件类型
     * @param strDirectBuy DirectBuy
     * @param strChangeSource 变更来源
     * @return 可编辑字段列表
     * @author CHENYAN
     * @date 2026/1/14
     */
    public static StringList getEditableFieldsByRole(boolean isBuyTable,boolean isMakeTable, String strRoleName,
                                                     String strProcurementType, String strPartType, String strDirectBuy, String strChangeSource,boolean isZeroPart) {
        JF_LOGGER.info("------------------------------------------getEditableFieldsByRole begin ----------------------------------------------------------");
        JF_LOGGER.info("getEditableFieldsByRole - isBuyTable:{}, isMakeTable:{}, strRoleName:{}, strProcurementType:{}, strPartType:{}, strDirectBuy:{}, strChangeSource:{}",
                isBuyTable, isMakeTable, strRoleName, strProcurementType, strPartType, strDirectBuy, strChangeSource);
        StringList roleEditAttrList ;
        switch (strRoleName) {
            case ATTR_PROJECT_ROLE_Range_FinancialBP:
            {
                roleEditAttrList =  getFinancialBPEditableFields(isBuyTable, isMakeTable, strChangeSource, strProcurementType,strPartType);
                break;
            }
            case ATTR_PROJECT_ROLE_Range_AME:
            {
                roleEditAttrList = getAMEEditableFields(isBuyTable, isMakeTable, strChangeSource, strProcurementType, strPartType,isZeroPart);
                break;
            }
            case ATTR_PROJECT_ROLE_Range_Foam_AME:
            {
                roleEditAttrList = getFoamAMEEditableFields(isBuyTable, isMakeTable, strChangeSource, strProcurementType, strPartType);
                break;
            }
            case ATTR_PROJECT_ROLE_Range_Trim_AME:
            {
                roleEditAttrList = getTrimAMEEditableFields(isBuyTable, isMakeTable, strChangeSource, strProcurementType,strPartType);
                break;
            }
            case ATTR_PROJECT_ROLE_Range_Costing:
            {
                roleEditAttrList = getCostingEditableFields(isBuyTable, isMakeTable, strChangeSource, strProcurementType,strPartType,isZeroPart);
                break;
            }
            case ATTR_PROJECT_ROLE_Range_PRR:
            {
                roleEditAttrList = getPRREditableFields(isBuyTable, isMakeTable, strChangeSource, strProcurementType,strPartType,strDirectBuy,isZeroPart);
                break;
            }
            case ATTR_PROJECT_ROLE_Range_InternalSupplier:
            {
                roleEditAttrList = getInternalSupplierEditableFields(isBuyTable, isMakeTable, strChangeSource, strProcurementType, strPartType, strDirectBuy);
                break;
            }
            default:
            {
                roleEditAttrList = new StringList();
                break;
            }
        }
        JF_LOGGER.info("roleEditAttrList:{}", roleEditAttrList);
        JF_LOGGER.info("------------------------------------------getEditableFieldsByRole end ----------------------------------------------------------");
        return roleEditAttrList;
    }

    /**
     *
     *@description 财务BP编辑属性
     *@param isBuyTable
     *@param isMakePart
     *@param strChangeSource
     *@param strProcurementType
     *@param strPartType
     *@return matrix.util.StringList
     *@throws
     *@author CHENYAN
     *@date 2026/1/15 16:29
     */
    private static StringList getFinancialBPEditableFields(boolean isBuyTable, boolean isMakePart, String strChangeSource, String strProcurementType, String strPartType) {

        StringList fields = new StringList();

        // 财务BP只能编辑自制件
        if (!isMakePart) {
            return fields;
        }
        switch (strChangeSource) {
            case ATTR_JFCHANGESOURCE_RANGE_InternalChanges:
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeSeatCost);
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeTargetPrice);
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPrice);
                break;

            case ATTR_JFCHANGESOURCE_RANGE_ExternalChanges:
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeSeatCostExternal);
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeTargetPrice);
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPriceExternal);

                break;

            case ATTR_JFCHANGESOURCE_RANGE_BOTH:
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeSeatCost);
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeSeatCostExternal);
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeTargetPrice);
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPrice);
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPriceExternal);

                break;
            default:
                break;
        }
        return fields;
    }


    /**
     *
     *@description 整椅AME的可编辑字段 非面套、发泡零级件
     *@param isBuyTable
     *@param isMakePart
     *@param strChangeSource
     *@param strProcurementType
     *@param strPartType
     *@return matrix.util.StringList
     *@throws
     *@author CHENYAN
     *@date 2026/1/15 15:11
     */
    private static StringList getAMEEditableFields(boolean isBuyTable, boolean isMakePart, String strChangeSource,String strProcurementType, String strPartType,boolean isZeroPart) {
        StringList fields = new StringList();
        // 只能编辑零件类型是零件自制件
        if (isMakePart && isZeroPart) {
            fields = getAMECommonFields(strChangeSource);
        }
        return fields;
    }

    /**
     *
     *@description 发泡AME的可编辑字段
     *@param isBuyTable
     *@param isMakePart
     *@param strChangeSource
     *@param strProcurementType
     *@param strPartType
     *@return matrix.util.StringList
     *@throws
     *@author CHENYAN
     *@date 2026/1/15 16:32
     */
    private static StringList getFoamAMEEditableFields(boolean isBuyTable, boolean isMakePart, String strChangeSource,String strProcurementType, String strPartType) {
        StringList fields = new StringList();
        // 只能编辑自制件
        if (isMakePart && ATTR_JFPartType_RANGE_U.equals(strPartType)) {
            fields = getAMECommonFields(strChangeSource);
        }
        return fields;
    }

    /**
     *
     *@description 面套AME的可编辑字段
     *@param isBuyTable
     *@param isMakePart
     *@param strChangeSource
     *@param strProcurementType
     *@param strPartType
     *@return matrix.util.StringList
     *@throws
     *@author CHENYAN
     *@date 2026/1/15 16:32
     */
    private static StringList getTrimAMEEditableFields(boolean isBuyTable, boolean isMakePart, String strChangeSource,String strProcurementType, String strPartType) {
        StringList fields = new StringList();
        // 只能编辑自制件
        if (isMakePart && ATTR_JFPartType_RANGE_T.equals(strPartType)) {
            fields = getAMECommonFields(strChangeSource);

        }
        return fields;
    }

    /**
     *
     *@description AME类角色的公共字段（整椅/发泡/面套共用）
     *@param strChangeSource
     *@return matrix.util.StringList
     *@throws
     *@author CHENYAN
     *@date 2026/1/15 16:32
     */

    private static StringList getAMECommonFields(String strChangeSource) {
        StringList fields = new StringList();
        switch (strChangeSource) {
            case ATTR_JFCHANGESOURCE_RANGE_InternalChanges:
            {
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeManHour);
                break;
            }

            case ATTR_JFCHANGESOURCE_RANGE_ExternalChanges:
            {
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeManHourExternal);
                break;
            }

            case ATTR_JFCHANGESOURCE_RANGE_BOTH:
            {
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeManHour);
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeManHourExternal);
                break;
            }
            default:{

            }
        }
        return fields;
    }

    /**
     *
     *@description Costing角色编辑属性
     *@param isBuyTable
     *@param isMakePart
     *@param strChangeSource
     *@param strProcurementType
     *@param strPartType
     *@return matrix.util.StringList
     *@throws
     *@author CHENYAN
     *@date 2026/1/15 16:33
     */
    private static StringList getCostingEditableFields(boolean isBuyTable, boolean isMakePart, String strChangeSource,String strProcurementType, String strPartType,boolean isZeroPart) {

        StringList fields = new StringList();
        // 只能编辑采购件
        if (isBuyTable && ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType)) {
            switch (strChangeSource) {
                case ATTR_JFCHANGESOURCE_RANGE_InternalChanges:
                {
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCost);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMoldCost);
                    break;
                }
                case ATTR_JFCHANGESOURCE_RANGE_ExternalChanges:
                {
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMoldPriceCostExternal);
                    break;
                }
                case ATTR_JFCHANGESOURCE_RANGE_BOTH:
                {
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCost);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMoldCost);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMoldPriceCostExternal);
                    break;
                }
                default:{

                }

            }
            return fields;
        }else if (isMakePart && (!isZeroPart)){
            switch (strChangeSource) {
                case ATTR_JFCHANGESOURCE_RANGE_InternalChanges:
                {
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCost);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMoldCost);
                    break;
                }
                case ATTR_JFCHANGESOURCE_RANGE_ExternalChanges:
                {
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMoldPriceCostExternal);
                    break;
                }
                case ATTR_JFCHANGESOURCE_RANGE_BOTH:
                {
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCost);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMoldCost);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMoldPriceCostExternal);

                    break;
                }
                default:{

                }
            }
        }
        return fields;
    }

    /**
     * 采购代表(PRR)的可编辑字段
     */
    private static StringList getPRREditableFields(boolean isBuyTable, boolean isMakePart, String strChangeSource,String strProcurementType, String strPartType,String strDirectBuy,boolean isZeroPart) {

        StringList fields = new StringList();

        if (isBuyTable) {
            // 采购件表逻辑
            fields.addAll(getPRRBuyTableFields(strProcurementType, strDirectBuy, strChangeSource));
        } else if (isMakePart){
            // 自制件表逻辑
            fields.addAll(getPRRMakeTableFields(strChangeSource,isZeroPart));
        }

        return fields;
    }

    /***
     *
     *@description 采购角色可以编辑的字段
     *@param strProcurementType
     *@param strDirectBuy
     *@param strChangeSource
     *@return matrix.util.StringList
     *@throws
     *@author CHENYAN
     *@date 2026/1/15 11:20
     */
    private static StringList getPRRBuyTableFields(String strProcurementType, String strDirectBuy,
                                                   String strChangeSource) {

        StringList fields = new StringList();
        //采购编辑属性的判断 N+Buy 或者 ((Y + Buy || Y + ICO)|| (consignment + Buy || consignment + ICO)
        boolean isDirectBuyN = ATTR_ATTR_JFDIRECT_BUY_RANGE_N.equals(strDirectBuy)
                && ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType);
        boolean isDirectBuyY = (ATTR_ATTR_JFDIRECT_BUY_RANGE_Y.equals(strDirectBuy)
                || ATTR_ATTR_JFDIRECT_BUY_RANGE_consignment.equals(strDirectBuy))
                && (ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType)
                || ATTR_JF_ProcurementType_RANGE_ICO.equals(strProcurementType));

        // Buy件才采购填写物流费 ICO件的物流费由ICO填写
        if(ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType)){
            fields.add(ATTR_JF_LogisticsFees);
        }
        if (isDirectBuyN || isDirectBuyY) {
            switch (strChangeSource) {
                case ATTR_JFCHANGESOURCE_RANGE_InternalChanges:
                {
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMold);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersInternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHours);
                    break;
                }

                case ATTR_JFCHANGESOURCE_RANGE_ExternalChanges:
                {
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHoursExternal);
                    break;
                }

                case ATTR_JFCHANGESOURCE_RANGE_BOTH:
                {
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMold);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersInternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHours);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHoursExternal);
                    break;
                }
                default:
                {
                    break;
                }
            }
        }

        return fields;
    }

    /***
     *
     *@description 采购代表在自制件表的可编辑字段
     *@param strChangeSource
     *@return matrix.util.StringList
     *@throws
     *@author CHENYAN
     *@date 2026/1/15 11:20
     */
    private static StringList getPRRMakeTableFields(String strChangeSource,boolean isZeroPart) {
        StringList fields = new StringList();
        //供货件不能编辑
        if (isZeroPart){
            return fields;
        }
        switch (strChangeSource) {
            case ATTR_JFCHANGESOURCE_RANGE_InternalChanges:
            {
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMold);
                break;
            }

            case ATTR_JFCHANGESOURCE_RANGE_ExternalChanges:
            {
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal);
                break;
            }

            case ATTR_JFCHANGESOURCE_RANGE_BOTH:
            {
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMold);
                fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal);
                break;
            } default:{
                break;
            }
        }
        return fields;
    }

    /***
     *
     *@description 内部供应商在自制件表的可编辑字段
     *@param strChangeSource
     *@return matrix.util.StringList
     *@throws
     *@author CHENYAN
     *@date 2026/1/15 11:20
     */
    private static StringList getInternalSupplierEditableFields(boolean isBuyTable, boolean isMakePart, String strChangeSource,String strProcurementType, String strPartType,String strDirectBuy) {

        StringList fields = new StringList();
        if (isBuyTable && ATTR_JF_ProcurementType_RANGE_ICO.equals(strProcurementType)){
            fields.add(ATTR_JF_LogisticsFees);
        }
        // 只能编辑采购件
        if (isBuyTable && ATTR_JF_ProcurementType_RANGE_ICO.equals(strProcurementType) && ATTR_ATTR_JFDIRECT_BUY_RANGE_N.equals(strDirectBuy)) {
            switch (strChangeSource) {
                case ATTR_JFCHANGESOURCE_RANGE_InternalChanges:
                {
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMold);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersInternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHours);
                    break;
                }
                case ATTR_JFCHANGESOURCE_RANGE_ExternalChanges:
                {
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHoursExternal);
                    break;
                }
                case ATTR_JFCHANGESOURCE_RANGE_BOTH:
                {
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMold);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersInternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHours);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersExternal);
                    fields.add(ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHoursExternal);
                    break;
                }
                default:{

                }
            }
        }
        return fields;
    }
    /**
     *
     *@description 根据角色列表获取可编辑字段列表
     *@param isBuyTable
     *@param isMakeTable
     *@param roleList
     *@param strProcurementType
     *@param strPartType
     *@param strDirectBuy
     *@param strChangeSource
     *@return matrix.util.StringList
     *@throws
     *@author CHENYAN
     *@date 2026/1/16 0:23
     */
    public static StringList getEditableFieldsByRoleList(boolean isBuyTable,boolean isMakeTable, MapList roleList, String strProcurementType, String strPartType, String strDirectBuy, String strChangeSource) {
        StringList allRoleEditAttrList = new StringList();
        for (int i = 0; i < roleList.size(); i++) {
            Map roleMap = (Map) roleList.get(i);
            String strRoleName = (String) roleMap.get("role");
            String strCurrent = (String) roleMap.get(SELECT_CURRENT);
            //会签任务状态为审核和已完成直接跳过
            if ("Review".equals(strCurrent) || "Complete".equals(strCurrent)) {
                continue;
            }
//            StringList roleEditAttrList = JF_FormalECRStaticMethod_mxJPO.getEditableFieldsByRole(isBuyTable, isMakeTable, strRoleName, strProcurementType, strPartType, strDirectBuy, strChangeSource,isZeroPart);
//            allRoleEditAttrList.addAll(roleEditAttrList);
        }
        return allRoleEditAttrList;
    }



    /**
    *
    *@description 根据单件成本的属性名匹配到对应的增量成本的属性名
    *@param strAttrName 单件成本属性名
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2026/1/13 15:39
    */
    public static StringList getChangeAttrNameByUnitCostingAttr(String strAttrName) {
        StringList changeAttrList = new StringList();
        switch (strAttrName) {
            case ATTR_JF_VPMReferenceCost_JF_UnitPrice: {
                changeAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice);
                changeAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceExternal);
                break;
            }
            case ATTR_JF_VPMReferenceCost_JF_MoldPrice: {
                changeAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldCost);
                changeAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldPriceCostExternal);
                break;
            }
            case ATTR_JF_VPMReferenceCost_JF_TrialExpensesPrice: {
                changeAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHours);
                changeAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHoursExternal);
                break;
            }
            case ATTR_JF_VPMReferenceCost_JF_StagnationPrice: {
                changeAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersInternal);
                changeAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersExternal);
                break;
            }
            case ATTR_JF_VPMReferenceCost_JF_CostMoldPrice: {
                changeAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMold);
                changeAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal);
                break;
            }
            case ATTR_JF_VPMReferenceCost_JF_TargetPrice: {
                changeAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeTargetPrice);
                break;
            }
            case ATTR_JF_VPMReferenceCost_JF_Man_hour: {
                changeAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeManHour);
                changeAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeManHourExternal);
                break;
            }
            case ATTR_JF_VPMReferenceCost_JF_UnitPriceCost: {
                changeAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCost);
                changeAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal);
                break;
            }
            default: {
                break;
            }
        }
        return changeAttrList;
    }
    /**
     *
     *@description 构造单件成本价格select
     *@param isBuyTable
     *@param isMakeTable
     *@param typeSelectList
     *@return void
     *@throws
     *@author CHENYAN
     *@date 2026/1/20 16:53
     */
    public static void buildSelectUnitPriceAttr(boolean isBuyTable ,boolean isMakeTable,StringList typeSelectList) {
        if (isBuyTable) {
            //原始成本
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_CostMoldPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_StagnationPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_TrialExpensesPrice);
//            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_MoldPrice);
        }
        if (isMakeTable){
//            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_MoldPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_CostMoldPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_TargetPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_Man_hour);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPrice);
        }
    }
    /**
     * @param strTableName
     * @param typeSelectList
     * @return void
     * @throws
     * @description 构造select 属性集合
     * @author CHENYAN
     * @date 2026/1/13 15:25
     */

    public static void buildSelectAttrByTableName(String strTableName, StringList typeSelectList) {
        if (strTableName.contains("JFFormalECRCosting")) {
            //原始成本
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_CostMoldPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_StagnationPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_TrialExpensesPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_MoldPrice);

            //增量成本
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMold);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersInternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHours);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHoursExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCost);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldCost);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldPriceCostExternal);
        } else if (strTableName.contains("JFFormalECRController")) {
            //原始成本
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_MoldPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_CostMoldPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_TargetPrice);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_Man_hour);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPrice);
            //增量成本
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMold);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeManHour);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeManHourExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCost);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldCost);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldPriceCostExternal);
            typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeTargetPrice);
        }
    }
    /**
     *
     *@description 创建一个bigdecimal对象
     *@param obj
     *@return java.math.BigDecimal
     *@throws
     *@author CHENYAN
     *@date 2026/1/13 16:07
     */
    public static BigDecimal newInstanceBigdecimal(Object obj){
        BigDecimal decimal = new BigDecimal(0.0);
        try {
            if (Objects.nonNull(obj)){
                decimal = new BigDecimal(obj.toString());
            }
        }catch (Exception e){
//            JF_LOGGER.error("e:{}",e.getMessage());
        }
        return decimal;
    }

    /**
    *
    *@description 通过变更成本匹配到原生成本
    *@param strChangeAttrName
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2026/1/22 12:42
    */
    public static  String findOriginalAttrPriceNameByChangeAttrName(String strChangeAttrName) {
        String strOriginalAttrName = "";
        switch (strChangeAttrName) {
            case ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice,ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceExternal: {
                strOriginalAttrName = ATTR_JF_VPMReferenceCost_JF_UnitPrice;
                break;
            }
            case ATTR_JF_VPMReferenceCost_JF_ChangeMold,ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal: {
                strOriginalAttrName = ATTR_JF_VPMReferenceCost_JF_CostMoldPrice;
                break;
            }
            case ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersInternal,ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersExternal: {
                strOriginalAttrName = ATTR_JF_VPMReferenceCost_JF_StagnationPrice;
                break;
            }
            case ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHours,ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHoursExternal: {
                strOriginalAttrName = ATTR_JF_VPMReferenceCost_JF_TrialExpensesPrice;
                break;
            }

            case ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCost,ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal: {
                strOriginalAttrName = ATTR_JF_VPMReferenceCost_JF_UnitPriceCost;
                break;
            }
            case ATTR_JF_VPMReferenceCost_JF_ChangeMoldCost,ATTR_JF_VPMReferenceCost_JF_ChangeMoldPriceCostExternal: {
                strOriginalAttrName = ATTR_JF_VPMReferenceCost_JF_MoldPrice;
                break;
            }
            case ATTR_JF_VPMReferenceCost_JF_ChangeTargetPrice: {
                strOriginalAttrName = ATTR_JF_VPMReferenceCost_JF_TargetPrice;
                break;
            }
            case ATTR_JF_VPMReferenceCost_JF_ChangeManHour,ATTR_JF_VPMReferenceCost_JF_ChangeManHourExternal: {
                strOriginalAttrName = ATTR_JF_VPMReferenceCost_JF_Man_hour;
                break;
            }
            default:
        }
        return strOriginalAttrName;
    }

    /**
    *
    *@description 构造打开模态框函数
    *@param strBeforePrice
	*@param strBOMBeforeQuantity
	*@param strCurrentPrice
	*@param strCurrentQuantity
	*@param strPartId
	*@param strTableName
	*@param strPartRelId
	*@param strBomPrice
	*@param styleCss
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2026/1/26 9:58
    */
    public static String buildOpenModelHtmlByPartMap(String strBeforePrice ,String strBOMBeforeQuantity
            ,String strCurrentPrice ,String strCurrentQuantity,String strPartId,String strTableName , String strPartRelId,String strBomPrice,String styleCss,String strPartRevision){
        StringBuilder sbLinkHtmlBuild = new StringBuilder();
        sbLinkHtmlBuild.append("<a ");
        sbLinkHtmlBuild.append("beforeprice='");
        sbLinkHtmlBuild.append(strBeforePrice);
        sbLinkHtmlBuild.append("' ");
        sbLinkHtmlBuild.append("beforepricequantity='");
        sbLinkHtmlBuild.append(strBOMBeforeQuantity);
        sbLinkHtmlBuild.append("' ");
        sbLinkHtmlBuild.append("currentprice='");
        sbLinkHtmlBuild.append(strCurrentPrice);
        sbLinkHtmlBuild.append("' ");
        sbLinkHtmlBuild.append("currentpricequantity='");
        sbLinkHtmlBuild.append(strCurrentQuantity);
        sbLinkHtmlBuild.append("' ");
        sbLinkHtmlBuild.append("isfirstrevision='");
        //是否是首版发布
        boolean isFirstRevision = checkPartIsFirstReleaseRevision(strPartRevision);
        sbLinkHtmlBuild.append(isFirstRevision);
        sbLinkHtmlBuild.append("' ");
        sbLinkHtmlBuild.append("class=\"object\" style=\"text-decoration: none;\" href=\"JavaScript:openVueModel('");
        sbLinkHtmlBuild.append(strPartId);
        sbLinkHtmlBuild.append("','");
        sbLinkHtmlBuild.append(strTableName);
        //                sbLinkHtmlBuild.append("JFFormalECRController");
        sbLinkHtmlBuild.append("' ");
        sbLinkHtmlBuild.append(",'");
        sbLinkHtmlBuild.append(strPartRelId);
        sbLinkHtmlBuild.append("')\"> <span ");
        if (UIUtil.isNotNullAndNotEmpty(styleCss)){
            sbLinkHtmlBuild.append(styleCss);
        }
        sbLinkHtmlBuild.append(">");
        sbLinkHtmlBuild.append(strBomPrice);
        sbLinkHtmlBuild.append("</span></a>");
        return sbLinkHtmlBuild.toString();
    }

    public static boolean isFreeState(Map partMap){
        boolean isFreeState = false;
        if (partMap.containsKey("freeFlag") && "1".equals(partMap.get("freeFlag"))){
            isFreeState = true ;
        }
        return isFreeState;
    }

    /**
    *
    *@description T
    *@param roleList
	*@param strPartType
	*@param isZeroPart 是否0级件
	*@param strProcurementType
    *@return boolean
    *@throws
    *@author CHENYAN
    *@date 2026/1/30 15:32
    */

    public static boolean checkRoleAndPartType(MapList roleList ,String strPartType,boolean isZeroPart,String strProcurementType){
        boolean isEdit = false;
        for (int i = 0; i < roleList.size(); i++) {
            Map taskMap = (Map) roleList.get(i);
            String strRoleName = (String) taskMap.get("role");
            JF_LOGGER.info("strRoleName:{}",strRoleName);
            JF_LOGGER.info("taskMap:{}",taskMap);
            String strTaskCurrent  = (String) taskMap.get(SELECT_CURRENT);
            if ("Review".equals(strTaskCurrent) || "Complete".equals(strTaskCurrent)) {
                continue;
            }
            if (ATTR_PROJECT_ROLE_Range_PRR.equals(strRoleName) && (ATTR_JF_ProcurementType_RANGE_BUY.equals(strProcurementType) || ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType))){
                isEdit = true;
            } else if (ATTR_PROJECT_ROLE_Range_InternalSupplier.equals(strRoleName) && ATTR_JF_ProcurementType_RANGE_ICO.equals(strProcurementType)) {
                isEdit = true;
            }else if (ATTR_PROJECT_ROLE_Range_AME.equals(strRoleName) && ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType) && isZeroPart){
                isEdit = true;
            }else if (ATTR_PROJECT_ROLE_Range_Foam_AME.equals(strRoleName) && ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType) && ATTR_JFPartType_RANGE_U.equals(strPartType)){
                isEdit = true;
            }
            else if (ATTR_PROJECT_ROLE_Range_Trim_AME.equals(strRoleName) && ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType) && ATTR_JFPartType_RANGE_T.equals(strPartType)){
                isEdit = true;
            }else if (ATTR_PROJECT_ROLE_Range_Costing.equals(strRoleName)){
                isEdit = true;
            }else if (ATTR_PROJECT_ROLE_Range_FinancialBP.equals(strRoleName)){
                isEdit = true;
            }
        }
        JF_LOGGER.info("isEdit:{}",isEdit);
        return isEdit;
    }
    /**
     *
     *@description 获取excel单元格公共方法
     *@param cell
     *@return java.lang.String
     *@throws
     *@author CHENYAN
     *@date 2026/2/6 10:46
     */
    public static String getCellValueAsString(Cell cell)  {
        if (Objects.isNull(cell)) {
            return "";
        }

        // 根据单元格类型进行处理
        switch (cell.getCellType()) {
            case STRING:
                // 字符串类型直接返回
                return cell.getStringCellValue();

            case NUMERIC:
                // 数字类型（包含日期）
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 如果是日期格式，格式化为字符串
                    return DATE_FORMAT.format(cell.getDateCellValue());
                } else {
                    // 普通数字，转换为字符串，防止科学计数法
                    return NUMBER_FORMAT.format(cell.getNumericCellValue());
                }

            case BOOLEAN:
                // 布尔类型
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                // 公式类型：获取计算后的值
                return evaluateFormulaCell(cell);
            case BLANK:
                // 空单元格
                return "";

            default:
                // 未知类型（如错误类型）
                return "";
        }
    }
    private static String evaluateFormulaCell(Cell cell) {
        FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        CellValue cellValue = evaluator.evaluate(cell);

        if (Objects.isNull(cell)) {
            return "";
        }

        // 根据计算结果的类型返回字符串
        switch (cellValue.getCellType()) {
            case STRING:
                return cellValue.getStringValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATE_FORMAT.format(cell.getDateCellValue());
                } else {
                    return NUMBER_FORMAT.format(cellValue.getNumberValue());
                }
            case BOOLEAN:
                return String.valueOf(cellValue.getBooleanValue());
            case ERROR:
                return "";
            default:
                return "";
        }
    }

    public static Map<String,BigDecimal> calcZeroRollupPrice(Map infoMap ){
        JF_LOGGER.info("----------------------------------calcZeroRollupPrice begin -------------------------------------------");
        JF_LOGGER.info("infoMap:{}",infoMap);
        Map<String,BigDecimal> res = new HashMap<>();
        //BOM数量
        String strQuantity = (String) infoMap.get(SELECT_ATTRIBUTE_JF_BOMQuantity);
        //  初始成本
        String strUnitPrice = (String) infoMap.get(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
        //外部单件变更成本
        String strChangeUnitPriceExternal = (String) infoMap.get(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal);
        //采购单件成本
        String strBuyUnitPrice = (String) infoMap.get(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPrice);
        //内部 (采购单件成本)
        String strChangeBuyUnitPrice = (String) infoMap.get(SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice);
        //物流费
        String strLogisticPrice = (String) infoMap.get(JF_FormalECRStaticMethod_mxJPO.STRING_SELECT_JF_LogisticsFees);
        if (UIUtil.isNullOrEmpty(strQuantity) || "0.0".equals(strQuantity)){
            strQuantity = "0" ;
        }
        BigDecimal bQuantity =JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strQuantity) ;
        BigDecimal unitPriceDecimal = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strUnitPrice);
        BigDecimal unitChangePriceDecimal = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strChangeUnitPriceExternal);
        //当前零件的内部卷积价格
        BigDecimal onePartInternalPrice = unitPriceDecimal.subtract(unitChangePriceDecimal).multiply(bQuantity);
        //当前零件的外部卷积价格
        BigDecimal buyUnitPriceDecimal =JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strBuyUnitPrice);
        BigDecimal buyUnitChangePriceDecimal = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strChangeBuyUnitPrice);
        BigDecimal logisticPriceDecimal = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strLogisticPrice);
        BigDecimal onePartExternalPrice = buyUnitPriceDecimal.subtract(buyUnitChangePriceDecimal).add(logisticPriceDecimal).multiply(bQuantity);
        JF_LOGGER.info("onePartInternalPrice:{}",onePartInternalPrice);
        JF_LOGGER.info("onePartExternalPrice:{}",onePartExternalPrice);
        res.put(ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPrice,onePartInternalPrice);
        res.put(ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPriceExternal,onePartExternalPrice);
        JF_LOGGER.info("res:{}",res);
        JF_LOGGER.info("----------------------------------calcZeroRollupPrice end -------------------------------------------");
        return res;
    }

   /* *//**
    *
    *@description 判断零件是否是首版发布 如果有线下数据版本是AB这种就无法判断
    *@param strPartRevision
    *@return boolean
    *@throws
    *@author CHENYAN
    *@date 2026/3/5 15:36
    *//*
    public static boolean checkPartIsFirstRevision(String strPartRevision){
       return checkPartIsFirstReleaseRevision(strPartRevision);
    }*/
    /**
    *
    *@description 判断零件版本是不是首版发布版本
    *@param strPartRevision
    *@return boolean
    *@throws
    *@author CHENYAN
    *@date 2026/3/13 11:05
    */
    public static boolean checkPartIsFirstReleaseRevision(String strPartRevision){
        boolean isFirstRevision = false;
        if (UIUtil.isNotNullAndNotEmpty(strPartRevision) && strPartRevision.matches("AA\\.(?:[1-9]|[1-9]\\d)-000")){
            isFirstRevision = true;
        }
        return isFirstRevision;
    }
}
