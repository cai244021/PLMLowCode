import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Array;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.*;
import java.util.stream.Collectors;

import static com.matrixone.apps.domain.DomainConstants.*;

/**
 * @author CHENYAN
 * @version v1.0.0
 * 创建时间：2026/1/6 11:18
 * @description
 */
public class JF_FormalECRService_mxJPO implements JF_PLMConstants_mxJPO {
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_FormalECRService_mxJPO.class);
    //关联ECR的变更来源
    private static final String STRING_SELECT_JFRELATEITEM_JFCHANGESOURCE = "to[JFRelateItem].attribute[JFChangeSource]";
    private static final String TEMPLATE_EXCEL_PATH = "jf_template/";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // 定义日期格式化器

    /**
    *
    *@description 获取BOM增量成本html列
    *@param context
	*@param args
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2026/1/21 12:16
    */
    public StringList getJFBOMIncrementHtml(Context context, String[] args) throws Exception {
        JF_LOGGER.info("------------------------------------------  getJFBOMIncrementHtml  begin ------------------------------------------");
        Map paramsMap = (Map) JPO.unpackArgs(args);
        Map paramList = (Map) paramsMap.get(STRING_PARAMLIST);
        String strSelectTable = (String) paramList.get(STRING_SELECT_TABLE);
        String strObjectId = (String) paramList.get(STRING_OBJECTID);
        Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);

        String strColName = (String) columnMap.get("name");
        JF_LOGGER.info("strColName:{}", strColName);
        boolean isBuyTable = JF_FormalECRStaticMethod_mxJPO.checkIsBuyTable(strSelectTable);
        boolean isMakeTable = JF_FormalECRStaticMethod_mxJPO.checkIsMakeTable(strSelectTable);
        //id 集合
        StringList strings = new StringList();
        MapList objectList = (MapList) paramsMap.get(STRING_OBJECTLIST);
        //获取登录用户的角色
        MapList roleList = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECR(context, new String[]{strObjectId});
        //初始成本可以编辑的属性
        StringList canEditUnitAttrList = JF_FormalECRStaticMethod_mxJPO.getCanEditUnitPartPriceByRoleNameList(roleList,isBuyTable,isMakeTable);
//        StringList canEditUnitAttrList = JF_FormalECRStaticMethod_mxJPO.getCanEditUnitPartPriceByRoleName(JF_FormalECRStaticMethod_mxJPO.STRING_ROLE_ALL, isBuyTable, isMakeTable);
        try {
            DomainObject part = DomainObject.newInstance(context);
            for (int i = 0; i < objectList.size(); i++) {
                Map infoMap = (Map) objectList.get(i);
                String strPartId = (String) infoMap.get(SELECT_ID);
                String strPartRelId = (String) infoMap.get(SELECT_RELATIONSHIP_ID);
                String strType = (String) infoMap.get(SELECT_TYPE);
                String strLinkHtml =  EMPTY_STRING;
//                String strStyleCss = "style=\"color:red\"";
//                默认黄色
                String strStyleCss = "style=\"color:#ffbf00\"";
                if (TYPE_VPMReference.equals(strType)) {
                    part.setId(strPartId);
                    String strUnitPrice = part.getInfo(context,SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
//                    String strUnitPrice = UIUtil.getValue(infoMap, SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
                    String strBOMQuantity = UIUtil.getValue(infoMap, SELECT_ATTRIBUTE_JF_BOMQuantity);
                    String strBOMBeforeQuantity = UIUtil.getValue(infoMap, SELECT_ATTRIBUTE_JF_BOMBeforeQuantity);
                    String strBOMChangeQuantity = UIUtil.getValue(infoMap, SELECT_ATTRIBUTE_JF_BOMChangeQuantity);
                    String strBOMBeforeRev = UIUtil.getValue(infoMap, SELECT_ATTRIBUTE_JF_ChangeBeforeRev);
                    String strPartRevision = UIUtil.getValue(infoMap, SELECT_REVISION);
                    //样式 该角色能不能编辑增量价格行
                    Boolean rowEdit = infoMap.containsKey(JF_FormalECRStaticMethod_mxJPO.STRING_Row_Edit) ? (Boolean) infoMap.get( JF_FormalECRStaticMethod_mxJPO.STRING_Row_Edit) : Boolean.FALSE;
                    //单件成本为 0 或者空  ||  变更前数量和变更数量不一致 标识为红色 且行可编辑
//                    if (UIUtil.isNotNullAndNotEmpty(strUnitPrice)) {
//                        if (isNonZero(strUnitPrice)) {
//                            if (strBOMQuantity.equals(strBOMBeforeQuantity)) {
//                                strStyleCss = "style=\"color:#ffbf00\"";
//                            }
//                        }
//                    }
                    //如果 行标识存在就设置成红色或蓝色 行能编辑就代表初始成本也能编辑
//                    JF_LOGGER.info("rowEdit:{}",rowEdit);
                    if (rowEdit) {
                        //先默认给红色 后面根据如果初始成本已经维护会设置成蓝色
                         strStyleCss = "style=\"color:red\"";
                        boolean isFirstReleaseRevision = JF_FormalECRStaticMethod_mxJPO.checkPartIsFirstReleaseRevision(strPartRevision);
                        //可以编辑属性的集合
                        //原始成本
                        StringList nativeAttrList = new StringList();
                        Map editAttrMap = (Map) infoMap.get(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT);
                        //首版情况 首版 isEdit 为空 直接拿角色可以编辑的初始成本属性
                        if (editAttrMap.size() == 0 && isFirstReleaseRevision){
                            for (int i1 = 0; i1 < canEditUnitAttrList.size(); i1++) {
                                String strSelectFullAttrName = JF_PublicMethodClass_mxJPO.buildStringInStrings(ATTR_PREFIX, canEditUnitAttrList.get(i1), ATTR_SUFFIX);
                                nativeAttrList.add(strSelectFullAttrName);
                            }
                        }else {
                            //非首版
                            Set editAttrSet = editAttrMap.keySet();
                            for (Object oAttrName : editAttrSet) {
                                String strEditAttrName = (String) oAttrName;
                                String strOriginAttrName = JF_FormalECRStaticMethod_mxJPO.findOriginalAttrPriceNameByChangeAttrName(strEditAttrName);
                                //是初始成本的话不会匹配到，需要校验该属性是否就是初始成本的属性
                                if (UIUtil.isNullOrEmpty(strOriginAttrName)) {
                                    if (canEditUnitAttrList.contains(strEditAttrName)) {
                                        strOriginAttrName = strEditAttrName;
                                    }
                                }
                                String strSelectFullAttrName = JF_PublicMethodClass_mxJPO.buildStringInStrings(ATTR_PREFIX, strOriginAttrName, ATTR_SUFFIX);
                                nativeAttrList.add(strSelectFullAttrName);
                            }
                        }
                        JF_LOGGER.info("nativeAttrList:{}",nativeAttrList);
                        if (nativeAttrList.size() > 0) {
                            Map nativeInfoMap = part.getInfo(context, nativeAttrList);
                            for (Object oEntry : nativeInfoMap.entrySet()) {
                                Map.Entry entry = (Map.Entry) oEntry;
                                String strNativeValue = (String) entry.getValue();
                                if (isNonZero(strNativeValue)) {
                                    strStyleCss = "style=\"color:#368ec4\"";
                                    break;
                                }
                            }
                        }
//                    }
                    }
                    BigDecimal currentPrice = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strUnitPrice);
                    BigDecimal currentQuantity = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strBOMQuantity);
                    BigDecimal bomIncrementPrice = currentPrice.multiply(currentQuantity);
                    //上一个版本的价格
                    String strBeforePrice = EMPTY_STRING;
                    //BOM增量价格
                    if (UIUtil.isNotNullAndNotEmpty(strBOMBeforeRev)){
//                    if (UIUtil.isNotNullAndNotEmpty(strBOMBeforeRev) && UIUtil.isNotNullAndNotEmpty(strBOMChangeQuantity)){
                        DomainObject beforeObj = DomainObject.newInstance(context, strBOMBeforeRev);
                        strBeforePrice = beforeObj.getInfo(context, SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
                        BigDecimal beforePrice = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strBeforePrice);
                        BigDecimal beforeQuantity = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strBOMBeforeQuantity);
                        bomIncrementPrice = bomIncrementPrice.subtract(beforePrice.multiply(beforeQuantity));
                    }
                    String strBomPrice = bomIncrementPrice.toString();

//                    JF_LOGGER.info("bomIncrementPrice:{}",strBomPrice);

                    strLinkHtml = JF_FormalECRStaticMethod_mxJPO.buildOpenModelHtmlByPartMap(strBeforePrice, strBOMBeforeQuantity, strUnitPrice, strBOMQuantity
                            , strPartId, strSelectTable, strPartRelId, strBomPrice, strStyleCss,strPartRevision);

                }

                strings.add(strLinkHtml);
            }
        } catch (Exception e) {
            JF_LOGGER.error("getJFBOMIncrementHtml error:{}",e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        JF_LOGGER.info("------------------------------------------  getJFBOMIncrementHtml  begin ------------------------------------------");
        return strings;
    }

    /**
    *
    *@description 获取零件关联项目物流费
    *@param context
	*@param args
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2026/1/22 10:33
    */
    public StringList getJFLogisticsFeesValue(Context context, String[] args) throws Exception {
        JF_LOGGER.info("------------------------------------------  getJFLogisticsFeesValue  begin ------------------------------------------");
        Map paramsMap = (Map) JPO.unpackArgs(args);
        Map paramList = (Map) paramsMap.get(STRING_PARAMLIST);
        String strTableName = (String) paramList.get(STRING_SELECT_TABLE);
        String strParentOID = (String) paramList.get(STRING_PARENTOID);
        Map columnMap = (Map) paramsMap.get(STRING_COLUMNMAP);
        String strColName = (String) columnMap.get("name");
        JF_LOGGER.info("strColName:{}", strColName);
        MapList objectList = (MapList) paramsMap.get(STRING_OBJECTLIST);
        StringList res = new StringList(objectList.size());
        for (int i = 0; i < objectList.size(); i++) {
            String strValue = EMPTY_STRING;
            Map infoMap = (Map) objectList.get(i);
            String strType = UIUtil.getValue(infoMap, SELECT_TYPE);
            if (TYPE_VPMReference.equals(strType)) {
                strValue = UIUtil.getValue(infoMap, JF_FormalECRStaticMethod_mxJPO.STRING_SELECT_JF_LogisticsFees);
            }
            res.add(strValue);
        }
        JF_LOGGER.info("------------------------------------------  getJFLogisticsFeesValue  end ------------------------------------------");
        return res ;
    }

    public static boolean isNonZero(String strValue) {
        if (UIUtil.isNullOrEmpty(strValue)) {
            return false; // 或抛异常，根据需求
        }
        try {
            // 尝试解析为 double（可兼容整数和浮点数）
            double dValue = Double.parseDouble(strValue.trim());

            // 判断是否为 0.0（包括 -0.0）
            return dValue != 0.0;
        } catch (NumberFormatException e) {
            // 如果不是有效数字，默认视为无效，返回 false
            return false;
        }
    }

    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取采购件清单数据
     * @author CHENYAN
     * @date 2026/1/14 14:44
     */
    public MapList getFormalECRBuyTableData(Context context, String[] args) throws Exception {

        JF_LOGGER.info("----------------------------- getFormalECRBuyTableData begin ------------------------------------------------");
        Map parameters = JPO.unpackArgs(args);
        String strObjectId = (String) parameters.get(STRING_OBJECTID);
        //标识是否是校验调用 ，如果是需要添加单件成本
        String strVerifyUnitPriceFlag = (String) parameters.get(JF_FormalECRStaticMethod_mxJPO.STRING_VERIFY_UNIT_PRICE);
        boolean isVerifyUnitPrice = false;
        if (UIUtil.isNotNullAndNotEmpty(strVerifyUnitPriceFlag) && "true".equals(strVerifyUnitPriceFlag)) {
            isVerifyUnitPrice = true;
        }
        String strSelectTable = (String) parameters.get(STRING_SELECT_TABLE);
        JF_LOGGER.info("strSelectTable:{}",strSelectTable);
        boolean isBuyTable = JF_FormalECRStaticMethod_mxJPO.checkIsBuyTable(strSelectTable);
        boolean isMakeTable = JF_FormalECRStaticMethod_mxJPO.checkIsMakeTable(strSelectTable);
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        StringList ecrSelectList = StringList.create(SELECT_ATTR_JFCHANGESOURCE, JF_FormalECRStaticMethod_mxJPO.STRING_SELECT_JFECRCONECTIONPROJECT, SELECT_CURRENT);
        //ECR变更来源
        Map ecrInfoMap = ecr.getInfo(context,ecrSelectList );
        String strECRChangeSource = UIUtil.getValue(ecrInfoMap,SELECT_ATTR_JFCHANGESOURCE );
        String strECRProject = UIUtil.getValue(ecrInfoMap,JF_FormalECRStaticMethod_mxJPO.STRING_SELECT_JFECRCONECTIONPROJECT );
        String strECRCurrent = UIUtil.getValue(ecrInfoMap,SELECT_CURRENT );
        //获取登录用户的角色
        MapList roleList = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECR(context, new String[]{strObjectId});
        //采购件清单行编辑权限的角色集合
        StringList rowEditRoleList = JF_FormalECRStaticMethod_mxJPO.getEditableRolesByRowEditFlag(isBuyTable, isMakeTable, "");
        JF_LOGGER.info("strECRChangeSource:{}",strECRChangeSource);
        StringList relSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        relSelectList.add(SELECT_FROM_ID);
        relSelectList.add(SELECT_ATTR_JFFREEState);
        relSelectList.add(SELECT_ATTRIBUTE_JF_BOMQuantity);
        relSelectList.add(SELECT_ATTRIBUTE_JF_BOMBeforeQuantity);
        relSelectList.add(SELECT_ATTRIBUTE_JF_BOMChangeQuantity);
        relSelectList.add(SELECT_ATTRIBUTE_JF_ChangeBeforeRev);
        relSelectList.add(SELECT_ATTRIBUTE_JF_BOMChangeDes);
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(JF_PublicMethodClass_mxJPO.buildStringInStrings("to[JFRelateItem|from.id==", strObjectId, "].attribute[JFChangeSource]"));
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFConnectECR);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_isLastVersion);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate);
        typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);

        //添加物流费
        if (UIUtil.isNotNullAndNotEmpty(strECRProject)){
            String strSelectProject = String.format(JF_FormalECRStaticMethod_mxJPO.STRING_SELECT_JF_LogisticsFeesWhere, "from.id==" + strECRProject);
            typeSelectList.add(strSelectProject);
        }
        StringList unitEditAttrNameList = null ;
        //获取价格属性列表
        JF_FormalECRStaticMethod_mxJPO.getPriceAttrListByTable(typeSelectList, isBuyTable, isMakeTable);
        if (isVerifyUnitPrice){
            //初始成本select
            JF_FormalECRStaticMethod_mxJPO.buildSelectUnitPriceAttr(isBuyTable,isMakeTable,typeSelectList);
        }
        //初始成本的当前角色可编辑的属性
        unitEditAttrNameList = JF_FormalECRStaticMethod_mxJPO.getCanEditUnitPartPriceByRoleNameList(roleList,isBuyTable,isMakeTable);

        MapList newECRAffectItemList = null;
        newECRAffectItemList = getNewECRAffectItemTableData(context, ecr, typeSelectList, relSelectList, strObjectId);
        JF_LOGGER.info("newECRAffectItemList:{}",newECRAffectItemList);

// ====================== 修复版：JF_freeState=Y 过滤逻辑（ID匹配+无Lambda报错）START ======================
// 1. 收集所有【非游离状态（≠Y）】的零件 唯一ID (根据你的要求：用ID唯一匹配)
        Set<String> nonFreeIdSet = new HashSet<>();
        for (Object obj : newECRAffectItemList) {
            Map item = (Map) obj;
            String freeState = UIUtil.getValue(item, SELECT_ATTR_JFFREEState);
            // 唯一ID：SELECT_ID （你业务的唯一标识）
            String objId = UIUtil.getValue(item, SELECT_ID);

            if (!"Y".equals(freeState) && UIUtil.isNotNullAndNotEmpty(objId)) {
                nonFreeIdSet.add(objId);
            }
        }

// 2. 标记需要移除的数据：freeState=Y 且 唯一ID已存在非游离版本
        Set<Integer> needRemoveIndex = new HashSet<>();
        for (int i = 0; i < newECRAffectItemList.size(); i++) {
            Map item = (Map) newECRAffectItemList.get(i);
            String freeState = UIUtil.getValue(item, SELECT_ATTR_JFFREEState);
            String objId = UIUtil.getValue(item, SELECT_ID);

            // 过滤规则：freeState=Y + ID在非游离集合中 → 移除整棵子树
            if ("Y".equals(freeState) && UIUtil.isNotNullAndNotEmpty(objId) && nonFreeIdSet.contains(objId)) {
                int subtreeEnd = findEndOfSubtree(newECRAffectItemList, i);
                for (int j = i; j < subtreeEnd; j++) {
                    needRemoveIndex.add(j);
                }
            }
        }

// 3. 【关键修复】弃用Lambda/Stream，改用传统方式倒序移除（彻底解决final报错）
        Set<Integer> removeIndexSet = new HashSet<>();
        removeIndexSet.addAll(needRemoveIndex);

        JF_LOGGER.info("过滤完成，剩余数据量：{}", newECRAffectItemList.size());
// ====================== 修复版：过滤逻辑 END ======================

//        HashSet<Integer> removeIndexSet = new HashSet<>();
        for (int i = 0; i < newECRAffectItemList.size(); ) {
            int nextLevel = findEndOfSubtree(newECRAffectItemList, i); // 找到以i为根的子树结束位置
            // add by chenyan 2025/05/15 设置游离一级件编辑权限标识
            Map itemMap = (Map) newECRAffectItemList.get(i);
            setFreeStateEditFlagByMapList(itemMap, i, nextLevel, newECRAffectItemList, null);
            recursionFilterTree(newECRAffectItemList, i, nextLevel, removeIndexSet);
            i = nextLevel;
        }
        // 转换为 ArrayList
        List<Integer> indexList = new ArrayList<>(removeIndexSet);
        // 倒序排序
        Collections.sort(indexList, Collections.reverseOrder());
        for (Integer index : indexList) {
            newECRAffectItemList.remove(index.intValue());
        }
        //  先确定行能不能编辑 然后再确定列能不能编辑
        for (int i = 0; i < newECRAffectItemList.size(); ) {
            int nextLevel = findEndOfSubtree(newECRAffectItemList, i); // 找到以i为根的子树结束位置
            // add by chenyan 2025/05/15 设置游离一级件编辑权限标识
            Map partMap = (Map) newECRAffectItemList.get(i);
            String strType = (String) partMap.get(SELECT_TYPE);
            String strProcurementType = (String) partMap.get(SELECT_ATTR_JF_ProcurementType);
            // 是否评估
            String strEvaluate = (String) partMap.get(SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate);
            //行可以编辑  不是自制件 + 未评估 + 物理产品 + 不是游离件
            if ((!ATTR_JF_ProcurementType_RANGE_MAKE.equalsIgnoreCase(strProcurementType)) && (ATTR_JF_VPMReferenceCost_JF_Evaluate_RANGE_N.equals(strEvaluate))  && TYPE_VPMReference.equals(strType) && (!JF_FormalECRStaticMethod_mxJPO.isFreeState(partMap))) {
                String strChangeSource = (String)partMap.get(STRING_SELECT_JFRELATEITEM_JFCHANGESOURCE);
//                JF_LOGGER.info("strChangeSource:{}",strChangeSource);
                //设置ECR的变更来源
                if (UIUtil.isNullOrEmpty(strChangeSource)){
                    strChangeSource = strECRChangeSource;
                }
                String strDirectBuy = (String)partMap.get(SELECT_ATTR_JFDIRECT_BUY);
                String strPartType = (String)partMap.get(SELECT_ATTR_JFPartType);
                //获取当前角色可以编辑的属性列
                StringList editAttrNameList = JF_FormalECRStaticMethod_mxJPO.getRoleEditableFieldsList(isBuyTable, isMakeTable, roleList, strProcurementType, strPartType,strDirectBuy ,strChangeSource,strECRCurrent,rowEditRoleList,false,isVerifyUnitPrice);
                //                StringList editAttrNameList = getEditableFieldsByRoleList(isBuyTable, isMakeTable, roleList, strProcurementType, strPartType,strDirectBuy ,strChangeSource);
//                HashMap<Object, Object> editMap = new HashMap<>();
//                for (int i1 = 0; i1 < editAttrNameList.size(); i1++) {
//                    editMap.put(editAttrNameList.get(i1), Boolean.TRUE);
//                }
//                partMap.put("isEdit", editMap);
//                JF_LOGGER.info("editAttrNameList:{}",editAttrNameList);
//                JF_LOGGER.info("unitEditAttrNameList:{}",unitEditAttrNameList);
                //设置可编辑属性Map
                setIsEditMap(partMap, editAttrNameList,unitEditAttrNameList);
                i = nextLevel;
            } else {
                i++;
            }
        }
        //重排结构
        newECRAffectItemList = rearrangeStructureByFreeState(newECRAffectItemList);
        JF_LOGGER.info("----------------------------- getFormalECRBuyTableData end ------------------------------------------------");
        return newECRAffectItemList;
    }

    /**
     * @param context
     * @param args
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 获取采购件清单为评估数据
     * @author CHENYAN
     * @date 2026/2/28 14:44
     */
    public MapList getFormalECRBuyTableNotEvaluateData(Context context, String[] args) throws Exception {
        MapList tableData = getFormalECRBuyTableData(context, args);
        //需要移除的下标
        Set<Integer> removeIndexSet = new HashSet<>();
        List<Integer> indexList = new ArrayList<>();
        //重新过滤掉已评估数据
        for (int i = 0; i < tableData.size();) {
            Map tableMap = (Map) tableData.get(i);
            String strEvaluate = (String) tableMap.get(SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate);
            if (ATTR_JF_VPMReferenceCost_JF_Evaluate_RANGE_Y.equals(strEvaluate)){
                int nextLevel = findEndOfSubtree(tableData, i); // 找到以i为根的子树结束位置
                for (int j = i ; j < nextLevel ; j++) {
                    removeIndexSet.add(j);
                }
                i = nextLevel;
            }else {
                i++;
            }
        }
        indexList.addAll(removeIndexSet);
        // 倒序排序
        Collections.sort(indexList, Collections.reverseOrder());
        for (Integer index : indexList) {
            tableData.remove(index.intValue());
        }
        return tableData;
    }



    /**
     * @param partMap
     * @param editAttrNameList
     * @return void
     * @throws
     * @description 设置编辑权限标识
     * @author CHENYAN
     * @date 2025/4/29 12:12
     */
    public void setIsEditMap(Map partMap, StringList editAttrNameList,StringList unitEditAttrNameList){


        // add by chenyan 新增首版发布的时候 增量价格成本属性不能编辑
        //JF_LOGGER.info("editAttrNameList:{}",editAttrNameList);
        String strPartRevision = (String) partMap.get(SELECT_REVISION);
        boolean isFirstRelease = JF_FormalECRStaticMethod_mxJPO.checkPartIsFirstReleaseRevision(strPartRevision);
        //JF_LOGGER.info("isFirstRelease:{} strPartRevision:{}",isFirstRelease,strPartRevision);
        if (editAttrNameList.size() > 0 ){
            partMap.put(JF_FormalECRStaticMethod_mxJPO.STRING_Row_Edit, Boolean.TRUE);
        }
        if (partMap.containsKey(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT)) {
            HashMap<Object, Object> editMap = (HashMap<Object, Object>) partMap.get(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT);
            if (Objects.isNull(editMap)){
                editMap = new HashMap<>();
                partMap.put(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT, editMap);
            }
            if (!isFirstRelease){
                for (int i1 = 0; i1 < editAttrNameList.size(); i1++) {
                    editMap.put(editAttrNameList.get(i1), Boolean.TRUE);
                }
            }
            // 任务挂起 会签任务提交暂定不校验 2026/03/11
//            if (Objects.nonNull(unitEditAttrNameList)){
//                for (int i1 = 0; i1 < unitEditAttrNameList.size(); i1++) {
//                    editMap.put(unitEditAttrNameList.get(i1), Boolean.TRUE);
//                }
//            }
        }else {
            HashMap<Object, Object> editMap = new HashMap<>();
            if (!isFirstRelease){
                for (int i1 = 0; i1 < editAttrNameList.size(); i1++) {
                    editMap.put(editAttrNameList.get(i1), Boolean.TRUE);
                }
            }
            // 任务挂起 会签任务提交暂定不校验 2026/03/11
//            if (Objects.nonNull(unitEditAttrNameList)){
//                for (int i1 = 0; i1 < unitEditAttrNameList.size(); i1++) {
//                    editMap.put(unitEditAttrNameList.get(i1), Boolean.TRUE);
//                }
//            }
            partMap.put(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT, editMap);
        }
    }

    /**
     * @param newECRAffectItemList 全部数据集合
     * @param startIndex           当前节点下标
     * @param endIndex             结束下标
     * @param removeIndexSet       保存移除下标集合
     * @return java.lang.Boolean
     * @throws
     * @description 递归判断当前节点是否需要过滤
     * @author CHENYAN
     * @date 2025/4/29 12:12
     */
    public Boolean recursionFilterTree(MapList newECRAffectItemList, int startIndex, int endIndex, Set<Integer> removeIndexSet) {
        Boolean isShow = Boolean.FALSE;
        Map partMap = (Map) newECRAffectItemList.get(startIndex);
        String strLevel = (String) partMap.get(SELECT_LEVEL);
        Integer currentLevel = Integer.parseInt(strLevel);
        //叶子结点标识
//            String strLeafNodeFlag = (String) partMap.get("from[JFECRRoot2Item]");
        //已经是叶子结点
//            if ("FALSE".equalsIgnoreCase(strLeafNodeFlag)){
//
//            }

        // 查找直接子节点范围 [startIndex+1, endIndex)
        for (int i = startIndex + 1; i < endIndex; i++) {
            Map childNode = (Map) newECRAffectItemList.get(i);
            Integer childLevel = Integer.parseInt((String) childNode.get(SELECT_LEVEL));

            if (childLevel > currentLevel) {
                // 下一层级 -> 子节点
                boolean childIsShow = recursionFilterTree(newECRAffectItemList, i, endIndex, removeIndexSet);
                if (childIsShow) {
                    isShow = true; // 任意子节点显示，则父节点也要显示
                }
            } else {
                // 当前节点之后没有子节点了，跳出
                break;
            }
        }
        // 如果不是叶子节点，还需要看自己的 ProcurementType
        String strProcurementType = (String) partMap.get(SELECT_ATTR_JF_ProcurementType);
        if (!"make".equalsIgnoreCase(strProcurementType)) {
            isShow = true;
        }
        // 设置 showFlag
        partMap.put("showFlag", isShow);
        // 记录需要移除的下标
        if (!isShow) {
            removeIndexSet.add(startIndex);
        }
        return isShow;
    }

    /**
     * @param itemMap     根节点map
     * @param startIndex  根节点下标
     * @param endIndex    结束下标
     * @param dataMapList
     * @return void
     * @throws
     * @description 设置游离一级件的编辑全为false
     * @author CHENYAN
     * @date 2025/5/15 10:55
     */
    public void setFreeStateEditFlagByMapList(Map itemMap, int startIndex, int endIndex, MapList dataMapList, Set<Integer> filterFreeIndex) {
        String strFreeState = (String) itemMap.get(SELECT_ATTR_JFFREEState);
        String strType = (String) itemMap.get(SELECT_TYPE);
        String strProcurementType = (String) itemMap.get(SELECT_ATTR_JF_ProcurementType);
        String strRelName = (String) itemMap.get("relationship");
//        _logger.info("itemMap:{}",itemMap);
        boolean isFilter = (!"make".equals(strProcurementType)) && TYPE_VPMReference.equals(strType);
        //必须是根节点
        if (REL_JFECRRelateRoot.equals(strRelName) || REL_JFECRRelateRootBubble.equals(strRelName)) {
            if ("Y".equals(strFreeState)) {
                for (; startIndex < endIndex; startIndex++) {
                    Map tempMap = (Map) dataMapList.get(startIndex);
                    tempMap.put("freeFlag", "1");
                    if (isFilter && Objects.nonNull(filterFreeIndex)) {
                        filterFreeIndex.add(startIndex);
                    }
                }
            }
        }
    }

    /**
     * @param context
     * @param newECR         新ECR对象
     * @param typeSelectList
     * @param relSelectList
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description ECR受影响项通用方法
     * @author CHENYAN
     * @date 2025/5/9 10:50
     */
    public MapList getNewECRAffectItemTableData(Context context, DomainObject newECR, StringList typeSelectList, StringList relSelectList, String strECRId) throws Exception {
        String strWhere = "attribute[JF_ECRID]~~*" + strECRId + "*";
        MapList maps = null;
        try {
            ContextUtil.pushContext(context);
            maps = newECR.getRelatedObjects(context, REL_JFECRRelateRoot + "," + REL_JFECRRoot2Item + "," + REL_JFECRRelateRootBubble + "," + REL_JFECRRoot2ItemBubble, // relationship pattern
                    TYPE_VPMReference,                                    // object pattern
                    typeSelectList,                            // object selects
                    relSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 0,                                    // recursion level
                    "",                // object where clause
                    strWhere,
                    (short) 0);
        } finally {
            ContextUtil.popContext(context);
        }

        return maps;
    }

    /**
     * @param partList 零件集合
     * @param startIdx 当前节点下标
     * @return int
     * @throws
     * @description 获取当前节点树的边界 用于减少重复遍历
     * @author CHENYAN
     * @date 2025/4/29 13:09
     */
    public static int findEndOfSubtree(MapList partList, int startIdx) {
        Map partMap = (Map) partList.get(startIdx);
        int level = Integer.parseInt((String) partMap.get(SELECT_LEVEL));
        int endIdx = startIdx + 1;
        int size = partList.size();
        while (endIdx < size) {
            Map endPartMap = (Map) partList.get(endIdx);
            int nextLevel = Integer.parseInt((String) endPartMap.get(SELECT_LEVEL));
            if (nextLevel <= level) {
                break;
            }
            endIdx++;
        }
        return endIdx;
    }

    /**
     * @param dataMapList
     * @return com.matrixone.apps.domain.util.MapList
     * @throws
     * @description 通过游离状态对结构进行冲重排序 非游离状态显示在前
     * @author CHENYAN
     * @date 2025/5/20 14:25
     */

    public MapList rearrangeStructureByFreeState(MapList dataMapList) {
        MapList res = new MapList();
        MapList freeMapList = new MapList();
        for (int i = 0; i < dataMapList.size(); ) {
            Map partMap = (Map) dataMapList.get(i);
            String strType = (String) partMap.get(SELECT_TYPE);
            String strRelName = (String) partMap.get(RELATIONSHIP);
            if (TYPE_VPMReference.equals(strType)) {
                int nextLevel = findEndOfSubtree(dataMapList, i); // 找到以i为根的子树结束位置
                int j = i;
                //游离
                if (partMap.containsKey("freeFlag") && (REL_JFECRRelateRoot.equals(strRelName) || REL_JFECRRelateRootBubble.equals(strRelName))) {
                    for (; j < nextLevel; j++) {
                        Map tmpPartMap = (Map) dataMapList.get(j);
                        freeMapList.add(tmpPartMap);
                    }
                } else {

                    for (; j < nextLevel; j++) {
                        Map tmpPartMap = (Map) dataMapList.get(j);
                        res.add(tmpPartMap);
                    }
                }
                i = nextLevel;
            } else {
                res.add(partMap);
                i++;
            }
        }
        res.addAll(freeMapList);
        //如果重排结构后数量不一致返回原数据
        if (res.size() != dataMapList.size()) {
            res = dataMapList;
        }
        return res;
    }
    /***
     *
     *@description 获取表格可编辑字段
     *@param context
     *@param args
     *@return matrix.util.StringList
     *@throws
     *@author CHENYAN
     *@date 2026/1/15 11:20
     */
    public StringList getJFFormalECRAffectedItemsCostTableEditAccess(Context context, String[] args) throws Exception {
        JF_LOGGER.info("-------------------------------------------- getJFFormalECRAffectedItemsCostTableEditAccess begin -----------------------------------------------------------");
        StringList res = null;
        try {
            res = new StringList();
            Map tableSettingMap = JPO.unpackArgs(args);
            MapList objectList = (MapList) tableSettingMap.get("objectList");
            Map columnMap = (Map) tableSettingMap.get("columnMap");
            String strFieldName = (String) columnMap.get("name");
            String strFullAttrName = strFieldName;
            if (!"JF_LogisticsFees".equals(strFieldName)){
                 strFullAttrName = JF_FormalECRStaticMethod_mxJPO.complementPackageNameByAttrName(strFieldName);
            }
            JF_LOGGER.info("strFullAttrName:{}", strFullAttrName);
            for (int i = 0; i < objectList.size(); i++) {
                String strEdit = Boolean.FALSE.toString();
                Map infoMap = (Map) objectList.get(i);
                Map isEditMap = (Map) infoMap.get(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT);
                if ("JF_LogisticsFees".equals(strFieldName)){
                   JF_LOGGER.info("isEditMap:{}", isEditMap);
                }

                if (Objects.nonNull(isEditMap) && isEditMap.containsKey(strFullAttrName)){
                    Object oEditValue = isEditMap.get(strFullAttrName);
                    if (oEditValue instanceof Boolean){
                        boolean isEdit = (boolean) oEditValue;
                        if (isEdit){
                            strEdit = Boolean.TRUE.toString();
                        }
                    }else if (oEditValue instanceof String){
                        String strEditValue = (String) oEditValue;
                        if (strEditValue.equals(Boolean.TRUE.toString())){
                            strEdit = Boolean.TRUE.toString();
                        }
                    }
                }
                res.add(strEdit);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        JF_LOGGER.info("res:{}", res);
        JF_LOGGER.info("-------------------------------------------- getJFFormalECRAffectedItemsCostTableEditAccess end -----------------------------------------------------------");
        return res;
    }
    /**
    *
    *@description 更新价格
    *@param context
	*@param args
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2026/1/16 17:15
    */
    public void updateFormalPriceField(Context context, String[] args){
        JF_LOGGER.info("-------------------------------------------- updateFormalPriceField begin -----------------------------------------------------------");
        try {
            ContextUtil.pushContext(context);
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map columnMap = (Map) paramsMap.get(JF_PLMConstants_mxJPO.STRING_COLUMNMAP);
            Map requestMap = (Map) paramsMap.get(STRING_REQUESTMAP);
            String strTableName = (String) requestMap.get("selectedTable");
            String strParentOID = (String) requestMap.get(STRING_PARENTOID);
            String strAttrName = (String) columnMap.get(DomainConstants.SELECT_NAME);
            HashMap paramMap = (HashMap) paramsMap.get(JF_PLMConstants_mxJPO.STRING_PARAMMAP);
            String strObjectId = (String) paramMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            String strNewValue = (String) paramMap.get(STRING_NEW_VALUE);
            JF_LOGGER.info("strAttrName:{}", strAttrName);
            JF_LOGGER.info("strNewValue:{}", strNewValue);
            DomainObject part = DomainObject.newInstance(context, strObjectId);
            String firstRevision = "AA.1-000";
            //如果是物流费特殊处理
            if ("JF_LogisticsFees".equals(strAttrName)){
                DomainObject ecr = DomainObject.newInstance(context, strParentOID);
                String strECRProject = ecr.getInfo(context, JF_FormalECRStaticMethod_mxJPO.STRING_SELECT_JFECRCONECTIONPROJECT);
                //getInfo 无法查询带条件的 select  getInfoList 可以 但是返回值是 key = value 的形式 需要自己spilt
                String strSelectProject = String.format(JF_FormalECRStaticMethod_mxJPO.STRING_SELECT_JFPS2VPMRELID_Where, "from.id==" + strECRProject);
                JF_LOGGER.info("strSelectProject:{}", strSelectProject);
                StringList partProjectList = part.getInfoList(context, strSelectProject);
//                String strPartProject = part.getInfoList(context, strSelectProject);
                JF_LOGGER.info("partProjectList:{}", partProjectList);
                if (Objects.nonNull(partProjectList) && partProjectList.size() > 0){
                    String strPartProject = partProjectList.get(0);
                    String[] split = strPartProject.split("=");
                    //防止下标越界
                    String strRelId = split[split.length-1];
                    strRelId = strRelId.trim();
                    JF_LOGGER.info("strRelId:{}", strRelId);
                    DomainRelationship rel ;
                if (UIUtil.isNotNullAndNotEmpty(strRelId)){
                    rel = DomainRelationship.newInstance(context, strRelId);
                }else {
                    DomainObject project = DomainObject.newInstance(context, strECRProject);
                    rel = DomainRelationship.connect(context,project , new RelationshipType(REL_JFPS2VPM), part);
                }
                rel.setAttributeValue(context,ATTR_JF_LogisticsFees,strNewValue);
                }else {
                    //关联物流关系
                    DomainObject project = DomainObject.newInstance(context, strECRProject);
                    DomainRelationship rel = DomainRelationship.connect(context,project , new RelationshipType(REL_JFPS2VPM), part);
                    rel.setAttributeValue(context,ATTR_JF_LogisticsFees,strNewValue);
                }

            }else {
                // 如果属性是单价变更成本需要同步BOM增量成本
                String strFullAttrName =  JF_FormalECRStaticMethod_mxJPO.complementPackageNameByAttrName(strAttrName);
                String strOriginAttrName = JF_FormalECRStaticMethod_mxJPO.findOriginalAttrPriceNameByChangeAttrName(strFullAttrName);
                firstRevision = part.getInfo(context, SELECT_REVISION);
                JF_LOGGER.info("strOriginAttrName:{}", strOriginAttrName);
                part.setAttributeValue(context, strFullAttrName, strNewValue);
                //增量价格变更对应的原始价格也要更新
                if (UIUtil.isNotNullAndNotEmpty(strOriginAttrName)){
                    //带 attribute 的属性名
                    String strSelectFullAttrName = JF_PublicMethodClass_mxJPO.buildStringInStrings(ATTR_PREFIX, strOriginAttrName, ATTR_SUFFIX);
//                    StringList typeSelectList = new StringList();
                    StringList incrementAttrList = JF_FormalECRStaticMethod_mxJPO.getChangeAttrNameByUnitCostingAttr(strOriginAttrName);
                    //是否以评估
                    incrementAttrList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate);
                    //单件成本
//                    incrementAttrList.add(strSelectFullAttrName);
                    BusinessObjectList allRevisionObjList = JF_Util_mxJPO.sortMapListNextRevision(context, new String[]{strObjectId,firstRevision});
                    int allRevisionSize = allRevisionObjList.size();
                    if (Objects.nonNull(allRevisionObjList) && allRevisionSize > 1) {
                        String strBeforeOriginAttrValue = "";
                        for (int i =1; i < allRevisionSize; i++) {
                            //同步其他版本属性的集合
                            Map saveAttrMap = new HashMap<>();
                            BusinessObject obj = allRevisionObjList.get(i);
                            DomainObject domainObj = DomainObject.newInstance(context, obj);
                            Map info = domainObj.getInfo(context, incrementAttrList);
                            JF_LOGGER.info("info:{}", info);
                            String strEvaluatePrice = UIUtil.getValue(info, SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate);
                            //未评估才需要重新计算
                            if (ATTR_JF_VPMReferenceCost_JF_Evaluate_RANGE_N.equals(strEvaluatePrice)){
                                //只需要初始化第一次
                                if (i == 1){
                                    BusinessObject before = allRevisionObjList.get(i-1);
                                    DomainObject beforeObj = DomainObject.newInstance(context, before);
                                    strBeforeOriginAttrValue = beforeObj.getInfo(context, strSelectFullAttrName);
                                }

                                //前一个版本的属性和属性值
                                //当前版本的增量属性
                                JF_LOGGER.info("strBeforeOriginAttrValue:{}",strBeforeOriginAttrValue);
                                BigDecimal beforeDecimal = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strBeforeOriginAttrValue);

                                JF_LOGGER.info("incrementAttrList:{}",incrementAttrList);

                                for (int i1 = 0; i1 < incrementAttrList.size(); i1++) {
                                    String strIncrementAttrName = incrementAttrList.get(i1);
                                    String strIncrementAttrValue = UIUtil.getValue(info, strIncrementAttrName);
                                    if (!(SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate.equals(strIncrementAttrName) || SELECT_ID.equals(strIncrementAttrName) || SELECT_TYPE.equals(strIncrementAttrName))){
                                        if (UIUtil.isNotNullAndNotEmpty(strIncrementAttrValue)){
                                            JF_LOGGER.info("strIncrementAttrValue:{}",strIncrementAttrValue);
                                            beforeDecimal = beforeDecimal.add(JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strIncrementAttrValue));
                                            beforeDecimal = beforeDecimal.setScale(2, RoundingMode.HALF_UP);
                                        }
                                    }

                                }
                                String strBeforeValue = beforeDecimal.toString();
                                JF_LOGGER.info("beforeDecimal:{}",strBeforeValue);
                                saveAttrMap.put(strOriginAttrName, strBeforeValue);
                                domainObj.setAttributeValues(context, saveAttrMap);
                                //设置初始成本以便下一个版本使用
                                strBeforeOriginAttrValue = strBeforeValue;
                            }else {
                                //保存当前版本的单件成本以便下一个版本使用
                                strBeforeOriginAttrValue = domainObj.getInfo(context, strSelectFullAttrName);
//                                strBeforeOriginAttrValue = UIUtil.getValue(info, strSelectFullAttrName);
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            try {
                ContextUtil.popContext(context);
            } catch (FrameworkException e) {
                JF_LOGGER.error("popContext error:{}", e.getMessage());
            }
        }
        JF_LOGGER.info("-------------------------------------------- updateFormalPriceField end -----------------------------------------------------------");
    }

    /**
    *
    *@description 自制件清单数据
    *@param context
	*@param args
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2026/1/21 11:32
    */

    public MapList getFormalECRMakeTableData(Context context, String[] args) throws Exception {
        JF_LOGGER.info("----------------------------- getFormalECRMakeTableData begin ------------------------------------------------");
        Map parameters = JPO.unpackArgs(args);
        JF_LOGGER.info("parameters:{}", parameters);
        String strObjectId = (String) parameters.get(STRING_OBJECTID);
        String strSelectTable = (String) parameters.get(STRING_SELECT_TABLE);
        JF_LOGGER.info("strSelectTable:{}",strSelectTable);

        //标识是否是校验调用 ，如果是需要添加单件成本
        String strVerifyUnitPriceFlag = (String) parameters.get(JF_FormalECRStaticMethod_mxJPO.STRING_VERIFY_UNIT_PRICE);
        boolean isVerifyUnitPrice = false;
        if (UIUtil.isNotNullAndNotEmpty(strVerifyUnitPriceFlag) && "true".equals(strVerifyUnitPriceFlag)) {
            isVerifyUnitPrice = true;
        }
        boolean isBuyTable = JF_FormalECRStaticMethod_mxJPO.checkIsBuyTable(strSelectTable);
        boolean isMakeTable = JF_FormalECRStaticMethod_mxJPO.checkIsMakeTable(strSelectTable);
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        //ECR变更来源
        String strECRChangeSource = ecr.getInfo(context, SELECT_ATTR_JFCHANGESOURCE);
        Map ecrInfoMap = ecr.getInfo(context, StringList.create("from[JFChange2Project].to.id",SELECT_CURRENT));
        String strProjectId = UIUtil.getValue(ecrInfoMap, "from[JFChange2Project].to.id");
        String strCurrent = UIUtil.getValue(ecrInfoMap, SELECT_CURRENT);

        //获取登录用户的角色
        MapList roleList = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECR(context, new String[]{strObjectId});
        JF_LOGGER.info("strECRChangeSource:{}",strECRChangeSource);
        StringList relSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        relSelectList.add(SELECT_FROM_ID);
        relSelectList.add(SELECT_ATTR_JFFREEState);
        relSelectList.add(SELECT_ATTRIBUTE_JF_BOMQuantity);
        relSelectList.add(SELECT_ATTRIBUTE_JF_BOMBeforeQuantity);
        relSelectList.add(SELECT_ATTRIBUTE_JF_BOMChangeQuantity);
        relSelectList.add(SELECT_ATTRIBUTE_JF_ChangeBeforeRev);
        relSelectList.add(SELECT_ATTRIBUTE_JF_BOMChangeDes);
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(JF_PublicMethodClass_mxJPO.buildStringInStrings("to[JFRelateItem|from.id==", strObjectId, "].attribute[JFChangeSource]"));
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_PART_NUMBER);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFDIRECT_BUY);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFPartType);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFConnectECR);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_V_isLastVersion);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_ProcurementType);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameCN);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_PartNameEN);
        typeSelectList.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate);
        typeSelectList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost);
        //添加物流费
        if (UIUtil.isNotNullAndNotEmpty(strProjectId)){
            String strSelectProject = String.format(JF_FormalECRStaticMethod_mxJPO.STRING_SELECT_JF_LogisticsFeesWhere, "from.id==" + strProjectId);
            typeSelectList.add(strSelectProject);
        }
        StringList unitEditAttrNameList = null ;
        //获取价格属性列表
        JF_FormalECRStaticMethod_mxJPO.getPriceAttrListByTable(typeSelectList, isBuyTable, isMakeTable);
        if (isVerifyUnitPrice){
            JF_FormalECRStaticMethod_mxJPO.buildSelectUnitPriceAttr(isBuyTable,isMakeTable,typeSelectList);
        }
        unitEditAttrNameList = JF_FormalECRStaticMethod_mxJPO.getCanEditUnitPartPriceByRoleNameList(roleList,isBuyTable,isMakeTable);

        MapList newECRAffectItemList = null;
        newECRAffectItemList = getNewECRAffectItemTableData(context, ecr, typeSelectList, relSelectList, strObjectId);
        JF_LOGGER.info("newECRAffectItemList:{}",newECRAffectItemList);

        // ====================== 优化版：过滤逻辑 BEGIN ======================
        // 1. 收集所有【非游离状态（≠Y）】的零件 唯一ID
        Set<String> nonFreeIdSet = new HashSet<>();
        for (Object obj : newECRAffectItemList) {
            Map item = (Map) obj;
            String freeState = UIUtil.getValue(item, SELECT_ATTR_JFFREEState);
            String objId = UIUtil.getValue(item, SELECT_ID);

            if (!"Y".equals(freeState) && UIUtil.isNotNullAndNotEmpty(objId)) {
                nonFreeIdSet.add(objId);
            }
        }

        // 2. 标记需要移除的数据：freeState=Y 且 唯一ID已存在非游离版本
        Set<Integer> needRemoveIndex = new HashSet<>();
        for (int i = 0; i < newECRAffectItemList.size(); ) {
            Map item = (Map) newECRAffectItemList.get(i);
            String freeState = UIUtil.getValue(item, SELECT_ATTR_JFFREEState);
            String objId = UIUtil.getValue(item, SELECT_ID);

            // 过滤规则：freeState=Y + ID在非游离集合中 → 移除整棵子树
            if ("Y".equals(freeState) && UIUtil.isNotNullAndNotEmpty(objId) && nonFreeIdSet.contains(objId)) {
                JF_LOGGER.info("需要移除的游离件ID:{}", objId);
                int subtreeEnd = findEndOfSubtree(newECRAffectItemList, i);
                JF_LOGGER.info("子树范围:{} - {}", i, subtreeEnd);
                for (int j = i; j < subtreeEnd; j++) {
                    needRemoveIndex.add(j);
                }
                i = subtreeEnd; // 跳过整个子树
            } else {
                i++;
            }
        }

        // 3. 倒序移除，避免索引混乱
        if (!needRemoveIndex.isEmpty()) {
            List<Integer> indexList = new ArrayList<>(needRemoveIndex);
            Collections.sort(indexList, Collections.reverseOrder());
            JF_LOGGER.info("移除前数据量:{}", newECRAffectItemList.size());
            for (Integer index : indexList) {
                newECRAffectItemList.remove(index.intValue());
            }
            JF_LOGGER.info("移除后数据量:{}", newECRAffectItemList.size());
        }
        // ====================== 优化版：过滤逻辑 END ======================

        HashSet<Integer> filterFreeSet = new HashSet<>();
        for (int i = 0; i < newECRAffectItemList.size();) {
            int nextLevel = findEndOfSubtree(newECRAffectItemList, i); // 找到以i为根的子树结束位置
            // add by chenyan 2025/05/15 设置游离一级件编辑权限标识
            Map itemMap = (Map) newECRAffectItemList.get(i);
            setFreeStateEditFlagByMapList(itemMap,i,nextLevel,newECRAffectItemList,filterFreeSet);
            i = nextLevel;
        }
        // 转换为 ArrayList
        JF_LOGGER.info("filterFreeSet:{}",filterFreeSet);
        List<Integer> indexList = new ArrayList<>(filterFreeSet);
        // 倒序排序
        Collections.sort(indexList, Collections.reverseOrder());
        //移除过滤出来的下标
        for (Integer index : indexList) {
            newECRAffectItemList.remove(index.intValue());
        }
        //make 清单里面的编辑角色
        StringList makeEditRoleList = JF_FormalECRStaticMethod_mxJPO.getEditableRolesByRowEditFlag(isBuyTable, isMakeTable, "1");
        //make 清单里面的采购编辑角色
        StringList buyEditRoleList = JF_FormalECRStaticMethod_mxJPO.getEditableRolesByRowEditFlag(isBuyTable, isMakeTable, "2");

        for (int i = 0; i < newECRAffectItemList.size(); i++) {
            int nextLevel = findEndOfSubtree(newECRAffectItemList, i); // 找到以i为根的子树结束位置
            // add by chenyan 2025/05/15 设置游离一级件编辑权限标识
            Map itemMap = (Map) newECRAffectItemList.get(i);
            String strPartId = UIUtil.getValue(itemMap, SELECT_ID);
            String strPartType = UIUtil.getValue(itemMap, SELECT_ATTR_JFPartType);
            String strRelationship = UIUtil.getValue(itemMap, RELATIONSHIP);
            String strProcurementType = UIUtil.getValue(itemMap, SELECT_ATTR_JF_ProcurementType);
            String strChangeSource = UIUtil.getValue(itemMap, STRING_SELECT_JFRELATEITEM_JFCHANGESOURCE);
            if (UIUtil.isNullOrEmpty(strChangeSource)){
                strChangeSource = strECRChangeSource;
            }
            String strDirectBuy = UIUtil.getValue(itemMap, SELECT_ATTR_JFDIRECT_BUY);
            String strEvaluate = UIUtil.getValue(itemMap, SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate);
            //是否是根节点
            if (isECRRootStructureRelationShipByRelName(strRelationship)){
                /** P950-P1029 逻辑是获取自制件清单中根节点自制角色的编辑权限 begin **/
                //是否游离
                String strFreeFlag = UIUtil.getValue(itemMap, "freeFlag");
                //游离件
                if ("1".equals(strFreeFlag)){
                    //自制件 && 价格为评估
                    if ( ATTR_JF_VPMReferenceCost_JF_Evaluate_RANGE_N.equals(strEvaluate) && ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)){
                        //是否是供货件
                        boolean isZeroPart = JF_FormalECRStaticMethod_mxJPO.checkPartIsZeroPart(context, strPartId, strProjectId);
                        //表示0级件
                        itemMap.put(ATTR_JFPartType_Flag_Zero,true);
                        // 1. 供货件 2.非供货件 且是面套和发泡
                        if( isZeroPart ){//|| "T".equals(strPartType)||"U".equals(strPartType)
                            String strPartNum = (String) itemMap.get(SELECT_ATTR_V_PART_NUMBER);
                            // 获取编辑属性
                            StringList editAttrNameList = JF_FormalECRStaticMethod_mxJPO.getRoleEditableFieldsList(isBuyTable, isMakeTable, roleList, strProcurementType, strPartType,strDirectBuy ,strChangeSource,strCurrent,makeEditRoleList,isZeroPart,isVerifyUnitPrice);
                            // 设置编辑标识
                            setIsEditMap(itemMap,editAttrNameList,unitEditAttrNameList);

                        }
                    }

                }else {
                    //非游离件
                    boolean isZeroPart = JF_FormalECRStaticMethod_mxJPO.checkPartIsZeroPart(context, strPartId, strProjectId);
                    itemMap.put(ATTR_JFPartType_Flag_Zero,true);
                    if (isZeroPart){
                        if (ATTR_JFPartType_RANGE_U.equals(strPartType)){
                            for (int i1 = i + 1; i1 < nextLevel;i1++) {
                                Map sunPartInfo = (Map)newECRAffectItemList.get(i1);
                                String strSunProcurementType = UIUtil.getValue(sunPartInfo, SELECT_ATTR_JF_ProcurementType);
                                String strSunPartType = UIUtil.getValue(sunPartInfo, SELECT_ATTR_JFPartType);
                                String strSunDirectBuy = UIUtil.getValue(sunPartInfo, SELECT_ATTR_JFDIRECT_BUY);
                                String strSunChangeSource = UIUtil.getValue(sunPartInfo, STRING_SELECT_JFRELATEITEM_JFCHANGESOURCE);
                                if (UIUtil.isNullOrEmpty(strSunChangeSource)){
                                    strSunChangeSource = strECRChangeSource;
                                }
                                String strSunEvaluate = UIUtil.getValue(sunPartInfo, SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate);

                                if (ATTR_JF_VPMReferenceCost_JF_Evaluate_RANGE_N.equals(strSunEvaluate) && ATTR_JF_ProcurementType_RANGE_MAKE.equals(strSunProcurementType)){
                                    //发泡供货件下面的面套一级件可以编辑
                                    if (ATTR_JFPartType_RANGE_T.equals(strSunPartType)){
                                        StringList editAttrNameList = JF_FormalECRStaticMethod_mxJPO.getRoleEditableFieldsList(isBuyTable, isMakeTable, roleList, strSunProcurementType, strSunPartType,strSunDirectBuy ,strSunChangeSource,strCurrent,makeEditRoleList,false,isVerifyUnitPrice);
                                        // 设置编辑标识
                                        setIsEditMap(sunPartInfo,editAttrNameList,unitEditAttrNameList);
                                    }
                                }
                            }

                        }else {
                            for (int i1 = i + 1; i1 < nextLevel;) {
                                Map sunPartInfo = (Map)newECRAffectItemList.get(i1);
                                String strSunLevel = UIUtil.getValue(sunPartInfo, SELECT_LEVEL);
                                String strSunProcurementType = UIUtil.getValue(sunPartInfo, SELECT_ATTR_JF_ProcurementType);
                                String strSunPartType = UIUtil.getValue(sunPartInfo, SELECT_ATTR_JFPartType);
                                String strSunDirectBuy = UIUtil.getValue(sunPartInfo, SELECT_ATTR_JFDIRECT_BUY);
                                String strSunChangeSource = UIUtil.getValue(sunPartInfo, STRING_SELECT_JFRELATEITEM_JFCHANGESOURCE);
                                if (UIUtil.isNullOrEmpty(strSunChangeSource)){
                                    strSunChangeSource = strECRChangeSource;
                                }
                                String strSunEvaluate = UIUtil.getValue(sunPartInfo, SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate);

                                int nextSunLevel = findEndOfSubtree(newECRAffectItemList, i1); // 找到以i为根的子树结束位置
                                if ("2".equals(strSunLevel)){
                                    if (ATTR_JF_VPMReferenceCost_JF_Evaluate_RANGE_N.equals(strSunEvaluate) && ATTR_JF_ProcurementType_RANGE_MAKE.equals(strSunProcurementType)){
                                        //一级件发泡，下面的子集都可以编辑
                                        if (ATTR_JFPartType_RANGE_U.equals(strSunPartType)){
                                            StringList editAttrNameList = JF_FormalECRStaticMethod_mxJPO.getRoleEditableFieldsList(isBuyTable, isMakeTable, roleList, strSunProcurementType, strSunPartType,strSunDirectBuy ,strSunChangeSource,strCurrent,makeEditRoleList,false,isVerifyUnitPrice);
                                            // 设置编辑标识
                                            setIsEditMap(sunPartInfo,editAttrNameList,unitEditAttrNameList);
                                        }else if (ATTR_JFPartType_RANGE_T.equals(strSunPartType) && (!ATTR_JFPartType_RANGE_T.equals(strPartType))){
                                            StringList editAttrNameList = JF_FormalECRStaticMethod_mxJPO.getRoleEditableFieldsList(isBuyTable, isMakeTable, roleList, strSunProcurementType, strSunPartType,strSunDirectBuy ,strSunChangeSource,strCurrent,makeEditRoleList,false,isVerifyUnitPrice);
                                            // 设置编辑标识
                                            setIsEditMap(sunPartInfo,editAttrNameList,unitEditAttrNameList);
                                        }
                                    }
                                }
                                if (i1 != nextSunLevel){
                                    i1 = nextSunLevel;
                                }else {
                                    i1++ ;
                                }

                            }
                        }

                        if (ATTR_JF_VPMReferenceCost_JF_Evaluate_RANGE_N.equals(strEvaluate) && ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)){
                            // 获取编辑属性
                            StringList editAttrNameList = JF_FormalECRStaticMethod_mxJPO.getRoleEditableFieldsList(isBuyTable, isMakeTable, roleList, strProcurementType, strPartType,strDirectBuy ,strChangeSource,strCurrent,makeEditRoleList,isZeroPart,isVerifyUnitPrice);
                            // 设置编辑标识
                            setIsEditMap(itemMap,editAttrNameList,unitEditAttrNameList);
                        }
                    }else {
                        //非供货件 GU 及其子集可以编辑 GT 根节点可以编辑
                        if (ATTR_JF_VPMReferenceCost_JF_Evaluate_RANGE_N.equals(strEvaluate) && ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)){
                            if (ATTR_JFPartType_RANGE_U.equals(strPartType) || ATTR_JFPartType_RANGE_T.equals(strPartType) ){
                                // 获取编辑属性
                                StringList editAttrNameList = JF_FormalECRStaticMethod_mxJPO.getRoleEditableFieldsList(isBuyTable, isMakeTable, roleList, strProcurementType, strPartType,strDirectBuy ,strChangeSource,strCurrent,makeEditRoleList,isZeroPart,isVerifyUnitPrice);
                                // 设置编辑标识
                                setIsEditMap(itemMap,editAttrNameList,unitEditAttrNameList);
                            }
                        }

                    }
                }
                /** P950-P1029 逻辑是获取自制件清单中根节点自制角色的编辑权限   end **/
                /** *P1051-P1056 逻辑是获取自制件清单中根节点采购角色的权限 begin   */
                if ( ATTR_JF_VPMReferenceCost_JF_Evaluate_RANGE_N.equals(strEvaluate) && ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)){
                    // 获取编辑属性 自制件清单中的采购等角色
                    StringList editAttrNameList = JF_FormalECRStaticMethod_mxJPO.getRoleEditableFieldsList(isBuyTable, isMakeTable, roleList, strProcurementType, strPartType,strDirectBuy ,strChangeSource,strCurrent,buyEditRoleList,false,isVerifyUnitPrice);
                    // 设置编辑标识
                    setIsEditMap(itemMap,editAttrNameList,unitEditAttrNameList);
                }
                /** *P1051-P1056 逻辑是获取自制件清单中根节点采购角色的权限 end   */

            }else {
                if (ATTR_JF_VPMReferenceCost_JF_Evaluate_RANGE_N.equals(strEvaluate) && ATTR_JF_ProcurementType_RANGE_MAKE.equals(strProcurementType)){
                    // 获取编辑属性 自制件清单中的采购等角色
                    StringList editAttrNameList = JF_FormalECRStaticMethod_mxJPO.getRoleEditableFieldsList(isBuyTable, isMakeTable, roleList, strProcurementType, strPartType,strDirectBuy ,strChangeSource,strCurrent,buyEditRoleList,false,isVerifyUnitPrice);
                    // 设置编辑标识
                    setIsEditMap(itemMap,editAttrNameList,unitEditAttrNameList);
                }
            }

        }
        newECRAffectItemList = rearrangeStructureByFreeState(newECRAffectItemList);
        JF_LOGGER.info("----------------------------- getFormalECRMakeTableData end ------------------------------------------------");
        return newECRAffectItemList;
    }

    /**
     *
     *@description 自制件清单未评估数据
     *@param context
     *@param args
     *@return com.matrixone.apps.domain.util.MapList
     *@throws
     *@author CHENYAN
     *@date 2026/2/28 11:32
     */

    public MapList getFormalECRMakeTableNotEvaluateData(Context context, String[] args) throws Exception {
        MapList tableData = getFormalECRMakeTableData(context, args);
        //需要移除的下标
        Set<Integer> removeIndexSet = new HashSet<>();
        List<Integer> indexList = new ArrayList<>();
        //重新过滤掉已评估数据
        for (int i = 0; i < tableData.size();) {
            Map tableMap = (Map) tableData.get(i);
            String strEvaluate = (String) tableMap.get(SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate);
            //收集以评估数据下标
            if (ATTR_JF_VPMReferenceCost_JF_Evaluate_RANGE_Y.equals(strEvaluate)){
                int nextLevel = findEndOfSubtree(tableData, i); // 找到以i为根的子树结束位置
                for (int j = i ; j < nextLevel ; j++) {
                    removeIndexSet.add(j);
                }
                i = nextLevel;
            }else {
                i++;
            }
        }
        indexList.addAll(removeIndexSet);
        // 倒序排序
        Collections.sort(indexList, Collections.reverseOrder());
        for (Integer index : indexList) {
            tableData.remove(index.intValue());
        }
        return tableData;
    }


    public boolean isECRRootStructureRelationShipByRelName(String strRelName) {
        boolean isRootStructureRelationShip = false;
        if (REL_JFECRRelateRoot.equalsIgnoreCase(strRelName) || REL_JFECRRelateRootBubble.equalsIgnoreCase(strRelName)) {
            isRootStructureRelationShip = true;
        }
        return isRootStructureRelationShip;
    }

    /**
    *
    *@description 保存过后防止样式回归需要再次设置一下
    *@param context
	*@param args
    *@return java.util.Map
    *@throws
    *@author CHENYAN
    *@date 2026/1/26 16:02
    */
    public Map flushChangedRows(Context context ,String[] args) throws Exception{
        JF_LOGGER.info("----------------------------- flushChangedRows begin ------------------------------------------------");
        HashMap resMap = new HashMap<>();
        Map argsMap = JPO.unpackArgs(args);
        resMap.put ("Action", "execScript");
        Map requestInfo         = (Map) argsMap.get("requestMap");
        //当前页面项目id
//        String projectId        = (String)requestInfo.get("parentOID");
        List elementList = null;
        Document doc = (Document) argsMap.get("XMLDoc");
        if(doc != null) {
            Element rootElement = (Element)doc.getRootElement();
            elementList     = rootElement.getChildren("object");
        }
        StringList rowIdList = new StringList();
        MapList changedRowList = new MapList();
        for (int i = 0; i < elementList.size(); i++) {
            Element childCElement = (Element) elementList.get(i);
            String objectId         = childCElement.getAttributeValue("objectId");
            String relId            = childCElement.getAttributeValue("relId");
            String rowId            = childCElement.getAttributeValue("rowId");
            String markup           = childCElement.getAttributeValue("markup");
            String parentId         = childCElement.getAttributeValue("parentId");
            String lastOperation    = childCElement.getAttributeValue("lastOperation");
            Map<String,String> rowInfoMap = new HashMap<String,String>();
            rowInfoMap.put("oid", objectId);
            rowInfoMap.put("rowId", rowId);
            rowInfoMap.put("relid", relId);
            rowInfoMap.put("markup", markup);
            rowIdList.add(rowId);
            changedRowList.add(rowInfoMap);
        }
        String strRowIdJson =rowIdList.join("@");
        JF_LOGGER.info("strRowIdJson:{}",strRowIdJson);
        StringBuilder sbScriptBuild = new StringBuilder();
        sbScriptBuild.append("{ main:function()  {");
        sbScriptBuild.append("flushChangedRow(");
        sbScriptBuild.append("\"");
        sbScriptBuild.append(strRowIdJson);
        sbScriptBuild.append("\"");
        sbScriptBuild.append(")");
        sbScriptBuild.append("}}");
        String strScript = sbScriptBuild.toString();
        JF_LOGGER.info("strScript:{}",strScript);
        resMap.put("Message", strScript);
        resMap.put("changedRows", changedRowList);
        JF_LOGGER.info("----------------------------- flushChangedRows end ------------------------------------------------");
        return resMap;
    }


    /**
     *
     *@description 获取正式ECR 自制件和采购件清单的表格上传权限
     *@param context
     *@param args
     *@return boolean
     *@throws
     *@author CHENYAN
     *@date 2026/2/2 17:23
     */

    public boolean getUploadFormalECRTableAccess(Context context, String[] args) throws Exception {
        JF_LOGGER.info("-----------------------------------------getUploadFormalECRTableAccess begin ----------------------------------------------------");
        boolean res = false;
        Map requestMap = JPO.unpackArgs(args);
        String strObjectId = (String) requestMap.get(STRING_OBJECTID);
        String strSelectedTable = (String) requestMap.get(STRING_TABLE);
        JF_LOGGER.info("strSelectedTable:{}", strSelectedTable);
        boolean isBuyTable = JF_FormalECRStaticMethod_mxJPO.checkIsBuyTable(strSelectedTable);
        boolean isMakeTable = JF_FormalECRStaticMethod_mxJPO.checkIsMakeTable(strSelectedTable);
        String strLoginUser = context.getUser();
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        Map ecrInfo = ecr.getInfo(context, StringList.create(SELECT_CURRENT,SELECT_TYPE));
        String strECRCurrent = (String) ecrInfo.get(SELECT_CURRENT);
        String strECRType = (String) ecrInfo.get(SELECT_TYPE);
        if ( TYPE_JFFormalECR.equals(strECRType)&&("Countersign".equals(strECRCurrent) || "APR".equals(strECRCurrent))) {
            //角色不为空的会签任务
            MapList loginUserSignTaskRole = new MapList();
            loginUserSignTaskRole = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECRAndUser(context, new String[]{strObjectId, strLoginUser});
            if (loginUserSignTaskRole.size() > 0) {
                for (int i = 0; i < loginUserSignTaskRole.size(); i++) {
                    Map signTask = (Map) loginUserSignTaskRole.get(i);
                    String strCurrent = (String) signTask.get("current");
                    JF_LOGGER.info("strCurrent:{}", strCurrent);
                    // Only Active sign tasks can upload/update formal ECR table data.
                    if ("Active".equals(strCurrent)) {
                        String strRole = (String) signTask.get("role");
                        if (isBuyTable) {
                            if (JF_FormalECRStaticMethod_mxJPO.EDIT_ECRCosting_TABLE_ROLE_LIST.contains(strRole)) {
                                res = true;
                                break;
                            }
                        }
                        if (isMakeTable) {
                            if (JF_FormalECRStaticMethod_mxJPO.EDIT_ECRController_TABLE_ROLE_LIST.contains(strRole)) {
                                res = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        JF_LOGGER.info("res:{}", res);
        JF_LOGGER.info("-----------------------------------------getUploadNewECRTableAccess end ----------------------------------------------------");
        return res;
    }

    /**
    *
    *@description 下载正式ECR成本excel权限
    *@param context
	*@param args
    *@return boolean
    *@throws
    *@author CHENYAN
    *@date 2026/2/2 17:23
    */
    public boolean getDownloadFormalECRTableAccess(Context context, String[] args) throws Exception {
        boolean res = false;
        Map requestMap = JPO.unpackArgs(args);
//        String strObjectId  = (String)requestMap.get("parentOID");
        String strObjectId = (String) requestMap.get("objectId");
        String strSelectedTable = (String) requestMap.get("table");
        String strLoginUser = context.getUser();
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        Map ecrInfo = ecr.getInfo(context, StringList.create(SELECT_CURRENT,SELECT_TYPE));
        String strECRCurrent = (String) ecrInfo.get(SELECT_CURRENT);
        String strECRType = (String) ecrInfo.get(SELECT_TYPE);
        JF_LOGGER.info("strSelectedTable:{}", strSelectedTable);
        boolean isBuyTable = JF_FormalECRStaticMethod_mxJPO.checkIsBuyTable(strSelectedTable);
        boolean isMakeTable = JF_FormalECRStaticMethod_mxJPO.checkIsMakeTable(strSelectedTable);
        if ( TYPE_JFFormalECR.equals(strECRType) &&("Countersign".equals(strECRCurrent) || "APR".equals(strECRCurrent) ||
                "Quotation".equals(strECRCurrent) || "Complete".equals(strECRCurrent))) {
            //角色不为空的会签任务
            MapList loginUserSignTaskRole = new MapList();
            loginUserSignTaskRole = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECRAndUser(context, new String[]{strObjectId, strLoginUser});
            if (loginUserSignTaskRole.size() > 0) {
                for (int i = 0; i < loginUserSignTaskRole.size(); i++) {
                    Map signTask = (Map) loginUserSignTaskRole.get(i);
                    String strCurrent = (String) signTask.get("current");
                    JF_LOGGER.info("strCurrent:{}", strCurrent);
                    String strRole = (String) signTask.get("role");
//                    strRole = JF_NewECRRESTService_mxJPO.getRoleKeyByValue(strRole);
                    JF_LOGGER.info("strRole:{}", strRole);
//                    if ("JFECRCosting".equals(strSelectedTable)) {
                    if (isBuyTable) {
                        if (JF_FormalECRStaticMethod_mxJPO.EDIT_ECRCosting_TABLE_ROLE_LIST.contains(strRole)) {
                            res = true;
                            break;
                        }
                    }
                    if (isMakeTable) {
                        if (JF_FormalECRStaticMethod_mxJPO.EDIT_ECRController_TABLE_ROLE_LIST.contains(strRole)) {
                            res = true;
                            break;
                        }
                    }
                }
            }
        }
        JF_LOGGER.info("res:{}", res);
        return res;
    }


    /**
    *
    *@description 正式ECR下载excel
    *@param context
	*@param args
    *@return java.util.Map
    *@throws
    *@author CHENYAN
    *@date 2026/2/5 10:47
    */
    public Map FormalECRDownload(Context context, String[] args) throws Exception {
        Map res = new HashMap<String, String>();
        Map paramMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) paramMap.get("objectId");
        //选择的表格
        String strSelectTable = (String) paramMap.get("type");
        JF_LOGGER.info("strSelectTable:{}", strSelectTable);
        boolean isBuyTable = JF_FormalECRStaticMethod_mxJPO.checkIsBuyTable(strSelectTable);
        boolean isMakeTable = JF_FormalECRStaticMethod_mxJPO.checkIsMakeTable(strSelectTable);
        //服务路径
        String strPath = (String) paramMap.get("path");
        //模板excel路径
        String strTemplatePath = JF_PublicMethodClass_mxJPO.buildStringInStrings(strPath,TEMPLATE_EXCEL_PATH,strSelectTable,".xlsx");
        JF_LOGGER.info("strTemplatePath:{}", strTemplatePath);
        InputStream inputStream = null;
        FileOutputStream fos = null;
        try {
            inputStream = new FileInputStream(strTemplatePath);
            Workbook workbook = WorkbookFactory.create(inputStream);
            //设置可编辑单元格背景色
            CellStyle cellEditStyle = workbook.createCellStyle();
            //设置背景色
            cellEditStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            //必须设置 否则背景色不生效
            cellEditStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellEditStyle.setBorderTop(BorderStyle.THIN);
            cellEditStyle.setBorderBottom(BorderStyle.THIN);
            cellEditStyle.setBorderLeft(BorderStyle.THIN);
            cellEditStyle.setBorderRight(BorderStyle.THIN);
            //设置单元格边框
            CellStyle cellBorderStyle = workbook.createCellStyle();
            cellBorderStyle.setBorderTop(BorderStyle.THIN);
            cellBorderStyle.setBorderBottom(BorderStyle.THIN);
            cellBorderStyle.setBorderLeft(BorderStyle.THIN);
            cellBorderStyle.setBorderRight(BorderStyle.THIN);
            //读取第一个sheet
            Sheet sheet = workbook.getSheetAt(0);

            //从第二行遍历sheet，并拿取其中每行的数据 头信息
            Row row = sheet.getRow(0);
            Map excelTitleMap = new HashMap<String, Integer>();
            for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
                String strCell = row.getCell(i, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK) == null ? "" : JF_ECRService_mxJPO.getCellValue(row, i);
                if (UIUtil.isNotNullAndNotEmpty(strCell)) {
                    excelTitleMap.put(strCell, i);
                }
            }
            JF_LOGGER.info("excelTitleMap:{}", excelTitleMap);
            HashMap<String, String> stringStringHashMap = new HashMap<>();
            stringStringHashMap.put("objectId", strObjectId);
            stringStringHashMap.put("expandLevel", "0");
            stringStringHashMap.put(STRING_SELECT_TABLE, strSelectTable);

            MapList ecrCostingTableData = new MapList();
            //costing 表格数据
            if (isBuyTable) {
                ecrCostingTableData = getFormalECRBuyTableData(context, JPO.packArgs(stringStringHashMap));
            } else if (isMakeTable) {
                ecrCostingTableData = getFormalECRMakeTableData(context, JPO.packArgs(stringStringHashMap));
            }

            MapList CostingMappingList = JF_PublicMethodClass_mxJPO.getFormalECRTableMapping(context, strSelectTable);
            JF_LOGGER.info("CostingMappingList:{}",CostingMappingList);
            updateTableData(context, strSelectTable, strObjectId, ecrCostingTableData, CostingMappingList);
            JF_LOGGER.info("ecrCostingTableData:{}",ecrCostingTableData);
            //编辑table 数据写入excel
            Set existSet = new HashSet();
            for (int i = 0; i < ecrCostingTableData.size(); i++) {
                Row createRow = sheet.createRow(i + 2);
                Map map = (Map) ecrCostingTableData.get(i);
                createRowValue(excelTitleMap, map, createRow, cellEditStyle, cellBorderStyle,CostingMappingList,existSet);
            }
            String strTmpPath = context.createWorkspace();
            strTmpPath = strTmpPath.endsWith("/") ? strTmpPath : JF_PublicMethodClass_mxJPO.buildStringInStrings(strTmpPath, "/");
            String strFileName = JF_PublicMethodClass_mxJPO.buildStringInStrings(String.valueOf(System.currentTimeMillis()), ".xlsx");
            String strFullPath = JF_PublicMethodClass_mxJPO.buildStringInStrings(strTmpPath, strFileName);
            JF_LOGGER.info("strFullPath:{}", strFullPath);
            fos = new FileOutputStream(strFullPath);
            workbook.write(fos);
            workbook.close();
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
    public static Map getECRChangeMess(Context context, String[] args) {
        HashMap<String, String> returnMap = new HashMap<>();
        try {
            String Add = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.Add", new String[]{});
            String AddRelease = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.AddRelease", new String[]{});
            String Delete = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.Delete", new String[]{});
            String Revise = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.Revise", new String[]{});
            String ReviseRelease = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.ReviseRelease", new String[]{});
            String VersionReplace = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.VersionReplace", new String[]{});
            String AddNum = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.AddNum", new String[]{});
            String DelNum = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.DelNum", new String[]{});
            String FirstRelease = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ECRChange.FirstRelease", new String[]{});
            returnMap.put("Add", Add);
            returnMap.put("AddRelease", AddRelease);
            returnMap.put("Delete", Delete);
            returnMap.put("Revise", Revise);
            returnMap.put("ReviseRelease", ReviseRelease);
            returnMap.put("VersionReplace", VersionReplace);
            returnMap.put("AddNum", AddNum);
            returnMap.put("DelNum", DelNum);
            returnMap.put("FirstRelease", FirstRelease);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnMap;
    }
    public  StringList getECRPartChangeDetails(Context context, String[] args) throws Exception {
        JF_LOGGER.info("getECRPartChangeDetails 。。。。。。。。。。。。。。。。");
        StringList res = new StringList();
        try {
            ContextUtil.pushContext(context);
            Map paramsMap = JPO.unpackArgs(args);
//            _logger.info("paramsMap:{}", paramsMap);
            //表格中的零件
            MapList objectList = (MapList) paramsMap.get(STRING_OBJECTLIST);
            Map paramList = (Map) paramsMap.get("paramList");
            Map columnMap = (Map) paramsMap.get("columnMap");
            String colName = (String) columnMap.get(SELECT_NAME);
            DomainObject domainObject = DomainObject.newInstance(context);
            JF_Util_mxJPO jfUtilMxJPO = new JF_Util_mxJPO();
            Map ecrChangeMess = getECRChangeMess(context, new String[]{});
            for (int i = 0; i < objectList.size(); i++) {
                Map objectMap = (Map) objectList.get(i);
                String changeDes = UIUtil.getValue(objectMap, SELECT_ATTRIBUTE_JF_BOMChangeDes);
                String changeBeforeRev = UIUtil.getValue(objectMap, SELECT_ATTRIBUTE_JF_ChangeBeforeRev);
                String current = UIUtil.getValue(objectMap, SELECT_CURRENT);
                String strRevision = UIUtil.getValue(objectMap, SELECT_REVISION);
                if ("JF_BeforeRev".equalsIgnoreCase(colName)) {
                    //上一个版本
                    String previousReleasedId = EMPTY_STRING;
                    if (changeDes.equalsIgnoreCase(UIUtil.getValue(ecrChangeMess, "Add"))
                            || changeDes.contains(UIUtil.getValue(ecrChangeMess, "AddRelease"))
                            || changeDes.contains(UIUtil.getValue(ecrChangeMess, "FirstRelease"))) {
                        previousReleasedId = EMPTY_STRING;
                    } else if (changeDes.contains("数量") || changeDes.contains(UIUtil.getValue(ecrChangeMess, "Delete"))) {
                        //为当前版本
                        previousReleasedId = (String) objectMap.get(SELECT_ID);
                    } else {
                        String oid = (String) objectMap.get(SELECT_ID);
                        previousReleasedId = jfUtilMxJPO.getPreviousReleasedMajorId(context, oid);
                    }
                    if (UIUtil.isNotNullAndNotEmpty(previousReleasedId)) {
                        domainObject.setId(previousReleasedId);
                        String revision = domainObject.getInfo(context, SELECT_REVISION);
                        res.add(revision);
                    } else {
                        res.add("");
                    }
                    continue;
                }
                if ("revision".equalsIgnoreCase(colName)) {
                    if (changeDes.contains(UIUtil.getValue(ecrChangeMess, "Delete"))) {
                        res.add("");
                    } else {
                        res.add(strRevision);
                    }
                    continue;
                }
                if (null != objectMap && objectMap.containsKey("id[connection]")) {
                    String connId = (String) objectMap.get("id[connection]");
                    DomainRelationship domainRelationship = DomainRelationship.newInstance(context, connId);
                    String value = domainRelationship.getAttributeValue(context, colName);
                    if ("JF_ChangeBeforeRev".equalsIgnoreCase(colName)) {
                        if (UIUtil.isNotNullAndNotEmpty(value)) {
                            domainObject.setId(value);
                            String revision = domainObject.getInfo(context, SELECT_REVISION);
                            value = revision;
                        }
                    }
                    res.add(value);
                } else {
                    res.add("");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return res;
    }

    public static void updateTableData(Context context, String strSelectedTable, String strECRId, MapList tableDataList, MapList xmlMappingMapList) throws Exception {
        try {
            for (int i = 0; i < xmlMappingMapList.size(); i++) {
                //获取列 key
                Map xmlFieldMap = (Map) xmlMappingMapList.get(i);
                String strFieldKey = (String) xmlFieldMap.get("id");
                //获取属性key
                String strFieldSelectKey = (String) xmlFieldMap.get("selectValue");
                String strFieldValueKey = (String) xmlFieldMap.get("value");
                String strFieldFunName = (String) xmlFieldMap.get("funName");
                String strFieldJPOName = (String) xmlFieldMap.get("jpoName");
                JF_LOGGER.info("strFieldKey:{}", strFieldKey);
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
                if (UIUtil.isNotNullAndNotEmpty(strFieldJPOName) && UIUtil.isNotNullAndNotEmpty(strFieldFunName)){
                    //列显示信息
                    StringList fieldValueList = JPO.invoke(context, strFieldJPOName, null, strFieldFunName, JPO.packArgs(paramsMap), StringList.class);
                    JF_LOGGER.info("fieldValueList:{}", fieldValueList);
                    for (int i1 = 0; i1 < tableDataList.size(); i1++) {
                        Map rowData = (Map) tableDataList.get(i1);
                        rowData.put(JF_PublicMethodClass_mxJPO.buildStringInStrings(strFieldKey, "disPlayValue"), fieldValueList.get(i1));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JF_LOGGER.error(e.getMessage());
            throw e;
        }
    }
    /**
    *
    *@description 根据数据库值写入到excel的row
    *@param excelTitleMap
	*@param dbMap
	*@param createRow
	*@param cellStyle
	*@param cellBorderStyle
	*@param CostingMappingList
    *@return org.apache.poi.ss.usermodel.Row
    *@throws
    *@author CHENYAN
    *@date 2026/2/5 10:46
    */

    public Row createRowValue(Map excelTitleMap, Map dbMap, Row createRow, CellStyle cellStyle, CellStyle cellBorderStyle,MapList CostingMappingList,Set existSet) throws Exception {
        Map editAttrMap = (Map) dbMap.get(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT);
        JF_LOGGER.info("editAttrMap:{}",editAttrMap);
        String id = (String)dbMap.get(SELECT_ID);
        boolean isIdEditStyle = false ;
        for (Object entry : excelTitleMap.entrySet()) {
            Map.Entry cellKey = (Map.Entry) entry;
            //列的key
            String strCellKey = (String) cellKey.getKey();
            //列的下标
            Integer iCellIndex = (Integer) cellKey.getValue();
            Cell createCell = createRow.createCell(iCellIndex);
            boolean isEditStyle = false ;
            //存在显示值直接设置
            String strFullKey = JF_PublicMethodClass_mxJPO.buildStringInStrings(strCellKey, "disPlayValue");
            String strKeyValue = EMPTY_STRING;
            if (dbMap.containsKey(strFullKey)){
                strKeyValue = (String) dbMap.get(strFullKey);
            }else {
                Map xmlMap = findXmlById(CostingMappingList, strCellKey);
                if (Objects.nonNull(xmlMap)){
                    //判断行是否可编辑
                    Boolean isRowEdit = (Boolean) dbMap.get(JF_FormalECRStaticMethod_mxJPO.STRING_Row_Edit);
                    //实际的select key
                    String strSelectValue = (String) xmlMap.get("selectValue");
                    //真实的属性值
                    String strAttrValue = (String) xmlMap.get("value");
//                    String isEdit = (String) xmlMap.get("isEdit");
                    strKeyValue = (String) dbMap.get(strSelectValue);
                    boolean  JF_StagnationPriceflag = !"JF_VPMReferenceCost.JF_StagnationPrice".equalsIgnoreCase(strAttrValue);
                    if (Objects.nonNull(isRowEdit) && isRowEdit){
                        if (Objects.nonNull(editAttrMap)){
                            JF_LOGGER.info("strAttrValue:{}",strAttrValue);
                            if (editAttrMap.containsKey(strAttrValue)) {
                                Object isEditValue = editAttrMap.get(strAttrValue);
                                JF_LOGGER.info("isEditValue:{}",isEditValue);
                                if (isEditValue instanceof Boolean){
                                    //可以编辑
                                    if ((Boolean) isEditValue &&JF_StagnationPriceflag && !existSet.contains(id)){
//                                        existSet.add(id);
                                        isIdEditStyle = true;
                                        isEditStyle  = true;
                                    }
                                }
                            //也是首版发布
                            }/*else if (editAttrMap.size() == 0){
                                String strFieldEditAccess = (String) xmlMap.get(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT);
                                if ("true".equals(strFieldEditAccess)){
                                    isEditStyle  = true;
                                }
                            }*/

                        }else {
                            //首版发布
                            String strFieldEditAccess = (String) xmlMap.get(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT);
                            if ("true".equals(strFieldEditAccess) &&JF_StagnationPriceflag && !existSet.contains(id)){
//                                existSet.add(id);
                                isIdEditStyle = true;
                                isEditStyle  = true;
                            }
                        }
                    }



                }
            }

            JF_LOGGER.info("strKeyValue:{}",strKeyValue);
            createCell.setCellValue(strKeyValue);

                if (isEditStyle) {
                    createCell.setCellStyle(cellStyle);
                } else {
                    if (!("id".equals(strCellKey) || "id[connection]".equals(strCellKey))) {
                        createCell.setCellStyle(cellBorderStyle);
                    }
                }
        }
        if (isIdEditStyle) {
            existSet.add(id);
        }
        return createRow;
    }

    public Map findXmlById(MapList CostingMappingList ,String strCellKey){
        //通过id 找到对应的 selectValue
        for (int i = 0; i < CostingMappingList.size(); i++) {
            Map tableXmlMap = (Map) CostingMappingList.get(i);
            String strColNameId = (String) tableXmlMap.get("id");
            if (strCellKey.equals(strColNameId)){
                return tableXmlMap;
            }
        }
        return null;
    }

    public Map readFormalECRExcelAndUpdateData(Context context, String[] args) throws Exception {
        JF_LOGGER.info("----------------------------------  readFormalECRExcelAndUpdateData begin --------------------------------------------------------");
        Map res = new HashMap<String, String>();
        Map paramMap = (Map) JPO.unpackArgs(args);
        List files = (List) paramMap.get("files");
        String strObjectId = (String) paramMap.get("objectId");
        String strSelectTable = (String) paramMap.get("type");
        boolean isBuyTable = JF_FormalECRStaticMethod_mxJPO.checkIsBuyTable(strSelectTable);
        boolean isMakeTable = JF_FormalECRStaticMethod_mxJPO.checkIsMakeTable(strSelectTable);
        java.io.File file = (java.io.File) files.get(0);
        String strMess = "";
        if (!file.exists()) {
            JF_LOGGER.info("======================================文件不存在，请核对文件位置");
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
            JF_LOGGER.info("======================================excel 内容错误");
            res.put("code", "404");
            strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRUploadContentError");
            res.put("mess", strMess);
            return res;
        }
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        //ECR关联项目
        String strProjectId = ecr.getInfo(context, JF_FormalECRStaticMethod_mxJPO.STRING_SELECT_JFECRCONECTIONPROJECT);

        String strLevel = "0";
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("objectId", strObjectId);
        stringStringHashMap.put("expandLevel", strLevel);
        stringStringHashMap.put(STRING_SELECT_TABLE, strSelectTable);
//        stringStringHashMap.put(JF_FormalECRStaticMethod_mxJPO.STRING_VERIFY_UNIT_PRICE, "true");
        //costing xml 映射
        MapList CostingMappingList = JF_PublicMethodClass_mxJPO.getFormalECRTableMapping(context, strSelectTable);
        //excel 标题和对应下标
        Map excelHeadInfoMap = getExcelHeadInfo(sheet);
        //校验excel数据
        Map checkMap = checkExcelData(sheet, excelHeadInfoMap, CostingMappingList);
        JF_LOGGER.info("checkMap:{}", checkMap);
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
        if (isBuyTable) {
            ecrCostingTableData = getFormalECRBuyTableData(context, JPO.packArgs(stringStringHashMap));
        }
        if (isMakeTable) {
            ecrCostingTableData = getFormalECRMakeTableData(context, JPO.packArgs(stringStringHashMap));
        }
        JF_LOGGER.info("ecrCostingTableData:{}", ecrCostingTableData);
        //获取excel中除标题数据
        List<Row> excelDataInfo = getExcelDataInfo(sheet);
        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            //遍历数据判断该行有没有可以编辑的属性
            //如果有，根据id找到对应excel的行（如果存在共用件那么会出现多个） 因为属性都在在对象上直接修改后移除已经改过的excel行
            //需要同步的当前版本的原始成本
            Map<String,Set<String>> syncOriginalMap = new HashMap();

            for (int i = 0; i < ecrCostingTableData.size(); i++) {
                Map costingMap = (Map) ecrCostingTableData.get(i);
                //行可以编辑
                if (costingMap.containsKey(JF_FormalECRStaticMethod_mxJPO.STRING_Row_Edit)) {
                    Map editAttrMap = (Map) costingMap.get(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT);
                    JF_LOGGER.info("editAttrMap:{}",editAttrMap);
                    // 可编辑属性map 不为空 且数量大于 0
                    if (Objects.nonNull(editAttrMap) && editAttrMap.size() > 0) {
                        // 找到对应excel的 row数据
                        String strPartId = (String) costingMap.get(SELECT_ID);
                        List<Row> editRowList = getExcelRowDataByMap(strPartId, excelDataInfo, excelHeadInfoMap);
                        if (Objects.nonNull(editRowList)&& editRowList.size() > 0){
                            //只取第一个
                            Row editRow = editRowList.get(0);
                            //更新的属性值集合
                            Map updateAttributeValueMap = new HashMap();
                            //可以编辑的属性
                            Set editAttrSet = editAttrMap.keySet();
                            JF_LOGGER.info("editAttrSet:{}",editAttrSet);
                            for (Object oAttrName : editAttrSet) {
                                String strAttrName = (String)oAttrName;
                                Map attrMap = getRowIndexByRequiredAttribute(excelHeadInfoMap, CostingMappingList, strAttrName);
                                JF_LOGGER.info("attrMap:{}", attrMap);
                                if (Objects.nonNull(attrMap) && attrMap.size() > 0){
                                    int iIndex = (int)attrMap.get("index");
                                    Cell cell = editRow.getCell(iIndex);

//                                    String strCellValue = cell.getStringCellValue();
                                    String strCellValue =  JF_FormalECRStaticMethod_mxJPO.getCellValueAsString(cell);
                                    if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                                        //默认保留两位小数
                                        String str2FCellValue = String.format("%.2f", Double.parseDouble(strCellValue));
                                        //如果是物流费需要特殊处理
                                        if (ATTR_JF_LogisticsFees.equals(strAttrName)) {
                                            updateLogisticsFees(context, strProjectId,strPartId, str2FCellValue);
                                        }else {
                                            updateAttributeValueMap.put(strAttrName, str2FCellValue);
                                        }
                                    }
                                }

                            }
                            if (updateAttributeValueMap.size() > 0){
                                DomainObject part = DomainObject.newInstance(context, strPartId);
                                part.setAttributeValues(context,updateAttributeValueMap);
                                //把修改的属性存下载，便于后面找到原始成本
                                Set keySet = updateAttributeValueMap.keySet();
                                syncOriginalMap.put(strPartId,keySet);
                            }

                            //移除已经修改过的excel行
                            excelDataInfo.remove(editRow);

                        }

                    }
                }
            }
            ContextUtil.commitTransaction(context);
            //提交事务后同步修改的增量成本导致 原始成本要更新
            syncOriginalCosting(context, syncOriginalMap);
        } catch (Exception e) {
            JF_LOGGER.error("e:{}", e.getMessage());
            ContextUtil.abortTransaction(context);
            throw new RuntimeException(e);
        } finally {
            ContextUtil.popContext(context);
        }
        res.put("code", "200");
        strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRUploadSuccess");
        res.put("mess", strMess);
        JF_LOGGER.info("----------------------------------  readFormalECRExcelAndUpdateData end --------------------------------------------------------");

        return res;
    }

    /**
    *
    *@description 初始成本Excel导入方法
    *@param context
    *@param args 包含files、objectId、type参数
    *@return Map 导入结果
    *@throws Exception
    *@author CHENYAN
    *@date 2026/3/10
    */
    public Map readFormalInitialCostExcelAndUpdateData(Context context, String[] args) throws Exception {
        JF_LOGGER.info("----------------------------------  readFormalInitialCostExcelAndUpdateData begin --------------------------------------------------------");
        Map res = new HashMap<String, String>();
        Map paramMap = (Map) JPO.unpackArgs(args);
        List files = (List) paramMap.get("files");
        String strObjectId = (String) paramMap.get("objectId");
        String strSelectTable = (String) paramMap.get("type");
        java.io.File file = (java.io.File) files.get(0);
        String strMess = "";
        if (!file.exists()) {
            JF_LOGGER.info("======================================文件不存在，请核对文件位置");
            strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRUploadNullError");
            res.put("code", "404");
            res.put("mess", strMess);
            return res;
        }
        InputStream inputStream = null;
        Workbook workbook = null;
        try {
            inputStream = new FileInputStream(file);
            workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            int iFirstRowNum = sheet.getFirstRowNum();
            int iLastRowNum = sheet.getLastRowNum();
            if (iFirstRowNum == iLastRowNum) {
                JF_LOGGER.info("======================================excel 内容错误");
                res.put("code", "404");
                strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRUploadContentError");
                res.put("mess", strMess);
                return res;
            }
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            String strECRProjectId = ecr.getInfo(context, "from[JFChange2Project].to.id");
            JF_LOGGER.info("strObjectId:{}",strObjectId);
            JF_LOGGER.info("strECRProjectId:{}",strECRProjectId);
            String strLoginUser = context.getUser();
            MapList initialCostMappingList = JF_PublicMethodClass_mxJPO.getFormalECRTableMapping(context, strSelectTable);
            Map excelHeadInfoMap = getExcelHeadInfo(sheet);
            Map checkMap = checkExcelData(sheet, excelHeadInfoMap, initialCostMappingList);
            JF_LOGGER.info("checkMap:{}", checkMap);
            boolean isCheckSuccess = (boolean) checkMap.get("checkResult");
            StringList errorRowIndex = (StringList) checkMap.get("errorRowIndex");
            if ((!isCheckSuccess) && errorRowIndex.size() > 0) {
                res.put("code", "404");
                strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRUploadCheckError");
                strMess = strMess.replace("{}", errorRowIndex.join(","));
                res.put("mess", strMess);
                return res;
            }
            //获取会签角色
            MapList loginUserSignTaskRole = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECRAndUser(context, new String[]{strObjectId, strLoginUser});
            //获取的select
            StringList notFirstPartSelectList = new StringList();
            Map<String, String> attrNameSelectMap = new HashMap<>();
            //固定列属性
            for (int i = 0; i < initialCostMappingList.size(); i++) {
                Map mappingMap = (Map) initialCostMappingList.get(i);
                String strIsFixed = (String) mappingMap.get("isFixed");
                if ("true".equals(strIsFixed)) {
                    String strSelectAttr = (String) mappingMap.get("selectValue");
                    notFirstPartSelectList.add(strSelectAttr);
                }
            }
            //当前角色可以编辑的属性
            StringList currentRoleEditAttr = new StringList() ;
            for (int i = 0; i < initialCostMappingList.size(); i++) {
                Map mappingMap = (Map) initialCostMappingList.get(i);
//                String strAttrName = (String) mappingMap.get("id");
                String strAttrName = (String) mappingMap.get("value");
                String strRole = (String) mappingMap.get("role");
                String strIsEdit = (String) mappingMap.get("isEdit");
                boolean hasRolePermission = false;

                //检查是否需要根据角色添加列
                if (UIUtil.isNotNullAndNotEmpty(strRole)) {
                    for (int j = 0; j < loginUserSignTaskRole.size(); j++) {
                        Map signTask = (Map) loginUserSignTaskRole.get(j);
                        String strUserRole = (String) signTask.get("role");
                        if (strRole.contains(strUserRole)) {
                            hasRolePermission = true;
                            break;
                        }
                    }
                    if (hasRolePermission) {
                        String strSelectAttr = (String) mappingMap.get("selectValue");
                        //添加动态的select 属性
                        notFirstPartSelectList.add(strSelectAttr);
                        if ("true".equals(strIsEdit)){
                            currentRoleEditAttr.add(strAttrName);
                        }
                    }
                }
            }

            //只有存在可以编辑的属性
            if (currentRoleEditAttr.size() > 0){
                MapList systemPartDataList = getFormalInitialCostPartListData(context, strObjectId, strSelectTable, loginUserSignTaskRole, notFirstPartSelectList,strECRProjectId);
                try {
                    ContextUtil.pushContext(context);
                    ContextUtil.startTransaction(context, true);
                    Map<String,Set<String>> syncOriginalMap = new HashMap();
                    //获取excel中除标题数据
                    List<Row> excelDataInfo = getExcelDataInfo(sheet);
                    for (int i = 0; i < systemPartDataList.size(); i++) {
                        Map costingMap = (Map) systemPartDataList.get(i);
                        //行可以编辑
//                        if (costingMap.containsKey(JF_FormalECRStaticMethod_mxJPO.STRING_Row_Edit)) {
                            Map editAttrMap = (Map) costingMap.get(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT);
                            JF_LOGGER.info("editAttrMap:{}",editAttrMap);
                            // 可编辑属性map 不为空 且数量大于 0
                            if (Objects.nonNull(editAttrMap) && editAttrMap.size() > 0) {
                                // 找到对应excel的 row数据
                                String strPartId = (String) costingMap.get(SELECT_ID);
                                List<Row> editRowList = getExcelRowDataByMap(strPartId, excelDataInfo, excelHeadInfoMap);
                                if (Objects.nonNull(editRowList)&& editRowList.size() > 0){
                                    //只取第一个
                                    Row editRow = editRowList.get(0);
                                    //更新的属性值集合
                                    Map updateAttributeValueMap = new HashMap();
                                    //可以编辑的属性
                                    Set editAttrSet = editAttrMap.keySet();
                                    JF_LOGGER.info("editAttrSet:{}",editAttrSet);
                                    for (Object oAttrName : editAttrSet) {
                                        String strAttrName = (String)oAttrName;
                                        //excel xml中配置了可以编辑 双重满足才可以
                                        if (currentRoleEditAttr.contains(strAttrName)){
                                            Map attrMap = getRowIndexByRequiredAttribute(excelHeadInfoMap, initialCostMappingList, strAttrName);
                                            JF_LOGGER.info("attrMap:{}", attrMap);
                                            if (Objects.nonNull(attrMap) && attrMap.size() > 0){
                                                int iIndex = (int)attrMap.get("index");
                                                Cell cell = editRow.getCell(iIndex);
        //                                    String strCellValue = cell.getStringCellValue();
                                                String strCellValue =  JF_FormalECRStaticMethod_mxJPO.getCellValueAsString(cell);
                                                if (UIUtil.isNotNullAndNotEmpty(strCellValue)) {
                                                    //默认保留两位小数
                                                    String str2FCellValue = String.format("%.2f", Double.parseDouble(strCellValue));
                                                    updateAttributeValueMap.put(strAttrName, str2FCellValue);
                                                }
                                            }
                                        }
                                    }
                                    JF_LOGGER.info("updateAttributeValueMap:{}",updateAttributeValueMap);
                                    //更新属性
                                    if (updateAttributeValueMap.size() > 0){
                                        DomainObject part = DomainObject.newInstance(context, strPartId);
                                        part.setAttributeValues(context,updateAttributeValueMap);
                                        //把修改的属性存下载，便于后面找到原始成本
                                        Set keySet = updateAttributeValueMap.keySet();
                                        syncOriginalMap.put(strPartId,keySet);
                                    }
                                    //移除已经修改过的excel行
                                    excelDataInfo.remove(editRow);
                                }

                            }
//                        }
                    }
                    JF_LOGGER.info("syncOriginalMap:{}",syncOriginalMap);
                    syncOriginalCostingByUpdateFirstPart(context, syncOriginalMap);
                    ContextUtil.commitTransaction(context);

                } catch (Exception e) {
                    JF_LOGGER.error("e:{}", e.getMessage());
                    ContextUtil.abortTransaction(context);
                    throw new RuntimeException(e);
                } finally {
                    ContextUtil.popContext(context);
                }

            }else {
                JF_LOGGER.info("======================================excel 内容错误");
                res.put("code", "404");
                strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRUploadContentError");
                res.put("mess", strMess);
                return res;
            }

        } finally {
            if (workbook != null) {
                workbook.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        
        res.put("code", "200");
        strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRUploadSuccess");
        res.put("mess", strMess);
        JF_LOGGER.info("----------------------------------  readFormalInitialCostExcelAndUpdateData end --------------------------------------------------------");

        return res;
    }

    private static boolean isValueEqualForCompare(String strValue1, String strValue2) {
        String strFormatValue1 = formatCompareValue(strValue1);
        String strFormatValue2 = formatCompareValue(strValue2);
        return strFormatValue1.equals(strFormatValue2);
    }

    private static String formatCompareValue(String strValue) {
        if (UIUtil.isNullOrEmpty(strValue)) {
            return EMPTY_STRING;
        }
        String strTrimValue = strValue.trim();
        try {
            BigDecimal value = new BigDecimal(strTrimValue);
            return value.stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            return strTrimValue;
        }
    }



    /**
    *
    *@description 更新execl中增量成本时同步零件的原始成本
    *@param context
	*@param syncOriginalMap  key partid  value 修改的属性
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2026/2/9 16:55
    */

    public void syncOriginalCosting(Context context,  Map<String,Set<String>> syncOriginalMap) throws Exception{
        if (syncOriginalMap.size() > 0){
            for (Map.Entry<String, Set<String>> entry : syncOriginalMap.entrySet()) {
                String strPartId = entry.getKey();
                Set<String> updateAttrSet = entry.getValue();
                Set<String> originAttrSet = new HashSet();
                Set<String> originSelectAttrSet = new HashSet();
                //更新更新的成本
                StringList fullAttrNameList = new StringList();
                for (String strAttrName : updateAttrSet) {
                    //原始成本属性
                    String strOriginalAttrName = JF_FormalECRStaticMethod_mxJPO.findOriginalAttrPriceNameByChangeAttrName(strAttrName);
                    if (UIUtil.isNotNullAndNotEmpty(strOriginalAttrName)){
                        originAttrSet.add(strOriginalAttrName);
                        originSelectAttrSet.add(JF_PublicMethodClass_mxJPO.buildStringInStrings(ATTR_PREFIX,strOriginalAttrName,ATTR_SUFFIX));
                        StringList selectAttrList = JF_FormalECRStaticMethod_mxJPO.getChangeAttrNameByUnitCostingAttr(strOriginalAttrName);
                        for (int i = 0; i < selectAttrList.size(); i++) {
                            String stSelectAttrName = selectAttrList.get(i);
                            if (!fullAttrNameList.contains(stSelectAttrName)) {
                                fullAttrNameList.add(stSelectAttrName);
                            }
                        }
                    }
                }

                //是否以评估
                fullAttrNameList.add(SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate);
                //单件成本
//                    incrementAttrList.add(strSelectFullAttrName);
                String firstRevision = DomainObject.newInstance(context,strPartId).getInfo(context, SELECT_REVISION);
                BusinessObjectList allRevisionObjList = JF_Util_mxJPO.sortMapListNextRevision(context, new String[]{strPartId,firstRevision});//获取当前版本之后的版本
                int allRevisionSize = allRevisionObjList.size();
                if (Objects.nonNull(allRevisionObjList) && allRevisionSize > 1) {
                    //前一个版本的原生成本信息
                    Map<String,String> beforeRevisionMap = new HashMap<>();
                    for (int i = 1; i < allRevisionSize; i++) {
                        //同步其他版本属性的集合
                        Map saveAttrMap = new HashMap<>();
                        Map saveSelectAttrMap = new HashMap<>();
                        BusinessObject obj = allRevisionObjList.get(i);
                        DomainObject domainObj = DomainObject.newInstance(context, obj);
                        Map info = domainObj.getInfo(context, fullAttrNameList);
                        JF_LOGGER.info("info:{}", info);
                        String strEvaluatePrice = UIUtil.getValue(info, SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate);
                        //未评估才需要重新计算
                        if (ATTR_JF_VPMReferenceCost_JF_Evaluate_RANGE_N.equals(strEvaluatePrice)){
                            //只需要初始化第一次
                            if (i == 1){
                                BusinessObject before = allRevisionObjList.get(i-1);
                                DomainObject beforeObj = DomainObject.newInstance(context, before);
                                beforeRevisionMap = beforeObj.getInfo(context, StringList.create(originSelectAttrSet));
                            }
                            //遍历需要修改的原始成本
                            for (Object oAttrEntry : beforeRevisionMap.entrySet()){
                                Map.Entry attrEntry = (Map.Entry)oAttrEntry;
                                String strSelectOriginalAttrName = (String) attrEntry.getKey();
                                String strSelectOriginalAttrValue = (String) attrEntry.getValue();
                                //去除前缀和后缀
                                String strActualOriginalAttrName = strSelectOriginalAttrName.replace(ATTR_PREFIX, "").replace(ATTR_SUFFIX, "");
                                BigDecimal beforeDecimal = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strSelectOriginalAttrValue);
                                StringList selectAttrList = JF_FormalECRStaticMethod_mxJPO.getChangeAttrNameByUnitCostingAttr(strActualOriginalAttrName);
                                //计算初始成本
                                for (int i1 = 0; i1 < selectAttrList.size(); i1++) {
                                    String strIncrementAttrName = selectAttrList.get(i1);
                                    String strIncrementAttrValue = UIUtil.getValue(info, strIncrementAttrName);
                                    if (!(SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate.equals(strIncrementAttrName) || SELECT_ID.equals(strIncrementAttrName))){
                                        if (UIUtil.isNotNullAndNotEmpty(strIncrementAttrValue)){
                                            beforeDecimal = beforeDecimal.add(JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strIncrementAttrValue));
                                            beforeDecimal = beforeDecimal.setScale(2, RoundingMode.HALF_UP);
                                        }
                                    }
                                }
                                String strBeforeValue = beforeDecimal.toString();
                                JF_LOGGER.info("beforeDecimal:{}",strBeforeValue);

                                if (!(SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate.equals(strActualOriginalAttrName) || SELECT_ID.equals(strActualOriginalAttrName) || SELECT_TYPE.equals(strActualOriginalAttrName))){
                                    saveAttrMap.put(strActualOriginalAttrName, strBeforeValue);
                                    saveSelectAttrMap.put(strSelectOriginalAttrName, strBeforeValue);
                                }
                            }
                            domainObj.setAttributeValues(context, saveAttrMap);
                            //设置初始成本以便下一个版本使用
                            beforeRevisionMap = saveSelectAttrMap;
                        }else {
                            //保存当前版本的初始成本以便下一个版本使用
                            beforeRevisionMap = domainObj.getInfo(context, StringList.create(originSelectAttrSet));
                        }

                    }
                }

            }

        }
    }
    /**
    *
    *@description excel更新首版的初始成本属性时同步更新后续版本的初始成本
    *@param context
	*@param syncOriginalMap
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2026/3/11 15:35
    */
    public void syncOriginalCostingByUpdateFirstPart(Context context,  Map<String,Set<String>> syncOriginalMap) throws Exception{
        JF_LOGGER.info("-------------------------------------- syncOriginalCostingByUpdateFirstPart begin-------------------------------------------------------");
        JF_LOGGER.info("同步所有更新初始成本零件的其他版本——————————————————————————————————————————————");
        JF_LOGGER.info("syncOriginalMap:{}",syncOriginalMap);
        if (syncOriginalMap.size() > 0){
            for (Map.Entry<String, Set<String>> entry : syncOriginalMap.entrySet()) {
                String strPartId = entry.getKey();
                Set<String> updateAttrSet = entry.getValue();
                Set<String> originAttrSet = new HashSet();
                Set<String> originSelectAttrSet = new HashSet();
                //更新更新的成本
                StringList fullAttrNameList = new StringList();
                //更新的初始成本属性 需要找到后续版本的增量属性
                for (String strOriginalAttrName : updateAttrSet) {
                    if (UIUtil.isNotNullAndNotEmpty(strOriginalAttrName)){
                        originAttrSet.add(strOriginalAttrName);
                        originSelectAttrSet.add(JF_PublicMethodClass_mxJPO.buildStringInStrings(ATTR_PREFIX,strOriginalAttrName,ATTR_SUFFIX));
                        StringList selectAttrList = JF_FormalECRStaticMethod_mxJPO.getChangeAttrNameByUnitCostingAttr(strOriginalAttrName);
                        for (int i = 0; i < selectAttrList.size(); i++) {
                            String stSelectAttrName = selectAttrList.get(i);
                            if (!fullAttrNameList.contains(stSelectAttrName)) {
                                fullAttrNameList.add(stSelectAttrName);
                            }
                        }
                    }
                }
                //单件成本
                String firstRevision = DomainObject.newInstance(context,strPartId).getInfo(context,SELECT_REVISION);
//                BusinessObjectList allRevisionObjList = JF_Util_mxJPO.sortMapListInRevisionCurrent(context, new String[]{strPartId,firstRevision});
                JF_LOGGER.info("strPartId:{} firstRevision:{}",strPartId,firstRevision);
                BusinessObjectList allRevisionObjList = JF_Util_mxJPO.sortMapListFilterRevision(context, new String[]{strPartId,firstRevision});
                JF_LOGGER.info("allRevisionObjList:{}",allRevisionObjList);
                int allRevisionSize = allRevisionObjList.size();
                if (Objects.nonNull(allRevisionObjList) && allRevisionSize > 1) {
                    //前一个版本的原生成本信息
                    Map<String,String> beforeRevisionMap = new HashMap<>();
                    for (int i = 1; i < allRevisionSize; i++) {
                        //同步其他版本属性的集合
                        Map saveAttrMap = new HashMap<>();
                        Map saveSelectAttrMap = new HashMap<>();
                        BusinessObject obj = allRevisionObjList.get(i);
                        DomainObject domainObj = DomainObject.newInstance(context, obj);
                        //增量成本
                        Map info = domainObj.getInfo(context, fullAttrNameList);
                        JF_LOGGER.info("info:{}", info);
                        //只需要初始化第一次
                        if (i == 1){
                            BusinessObject before = allRevisionObjList.get(i-1);
                            DomainObject beforeObj = DomainObject.newInstance(context, before);
                            //初始成本
                            beforeRevisionMap = beforeObj.getInfo(context, StringList.create(originSelectAttrSet));
                        }
                        //遍历需要修改的原始成本
                        for (Object oAttrEntry : beforeRevisionMap.entrySet()){
                            Map.Entry attrEntry = (Map.Entry)oAttrEntry;
                            String strSelectOriginalAttrName = (String) attrEntry.getKey();
                            String strSelectOriginalAttrValue = (String) attrEntry.getValue();
                            if (strSelectOriginalAttrName.startsWith(ATTR_PREFIX) && strSelectOriginalAttrName.endsWith(ATTR_SUFFIX)){
                                //去除前缀和后缀
                                String strActualOriginalAttrName = strSelectOriginalAttrName.replace(ATTR_PREFIX, "").replace(ATTR_SUFFIX, "");
                                BigDecimal beforeDecimal = JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strSelectOriginalAttrValue);
                                StringList selectAttrList = JF_FormalECRStaticMethod_mxJPO.getChangeAttrNameByUnitCostingAttr(strActualOriginalAttrName);
                                //计算初始成本
                                for (int i1 = 0; i1 < selectAttrList.size(); i1++) {
                                    String strIncrementAttrName = selectAttrList.get(i1);
                                    String strIncrementAttrValue = UIUtil.getValue(info, strIncrementAttrName);
                                    if (!(SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate.equals(strIncrementAttrName) || SELECT_ID.equals(strIncrementAttrName))){
                                        if (UIUtil.isNotNullAndNotEmpty(strIncrementAttrValue)){
                                            beforeDecimal = beforeDecimal.add(JF_FormalECRStaticMethod_mxJPO.newInstanceBigdecimal(strIncrementAttrValue));
                                            beforeDecimal = beforeDecimal.setScale(2, RoundingMode.HALF_UP);
                                        }
                                    }
                                }
                                String strBeforeValue = beforeDecimal.toString();
                                saveAttrMap.put(strActualOriginalAttrName,strBeforeValue );
                                saveSelectAttrMap.put(strSelectOriginalAttrName, strBeforeValue);

                            }

                        }
                        JF_LOGGER.info("saveAttrMap:{}",saveAttrMap);
                        JF_LOGGER.info("saveSelectAttrMap:{}",saveSelectAttrMap);
                        domainObj.setAttributeValues(context, saveAttrMap);
                        //设置初始成本以便下一个版本使用
                        beforeRevisionMap = saveSelectAttrMap;
                    }
                }

            }

        }
        JF_LOGGER.info("-------------------------------------- syncOriginalCostingByUpdateFirstPart end-------------------------------------------------------");

    }

    /**
    *
    *@description 更新物流费
    *@param context
	*@param strProjectId
	*@param strPartId
	*@param strLogisticsFees
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2026/2/5 16:09
    */
    public void updateLogisticsFees(Context context, String strProjectId, String strPartId, String strLogisticsFees) throws Exception {
        DomainObject part = DomainObject.newInstance(context, strPartId);
        String strSelectProject = String.format(JF_FormalECRStaticMethod_mxJPO.STRING_SELECT_JFPS2VPMRELID_Where, "from.id==" + strProjectId);
        JF_LOGGER.info("strSelectProject:{}", strSelectProject);
        StringList partProjectList = part.getInfoList(context, strSelectProject);
//                String strPartProject = part.getInfoList(context, strSelectProject);
        JF_LOGGER.info("partProjectList:{}", partProjectList);
        DomainRelationship rel;
        if (Objects.nonNull(partProjectList) && partProjectList.size() > 0) {
            String strPartProject = partProjectList.get(0);
            String[] split = strPartProject.split("=");
            //防止下标越界
            String strRelId = split[split.length - 1];
            strRelId = strRelId.trim();
            JF_LOGGER.info("strRelId:{}", strRelId);

            if (UIUtil.isNotNullAndNotEmpty(strRelId)) {
                rel = DomainRelationship.newInstance(context, strRelId);
            } else {
                DomainObject project = DomainObject.newInstance(context, strProjectId);
                rel = DomainRelationship.connect(context, project, new RelationshipType(REL_JFPS2VPM), part);
            }
        }else {
            DomainObject project = DomainObject.newInstance(context, strProjectId);
            rel = DomainRelationship.connect(context, project, new RelationshipType(REL_JFPS2VPM), part);
        }
        rel.setAttributeValue(context, ATTR_JF_LogisticsFees, strLogisticsFees);

    }
    /**
    *
    *@description 根据属性名映射到xml的id 再根据id 映射到 excel中列的下标
    *@param headMap
	*@param xmlMapping
	*@param strAttrName
    *@return java.util.Map
    *@throws
    *@author CHENYAN
    *@date 2026/2/5 15:44
    */
    public static Map getRowIndexByRequiredAttribute(Map headMap, MapList xmlMapping, String strAttrName) {
        Map res = new HashMap<>();
        //防止属性和 xml中id不一致时使用
        for (int i = 0; i < xmlMapping.size(); i++) {
            Map xmlMap = (Map) xmlMapping.get(i);
            String strFieldName = (String)xmlMap.get("value");
            if (strFieldName.equals(strAttrName)){
                String strXmlColId = (String) xmlMap.get("id");
                int iRowIndex = (int) headMap.get(strXmlColId);
                res.put("index",iRowIndex);
                res.put("id",strXmlColId);
                res.put("value",strAttrName);
            }
        }
        return res;
    }
    /**
     * @param excelDataList excel 所有行
     * @param headMap       头信息
     * @return java.util.List<org.apache.poi.ss.usermodel.Row>
     * @throws
     * @description 根据数据库 id 过滤出excel中id相同行
     * @author CHENYAN
     * @date 2024/11/26 14:03
     */
    public static List<Row> getExcelRowDataByMap(String strUniqueId, List<Row> excelDataList, Map headMap) {
        List<Row> rows  = null ;
        int iRowIndex;
        //获取ID的下标映射
        if (headMap.containsKey(SELECT_ID)) {
            iRowIndex = (int) headMap.get(SELECT_ID);
        } else {
            iRowIndex = -1;
        }
        if (iRowIndex != -1){
            rows = (List) excelDataList.stream().filter(excelMap -> {
                Row excelMapData = (Row) excelMap;
                Cell idCell = excelMapData.getCell(iRowIndex);
                String strExcelDataId = (String) idCell.getStringCellValue();
                return strUniqueId.equals(strExcelDataId);
            }).collect(Collectors.toList());
        }

        return rows;
    }

    /**
     *
     *@description 获取ECR的表格头信息
     *@param sheet
     *@return java.util.Map
     *@throws
     *@author CHENYAN
     *@date 2025/6/25 13:39
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
    public static String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        cell.setCellType(CellType.STRING);
        return String.valueOf(cell.getStringCellValue().trim());
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
    *
    *@description 校验excel数据
    *@param sheet
	*@param headMap
	*@param xmlMapping
    *@return java.util.Map
    *@throws
    *@author CHENYAN
    *@date 2026/2/5 11:11
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
                                JF_LOGGER.error("strCellValue:{}", strCellValue);
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
    *
    *@description 下载初始成本Excel权限
    *@param context
    *@param args
    *@return boolean
    *@throws Exception
    *@author CHENYAN
    *@date 2026/3/10
    */
    public boolean getDownloadFormalInitialCostTableAccess(Context context, String[] args) throws Exception {
        JF_LOGGER.info("-----------------------------------------getDownloadFormalInitialCostTableAccess begin ----------------------------------------------------");
        boolean res = false;
        Map requestMap = JPO.unpackArgs(args);
        String strObjectId = (String) requestMap.get(STRING_OBJECTID);
        String strSelectTable = (String) requestMap.get(STRING_SELECT_TABLE);
        String strLoginUser = context.getUser();
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        Map ecrInfo = ecr.getInfo(context, StringList.create(SELECT_CURRENT, SELECT_TYPE));
        String strECRCurrent = (String) ecrInfo.get(SELECT_CURRENT);
        String strECRType = (String) ecrInfo.get(SELECT_TYPE);
        JF_LOGGER.info("strSelectedTable:{}",strSelectTable);
        JF_LOGGER.info("strObjectId:{}",strObjectId);
        // 下载权限：ECR状态必须是会签之后的状态
        if (TYPE_JFFormalECR.equals(strECRType) && ("Countersign".equals(strECRCurrent) || "APR".equals(strECRCurrent) || 
                "Quotation".equals(strECRCurrent) || "Complete".equals(strECRCurrent))) {
            boolean isBuyTable = JF_FormalECRStaticMethod_mxJPO.checkIsBuyTable(strSelectTable);
            boolean isMakeTable = JF_FormalECRStaticMethod_mxJPO.checkIsMakeTable(strSelectTable);
            // 获取登录用户的会签任务角色
            MapList loginUserSignTaskRole = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECRAndUser(context, new String[]{strObjectId, strLoginUser});
            if (loginUserSignTaskRole.size() > 0) {
                for (int i = 0; i < loginUserSignTaskRole.size(); i++) {
                    Map signTask = (Map) loginUserSignTaskRole.get(i);
                    String strRole = (String) signTask.get("role");
                    String strTaskCurrent  = (String) signTask.get(SELECT_CURRENT);
                    JF_LOGGER.info("strRole:{}", strRole);
                    // 采购件清单 ：Costing和采购 ICO角色下载
                    //自制件清单 三个AME角色、采购 ICO角色下载
                    if (isBuyTable){
                        if (ATTR_PROJECT_ROLE_Range_Costing.equals(strRole) ||
                                ATTR_PROJECT_ROLE_Range_PRR.equals(strRole) ||
                                ATTR_PROJECT_ROLE_Range_InternalSupplier.equals(strRole)) {
                            res = true;
                            break;
                        }
                    }
                    if (isMakeTable){
                        if (ATTR_PROJECT_ROLE_Range_AME.equals(strRole) ||
                                ATTR_PROJECT_ROLE_Range_Foam_AME.equals(strRole) ||
                                ATTR_PROJECT_ROLE_Range_Trim_AME.equals(strRole) ||
                                ATTR_PROJECT_ROLE_Range_PRR.equals(strRole) ||
                                ATTR_PROJECT_ROLE_Range_InternalSupplier.equals(strRole) ||
                                ATTR_PROJECT_ROLE_Range_Costing.equals(strRole)){
                            res = true;
                            break;
                        }
                    }

                }
            }
        }
        
        JF_LOGGER.info("res:{}", res);
        JF_LOGGER.info("-----------------------------------------getDownloadFormalInitialCostTableAccess end ----------------------------------------------------");
        return res;
    }

    /**
    *
    *@description 上传初始成本Excel权限
    *@param context
    *@param args
    *@return boolean
    *@throws Exception
    *@author CHENYAN
    *@date 2026/3/10
    */
    public boolean getUploadFormalInitialCostTableAccess(Context context, String[] args) throws Exception {
        JF_LOGGER.info("-----------------------------------------getUploadFormalInitialCostTableAccess begin ----------------------------------------------------");
        boolean res = false;
        Map requestMap = JPO.unpackArgs(args);
        String strObjectId = (String) requestMap.get(STRING_OBJECTID);
        String strSelectTable = (String) requestMap.get(STRING_SELECT_TABLE);
        String strLoginUser = context.getUser();
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        Map ecrInfo = ecr.getInfo(context, StringList.create(SELECT_CURRENT, SELECT_TYPE));
        String strECRCurrent = (String) ecrInfo.get(SELECT_CURRENT);
        String strECRType = (String) ecrInfo.get(SELECT_TYPE);
        JF_LOGGER.info("strSelectedTable:{}",strSelectTable);
        JF_LOGGER.info("strObjectId:{}",strObjectId);
        // 上传更新只能是会签状态 不判断会签任务的状态
        if (TYPE_JFFormalECR.equals(strECRType) && ("Countersign".equals(strECRCurrent)||"APR".equals(strECRCurrent))) {
            // 获取登录用户的会签任务角色
            MapList loginUserSignTaskRole = new MapList();
            loginUserSignTaskRole = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECRAndUser(context, new String[]{strObjectId, strLoginUser});
            boolean isBuyTable = JF_FormalECRStaticMethod_mxJPO.checkIsBuyTable(strSelectTable);
            boolean isMakeTable = JF_FormalECRStaticMethod_mxJPO.checkIsMakeTable(strSelectTable);
            if (loginUserSignTaskRole.size() > 0) {
                for (int i = 0; i < loginUserSignTaskRole.size(); i++) {
                    Map signTask = (Map) loginUserSignTaskRole.get(i);
                    String strRole = (String) signTask.get("role");
                    JF_LOGGER.info("strRole:{}", strRole);
                    String strTaskCurrent  = (String) signTask.get(SELECT_CURRENT);
                    // 采购件清单 ：Costing和采购角色下载
                    //自制件清单 三个AME角色、采购角色下载
                    if ("Review".equals(strTaskCurrent) || "Complete".equals(strTaskCurrent)) {
                        continue;
                    }
                    if (isBuyTable){
                        if (ATTR_PROJECT_ROLE_Range_Costing.equals(strRole) ||
                                ATTR_PROJECT_ROLE_Range_PRR.equals(strRole) ||
                                ATTR_PROJECT_ROLE_Range_InternalSupplier.equals(strRole)) {
                            res = true;
                            break;
                        }
                    }
                    if (isMakeTable){
                        if (ATTR_PROJECT_ROLE_Range_AME.equals(strRole) ||
                                ATTR_PROJECT_ROLE_Range_Foam_AME.equals(strRole) ||
                                ATTR_PROJECT_ROLE_Range_Trim_AME.equals(strRole) ||
                                ATTR_PROJECT_ROLE_Range_PRR.equals(strRole) ||
                                ATTR_PROJECT_ROLE_Range_InternalSupplier.equals(strRole) ||
                                ATTR_PROJECT_ROLE_Range_Costing.equals(strRole)){
                            res = true;
                            break;
                        }
                    }

                }
            }
        }
        
        JF_LOGGER.info("res:{}", res);
        JF_LOGGER.info("-----------------------------------------getUploadFormalInitialCostTableAccess end ----------------------------------------------------");
        return res;
    }

    /**
    *
    *@description 导出初始成本Excel
    *@param context
    *@param args 包含objectId、type、path参数
    *@return Map 导出结果
    *@throws Exception
    *@author CHENYAN
    *@date 2026/3/10
    */
    public Map FormalInitialCostDownload(Context context, String[] args) throws Exception {
        JF_LOGGER.info("----------------------------------  FormalInitialCostDownload begin --------------------------------------------------------");
        Map res = new HashMap<String, String>();
        Map paramMap = (Map) JPO.unpackArgs(args);
        String strObjectId = (String) paramMap.get("objectId");
        //选择的表格类型
        String strSelectTable = (String) paramMap.get("type");
        JF_LOGGER.info("strSelectTable:{}", strSelectTable);
        //服务路径
        String strPath = (String) paramMap.get("path");
        //模板excel路径
        String strTemplatePath = JF_PublicMethodClass_mxJPO.buildStringInStrings(strPath, TEMPLATE_EXCEL_PATH, "JFFormalInitialCost", ".xlsx");
        JF_LOGGER.info("strTemplatePath:{}", strTemplatePath);
        InputStream inputStream = null;
        FileOutputStream fos = null;
        try {
            inputStream = new FileInputStream(strTemplatePath);
            Workbook workbook = WorkbookFactory.create(inputStream);
            //设置可编辑单元格背景色
            CellStyle cellEditStyle = workbook.createCellStyle();
            //设置背景色
            cellEditStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            //必须设置 否则背景色不生效
            cellEditStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellEditStyle.setBorderTop(BorderStyle.THIN);
            cellEditStyle.setBorderBottom(BorderStyle.THIN);
            cellEditStyle.setBorderLeft(BorderStyle.THIN);
            cellEditStyle.setBorderRight(BorderStyle.THIN);
            //设置单元格边框
            CellStyle cellBorderStyle = workbook.createCellStyle();
            cellBorderStyle.setBorderTop(BorderStyle.THIN);
            cellBorderStyle.setBorderBottom(BorderStyle.THIN);
            cellBorderStyle.setBorderLeft(BorderStyle.THIN);
            cellBorderStyle.setBorderRight(BorderStyle.THIN);
            //读取第一个sheet
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow1 = sheet.getRow(0);
            Row headerRow2 = sheet.getRow(1);
            //根据角色动态添加表头列
            Map<String, Integer> excelTitleMap = new HashMap<>();
            //收集固定列的key和对象列下标
            for (int i = headerRow1.getFirstCellNum(); i < headerRow1.getLastCellNum(); i++) {
                String strCell = headerRow1.getCell(i, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK) == null ? "" : JF_ECRService_mxJPO.getCellValue(headerRow1, i);
                if (UIUtil.isNotNullAndNotEmpty(strCell)) {
                    excelTitleMap.put(strCell, i);
                }
            }
            DomainObject ecr = DomainObject.newInstance(context, strObjectId);
            String strECRProjectId = ecr.getInfo(context, "from[JFChange2Project].to.id");
            JF_LOGGER.info("strObjectId:{}",strObjectId);
            JF_LOGGER.info("strECRProjectId:{}",strECRProjectId);
            //获取登录用户信息
            String strLoginUser = context.getUser();
            //获取初始成本xml映射
            MapList initialCostMappingList = JF_PublicMethodClass_mxJPO.getFormalECRTableMapping(context, strSelectTable);
            JF_LOGGER.info("initialCostMappingList:{}", initialCostMappingList);
            MapList loginUserSignTaskRole = JF_PublicMethodClass_mxJPO.getLoginUserSignTaskRoleByECRAndUser(context, new String[]{strObjectId, strLoginUser});
            JF_LOGGER.info("loginUserSignTaskRole:{}",loginUserSignTaskRole);
            //非首版的零件select的集合
            StringList notFirstPartSelectList = new StringList();
            //获取固定列数量（从XML配置中统计isFixed=true的个数）
            int fixedColumnsCount = 0;
            for (int i = 0; i < initialCostMappingList.size(); i++) {
                Map mappingMap = (Map) initialCostMappingList.get(i);
                String strIsFixed = (String) mappingMap.get("isFixed");
                if ("true".equals(strIsFixed)) {
                    fixedColumnsCount++;
                    String strSelectAttr = (String) mappingMap.get("selectValue");
                    notFirstPartSelectList.add(strSelectAttr);
                }
            }
            JF_LOGGER.info("fixedColumnsCount:{}", fixedColumnsCount);
            
            //动态列的起始下标（固定列个数减1）
//            int currentColumnIndex = fixedColumnsCount > 0 ? fixedColumnsCount - 1 : 0;
            int currentColumnIndex = fixedColumnsCount > 0 ? fixedColumnsCount  : 0;

            
            for (int i = 0; i < initialCostMappingList.size(); i++) {
                Map mappingMap = (Map) initialCostMappingList.get(i);
                String strAttrName = (String) mappingMap.get("id");
                String strRole = (String) mappingMap.get("role");
                String strIsEdit = (String) mappingMap.get("isEdit");
                String strNlsKey = (String) mappingMap.get("cellValue");
                boolean hasRolePermission = false;

                //检查是否需要根据角色添加列
                if (UIUtil.isNotNullAndNotEmpty(strRole)) {
                    for (int j = 0; j < loginUserSignTaskRole.size(); j++) {
                        Map signTask = (Map) loginUserSignTaskRole.get(j);
                        String strUserRole = (String) signTask.get("role");
                        if (strRole.contains(strUserRole)) {
                            hasRolePermission = true;
                            break;
                        }
                    }
                    if (hasRolePermission) {
                        //添加新的表头列 - 第一行：xml id
                        Cell cell1 = headerRow1.getCell(currentColumnIndex);
                        cell1.setCellValue(strAttrName);
                        //第二行：列标题
                        Cell cell2 = headerRow2.getCell(currentColumnIndex);
                        String strHeaderName = UIUtil.isNotNullAndNotEmpty(strNlsKey) ? strNlsKey : strAttrName;
                        cell2.setCellValue(strHeaderName);
                        JF_LOGGER.info("strHeaderName:{}",strHeaderName);
                        JF_LOGGER.info("strAttrName:{}",strAttrName);
                        excelTitleMap.put(strAttrName, currentColumnIndex);
                        currentColumnIndex++;
                        String strSelectAttr = (String) mappingMap.get("selectValue");

                        //添加动态的select 属性
                        notFirstPartSelectList.add(strSelectAttr);
                    }
                }
            }
            JF_LOGGER.info("excelTitleMap:{}",excelTitleMap);
            JF_LOGGER.info("notFirstPartSelectList:{}",notFirstPartSelectList);
            //获取采购件或自制件清单数据
            MapList partDataList = getFormalInitialCostPartListData(context, strObjectId, strSelectTable, loginUserSignTaskRole,notFirstPartSelectList,strECRProjectId);
            JF_LOGGER.info("partDataList size :{}", partDataList.size());
            updateTableData(context, strSelectTable, strObjectId, partDataList, initialCostMappingList);
//            JF_LOGGER.info("ecrCostingTableData:{}",partDataList);
            //编辑table 数据写入excel
            Set existSet = new HashSet();
            for (int i = 0; i < partDataList.size(); i++) {
                Row createRow = sheet.createRow(i + 2);
                Map map = (Map) partDataList.get(i);
                createRowValue(excelTitleMap, map, createRow, cellEditStyle, cellBorderStyle,initialCostMappingList,existSet);
            }
            String strTmpPath = context.createWorkspace();
            strTmpPath = strTmpPath.endsWith("/") ? strTmpPath : JF_PublicMethodClass_mxJPO.buildStringInStrings(strTmpPath, "/");
            String strFileName = JF_PublicMethodClass_mxJPO.buildStringInStrings(String.valueOf(System.currentTimeMillis()), ".xlsx");
            String strFullPath = JF_PublicMethodClass_mxJPO.buildStringInStrings(strTmpPath, strFileName);
            JF_LOGGER.info("strFullPath:{}", strFullPath);
            fos = new FileOutputStream(strFullPath);
            workbook.write(fos);
            workbook.close();
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
        JF_LOGGER.info("----------------------------------  FormalInitialCostDownload end --------------------------------------------------------");
        return res;
    }

    /**
    *
    *@description 获取初始成本零件清单数据
    *@param context
    *@param strObjectId ECR对象ID
    *@param strSelectTable 表格类型
    *@param loginUserSignTaskRole 会签角色
    *@return MapList 零件数据列表
    *@throws Exception
    *@author CHENYAN
    *@date 2026/3/10
    */
    private MapList getFormalInitialCostPartListData(Context context, String strObjectId, String strSelectTable, MapList loginUserSignTaskRole,StringList notFirstPartSelectList,String strECRProjectId) throws Exception {
        MapList partDataList = new MapList();
        boolean isBuyTable = "JFFormalInitialCostBuy".equals(strSelectTable);
        boolean isMakeTable = "JFFormalInitialCostMake".equals(strSelectTable);
        StringList unitEditAttrNameList = JF_FormalECRStaticMethod_mxJPO.getCanEditUnitPartPriceByRoleNameList(loginUserSignTaskRole,isBuyTable,isMakeTable);

        //获取采购件或自制件清单数据
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put(STRING_OBJECTID, strObjectId);
        stringStringHashMap.put("expandLevel", "0");
        //获取初始成本select
        stringStringHashMap.put(JF_FormalECRStaticMethod_mxJPO.STRING_VERIFY_UNIT_PRICE, "true");

        MapList ecrCostingTableData = new MapList();
        if (isBuyTable) {
            stringStringHashMap.put(STRING_SELECT_TABLE, "JFFormalECRCosting");
            ecrCostingTableData = getFormalECRBuyTableData(context, JPO.packArgs(stringStringHashMap));
        } else if (isMakeTable) {
            stringStringHashMap.put(STRING_SELECT_TABLE, "JFFormalECRController");
            ecrCostingTableData = getFormalECRMakeTableData(context, JPO.packArgs(stringStringHashMap));
        }
        //记录唯一id 因为需要去重
        HashSet<String> recordUniqueIdSet = new HashSet<>();
        //处理清单数据
        for (int i = 0; i < ecrCostingTableData.size(); i++) {
            Map map = (Map) ecrCostingTableData.get(i);
            String strPartId = (String) map.get(SELECT_ID);
            String strPartRevision = (String) map.get(SELECT_REVISION);
            //判断是否为首版
            boolean isFirstRevision = JF_FormalECRStaticMethod_mxJPO.checkPartIsFirstReleaseRevision(strPartRevision);
            //不是首版时需要找到首版数据
            if (!isFirstRevision){
                //获取首版数据
                Map firstVersionData = getFirstVersionData(context, strPartId,notFirstPartSelectList);
                //覆盖数据
                map.putAll(firstVersionData);
            }
            strPartId = (String) map.get(SELECT_ID);
            if (!recordUniqueIdSet.contains(strPartId)){
                recordUniqueIdSet.add(strPartId);
                String strPartType = (String) map.get(SELECT_ATTR_JF_PartType);
                String strProcurementType = (String) map.get(SELECT_ATTR_JF_ProcurementType);
                //判断是否是供货件
                boolean isZeroPart = JF_FormalECRStaticMethod_mxJPO.checkPartIsZeroPart(context, strPartId, strECRProjectId);
                //是否有权限编辑初始成本
                boolean isEdit = JF_FormalECRStaticMethod_mxJPO.checkRoleAndPartType(loginUserSignTaskRole, strPartType, isZeroPart, strProcurementType);
                if (isEdit){
                    //是否包含isEdit key 不包含的话需要put
                    if (map.containsKey(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT)) {
                        Map editAccessMap = (Map) map.get(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT);
                        for (int i1 = 0; i1 < unitEditAttrNameList.size(); i1++) {
                            editAccessMap.put(unitEditAttrNameList.get(i1),Boolean.TRUE);
                        }
                    }else {
                        Map editAccessMap = new HashMap<>();
                        for (int i1 = 0; i1 < unitEditAttrNameList.size(); i1++) {
                            editAccessMap.put(unitEditAttrNameList.get(i1),Boolean.TRUE);
                        }
                        map.put(JF_FormalECRStaticMethod_mxJPO.STRING_ISEDIT,editAccessMap);
                    }
                }
                partDataList.add(map);
            }
        }
        return partDataList;
    }

    /**
    *
    *@description 获取零件的首版数据
    *@param context
    *@param strPartId 零件 ID
    *@return Map 首版数据
    *@throws Exception
    *@author CHENYAN
    *@date 2026/3/10
    */
    private Map getFirstVersionData(Context context, String strPartId,StringList selectAttrList) throws Exception {
        Map firstVersionData = new HashMap();
        try {
                //不是首版，获取所有版本并取第一个
                BusinessObjectList allRevisionObjList = JF_Util_mxJPO.sortMapListInRevisionCurrent(context, new String[]{strPartId});
                if (allRevisionObjList != null && allRevisionObjList.size() > 0) {
                    //取第一个版本（首版）
                    BusinessObject firstRevisionObj = allRevisionObjList.get(0);
                    DomainObject firstRevision = DomainObject.newInstance(context, firstRevisionObj);
                    firstVersionData = firstRevision.getInfo(context, selectAttrList);
                }

        } catch (Exception e) {
            JF_LOGGER.warn("Error getting first version data: {}", e.getMessage());
        }
        return firstVersionData;
    }

}
