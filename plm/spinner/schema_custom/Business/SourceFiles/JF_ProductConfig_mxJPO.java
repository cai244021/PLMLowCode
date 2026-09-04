import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.DocumentUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.PostProcessCallable;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import com.google.gson.Gson;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.Policy;
import matrix.db.RelationshipType;
import matrix.util.StringList;
import org.apache.commons.fileupload.FileItem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import static com.matrixone.apps.domain.DomainConstants.*;
import static com.matrixone.apps.domain.MultiValueSelects.RELATIONSHIP_REFERENCE_DOCUMENT;

public class JF_ProductConfig_mxJPO {
    private static final Logger LOGGER = LoggerFactory.getLogger(JF_ProductConfig_mxJPO.class);

    private static final String TYPE_PRODUCT_CONFIG_TABLE = "JFProductConfigTable";
    private static final String TYPE_DB_LIST = "JFDBList";
    private static final String TYPE_SERVICE_PARTS_LIST = "JFServicePartsList";
    private static final String TYPE_POSITION_PRODUCT = "JFPositionProduct";
    private static final String SYMBOLIC_TYPE_POSITION_PRODUCT = "type_JFPositionProduct";
    private static final String TYPE_VEHICLE_CONFIGURATION = "JFVehicleConfiguration";
    private static final String SYMBOLIC_TYPE_VEHICLE_CONFIGURATION = "type_JFVehicleConfiguration";
    private static final String TYPE_TARGET_SALES = "JFTargetSales";
    private static final String SYMBOLIC_TYPE_TARGET_SALES = "type_JFTargetSales";
    private static final String TYPE_VPM_REFERENCE = "VPMReference";
    private static final String TYPE_CONFIG_TABLE_ROUTE = "JFConfigTableRoute";
    private static final String SYMBOLIC_TYPE_CONFIG_TABLE_ROUTE = "type_JFConfigTableRoute";
    private static final String SYMBOLIC_TYPE_PRODUCT_CONFIG_TASK = "type_JF_ProductConfigTask";
    private static final String POLICY_PRODUCT_CONFIG_TABLE = "JFProductConfigTable";
    private static final String SYMBOLIC_POLICY_CONFIG_TABLE_ROUTE = "policy_JFConfigTableRoute";
    private static final String RESOURCE_MISSING_CUSTOMER_PART_MESSAGE = "emxFramework.JFProductConfigApproval.MissingCustomerPartMessage";
    private static final String RESOURCE_MISSING_CUSTOMER_PART_TOTAL = "emxFramework.JFProductConfigApproval.MissingCustomerPartTotal";
    private static final String RESOURCE_ROUTE_NODE_CHAIR_MANAGER = "emxFramework.JFProductConfigApproval.RouteNodeChairManager";
    private static final String RESOURCE_ROUTE_NODE_BUSINESS_MANAGER = "emxFramework.JFProductConfigApproval.RouteNodeBusinessManager";
    private static final String RESOURCE_ROUTE_MISSING_ROLE = "emxFramework.JFProductConfigApproval.MissingProjectRole";
    private static final String RESOURCE_ROUTE_INVALID_STATE = "emxFramework.JFProductConfigApproval.InvalidPromoteState";
    private static final String RESOURCE_ROUTE_EMPTY_OBJECT = "emxFramework.JFProductConfigApproval.EmptyApprovalObject";
    private static final String RESOURCE_ROUTE_EMPTY_PRODUCT_CONFIG = "emxFramework.JFProductConfigApproval.EmptyProductConfig";
    private static final String RESOURCE_ROUTE_EMPTY_PROJECT = "emxFramework.JFProductConfigApproval.ProductConfigWithoutProject";
    private static final String RESOURCE_ROUTE_CONTENT_NO_ACCESS = "emxFramework.JFConfigTableRouteApprovalContent.NoAccess";
    private static final String RESOURCE_ROUTE_CONTENT_INVALID_SELECTION = "emxFramework.JFConfigTableRouteApprovalContent.InvalidSelection";
    private static final String RESOURCE_ROUTE_CONTENT_DIFFERENT_PROJECT = "emxFramework.JFConfigTableRouteApprovalContent.DifferentProject";
    private static final String RESOURCE_ROUTE_CONTENT_EMPTY = "emxFramework.JFConfigTableRouteApprovalContent.Empty";
    private static final String RESOURCE_ROUTE_PRODUCT_CONFIG_QUANTITY = "emxFramework.JFConfigTableRouteApprovalContent.ProductConfigQuantity";
    private static final String RESOURCE_ROUTE_SERVICE_PARTS_LIST_QUANTITY = "emxFramework.JFConfigTableRouteApprovalContent.ServicePartsListQuantity";
    private static final String RESOURCE_DUPLICATE_PRODUCT_CONFIG_APPROVAL = "emxFramework.JFProductConfigApproval.DuplicateRoute";
    private static final String RESOURCE_DUPLICATE_SERVICE_PARTS_LIST_APPROVAL = "emxFramework.JFProductConfigApproval.DuplicateServicePartsListRoute";
    private static final String RESOURCE_QUANTITY_CHECK_TITLE = "emxFramework.JFProductConfigApproval.QuantityCheckTitle";
    private static final String RESOURCE_QUANTITY_CHECK_MINUS_ONE = "emxFramework.JFProductConfigApproval.QuantityCheckMinusOne";
    private static final String RESOURCE_QUANTITY_CHECK_ZERO_TOTAL = "emxFramework.JFProductConfigApproval.QuantityCheckZeroTotal";
    private static final String RESOURCE_CONFIG_ROUTE_DELETE_INVALID_OBJECT = "emxFramework.JFProductConfigRoute.DeleteInvalidObject";
    private static final String RESOURCE_DELETE_ONLY_IN_WORK = "emxFramework.JFProductConfigDelete.OnlyInWork";
    private static final String RESOURCE_DELETE_ONLY_OWNER = "emxFramework.JFProductConfigDelete.OnlyOwner";
    private static final String RESOURCE_PRODUCT_CONFIG_REVISION_NO_ACCESS = "emxFramework.JFProductConfigRevision.NoAccess";
    private static final String RESOURCE_PRODUCT_CONFIG_REVISION_SUCCESS = "emxFramework.JFProductConfigRevision.Success";
    private static final String RESOURCE_SERVICE_PARTS_LIST_REVISION_NO_ACCESS = "emxFramework.JFServicePartsListRevision.NoAccess";
    private static final String RESOURCE_SERVICE_PARTS_LIST_REVISION_SUCCESS = "emxFramework.JFServicePartsListRevision.Success";
    private static final String RESOURCE_REVISION_NOT_LATEST = "emxFramework.Revision.NotLatest";
    private static final String RESOURCE_TRANSFER_OWNER_NO_ACCESS = "emxFramework.JFProductConfigTransferOwner.NoAccess";
    private static final String RESOURCE_TRANSFER_OWNER_INVALID_SELECTION = "emxFramework.JFProductConfigTransferOwner.InvalidSelection";
    private static final String RESOURCE_TRANSFER_OWNER_INVALID_PERSON = "emxFramework.JFProductConfigTransferOwner.InvalidPerson";
    private static final String RESOURCE_TRANSFER_OWNER_PERSON_NO_ORGANIZATION = "emxFramework.JFProductConfigTransferOwner.PersonNoOrganization";
    private static final String RESOURCE_TRANSFER_OWNER_SUCCESS = "emxFramework.JFProductConfigTransferOwner.Success";
    private static final String RESOURCE_TRANSFER_OWNER_FAILED = "emxFramework.JFProductConfigTransferOwner.Failed";
    private static final String RESOURCE_SERVICE_PARTS_TRANSFER_OWNER_NO_ACCESS = "emxFramework.JFServicePartsListTransferOwner.NoAccess";
    private static final String RESOURCE_SERVICE_PARTS_TRANSFER_OWNER_INVALID_SELECTION = "emxFramework.JFServicePartsListTransferOwner.InvalidSelection";
    private static final String RESOURCE_SERVICE_PARTS_TRANSFER_OWNER_SUCCESS = "emxFramework.JFServicePartsListTransferOwner.Success";
    private static final String RESOURCE_SERVICE_PARTS_TRANSFER_OWNER_FAILED = "emxFramework.JFServicePartsListTransferOwner.Failed";
    private static final String POLICY_POSITION_PRODUCT = "JFPositionProduct";
    private static final String SYMBOLIC_POLICY_POSITION_PRODUCT = "policy_JFPositionProduct";
    private static final String POLICY_VEHICLE_CONFIGURATION = "JFVehicleConfiguration";
    private static final String SYMBOLIC_POLICY_VEHICLE_CONFIGURATION = "policy_JFVehicleConfiguration";
    private static final String SYMBOLIC_POLICY_TARGET_SALES = "policy_JFTargetSales";
    private static final String REL_PROJECT_TO_PRODUCT_CONFIG = "JFProject2ProductConfigTable";
    private static final String REL_PROJECT_TO_DB_LIST = "JFProject2DBList";
    private static final String REL_PROJECT_TO_SERVICE_PARTS_LIST = "JFProject2ServicePartsList";
    private static final String REL_CONFIG_ROUTE_TO_PROJECT = "JFConfigTableRoute2Project";
    private static final String REL_PROJECT_TO_SERVICE_PARTS = "JFProject2ServiceParts";
    private static final String REL_PRODUCT_CONFIG_TO_POSITION = "JFProductConfig2PositionProduct";
    private static final String REL_PRODUCT_CONFIG_TO_VEHICLE_CONFIG = "JFProductConfig2VehicleConfiguration";
    private static final String REL_PRODUCT_CONFIG_TO_TARGET_SALES = "JFProductConfigTable2TargetSales";
    private static final String REL_POSITION_TO_VEHICLE_CONFIG = "JFPositionProduct2VehicleConfiguration";
    private static final String REL_POSITION_TO_VPM = "JFPositionProduct2VPMReference";
    private static final String REL_PV_TO_VPM = "JFPVRelation2VPMReference";
    private static final String REL_PRODUCT_CONFIG_TO_VPM = "JFProductConfigTable2VPMReference";
    private static final String REL_DB_LIST_TO_VPM = "JFDBList2VPMReference";
    private static final String REL_SERVICE_PARTS_LIST_TO_VPM = "JFServicePartsList2VPMReference";
    private static final String REL_TARGET_SALES_TO_VPM = "JFTargetSales2VPMReference";
    private static final String REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG = "JFConfigTableRoute2ProductConfigTable";
    private static final String REL_PRODUCT_CONFIG_TO_PRODUCT_CONFIG_TASK = "JFProductConfigTable2ProductConfigTask";
    private static final String REL_CONFIG_ROUTE_TO_SERVICE_PARTS_LIST = "JFConfigTableRoute2ServicePartsList";
    private static final String CONFIG_RD_CENTER_DEPARTMENT_ID = "JF_RDCenterDepartment.id";
    private static final String ATTR_TITLE = DomainConstants.ATTRIBUTE_TITLE;
    private static final String ATTR_CATEGORY = "JFProductConfigCategory";
    private static final String ATTR_PROJECT_ROLE = "Project Role";
    private static final String ATTR_JFPP_TYPE = "JFPPType";
    private static final String ATTR_JFPP_TITLE = "JFPPTitle";
    private static final String JFPP_TYPE_NON_WHOLE_CHAIR = "nonWholeChair";
    private static final String ATTR_VC_INFO = "JFVCInfo";
    private static final String ATTR_ORDER = "JFOrder";
    private static final String ATTR_ASSEMBLY_LEVEL = "JFAssemblyLevel";
    private static final String ATTR_NUMBER_OF_CHAIRS = "JFNumberOfChairsInTotal";
    private static final String ATTR_OPTIONAL_OR_NOT = "JFOptionalOrNot";
    private static final String ATTR_OVERSEAS = "JFOverseas";
    private static final String ATTR_DOMESTIC = "JFDomestic";
    private static final String ATTR_OTHER = "JFOther";
    private static final String ATTR_ANNUAL_QUANTITY = "JFAnnualQuantity";
    private static final String ATTR_PROJECT_LEVEL_APR_DISCOUNT = "JFProjectLevelAPRDiscount";
    private static final String ATTR_SUMMARY_RATIO_READY = "JFProductConfigSummaryRatioReady";
    private static final String ATTR_JFSBOM_TYPE = "JFSBOMType";
    private static final String ATTR_CUSTOMER_PART_NUMBER = "JFCustomerPartNumber";
    private static final String ATTR_CUSTOMER_PART_NAME = "JFCustomerPartName";
    private static final String ATTR_DIRECT_BUY = "JF_DirectBuy";
    private static final String COLUMN_DIRECT_BUY = "DirectBuy";
    private static final String JFSBOM_TYPE_PURE_AFTER_SALES = "PureAfterSalesParts";
    private static final String JFSBOM_TYPE_SUPPLY = "SupplyParts";
    private static final String JFSBOM_TYPE_OTHER = "Other";
    private static final String STATE_IN_WORK = "InWork";
    private static final String STATE_REVIEW = "Review";
    private static final String STATE_RELEASED = "Released";
    private static final String STATE_MANAGER_FILLS_IN = "ManagerFillsIn";
    private static final String STATE_OBSOLETE = "Obsolete";
    private static final String STATE_COMPLETE = "Complete";
    private static final String SYMBOLIC_STATE_REVIEW = "state_Review";
    private static final String PROJECT_ROLE_CHAIR_MANAGER = "Chair manager";
    private static final String PROJECT_ROLE_BUSINESS_MANAGER = "Business manager";
    private static final String PROJECT_ROLE_QUESTIONS_LIST = "Questions List";
    private static final String SELECT_ATTR_TITLE = "attribute[" + ATTR_TITLE + "]";
    private static final String SELECT_ATTR_CATEGORY = "attribute[" + ATTR_CATEGORY + "]";
    private static final String SELECT_ATTR_JFPP_TYPE = "attribute[" + ATTR_JFPP_TYPE + "]";
    private static final String SELECT_ATTR_JFPP_TITLE = "attribute[" + ATTR_JFPP_TITLE + "]";
    private static final String SELECT_ATTR_VC_INFO = "attribute[" + ATTR_VC_INFO + "]";
    private static final String SELECT_ATTR_ORDER = "attribute[" + ATTR_ORDER + "]";
    private static final String SELECT_ATTR_OVERSEAS = "attribute[" + ATTR_OVERSEAS + "]";
    private static final String SELECT_ATTR_DOMESTIC = "attribute[" + ATTR_DOMESTIC + "]";
    private static final String SELECT_ATTR_OTHER = "attribute[" + ATTR_OTHER + "]";
    private static final String SELECT_ATTR_PROJECT_LEVEL_APR_DISCOUNT = "attribute[" + ATTR_PROJECT_LEVEL_APR_DISCOUNT + "]";
    private static final String SELECT_ATTR_SUMMARY_RATIO_READY = "attribute[" + ATTR_SUMMARY_RATIO_READY + "]";
    private static final String SELECT_REL_ATTR_ORDER = "attribute[" + ATTR_ORDER + "]";
    private static final String SELECT_REL_ATTR_ASSEMBLY_LEVEL = "attribute[" + ATTR_ASSEMBLY_LEVEL + "]";
    private static final String SELECT_REL_ATTR_NUMBER_OF_CHAIRS = "attribute[" + ATTR_NUMBER_OF_CHAIRS + "]";
    private static final String SELECT_REL_ATTR_OPTIONAL_OR_NOT = "attribute[" + ATTR_OPTIONAL_OR_NOT + "]";
    private static final String SELECT_REL_ATTR_ANNUAL_QUANTITY = "attribute[" + ATTR_ANNUAL_QUANTITY + "]";
    private static final String SELECT_REL_ATTR_JFSBOM_TYPE = "attribute[" + ATTR_JFSBOM_TYPE + "]";
    private static final String SELECT_REL_ATTR_CUSTOMER_PART_NUMBER = "attribute[" + ATTR_CUSTOMER_PART_NUMBER + "]";
    private static final String SELECT_REL_ATTR_CUSTOMER_PART_NAME = "attribute[" + ATTR_CUSTOMER_PART_NAME + "]";
    private static final String SELECT_REL_ATTR_DIRECT_BUY = "attribute[" + ATTR_DIRECT_BUY + "]";
    private static final String SELECT_ATTR_PART_NUMBER = "attribute[EnterpriseExtension.V_PartNumber]";
    private static final String SELECT_ATTR_PART_CN = "attribute[JF_VPMReference.JF_PartNameCN]";
    private static final String SELECT_ATTR_PART_LEVEL = "attribute[JF_VPMReference.JF_PartType]";
    private static final String SELECT_ATTR_CUSTOMER_PART_NUMBER = "attribute[CustomerPartNumber]";
    private static final String SELECT_LOGICAL_ID = "logicalid";
    private static final String SELECT_ATTR_V_IS_LAST_VERSION = "attribute[PLMReference.V_isLastVersion]";
    private static final String KEY_LATEST_PART_ID = "latestPartId";
    private static final String EXCEL_SHEET_PARTS_CONFIG = "PartsConfig";
    private static final String EXCEL_SHEET_VEHICLE_CONFIGS = "VehicleConfigs";
    private static final String EXCEL_SHEET_TARGET_SALES_SUMMARY = "TargetSalesSummary";
    private static final String EXCEL_OPERATION_ADD = "ADD";
    private static final String EXCEL_OPERATION_REMOVE = "REMOVE";
    private static final String EXCEL_OPERATION_DELETE = "DELETE";
    private static final String EXCEL_TITLE_EDIT_MARKER = "PartsConfigTitleEditV1";
    private static final int EXCEL_ROW_VEHICLE_CONFIG_ID = 0;
    private static final int EXCEL_ROW_FIELD_TYPE = 1;
    private static final int EXCEL_ROW_CONFIG_TITLE = 2;
    private static final int EXCEL_ROW_CONFIG_INFO = 3;
    private static final int EXCEL_ROW_PART_HEADER = 4;
    private static final int EXCEL_ROW_PART_DATA_START = 5;
    private static final int EXCEL_COL_POSITION_ID = 0;
    private static final int EXCEL_COL_POSITION_TITLE = 1;
    private static final int EXCEL_COL_POSITION_PART_REL_ID = 2;
    private static final int EXCEL_COL_PART_ID = 3;
    private static final int EXCEL_COL_PART_OPERATION = 4;
    private static final int EXCEL_COL_PART_NUMBER = 5;
    private static final int EXCEL_COL_PART_CN = 6;
    private static final int EXCEL_COL_CUSTOMER_PART_NUMBER = 7;
    private static final int EXCEL_COL_ASSEMBLY_LEVEL = 8;
    private static final int EXCEL_COL_MATRIX_START = 9;

    public MapList getProjectProductConfigTableList(Context context, String[] args) throws Exception {
        Map paramsMap = JPO.unpackArgs(args);
        String projectId = (String) paramsMap.get("objectId");
        if (UIUtil.isNullOrEmpty(projectId)) {
            return new MapList();
        }
        StringList busSelects = JF_Util_mxJPO.basicBolistSel();
        busSelects.add(SELECT_ATTR_TITLE);
        busSelects.add(SELECT_ATTR_CATEGORY);
        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        return projectObj.getRelatedObjects(context,
                REL_PROJECT_TO_PRODUCT_CONFIG,
                TYPE_PRODUCT_CONFIG_TABLE,
                busSelects,
                JF_Util_mxJPO.basicRellistSel(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
    }

    /**
     * 获取项目产品配置表关联的审批申请列表
     **
     * @param context
     * @param args 请求参数
     * @return MapList 审批申请列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/14 16:13
     */
    public MapList getProjectProductConfigRouteList(Context context, String[] args) throws Exception {
        Map paramsMap = JPO.unpackArgs(args);
        String projectId = (String) paramsMap.get("objectId");
        MapList resultList = new MapList();
        if (UIUtil.isNullOrEmpty(projectId)) {
            return resultList;
        }

        StringList productConfigSelects = new StringList(SELECT_ID);
        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        MapList productConfigList = projectObj.getRelatedObjects(context,
                REL_PROJECT_TO_PRODUCT_CONFIG,
                TYPE_PRODUCT_CONFIG_TABLE,
                productConfigSelects,
                new StringList(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);

        StringList routeSelects = JF_Util_mxJPO.basicBolistSel();
        routeSelects.add(SELECT_ATTR_TITLE);
        routeSelects.add("from[" + REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG + "].to.attribute[" + ATTR_TITLE + "]");
        routeSelects.add("from[" + REL_CONFIG_ROUTE_TO_SERVICE_PARTS_LIST + "].to.attribute[" + ATTR_TITLE + "]");
        routeSelects.add("from[" + DomainConstants.RELATIONSHIP_OBJECT_ROUTE + "].to.name");

        Map routeMapById = new LinkedHashMap();
        MapList directRouteList = projectObj.getRelatedObjects(context,
                REL_CONFIG_ROUTE_TO_PROJECT,
                TYPE_CONFIG_TABLE_ROUTE,
                routeSelects,
                new StringList(),
                true,
                false,
                (short) 1,
                "",
                "",
                (short) 0);
        for (Object routeObj : directRouteList) {
            Map routeMap = (Map) routeObj;
            String routeId = UIUtil.getValue(routeMap, SELECT_ID);
            if (UIUtil.isNotNullAndNotEmpty(routeId)) {
                routeMapById.put(routeId, routeMap);
            }
        }
        for (Object productConfigObj : productConfigList) {
            Map productConfigMap = (Map) productConfigObj;
            String productConfigId = UIUtil.getValue(productConfigMap, SELECT_ID);
            if (UIUtil.isNullOrEmpty(productConfigId)) {
                continue;
            }
            DomainObject productConfig = DomainObject.newInstance(context, productConfigId);
            MapList routeList = productConfig.getRelatedObjects(context,
                    REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG,
                    TYPE_CONFIG_TABLE_ROUTE,
                    routeSelects,
                    new StringList(),
                    true,
                    false,
                    (short) 1,
                    "",
                    "",
                    (short) 0);
            for (Object routeObj : routeList) {
                Map routeMap = (Map) routeObj;
                String routeId = UIUtil.getValue(routeMap, SELECT_ID);
                if (UIUtil.isNotNullAndNotEmpty(routeId) && !routeMapById.containsKey(routeId)) {
                    routeMapById.put(routeId, routeMap);
                }
            }
        }
        resultList.addAll(routeMapById.values());
        return resultList;
    }

    /**
     * 获取产品配置表审批申请关联的审批内容
     **
     * @param context
     * @param args 请求参数
     * @return MapList 产品配置表和售后件清单列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/15 16:13
     */
    public MapList getConfigTableRouteApprovalContentList(Context context, String[] args) throws Exception {
        Map paramsMap = JPO.unpackArgs(args);
        String routeId = (String) paramsMap.get("objectId");
        MapList resultList = new MapList();
        if (UIUtil.isNullOrEmpty(routeId)) {
            return resultList;
        }

        StringList busSelects = JF_Util_mxJPO.basicBolistSel();
        busSelects.add(SELECT_ATTR_TITLE);
        busSelects.add("to[" + REL_PROJECT_TO_PRODUCT_CONFIG + "," + REL_PROJECT_TO_SERVICE_PARTS_LIST + "].from.description");
        DomainObject routeObj = DomainObject.newInstance(context, routeId);
        resultList.addAll(routeObj.getRelatedObjects(context,
                REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG,
                TYPE_PRODUCT_CONFIG_TABLE,
                busSelects,
                new StringList(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0));
        resultList.addAll(routeObj.getRelatedObjects(context,
                REL_CONFIG_ROUTE_TO_SERVICE_PARTS_LIST,
                TYPE_SERVICE_PARTS_LIST,
                busSelects,
                new StringList(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0));
        return resultList;
    }

    /**
     * 添加或移除产品配置审批表关联的审批内容
     **
     * @param context
     * @param args 请求参数，包含routeId、contentIds和mode
     * @return Map 操作结果
     * @throws Exception
     * @author caipan
     * @date 2026/7/23 16:13
     */
    public Map updateConfigTableRouteApprovalContent(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String routeId = normalizeSelectValue(params.get("routeId"));
        String mode = normalizeSelectValue(params.get("mode"));
        StringList contentIds = toStringList(params.get("contentIds"));
        if (UIUtil.isNullOrEmpty(routeId) || contentIds.isEmpty()) {
            return fail(getFrameworkString(context, RESOURCE_ROUTE_CONTENT_INVALID_SELECTION, "Please select approval content."));
        }
        DomainObject routeObj = DomainObject.newInstance(context, routeId);
        StringList routeSelects = new StringList(SELECT_TYPE);
        routeSelects.add(SELECT_CURRENT);
        routeSelects.add(SELECT_OWNER);
        Map routeInfo = routeObj.getInfo(context, routeSelects);
        if (!TYPE_CONFIG_TABLE_ROUTE.equals(normalizeSelectValue(routeInfo.get(SELECT_TYPE)))
                || !STATE_IN_WORK.equals(normalizeSelectValue(routeInfo.get(SELECT_CURRENT)))
                || !context.getUser().equals(normalizeSelectValue(routeInfo.get(SELECT_OWNER)))) {
            return fail(getFrameworkString(context, RESOURCE_ROUTE_CONTENT_NO_ACCESS,
                    "Only the owner can modify approval content in the InWork state."));
        }
        Map routeRequestMap = new HashMap();
        routeRequestMap.put("objectId", routeId);
        String projectId = getConfigTableRouteProjectId(context, routeRequestMap, "");

        boolean isPush = false;
        try {
            ContextUtil.startTransaction(context, true);
            ContextUtil.pushContext(context);
            isPush = true;
            if (UIUtil.isNotNullAndNotEmpty(projectId)
                    && !routeObj.getInfoList(context, "from[" + REL_CONFIG_ROUTE_TO_PROJECT + "].to.id").contains(projectId)) {
                routeObj.addToObject(context, new RelationshipType(REL_CONFIG_ROUTE_TO_PROJECT), projectId);
            }
            if ("remove".equalsIgnoreCase(mode)) {
                Set selectedIdSet = new HashSet(contentIds);
                StringList objectSelects = new StringList(SELECT_ID);
                StringList relationshipSelects = new StringList(DomainRelationship.SELECT_ID);
                MapList relatedList = routeObj.getRelatedObjects(context,
                        REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG + "," + REL_CONFIG_ROUTE_TO_SERVICE_PARTS_LIST,
                        TYPE_PRODUCT_CONFIG_TABLE + "," + TYPE_SERVICE_PARTS_LIST,
                        objectSelects,
                        relationshipSelects,
                        false,
                        true,
                        (short) 1,
                        "",
                        "",
                        (short) 0);
                for (Object relatedObj : relatedList) {
                    Map relatedMap = (Map) relatedObj;
                    if (selectedIdSet.contains(normalizeSelectValue(relatedMap.get(SELECT_ID)))) {
                        String relationshipId = normalizeSelectValue(relatedMap.get(DomainRelationship.SELECT_ID));
                        if (UIUtil.isNotNullAndNotEmpty(relationshipId)) {
                            DomainRelationship.disconnect(context, relationshipId);
                        }
                    }
                }
            } else if ("add".equalsIgnoreCase(mode)) {
                Set existingProductConfigIds = new HashSet(routeObj.getInfoList(context,
                        "from[" + REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG + "].to.id"));
                Set existingServicePartsListIds = new HashSet(routeObj.getInfoList(context,
                        "from[" + REL_CONFIG_ROUTE_TO_SERVICE_PARTS_LIST + "].to.id"));
                for (Object contentIdObj : contentIds) {
                    String contentId = normalizeSelectValue(contentIdObj);
                    DomainObject contentObj = DomainObject.newInstance(context, contentId);
                    StringList contentSelects = new StringList(SELECT_TYPE);
                    contentSelects.add(SELECT_CURRENT);
                    Map contentInfo = contentObj.getInfo(context, contentSelects);
                    String contentType = normalizeSelectValue(contentInfo.get(SELECT_TYPE));
                    if (!STATE_IN_WORK.equals(normalizeSelectValue(contentInfo.get(SELECT_CURRENT)))
                            || !(TYPE_PRODUCT_CONFIG_TABLE.equals(contentType) || TYPE_SERVICE_PARTS_LIST.equals(contentType))) {
                        ContextUtil.abortTransaction(context);
                        return fail(getFrameworkString(context, RESOURCE_ROUTE_CONTENT_INVALID_SELECTION,
                                "Please select valid approval content in the InWork state."));
                    }
                    String relationship;
                    String projectRelationship;
                    Set existingIds;
                    String duplicateMessage;
                    if (TYPE_PRODUCT_CONFIG_TABLE.equals(contentType)) {
                        relationship = REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG;
                        projectRelationship = REL_PROJECT_TO_PRODUCT_CONFIG;
                        existingIds = existingProductConfigIds;
                        duplicateMessage = getProductConfigApprovalDuplicateMessage(context, contentId);
                    } else {
                        relationship = REL_CONFIG_ROUTE_TO_SERVICE_PARTS_LIST;
                        projectRelationship = REL_PROJECT_TO_SERVICE_PARTS_LIST;
                        existingIds = existingServicePartsListIds;
                        duplicateMessage = getServicePartsListApprovalDuplicateMessage(context, contentId);
                    }
                    if (existingIds.contains(contentId)) {
                        continue;
                    }
                    if (UIUtil.isNotNullAndNotEmpty(duplicateMessage)) {
                        ContextUtil.abortTransaction(context);
                        return fail(duplicateMessage);
                    }
                    StringList contentProjectIds = contentObj.getInfoList(context,
                            "to[" + projectRelationship + "].from.id");
                    if (UIUtil.isNullOrEmpty(projectId) && !contentProjectIds.isEmpty()) {
                        projectId = normalizeSelectValue(contentProjectIds.get(0));
                    }
                    if (UIUtil.isNullOrEmpty(projectId) || !contentProjectIds.contains(projectId)) {
                        ContextUtil.abortTransaction(context);
                        return fail(getFrameworkString(context, RESOURCE_ROUTE_CONTENT_DIFFERENT_PROJECT,
                                "The selected object does not belong to the current project."));
                    }
                    routeObj.addToObject(context, new RelationshipType(relationship), contentId);
                    existingIds.add(contentId);
                }
            } else {
                ContextUtil.abortTransaction(context);
                return fail(getFrameworkString(context, RESOURCE_ROUTE_CONTENT_INVALID_SELECTION, "Invalid operation."));
            }
            ContextUtil.commitTransaction(context);
            return success();
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 获取项目DB件清单列表
     **
     * @param context
     * @param args 请求参数
     * @return MapList 项目DB件清单列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public MapList getProjectDBList(Context context, String[] args) throws Exception {
        return getProjectSimpleList(context, args, REL_PROJECT_TO_DB_LIST, TYPE_DB_LIST);
    }

    /**
     * 获取项目售后件清单列表
     **
     * @param context
     * @param args 请求参数
     * @return MapList 项目售后件清单列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public MapList getProjectServicePartsList(Context context, String[] args) throws Exception {
        return getProjectSimpleList(context, args, REL_PROJECT_TO_SERVICE_PARTS_LIST, TYPE_SERVICE_PARTS_LIST);
    }

    /**
     * 获取项目下指定类型清单
     **
     * @param context
     * @param args 请求参数
     * @param relationship 项目到清单关系
     * @param type 清单类型
     * @return MapList 清单列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private MapList getProjectSimpleList(Context context, String[] args, String relationship, String type) throws Exception {
        Map paramsMap = JPO.unpackArgs(args);
        String projectId = (String) paramsMap.get("objectId");
        if (UIUtil.isNullOrEmpty(projectId)) {
            return new MapList();
        }
        StringList busSelects = JF_Util_mxJPO.basicBolistSel();
        busSelects.add(SELECT_ATTR_TITLE);
        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        return projectObj.getRelatedObjects(context,
                relationship,
                type,
                busSelects,
                JF_Util_mxJPO.basicRellistSel(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
    }

    /**
     * 获取DB件清单关联零件列表
     **
     * @param context
     * @param args 请求参数
     * @return MapList DB件清单关联零件列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public MapList getDBListPartList(Context context, String[] args) throws Exception {
        return getSimplePartList(context, args, REL_DB_LIST_TO_VPM);
    }

    /**
     * 获取售后件清单关联零件列表
     **
     * @param context
     * @param args 请求参数
     * @return MapList 售后件清单关联零件列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public MapList getServicePartsListPartList(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String objectId = (String) paramMap.get("objectId");
        if (UIUtil.isNullOrEmpty(objectId)) {
            return new MapList();
        }
        StringList selectList = JF_Util_mxJPO.basicBolistSel();
        selectList.add(SELECT_DESCRIPTION);
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        relSelectList.add(SELECT_REL_ATTR_JFSBOM_TYPE);
        DomainObject listObj = DomainObject.newInstance(context, objectId);
        MapList partList = listObj.getRelatedObjects(context,
                REL_SERVICE_PARTS_LIST_TO_VPM,
                TYPE_VPM_REFERENCE,
                selectList,
                relSelectList,
                false,
                true,
                (short) 1,
                "attribute[PLMReference.V_isLastVersion]=='TRUE'",
                "",
                (short) 0);
        for (Object partObj : partList) {
            Map partMap = (Map) partObj;
            partMap.put(KEY_LATEST_PART_ID, normalizeSelectValue(partMap.get(SELECT_ID)));
        }
        String projectId = listObj.getInfo(context, "to[" + REL_PROJECT_TO_SERVICE_PARTS_LIST + "].from.id");
        fillCustomerPartInfoValues(context, partList, projectId);
        return partList;
    }

    /**
     * 售后件清单InWork提交时计算售后类型
     **
     * @param context
     * @param args trigger参数，args[0]为售后件清单ID
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/7/10 16:13
     */
    public void setServicePartsListBOMTypeOnPromote(Context context, String[] args) throws Exception {
        String objectId = args != null && args.length > 0 ? args[0] : "";
        if (UIUtil.isNullOrEmpty(objectId)) {
            return;
        }
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            DomainObject servicePartsListObj = DomainObject.newInstance(context, objectId);
            String projectId = servicePartsListObj.getInfo(context, "to[" + REL_PROJECT_TO_SERVICE_PARTS_LIST + "].from.id");
            if (UIUtil.isNullOrEmpty(projectId)) {
                throw new Exception("当前售后件清单未关联项目，无法计算售后类型");
            }
            String productConfigId = getLatestProductConfigTableId(context, projectId);
            if (UIUtil.isNullOrEmpty(productConfigId)) {
                throw new Exception("当前项目未找到最新版配置表，无法计算售后类型");
            }

            MapList supplyPartList = getProductConfigSupplyPartList(context, productConfigId);
            Set<String> supplyPartLogicalIds = new HashSet<>();
            StringList supplyPartIds = new StringList();
            for (Object supplyPartObj : supplyPartList) {
                Map supplyPartMap = (Map) supplyPartObj;
                String supplyPartId = UIUtil.getValue(supplyPartMap, SELECT_ID);
                String logicalId = UIUtil.getValue(supplyPartMap, SELECT_LOGICAL_ID);
                if (UIUtil.isNotNullAndNotEmpty(supplyPartId)) {
                    supplyPartIds.add(supplyPartId);
                }
                if (UIUtil.isNotNullAndNotEmpty(logicalId)) {
                    supplyPartLogicalIds.add(logicalId);
                }
            }
            Set<String> childPartLogicalIds = getSupplyPartBOMChildLogicalIds(context, supplyPartIds);
            Map<String, StringList> relIdsByLogicalId = getServicePartsListRelIdsByLogicalId(context, servicePartsListObj);
            for (Map.Entry<String, StringList> entry : relIdsByLogicalId.entrySet()) {
                String logicalId = entry.getKey();
                String bomType = JFSBOM_TYPE_PURE_AFTER_SALES;
                if (supplyPartLogicalIds.contains(logicalId)) {
                    bomType = JFSBOM_TYPE_SUPPLY;
                } else if (childPartLogicalIds.contains(logicalId)) {
                    bomType = JFSBOM_TYPE_OTHER;
                }
                StringList relIds = entry.getValue();
                for (Object relIdObj : relIds) {
                    String relId = String.valueOf(relIdObj);
                    if (UIUtil.isNotNullAndNotEmpty(relId)) {
                        DomainRelationship.newInstance(context, relId).setAttributeValue(context, ATTR_JFSBOM_TYPE, bomType);
                    }
                }
            }
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 获取项目最新版产品配置表ID
     **
     * @param context
     * @param projectId 项目ID
     * @return String 最新版产品配置表ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/10 16:13
     */
    private String getLatestProductConfigTableId(Context context, String projectId) throws Exception {
        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        StringList selects = new StringList(SELECT_ID);
        MapList productConfigList = projectObj.getRelatedObjects(context,
                REL_PROJECT_TO_PRODUCT_CONFIG,
                TYPE_PRODUCT_CONFIG_TABLE,
                selects,
                new StringList(),
                false,
                true,
                (short) 1,
                "islast==TRUE",
                "",
                (short) 1);
        if (productConfigList.isEmpty()) {
            return "";
        }
        Map productConfigMap = (Map) productConfigList.get(0);
        return UIUtil.getValue(productConfigMap, SELECT_ID);
    }

    /**
     * 获取产品配置表供货件清单
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return MapList 供货件清单
     * @throws Exception
     * @author caipan
     * @date 2026/7/10 16:13
     */
    private MapList getProductConfigSupplyPartList(Context context, String productConfigId) throws Exception {
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        StringList selects = new StringList(SELECT_ID);
        selects.add(SELECT_LOGICAL_ID);
        return productConfigObj.getRelatedObjects(context,
                REL_PRODUCT_CONFIG_TO_VPM,
                TYPE_VPM_REFERENCE,
                selects,
                new StringList(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
    }

    /**
     * 获取供货件BOM子件logicalid集合
     **
     * @param context
     * @param supplyPartIds 供货件ID集合
     * @return Set 子件logicalid集合
     * @throws Exception
     * @author caipan
     * @date 2026/7/10 16:13
     */
    private Set<String> getSupplyPartBOMChildLogicalIds(Context context, StringList supplyPartIds) throws Exception {
        Set<String> logicalIds = new HashSet<>();
        if (supplyPartIds == null || supplyPartIds.isEmpty()) {
            return logicalIds;
        }
        StringList selects = new StringList(SELECT_ID);
        selects.add(SELECT_LOGICAL_ID);
        for (Object supplyPartIdObj : supplyPartIds) {
            String supplyPartId = String.valueOf(supplyPartIdObj);
            if (UIUtil.isNullOrEmpty(supplyPartId)) {
                continue;
            }
            DomainObject supplyPartObj = DomainObject.newInstance(context, supplyPartId);
            MapList childList = supplyPartObj.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.REL_Instance,
                    TYPE_VPM_REFERENCE,
                    selects,
                    new StringList(),
                    false,
                    true,
                    (short) 0,
                    "",
                    "",
                    (short) 0);
            for (Object childObj : childList) {
                Map childMap = (Map) childObj;
                String logicalId = UIUtil.getValue(childMap, SELECT_LOGICAL_ID);
                if (UIUtil.isNotNullAndNotEmpty(logicalId)) {
                    logicalIds.add(logicalId);
                }
            }
        }
        return logicalIds;
    }

    /**
     * 按最新版零件logicalid获取售后件清单零件关系ID
     **
     * @param context
     * @param servicePartsListObj 售后件清单对象
     * @return Map logicalid到关系ID集合
     * @throws Exception
     * @author caipan
     * @date 2026/7/10 16:13
     */
    private Map<String, StringList> getServicePartsListRelIdsByLogicalId(Context context, DomainObject servicePartsListObj) throws Exception {
        Map<String, StringList> relIdsByLogicalId = new HashMap<>();
        Set<String> lastVersionLogicalIds = new HashSet<>();
        StringList selects = new StringList(SELECT_ID);
        selects.add(SELECT_LOGICAL_ID);
        selects.add(SELECT_ATTR_V_IS_LAST_VERSION);
        StringList relSelects = JF_Util_mxJPO.basicRellistSel();
        MapList servicePartList = servicePartsListObj.getRelatedObjects(context,
                REL_SERVICE_PARTS_LIST_TO_VPM,
                TYPE_VPM_REFERENCE,
                selects,
                relSelects,
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
        for (Object servicePartObj : servicePartList) {
            Map servicePartMap = (Map) servicePartObj;
            String logicalId = UIUtil.getValue(servicePartMap, SELECT_LOGICAL_ID);
            String relId = UIUtil.getValue(servicePartMap, SELECT_RELATIONSHIP_ID);
            if (UIUtil.isNullOrEmpty(logicalId) || UIUtil.isNullOrEmpty(relId)) {
                continue;
            }
            String isLastVersion = UIUtil.getValue(servicePartMap, SELECT_ATTR_V_IS_LAST_VERSION);
            if ("TRUE".equalsIgnoreCase(isLastVersion)) {
                lastVersionLogicalIds.add(logicalId);
            }
            StringList relIds = relIdsByLogicalId.get(logicalId);
            if (relIds == null) {
                relIds = new StringList();
                relIdsByLogicalId.put(logicalId, relIds);
            }
            relIds.add(relId);
        }
        relIdsByLogicalId.keySet().retainAll(lastVersionLogicalIds);
        return relIdsByLogicalId;
    }

    /**
     * 售后件清单添加勾选零件
     **
     * @param context
     * @param args 请求参数，包含objectId和partIds
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/7/9 16:13
     */
    public void addServicePartsListAllRevisionParts(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String objectId = (String) paramMap.get("objectId");
        StringList partIds = (StringList) paramMap.get("partIds");
        if (UIUtil.isNullOrEmpty(objectId) || partIds == null || partIds.isEmpty()) {
            return;
        }
        DomainObject listObj = DomainObject.newInstance(context, objectId);
        Set<String> existingPartIds = new HashSet<>(listObj.getInfoList(context, "from[" + REL_SERVICE_PARTS_LIST_TO_VPM + "].to.id"));
        for (Object partIdObj : partIds) {
            String partId = String.valueOf(partIdObj);
            if (UIUtil.isNullOrEmpty(partId) || existingPartIds.contains(partId)) {
                continue;
            }
            LOGGER.info("partId:{}",partId);
            listObj.addToObject(context, new RelationshipType(REL_SERVICE_PARTS_LIST_TO_VPM), partId);
            existingPartIds.add(partId);
        }
    }

    /**
     * 售后件清单移除勾选零件
     **
     * @param context
     * @param args 请求参数，包含objectId和partIds
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/7/9 16:13
     */
    public void removeServicePartsListAllRevisionParts(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String objectId = (String) paramMap.get("objectId");
        StringList partIds = (StringList) paramMap.get("partIds");
        if (UIUtil.isNullOrEmpty(objectId) || partIds == null || partIds.isEmpty()) {
            return;
        }
        DomainObject listObj = DomainObject.newInstance(context, objectId);
        StringList busSelects = new StringList(SELECT_ID);
        StringList relSelects = new StringList(DomainRelationship.SELECT_ID);
        MapList relatedObjects = listObj.getRelatedObjects(context,
                REL_SERVICE_PARTS_LIST_TO_VPM,
                TYPE_VPM_REFERENCE,
                busSelects,
                relSelects,
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
        Map<String, String> partRelIdMap = new HashMap<>();
        for (Object relatedObj : relatedObjects) {
            Map relatedMap = (Map) relatedObj;
            partRelIdMap.put((String) relatedMap.get(SELECT_ID), (String) relatedMap.get(DomainRelationship.SELECT_ID));
        }
        StringList relIds = new StringList();
        for (Object partIdObj : partIds) {
            String partId = String.valueOf(partIdObj);
            String relId = partRelIdMap.get(partId);
            if (UIUtil.isNotNullAndNotEmpty(relId) && !relIds.contains(relId)) {
                relIds.add(relId);
            }
        }
        if (!relIds.isEmpty()) {
            ContextUtil.pushContext(context);
            try {
                DomainRelationship.disconnect(context, relIds.toStringArray());
            } finally {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 售后件清单一键添加项目最新版产品配置表关联的最新版供货件，同一logicalid的零件仅添加一次
     **
     * @param context
     * @param args 请求参数，包含objectId
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/7/9 16:13
     */
    public void addProjectSupplyPartsToServicePartsList(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String objectId = (String) paramMap.get("objectId");
        if (UIUtil.isNullOrEmpty(objectId)) {
            return;
        }
        DomainObject listObj = DomainObject.newInstance(context, objectId);
        String projectId = listObj.getInfo(context, "to[" + REL_PROJECT_TO_SERVICE_PARTS_LIST + "].from.id");
        if (UIUtil.isNullOrEmpty(projectId)) {
            throw new Exception("当前售后件清单未关联项目，无法添加供货件");
        }
        //20260828 update by caipan 改为读取项目最新版产品配置表关联的供货件，并统一转换为零件最新版
        String productConfigId = getLatestProductConfigTableId(context, projectId);
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            throw new Exception("当前项目未关联最新版产品配置表，无法添加供货件");
        }
        StringList latestSupplyPartIds = getLatestDirectPartIds(context,
                productConfigId, REL_PRODUCT_CONFIG_TO_VPM);
        if (latestSupplyPartIds.isEmpty()) {
            throw new Exception("当前项目最新版产品配置表未关联供货件");
        }
        StringList busSelects = new StringList(SELECT_ID);
        busSelects.add(SELECT_LOGICAL_ID);
        MapList supplyPartList = DomainObject.getInfo(context,
                latestSupplyPartIds.toStringArray(), busSelects);
        Set<String> existingPartIds = new HashSet<>(listObj.getInfoList(context, "from[" + REL_SERVICE_PARTS_LIST_TO_VPM + "].to.id"));
        //20260828 update by caipan 按logicalid识别清单中已关联的同一零件，避免重复添加不同版本
        Set<String> existingPartLogicalIds = new HashSet<>(listObj.getInfoList(context,
                "from[" + REL_SERVICE_PARTS_LIST_TO_VPM + "].to.logicalid"));
        for (Object supplyPartObj : supplyPartList) {
            Map supplyPartMap = (Map) supplyPartObj;
            String supplyPartId = (String) supplyPartMap.get(SELECT_ID);
            String supplyPartLogicalId = normalizeSelectValue(supplyPartMap.get(SELECT_LOGICAL_ID));
            if (UIUtil.isNullOrEmpty(supplyPartId)
                    || existingPartIds.contains(supplyPartId)
                    || (UIUtil.isNotNullAndNotEmpty(supplyPartLogicalId)
                    && existingPartLogicalIds.contains(supplyPartLogicalId))) {
                continue;
            }
            listObj.addToObject(context, new RelationshipType(REL_SERVICE_PARTS_LIST_TO_VPM), supplyPartId);
            existingPartIds.add(supplyPartId);
            if (UIUtil.isNotNullAndNotEmpty(supplyPartLogicalId)) {
                existingPartLogicalIds.add(supplyPartLogicalId);
            }
        }
    }

    /**
     * 获取零件所有大版本ID
     **
     * @param context
     * @param partId 零件ID
     * @return StringList 零件所有大版本ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/9 16:13
     */
    private StringList getMajorRevisionIds(Context context, String partId) throws Exception {
        StringList revisionIds = new StringList();
        if (UIUtil.isNullOrEmpty(partId)) {
            return revisionIds;
        }
        DomainObject partObj = DomainObject.newInstance(context, partId);
        BusinessObjectList majorRevisions = partObj.getMajorRevisions(context);
        for (int i = 0; i < majorRevisions.size(); i++) {
            DomainObject revisionObj = (DomainObject) majorRevisions.get(i);
            String revisionId = revisionObj.getInfo(context, SELECT_ID);
            if (UIUtil.isNotNullAndNotEmpty(revisionId) && !revisionIds.contains(revisionId)) {
                revisionIds.add(revisionId);
            }
        }
        return revisionIds;
    }

    /**
     * 获取清单关联零件列表
     **
     * @param context
     * @param args 请求参数
     * @param relationship 清单到零件关系
     * @return MapList 清单关联零件列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private MapList getSimplePartList(Context context, String[] args, String relationship) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String objectId = (String) paramMap.get("objectId");
        if (UIUtil.isNullOrEmpty(objectId)) {
            return new MapList();
        }
        StringList selectList = JF_Util_mxJPO.basicBolistSel();
        selectList.add(SELECT_DESCRIPTION);
        DomainObject listObj = DomainObject.newInstance(context, objectId);
        return listObj.getRelatedObjects(context,
                relationship,
                TYPE_VPM_REFERENCE,
                selectList,
                JF_Util_mxJPO.basicRellistSel(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
    }

    /**
     * DB件清单零件搜索过滤
     **
     * @param context
     * @param args 请求参数
     * @return StringList 可选择零件ID列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public StringList includeDBListParts(Context context, String[] args) throws Exception {
        return includeSimpleListParts(context, args, REL_DB_LIST_TO_VPM);
    }

    /**
     * 售后件清单零件搜索过滤
     **
     * @param context
     * @param args 请求参数
     * @return StringList 可选择零件ID列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public StringList includeServicePartsListParts(Context context, String[] args) throws Exception {
        return includeSimpleListParts(context, args, REL_SERVICE_PARTS_LIST_TO_VPM);
    }

    /**
     * 清单零件搜索过滤
     **
     * @param context
     * @param args 请求参数
     * @param relationship 清单到零件关系
     * @return StringList 可选择零件ID列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private StringList includeSimpleListParts(Context context, String[] args, String relationship) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String objectId = (String) paramMap.get("objectId");
        StringList result = new StringList();
        StringList selectList = new StringList(SELECT_ID);
        if (UIUtil.isNullOrEmpty(objectId)) {
            MapList partMapList = DomainObject.findObjects(context, TYPE_VPM_REFERENCE, "*", "", selectList);
            for (Object partObj : partMapList) {
                result.add((String) ((Map) partObj).get(SELECT_ID));
            }
            return result;
        }
        DomainObject listObj = DomainObject.newInstance(context, objectId);
        StringList existingPartIds = listObj.getInfoList(context, "from[" + relationship + "].to.id");
        MapList partMapList = DomainObject.findObjects(context, TYPE_VPM_REFERENCE, "*", "", selectList);
        for (Object partObj : partMapList) {
            String partId = (String) ((Map) partObj).get(SELECT_ID);
            if (!existingPartIds.contains(partId)) {
                result.add(partId);
            }
        }
        return result;
    }

    /**
     * 获取产品配置表关联零件最新版清单
     **
     * @param context
     * @param args 请求参数
     * @return MapList 产品配置表关联零件最新版清单
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public MapList getProductConfigLatestPartList(Context context, String[] args) throws Exception {
        return getProductConfigLatestPartList(context, args, "");
    }

    /**
     * 获取产品配置表关联GX零件最新版清单
     **
     * @param context
     * @param args 请求参数
     * @return MapList 产品配置表关联GX零件最新版清单
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public MapList getProductConfigLatestGXPartList(Context context, String[] args) throws Exception {
        return getProductConfigLatestPartList(context, args, "X");
    }

    /**
     * 获取产品配置表关联GC零件最新版清单
     **
     * @param context
     * @param args 请求参数
     * @return MapList 产品配置表关联GC零件最新版清单
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public MapList getProductConfigLatestGCPartList(Context context, String[] args) throws Exception {
        return getProductConfigLatestPartList(context, args, "!X");
    }

    /**
     * 获取产品配置表关联零件最新版清单
     **
     * @param context
     * @param args 请求参数
     * @param partType 零件类型过滤值，!开头时表示排除该类型
     * @return MapList 产品配置表关联零件最新版清单
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private MapList getProductConfigLatestPartList(Context context, String[] args, String partType) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String productConfigId = (String) paramMap.get("objectId");
        MapList result = new MapList();
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            return result;
        }
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        StringList busSelects = JF_Util_mxJPO.basicBolistSel();
        busSelects.add(SELECT_ATTR_PART_NUMBER);
        busSelects.add(SELECT_ATTR_PART_LEVEL);
        MapList partList = productConfigObj.getRelatedObjects(context,
                REL_PRODUCT_CONFIG_TO_VPM,
                TYPE_VPM_REFERENCE,
                busSelects,
                JF_Util_mxJPO.basicRellistSel(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
        StringList partIds = new StringList();
        for (Object partObj : partList) {
            String partId = normalizeSelectValue(((Map) partObj).get(SELECT_ID));
            if (UIUtil.isNotNullAndNotEmpty(partId)) {
                partIds.add(partId);
            }
        }
        Map latestPartIdMap = getLatestPartIdMap(context, partIds);
        Set latestPartIds = new HashSet();
        for (Object partObj : partList) {
            Map partMap = (Map) partObj;
            String partId = (String) partMap.get(SELECT_ID);
            String latestPartId = normalizeSelectValue(latestPartIdMap.get(partId));
            if (UIUtil.isNullOrEmpty(latestPartId)) {
                latestPartId = partId;
            }
            if (UIUtil.isNotNullAndNotEmpty(latestPartId) && latestPartIds.add(latestPartId)) {
                if (UIUtil.isNotNullAndNotEmpty(partType) && !isVpmReferencePartType(context, latestPartId, partType)) {
                    continue;
                }
                Map map = new HashMap();
                map.put(SELECT_ID, latestPartId);
                map.put("id", latestPartId);
                map.put(SELECT_TYPE, TYPE_VPM_REFERENCE);
                map.put(KEY_LATEST_PART_ID, latestPartId);
                result.add(map);
            }
        }
        fillCustomerPartInfoValues(context, result, getProjectIdByProductConfig(context, productConfigId));
        result.sort(DomainObject.SELECT_NAME, "ascending", "String");
        return result;
    }

    /**
     * 判断物理产品零件类型
     **
     * @param context
     * @param partId 零件ID
     * @param partType 零件类型，!开头时表示排除该类型
     * @return boolean 零件类型满足过滤条件时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private boolean isVpmReferencePartType(Context context, String partId, String partType) throws Exception {
        if (UIUtil.isNullOrEmpty(partId) || UIUtil.isNullOrEmpty(partType)) {
            return false;
        }
        String currentPartType = DomainObject.newInstance(context, partId).getInfo(context, SELECT_ATTR_PART_LEVEL);
        if (partType.startsWith("!")) {
            return !partType.substring(1).equals(currentPartType);
        }
        return partType.equals(currentPartType);
    }

    /**
     * 产品配置表新建按钮显示权限
     **
     * @param context
     * @param args 请求参数
     * @return boolean 当前项目下不存在产品配置表时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public boolean isCreateProductConfigTableCmdAccess(Context context, String[] args) throws Exception {
        return isCreateProjectSimpleListCmdAccess(context, args, REL_PROJECT_TO_PRODUCT_CONFIG, TYPE_PRODUCT_CONFIG_TABLE)&&new JF_DataOutSource_mxJPO().getJFDataOutSourceAccess(context,args);
    }

    /**
     * DB件清单新建按钮显示权限
     **
     * @param context
     * @param args 请求参数
     * @return boolean 当前项目下不存在DB件清单时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public boolean isCreateDBListCmdAccess(Context context, String[] args) throws Exception {
        return isCreateProjectSimpleListCmdAccess(context, args, REL_PROJECT_TO_DB_LIST, TYPE_DB_LIST);
    }

    /**
     * 售后件清单新建按钮显示权限
     **
     * @param context
     * @param args 请求参数
     * @return boolean 当前项目下不存在售后件清单时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public boolean isCreateServicePartsListCmdAccess(Context context, String[] args) throws Exception {

        return isCreateProjectSimpleListCmdAccess(context, args, REL_PROJECT_TO_SERVICE_PARTS_LIST, TYPE_SERVICE_PARTS_LIST)&&new JF_DataOutSource_mxJPO().getJFDataOutSourceAccess(context,args);
    }

    /**
     * 项目下单对象清单新建按钮显示权限
     **
     * @param context
     * @param args 请求参数
     * @param relationship 项目到清单关系
     * @param type 清单类型
     * @return boolean 当前项目下不存在指定清单时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private boolean isCreateProjectSimpleListCmdAccess(Context context, String[] args, String relationship, String type) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        String projectId = getProjectIdFromProgramMap(programMap);
        if (UIUtil.isNullOrEmpty(projectId)) {
            return true;
        }
        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        MapList productConfigList = projectObj.getRelatedObjects(context,
                relationship,
                type,
                new StringList(SELECT_ID),
                new StringList(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 1);
        return productConfigList.isEmpty();
    }

    /**
     * 产品配置表或售后件清单提交审批按钮显示权限
     **
     * @param context
     * @param args 请求参数
     * @return boolean 当前toolbar最新版清单处于工作中、所有者是当前用户且具备研发权限时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public boolean isDeleteProductConfigTableCmdAccess(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        String toolbar = getFirstValue(programMap.get("toolbar"));
        Map requestMap = (Map) programMap.get("requestMap");
        if (UIUtil.isNullOrEmpty(toolbar) && requestMap != null) {
            toolbar = getFirstValue(requestMap.get("toolbar"));
        }
        //20260828 update by caipan 售后件清单toolbar复用提交审批按钮时按售后件清单校验入口权限
        boolean hasAccess = "JFProductConfigServicePartsListToolbar".equals(toolbar)
                ? isDeleteProjectSimpleListCmdAccess(context, args, REL_PROJECT_TO_SERVICE_PARTS_LIST, TYPE_SERVICE_PARTS_LIST)
                : isDeleteProjectSimpleListCmdAccess(context, args, REL_PROJECT_TO_PRODUCT_CONFIG, TYPE_PRODUCT_CONFIG_TABLE);
        return hasAccess && new JF_DataOutSource_mxJPO().getJFDataOutSourceAccess(context, args);
    }

    /**
     * DB件清单删除按钮显示权限
     **
     * @param context
     * @param args 请求参数
     * @return boolean 当前项目下DB件清单处于工作中且所有者是当前用户时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public boolean isDeleteDBListCmdAccess(Context context, String[] args) throws Exception {
        return isDeleteProjectSimpleListCmdAccess(context, args, REL_PROJECT_TO_DB_LIST, TYPE_DB_LIST);
    }

    /**
     * 售后件清单删除按钮显示权限
     **
     * @param context
     * @param args 请求参数
     * @return boolean 当前项目下售后件清单处于工作中且所有者是当前用户时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public boolean isDeleteServicePartsListCmdAccess(Context context, String[] args) throws Exception {
        return isDeleteProjectSimpleListCmdAccess(context, args, REL_PROJECT_TO_SERVICE_PARTS_LIST, TYPE_SERVICE_PARTS_LIST);
    }

    /**
     * 项目下单对象清单删除按钮显示权限
     **
     * @param context
     * @param args 请求参数
     * @param relationship 项目到清单关系
     * @param type 清单类型
     * @return boolean 当前项目下指定清单处于工作中且所有者是当前用户时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private boolean isDeleteProjectSimpleListCmdAccess(Context context, String[] args, String relationship, String type) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        String projectId = getProjectIdFromProgramMap(programMap);
        if (UIUtil.isNullOrEmpty(projectId)) {
            return false;
        }
        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        StringList selects = new StringList();
        selects.add(SELECT_ID);
        selects.add(SELECT_OWNER);
        selects.add(DomainConstants.SELECT_CURRENT);
        selects.add(SELECT_IS_LAST);
        String where = "islast==TRUE";
        MapList simpleList = projectObj.getRelatedObjects(context,
                relationship,
                type,
                selects,
                new StringList(),
                false,
                true,
                (short) 1,
                where,
                "",
                1);
        LOGGER.info("simpleList:{}",simpleList);
        if (simpleList.isEmpty()) {
            return false;
        }
        Map simpleMap = (Map) simpleList.get(0);
        String owner = UIUtil.getValue(simpleMap, SELECT_OWNER);
        String current =  UIUtil.getValue(simpleMap, SELECT_CURRENT);
        return "InWork".equals(current) && context.getUser().equals(owner);
    }

    /**
     * 从命令上下文中获取项目ID
     **
     * @param programMap 命令参数
     * @return String 项目ID
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private String getProjectIdFromProgramMap(Map programMap) {
        String projectId = getFirstValue(programMap.get("objectId"));
        Map requestMap = (Map) programMap.get("requestMap");
        if (UIUtil.isNullOrEmpty(projectId) && requestMap != null) {
            projectId = getFirstValue(requestMap.get("objectId"));
        }
        if (UIUtil.isNullOrEmpty(projectId) && requestMap != null) {
            projectId = getFirstValue(requestMap.get("parentOID"));
        }
        return projectId;
    }

    /**
     * 删除产品配置相关清单或审批申请
     **
     * @param context
     * @param args 请求参数
     * @throws Exception
     * @author caipan
     * @date 2026/7/15 16:13
     */
    public void deleteProjectConfigLists(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        StringList objectIds = normalizeIdList(params.get("objectIds"));
        String expectedType = normalizeSelectValue(params.get("expectedType"));
        if (objectIds.isEmpty()) {
            throw new Exception(getFrameworkString(context, "emxComponents.Common.PleaseSelectAnItem", "Please select an item."));
        }
        for (Object objectIdObj : objectIds) {
            String objectId = normalizeSelectValue(objectIdObj);
            checkProjectConfigListDeleteAccess(context, objectId, expectedType);
        }
        ContextUtil.startTransaction(context, true);
        try {
            ContextUtil.pushContext(context);
            try {
                for (Object objectIdObj : objectIds) {
                    String objectId = normalizeSelectValue(objectIdObj);
                    deleteProjectConfigList(context, objectId);
                }
            } finally {
                ContextUtil.popContext(context);
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("deleteProjectConfigLists error", e);
            throw e;
        }
    }

    /**
     * 按勾选对象状态和Owner校验产品配置相关清单或审批申请删除权限
     **
     * @param context
     * @param objectId 清单或审批申请ID
     * @param expectedType 删除入口要求的对象类型
     * @throws Exception
     * @author caipan
     * @date 2026/7/20 16:13
     */
    private void checkProjectConfigListDeleteAccess(Context context, String objectId, String expectedType) throws Exception {
        if (UIUtil.isNullOrEmpty(objectId)) {
            throw new Exception(getFrameworkString(context, "emxComponents.Common.PleaseSelectAnItem", "Please select an item."));
        }
        DomainObject object = DomainObject.newInstance(context, objectId);
        StringList selects = new StringList();
        selects.add(SELECT_TYPE);
        selects.add(SELECT_OWNER);
        selects.add(DomainConstants.SELECT_CURRENT);
        Map info = object.getInfo(context, selects);
        String type = normalizeSelectValue(info.get(SELECT_TYPE));
        String owner = normalizeSelectValue(info.get(SELECT_OWNER));
        String current = normalizeSelectValue(info.get(DomainConstants.SELECT_CURRENT));
        boolean isConfigTableRoute = TYPE_CONFIG_TABLE_ROUTE.equals(type);
        if (UIUtil.isNotNullAndNotEmpty(expectedType) && !expectedType.equals(type)) {
            throw new Exception(getFrameworkString(context, RESOURCE_CONFIG_ROUTE_DELETE_INVALID_OBJECT,
                    "Only product config approval requests can be deleted from this list."));
        }
        if (!TYPE_PRODUCT_CONFIG_TABLE.equals(type) && !TYPE_DB_LIST.equals(type) && !TYPE_SERVICE_PARTS_LIST.equals(type) && !isConfigTableRoute) {
            throw new Exception(getFrameworkString(context, "emxFramework.Message.InvalidObject", "Invalid object."));
        }
        boolean validateSelectedObject = TYPE_PRODUCT_CONFIG_TABLE.equals(expectedType)
                || TYPE_SERVICE_PARTS_LIST.equals(expectedType) || TYPE_CONFIG_TABLE_ROUTE.equals(expectedType);
        if (validateSelectedObject) {
            if (!STATE_IN_WORK.equals(current)) {
                throw new Exception(getFrameworkString(context, RESOURCE_DELETE_ONLY_IN_WORK,
                        "Only objects in the InWork state can be deleted."));
            }
            if (!context.getUser().equals(owner)) {
                throw new Exception(getFrameworkString(context, RESOURCE_DELETE_ONLY_OWNER,
                        "Only the owner can delete the selected object."));
            }
        } else if (!STATE_IN_WORK.equals(current) || !context.getUser().equals(owner)) {
            throw new Exception(getFrameworkString(context, "emxFramework.Message.NoDeleteAccess",
                    "Only the owner can delete in-work data."));
        }
    }

    /**
     * 删除产品配置相关清单或审批申请对象
     **
     * @param context
     * @param objectId 清单或审批申请ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/15 16:13
     */
    private void deleteProjectConfigList(Context context, String objectId) throws Exception {
        DomainObject object = DomainObject.newInstance(context, objectId);
        String type = object.getInfo(context, SELECT_TYPE);
        if (TYPE_PRODUCT_CONFIG_TABLE.equals(type)) {
            deleteProductConfigOwnedObjects(context, objectId);
        }
        object.deleteObject(context);
    }

    /**
     * 删除产品配置表下属业务对象
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private void deleteProductConfigOwnedObjects(Context context, String productConfigId) throws Exception {
        StringList deleteIds = new StringList();
        Set handledIds = new HashSet();
        addRelatedObjectIds(context, productConfigId, REL_PRODUCT_CONFIG_TO_TARGET_SALES, TYPE_TARGET_SALES, deleteIds, handledIds);
        addRelatedObjectIds(context, productConfigId, REL_PRODUCT_CONFIG_TO_VEHICLE_CONFIG, TYPE_VEHICLE_CONFIGURATION, deleteIds, handledIds);
        addRelatedObjectIds(context, productConfigId, REL_PRODUCT_CONFIG_TO_POSITION, TYPE_POSITION_PRODUCT, deleteIds, handledIds);
        if (!deleteIds.isEmpty()) {
            DomainObject.deleteObjects(context, deleteIds.toStringArray());
        }
    }

    /**
     * 收集待删除的关联对象ID
     **
     * @param context
     * @param objectId 基础对象ID
     * @param relationship 关系
     * @param type 类型
     * @param deleteIds 待删除ID集合
     * @param handledIds 已处理ID集合
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private void addRelatedObjectIds(Context context, String objectId, String relationship, String type, StringList deleteIds, Set handledIds) throws Exception {
        DomainObject object = DomainObject.newInstance(context, objectId);
        MapList relatedObjects = object.getRelatedObjects(context,
                relationship,
                type,
                new StringList(SELECT_ID),
                new StringList(),
                false,
                true,
                (short) 1,
                "",
                "",
                0);
        for (Object relatedObj : relatedObjects) {
            Map relatedMap = (Map) relatedObj;
            String relatedId = normalizeSelectValue(relatedMap.get(SELECT_ID));
            if (UIUtil.isNotNullAndNotEmpty(relatedId) && !handledIds.contains(relatedId)) {
                deleteIds.add(relatedId);
                handledIds.add(relatedId);
            }
        }
    }

    @PostProcessCallable
    public void connectProjectAfterCreate(Context context, String[] args) throws Exception {
        Map parameter = JPO.unpackArgs(args);
        Map requestMap = (Map) parameter.get("requestMap");
        Map paramMap = (Map) parameter.get("paramMap");
        String projectId = getFirstValue(requestMap.get("parentOID"));
        if (UIUtil.isNullOrEmpty(projectId)) {
            projectId = getFirstValue(requestMap.get("objectId"));
        }
        String newObjectId = getFirstValue(paramMap.get("newObjectId"));
        if (UIUtil.isNullOrEmpty(projectId) || UIUtil.isNullOrEmpty(newObjectId)) {
            throw new Exception("Failed to connect product config table to project: project or new object is empty.");
        }
        String questionListFileId = getFirstValue(requestMap.get("JFECRQQFileId2"));
        ContextUtil.startTransaction(context, true);
        boolean isPush = false;
        try {
            DomainObject productConfig = DomainObject.newInstance(context, newObjectId);
            String objectType = productConfig.getInfo(context, SELECT_TYPE);
            String relationship = getProjectListRelationship(objectType);
            if (UIUtil.isNullOrEmpty(relationship)) {
                throw new Exception("Unsupported project list type: " + objectType);
            }
            ContextUtil.pushContext(context);
            isPush  = true;
            DomainRelationship.connect(context,
                    DomainObject.newInstance(context, projectId),
                    relationship,
                    productConfig);
            connectQuestionListAttachment(context, productConfig, questionListFileId);
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        }finally {
            if(isPush){
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 获取项目到清单关系
     **
     * @param objectType 清单对象类型
     * @return String 项目到清单关系
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private String getProjectListRelationship(String objectType) {
        if (TYPE_PRODUCT_CONFIG_TABLE.equals(objectType)) {
            return REL_PROJECT_TO_PRODUCT_CONFIG;
        }
        if (TYPE_DB_LIST.equals(objectType)) {
            return REL_PROJECT_TO_DB_LIST;
        }
        if (TYPE_SERVICE_PARTS_LIST.equals(objectType)) {
            return REL_PROJECT_TO_SERVICE_PARTS_LIST;
        }
        return "";
    }

    public String getCreateProjectName(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        String projectId = getFirstValue(requestMap.get("parentOID"));
        if (UIUtil.isNullOrEmpty(projectId)) {
            projectId = getFirstValue(requestMap.get("objectId"));
        }
        if (UIUtil.isNullOrEmpty(projectId)) {
            return "";
        }
        DomainObject projectObj = DomainObject.newInstance(context, projectId);
        String projectName = projectObj.getInfo(context, SELECT_DESCRIPTION);
        if (UIUtil.isNullOrEmpty(projectName)) {
            projectName = projectObj.getInfo(context, SELECT_NAME);
        }
        return "<input type=\"text\" readonly=\"readonly\" value=\"" + escapeHtml(projectName) + "\" />";
    }

    /**
     * 获取附件上传占位字段
     **
     * @param context
     * @param args 请求参数
     * @return String 附件上传字段HTML
     * @throws Exception
     * @author caipan
     * @date 2026/8/3 16:13
     */
    public String getAttachmentPlaceholder(Context context, String[] args) throws Exception {
        String strLang = context.getSession().getLanguage();
        String strFileUploadNls = ComponentsUtil.i18nStringNow("emxComponents.Common.JFECRQQFileUpload", strLang);
        StringBuffer sbDocDown = new StringBuffer();
        sbDocDown.append("<table><tr>");
        sbDocDown.append("<td>");
        sbDocDown.append("<input value=\"\" id=\"JFQuestionsList\" name=\"JFQuestionsList\" type=\"text\" size=\"20\" readonly=\"readonly\" style=\"background-color:#ebebe4;\"/>");
        sbDocDown.append("</td>");
        sbDocDown.append("<td id=\"fileTd2\">");
        sbDocDown.append("<input id=\"JFECRQQFileId2\" name=\"JFECRQQFileId2\" type=\"hidden\"/>");
        sbDocDown.append("<input onclick=\"javascript:showModalDialog('../components/JF_ECRQuestionsListPreCheckin.jsp?objectAction=checkin&amp;msfBypass=true&amp;objectId=;','730','450')\"");
        sbDocDown.append(" value=\"");
        sbDocDown.append(escapeHtml(strFileUploadNls));
        sbDocDown.append("\" type=\"button\"/>");
        sbDocDown.append("</td>");
        sbDocDown.append("<td id=\"JFReplace2\">");
        sbDocDown.append("</td>");
        sbDocDown.append("</tr></table>");
        return sbDocDown.toString();
    }

    /**
     * 产品配置表审批申请产品配置表字段
     **
     * @param context
     * @param args 请求参数
     * @return String 产品配置表搜索字段HTML
     * @throws Exception
     * @author caipan
     * @date 2026/7/13 16:13
     */
    public String getConfigTableRouteProductConfigField(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        String selectedObjectId = requestMap == null ? "" : getObjectIdFromTableRowId(getFirstValue(requestMap.get("emxTableRowId")));
        String selectedObjectType = UIUtil.isNullOrEmpty(selectedObjectId) ? ""
                : DomainObject.newInstance(context, selectedObjectId).getInfo(context, SELECT_TYPE);
        //20260828 update by caipan 仅从产品配置表入口发起时自动带入产品配置表
        String productConfigId = TYPE_PRODUCT_CONFIG_TABLE.equals(selectedObjectType) ? selectedObjectId : "";
        String productConfigDisplay = getObjectDisplayValue(context, productConfigId);
        String projectId = getConfigTableRouteProjectId(context, requestMap, selectedObjectId);
        return buildConfigTableRouteChooserHtml("JFApprovalProductConfigId","CURRENT=policy_JFProductConfigTable.state_InWork",
                "JFApprovalProductConfigDisplay",
                productConfigId,
                productConfigDisplay,
                "type_JFProductConfigTable",
                REL_PROJECT_TO_PRODUCT_CONFIG,
                projectId,
                false);
    }

    /**
     * 产品配置表审批申请售后件清单字段
     **
     * @param context
     * @param args 请求参数
     * @return String 售后件清单搜索字段HTML
     * @throws Exception
     * @author caipan
     * @date 2026/7/13 16:13
     */
    public String getConfigTableRouteServicePartsListField(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        String selectedObjectId = requestMap == null ? "" : getObjectIdFromTableRowId(getFirstValue(requestMap.get("emxTableRowId")));
        String selectedObjectType = UIUtil.isNullOrEmpty(selectedObjectId) ? ""
                : DomainObject.newInstance(context, selectedObjectId).getInfo(context, SELECT_TYPE);
        //20260828 update by caipan 从售后件清单入口发起时自动带入所选售后件清单
        String servicePartsListId = TYPE_SERVICE_PARTS_LIST.equals(selectedObjectType) ? selectedObjectId : "";
        String servicePartsListDisplay = getObjectDisplayValue(context, servicePartsListId);
        String projectId = getConfigTableRouteProjectId(context, requestMap, selectedObjectId);
        return buildConfigTableRouteChooserHtml("JFApprovalServicePartsListId","CURRENT=policy_JFServicePartsList.state_InWork",
                "JFApprovalServicePartsListDisplay",
                servicePartsListId,
                servicePartsListDisplay,
                "type_JFServicePartsList",
                REL_PROJECT_TO_SERVICE_PARTS_LIST,
                projectId,
                false);
    }

    /**
     * 产品配置表审批申请附件字段
     **
     * @param context
     * @param args 请求参数
     * @return String 附件上传字段HTML
     * @throws Exception
     * @author caipan
     * @date 2026/7/13 16:13
     */
    public String getConfigTableRouteAttachmentField(Context context, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<input type=\"hidden\" id=\"JFApprovalAttachmentDocId\" name=\"JFApprovalAttachmentDocId\" value=\"\"/>");
        sb.append("<input type=\"hidden\" id=\"JFApprovalForceSubmit\" name=\"JFApprovalForceSubmit\" value=\"false\"/>");
        sb.append("<input type=\"file\" id=\"JFApprovalAttachmentFile\" name=\"JFApprovalAttachmentFile\"/>");
        sb.append("<span id=\"JFApprovalAttachmentMessage\" style=\"margin-left:8px;color:#666;\"></span>");
        return sb.toString();
    }

    /**
     * 产品配置表审批申请创建表单旧脚本字段兼容
     **
     * @param context
     * @param args 请求参数
     * @return String 空字符串，提交拦截改由preProcessJavaScript加载
     * @throws Exception
     * @author caipan
     * @date 2026/7/13 16:13
     */
    public String getConfigTableRouteInitScript(Context context, String[] args) throws Exception {
        return "";
    }

    /**
     * 产品配置表关联附件
     **
     * @param context
     * @param productConfig 产品配置对象
     * @param questionListFileId 附件ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void connectQuestionListAttachment(Context context, DomainObject productConfig, String questionListFileId) throws Exception {
        if (UIUtil.isNullOrEmpty(questionListFileId)) {
            return;
        }
        DomainRelationship rel = DomainRelationship.connect(context,
                productConfig,
                RELATIONSHIP_REFERENCE_DOCUMENT,
                DomainObject.newInstance(context, questionListFileId));
        Map relAttrMap = new HashMap();
        relAttrMap.put(ATTR_PROJECT_ROLE, PROJECT_ROLE_QUESTIONS_LIST);
        rel.setAttributeValues(context, relAttrMap);
    }

    public Vector getCreatorDepartment(Context context, String[] args) throws Exception {
      /*  Vector result = new Vector();
        Map programMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        if (objectList == null) {
            return result;
        }
        for (Iterator itr = objectList.iterator(); itr.hasNext();) {
            Map row = (Map) itr.next();
            String owner = (String) row.get(SELECT_OWNER);
            result.add(getOwnerDepartment(context, owner));
        }
        return result;*/
       return new JF_PCR_mxJPO().getPCRORG(context, args);
    }

    /**
     * 售后件清单零件售后类型列
     **
     * @param context
     * @param args 请求参数
     * @return Vector 售后类型HTML
     * @throws Exception
     * @author caipan
     * @date 2026/7/10 16:13
     */
    public Vector getServicePartsListBOMType(Context context, String[] args) throws Exception {
        Vector result = new Vector();
        Map programMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        if (objectList == null) {
            return result;
        }
        /*String tip = "纯售后件：不在本项目最新发布的配置表供货件清单里及其BOM结构里\n"
                + "供货件：在本项目最新发布的配置表供货件清单里的\n"
                + "其他：在本项目最新发布的配置表供货件清单里的BOM结构里，即供货件的子件";*/
        String tip = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Tip.Attribute.JFSBOMType", context.getLocale());
        String tipHtml = escapeHtml(tip).replace("\r\n", "&#10;").replace("\n", "&#10;").replace("\r", "&#10;");
        for (Iterator itr = objectList.iterator(); itr.hasNext();) {
            Map row = (Map) itr.next();
            String bomType = UIUtil.getValue(row, SELECT_REL_ATTR_JFSBOM_TYPE);
            String displayValue = getServicePartsListBOMTypeDisplayValue(bomType);
            result.add("<span title=\"" + tipHtml + "\">" + escapeHtml(displayValue) + "</span>");
        }
        return result;
    }

    /**
     * 获取共享零件表的客户零件号、客户零件名称、Direct buy列值，预取入口使用最新版，其他入口使用当前行零件
     **
     * @param context
     * @param args 表格参数
     * @return Vector 客户零件号、客户零件名称或Direct buy列值
     * @throws Exception
     * @author caipan
     * @date 2026/7/21 16:13
     */
    public Vector getCustomerPartInfoValue(Context context, String[] args) throws Exception {
        Vector result = new Vector();
        Map programMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Map columnMap = (Map) programMap.get("columnMap");
        Map paramList = (Map) programMap.get("paramList");
        if (objectList == null || columnMap == null) {
            return result;
        }
        String columnName = normalizeSelectValue(columnMap.get(SELECT_NAME));
        String selectName = ATTR_CUSTOMER_PART_NAME.equals(columnName)
                ? SELECT_REL_ATTR_CUSTOMER_PART_NAME
                : COLUMN_DIRECT_BUY.equals(columnName)
                ? SELECT_REL_ATTR_DIRECT_BUY : SELECT_REL_ATTR_CUSTOMER_PART_NUMBER;
        boolean hasPrefetchedValue = true;
        for (Object rowObj : objectList) {
            if (!((Map) rowObj).containsKey(columnName)) {
                hasPrefetchedValue = false;
                break;
            }
        }
        if (hasPrefetchedValue) {
            for (Object rowObj : objectList) {
                result.add(normalizeSelectValue(((Map) rowObj).get(columnName)));
            }
            return result;
        }
        String projectId = getConfigTableRouteProjectId(context, paramList, "");
        if (UIUtil.isNullOrEmpty(projectId)) {
            for (int i = 0; i < objectList.size(); i++) {
                result.add("");
            }
            return result;
        }
        Set validPartIds = new LinkedHashSet();
        for (Object rowObj : objectList) {
            Map row = (Map) rowObj;
            String partId = normalizeSelectValue(row.get(SELECT_ID));
            String partType = normalizeSelectValue(row.get(SELECT_TYPE));
            if (UIUtil.isNullOrEmpty(partType) && UIUtil.isNotNullAndNotEmpty(partId)) {
                partType = DomainObject.newInstance(context, partId).getInfo(context, SELECT_TYPE);
            }
            if (TYPE_VPM_REFERENCE.equals(partType)) {
                validPartIds.add(partId);
            }
        }
        Map customerPartInfoCache = new HashMap();
        JF_DR_mxJPO dr = new JF_DR_mxJPO();
        for (Object rowObj : objectList) {
            Map row = (Map) rowObj;
            String partId = normalizeSelectValue(row.get(SELECT_ID));
            if (!validPartIds.contains(partId)) {
                result.add("");
                continue;
            }
            Map customerPartInfo = (Map) customerPartInfoCache.get(partId);
            if (customerPartInfo == null) {
                customerPartInfo = dr.getDRCustomerPartsDBRelationInfo(context, partId, projectId);
                customerPartInfoCache.put(partId, customerPartInfo);
            }
            result.add(normalizeSelectValue(customerPartInfo.get(selectName)));
        }
        return result;
    }

    /**
     * 获取售后类型显示值
     **
     * @param bomType 售后类型属性值
     * @return String 售后类型显示值
     * @throws Exception
     * @author caipan
     * @date 2026/7/10 16:13
     */
    private String getServicePartsListBOMTypeDisplayValue(String bomType) throws Exception {
        if ("PureAfterSalesParts".equals(bomType)) {
            return "纯售后件";
        } else if ("SupplyParts".equals(bomType)) {
            return "供货件";
        } else if ("Other".equals(bomType)) {
            return "其他";
        }
        return bomType;
    }

    public String getPartsConfigData(Context context, String[] args) throws Exception {
        return new Gson().toJson(getPartsConfigDataMap(context, args));
    }

    /**
     * 获取零件及配置页面数据
     **
     * @param context
     * @param args 请求参数
     * @return Map 页面数据及当前登录账号
     * @throws Exception
     * @author caipan
     * @date 2026/7/21 16:00
     */
    private Map getPartsConfigDataMap(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        Map result = new HashMap();
        result.put("code", "200");
        result.put("contextUser", context.getUser());
        result.put("canEdit", isProductConfigEditable(context, productConfigId));
        result.put("vehicleConfigs", getVehicleConfigList(context, productConfigId));
        result.put("positions", getPositionProductList(context, productConfigId, false));
        return result;
    }

    public String getPartsConfigDataRest(Context context, String[] args) throws Exception {
        return getPartsConfigData(context, args);
    }

    /**
     * 获取目标销量页面数据
     **
     * @param context
     * @param args 请求参数
     * @return String 目标销量页面JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    public String getTargetSalesData(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        Map result = success();
        result.put("canEdit", isTargetSalesEditable(context, productConfigId));
        result.put("projectLevelAPRDiscount", UIUtil.isNullOrEmpty(productConfigId) ? "" : normalizeSelectValue(DomainObject.newInstance(context, productConfigId).getInfo(context, SELECT_ATTR_PROJECT_LEVEL_APR_DISCOUNT)));
        MapList targetSales = getTargetSalesList(context, productConfigId);
        result.put("targetSales", targetSales);
        result.put("vehicleConfigs", getTargetSalesVehicleConfigList(context, productConfigId));
        result.put("parts", getTargetSalesPartList(context, productConfigId, targetSales));
        result.put("managerTasks", getProductConfigTaskList(context, productConfigId));
        return new Gson().toJson(result);
    }

    /**
     * 获取目标销量页面数据REST入口
     **
     * @param context
     * @param args 请求参数
     * @return String 目标销量页面JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    public String getTargetSalesDataRest(Context context, String[] args) throws Exception {
        return getTargetSalesData(context, args);
    }

    /**
     * 修改项目级别APR折扣
     **
     * @param context
     * @param args 请求参数
     * @return String 修改结果JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    public String updateProjectLevelAPRDiscount(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.ProductConfigTableIdEmpty", "Current product config table id is empty.")));
        }
        Map editableCheck = checkTargetSalesEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        boolean isPush = false;
        try {
            String discount = normalizeQuantityValue(context, params.get("projectLevelAPRDiscount"));
            ContextUtil.pushContext(context);
            isPush = true;
            DomainObject.newInstance(context, productConfigId).setAttributeValue(context, ATTR_PROJECT_LEVEL_APR_DISCOUNT, discount);
            return new Gson().toJson(success());
        } catch (Exception e) {
            LOGGER.error("updateProjectLevelAPRDiscount error", e);
            return new Gson().toJson(fail(e.getMessage()));
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 修改项目级别APR折扣REST入口
     **
     * @param context
     * @param args 请求参数
     * @return String 修改结果JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    public String updateProjectLevelAPRDiscountRest(Context context, String[] args) throws Exception {
        return updateProjectLevelAPRDiscount(context, args);
    }

    /**
     * 新增目标销量年度
     **
     * @param context
     * @param args 请求参数
     * @return String 新增结果JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    public String createTargetSalesYear(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String year = normalizeSelectValue(params.get("year"));
        if (UIUtil.isNullOrEmpty(productConfigId) || UIUtil.isNullOrEmpty(year)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.YearRequired", "Year is required.")));
        }
        if (!year.matches("\\d{4}")) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.YearInvalid", "Year must be a 4-digit number.")));
        }
        Map editableCheck = checkTargetSalesEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        if (isTargetSalesYearExists(context, productConfigId, year, "")) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.YearExists", "Year already exists.")));
        }
        ContextUtil.startTransaction(context, true);
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            DomainObject targetSalesObj = createAutoNamedObject(context, SYMBOLIC_TYPE_TARGET_SALES, SYMBOLIC_POLICY_TARGET_SALES);
            Map attrMap = new HashMap();
            attrMap.put(ATTR_TITLE, year);
            attrMap.put(ATTR_OVERSEAS, "");
            attrMap.put(ATTR_DOMESTIC, "");
            attrMap.put(ATTR_OTHER, "");
            targetSalesObj.setAttributeValues(context, attrMap);
            DomainRelationship.connect(context,
                    DomainObject.newInstance(context, productConfigId),
                    REL_PRODUCT_CONFIG_TO_TARGET_SALES,
                    targetSalesObj);
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("createTargetSalesYear error", e);
            return new Gson().toJson(fail(e.getMessage()));
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        return getTargetSalesData(context, args);
    }

    /**
     * 新增目标销量年度REST入口
     **
     * @param context
     * @param args 请求参数
     * @return String 新增结果JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    public String createTargetSalesYearRest(Context context, String[] args) throws Exception {
        return createTargetSalesYear(context, args);
    }

    /**
     * 修改年度目标销量
     **
     * @param context
     * @param args 请求参数
     * @return String 修改结果JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    public String updateTargetSales(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String targetSalesId = (String) params.get("targetSalesId");
        if (UIUtil.isNullOrEmpty(productConfigId) || UIUtil.isNullOrEmpty(targetSalesId) || !isProductTargetSales(context, productConfigId, targetSalesId)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.SelectYearFirst", "Please select a target sales year first.")));
        }
        Map editableCheck = checkTargetSalesEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        boolean isPush = false;
        try {
            Map attrMap = new HashMap();
            attrMap.put(ATTR_OVERSEAS, normalizeQuantityValue(context, params.get("overseas")));
            attrMap.put(ATTR_DOMESTIC, normalizeQuantityValue(context, params.get("domestic")));
            attrMap.put(ATTR_OTHER, normalizeQuantityValue(context, params.get("other")));
            ContextUtil.pushContext(context);
            isPush = true;
            DomainObject.newInstance(context, targetSalesId).setAttributeValues(context, attrMap);
            return new Gson().toJson(success());
        } catch (Exception e) {
            LOGGER.error("updateTargetSales error", e);
            return new Gson().toJson(fail(e.getMessage()));
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 修改年度目标销量REST入口
     **
     * @param context
     * @param args 请求参数
     * @return String 修改结果JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    public String updateTargetSalesRest(Context context, String[] args) throws Exception {
        return updateTargetSales(context, args);
    }

    /**
     * 删除目标销量年度
     **
     * @param context
     * @param args 请求参数
     * @return String 删除结果JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public String deleteTargetSalesYears(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        StringList targetSalesIds = normalizeIdList(params.get("targetSalesIds"));
        if (UIUtil.isNullOrEmpty(productConfigId) || targetSalesIds.isEmpty()) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.SelectYearFirst", "Please select a target sales year first.")));
        }
        Map editableCheck = checkTargetSalesEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        ContextUtil.startTransaction(context, true);
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            for (Object targetSalesIdObj : targetSalesIds) {
                String targetSalesId = normalizeSelectValue(targetSalesIdObj);
                if (UIUtil.isNullOrEmpty(targetSalesId) || !isProductTargetSales(context, productConfigId, targetSalesId)) {
                    throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.SelectYearFirst", "Please select a target sales year first."));
                }
                deleteTargetSalesConnections(context, targetSalesId);
                DomainObject.newInstance(context, targetSalesId).deleteObject(context);
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("deleteTargetSalesYears error", e);
            return new Gson().toJson(fail(e.getMessage()));
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        return getTargetSalesData(context, args);
    }

    /**
     * 删除目标销量年度REST入口
     **
     * @param context
     * @param args 请求参数
     * @return String 删除结果JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public String deleteTargetSalesYearsRest(Context context, String[] args) throws Exception {
        return deleteTargetSalesYears(context, args);
    }

    /**
     * 修改整车配置比例
     **
     * @param context
     * @param args 请求参数
     * @return String 修改结果JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    public String updateVehicleConfigSalesRatio(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String vehicleConfigId = (String) params.get("vehicleConfigId");
        if (UIUtil.isNullOrEmpty(productConfigId) || UIUtil.isNullOrEmpty(vehicleConfigId) || !isProductVehicleConfig(context, productConfigId, vehicleConfigId)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.SelectVehicleConfigFirst")));
        }
        Map editableCheck = checkTargetSalesEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        boolean isPush = false;
        try {
            Map attrMap = new HashMap();
            attrMap.put(ATTR_OVERSEAS, normalizePercentValue(context, params.get("overseas")));
            attrMap.put(ATTR_DOMESTIC, normalizePercentValue(context, params.get("domestic")));
            attrMap.put(ATTR_OTHER, normalizePercentValue(context, params.get("other")));
            ContextUtil.pushContext(context);
            isPush = true;
            DomainObject.newInstance(context, vehicleConfigId).setAttributeValues(context, attrMap);
            return new Gson().toJson(success());
        } catch (Exception e) {
            LOGGER.error("updateVehicleConfigSalesRatio error", e);
            return new Gson().toJson(fail(e.getMessage()));
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 修改整车配置比例REST入口
     **
     * @param context
     * @param args 请求参数
     * @return String 修改结果JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    public String updateVehicleConfigSalesRatioRest(Context context, String[] args) throws Exception {
        return updateVehicleConfigSalesRatio(context, args);
    }

    /**
     * 修改目标销量零件年度数量并返回最新汇总比例
     **
     * @param context
     * @param args 请求参数
     * @return String 修改结果JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    public String updateTargetSalesPartQuantity(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String targetSalesId = (String) params.get("targetSalesId");
        String partId = (String) params.get("partId");
        if (UIUtil.isNullOrEmpty(productConfigId)
                || UIUtil.isNullOrEmpty(targetSalesId)
                || UIUtil.isNullOrEmpty(partId)
                || !isProductTargetSales(context, productConfigId, targetSalesId)
                || !isProductConfigPart(context, productConfigId, partId)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.SelectPartsFirst")));
        }
        Map editableCheck = checkTargetSalesEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        String annualQuantity;
        try {
            annualQuantity = normalizeQuantityValue(context, params.get("annualQuantity"));
        } catch (Exception e) {
            return new Gson().toJson(fail(e.getMessage()));
        }
        ContextUtil.startTransaction(context, true);
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            setTargetSalesPartAnnualQuantity(context, targetSalesId, partId, annualQuantity);
            String ratio = "";
            DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
            boolean ratioReady = !"FALSE".equalsIgnoreCase(normalizeSelectValue(productConfigObj.getInfo(context, SELECT_ATTR_SUMMARY_RATIO_READY)));
            //20260831 update by caipan 手工修改汇总销量后复用现有公式重新计算当前零件比例
            if (ratioReady) {
                MapList targetSales = getTargetSalesList(context, productConfigId);
                Map annualQuantityMap = getTargetSalesPartAnnualQuantityMap(context, targetSales);
                Map quantities = new HashMap();
                for (Object targetObj : targetSales) {
                    Map targetMap = (Map) targetObj;
                    String currentTargetSalesId = normalizeSelectValue(targetMap.get(SELECT_ID));
                    quantities.put(currentTargetSalesId, normalizeSelectValue(annualQuantityMap.get(currentTargetSalesId + "|" + partId)));
                }
                ratio = calculateTargetSalesPartRatio(quantities, getTargetSalesTotal(targetSales));
            }
            ContextUtil.commitTransaction(context);
            Map result = success();
            result.put("ratio", ratio);
            return new Gson().toJson(result);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("updateTargetSalesPartQuantity error", e);
            return new Gson().toJson(fail(e.getMessage()));
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 修改目标销量零件年度数量REST入口
     **
     * @param context
     * @param args 请求参数
     * @return String 修改结果JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    public String updateTargetSalesPartQuantityRest(Context context, String[] args) throws Exception {
        return updateTargetSalesPartQuantity(context, args);
    }

    /**
     * 汇总计算目标销量零件年度数量
     **
     * @param context
     * @param args 请求参数
     * @return String 计算后目标销量页面JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public String calculateTargetSalesSummary(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        boolean overwrite = !"false".equalsIgnoreCase(normalizeSelectValue(params.get("overwrite")));
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.ProductConfigTableIdEmpty", "Current product config table id is empty.")));
        }
        Map editableCheck = checkTargetSalesEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        ContextUtil.startTransaction(context, true);
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            MapList targetSales = getTargetSalesList(context, productConfigId);
            MapList parts = getTargetSalesPartList(context, productConfigId, targetSales);
            Map partVehicleQuantityMap = getTargetSalesPartVehicleQuantityMap(context, productConfigId);
            Map vehicleRatioMap = getTargetSalesVehicleRatioMap(context, productConfigId);
            Map annualQuantityMap = getTargetSalesPartAnnualQuantityMap(context, targetSales);
            for (Object partObj : parts) {
                Map partMap = (Map) partObj;
                String partId = normalizeSelectValue(partMap.get(SELECT_ID));
                String partKey = getTargetSalesPartQuantityKey(partMap);
                if (UIUtil.isNullOrEmpty(partId) || UIUtil.isNullOrEmpty(partKey)) {
                    continue;
                }
                for (Object targetObj : targetSales) {
                    Map targetMap = (Map) targetObj;
                    String targetSalesId = normalizeSelectValue(targetMap.get(SELECT_ID));
                    if (UIUtil.isNullOrEmpty(targetSalesId)) {
                        continue;
                    }
                    String oldQuantity = normalizeSelectValue(annualQuantityMap.get(targetSalesId + "|" + partId));
                    if (!overwrite && UIUtil.isNotNullAndNotEmpty(oldQuantity)) {
                        continue;
                    }
                    BigDecimal annualQuantity = calculateTargetSalesPartAnnualQuantity(targetMap, partKey, partVehicleQuantityMap, vehicleRatioMap);
                    setTargetSalesPartAnnualQuantity(context, targetSalesId, partId, annualQuantity.setScale(0, RoundingMode.HALF_UP).toPlainString());
                }
            }
            DomainObject.newInstance(context, productConfigId).setAttributeValue(context, ATTR_SUMMARY_RATIO_READY, "TRUE");
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("calculateTargetSalesSummary error", e);
            return new Gson().toJson(fail(e.getMessage()));
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        return getTargetSalesData(context, args);
    }

    /**
     * 汇总计算目标销量零件年度数量REST入口
     **
     * @param context
     * @param args 请求参数
     * @return String 计算后目标销量页面JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public String calculateTargetSalesSummaryRest(Context context, String[] args) throws Exception {
        return calculateTargetSalesSummary(context, args);
    }

    /**
     * 获取单个位置产品零件配置数据
     **
     * @param context
     * @param args 请求参数
     * @return String 单个位置产品零件配置JSON
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String getPositionPartsConfigData(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String positionId = (String) params.get("positionId");
        Map result = new HashMap();
        result.put("code", "200");
        if (UIUtil.isNullOrEmpty(productConfigId) || UIUtil.isNullOrEmpty(positionId) || !isProductPosition(context, productConfigId, positionId)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.SelectPositionProductFirst")));
        }
        result.put("position", getPositionProduct(context, productConfigId, positionId, true));
        return new Gson().toJson(result);
    }

    /**
     * 获取单个位置产品零件配置数据REST入口
     **
     * @param context
     * @param args 请求参数
     * @return String 单个位置产品零件配置JSON
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String getPositionPartsConfigDataRest(Context context, String[] args) throws Exception {
        return getPositionPartsConfigData(context, args);
    }

    public String createPositionProduct(Context context, String[] args) throws Exception {
        return new Gson().toJson(createPositionProductMap(context, args));
    }

    /**
     * 获取位置产品创建弹窗元数据
     **
     * @param context
     * @param args 请求参数
     * @return String 位置产品类型和名称range列表
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String getPositionProductCreateMeta(Context context, String[] args) throws Exception {
        Map result = success();
        result.put("ppTypes", getAttributeRangeOptions(context, ATTR_JFPP_TYPE));
        result.put("ppTitles", getAttributeRangeOptions(context, ATTR_JFPP_TITLE));
        return new Gson().toJson(result);
    }

    private Map createPositionProductMap(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String ppType = (String) params.get("ppType");
        String ppTitle = (String) params.get("ppTitle");
        String title = (String) params.get("title");
        String description = (String) params.get("description");
        Map editableCheck = checkProductConfigEditable(context, productConfigId);
        if (editableCheck != null) {
            return editableCheck;
        }
        if (UIUtil.isNullOrEmpty(productConfigId) || UIUtil.isNullOrEmpty(ppType) || UIUtil.isNullOrEmpty(ppTitle)) {
            return fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.PositionProductNameRequired"));
        }
        if (UIUtil.isNullOrEmpty(title)) {
            title = ppTitle;
        }
        //20260826 update by caipan 按JFPPTitle原始key校验，避免中英文展示名称不同导致重复创建
        if (isPositionTitleExists(context, productConfigId, ppTitle)) {
            return fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.PositionProductNameExists"));
        }
        ContextUtil.startTransaction(context, true);
        try {
            DomainObject positionObj = createAutoNamedObject(context, SYMBOLIC_TYPE_POSITION_PRODUCT, SYMBOLIC_POLICY_POSITION_PRODUCT);
            Map attrMap = new HashMap();
            attrMap.put(ATTR_TITLE, title);
            attrMap.put(ATTR_JFPP_TYPE, ppType);
            attrMap.put(ATTR_JFPP_TITLE, ppTitle);
            positionObj.setAttributeValues(context, attrMap);
            positionObj.setDescription(context, UIUtil.isNullOrEmpty(description) ? "" : description);
            DomainRelationship.connect(context,
                    DomainObject.newInstance(context, productConfigId),
                    REL_PRODUCT_CONFIG_TO_POSITION,
                    positionObj);
            connectExistingVehicleConfigsToPosition(context, productConfigId, positionObj.getId(context));
            ContextUtil.commitTransaction(context);
            Map result = success();
            result.put("positionId", positionObj.getId(context));
            result.put("positions", getPositionProductList(context, productConfigId));
            return result;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("createPositionProduct error", e);
            return fail(e.getMessage());
        }
    }

    private boolean isPositionTitleExists(Context context, String productConfigId, String title) throws Exception {
        return isPositionTitleExists(context, productConfigId, title, "");
    }

    /**
     * 按JFPPTitle原始key校验位置产品是否重复
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param ppTitle 位置产品JFPPTitle原始key
     * @param excludeId 排除的位置产品ID
     * @return boolean key重复时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private boolean isPositionTitleExists(Context context, String productConfigId, String ppTitle, String excludeId) throws Exception {
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        MapList positionList = productConfigObj.getRelatedObjects(context,
                REL_PRODUCT_CONFIG_TO_POSITION,
                TYPE_POSITION_PRODUCT,
                //20260826 update by caipan 唯一性校验使用不受语言环境影响的JFPPTitle原始key
                new StringList(SELECT_ATTR_JFPP_TITLE),
                new StringList(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
        for (Object positionObj : positionList) {
            Map positionMap = (Map) positionObj;
            String positionId = (String) positionMap.get(SELECT_ID);
            if (!excludeId.equals(positionId) && ppTitle.equals((String) positionMap.get(SELECT_ATTR_JFPP_TITLE))) {
                return true;
            }
        }
        return false;
    }

    public String createPositionProductRest(Context context, String[] args) throws Exception {
        return createPositionProduct(context, args);
    }

    /**
     * 删除位置产品并清理配置矩阵关系
     **
     * @param context
     * @param args 请求参数
     * @return String 删除结果
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String deletePositionProduct(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String positionId = (String) params.get("positionId");
        StringList positionIds = toStringList(params.get("positionIds"));
        if (UIUtil.isNotNullAndNotEmpty(positionId)) {
            positionIds.add(positionId);
        }
        if (UIUtil.isNullOrEmpty(productConfigId) || positionIds.isEmpty()) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.SelectPositionProductFirst")));
        }
        Map editableCheck = checkProductConfigEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        //20260813 update by caipan 升版复制对象Owner为usage agent时，提权删除位置产品及其关系。
        boolean isPush = false;
        ContextUtil.startTransaction(context, true);
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            Set deletedIds = new HashSet();
            for (Object positionIdObj : positionIds) {
                String currentPositionId = String.valueOf(positionIdObj);
                if (UIUtil.isNullOrEmpty(currentPositionId) || deletedIds.contains(currentPositionId) || !isProductPosition(context, productConfigId, currentPositionId)) {
                    continue;
                }
                deleteAllPositionVehicleConfigRelations(context, currentPositionId);
                deleteAllPositionPartRelations(context, productConfigId, currentPositionId);
                disconnectPositionFromProductConfig(context, productConfigId, currentPositionId);
                DomainObject.newInstance(context, currentPositionId).deleteObject(context);
                deletedIds.add(currentPositionId);
            }
            ContextUtil.commitTransaction(context);
            return new Gson().toJson(getPartsConfigDataMap(context, args));
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("deletePositionProduct error", e);
            return new Gson().toJson(fail(e.getMessage()));
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    public String deletePositionProductRest(Context context, String[] args) throws Exception {
        return deletePositionProduct(context, args);
    }

    /**
     * 修改位置产品表头名称
     **
     * @param context
     * @param args 请求参数
     * @return String 修改结果
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String updatePositionProduct(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String positionId = (String) params.get("positionId");
        String title = (String) params.get("title");
        if (UIUtil.isNullOrEmpty(productConfigId) || UIUtil.isNullOrEmpty(positionId) || UIUtil.isNullOrEmpty(title) || !isProductPosition(context, productConfigId, positionId)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.PositionProductNameRequired")));
        }
        Map editableCheck = checkProductConfigEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        DomainObject positionObj = DomainObject.newInstance(context, positionId);
        if (!JFPP_TYPE_NON_WHOLE_CHAIR.equals(positionObj.getInfo(context, SELECT_ATTR_JFPP_TYPE))) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.PositionProductNameNotEditable")));
        }
        if (isPositionTitleExists(context, productConfigId, title, positionId)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.PositionProductNameExists")));
        }
        try {
            Map attrMap = new HashMap();
            attrMap.put(ATTR_TITLE, title);
            attrMap.put(ATTR_JFPP_TITLE, title);
            positionObj.setAttributeValues(context, attrMap);
            return new Gson().toJson(getPartsConfigDataMap(context, args));
        } catch (Exception e) {
            LOGGER.error("updatePositionProduct error", e);
            return new Gson().toJson(fail(e.getMessage()));
        }
    }

    public String updatePositionProductRest(Context context, String[] args) throws Exception {
        return updatePositionProduct(context, args);
    }

    /**
     * 获取整车配置创建弹窗元数据
     **
     * @param context
     * @param args 请求参数
     * @return String 当前配置表整车配置列表和默认名称
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String getVehicleConfigCreateMeta(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        Map result = success();
        MapList vehicleConfigs = getVehicleConfigList(context, productConfigId);
        result.put("vehicleConfigs", vehicleConfigs);
        result.put("defaultTitle", getNextVehicleConfigTitle(context, vehicleConfigs));
        return new Gson().toJson(result);
    }

    public String getVehicleConfigCreateMetaRest(Context context, String[] args) throws Exception {
        return getVehicleConfigCreateMeta(context, args);
    }

    /**
     * 获取矩阵编辑元数据
     **
     * @param context
     * @param args 请求参数
     * @return String 是否选配range列表
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String getProductConfigMatrixMeta(Context context, String[] args) throws Exception {
        Map result = success();
        MapList optionalOptions = getAttributeRangeOptions(context, ATTR_OPTIONAL_OR_NOT);
        Map emptyOption = new HashMap();
        emptyOption.put("value", "");
        emptyOption.put("label", "");
        optionalOptions.add(0, emptyOption);
        result.put("optionalOptions", optionalOptions);
        return new Gson().toJson(result);
    }

    public String getProductConfigMatrixMetaRest(Context context, String[] args) throws Exception {
        return getProductConfigMatrixMeta(context, args);
    }

    /**
     * 创建整车配置并同步到所有位置产品配置表
     **
     * @param context
     * @param args 请求参数
     * @return String 创建结果
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String createVehicleConfig(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String title = (String) params.get("title");
        String vcInfo = (String) params.get("vcInfo");
        String copyFromId = (String) params.get("copyFromId");
        if (UIUtil.isNullOrEmpty(productConfigId) || UIUtil.isNullOrEmpty(title)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.VehicleConfigNameRequired")));
        }
        if (UIUtil.isNullOrEmpty(vcInfo)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.VehicleConfigInfoRequired")));
        }
        if (isVehicleConfigTitleExists(context, productConfigId, title, "")) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.VehicleConfigNameExists")));
        }
        Map editableCheck = checkProductConfigEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        ContextUtil.startTransaction(context, true);
        try {
            MapList vehicleConfigs = getVehicleConfigList(context, productConfigId);
            DomainObject vehicleConfigObj = createAutoNamedObject(context, SYMBOLIC_TYPE_VEHICLE_CONFIGURATION, SYMBOLIC_POLICY_VEHICLE_CONFIGURATION);
            Map attrMap = new HashMap();
            attrMap.put(ATTR_TITLE, title);
            attrMap.put(ATTR_VC_INFO, UIUtil.isNullOrEmpty(vcInfo) ? "" : vcInfo);
            attrMap.put(ATTR_ORDER, formatOrder(getNextVehicleConfigOrder(vehicleConfigs)));
            vehicleConfigObj.setAttributeValues(context, attrMap);
            DomainRelationship.connect(context,
                    DomainObject.newInstance(context, productConfigId),
                    REL_PRODUCT_CONFIG_TO_VEHICLE_CONFIG,
                    vehicleConfigObj);
            connectVehicleConfigToPositions(context, productConfigId, vehicleConfigObj.getId(context), copyFromId);
            ContextUtil.commitTransaction(context);
            Map result = getPartsConfigDataMap(context, args);
            result.put("vehicleConfigId", vehicleConfigObj.getId(context));
            result.put("defaultTitle", getNextVehicleConfigTitle(context, (MapList) result.get("vehicleConfigs")));
            return new Gson().toJson(result);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("createVehicleConfig error", e);
            return new Gson().toJson(fail(e.getMessage()));
        }
    }

    public String createVehicleConfigRest(Context context, String[] args) throws Exception {
        return createVehicleConfig(context, args);
    }

    /**
     * 修改整车配置表头信息
     **
     * @param context
     * @param args 请求参数
     * @return String 修改结果
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String updateVehicleConfig(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String vehicleConfigId = (String) params.get("vehicleConfigId");
        String title = (String) params.get("title");
        String vcInfo = (String) params.get("vcInfo");
        if (UIUtil.isNullOrEmpty(productConfigId) || UIUtil.isNullOrEmpty(vehicleConfigId) || UIUtil.isNullOrEmpty(title)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.VehicleConfigNameRequired")));
        }
        if (isVehicleConfigTitleExists(context, productConfigId, title, vehicleConfigId)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.VehicleConfigNameExists")));
        }
        Map editableCheck = checkProductConfigEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        try {
            DomainObject vehicleConfigObj = DomainObject.newInstance(context, vehicleConfigId);
            Map attrMap = new HashMap();
            attrMap.put(ATTR_TITLE, title);
            attrMap.put(ATTR_VC_INFO, UIUtil.isNullOrEmpty(vcInfo) ? "" : vcInfo);
            vehicleConfigObj.setAttributeValues(context, attrMap);
            return new Gson().toJson(getPartsConfigDataMap(context, args));
        } catch (Exception e) {
            LOGGER.error("updateVehicleConfig error", e);
            return new Gson().toJson(fail(e.getMessage()));
        }
    }

    public String updateVehicleConfigRest(Context context, String[] args) throws Exception {
        return updateVehicleConfig(context, args);
    }

    /**
     * 删除整车配置并从所有位置产品配置表删除
     **
     * @param context
     * @param args 请求参数
     * @return String 删除结果
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String deleteVehicleConfig(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String vehicleConfigId = (String) params.get("vehicleConfigId");
        StringList vehicleConfigIds = toStringList(params.get("vehicleConfigIds"));
        if (UIUtil.isNotNullAndNotEmpty(vehicleConfigId)) {
            vehicleConfigIds.add(vehicleConfigId);
        }
        if (UIUtil.isNullOrEmpty(productConfigId) || vehicleConfigIds.isEmpty()) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.SelectVehicleConfigFirst")));
        }
        Map editableCheck = checkProductConfigEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        //20260813 update by caipan 升版复制对象Owner为usage agent时，提权删除整车配置及其关系。
        boolean isPush = false;
        ContextUtil.startTransaction(context, true);
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            Set deletedIds = new HashSet();
            for (Object itemObj : vehicleConfigIds) {
                String currentVehicleConfigId = String.valueOf(itemObj);
                if (UIUtil.isNullOrEmpty(currentVehicleConfigId) || deletedIds.contains(currentVehicleConfigId)) {
                    continue;
                }
                if (!isProductVehicleConfig(context, productConfigId, currentVehicleConfigId)) {
                    continue;
                }
                DomainObject vehicleConfigObj = DomainObject.newInstance(context, currentVehicleConfigId);
                deletePositionVehicleConfigRelations(context, productConfigId, currentVehicleConfigId);
                disconnectVehicleConfigFromProductConfig(context, productConfigId, currentVehicleConfigId);
                vehicleConfigObj.deleteObject(context);
                deletedIds.add(currentVehicleConfigId);
            }
            ContextUtil.commitTransaction(context);
            return new Gson().toJson(getPartsConfigDataMap(context, args));
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("deleteVehicleConfig error", e);
            return new Gson().toJson(fail(e.getMessage()));
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    public String deleteVehicleConfigRest(Context context, String[] args) throws Exception {
        return deleteVehicleConfig(context, args);
    }

    /**
     * 校验产品配置表是否可编辑
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return Map 不可编辑时返回错误信息，可编辑时返回null
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private Map checkProductConfigEditable(Context context, String productConfigId) throws Exception {
        if (!isProductConfigEditable(context, productConfigId)) {
            return fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.NoEditAccess"));
        }
        return null;
    }

    /**
     * 判断产品配置表是否可编辑
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return boolean 工作中且Owner为当前登录人时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private boolean isProductConfigEditable(Context context, String productConfigId) throws Exception {
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            return false;
        }
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        String current = productConfigObj.getInfo(context, "current");
        String owner = productConfigObj.getInfo(context, SELECT_OWNER);
        return STATE_IN_WORK.equals(current) && context.getUser().equals(owner);
    }

    /**
     * 校验目标销量是否可编辑
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return Map 不可编辑时返回错误信息，可编辑时返回null
     * @throws Exception
     * @author caipan
     * @date 2026/7/16 16:13
     */
    private Map checkTargetSalesEditable(Context context, String productConfigId) throws Exception {
        if (!isTargetSalesEditable(context, productConfigId)) {
            return fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.NoEditAccess"));
        }
        return null;
    }

    /**
     * 判断当前登录人是否具有目标销量编辑权限
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return boolean 产品配置表处于客户经理填写状态且当前登录人为关联配置任务Owner时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/7/16 16:13
     */
    private boolean isTargetSalesEditable(Context context, String productConfigId) throws Exception {
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            return false;
        }
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        if (!STATE_MANAGER_FILLS_IN.equals(productConfigObj.getInfo(context, SELECT_CURRENT))) {
            return false;
        }
        String currentUser = context.getUser();
        StringList taskOwners = productConfigObj.getInfoList(context,
                "from[" + REL_PRODUCT_CONFIG_TO_PRODUCT_CONFIG_TASK + "].to.owner");
        for (Object taskOwnerObj : taskOwners) {
            if (currentUser.equalsIgnoreCase(normalizeSelectValue(taskOwnerObj))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取属性range及国际化显示值，位置产品名称按业务顺序返回
     **
     * @param context
     * @param attrName 属性名称
     * @return MapList range选项
     * @throws Exception
     * @author caipan
     * @date 2026/7/23 16:13
     */
    private MapList getAttributeRangeOptions(Context context, String attrName) throws Exception {
        MapList options = new MapList();
        StringList ranges = getAttributeRangesByMql(context, attrName);
        if (ATTR_JFPP_TITLE.equals(attrName)) {
            String[] orderedRangeValues = new String[]{
                    "leftFrontSeat",
                    "rightFrontSeat",
                    "secondRowSeat",
                    "secondRowLeftSeat",
                    "secondRowMiddleSeat",
                    "secondRowRightSeat",
                    "thirdRowSeat",
                    "thirdRowLeftSeat",
                    "thirdRowMiddleSeat",
                    "thirdRowRightSeat"
            };
            StringList orderedRanges = new StringList();
            for (String rangeValue : orderedRangeValues) {
                if (ranges.contains(rangeValue)) {
                    orderedRanges.add(rangeValue);
                }
            }
            ranges = orderedRanges;
        }
        for (Object rangeObj : ranges) {
            String rangeValue = (String) rangeObj;
            Map option = new HashMap();
            option.put("value", rangeValue);
            option.put("label", getAttributeRangeLabel(context, attrName, rangeValue));
            options.add(option);
        }
        return options;
    }

    /**
     * 通过MQL读取属性range
     **
     * @param context
     * @param attrName 属性名称
     * @return StringList range值
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private StringList getAttributeRangesByMql(Context context, String attrName) throws Exception {
        String output = MqlUtil.mqlCommand(context, "print attribute $1 select $2 dump $3", attrName, "range", "|");
        StringList ranges = new StringList();
        if (UIUtil.isNullOrEmpty(output)) {
            return ranges;
        }
        String[] items = output.split("\\|");
        for (String item : items) {
            String rangeValue = item == null ? "" : item.trim();
            while (rangeValue.startsWith("!=")) {
                rangeValue = rangeValue.substring(2).trim();
            }
            while (rangeValue.startsWith("=")) {
                rangeValue = rangeValue.substring(1).trim();
            }
            if (UIUtil.isNullOrEmpty(rangeValue) || "!".equals(rangeValue)) {
                continue;
            }
            if (!ranges.contains(rangeValue)) {
                ranges.add(rangeValue);
            }
        }
        return ranges;
    }

    private String getAttributeRangeLabel(Context context, String attrName, String rangeValue) {
        String key = "emxFramework.Range." + attrName + "." + rangeValue;
        return getFrameworkString(context, key, rangeValue);
    }

    private String getFrameworkString(Context context, String key) {
        return getFrameworkString(context, key, key);
    }

    private String getFrameworkString(Context context, String key, String fallback) {
        try {
            return EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), key);
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * 添加零件到位置产品矩阵
     **
     * @param context
     * @param args 请求参数
     * @return Map 添加结果
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public Map addPartsToPosition(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String positionId = (String) params.get("positionId");
        StringList positionIds = toStringList(params.get("positionIds"));
        if (UIUtil.isNotNullAndNotEmpty(positionId)) {
            positionIds.add(positionId);
        }
        StringList partIds = resolveVpmReferenceIds(context, toStringList(params.get("partIds")));
        Map editableCheck = checkProductConfigEditable(context, productConfigId);
        if (editableCheck != null) {
            return editableCheck;
        }
        if (UIUtil.isNullOrEmpty(productConfigId) || positionIds.isEmpty() || partIds.isEmpty()) {
            return fail("Please select a position product and part.");
        }
        ContextUtil.startTransaction(context, true);
        try {
            Set repeatNames = new HashSet();
            for (Object positionIdObj : positionIds) {
                String currentPositionId = String.valueOf(positionIdObj);
                if (!isProductPosition(context, productConfigId, currentPositionId)) {
                    continue;
                }
                DomainObject positionObj = DomainObject.newInstance(context, currentPositionId);
                String positionTitle = positionObj.getInfo(context, SELECT_ATTR_TITLE);
                for (Object partIdObj : partIds) {
                    String partId = (String) partIdObj;
                    if (hasPositionPartByLogicalId(context, currentPositionId, partId)) {
                        DomainObject partObj = DomainObject.newInstance(context, partId);
                        String partName = partObj.getInfo(context, SELECT_ATTR_PART_NUMBER);
                        repeatNames.add((UIUtil.isNullOrEmpty(positionTitle) ? currentPositionId : positionTitle) + ":" + (UIUtil.isNullOrEmpty(partName) ? partObj.getInfo(context, SELECT_NAME) : partName));
                    }
                }
            }
            for (Object partIdObj : partIds) {
                ensureProductConfigPartRel(context, productConfigId, String.valueOf(partIdObj));
            }
            for (Object positionIdObj : positionIds) {
                String currentPositionId = String.valueOf(positionIdObj);
                if (!isProductPosition(context, productConfigId, currentPositionId)) {
                    continue;
                }
                DomainObject positionObj = DomainObject.newInstance(context, currentPositionId);
                BigDecimal nextOrder = getNextPositionPartOrder(context, currentPositionId);
                for (Object partIdObj : partIds) {
                    String partId = (String) partIdObj;
                    if (hasPositionPartByLogicalId(context, currentPositionId, partId)) {
                        continue;
                    }
                    DomainRelationship rel = DomainRelationship.connect(context, positionObj, REL_POSITION_TO_VPM, DomainObject.newInstance(context, partId));
                    DomainObject partObj = DomainObject.newInstance(context, partId);
                    Map relAttrMap = new HashMap();
                    relAttrMap.put(ATTR_ASSEMBLY_LEVEL, getAssemblyLevel(partObj.getInfo(context, SELECT_ATTR_PART_NUMBER)));
                    relAttrMap.put(ATTR_ORDER, formatOrder(nextOrder));
                    rel.setAttributeValues(context, relAttrMap);
                    createMatrixRelsForPositionPart(context, currentPositionId, partId, "");
                    nextOrder = nextOrder.add(new BigDecimal("1000"));
                }
            }
            ContextUtil.commitTransaction(context);
            Map result = success();
            if (!repeatNames.isEmpty()) {
                result.put("mess", getFrameworkString(context, "emxFramework.JFProductConfigParts.AddPartsSkippedPositions", "Part already exists in the following position products and was skipped: ") + joinSet(repeatNames));
            }
            result.put("positions", getPositionProductList(context, productConfigId));
            return result;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("addPartsToPosition error", e);
            return fail(e.getMessage());
        }
    }

    public String addPartsToPositionRest(Context context, String[] args) throws Exception {
        return new Gson().toJson(addPartsToPosition(context, args));
    }

    /**
     * 从位置产品矩阵中移除零件
     **
     * @param context
     * @param args 请求参数
     * @return String 移除结果
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String removePartsFromPosition(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String positionId = (String) params.get("positionId");
        StringList relIds = toStringList(params.get("relIds"));
        Map editableCheck = checkProductConfigEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        if (UIUtil.isNullOrEmpty(productConfigId) || relIds.isEmpty()) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.SelectPartsFirst")));
        }
        //20260813 update by caipan 升版复制关系Owner为usage agent时，提权移除位置产品零件及矩阵关系。
        boolean isPush = false;
        ContextUtil.startTransaction(context, true);
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            for (Object relIdObj : relIds) {
                String relId = String.valueOf(relIdObj);
                String currentPositionId = UIUtil.isNullOrEmpty(positionId) ? MqlUtil.mqlCommand(context, "print connection $1 select $2 dump", relId, "from.id") : positionId;
                if (isProductPosition(context, productConfigId, currentPositionId) && isPositionPartRel(context, currentPositionId, relId)) {
                    String partId = resolvePositionPartVpmId(context, relId);
                    deleteMatrixRelsForPositionPart(context, currentPositionId, relId);
                    MqlUtil.mqlCommand(context, "delete connection $1", normalizeSelectValue(relId));
                    disconnectProductConfigPartIfUnused(context, productConfigId, partId);
                }
            }
            ContextUtil.commitTransaction(context);
            Map result = success();
            result.put("positions", getPositionProductList(context, productConfigId));
            return new Gson().toJson(result);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("removePartsFromPosition error", e);
            return new Gson().toJson(fail(e.getMessage()));
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    public String removePartsFromPositionRest(Context context, String[] args) throws Exception {
        return removePartsFromPosition(context, args);
    }

    /**
     * 调整位置产品零件顺序
     **
     * @param context
     * @param args 请求参数
     * @return String 调整结果
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String setPositionPartOrder(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String positionId = (String) params.get("positionId");
        String relId = (String) params.get("relId");
        String previousRelId = (String) params.get("previousRelId");
        String nextRelId = (String) params.get("nextRelId");
        Map editableCheck = checkProductConfigEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        if (UIUtil.isNullOrEmpty(productConfigId) || UIUtil.isNullOrEmpty(positionId) || UIUtil.isNullOrEmpty(relId) || !isPositionPartRel(context, positionId, relId)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.SelectPartsFirst")));
        }
        try {
            BigDecimal newOrder = calculateNewOrder(context, positionId, previousRelId, nextRelId);
            MqlUtil.mqlCommand(context, "modify connection $1 $2 $3", relId, ATTR_ORDER, formatOrder(newOrder));
            return new Gson().toJson(success());
        } catch (Exception e) {
            LOGGER.error("setPositionPartOrder error", e);
            return new Gson().toJson(fail(e.getMessage()));
        }
    }

    public String setPositionPartOrderRest(Context context, String[] args) throws Exception {
        return setPositionPartOrder(context, args);
    }

    /**
     * 调整整车配置顺序
     **
     * @param context
     * @param args 请求参数
     * @return String 调整结果
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String setVehicleConfigOrder(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String vehicleConfigId = (String) params.get("vehicleConfigId");
        String previousVehicleConfigId = (String) params.get("previousVehicleConfigId");
        String nextVehicleConfigId = (String) params.get("nextVehicleConfigId");
        Map editableCheck = checkProductConfigEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        if (UIUtil.isNullOrEmpty(productConfigId) || UIUtil.isNullOrEmpty(vehicleConfigId) || !isProductVehicleConfig(context, productConfigId, vehicleConfigId)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.SelectVehicleConfigFirst")));
        }
        try {
            BigDecimal newOrder = calculateNewVehicleConfigOrder(context, productConfigId, previousVehicleConfigId, nextVehicleConfigId);
            DomainObject.newInstance(context, vehicleConfigId).setAttributeValue(context, ATTR_ORDER, formatOrder(newOrder));
            return new Gson().toJson(success());
        } catch (Exception e) {
            LOGGER.error("setVehicleConfigOrder error", e);
            return new Gson().toJson(fail(e.getMessage()));
        }
    }

    public String setVehicleConfigOrderRest(Context context, String[] args) throws Exception {
        return setVehicleConfigOrder(context, args);
    }

    /**
     * 更新产品配置矩阵单元格
     **
     * @param context
     * @param args 请求参数
     * @return String 更新结果
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String updateProductConfigMatrixCell(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        Map change = new HashMap();
        change.put("positionId", params.get("positionId"));
        change.put("positionPartRelId", params.get("positionPartRelId"));
        change.put("vehicleConfigId", params.get("vehicleConfigId"));
        change.put("quantity", params.get("quantity"));
        change.put("optional", params.get("optional"));
        params.put("changes", Collections.singletonList(change));
        return updateProductConfigMatrixCells(context, JPO.packArgs(params));
    }

    /**
     * 更新产品配置矩阵单元格REST入口
     **
     * @param context
     * @param args 请求参数
     * @return String 更新结果
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String updateProductConfigMatrixCellRest(Context context, String[] args) throws Exception {
        return updateProductConfigMatrixCell(context, args);
    }

    /**
     * 批量更新产品配置矩阵单元格
     **
     * @param context
     * @param args 请求参数
     * @return String 更新结果
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String updateProductConfigMatrixCells(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        java.util.List changes = params.get("changes") instanceof java.util.List ? (java.util.List) params.get("changes") : Collections.EMPTY_LIST;
        Map editableCheck = checkProductConfigEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        if (UIUtil.isNullOrEmpty(productConfigId) || changes.isEmpty()) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.LoadFailed")));
        }
        Map productVehicleConfigCache = new HashMap();
        Map positionVehicleRelCache = new HashMap();
        Map positionPartInfoCache = new HashMap();
        Map matrixRelCache = new HashMap();
        Map matrixValueCache = new HashMap();
        Set matrixLoadedPositionIds = new HashSet();
        ContextUtil.startTransaction(context, true);
        try {
            for (Object changeObj : changes) {
                Map changeMap = (Map) changeObj;
                String positionId = (String) changeMap.get("positionId");
                String positionPartRelId = (String) changeMap.get("positionPartRelId");
                String vehicleConfigId = (String) changeMap.get("vehicleConfigId");
                String quantity = (String) changeMap.get("quantity");
                String optional = (String) changeMap.get("optional");
                updateMatrixCellValue(context, productConfigId, positionId, positionPartRelId, vehicleConfigId, quantity, optional,
                        productVehicleConfigCache, positionVehicleRelCache, positionPartInfoCache, matrixRelCache, matrixValueCache, matrixLoadedPositionIds);
            }
            ContextUtil.commitTransaction(context);
            return new Gson().toJson(success());
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("updateProductConfigMatrixCells error", e);
            return new Gson().toJson(fail(e.getMessage()));
        }
    }

    /**
     * 批量更新产品配置矩阵单元格REST入口
     **
     * @param context
     * @param args 请求参数
     * @return String 更新结果
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public String updateProductConfigMatrixCellsRest(Context context, String[] args) throws Exception {
        return updateProductConfigMatrixCells(context, args);
    }

    /**
     * 导出产品配置零件矩阵Excel
     **
     * @param context
     * @param args 请求参数
     * @return XSSFWorkbook 导出Excel
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public XSSFWorkbook exportProductConfigExcel(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet partsSheet = workbook.createSheet(EXCEL_SHEET_PARTS_CONFIG);
        Sheet vehicleSheet = workbook.createSheet(EXCEL_SHEET_VEHICLE_CONFIGS);
        CellStyle headerStyle = createExcelHeaderStyle(workbook);
        CellStyle systemStyle = createExcelSystemStyle(workbook);
        CellStyle editableStyle = createExcelEditableStyle(workbook);
        CellStyle normalStyle = createExcelNormalStyle(workbook);
        CellStyle headerGroupStartStyle = createExcelGroupStartStyle(workbook, headerStyle);
        CellStyle editableGroupStartStyle = createExcelGroupStartStyle(workbook, editableStyle);
        MapList vehicleConfigs = getVehicleConfigList(context, productConfigId);
        MapList positions = getPositionProductList(context, productConfigId, true);
        createPartsConfigSheet(context, partsSheet, positions, vehicleConfigs, headerStyle, systemStyle, editableStyle, normalStyle, headerGroupStartStyle, editableGroupStartStyle);
        createVehicleConfigSheet(vehicleSheet, vehicleConfigs, headerStyle, systemStyle, editableStyle, normalStyle);
        return workbook;
    }

    /**
     * 导入产品配置零件矩阵Excel
     **
     * @param context
     * @param args 请求参数
     * @return Map 导入结果
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    public Map importProductConfigExcel(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        Map editableCheck = checkProductConfigEditable(context, productConfigId);
        if (editableCheck != null) {
            return editableCheck;
        }
        InputStream inputStream = getUploadedExcelInputStream((List) params.get("files"));
        if (inputStream == null) {
            return fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.ImportFailed", "Import failed."));
        }
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(inputStream);
            Map importData = parseProductConfigWorkbook(context, productConfigId, workbook);
            List errors = (List) importData.get("errors");
            if (!errors.isEmpty()) {
                Map result = fail(getFrameworkString(context, "emxFramework.JFProductConfigParts.ImportFailed", "Import failed.") + " " + errors.size());
                result.put("errorWorkbook", buildProductConfigErrorWorkbook(workbook, errors));
                workbook = null;
                return result;
            }
            ContextUtil.startTransaction(context, true);
            Map importReport = createImportReport();
            //20260813 update by caipan 升版复制对象和关系Owner为usage agent时，提权执行产品配置矩阵导入写入。
            boolean isPush = false;
            try {
                ContextUtil.pushContext(context);
                isPush = true;
                applyProductConfigImport(context, productConfigId, importData, importReport);
                ContextUtil.commitTransaction(context);
            } catch (Exception e) {
                ContextUtil.abortTransaction(context);
                throw e;
            } finally {
                if (isPush) {
                    ContextUtil.popContext(context);
                }
            }
            Map result = success();
            result.put("report", importReport);
            result.put("mess", buildImportReportMessage(context, importReport));
            return result;
        } catch (Exception e) {
            LOGGER.error("importProductConfigExcel error", e);
            return fail(e.getMessage());
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            inputStream.close();
        }
    }

    /**
     * 导出目标销量汇总修改Excel
     **
     * @param context
     * @param args 请求参数
     * @return XSSFWorkbook 导出Excel
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public XSSFWorkbook exportTargetSalesSummaryExcel(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        Map editableCheck = checkTargetSalesEditable(context, productConfigId);
        if (editableCheck != null) {
            throw new Exception(normalizeSelectValue(editableCheck.get("mess")));
        }
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(EXCEL_SHEET_TARGET_SALES_SUMMARY);
        CellStyle headerStyle = createExcelHeaderStyle(workbook);
        CellStyle systemStyle = createExcelSystemStyle(workbook);
        CellStyle editableStyle = createExcelEditableStyle(workbook);
        CellStyle normalStyle = createExcelNormalStyle(workbook);
        MapList targetSales = getTargetSalesList(context, productConfigId);
        MapList parts = getTargetSalesPartList(context, productConfigId, targetSales);
        writeCell(sheet, 0, 0, "Part ID", systemStyle);
        writeCell(sheet, 0, 1, getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.PartNumber", "Part Number"), headerStyle);
        writeCell(sheet, 0, 2, getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.PartName", "Part Name"), headerStyle);
        writeCell(sheet, 0, 3, getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.CustomerPartNumber", "Customer Part Number"), headerStyle);
        writeCell(sheet, 0, 4, getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.PartInfo", "Part Info"), headerStyle);
        writeCell(sheet, 0, 5, getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.Ratio", "Ratio"), headerStyle);
        for (int i = 0; i < targetSales.size(); i++) {
            Map targetMap = (Map) targetSales.get(i);
            writeCell(sheet, 0, 6 + i, normalizeSelectValue(targetMap.get("year")), editableStyle);
        }
        for (int rowIndex = 0; rowIndex < parts.size(); rowIndex++) {
            Map partMap = (Map) parts.get(rowIndex);
            Row row = sheet.createRow(rowIndex + 1);
            writeCell(row, 0, normalizeSelectValue(partMap.get(SELECT_ID)), systemStyle);
            writeCell(row, 1, normalizeSelectValue(partMap.get("partNumber")), normalStyle);
            writeCell(row, 2, normalizeSelectValue(partMap.get("partNameCN")), normalStyle);
            writeCell(row, 3, normalizeSelectValue(partMap.get("customerPartNumber")), normalStyle);
            writeCell(row, 4, normalizeSelectValue(partMap.get("partInfo")), normalStyle);
            writeCell(row, 5, normalizeSelectValue(partMap.get("ratio")), normalStyle);
            Map quantities = (Map) partMap.get("annualQuantities");
            for (int i = 0; i < targetSales.size(); i++) {
                Map targetMap = (Map) targetSales.get(i);
                String targetSalesId = normalizeSelectValue(targetMap.get(SELECT_ID));
                writeCell(row, 6 + i, quantities == null ? "" : normalizeSelectValue(quantities.get(targetSalesId)), editableStyle);
            }
        }
        sheet.setColumnHidden(0, true);
        for (int i = 0; i < 6 + targetSales.size(); i++) {
            sheet.autoSizeColumn(i);
        }
        return workbook;
    }

    /**
     * 导入目标销量汇总修改Excel
     **
     * @param context
     * @param args 请求参数
     * @return Map 导入结果
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    public Map importTargetSalesSummaryExcel(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        Map editableCheck = checkTargetSalesEditable(context, productConfigId);
        if (editableCheck != null) {
            return editableCheck;
        }
        InputStream inputStream = getUploadedExcelInputStream((List) params.get("files"));
        if (inputStream == null) {
            return fail(getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.ImportFailed", "Import failed."));
        }
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheet(EXCEL_SHEET_TARGET_SALES_SUMMARY);
            if (sheet == null) {
                return fail("Missing required sheet.");
            }
            Map yearTargetMap = getTargetSalesYearMap(context, productConfigId);
            DataFormatter formatter = new DataFormatter();
            int updated = 0;
            ContextUtil.startTransaction(context, true);
            boolean isPush = false;
            try {
                ContextUtil.pushContext(context);
                isPush = true;
                Row header = sheet.getRow(0);
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) {
                        continue;
                    }
                    String partId = normalizeSelectValue(formatter.formatCellValue(row.getCell(0)));
                    if (UIUtil.isNullOrEmpty(partId) || !isProductConfigPart(context, productConfigId, partId)) {
                        continue;
                    }
                    for (int col = 6; header != null && col < header.getLastCellNum(); col++) {
                        String year = normalizeSelectValue(formatter.formatCellValue(header.getCell(col)));
                        String targetSalesId = normalizeSelectValue(yearTargetMap.get(year));
                        if (UIUtil.isNullOrEmpty(targetSalesId)) {
                            continue;
                        }
                        String value = normalizeQuantityValue(context, formatter.formatCellValue(row.getCell(col)));
                        setTargetSalesPartAnnualQuantity(context, targetSalesId, partId, value);
                        updated++;
                    }
                }
                ContextUtil.commitTransaction(context);
            } catch (Exception e) {
                ContextUtil.abortTransaction(context);
                throw e;
            } finally {
                if (isPush) {
                    ContextUtil.popContext(context);
                }
            }
            Map result = success();
            result.put("mess", "导入成功：修改年度数量 " + updated + " 个。");
            return result;
        } catch (Exception e) {
            LOGGER.error("importTargetSalesSummaryExcel error", e);
            return fail(e.getMessage());
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            inputStream.close();
        }
    }

    /**
     * 获取上传Excel输入流
     **
     * @param files 上传文件列表
     * @return InputStream Excel输入流
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private InputStream getUploadedExcelInputStream(List files) throws Exception {
        if (files == null) {
            return null;
        }
        for (Object itemObj : files) {
            FileItem item = (FileItem) itemObj;
            if (!item.isFormField()) {
                return item.getInputStream();
            }
        }
        return null;
    }

    /**
     * 创建零件配置导出Sheet
     **
     * @param context
     * @param sheet 导出Sheet
     * @param positions 位置产品列表
     * @param vehicleConfigs 整车配置列表
     * @param headerStyle 表头样式
     * @param systemStyle 系统字段样式
     * @param editableStyle 可编辑单元格样式
     * @param normalStyle 普通单元格样式
     * @param headerGroupStartStyle 表头分组起始样式
     * @param editableGroupStartStyle 可编辑分组起始样式
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void createPartsConfigSheet(Context context, Sheet sheet, MapList positions, MapList vehicleConfigs, CellStyle headerStyle, CellStyle systemStyle, CellStyle editableStyle, CellStyle normalStyle, CellStyle headerGroupStartStyle, CellStyle editableGroupStartStyle) throws Exception {
        for (int i = 0; i <= EXCEL_ROW_PART_HEADER; i++) {
            sheet.createRow(i);
        }
        writeCell(sheet, EXCEL_ROW_FIELD_TYPE, EXCEL_COL_POSITION_ID, EXCEL_TITLE_EDIT_MARKER, systemStyle);
        writeCell(sheet, EXCEL_ROW_PART_HEADER, EXCEL_COL_POSITION_ID, "Position ID", systemStyle);
        writeCell(sheet, EXCEL_ROW_PART_HEADER, EXCEL_COL_POSITION_TITLE, getFrameworkString(context, "emxFramework.JFProductConfigParts.PositionProductName", "Position Product Name"), headerStyle);
        writeCell(sheet, EXCEL_ROW_PART_HEADER, EXCEL_COL_POSITION_PART_REL_ID, "Position Part Rel ID", systemStyle);
        writeCell(sheet, EXCEL_ROW_PART_HEADER, EXCEL_COL_PART_ID, "Part ID", systemStyle);
        writeCell(sheet, EXCEL_ROW_PART_HEADER, EXCEL_COL_PART_OPERATION, "Operation", headerStyle);
        writeCell(sheet, EXCEL_ROW_PART_HEADER, EXCEL_COL_PART_NUMBER, getFrameworkString(context, "emxFramework.JFProductConfigParts.PartNumber", "Part Number"), headerStyle);
        writeCell(sheet, EXCEL_ROW_PART_HEADER, EXCEL_COL_PART_CN, getFrameworkString(context, "emxFramework.JFProductConfigParts.PartCNName", "Part CN Name"), headerStyle);
        writeCell(sheet, EXCEL_ROW_PART_HEADER, EXCEL_COL_CUSTOMER_PART_NUMBER, getFrameworkString(context, "emxFramework.JFProductConfigParts.CustomerPartNumber", "Customer Part Number"), headerStyle);
        writeCell(sheet, EXCEL_ROW_PART_HEADER, EXCEL_COL_ASSEMBLY_LEVEL, getFrameworkString(context, "emxFramework.JFProductConfigParts.AssemblyLevel", "Assembly Level"), headerStyle);
        int col = EXCEL_COL_MATRIX_START;
        for (Object configObj : vehicleConfigs) {
            Map configMap = (Map) configObj;
            String configId = (String) configMap.get(SELECT_ID);
            String title = normalizeSelectValue(configMap.get("title"));
            String vcInfo = normalizeSelectValue(configMap.get("vcInfo"));
            writeCell(sheet, EXCEL_ROW_VEHICLE_CONFIG_ID, col, configId, systemStyle);
            writeCell(sheet, EXCEL_ROW_VEHICLE_CONFIG_ID, col + 1, configId, systemStyle);
            writeCell(sheet, EXCEL_ROW_FIELD_TYPE, col, "quantity", systemStyle);
            writeCell(sheet, EXCEL_ROW_FIELD_TYPE, col + 1, "optional", systemStyle);
            writeCell(sheet, EXCEL_ROW_CONFIG_TITLE, col, title, editableGroupStartStyle);
            writeCell(sheet, EXCEL_ROW_CONFIG_TITLE, col + 1, title, editableStyle);
            writeCell(sheet, EXCEL_ROW_CONFIG_INFO, col, vcInfo, headerGroupStartStyle);
            writeCell(sheet, EXCEL_ROW_CONFIG_INFO, col + 1, vcInfo, headerStyle);
            writeCell(sheet, EXCEL_ROW_PART_HEADER, col, getFrameworkString(context, "emxFramework.JFProductConfigParts.Quantity", "Quantity"), headerGroupStartStyle);
            writeCell(sheet, EXCEL_ROW_PART_HEADER, col + 1, getFrameworkString(context, "emxFramework.JFProductConfigParts.OptionalOrNot", "Optional"), headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(EXCEL_ROW_CONFIG_TITLE, EXCEL_ROW_CONFIG_TITLE, col, col + 1));
            sheet.addMergedRegion(new CellRangeAddress(EXCEL_ROW_CONFIG_INFO, EXCEL_ROW_CONFIG_INFO, col, col + 1));
            col += 2;
        }
        int rowIndex = EXCEL_ROW_PART_DATA_START;
        for (Object positionObj : positions) {
            Map positionMap = (Map) positionObj;
            MapList parts = (MapList) positionMap.get("parts");
            Row groupRow = sheet.createRow(rowIndex++);
            writeCell(groupRow, EXCEL_COL_POSITION_ID, normalizeSelectValue(positionMap.get(SELECT_ID)), systemStyle);
            CellStyle positionTitleStyle = JFPP_TYPE_NON_WHOLE_CHAIR.equals(normalizeSelectValue(positionMap.get(SELECT_ATTR_JFPP_TYPE)))
                    ? editableStyle
                    : headerStyle;
            writeCell(groupRow, EXCEL_COL_POSITION_TITLE, normalizeSelectValue(positionMap.get(SELECT_ATTR_TITLE)), positionTitleStyle);
            for (int titleCol = EXCEL_COL_POSITION_TITLE + 1; titleCol <= EXCEL_COL_ASSEMBLY_LEVEL; titleCol++) {
                writeCell(groupRow, titleCol, "", positionTitleStyle);
            }
            sheet.addMergedRegion(new CellRangeAddress(groupRow.getRowNum(), groupRow.getRowNum(), EXCEL_COL_POSITION_TITLE, EXCEL_COL_ASSEMBLY_LEVEL));
            if (parts == null || parts.isEmpty()) {
                continue;
            }
            for (Object partObj : parts) {
                Map partMap = (Map) partObj;
                Row row = sheet.createRow(rowIndex++);
                writeCell(row, EXCEL_COL_POSITION_ID, normalizeSelectValue(positionMap.get(SELECT_ID)), systemStyle);
                writeCell(row, EXCEL_COL_POSITION_TITLE, normalizeSelectValue(positionMap.get(SELECT_ATTR_TITLE)), normalStyle);
                writeCell(row, EXCEL_COL_POSITION_PART_REL_ID, normalizeSelectValue(partMap.get("id[connection]")), systemStyle);
                writeCell(row, EXCEL_COL_PART_ID, normalizeSelectValue(partMap.get(SELECT_ID)), systemStyle);
                writeCell(row, EXCEL_COL_PART_OPERATION, "", editableStyle);
                String partNumber = normalizeSelectValue(partMap.get(SELECT_ATTR_PART_NUMBER));
                writeCell(row, EXCEL_COL_PART_NUMBER, UIUtil.isNullOrEmpty(partNumber) ? normalizeSelectValue(partMap.get(SELECT_NAME)) : partNumber, normalStyle);
                writeCell(row, EXCEL_COL_PART_CN, normalizeSelectValue(partMap.get(SELECT_ATTR_PART_CN)), normalStyle);
                writeCell(row, EXCEL_COL_CUSTOMER_PART_NUMBER, normalizeSelectValue(partMap.get("customerPartNumber")), normalStyle);
                writeCell(row, EXCEL_COL_ASSEMBLY_LEVEL, normalizeSelectValue(partMap.get("assemblyLevel")), normalStyle);
                Map matrix = (Map) partMap.get("matrix");
                col = EXCEL_COL_MATRIX_START;
                for (Object configObj : vehicleConfigs) {
                    Map configMap = (Map) configObj;
                    Map matrixMap = matrix == null ? null : (Map) matrix.get(configMap.get(SELECT_ID));
                    writeCell(row, col, matrixMap == null ? "" : normalizeSelectValue(matrixMap.get("quantity")), editableGroupStartStyle);
                    writeCell(row, col + 1, matrixMap == null ? "" : normalizeSelectValue(matrixMap.get("optional")), editableStyle);
                    col += 2;
                }
            }
        }
        sheet.setColumnHidden(EXCEL_COL_POSITION_ID, true);
        sheet.setColumnHidden(EXCEL_COL_POSITION_PART_REL_ID, true);
        sheet.setColumnHidden(EXCEL_COL_PART_ID, true);
        sheet.getRow(EXCEL_ROW_VEHICLE_CONFIG_ID).setZeroHeight(true);
        sheet.getRow(EXCEL_ROW_FIELD_TYPE).setZeroHeight(true);
        sheet.createFreezePane(0, EXCEL_ROW_PART_DATA_START);
        int validationEndRow = Math.max(EXCEL_ROW_PART_DATA_START + 2000, rowIndex + 100);
        addListValidation(sheet, EXCEL_COL_PART_OPERATION, EXCEL_ROW_PART_DATA_START, validationEndRow, new String[]{"Add", "Remove"});
        for (int validateCol = EXCEL_COL_MATRIX_START; validateCol < col; validateCol += 2) {
            addIntegerValidation(sheet, validateCol, EXCEL_ROW_PART_DATA_START, validationEndRow);
            addListValidation(sheet, validateCol + 1, EXCEL_ROW_PART_DATA_START, validationEndRow, new String[]{"", "O"});
        }
        autosizeColumns(sheet, Math.max(EXCEL_COL_MATRIX_START, col));
    }

    /**
     * 创建整车配置导出Sheet
     **
     * @param sheet 整车配置Sheet
     * @param vehicleConfigs 整车配置列表
     * @param headerStyle 表头样式
     * @param systemStyle 系统字段样式
     * @param editableStyle 可编辑单元格样式
     * @param normalStyle 普通单元格样式
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void createVehicleConfigSheet(Sheet sheet, MapList vehicleConfigs, CellStyle headerStyle, CellStyle systemStyle, CellStyle editableStyle, CellStyle normalStyle) throws Exception {
        Row header = sheet.createRow(0);
        writeCell(header, 0, "Vehicle Config ID", systemStyle);
        writeCell(header, 1, "Operation", headerStyle);
        writeCell(header, 2, "Title", headerStyle);
        writeCell(header, 3, "Config Info", headerStyle);
        int rowIndex = 1;
        for (Object configObj : vehicleConfigs) {
            Map configMap = (Map) configObj;
            Row row = sheet.createRow(rowIndex++);
            writeCell(row, 0, normalizeSelectValue(configMap.get(SELECT_ID)), systemStyle);
            writeCell(row, 1, "", editableStyle);
            writeCell(row, 2, normalizeSelectValue(configMap.get("title")), editableStyle);
            writeCell(row, 3, normalizeSelectValue(configMap.get("vcInfo")), editableStyle);
        }
        sheet.setColumnHidden(0, true);
        addListValidation(sheet, 1, 1, Math.max(2000, rowIndex + 100), new String[]{"Add", "Remove"});
        autosizeColumns(sheet, 4);
    }

    /**
     * 解析产品配置导入Excel
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param workbook 导入Excel
     * @return Map 导入数据
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private Map parseProductConfigWorkbook(Context context, String productConfigId, Workbook workbook) throws Exception {
        Map result = new HashMap();
        List errors = new ArrayList();
        result.put("errors", errors);
        Sheet partsSheet = workbook.getSheet(EXCEL_SHEET_PARTS_CONFIG);
        Sheet vehicleSheet = workbook.getSheet(EXCEL_SHEET_VEHICLE_CONFIGS);
        if (partsSheet == null || vehicleSheet == null) {
            errors.add(createImportError(EXCEL_SHEET_PARTS_CONFIG, 1, 1, "Missing required sheet."));
            return result;
        }
        boolean titleEditEnabled = EXCEL_TITLE_EDIT_MARKER.equals(
                getCellText(partsSheet.getRow(EXCEL_ROW_FIELD_TYPE), EXCEL_COL_POSITION_ID));
        Map existingVehicleMap = getExistingVehicleConfigMap(context, productConfigId);
        Map existingPositionMaps = getExistingPositionMaps(context, productConfigId);
        Map existingPositionMap = (Map) existingPositionMaps.get("byTitle");
        Map existingPositionIdMap = (Map) existingPositionMaps.get("byId");
        Map optionalValueMap = getOptionalImportValueMap(context);
        Map partsVehicleTitleMap = titleEditEnabled
                ? getPartsVehicleTitleMap(partsSheet, existingVehicleMap)
                : new HashMap();
        List vehicleRows = parseVehicleConfigRows(vehicleSheet, existingVehicleMap, partsVehicleTitleMap, errors);
        Map vehicleTitleToRow = new HashMap();
        for (Object rowObj : vehicleRows) {
            Map rowMap = (Map) rowObj;
            if (!isRemoveOperation((String) rowMap.get("operation"))) {
                vehicleTitleToRow.put(rowMap.get("title"), rowMap);
            }
        }
        List matrixColumns = parseMatrixColumns(partsSheet, existingVehicleMap, vehicleTitleToRow, errors);
        List positionTitleRows = new ArrayList();
        if (titleEditEnabled) {
            positionTitleRows = parsePositionTitleRows(context, partsSheet, existingPositionMap, existingPositionIdMap, errors);
        }
        List partRows = parsePartConfigRows(context, productConfigId, partsSheet, existingPositionMap, matrixColumns, optionalValueMap, errors);
        result.put("vehicleRows", vehicleRows);
        result.put("positionTitleRows", positionTitleRows);
        result.put("matrixColumns", matrixColumns);
        result.put("partRows", partRows);
        return result;
    }

    /**
     * 解析整车配置Sheet
     **
     * @param sheet 整车配置Sheet
     * @param existingVehicleMap 已有整车配置Map
     * @param partsVehicleTitleMap PartsConfig中的整车配置标题Map
     * @param errors 错误列表
     * @return List 整车配置导入行
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private List parseVehicleConfigRows(Sheet sheet, Map existingVehicleMap, Map partsVehicleTitleMap, List errors) throws Exception {
        List rows = new ArrayList();
        Set titleSet = new HashSet();
        int lastRow = sheet.getLastRowNum();
        for (int i = 1; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String vehicleConfigId = getCellText(row, 0);
            String operation = getCellText(row, 1).toUpperCase();
            String title = getCellText(row, 2);
            String vcInfo = getCellText(row, 3);
            Map partsTitleMap = (Map) partsVehicleTitleMap.get(vehicleConfigId);
            if (partsTitleMap != null) {
                title = normalizeSelectValue(partsTitleMap.get("title"));
            }
            if (UIUtil.isNullOrEmpty(vehicleConfigId) && UIUtil.isNullOrEmpty(operation) && UIUtil.isNullOrEmpty(title) && UIUtil.isNullOrEmpty(vcInfo)) {
                continue;
            }
            if (UIUtil.isNotNullAndNotEmpty(vehicleConfigId) && !existingVehicleMap.containsKey(vehicleConfigId) && !isRemoveOperation(operation)) {
                errors.add(createImportError(EXCEL_SHEET_VEHICLE_CONFIGS, i + 1, 1, "Vehicle config id does not exist."));
                continue;
            }
            if (isRemoveOperation(operation)) {
                if (UIUtil.isNullOrEmpty(vehicleConfigId)) {
                    errors.add(createImportError(EXCEL_SHEET_VEHICLE_CONFIGS, i + 1, 2, "Remove requires vehicle config id."));
                }
            } else {
                if (UIUtil.isNullOrEmpty(title)) {
                    addVehicleTitleImportError(errors, i + 1, partsTitleMap, "Vehicle config title is required.");
                }
                if (UIUtil.isNullOrEmpty(vcInfo)) {
                    errors.add(createImportError(EXCEL_SHEET_VEHICLE_CONFIGS, i + 1, 4, "Config info is required."));
                }
                if (UIUtil.isNotNullAndNotEmpty(title) && titleSet.contains(title)) {
                    addVehicleTitleImportError(errors, i + 1, partsTitleMap, "Vehicle config title duplicated in Excel.");
                }
                for (Object existingObj : existingVehicleMap.values()) {
                    Map existingMap = (Map) existingObj;
                    String existingId = normalizeSelectValue(existingMap.get(SELECT_ID));
                    String existingTitle = normalizeSelectValue(existingMap.get("title"));
                    if (title.equals(existingTitle) && !vehicleConfigId.equals(existingId)
                            && !partsVehicleTitleMap.containsKey(existingId)) {
                        addVehicleTitleImportError(errors, i + 1, partsTitleMap, "Vehicle config title already exists.");
                    }
                }
                titleSet.add(title);
            }
            Map rowMap = new HashMap();
            rowMap.put("row", String.valueOf(i));
            rowMap.put("excelRow", String.valueOf(i + 1));
            rowMap.put("vehicleConfigId", vehicleConfigId);
            rowMap.put("operation", operation);
            rowMap.put("title", title);
            rowMap.put("vcInfo", vcInfo);
            rows.add(rowMap);
        }
        return rows;
    }

    /**
     * 解析矩阵整车配置列
     **
     * @param sheet 零件配置Sheet
     * @param existingVehicleMap 已有整车配置Map
     * @param vehicleTitleToRow 整车配置标题Map
     * @param errors 错误列表
     * @return List 矩阵列定义
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private List parseMatrixColumns(Sheet sheet, Map existingVehicleMap, Map vehicleTitleToRow, List errors) throws Exception {
        List columns = new ArrayList();
        Row idRow = sheet.getRow(EXCEL_ROW_VEHICLE_CONFIG_ID);
        Row typeRow = sheet.getRow(EXCEL_ROW_FIELD_TYPE);
        Row titleRow = sheet.getRow(EXCEL_ROW_CONFIG_TITLE);
        int lastCell = titleRow == null ? 0 : titleRow.getLastCellNum();
        for (int col = EXCEL_COL_MATRIX_START; col < lastCell; col += 2) {
            String vehicleConfigId = getCellText(idRow, col);
            String quantityType = getCellText(typeRow, col);
            String optionalType = getCellText(typeRow, col + 1);
            String title = getCellText(titleRow, col);
            if (UIUtil.isNullOrEmpty(vehicleConfigId) && UIUtil.isNullOrEmpty(title)) {
                continue;
            }
            if (!"quantity".equals(quantityType) || !"optional".equals(optionalType)) {
                errors.add(createImportError(EXCEL_SHEET_PARTS_CONFIG, EXCEL_ROW_FIELD_TYPE + 1, col + 1, "Invalid matrix field type."));
            }
            if (UIUtil.isNotNullAndNotEmpty(vehicleConfigId) && !existingVehicleMap.containsKey(vehicleConfigId)) {
                errors.add(createImportError(EXCEL_SHEET_PARTS_CONFIG, EXCEL_ROW_VEHICLE_CONFIG_ID + 1, col + 1, "Vehicle config id does not exist."));
            }
            if (UIUtil.isNullOrEmpty(vehicleConfigId) && !vehicleTitleToRow.containsKey(title)) {
                errors.add(createImportError(EXCEL_SHEET_PARTS_CONFIG, EXCEL_ROW_CONFIG_TITLE + 1, col + 1, "New vehicle config must be defined in VehicleConfigs sheet."));
            }
            Map columnMap = new HashMap();
            columnMap.put("vehicleConfigId", vehicleConfigId);
            columnMap.put("title", title);
            columnMap.put("quantityCol", String.valueOf(col));
            columnMap.put("optionalCol", String.valueOf(col + 1));
            columns.add(columnMap);
        }
        return columns;
    }

    /**
     * 获取PartsConfig中已有整车配置的表头标题
     **
     * @param sheet PartsConfig Sheet
     * @param existingVehicleMap 已有整车配置Map
     * @return Map 整车配置ID到表头标题和列索引映射
     * @throws Exception
     * @author caipan
     * @date 2026/8/3 16:13
     */
    private Map getPartsVehicleTitleMap(Sheet sheet, Map existingVehicleMap) throws Exception {
        Map result = new HashMap();
        Row idRow = sheet.getRow(EXCEL_ROW_VEHICLE_CONFIG_ID);
        Row titleRow = sheet.getRow(EXCEL_ROW_CONFIG_TITLE);
        int lastCell = titleRow == null ? 0 : titleRow.getLastCellNum();
        for (int col = EXCEL_COL_MATRIX_START; col < lastCell; col += 2) {
            String vehicleConfigId = getCellText(idRow, col);
            if (UIUtil.isNullOrEmpty(vehicleConfigId) || !existingVehicleMap.containsKey(vehicleConfigId)) {
                continue;
            }
            Map titleMap = new HashMap();
            titleMap.put("title", getCellText(titleRow, col));
            titleMap.put("col", String.valueOf(col));
            result.put(vehicleConfigId, titleMap);
        }
        return result;
    }

    /**
     * 增加整车配置标题导入错误
     **
     * @param errors 错误列表
     * @param vehicleExcelRow VehicleConfigs行号
     * @param partsTitleMap PartsConfig表头信息
     * @param message 错误信息
     * @throws Exception
     * @author caipan
     * @date 2026/8/3 16:13
     */
    private void addVehicleTitleImportError(List errors, int vehicleExcelRow, Map partsTitleMap, String message) throws Exception {
        if (partsTitleMap != null) {
            errors.add(createImportError(EXCEL_SHEET_PARTS_CONFIG,
                    EXCEL_ROW_CONFIG_TITLE + 1,
                    Integer.parseInt(normalizeSelectValue(partsTitleMap.get("col"))) + 1,
                    message));
        } else {
            errors.add(createImportError(EXCEL_SHEET_VEHICLE_CONFIGS, vehicleExcelRow, 3, message));
        }
    }

    /**
     * 解析PartsConfig位置产品分组标题
     **
     * @param context
     * @param sheet PartsConfig Sheet
     * @param existingPositionMap 位置产品标题到ID映射
     * @param existingPositionIdMap 位置产品ID到数据映射
     * @param errors 错误列表
     * @return List 位置产品标题导入行
     * @throws Exception
     * @author caipan
     * @date 2026/8/3 16:13
     */
    private List parsePositionTitleRows(Context context, Sheet sheet, Map existingPositionMap, Map existingPositionIdMap, List errors) throws Exception {
        List rows = new ArrayList();
        Map rowByPositionId = new HashMap();
        int lastRow = sheet.getLastRowNum();
        for (int i = EXCEL_ROW_PART_DATA_START; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String positionId = getCellText(row, EXCEL_COL_POSITION_ID);
            if (UIUtil.isNullOrEmpty(positionId)
                    || UIUtil.isNotNullAndNotEmpty(getCellText(row, EXCEL_COL_POSITION_PART_REL_ID))
                    || UIUtil.isNotNullAndNotEmpty(getCellText(row, EXCEL_COL_PART_ID))
                    || UIUtil.isNotNullAndNotEmpty(getCellText(row, EXCEL_COL_PART_OPERATION))
                    || UIUtil.isNotNullAndNotEmpty(getCellText(row, EXCEL_COL_PART_NUMBER))) {
                continue;
            }
            if (!existingPositionIdMap.containsKey(positionId)) {
                errors.add(createImportError(EXCEL_SHEET_PARTS_CONFIG, i + 1,
                        EXCEL_COL_POSITION_ID + 1, "Position product does not exist."));
                continue;
            }
            if (rowByPositionId.containsKey(positionId)) {
                errors.add(createImportError(EXCEL_SHEET_PARTS_CONFIG, i + 1,
                        EXCEL_COL_POSITION_TITLE + 1, "Position product title duplicated in Excel."));
                continue;
            }
            Map existingMap = (Map) existingPositionIdMap.get(positionId);
            String oldTitle = normalizeSelectValue(existingMap.get(SELECT_ATTR_TITLE));
            String title = getCellText(row, EXCEL_COL_POSITION_TITLE);
            if (UIUtil.isNullOrEmpty(title)) {
                errors.add(createImportError(EXCEL_SHEET_PARTS_CONFIG, i + 1,
                        EXCEL_COL_POSITION_TITLE + 1,
                        getFrameworkString(context, "emxFramework.JFProductConfigParts.PositionProductNameRequired")));
            }
            if (!safeEquals(oldTitle, title)
                    && !JFPP_TYPE_NON_WHOLE_CHAIR.equals(normalizeSelectValue(existingMap.get(SELECT_ATTR_JFPP_TYPE)))) {
                errors.add(createImportError(EXCEL_SHEET_PARTS_CONFIG, i + 1,
                        EXCEL_COL_POSITION_TITLE + 1,
                        getFrameworkString(context, "emxFramework.JFProductConfigParts.PositionProductNameNotEditable")));
            }
            Map rowMap = new HashMap();
            rowMap.put("row", String.valueOf(i));
            rowMap.put("excelRow", String.valueOf(i + 1));
            rowMap.put("positionId", positionId);
            rowMap.put("oldTitle", oldTitle);
            rowMap.put("title", title);
            rows.add(rowMap);
            rowByPositionId.put(positionId, rowMap);
        }

        Map titleOwnerMap = new HashMap();
        for (Object existingObj : existingPositionIdMap.values()) {
            Map existingMap = (Map) existingObj;
            String positionId = normalizeSelectValue(existingMap.get(SELECT_ID));
            Map rowMap = (Map) rowByPositionId.get(positionId);
            String title = rowMap == null
                    ? normalizeSelectValue(existingMap.get(SELECT_ATTR_TITLE))
                    : normalizeSelectValue(rowMap.get("title"));
            if (UIUtil.isNullOrEmpty(title)) {
                continue;
            }
            if (titleOwnerMap.containsKey(title) && !positionId.equals(titleOwnerMap.get(title))) {
                String ownerId = normalizeSelectValue(titleOwnerMap.get(title));
                Map errorRow = rowMap == null ? (Map) rowByPositionId.get(ownerId) : rowMap;
                if (errorRow != null) {
                    errors.add(createImportError(EXCEL_SHEET_PARTS_CONFIG,
                            Integer.parseInt((String) errorRow.get("excelRow")),
                            EXCEL_COL_POSITION_TITLE + 1,
                            getFrameworkString(context, "emxFramework.JFProductConfigParts.PositionProductNameExists")));
                }
            } else {
                titleOwnerMap.put(title, positionId);
            }
            existingPositionMap.put(title, positionId);
        }
        return rows;
    }

    /**
     * 解析零件配置行
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param sheet 零件配置Sheet
     * @param existingPositionMap 已有位置产品Map
     * @param matrixColumns 矩阵列定义
     * @param optionalValueMap 是否选配导入值Map
     * @param errors 错误列表
     * @return List 零件配置导入行
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private List parsePartConfigRows(Context context, String productConfigId, Sheet sheet, Map existingPositionMap, List matrixColumns, Map optionalValueMap, List errors) throws Exception {
        List rows = new ArrayList();
        int lastRow = sheet.getLastRowNum();
        String currentPositionId = "";
        Map partNumberCache = new HashMap();
        for (int i = EXCEL_ROW_PART_DATA_START; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String positionId = getCellText(row, EXCEL_COL_POSITION_ID);
            String positionTitle = getCellText(row, EXCEL_COL_POSITION_TITLE);
            String positionPartRelId = getCellText(row, EXCEL_COL_POSITION_PART_REL_ID);
            String partId = getCellText(row, EXCEL_COL_PART_ID);
            String operation = getCellText(row, EXCEL_COL_PART_OPERATION).toUpperCase();
            String partNumber = getCellText(row, EXCEL_COL_PART_NUMBER);
            String titlePositionId = "";
            if (UIUtil.isNullOrEmpty(positionId) && UIUtil.isNotNullAndNotEmpty(positionTitle)) {
                titlePositionId = (String) existingPositionMap.get(positionTitle);
            }
            if (UIUtil.isNotNullAndNotEmpty(titlePositionId)) {
                positionId = titlePositionId;
            }
            if (UIUtil.isNotNullAndNotEmpty(positionId) && existingPositionMap.containsValue(positionId)) {
                currentPositionId = positionId;
            }
            if (isBlankPartImportRow(row, matrixColumns)) {
                continue;
            }
            if (UIUtil.isNullOrEmpty(partNumber)) {
                continue;
            }
            if (UIUtil.isNullOrEmpty(positionId)) {
                positionId = currentPositionId;
            }
            if (UIUtil.isNullOrEmpty(positionId) || !existingPositionMap.containsValue(positionId)) {
                errors.add(createImportError(EXCEL_SHEET_PARTS_CONFIG, i + 1, EXCEL_COL_POSITION_TITLE + 1, "Position product does not exist."));
            }
            if (EXCEL_OPERATION_ADD.equalsIgnoreCase(operation)) {
                positionPartRelId = "";
                partId = "";
            }
            if (UIUtil.isNullOrEmpty(partId) && UIUtil.isNotNullAndNotEmpty(partNumber)) {
                partId = (String) partNumberCache.get(partNumber);
                if (partId == null) {
                    partId = resolveVpmReferenceIdByPartNumber(context, partNumber);
                    partNumberCache.put(partNumber, partId);
                }
                if (UIUtil.isNullOrEmpty(partId)) {
                    errors.add(createImportError(EXCEL_SHEET_PARTS_CONFIG, i + 1, EXCEL_COL_PART_NUMBER + 1, "Cannot find VPMReference by part number."));
                }
            }
            List matrixValues = new ArrayList();
            for (Object columnObj : matrixColumns) {
                Map columnMap = (Map) columnObj;
                int quantityCol = Integer.parseInt((String) columnMap.get("quantityCol"));
                int optionalCol = Integer.parseInt((String) columnMap.get("optionalCol"));
                String quantity = getCellText(row, quantityCol);
                String optional = getCellText(row, optionalCol);
                if (UIUtil.isNotNullAndNotEmpty(quantity) && !quantity.matches("\\d+")) {
                    errors.add(createImportError(EXCEL_SHEET_PARTS_CONFIG, i + 1, quantityCol + 1, getFrameworkString(context, "emxFramework.JFProductConfigParts.QuantityNumberOnly", "Quantity can only contain numbers.")));
                }
                String optionalValue = normalizeOptionalImportValue(optional, optionalValueMap);
                if (UIUtil.isNotNullAndNotEmpty(optional) && optionalValue == null) {
                    errors.add(createImportError(EXCEL_SHEET_PARTS_CONFIG, i + 1, optionalCol + 1, "Invalid optional value."));
                }
                Map matrixMap = new HashMap();
                matrixMap.put("vehicleConfigId", columnMap.get("vehicleConfigId"));
                matrixMap.put("vehicleConfigTitle", columnMap.get("title"));
                matrixMap.put("quantity", quantity);
                matrixMap.put("optional", optionalValue == null ? "" : optionalValue);
                matrixValues.add(matrixMap);
            }
            Map rowMap = new HashMap();
            rowMap.put("row", String.valueOf(i));
            rowMap.put("excelRow", String.valueOf(i + 1));
            rowMap.put("positionId", positionId);
            rowMap.put("positionPartRelId", positionPartRelId);
            rowMap.put("partId", partId);
            rowMap.put("operation", operation);
            rowMap.put("partNumber", partNumber);
            rowMap.put("matrixValues", matrixValues);
            rows.add(rowMap);
        }
        return rows;
    }

    /**
     * 应用产品配置导入数据
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param importData 导入数据
     * @param importReport 导入成功报告
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void applyProductConfigImport(Context context, String productConfigId, Map importData, Map importReport) throws Exception {
        applyPositionTitleImport(context, productConfigId, (List) importData.get("positionTitleRows"), importReport);
        Map titleToVehicleId = applyVehicleConfigImport(context, productConfigId, (List) importData.get("vehicleRows"), importReport);
        Set removedVehicleConfigIds = getRemovedVehicleConfigIds((List) importData.get("vehicleRows"));
        List partRows = (List) importData.get("partRows");
        Map productVehicleConfigCache = new HashMap();
        Map positionVehicleRelCache = new HashMap();
        Map positionPartInfoCache = new HashMap();
        Map matrixRelCache = new HashMap();
        Map matrixValueCache = new HashMap();
        Set matrixLoadedPositionIds = new HashSet();
        for (Object rowObj : partRows) {
            Map rowMap = (Map) rowObj;
            String operation = (String) rowMap.get("operation");
            String positionId = (String) rowMap.get("positionId");
            String positionPartRelId = (String) rowMap.get("positionPartRelId");
            String partId = (String) rowMap.get("partId");
            if (isRemoveOperation(operation)) {
                if (UIUtil.isNotNullAndNotEmpty(positionPartRelId) && isPositionPartRel(context, positionId, positionPartRelId)) {
                    String removePartId = UIUtil.isNullOrEmpty(partId) ? resolvePositionPartVpmId(context, positionPartRelId) : partId;
                    deleteMatrixRelsForPositionPart(context, positionId, positionPartRelId);
                    MqlUtil.mqlCommand(context, "delete connection $1", normalizeSelectValue(positionPartRelId));
                    disconnectProductConfigPartIfUnused(context, productConfigId, removePartId);
                    increaseImportReport(importReport, "removedParts");
                } else {
                    addImportIgnoredRow(importReport, EXCEL_SHEET_PARTS_CONFIG, parseImportExcelRow(rowMap), "Remove relation already not exists.");
                }
                continue;
            }
            if (UIUtil.isNullOrEmpty(positionPartRelId)) {
                boolean addedPart = isImportNewPositionPart(context, positionId, positionPartRelId, partId);
                positionPartRelId = ensureImportPositionPartRel(context, productConfigId, positionId, positionPartRelId, partId);
                if (addedPart) {
                    increaseImportReport(importReport, "addedParts");
                }
            }
            List matrixValues = (List) rowMap.get("matrixValues");
            for (Object matrixObj : matrixValues) {
                Map matrixMap = (Map) matrixObj;
                String vehicleConfigId = (String) matrixMap.get("vehicleConfigId");
                if (UIUtil.isNullOrEmpty(vehicleConfigId)) {
                    vehicleConfigId = (String) titleToVehicleId.get(matrixMap.get("vehicleConfigTitle"));
                }
                if (UIUtil.isNullOrEmpty(vehicleConfigId) || removedVehicleConfigIds.contains(vehicleConfigId)) {
                    continue;
                }
                if (updateMatrixCellValue(context,
                        productConfigId,
                        positionId,
                        positionPartRelId,
                        vehicleConfigId,
                        (String) matrixMap.get("quantity"),
                        (String) matrixMap.get("optional"),
                        productVehicleConfigCache,
                        positionVehicleRelCache,
                        positionPartInfoCache,
                        matrixRelCache,
                        matrixValueCache,
                        matrixLoadedPositionIds)) {
                    increaseImportReport(importReport, "updatedMatrixCells");
                }
            }
        }
    }

    /**
     * 应用位置产品标题导入数据
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param positionTitleRows 位置产品标题导入行
     * @param importReport 导入成功报告
     * @throws Exception
     * @author caipan
     * @date 2026/8/3 16:13
     */
    private void applyPositionTitleImport(Context context, String productConfigId, List positionTitleRows, Map importReport) throws Exception {
        if (positionTitleRows == null) {
            return;
        }
        for (Object rowObj : positionTitleRows) {
            Map rowMap = (Map) rowObj;
            String positionId = normalizeSelectValue(rowMap.get("positionId"));
            String oldTitle = normalizeSelectValue(rowMap.get("oldTitle"));
            String title = normalizeSelectValue(rowMap.get("title"));
            if (safeEquals(oldTitle, title)) {
                continue;
            }
            if (!isProductPosition(context, productConfigId, positionId)) {
                throw new Exception("Position product does not exist.");
            }
            DomainObject positionObj = DomainObject.newInstance(context, positionId);
            if (!JFPP_TYPE_NON_WHOLE_CHAIR.equals(positionObj.getInfo(context, SELECT_ATTR_JFPP_TYPE))) {
                throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigParts.PositionProductNameNotEditable"));
            }
            Map attrMap = new HashMap();
            attrMap.put(ATTR_TITLE, title);
            attrMap.put(ATTR_JFPP_TITLE, title);
            positionObj.setAttributeValues(context, attrMap);
            increaseImportReport(importReport, "updatedPositionProducts");
        }
    }

    /**
     * 获取导入中标记移除的整车配置ID
     **
     * @param vehicleRows 整车配置导入行
     * @return Set 标记移除的整车配置ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private Set getRemovedVehicleConfigIds(List vehicleRows) throws Exception {
        Set result = new HashSet();
        if (vehicleRows == null) {
            return result;
        }
        for (Object rowObj : vehicleRows) {
            Map rowMap = (Map) rowObj;
            String vehicleConfigId = (String) rowMap.get("vehicleConfigId");
            if (UIUtil.isNotNullAndNotEmpty(vehicleConfigId) && isRemoveOperation((String) rowMap.get("operation"))) {
                result.add(vehicleConfigId);
            }
        }
        return result;
    }

    /**
     * 应用整车配置导入数据
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param vehicleRows 整车配置导入行
     * @param importReport 导入成功报告
     * @return Map 整车配置标题到ID映射
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private Map applyVehicleConfigImport(Context context, String productConfigId, List vehicleRows, Map importReport) throws Exception {
        Map titleToVehicleId = new HashMap();
        MapList vehicleConfigs = getVehicleConfigList(context, productConfigId);
        for (Object configObj : vehicleConfigs) {
            Map configMap = (Map) configObj;
            titleToVehicleId.put(configMap.get("title"), configMap.get(SELECT_ID));
        }
        for (Object rowObj : vehicleRows) {
            Map rowMap = (Map) rowObj;
            String vehicleConfigId = (String) rowMap.get("vehicleConfigId");
            String operation = (String) rowMap.get("operation");
            String title = (String) rowMap.get("title");
            String vcInfo = (String) rowMap.get("vcInfo");
            if (isRemoveOperation(operation)) {
                if (UIUtil.isNotNullAndNotEmpty(vehicleConfigId) && isProductVehicleConfig(context, productConfigId, vehicleConfigId)) {
                    deletePositionVehicleConfigRelations(context, productConfigId, vehicleConfigId);
                    disconnectVehicleConfigFromProductConfig(context, productConfigId, vehicleConfigId);
                    DomainObject.newInstance(context, vehicleConfigId).deleteObject(context);
                    increaseImportReport(importReport, "removedVehicleConfigs");
                } else {
                    addImportIgnoredRow(importReport, EXCEL_SHEET_VEHICLE_CONFIGS, parseImportExcelRow(rowMap), "Remove object already not exists.");
                }
                continue;
            }
            if (UIUtil.isNotNullAndNotEmpty(vehicleConfigId)) {
                DomainObject vehicleObj = DomainObject.newInstance(context, vehicleConfigId);
                String oldTitle = normalizeSelectValue(vehicleObj.getInfo(context, SELECT_ATTR_TITLE));
                String oldVcInfo = normalizeSelectValue(vehicleObj.getInfo(context, SELECT_ATTR_VC_INFO));
                Map attrMap = new HashMap();
                attrMap.put(ATTR_TITLE, title);
                attrMap.put(ATTR_VC_INFO, vcInfo);
                vehicleObj.setAttributeValues(context, attrMap);
                if (!safeEquals(oldTitle, title) || !safeEquals(oldVcInfo, vcInfo)) {
                    increaseImportReport(importReport, "updatedVehicleConfigs");
                }
                titleToVehicleId.put(title, vehicleConfigId);
            } else {
                DomainObject vehicleObj = createAutoNamedObject(context, SYMBOLIC_TYPE_VEHICLE_CONFIGURATION, SYMBOLIC_POLICY_VEHICLE_CONFIGURATION);
                Map attrMap = new HashMap();
                attrMap.put(ATTR_TITLE, title);
                attrMap.put(ATTR_VC_INFO, vcInfo);
                attrMap.put(ATTR_ORDER, formatOrder(getNextVehicleConfigOrder(getVehicleConfigList(context, productConfigId))));
                vehicleObj.setAttributeValues(context, attrMap);
                DomainRelationship.connect(context,
                        DomainObject.newInstance(context, productConfigId),
                        REL_PRODUCT_CONFIG_TO_VEHICLE_CONFIG,
                        vehicleObj);
                connectVehicleConfigToPositions(context, productConfigId, vehicleObj.getId(context), "");
                increaseImportReport(importReport, "addedVehicleConfigs");
                titleToVehicleId.put(title, vehicleObj.getId(context));
            }
        }
        return titleToVehicleId;
    }

    /**
     * 确保导入零件已关联位置产品
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param positionId 位置产品ID
     * @param positionPartRelId 位置产品零件关系ID
     * @param partId 零件ID
     * @return String 位置产品零件关系ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private String ensureImportPositionPartRel(Context context, String productConfigId, String positionId, String positionPartRelId, String partId) throws Exception {
        if (UIUtil.isNotNullAndNotEmpty(positionPartRelId) && isPositionPartRel(context, positionId, positionPartRelId)) {
            return positionPartRelId;
        }
        if (UIUtil.isNullOrEmpty(partId)) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigParts.SelectPartsFirst"));
        }
        ensureProductConfigPartRel(context, productConfigId, partId);
        String existingRelId = getPositionPartRelIdByLogicalId(context, positionId, partId);
        if (UIUtil.isNotNullAndNotEmpty(existingRelId)) {
            return existingRelId;
        }
        DomainRelationship rel = DomainRelationship.connect(context,
                DomainObject.newInstance(context, positionId),
                REL_POSITION_TO_VPM,
                DomainObject.newInstance(context, partId));
        DomainObject partObj = DomainObject.newInstance(context, partId);
        Map relAttrMap = new HashMap();
        relAttrMap.put(ATTR_ASSEMBLY_LEVEL, getAssemblyLevel(partObj.getInfo(context, SELECT_ATTR_PART_NUMBER)));
        relAttrMap.put(ATTR_ORDER, formatOrder(getNextPositionPartOrder(context, positionId)));
        rel.setAttributeValues(context, relAttrMap);
        createMatrixRelsForPositionPart(context, positionId, partId, "");
        return getPositionPartRelIdByLogicalId(context, positionId, partId);
    }


    /**
     * 创建导入成功报告
     **
     * @return Map 导入报告计数
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private Map createImportReport() {
        Map report = new HashMap();
        report.put("addedParts", Integer.valueOf(0));
        report.put("removedParts", Integer.valueOf(0));
        report.put("updatedPositionProducts", Integer.valueOf(0));
        report.put("addedVehicleConfigs", Integer.valueOf(0));
        report.put("removedVehicleConfigs", Integer.valueOf(0));
        report.put("updatedVehicleConfigs", Integer.valueOf(0));
        report.put("updatedMatrixCells", Integer.valueOf(0));
        report.put("ignoredRows", new ArrayList());
        return report;
    }

    /**
     * 增加导入报告计数
     **
     * @param importReport 导入报告
     * @param key 计数字段
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void increaseImportReport(Map importReport, String key) {
        Integer count = (Integer) importReport.get(key);
        importReport.put(key, Integer.valueOf(count == null ? 1 : count.intValue() + 1));
    }

    /**
     * 增加导入忽略行报告
     **
     * @param importReport 导入报告
     * @param sheet Sheet名称
     * @param row 行号
     * @param reason 忽略原因
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void addImportIgnoredRow(Map importReport, String sheet, int row, String reason) {
        List ignoredRows = (List) importReport.get("ignoredRows");
        if (ignoredRows == null) {
            ignoredRows = new ArrayList();
            importReport.put("ignoredRows", ignoredRows);
        }
        ignoredRows.add(sheet + "!" + row + " " + reason);
    }

    /**
     * 构建导入成功报告提示
     **
     * @param context
     * @param importReport 导入报告
     * @return String 导入成功报告提示
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private String buildImportReportMessage(Context context, Map importReport) {
        return "导入成功：新增零件 " + getImportReportCount(importReport, "addedParts")
                + " 个，移除零件 " + getImportReportCount(importReport, "removedParts")
                + " 个，修改位置产品 " + getImportReportCount(importReport, "updatedPositionProducts")
                + " 个，新增整车配置 " + getImportReportCount(importReport, "addedVehicleConfigs")
                + " 个，移除整车配置 " + getImportReportCount(importReport, "removedVehicleConfigs")
                + " 个，修改整车配置 " + getImportReportCount(importReport, "updatedVehicleConfigs")
                + " 个，修改数量/是否选配单元格 " + getImportReportCount(importReport, "updatedMatrixCells")
                + " 个。" + buildImportIgnoredRowsMessage(importReport);
    }

    /**
     * 构建导入忽略行报告提示
     **
     * @param importReport 导入报告
     * @return String 忽略行报告提示
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private String buildImportIgnoredRowsMessage(Map importReport) {
        List ignoredRows = (List) importReport.get("ignoredRows");
        if (ignoredRows == null || ignoredRows.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(" 忽略行：");
        for (int i = 0; i < ignoredRows.size(); i++) {
            if (i > 0) {
                builder.append("；");
            }
            builder.append(ignoredRows.get(i));
        }
        return builder.toString();
    }

    /**
     * 获取导入报告计数
     **
     * @param importReport 导入报告
     * @param key 计数字段
     * @return int 报告计数
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private int getImportReportCount(Map importReport, String key) {
        Integer count = (Integer) importReport.get(key);
        return count == null ? 0 : count.intValue();
    }

    /**
     * 获取导入Excel行号
     **
     * @param rowMap 导入行数据
     * @return int Excel行号
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private int parseImportExcelRow(Map rowMap) {
        String excelRow = (String) rowMap.get("excelRow");
        if (UIUtil.isNotNullAndNotEmpty(excelRow)) {
            return Integer.parseInt(excelRow);
        }
        String row = (String) rowMap.get("row");
        return UIUtil.isNullOrEmpty(row) ? 0 : Integer.parseInt(row) + 1;
    }

    /**
     * 判断导入行是否会新增位置产品零件关系
     **
     * @param context
     * @param positionId 位置产品ID
     * @param positionPartRelId 位置产品零件关系ID
     * @param partId 零件ID
     * @return boolean 需要新增位置产品零件关系时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private boolean isImportNewPositionPart(Context context, String positionId, String positionPartRelId, String partId) throws Exception {
        if (UIUtil.isNotNullAndNotEmpty(positionPartRelId) && isPositionPartRel(context, positionId, positionPartRelId)) {
            return false;
        }
        if (UIUtil.isNullOrEmpty(partId)) {
            return false;
        }
        return UIUtil.isNullOrEmpty(getPositionPartRelIdByLogicalId(context, positionId, partId));
    }

    /**
     * 判断导入矩阵值是否发生变化
     **
     * @param context
     * @param positionId 位置产品ID
     * @param positionPartRelId 位置产品零件关系ID
     * @param vehicleConfigId 整车配置ID
     * @param quantity 数量
     * @param optional 是否选配
     * @return boolean 数量或是否选配发生变化时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private boolean isMatrixValueChanged(Context context, String positionId, String positionPartRelId, String vehicleConfigId, String quantity, String optional) throws Exception {
        String targetVpmId = resolvePositionPartVpmId(context, positionPartRelId);
        if (UIUtil.isNullOrEmpty(targetVpmId)) {
            return false;
        }
        String positionVehicleRelId = getPositionVehicleConfigRelId(context, positionId, vehicleConfigId);
        if (UIUtil.isNullOrEmpty(positionVehicleRelId)) {
            return false;
        }
        String matrixRelId = getMatrixRelId(context, positionVehicleRelId, targetVpmId);
        String newQuantity = normalizeSelectValue(quantity);
        String newOptional = normalizeSelectValue(optional);
        if (UIUtil.isNullOrEmpty(matrixRelId)) {
            return UIUtil.isNotNullAndNotEmpty(newQuantity) || UIUtil.isNotNullAndNotEmpty(newOptional);
        }
        String oldQuantity = normalizeSelectValue(getConnectionAttribute(context, matrixRelId, ATTR_NUMBER_OF_CHAIRS));
        String oldOptional = normalizeSelectValue(getConnectionAttribute(context, matrixRelId, ATTR_OPTIONAL_OR_NOT));
        return !safeEquals(oldQuantity, newQuantity) || !safeEquals(oldOptional, newOptional);
    }

    /**
     * 比较两个字符串是否相同
     **
     * @param first 第一个字符串
     * @param second 第二个字符串
     * @return boolean 两个字符串相同时返回true
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private boolean safeEquals(String first, String second) {
        return normalizeSelectValue(first).equals(normalizeSelectValue(second));
    }



    /**
     * 获取已有整车配置Map
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return Map 整车配置ID到数据映射
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private Map getExistingVehicleConfigMap(Context context, String productConfigId) throws Exception {
        Map result = new HashMap();
        MapList vehicleConfigs = getVehicleConfigList(context, productConfigId);
        for (Object configObj : vehicleConfigs) {
            Map configMap = (Map) configObj;
            result.put(configMap.get(SELECT_ID), configMap);
        }
        return result;
    }

    /**
     * 获取已有位置产品标题和ID索引
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return Map 包含按标题和按ID索引的位置产品数据
     * @throws Exception
     * @author caipan
     * @date 2026/8/3 16:13
     */
    private Map getExistingPositionMaps(Context context, String productConfigId) throws Exception {
        Map result = new HashMap();
        Map byTitle = new HashMap();
        Map byId = new HashMap();
        MapList positions = getPositionProductList(context, productConfigId, false);
        for (Object positionObj : positions) {
            Map positionMap = (Map) positionObj;
            byTitle.put(positionMap.get(SELECT_ATTR_TITLE), positionMap.get(SELECT_ID));
            byId.put(positionMap.get(SELECT_ID), positionMap);
        }
        result.put("byTitle", byTitle);
        result.put("byId", byId);
        return result;
    }

    /**
     * 获取是否选配导入值映射
     **
     * @param context
     * @return Map 是否选配值和翻译映射
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private Map getOptionalImportValueMap(Context context) throws Exception {
        Map result = new HashMap();
        result.put("", "");
        MapList options = getAttributeRangeOptions(context, ATTR_OPTIONAL_OR_NOT);
        for (Object optionObj : options) {
            Map option = (Map) optionObj;
            String value = normalizeSelectValue(option.get("value"));
            String label = normalizeSelectValue(option.get("label"));
            result.put(value, value);
            result.put(label, value);
        }
        return result;
    }

    /**
     * 规范化导入是否选配值
     **
     * @param optional 导入显示值
     * @param optionalValueMap 是否选配值映射
     * @return String 是否选配真实值
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private String normalizeOptionalImportValue(String optional, Map optionalValueMap) throws Exception {
        if (UIUtil.isNullOrEmpty(optional)) {
            return "";
        }
        return optionalValueMap.containsKey(optional) ? (String) optionalValueMap.get(optional) : null;
    }

    /**
     * 按零件号解析零件logicalid对象ID
     **
     * @param context
     * @param partNumber 零件号或对象名称
     * @return String 零件logicalid对象ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private String resolveVpmReferenceIdByPartNumber(Context context, String partNumber) throws Exception {
        if (UIUtil.isNullOrEmpty(partNumber)) {
            return "";
        }
        String safeValue = partNumber.replace("\\", "\\\\").replace("\"", "\\\"");
        String where = "type == \"" + TYPE_VPM_REFERENCE + "\" && attribute[EnterpriseExtension.V_PartNumber] == \"" + safeValue + "\"";
        String partId = queryFirstVpmReferenceId(context, where);
        if (UIUtil.isNullOrEmpty(partId)) {
            where = "type == \"" + TYPE_VPM_REFERENCE + "\" && name == \"" + safeValue + "\"";
            partId = queryFirstVpmReferenceId(context, where);
        }
        return UIUtil.isNullOrEmpty(partId) ? "" : getVpmReferenceLogicalObjectId(context, partId);
    }

    /**
     * 查询第一个物理产品ID
     **
     * @param context
     * @param where 查询条件
     * @return String 物理产品ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private String queryFirstVpmReferenceId(Context context, String where) throws Exception {
        String output = MqlUtil.mqlCommand(context,
                "temp query bus $1 $2 $3 where $4 select $5 dump $6",
                TYPE_VPM_REFERENCE,
                "*",
                "*",
                where,
                SELECT_ID,
                "|");
        if (UIUtil.isNullOrEmpty(output)) {
            return "";
        }
        String firstLine = output.split("\\r?\\n")[0];
        String[] values = firstLine.split("\\|");
        return values.length == 0 ? "" : values[values.length - 1];
    }

    /**
     * 按logicalid获取位置产品零件关系ID
     **
     * @param context
     * @param positionId 位置产品ID
     * @param partId 零件ID
     * @return String 位置产品零件关系ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private String getPositionPartRelIdByLogicalId(Context context, String positionId, String partId) throws Exception {
        String logicalId = getVpmReferenceLogicalId(context, partId);
        if (UIUtil.isNullOrEmpty(logicalId)) {
            return "";
        }
        DomainObject positionObj = DomainObject.newInstance(context, positionId);
        StringList relIds = positionObj.getInfoList(context, "from[" + REL_POSITION_TO_VPM + "].id");
        StringList logicalIds = positionObj.getInfoList(context, "from[" + REL_POSITION_TO_VPM + "].to." + SELECT_LOGICAL_ID);
        for (int i = 0; i < relIds.size() && i < logicalIds.size(); i++) {
            if (logicalId.equals(normalizeSelectValue(logicalIds.get(i)))) {
                return normalizeSelectValue(relIds.get(i));
            }
        }
        return "";
    }

    /**
     * 创建导入错误Excel
     **
     * @param workbook 原始Excel
     * @param errors 错误列表
     * @return Workbook 错误Excel
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private Workbook buildProductConfigErrorWorkbook(Workbook workbook, List errors) throws Exception {
        CellStyle errorStyle = workbook.createCellStyle();
        errorStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Map errorColMap = new HashMap();
        for (Object errorObj : errors) {
            Map error = (Map) errorObj;
            String sheetName = (String) error.get("sheet");
            int rowIndex = Integer.parseInt((String) error.get("row")) - 1;
            int colIndex = Integer.parseInt((String) error.get("col")) - 1;
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                continue;
            }
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }
            Cell cell = row.getCell(colIndex);
            if (cell == null) {
                cell = row.createCell(colIndex);
            }
            cell.setCellStyle(errorStyle);
            Integer errorCol = (Integer) errorColMap.get(sheetName);
            if (errorCol == null) {
                Row firstRow = sheet.getRow(0);
                errorCol = Integer.valueOf(firstRow == null ? 0 : Math.max(0, firstRow.getLastCellNum()) + 1);
                errorColMap.put(sheetName, errorCol);
                writeCell(sheet, 0, errorCol.intValue(), "Error Message", null);
            }
            String oldError = getCellText(row, errorCol.intValue());
            writeCell(row, errorCol.intValue(), (UIUtil.isNullOrEmpty(oldError) ? "" : oldError + "; ") + error.get("message"), null);
        }
        return workbook;
    }

    /**
     * 创建导入错误对象
     **
     * @param sheet Sheet名称
     * @param row 行号
     * @param col 列号
     * @param message 错误信息
     * @return Map 错误对象
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private Map createImportError(String sheet, int row, int col, String message) throws Exception {
        Map error = new HashMap();
        error.put("sheet", sheet);
        error.put("row", String.valueOf(row));
        error.put("col", String.valueOf(col));
        error.put("message", message);
        return error;
    }

    /**
     * 判断导入零件行是否为空行
     **
     * @param row Excel行
     * @param matrixColumns 矩阵列
     * @return boolean 空行返回true
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private boolean isBlankPartImportRow(Row row, List matrixColumns) throws Exception {
        for (int i = EXCEL_COL_POSITION_ID; i <= EXCEL_COL_ASSEMBLY_LEVEL; i++) {
            if (UIUtil.isNotNullAndNotEmpty(getCellText(row, i))) {
                return false;
            }
        }
        for (Object columnObj : matrixColumns) {
            Map columnMap = (Map) columnObj;
            if (UIUtil.isNotNullAndNotEmpty(getCellText(row, Integer.parseInt((String) columnMap.get("quantityCol"))))) {
                return false;
            }
            if (UIUtil.isNotNullAndNotEmpty(getCellText(row, Integer.parseInt((String) columnMap.get("optionalCol"))))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断导入矩阵单元格是否全部为空
     **
     * @param row Excel行
     * @param matrixColumns 矩阵列
     * @return boolean 矩阵单元格为空时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private boolean isMatrixImportRowBlank(Row row, List matrixColumns) throws Exception {
        for (Object columnObj : matrixColumns) {
            Map columnMap = (Map) columnObj;
            if (UIUtil.isNotNullAndNotEmpty(getCellText(row, Integer.parseInt((String) columnMap.get("quantityCol"))))) {
                return false;
            }
            if (UIUtil.isNotNullAndNotEmpty(getCellText(row, Integer.parseInt((String) columnMap.get("optionalCol"))))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取单元格文本
     **
     * @param row Excel行
     * @param colIndex 列索引
     * @return String 单元格文本
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private String getCellText(Row row, int colIndex) throws Exception {
        if (row == null) {
            return "";
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return "";
        }
        return new DataFormatter().formatCellValue(cell).trim();
    }

    /**
     * 写入Sheet单元格
     **
     * @param sheet Excel Sheet
     * @param rowIndex 行索引
     * @param colIndex 列索引
     * @param value 单元格值
     * @param style 单元格样式
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void writeCell(Sheet sheet, int rowIndex, int colIndex, String value, CellStyle style) throws Exception {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        writeCell(row, colIndex, value, style);
    }

    /**
     * 写入Row单元格
     **
     * @param row Excel行
     * @param colIndex 列索引
     * @param value 单元格值
     * @param style 单元格样式
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void writeCell(Row row, int colIndex, String value, CellStyle style) throws Exception {
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }
        cell.setCellValue(UIUtil.isNullOrEmpty(value) ? "" : value);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    /**
     * 创建Excel表头样式
     **
     * @param workbook Excel工作簿
     * @return CellStyle 表头样式
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private CellStyle createExcelHeaderStyle(XSSFWorkbook workbook) throws Exception {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        applyExcelThinBorder(style);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    /**
     * 创建Excel系统字段样式
     **
     * @param workbook Excel工作簿
     * @return CellStyle 系统字段样式
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private CellStyle createExcelSystemStyle(XSSFWorkbook workbook) throws Exception {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        applyExcelThinBorder(style);
        return style;
    }

    /**
     * 创建Excel可编辑单元格样式
     **
     * @param workbook Excel工作簿
     * @return CellStyle 可编辑单元格样式
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private CellStyle createExcelEditableStyle(XSSFWorkbook workbook) throws Exception {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        applyExcelThinBorder(style);
        return style;
    }

    /**
     * 创建Excel普通单元格样式
     **
     * @param workbook Excel工作簿
     * @return CellStyle 普通单元格样式
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private CellStyle createExcelNormalStyle(XSSFWorkbook workbook) throws Exception {
        CellStyle style = workbook.createCellStyle();
        applyExcelThinBorder(style);
        return style;
    }

    /**
     * 创建Excel分组起始单元格样式
     **
     * @param workbook Excel工作簿
     * @param baseStyle 基础样式
     * @return CellStyle 分组起始单元格样式
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private CellStyle createExcelGroupStartStyle(XSSFWorkbook workbook, CellStyle baseStyle) throws Exception {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(baseStyle);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        return style;
    }

    /**
     * 设置Excel单元格细边框
     **
     * @param style 单元格样式
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void applyExcelThinBorder(CellStyle style) throws Exception {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
    }

    /**
     * 自动调整Excel列宽
     **
     * @param sheet Excel Sheet
     * @param columnCount 列数
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void autosizeColumns(Sheet sheet, int columnCount) throws Exception {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) < 3500) {
                sheet.setColumnWidth(i, 3500);
            }
        }
    }

    /**
     * 添加Excel下拉值
     **
     * @param sheet Excel Sheet
     * @param colIndex 列索引
     * @param firstRow 起始行
     * @param lastRow 结束行
     * @param values 下拉值
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void addListValidation(Sheet sheet, int colIndex, int firstRow, int lastRow, String[] values) throws Exception {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(values);
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, colIndex, colIndex);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setEmptyCellAllowed(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    /**
     * 添加Excel整数校验
     **
     * @param sheet Excel Sheet
     * @param colIndex 列索引
     * @param firstRow 起始行
     * @param lastRow 结束行
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void addIntegerValidation(Sheet sheet, int colIndex, int firstRow, int lastRow) throws Exception {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createIntegerConstraint(DataValidationConstraint.OperatorType.BETWEEN, "0", "999999999");
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, colIndex, colIndex);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setEmptyCellAllowed(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    /**
     * 判断导入操作是否为移除
     **
     * @param operation 操作值
     * @return boolean Remove或兼容旧DELETE时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private boolean isRemoveOperation(String operation) throws Exception {
        return EXCEL_OPERATION_REMOVE.equalsIgnoreCase(operation) || EXCEL_OPERATION_DELETE.equalsIgnoreCase(operation);
    }

    /**
     * 更新单个产品配置矩阵单元格
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param positionId 位置产品ID
     * @param positionPartRelId 位置产品与零件关系ID
     * @param vehicleConfigId 整车配置ID
     * @param quantity 数量
     * @param optional 是否选配
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void updateMatrixCellValue(Context context, String productConfigId, String positionId, String positionPartRelId, String vehicleConfigId, String quantity, String optional) throws Exception {
        if (UIUtil.isNullOrEmpty(positionId) || UIUtil.isNullOrEmpty(positionPartRelId) || UIUtil.isNullOrEmpty(vehicleConfigId)) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigParts.LoadFailed"));
        }
        if (!isPositionPartRel(context, positionId, positionPartRelId)) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigParts.SelectPartsFirst"));
        }
        if (!isProductVehicleConfig(context, productConfigId, vehicleConfigId)) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigParts.SelectVehicleConfigFirst"));
        }
        if (UIUtil.isNotNullAndNotEmpty(quantity) && !quantity.matches("\\d+")) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigParts.QuantityNumberOnly"));
        }
        String targetVpmId = resolvePositionPartVpmId(context, positionPartRelId);
        if (UIUtil.isNullOrEmpty(targetVpmId)) {
            throw new Exception("Cannot find VPMReference for current part.");
        }
        String positionVehicleRelId = getPositionVehicleConfigRelId(context, positionId, vehicleConfigId);
        if (UIUtil.isNullOrEmpty(positionVehicleRelId)) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigParts.SelectVehicleConfigFirst"));
        }
        String matrixRelId = ensureMatrixRel(context, positionVehicleRelId, targetVpmId, "");
        if (UIUtil.isNullOrEmpty(matrixRelId)) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigParts.LoadFailed"));
        }
        MqlUtil.mqlCommand(context, "modify connection $1 $2 $3", matrixRelId, ATTR_NUMBER_OF_CHAIRS, UIUtil.isNullOrEmpty(quantity) ? "" : quantity);
        MqlUtil.mqlCommand(context, "modify connection $1 $2 $3", matrixRelId, ATTR_OPTIONAL_OR_NOT, UIUtil.isNullOrEmpty(optional) ? "" : optional);
    }

    /**
     * 批量更新单个产品配置矩阵单元格
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param positionId 位置产品ID
     * @param positionPartRelId 位置产品与零件关系ID
     * @param vehicleConfigId 整车配置ID
     * @param quantity 数量
     * @param optional 是否选配
     * @param productVehicleConfigCache 产品配置表与整车配置缓存
     * @param positionVehicleRelCache 位置产品与整车配置关系缓存
     * @param positionPartInfoCache 位置产品与零件关系缓存
     * @param matrixRelCache 矩阵关系缓存
     * @param matrixValueCache 矩阵值缓存
     * @param matrixLoadedPositionIds 已加载矩阵值的位置产品ID
     * @return boolean 单元格值发生变化时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private boolean updateMatrixCellValue(Context context, String productConfigId, String positionId, String positionPartRelId, String vehicleConfigId, String quantity, String optional,
                                          Map productVehicleConfigCache, Map positionVehicleRelCache, Map positionPartInfoCache, Map matrixRelCache, Map matrixValueCache, Set matrixLoadedPositionIds) throws Exception {
        if (UIUtil.isNullOrEmpty(positionId) || UIUtil.isNullOrEmpty(positionPartRelId) || UIUtil.isNullOrEmpty(vehicleConfigId)) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigParts.LoadFailed"));
        }
        if (UIUtil.isNotNullAndNotEmpty(quantity) && !quantity.matches("\\d+")) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigParts.QuantityNumberOnly"));
        }
        String productVehicleKey = productConfigId + "|" + vehicleConfigId;
        Boolean isProductVehicleConfig = (Boolean) productVehicleConfigCache.get(productVehicleKey);
        if (isProductVehicleConfig == null) {
            isProductVehicleConfig = Boolean.valueOf(isProductVehicleConfig(context, productConfigId, vehicleConfigId));
            productVehicleConfigCache.put(productVehicleKey, isProductVehicleConfig);
        }
        if (!isProductVehicleConfig.booleanValue()) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigParts.SelectVehicleConfigFirst"));
        }
        Map positionPartInfo = (Map) positionPartInfoCache.get(positionPartRelId);
        if (positionPartInfo == null) {
            positionPartInfo = getPositionPartRelInfo(context, positionPartRelId);
            positionPartInfoCache.put(positionPartRelId, positionPartInfo);
        }
        if (!positionId.equals(positionPartInfo.get("fromId")) || !REL_POSITION_TO_VPM.equals(positionPartInfo.get("type"))) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigParts.SelectPartsFirst"));
        }
        String targetVpmId = (String) positionPartInfo.get("toId");
        if (UIUtil.isNullOrEmpty(targetVpmId)) {
            throw new Exception("Cannot find VPMReference for current part.");
        }
        loadImportPositionMatrixCache(context, positionId, positionVehicleRelCache, matrixRelCache, matrixValueCache, matrixLoadedPositionIds);
        String positionVehicleKey = positionId + "|" + vehicleConfigId;
        String positionVehicleRelId = (String) positionVehicleRelCache.get(positionVehicleKey);
        if (positionVehicleRelId == null) {
            positionVehicleRelId = getPositionVehicleConfigRelId(context, positionId, vehicleConfigId);
            positionVehicleRelCache.put(positionVehicleKey, positionVehicleRelId);
        }
        if (UIUtil.isNullOrEmpty(positionVehicleRelId)) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigParts.SelectVehicleConfigFirst"));
        }
        String matrixRelKey = positionVehicleRelId + "|" + targetVpmId;
        String newQuantity = UIUtil.isNullOrEmpty(quantity) ? "" : quantity;
        String newOptional = UIUtil.isNullOrEmpty(optional) ? "" : optional;
        String matrixRelId = (String) matrixRelCache.get(matrixRelKey);
        Map matrixValueMap = (Map) matrixValueCache.get(matrixRelKey);
        if (matrixRelId == null) {
            if (UIUtil.isNullOrEmpty(newQuantity) && UIUtil.isNullOrEmpty(newOptional)) {
                return false;
            }
            matrixRelId = ensureMatrixRel(context, positionVehicleRelId, targetVpmId, "");
            matrixRelCache.put(matrixRelKey, matrixRelId);
            matrixValueMap = new HashMap();
            matrixValueMap.put("quantity", "");
            matrixValueMap.put("optional", "");
            matrixValueMap.put("relId", matrixRelId);
            matrixValueCache.put(matrixRelKey, matrixValueMap);
        }
        if (UIUtil.isNullOrEmpty(matrixRelId)) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigParts.LoadFailed"));
        }
        String oldQuantity = matrixValueMap == null ? "" : normalizeSelectValue(matrixValueMap.get("quantity"));
        String oldOptional = matrixValueMap == null ? "" : normalizeSelectValue(matrixValueMap.get("optional"));
        if (safeEquals(oldQuantity, newQuantity) && safeEquals(oldOptional, newOptional)) {
            return false;
        }
        MqlUtil.mqlCommand(context, "modify connection $1 $2 $3", matrixRelId, ATTR_NUMBER_OF_CHAIRS, newQuantity);
        MqlUtil.mqlCommand(context, "modify connection $1 $2 $3", matrixRelId, ATTR_OPTIONAL_OR_NOT, newOptional);
        if (matrixValueMap != null) {
            matrixValueMap.put("quantity", newQuantity);
            matrixValueMap.put("optional", newOptional);
        }
        return true;
    }

    /**
     * 加载导入位置产品矩阵缓存
     **
     * @param context
     * @param positionId 位置产品ID
     * @param positionVehicleRelCache 位置产品整车配置关系缓存
     * @param matrixRelCache 矩阵关系缓存
     * @param matrixValueCache 矩阵值缓存
     * @param matrixLoadedPositionIds 已加载矩阵值的位置产品ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void loadImportPositionMatrixCache(Context context, String positionId, Map positionVehicleRelCache, Map matrixRelCache, Map matrixValueCache, Set matrixLoadedPositionIds) throws Exception {
        if (UIUtil.isNullOrEmpty(positionId) || matrixLoadedPositionIds.contains(positionId)) {
            return;
        }
        Map positionVehicleRelMap = getPositionVehicleConfigRelMap(context, positionId);
        for (Object entryObj : positionVehicleRelMap.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            String vehicleConfigId = (String) entry.getKey();
            String positionVehicleRelId = (String) entry.getValue();
            positionVehicleRelCache.put(positionId + "|" + vehicleConfigId, positionVehicleRelId);
        }
        Map positionMatrixValueMap = getPositionMatrixValueMap(context, positionVehicleRelMap);
        for (Object entryObj : positionMatrixValueMap.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            String[] keyItems = String.valueOf(entry.getKey()).split("\\|", -1);
            if (keyItems.length < 2) {
                continue;
            }
            String vehicleConfigId = keyItems[0];
            String partId = keyItems[1];
            String positionVehicleRelId = (String) positionVehicleRelMap.get(vehicleConfigId);
            if (UIUtil.isNullOrEmpty(positionVehicleRelId) || UIUtil.isNullOrEmpty(partId)) {
                continue;
            }
            Map matrixMap = (Map) entry.getValue();
            String matrixRelKey = positionVehicleRelId + "|" + partId;
            matrixRelCache.put(matrixRelKey, matrixMap.get("relId"));
            matrixValueCache.put(matrixRelKey, matrixMap);
        }
        matrixLoadedPositionIds.add(positionId);
    }

    /**
     * 获取目标销量年度列表
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return MapList 目标销量年度列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    private MapList getTargetSalesList(Context context, String productConfigId) throws Exception {
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            return new MapList();
        }
        StringList targetSelects = JF_Util_mxJPO.basicBolistSel();
        targetSelects.add(SELECT_ATTR_TITLE);
        targetSelects.add(SELECT_ATTR_OVERSEAS);
        targetSelects.add(SELECT_ATTR_DOMESTIC);
        targetSelects.add(SELECT_ATTR_OTHER);
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        MapList targetSales = productConfigObj.getRelatedObjects(context,
                REL_PRODUCT_CONFIG_TO_TARGET_SALES,
                TYPE_TARGET_SALES,
                targetSelects,
                JF_Util_mxJPO.basicRellistSel(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
        for (Object targetObj : targetSales) {
            Map targetMap = (Map) targetObj;
            targetMap.put("year", normalizeSelectValue(targetMap.get(SELECT_ATTR_TITLE)));
            targetMap.put("overseas", normalizeSelectValue(targetMap.get(SELECT_ATTR_OVERSEAS)));
            targetMap.put("domestic", normalizeSelectValue(targetMap.get(SELECT_ATTR_DOMESTIC)));
            targetMap.put("other", normalizeSelectValue(targetMap.get(SELECT_ATTR_OTHER)));
        }
        targetSales.sort("year", "ascending", "integer");
        return targetSales;
    }

    /**
     * 获取目标销量整车配置比例列表
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return MapList 整车配置比例列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    private MapList getTargetSalesVehicleConfigList(Context context, String productConfigId) throws Exception {
        MapList vehicleConfigs = getVehicleConfigList(context, productConfigId);
        for (Object configObj : vehicleConfigs) {
            Map configMap = (Map) configObj;
            configMap.put("overseas", normalizeSelectValue(configMap.get(SELECT_ATTR_OVERSEAS)));
            configMap.put("domestic", normalizeSelectValue(configMap.get(SELECT_ATTR_DOMESTIC)));
            configMap.put("other", normalizeSelectValue(configMap.get(SELECT_ATTR_OTHER)));
        }
        return vehicleConfigs;
    }

    /**
     * 获取目标销量汇总零件列表，按最新版零件及产品配置所属项目读取客户零件号/DB关系
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param targetSales 已查询的目标销量年度列表
     * @return MapList 汇总零件列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/23 16:13
     */
    private MapList getTargetSalesPartList(Context context, String productConfigId, MapList targetSales) throws Exception {
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            return new MapList();
        }
        StringList partSelects = JF_Util_mxJPO.basicBolistSel();
        partSelects.add(SELECT_ATTR_PART_NUMBER);
        partSelects.add(SELECT_ATTR_PART_CN);
        partSelects.add(SELECT_ATTR_PART_LEVEL);
        partSelects.add(SELECT_LOGICAL_ID);
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        MapList partList = productConfigObj.getRelatedObjects(context,
                REL_PRODUCT_CONFIG_TO_VPM,
                TYPE_VPM_REFERENCE,
                partSelects,
                JF_Util_mxJPO.basicRellistSel(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
        Map annualQuantityMap = getTargetSalesPartAnnualQuantityMap(context, targetSales);
        Map optionalInfoMap = getTargetSalesPartOptionalInfoMap(context, productConfigId);
        BigDecimal targetSalesTotal = getTargetSalesTotal(targetSales);
        boolean ratioReady = !"FALSE".equalsIgnoreCase(normalizeSelectValue(productConfigObj.getInfo(context, SELECT_ATTR_SUMMARY_RATIO_READY)));
        String projectId = getProjectIdByProductConfig(context, productConfigId);
        fillCustomerPartInfoValues(context, partList, projectId);
        MapList resultList = new MapList();
        for (Object partObj : partList) {
            Map partMap = (Map) partObj;
            String partId = (String) partMap.get(SELECT_ID);
            String partType = normalizeSelectValue(partMap.get(SELECT_ATTR_PART_LEVEL));
            if ("X".equals(partType)) {
                continue;
            }
            Map quantities = new HashMap();
            for (Object targetObj : targetSales) {
                Map targetMap = (Map) targetObj;
                String targetSalesId = (String) targetMap.get(SELECT_ID);
                quantities.put(targetSalesId, normalizeSelectValue(annualQuantityMap.get(targetSalesId + "|" + partId)));
            }
            partMap.put("partNumber", normalizeSelectValue(partMap.get(SELECT_ATTR_PART_NUMBER)));
            partMap.put("partNameCN", normalizeSelectValue(partMap.get(SELECT_ATTR_PART_CN)));
            partMap.put("customerPartNumber", normalizeSelectValue(partMap.get(ATTR_CUSTOMER_PART_NUMBER)));
            partMap.put("customerPartName", normalizeSelectValue(partMap.get(ATTR_CUSTOMER_PART_NAME)));
            partMap.put("partLevel", partType);
            partMap.put("partInfo", getTargetSalesPartOptionalInfo(optionalInfoMap, partId, normalizeSelectValue(partMap.get(SELECT_LOGICAL_ID))));
            partMap.put("annualQuantities", quantities);
            partMap.put("ratio", ratioReady ? calculateTargetSalesPartRatio(quantities, targetSalesTotal) : "");
            resultList.add(partMap);
        }
        return resultList;
    }

    /**
     * 获取目标销量汇总计算用零件配置数量
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return Map 零件Key和整车配置对应数量
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private Map getTargetSalesPartVehicleQuantityMap(Context context, String productConfigId) throws Exception {
        Map result = new HashMap();
        MapList positions = getPositionProductList(context, productConfigId, false);
        for (Object positionObj : positions) {
            Map positionMap = (Map) positionObj;
            String positionId = normalizeSelectValue(positionMap.get(SELECT_ID));
            if (UIUtil.isNullOrEmpty(positionId)) {
                continue;
            }
            StringList partIds = getPositionPartIds(context, positionId);
            Map positionVehicleRelMap = getPositionVehicleConfigRelMap(context, positionId);
            Map matrixValueMap = getPositionMatrixValueMap(context, positionVehicleRelMap);
            for (Object partIdObj : partIds) {
                String partId = normalizeSelectValue(partIdObj);
                String partKey = getTargetSalesPartQuantityKey(context, partId);
                if (UIUtil.isNullOrEmpty(partKey)) {
                    continue;
                }
                for (Object vehicleObj : positionVehicleRelMap.keySet()) {
                    String vehicleConfigId = normalizeSelectValue(vehicleObj);
                    Map matrixMap = (Map) matrixValueMap.get(vehicleConfigId + "|" + partId);
                    if (matrixMap == null) {
                        continue;
                    }
                    BigDecimal quantity = parseDecimal(matrixMap.get("quantity"));
                    if (BigDecimal.ZERO.compareTo(quantity) == 0) {
                        continue;
                    }
                    String key = partKey + "|" + vehicleConfigId;
                    BigDecimal oldQuantity = (BigDecimal) result.get(key);
                    result.put(key, (oldQuantity == null ? BigDecimal.ZERO : oldQuantity).add(quantity));
                }
            }
        }
        return result;
    }

    /**
     * 计算单个零件单个年度数量
     **
     * @param targetMap 目标销量年度
     * @param partKey 零件Key
     * @param partVehicleQuantityMap 零件配置数量Map
     * @param vehicleRatioMap 整车配置比例Map
     * @return BigDecimal 年度数量
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private BigDecimal calculateTargetSalesPartAnnualQuantity(Map targetMap, String partKey, Map partVehicleQuantityMap, Map vehicleRatioMap) throws Exception {
        BigDecimal overseasSales = parseDecimal(targetMap.get("overseas"));
        BigDecimal domesticSales = parseDecimal(targetMap.get("domestic"));
        BigDecimal otherSales = parseDecimal(targetMap.get("other"));
        BigDecimal result = BigDecimal.ZERO;
        for (Object keyObj : partVehicleQuantityMap.keySet()) {
            String key = normalizeSelectValue(keyObj);
            if (!key.startsWith(partKey + "|")) {
                continue;
            }
            String vehicleConfigId = key.substring((partKey + "|").length());
            Map ratioMap = (Map) vehicleRatioMap.get(vehicleConfigId);
            if (ratioMap == null) {
                continue;
            }
            BigDecimal partQuantity = (BigDecimal) partVehicleQuantityMap.get(key);
            result = result.add(overseasSales.multiply((BigDecimal) ratioMap.get("overseas")).multiply(partQuantity));
            result = result.add(domesticSales.multiply((BigDecimal) ratioMap.get("domestic")).multiply(partQuantity));
            result = result.add(otherSales.multiply((BigDecimal) ratioMap.get("other")).multiply(partQuantity));
        }
        return result;
    }

    /**
     * 获取目标销量整车配置比例Map
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return Map 整车配置比例Map
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private Map getTargetSalesVehicleRatioMap(Context context, String productConfigId) throws Exception {
        Map result = new HashMap();
        MapList vehicleConfigs = getTargetSalesVehicleConfigList(context, productConfigId);
        for (Object configObj : vehicleConfigs) {
            Map configMap = (Map) configObj;
            Map ratioMap = new HashMap();
            ratioMap.put("overseas", parsePercent(configMap.get("overseas")));
            ratioMap.put("domestic", parsePercent(configMap.get("domestic")));
            ratioMap.put("other", parsePercent(configMap.get("other")));
            result.put(normalizeSelectValue(configMap.get(SELECT_ID)), ratioMap);
        }
        return result;
    }

    /**
     * 获取汇总零件选配信息
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return Map 零件ID或logicalid对应选配信息
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    private Map getTargetSalesPartOptionalInfoMap(Context context, String productConfigId) throws Exception {
        Map result = new HashMap();
        MapList positions = DomainObject.newInstance(context, productConfigId).getRelatedObjects(context,
                REL_PRODUCT_CONFIG_TO_POSITION,
                TYPE_POSITION_PRODUCT,
                new StringList(SELECT_ID),
                new StringList(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
        StringList partSelects = new StringList(SELECT_ID);
        partSelects.add(SELECT_LOGICAL_ID);
        for (Object positionObj : positions) {
            Map positionMap = (Map) positionObj;
            String positionId = normalizeSelectValue(positionMap.get(SELECT_ID));
            if (UIUtil.isNullOrEmpty(positionId)) {
                continue;
            }
            MapList parts = DomainObject.newInstance(context, positionId).getRelatedObjects(context,
                    REL_POSITION_TO_VPM,
                    TYPE_VPM_REFERENCE,
                    partSelects,
                    new StringList(),
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    (short) 0);
            Map positionVehicleRelMap = getPositionVehicleConfigRelMap(context, positionId);
            Map matrixValueMap = getPositionMatrixValueMap(context, positionVehicleRelMap);
            for (Object partObj : parts) {
                Map partMap = (Map) partObj;
                String partId = normalizeSelectValue(partMap.get(SELECT_ID));
                if (UIUtil.isNullOrEmpty(partId)) {
                    continue;
                }
                String logicalId = normalizeSelectValue(partMap.get(SELECT_LOGICAL_ID));
                for (Object vehicleObj : positionVehicleRelMap.keySet()) {
                    String vehicleConfigId = normalizeSelectValue(vehicleObj);
                    Map matrixMap = (Map) matrixValueMap.get(vehicleConfigId + "|" + partId);
                    if (matrixMap == null || UIUtil.isNullOrEmpty(normalizeSelectValue(matrixMap.get("quantity")))) {
                        continue;
                    }
                    String optional = normalizeSelectValue(matrixMap.get("optional"));
                    updateTargetSalesPartOptionalState(result, partId, optional);
                    if (UIUtil.isNotNullAndNotEmpty(logicalId)) {
                        updateTargetSalesPartOptionalState(result, logicalId, optional);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 更新汇总零件选配状态
     **
     * @param optionalInfoMap 选配信息Map
     * @param partKey 零件ID或logicalid
     * @param optional 是否选配
     * @author caipan
     * @date 2026/7/2 16:13
     */
    private void updateTargetSalesPartOptionalState(Map optionalInfoMap, String partKey, String optional) {
        Map stateMap = (Map) optionalInfoMap.get(partKey);
        if (stateMap == null) {
            stateMap = new HashMap();
            optionalInfoMap.put(partKey, stateMap);
        }
        if (UIUtil.isNullOrEmpty(optional)) {
            stateMap.put("standard", Boolean.TRUE);
        } else if ("O".equals(optional)) {
            stateMap.put("optional", Boolean.TRUE);
        }
    }

    /**
     * 格式化汇总零件选配信息
     **
     * @param optionalInfoMap 选配信息Map
     * @param partId 零件ID
     * @param logicalId 零件logicalid
     * @return String 标配、选配或标配&选配
     * @author caipan
     * @date 2026/7/2 16:13
     */
    private String getTargetSalesPartOptionalInfo(Map optionalInfoMap, String partId, String logicalId) {
        Map stateMap = (Map) optionalInfoMap.get(partId);
        if (stateMap == null && UIUtil.isNotNullAndNotEmpty(logicalId)) {
            stateMap = (Map) optionalInfoMap.get(logicalId);
        }
        if (stateMap == null) {
            return "";
        }
        boolean standard = Boolean.TRUE.equals(stateMap.get("standard"));
        boolean optional = Boolean.TRUE.equals(stateMap.get("optional"));
        if (standard && optional) {
            return "标配&选配";
        }
        if (standard) {
            return "标配";
        }
        return optional ? "选配" : "";
    }

    /**
     * 批量获取目标销量零件年度数量
     **
     * @param context
     * @param targetSales 目标销量年度列表
     * @return Map 年度数量Map
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    private Map getTargetSalesPartAnnualQuantityMap(Context context, MapList targetSales) throws Exception {
        Map result = new HashMap();
        StringList partSelects = new StringList(SELECT_ID);
        StringList relSelects = new StringList(SELECT_REL_ATTR_ANNUAL_QUANTITY);
        for (Object targetObj : targetSales) {
            Map targetMap = (Map) targetObj;
            String targetSalesId = (String) targetMap.get(SELECT_ID);
            DomainObject targetSalesObj = DomainObject.newInstance(context, targetSalesId);
            MapList parts = targetSalesObj.getRelatedObjects(context,
                    REL_TARGET_SALES_TO_VPM,
                    TYPE_VPM_REFERENCE,
                    partSelects,
                    relSelects,
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    (short) 0);
            for (Object partObj : parts) {
                Map partMap = (Map) partObj;
                String partId = normalizeSelectValue(partMap.get(SELECT_ID));
                if (UIUtil.isNullOrEmpty(partId)) {
                    continue;
                }
                result.put(targetSalesId + "|" + partId, normalizeSelectValue(partMap.get(SELECT_REL_ATTR_ANNUAL_QUANTITY)));
            }
        }
        return result;
    }

    /**
     * 获取目标销量年度映射
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return Map 年度到目标销量ID映射
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private Map getTargetSalesYearMap(Context context, String productConfigId) throws Exception {
        Map result = new HashMap();
        MapList targetSales = getTargetSalesList(context, productConfigId);
        for (Object targetObj : targetSales) {
            Map targetMap = (Map) targetObj;
            result.put(normalizeSelectValue(targetMap.get("year")), normalizeSelectValue(targetMap.get(SELECT_ID)));
        }
        return result;
    }

    /**
     * 写入目标销量零件年度数量
     **
     * @param context
     * @param targetSalesId 目标销量ID
     * @param partId 零件ID
     * @param annualQuantity 年度数量
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private void setTargetSalesPartAnnualQuantity(Context context, String targetSalesId, String partId, String annualQuantity) throws Exception {
        String relId = getTargetSalesPartRelId(context, targetSalesId, partId);
        if (UIUtil.isNullOrEmpty(relId)) {
            DomainRelationship.connect(context,
                    DomainObject.newInstance(context, targetSalesId),
                    REL_TARGET_SALES_TO_VPM,
                    DomainObject.newInstance(context, partId));
            relId = getTargetSalesPartRelId(context, targetSalesId, partId);
        }
        MqlUtil.mqlCommand(context, "modify connection $1 $2 $3", relId, ATTR_ANNUAL_QUANTITY, annualQuantity);
    }

    /**
     * 获取汇总计算零件Key
     **
     * @param context
     * @param partId 零件ID
     * @return String 零件logicalid或ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private String getTargetSalesPartQuantityKey(Context context, String partId) throws Exception {
        String logicalId = getVpmReferenceLogicalId(context, partId);
        return UIUtil.isNullOrEmpty(logicalId) ? partId : logicalId;
    }

    /**
     * 获取汇总计算零件Key
     **
     * @param partMap 零件Map
     * @return String 零件logicalid或ID
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private String getTargetSalesPartQuantityKey(Map partMap) {
        String logicalId = normalizeSelectValue(partMap.get(SELECT_LOGICAL_ID));
        return UIUtil.isNullOrEmpty(logicalId) ? normalizeSelectValue(partMap.get(SELECT_ID)) : logicalId;
    }

    /**
     * 计算项目目标销量合计
     **
     * @param targetSales 目标销量列表
     * @return BigDecimal 目标销量合计
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private BigDecimal getTargetSalesTotal(MapList targetSales) throws Exception {
        BigDecimal result = BigDecimal.ZERO;
        for (Object targetObj : targetSales) {
            Map targetMap = (Map) targetObj;
            result = result.add(parseDecimal(targetMap.get("overseas")));
            result = result.add(parseDecimal(targetMap.get("domestic")));
            result = result.add(parseDecimal(targetMap.get("other")));
        }
        return result;
    }

    /**
     * 计算汇总零件生命周期比例
     **
     * @param quantities 年度数量Map
     * @param targetSalesTotal 项目目标销量合计
     * @return String 比例
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private String calculateTargetSalesPartRatio(Map quantities, BigDecimal targetSalesTotal) throws Exception {
        if (targetSalesTotal == null || BigDecimal.ZERO.compareTo(targetSalesTotal) == 0) {
            return "";
        }
        BigDecimal partTotal = BigDecimal.ZERO;
        for (Object quantityObj : quantities.values()) {
            partTotal = partTotal.add(parseDecimal(quantityObj));
        }
        if (BigDecimal.ZERO.compareTo(partTotal) == 0) {
            return "";
        }
        return partTotal.divide(targetSalesTotal, 3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    /**
     * 解析数字
     **
     * @param value 原始值
     * @return BigDecimal 数字值
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private BigDecimal parseDecimal(Object value) {
        String text = normalizeSelectValue(value);
        if (UIUtil.isNullOrEmpty(text)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(text);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 解析百分比
     **
     * @param value 原始百分比
     * @return BigDecimal 小数比例
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private BigDecimal parsePercent(Object value) {
        return parseDecimal(value).divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP);
    }

    /**
     * 判断目标销量年度是否已存在
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param year 年度
     * @param excludeId 排除的目标销量ID
     * @return boolean 存在时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    private boolean isTargetSalesYearExists(Context context, String productConfigId, String year, String excludeId) throws Exception {
        MapList targetSales = getTargetSalesList(context, productConfigId);
        for (Object targetObj : targetSales) {
            Map targetMap = (Map) targetObj;
            String targetSalesId = (String) targetMap.get(SELECT_ID);
            if (UIUtil.isNotNullAndNotEmpty(excludeId) && excludeId.equals(targetSalesId)) {
                continue;
            }
            if (year.equals(normalizeSelectValue(targetMap.get("year")))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断目标销量是否属于产品配置表
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param targetSalesId 目标销量ID
     * @return boolean 属于时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    private boolean isProductTargetSales(Context context, String productConfigId, String targetSalesId) throws Exception {
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        StringList relIds = productConfigObj.getInfoList(context,
                "from[" + REL_PRODUCT_CONFIG_TO_TARGET_SALES + "|to.id==" + targetSalesId + "].id");
        return !relIds.isEmpty();
    }

    /**
     * 获取目标销量与零件关系ID
     **
     * @param context
     * @param targetSalesId 目标销量ID
     * @param partId 零件ID
     * @return String 关系ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    private String getTargetSalesPartRelId(Context context, String targetSalesId, String partId) throws Exception {
        DomainObject targetSalesObj = DomainObject.newInstance(context, targetSalesId);
        StringList relIds = targetSalesObj.getInfoList(context,
                "from[" + REL_TARGET_SALES_TO_VPM + "|to.id==" + partId + "].id");
        return relIds.isEmpty() ? "" : normalizeSelectValue(relIds.get(0));
    }

    /**
     * 删除目标销量相关连接
     **
     * @param context
     * @param targetSalesId 目标销量ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private void deleteTargetSalesConnections(Context context, String targetSalesId) throws Exception {
        DomainObject targetSalesObj = DomainObject.newInstance(context, targetSalesId);
        StringList relIds = targetSalesObj.getInfoList(context, "from[" + REL_TARGET_SALES_TO_VPM + "].id");
        relIds.addAll(targetSalesObj.getInfoList(context, "to[" + REL_PRODUCT_CONFIG_TO_TARGET_SALES + "].id"));
        for (Object relIdObj : relIds) {
            String relId = normalizeSelectValue(relIdObj);
            if (UIUtil.isNotNullAndNotEmpty(relId)) {
                MqlUtil.mqlCommand(context, "delete connection $1", relId);
            }
        }
    }

    private MapList getVehicleConfigList(Context context, String productConfigId) throws Exception {
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            return new MapList();
        }
        StringList vehicleSelects = JF_Util_mxJPO.basicBolistSel();
        vehicleSelects.add(SELECT_ATTR_TITLE);
        vehicleSelects.add(SELECT_ATTR_VC_INFO);
        vehicleSelects.add(SELECT_ATTR_ORDER);
        vehicleSelects.add(SELECT_ATTR_OVERSEAS);
        vehicleSelects.add(SELECT_ATTR_DOMESTIC);
        vehicleSelects.add(SELECT_ATTR_OTHER);
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        MapList vehicleConfigs = productConfigObj.getRelatedObjects(context,
                REL_PRODUCT_CONFIG_TO_VEHICLE_CONFIG,
                TYPE_VEHICLE_CONFIGURATION,
                vehicleSelects,
                JF_Util_mxJPO.basicRellistSel(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
        for (Object configObj : vehicleConfigs) {
            Map configMap = (Map) configObj;
            configMap.put("title", configMap.get(SELECT_ATTR_TITLE));
            configMap.put("vcInfo", configMap.get(SELECT_ATTR_VC_INFO));
            configMap.put("order", configMap.get(SELECT_ATTR_ORDER));
        }
        sortVehicleConfigListByOrder(vehicleConfigs);
        return vehicleConfigs;
    }

    private String getNextVehicleConfigTitle(Context context, MapList vehicleConfigs) {
        String prefix = getFrameworkString(context, "emxFramework.JFProductConfigParts.VehicleConfigTitlePrefix", "Vehicle Config");
        int maxIndex = 0;
        for (Object configObj : vehicleConfigs) {
            Map configMap = (Map) configObj;
            String title = (String) configMap.get(SELECT_ATTR_TITLE);
            if (UIUtil.isNullOrEmpty(title) || !title.startsWith(prefix)) {
                continue;
            }
            String suffix = title.substring(prefix.length()).trim();
            try {
                maxIndex = Math.max(maxIndex, Integer.parseInt(suffix));
            } catch (NumberFormatException e) {
                LOGGER.debug("Ignore vehicle config title without numeric suffix: {}", title);
            }
        }
        return prefix + (maxIndex + 1);
    }

    private boolean isVehicleConfigTitleExists(Context context, String productConfigId, String title, String excludeId) throws Exception {
        MapList vehicleConfigs = getVehicleConfigList(context, productConfigId);
        for (Object configObj : vehicleConfigs) {
            Map configMap = (Map) configObj;
            String configId = (String) configMap.get(SELECT_ID);
            if (UIUtil.isNotNullAndNotEmpty(excludeId) && excludeId.equals(configId)) {
                continue;
            }
            if (title.equals((String) configMap.get(SELECT_ATTR_TITLE))) {
                return true;
            }
        }
        return false;
    }

    private boolean isProductVehicleConfig(Context context, String productConfigId, String vehicleConfigId) throws Exception {
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        StringList relIds = productConfigObj.getInfoList(context,
                "from[" + REL_PRODUCT_CONFIG_TO_VEHICLE_CONFIG + "|to.id==" + vehicleConfigId + "].id");
        return !relIds.isEmpty();
    }

    private boolean isProductPosition(Context context, String productConfigId, String positionId) throws Exception {
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        StringList relIds = productConfigObj.getInfoList(context,
                "from[" + REL_PRODUCT_CONFIG_TO_POSITION + "|to.id==" + positionId + "].id");
        return !relIds.isEmpty();
    }

    /**
     * 判断零件是否属于产品配置表
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param partId 零件ID
     * @return boolean 属于时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    private boolean isProductConfigPart(Context context, String productConfigId, String partId) throws Exception {
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        StringList relIds = productConfigObj.getInfoList(context,
                "from[" + REL_PRODUCT_CONFIG_TO_VPM + "|to.id==" + partId + "].id");
        if (!relIds.isEmpty()) {
            return true;
        }
        return hasProductConfigPartByLogicalId(context, productConfigId, getVpmReferenceLogicalId(context, partId));
    }

    private BigDecimal getNextVehicleConfigOrder(MapList vehicleConfigs) {
        BigDecimal maxOrder = BigDecimal.ZERO;
        for (Object configObj : vehicleConfigs) {
            Map configMap = (Map) configObj;
            BigDecimal order = parseOrder(String.valueOf(configMap.get(SELECT_ATTR_ORDER)));
            if (order.compareTo(maxOrder) > 0) {
                maxOrder = order;
            }
        }
        return maxOrder.add(new BigDecimal("1000"));
    }

    private BigDecimal calculateNewVehicleConfigOrder(Context context, String productConfigId, String previousVehicleConfigId, String nextVehicleConfigId) throws Exception {
        boolean hasPrevious = UIUtil.isNotNullAndNotEmpty(previousVehicleConfigId) && isProductVehicleConfig(context, productConfigId, previousVehicleConfigId);
        boolean hasNext = UIUtil.isNotNullAndNotEmpty(nextVehicleConfigId) && isProductVehicleConfig(context, productConfigId, nextVehicleConfigId);
        if (!hasPrevious && hasNext) {
            return getVehicleConfigOrder(context, nextVehicleConfigId).subtract(new BigDecimal("1000"));
        }
        if (hasPrevious && hasNext) {
            BigDecimal previousOrder = getVehicleConfigOrder(context, previousVehicleConfigId);
            BigDecimal nextOrder = getVehicleConfigOrder(context, nextVehicleConfigId);
            return previousOrder.add(nextOrder).divide(new BigDecimal("2"));
        }
        if (hasPrevious) {
            return getVehicleConfigOrder(context, previousVehicleConfigId).add(new BigDecimal("1000"));
        }
        return getNextVehicleConfigOrder(getVehicleConfigList(context, productConfigId));
    }

    private BigDecimal getVehicleConfigOrder(Context context, String vehicleConfigId) throws Exception {
        return parseOrder(DomainObject.newInstance(context, vehicleConfigId).getInfo(context, SELECT_ATTR_ORDER));
    }

    private void sortVehicleConfigListByOrder(MapList vehicleConfigs) {
        Collections.sort(vehicleConfigs, new Comparator() {
            public int compare(Object leftObj, Object rightObj) {
                Map left = (Map) leftObj;
                Map right = (Map) rightObj;
                return parseOrder(String.valueOf(left.get(SELECT_ATTR_ORDER))).compareTo(parseOrder(String.valueOf(right.get(SELECT_ATTR_ORDER))));
            }
        });
    }

    /**
     * 整车配置关联所有位置产品
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param vehicleConfigId 新建整车配置ID
     * @param copyFromId 复制来源整车配置ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void connectVehicleConfigToPositions(Context context, String productConfigId, String vehicleConfigId, String copyFromId) throws Exception {
        MapList positions = getPositionProductList(context, productConfigId, false);
        for (Object positionObj : positions) {
            Map positionMap = (Map) positionObj;
            String positionId = (String) positionMap.get(SELECT_ID);
            String newPositionVehicleRelId = ensurePositionVehicleConfigRel(context, positionId, vehicleConfigId);
            syncPositionPartsToVehicleConfig(context, positionId, newPositionVehicleRelId, copyFromId);
        }
    }

    private void connectExistingVehicleConfigsToPosition(Context context, String productConfigId, String positionId) throws Exception {
        MapList vehicleConfigs = getVehicleConfigList(context, productConfigId);
        for (Object configObj : vehicleConfigs) {
            Map configMap = (Map) configObj;
            ensurePositionVehicleConfigRel(context, positionId, (String) configMap.get(SELECT_ID));
        }
    }

    private String ensurePositionVehicleConfigRel(Context context, String positionId, String vehicleConfigId) throws Exception {
        String relId = getPositionVehicleConfigRelId(context, positionId, vehicleConfigId);
        if (UIUtil.isNotNullAndNotEmpty(relId)) {
            return relId;
        }
        DomainRelationship.connect(context,
                DomainObject.newInstance(context, positionId),
                REL_POSITION_TO_VEHICLE_CONFIG,
                DomainObject.newInstance(context, vehicleConfigId));
        return getPositionVehicleConfigRelId(context, positionId, vehicleConfigId);
    }

    /**
     * 同步位置产品已有零件到新整车配置矩阵关系
     **
     * @param context
     * @param positionId 位置产品ID
     * @param newPositionVehicleRelId 新建整车配置与位置产品关系ID
     * @param copyFromVehicleConfigId 复制来源整车配置ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void syncPositionPartsToVehicleConfig(Context context, String positionId, String newPositionVehicleRelId, String copyFromVehicleConfigId) throws Exception {
        StringList partIds = getPositionPartIds(context, positionId);
        for (Object partIdObj : partIds) {
            String partId = String.valueOf(partIdObj);
            createMatrixRelForNewVehicleConfig(context, positionId, newPositionVehicleRelId, partId, copyFromVehicleConfigId);
        }
    }

    /**
     * 创建新整车配置对应的矩阵关系并复制来源配置数量和选配
     **
     * @param context
     * @param positionId 位置产品ID
     * @param newPositionVehicleRelId 新建整车配置与位置产品关系ID
     * @param partId 零件ID
     * @param copyFromVehicleConfigId 复制来源整车配置ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void createMatrixRelForNewVehicleConfig(Context context, String positionId, String newPositionVehicleRelId, String partId, String copyFromVehicleConfigId) throws Exception {
        String copyFromPositionVehicleRelId = UIUtil.isNullOrEmpty(copyFromVehicleConfigId) ? "" : getPositionVehicleConfigRelId(context, positionId, copyFromVehicleConfigId);
        ensureMatrixRel(context, newPositionVehicleRelId, partId, copyFromPositionVehicleRelId);
    }

    /**
     * 为位置产品零件创建所有整车配置矩阵关系
     **
     * @param context
     * @param positionId 位置产品ID
     * @param partId 零件ID
     * @param copyFromVehicleConfigId 复制来源整车配置ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void createMatrixRelsForPositionPart(Context context, String positionId, String partId, String copyFromVehicleConfigId) throws Exception {
        Map positionVehicleRelMap = getPositionVehicleConfigRelMap(context, positionId);
        String copyFromPositionVehicleRelId = UIUtil.isNullOrEmpty(copyFromVehicleConfigId) ? "" : getPositionVehicleConfigRelId(context, positionId, copyFromVehicleConfigId);
        for (Object relIdObj : positionVehicleRelMap.values()) {
            ensureMatrixRel(context, String.valueOf(relIdObj), partId, copyFromPositionVehicleRelId);
        }
    }

    /**
     * 创建矩阵关系并复制来源矩阵属性
     **
     * @param context
     * @param positionVehicleRelId 位置产品与整车配置关系ID
     * @param partId 零件ID
     * @param copyFromPositionVehicleRelId 复制来源位置产品与整车配置关系ID
     * @return String 矩阵关系ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private String ensureMatrixRel(Context context, String positionVehicleRelId, String partId, String copyFromPositionVehicleRelId) throws Exception {
        String matrixRelId = getMatrixRelId(context, positionVehicleRelId, partId);
        if (UIUtil.isNotNullAndNotEmpty(matrixRelId)) {
            return matrixRelId;
        }
        matrixRelId = MqlUtil.mqlCommand(context,
                "add connection $1 fromrel $2 to $3 select $4 dump",
                REL_PV_TO_VPM,
                positionVehicleRelId,
                partId,
                "id");
        if (UIUtil.isNotNullAndNotEmpty(copyFromPositionVehicleRelId)) {
            String sourceMatrixRelId = getMatrixRelId(context, copyFromPositionVehicleRelId, partId);
            if (UIUtil.isNotNullAndNotEmpty(sourceMatrixRelId)) {
                MqlUtil.mqlCommand(context, "modify connection $1 $2 $3", matrixRelId, ATTR_NUMBER_OF_CHAIRS, getConnectionAttribute(context, sourceMatrixRelId, ATTR_NUMBER_OF_CHAIRS));
                MqlUtil.mqlCommand(context, "modify connection $1 $2 $3", matrixRelId, ATTR_OPTIONAL_OR_NOT, getConnectionAttribute(context, sourceMatrixRelId, ATTR_OPTIONAL_OR_NOT));
            }
        }
        return matrixRelId;
    }

    /**
     * 产品配置表关联零件
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param partId 零件logicalid对象ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void ensureProductConfigPartRel(Context context, String productConfigId, String partId) throws Exception {
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        String logicalId = getVpmReferenceLogicalId(context, partId);
        if (!hasProductConfigPartByLogicalId(context, productConfigId, logicalId)) {
            DomainRelationship.connect(context, productConfigObj, REL_PRODUCT_CONFIG_TO_VPM, DomainObject.newInstance(context, partId));
        }
    }

    /**
     * 判断产品配置表是否已关联零件logicalid
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param logicalId 零件logicalid
     * @return boolean 已关联返回true
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private boolean hasProductConfigPartByLogicalId(Context context, String productConfigId, String logicalId) throws Exception {
        if (UIUtil.isNullOrEmpty(productConfigId) || UIUtil.isNullOrEmpty(logicalId)) {
            return false;
        }
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        StringList logicalIds = productConfigObj.getInfoList(context, "from[" + REL_PRODUCT_CONFIG_TO_VPM + "].to." + SELECT_LOGICAL_ID);
        return logicalIds.contains(logicalId);
    }

    /**
     * 判断位置产品是否已关联零件logicalid
     **
     * @param context
     * @param positionId 位置产品ID
     * @param partId 零件logicalid对象ID
     * @return boolean 已关联返回true
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private boolean hasPositionPartByLogicalId(Context context, String positionId, String partId) throws Exception {
        if (UIUtil.isNullOrEmpty(positionId) || UIUtil.isNullOrEmpty(partId)) {
            return false;
        }
        String logicalId = getVpmReferenceLogicalId(context, partId);
        if (UIUtil.isNullOrEmpty(logicalId)) {
            return false;
        }
        DomainObject positionObj = DomainObject.newInstance(context, positionId);
        StringList logicalIds = positionObj.getInfoList(context, "from[" + REL_POSITION_TO_VPM + "].to." + SELECT_LOGICAL_ID);
        return logicalIds.contains(logicalId);
    }

    private StringList getProductConfigPartIds(Context context, String productConfigId) throws Exception {
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        return productConfigObj.getInfoList(context, "from[" + REL_PRODUCT_CONFIG_TO_VPM + "].to.id");
    }

    /**
     * 获取位置产品已关联零件ID
     **
     * @param context
     * @param positionId 位置产品ID
     * @return StringList 位置产品已关联零件ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private StringList getPositionPartIds(Context context, String positionId) throws Exception {
        DomainObject positionObj = DomainObject.newInstance(context, positionId);
        return positionObj.getInfoList(context, "from[" + REL_POSITION_TO_VPM + "].to.id");
    }

    private String getPositionPartRelId(Context context, String positionId, String partId) throws Exception {
        DomainObject positionObj = DomainObject.newInstance(context, positionId);
        StringList relIds = positionObj.getInfoList(context, "from[" + REL_POSITION_TO_VPM + "|to.id==" + partId + "].id");
        return relIds.isEmpty() ? "" : normalizeSelectValue(relIds.get(0));
    }

    private Map getPositionVehicleConfigRelMap(Context context, String positionId) throws Exception {
        Map result = new HashMap();
        DomainObject positionObj = DomainObject.newInstance(context, positionId);
        StringList busSelects = new StringList(SELECT_ID);
        StringList relSelects = new StringList("id[connection]");
        MapList vehicleConfigs = positionObj.getRelatedObjects(context,
                REL_POSITION_TO_VEHICLE_CONFIG,
                TYPE_VEHICLE_CONFIGURATION,
                busSelects,
                relSelects,
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
        for (Object configObj : vehicleConfigs) {
            Map configMap = (Map) configObj;
            result.put(configMap.get(SELECT_ID), normalizeSelectValue(configMap.get("id[connection]")));
        }
        return result;
    }

    private void deleteMatrixRelsForPositionPart(Context context, String positionId, String positionPartRelId) throws Exception {
        String partId = resolvePositionPartVpmId(context, positionPartRelId);
        if (UIUtil.isNullOrEmpty(partId)) {
            return;
        }
        Map positionVehicleRelMap = getPositionVehicleConfigRelMap(context, positionId);
        for (Object relIdObj : positionVehicleRelMap.values()) {
            String matrixRelId = getMatrixRelId(context, String.valueOf(relIdObj), partId);
            if (UIUtil.isNotNullAndNotEmpty(matrixRelId)) {
                MqlUtil.mqlCommand(context, "delete connection $1", normalizeSelectValue(matrixRelId));
            }
        }
    }

    /**
     * 删除位置产品关联零件并清理产品配置表孤立零件关系
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param positionId 位置产品ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void deleteAllPositionPartRelations(Context context, String productConfigId, String positionId) throws Exception {
        DomainObject positionObj = DomainObject.newInstance(context, positionId);
        StringList relIds = positionObj.getInfoList(context, "from[" + REL_POSITION_TO_VPM + "].id");
        for (Object relIdObj : relIds) {
            String relId = normalizeSelectValue(relIdObj);
            if (UIUtil.isNotNullAndNotEmpty(relId)) {
                String partId = resolvePositionPartVpmId(context, relId);
                deleteMatrixRelsForPositionPart(context, positionId, relId);
                MqlUtil.mqlCommand(context, "delete connection $1", relId);
                disconnectProductConfigPartIfUnused(context, productConfigId, partId);
            }
        }
    }

    /**
     * 产品配置表断开未被位置产品使用的零件关系
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param partId 零件ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void disconnectProductConfigPartIfUnused(Context context, String productConfigId, String partId) throws Exception {
        if (UIUtil.isNullOrEmpty(productConfigId) || UIUtil.isNullOrEmpty(partId)) {
            return;
        }
        String logicalId = getVpmReferenceLogicalId(context, partId);
        if (UIUtil.isNullOrEmpty(logicalId)) {
            return;
        }
        MapList positions = getPositionProductList(context, productConfigId, false);
        for (Object positionObj : positions) {
            Map positionMap = (Map) positionObj;
            DomainObject positionDomainObj = DomainObject.newInstance(context, (String) positionMap.get(SELECT_ID));
            StringList logicalIds = positionDomainObj.getInfoList(context, "from[" + REL_POSITION_TO_VPM + "].to." + SELECT_LOGICAL_ID);
            if (logicalIds.contains(logicalId)) {
                return;
            }
        }
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        StringList relIds = productConfigObj.getInfoList(context, "from[" + REL_PRODUCT_CONFIG_TO_VPM + "].id");
        StringList logicalIds = productConfigObj.getInfoList(context, "from[" + REL_PRODUCT_CONFIG_TO_VPM + "].to." + SELECT_LOGICAL_ID);
        for (int i = 0; i < relIds.size(); i++) {
            String currentLogicalId = i < logicalIds.size() ? normalizeSelectValue(logicalIds.get(i)) : "";
            if (logicalId.equals(currentLogicalId)) {
                MqlUtil.mqlCommand(context, "delete connection $1", normalizeSelectValue(relIds.get(i)));
            }
        }
    }

    private void deletePositionVehicleConfigRelations(Context context, String productConfigId, String vehicleConfigId) throws Exception {
        MapList positions = getPositionProductList(context, productConfigId);
        for (Object positionObj : positions) {
            Map positionMap = (Map) positionObj;
            String relId = getPositionVehicleConfigRelId(context, (String) positionMap.get(SELECT_ID), vehicleConfigId);
            if (UIUtil.isNotNullAndNotEmpty(relId)) {
                deleteMatrixRelsForPositionVehicleRel(context, relId);
                MqlUtil.mqlCommand(context, "delete connection $1", normalizeSelectValue(relId));
            }
        }
    }

    private void deleteAllPositionVehicleConfigRelations(Context context, String positionId) throws Exception {
        DomainObject positionObj = DomainObject.newInstance(context, positionId);
        StringList relIds = positionObj.getInfoList(context, "from[" + REL_POSITION_TO_VEHICLE_CONFIG + "].id");
        for (Object relIdObj : relIds) {
            String relId = normalizeSelectValue(relIdObj);
            if (UIUtil.isNotNullAndNotEmpty(relId)) {
                deleteMatrixRelsForPositionVehicleRel(context, relId);
                MqlUtil.mqlCommand(context, "delete connection $1", relId);
            }
        }
    }

    private void deleteMatrixRelsForPositionVehicleRel(Context context, String positionVehicleRelId) throws Exception {
        StringList matrixRelIds = getConnectionInfoListByMql(context, positionVehicleRelId, "from[" + REL_PV_TO_VPM + "].id");
        for (Object relIdObj : matrixRelIds) {
            MqlUtil.mqlCommand(context, "delete connection $1", normalizeSelectValue(relIdObj));
        }
    }

    private String getPositionVehicleConfigRelId(Context context, String positionId, String vehicleConfigId) throws Exception {
        DomainObject positionObj = DomainObject.newInstance(context, positionId);
        StringList relIds = positionObj.getInfoList(context,
                "from[" + REL_POSITION_TO_VEHICLE_CONFIG + "|to.id==" + vehicleConfigId + "].id");
        return relIds.isEmpty() ? "" : normalizeSelectValue(relIds.get(0));
    }

    private void disconnectVehicleConfigFromProductConfig(Context context, String productConfigId, String vehicleConfigId) throws Exception {
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        StringList relIds = productConfigObj.getInfoList(context,
                "from[" + REL_PRODUCT_CONFIG_TO_VEHICLE_CONFIG + "|to.id==" + vehicleConfigId + "].id");
        for (Object relIdObj : relIds) {
            MqlUtil.mqlCommand(context, "delete connection $1", normalizeSelectValue(relIdObj));
        }
    }

    private void disconnectPositionFromProductConfig(Context context, String productConfigId, String positionId) throws Exception {
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        StringList relIds = productConfigObj.getInfoList(context,
                "from[" + REL_PRODUCT_CONFIG_TO_POSITION + "|to.id==" + positionId + "].id");
        for (Object relIdObj : relIds) {
            MqlUtil.mqlCommand(context, "delete connection $1", normalizeSelectValue(relIdObj));
        }
    }

    private MapList getPositionProductList(Context context, String productConfigId) throws Exception {
        return getPositionProductList(context, productConfigId, true);
    }

    /**
     * 获取产品配置表位置产品列表
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param includeParts 是否加载位置产品下零件矩阵
     * @return MapList 位置产品列表
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private MapList getPositionProductList(Context context, String productConfigId, boolean includeParts) throws Exception {
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            return new MapList();
        }
        StringList positionSelects = JF_Util_mxJPO.basicBolistSel();
        positionSelects.add(SELECT_ATTR_TITLE);
        positionSelects.add(SELECT_ATTR_JFPP_TYPE);
        positionSelects.add(SELECT_ATTR_JFPP_TITLE);
        positionSelects.add(SELECT_DESCRIPTION);
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        MapList positionList = productConfigObj.getRelatedObjects(context,
                REL_PRODUCT_CONFIG_TO_POSITION,
                TYPE_POSITION_PRODUCT,
                positionSelects,
                JF_Util_mxJPO.basicRellistSel(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
        StringList partSelects = JF_Util_mxJPO.basicBolistSel();
        partSelects.add(SELECT_ATTR_PART_NUMBER);
        partSelects.add(SELECT_ATTR_PART_CN);
        partSelects.add(SELECT_ATTR_PART_LEVEL);
        StringList partRelSelects = JF_Util_mxJPO.basicRellistSel();
        partRelSelects.add(SELECT_REL_ATTR_ORDER);
        partRelSelects.add(SELECT_REL_ATTR_ASSEMBLY_LEVEL);
        String projectId = includeParts ? getProjectIdByProductConfig(context, productConfigId) : "";
        for (Object positionObj : positionList) {
            Map positionMap = (Map) positionObj;
            fillPositionPartData(context, positionMap, includeParts, partSelects, partRelSelects, projectId);
        }
        return positionList;
    }

    /**
     * 获取单个位置产品数据
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param positionId 位置产品ID
     * @param includeParts 是否加载位置产品下零件矩阵
     * @return Map 位置产品数据
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private Map getPositionProduct(Context context, String productConfigId, String positionId, boolean includeParts) throws Exception {
        StringList positionSelects = JF_Util_mxJPO.basicBolistSel();
        positionSelects.add(SELECT_ATTR_TITLE);
        positionSelects.add(SELECT_ATTR_JFPP_TYPE);
        positionSelects.add(SELECT_ATTR_JFPP_TITLE);
        positionSelects.add(SELECT_DESCRIPTION);
        Map positionMap = DomainObject.newInstance(context, positionId).getInfo(context, positionSelects);
        StringList partSelects = JF_Util_mxJPO.basicBolistSel();
        partSelects.add(SELECT_ATTR_PART_NUMBER);
        partSelects.add(SELECT_ATTR_PART_CN);
        partSelects.add(SELECT_ATTR_PART_LEVEL);
        StringList partRelSelects = JF_Util_mxJPO.basicRellistSel();
        partRelSelects.add(SELECT_REL_ATTR_ORDER);
        partRelSelects.add(SELECT_REL_ATTR_ASSEMBLY_LEVEL);
        String projectId = includeParts ? getProjectIdByProductConfig(context, productConfigId) : "";
        fillPositionPartData(context, positionMap, includeParts, partSelects, partRelSelects, projectId);
        return positionMap;
    }

    /**
     * 填充位置产品零件数量和明细
     **
     * @param context
     * @param positionMap 位置产品数据
     * @param includeParts 是否加载位置产品下零件矩阵
     * @param partSelects 零件select
     * @param partRelSelects 零件关系select
     * @param projectId 产品配置表所属项目ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private void fillPositionPartData(Context context, Map positionMap, boolean includeParts, StringList partSelects, StringList partRelSelects, String projectId) throws Exception {
        String positionId = (String) positionMap.get(SELECT_ID);
        DomainObject positionDomainObj = DomainObject.newInstance(context, positionId);
        if (!includeParts) {
            StringList partRelIds = positionDomainObj.getInfoList(context, "from[" + REL_POSITION_TO_VPM + "].id");
            positionMap.put("partCount", String.valueOf(partRelIds.size()));
            positionMap.put("parts", new MapList());
            return;
        }
        MapList partList = positionDomainObj.getRelatedObjects(context,
                REL_POSITION_TO_VPM,
                TYPE_VPM_REFERENCE,
                partSelects,
                partRelSelects,
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
//        sortPartListByOrder(partList);
            partList.sort( SELECT_REL_ATTR_ORDER , "ascending", "integer");//这样排序速度快
        positionMap.put("partCount", String.valueOf(partList.size()));
        fillPartDisplayValues(context, partList, projectId);
        fillPartMatrixValues(context, positionId, partList);
        positionMap.put("parts", partList);
    }

    /**
     * 填充零件展示字段
     **
     * @param context
     * @param partList 零件列表
     * @param projectId 产品配置表所属项目ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/21 16:13
     */
    private void fillPartDisplayValues(Context context, MapList partList, String projectId) throws Exception {
        fillCustomerPartInfoValues(context, partList, projectId);
        for (Object partObj : partList) {
            Map partMap = (Map) partObj;
            partMap.put("customerPartNumber", normalizeSelectValue(partMap.get(ATTR_CUSTOMER_PART_NUMBER)));
            partMap.put("customerPartName", normalizeSelectValue(partMap.get(ATTR_CUSTOMER_PART_NAME)));
            partMap.put("assemblyLevel", partMap.get(SELECT_REL_ATTR_ASSEMBLY_LEVEL));
            partMap.put("order", partMap.get(SELECT_REL_ATTR_ORDER));
        }
    }

    /**
     * 批量填充零件客户零件号、客户零件名称、Direct buy
     **
     * @param context
     * @param partList 零件列表
     * @param projectId 当前对象所属项目ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/21 16:13
     */
    private void fillCustomerPartInfoValues(Context context, MapList partList, String projectId) throws Exception {
        if (partList == null || partList.isEmpty()) {
            return;
        }
        if (UIUtil.isNullOrEmpty(projectId)) {
            for (Object partObj : partList) {
                Map partMap = (Map) partObj;
                partMap.put(ATTR_CUSTOMER_PART_NUMBER, "");
                partMap.put(ATTR_CUSTOMER_PART_NAME, "");
                partMap.put(COLUMN_DIRECT_BUY, "");
            }
            return;
        }
        Map latestPartIdMap = new HashMap();
        Set unresolvedPartIds = new LinkedHashSet();
        for (Object partObj : partList) {
            Map partMap = (Map) partObj;
            String partId = normalizeSelectValue(partMap.get(SELECT_ID));
            if (UIUtil.isNotNullAndNotEmpty(partId)) {
                String latestPartId = normalizeSelectValue(partMap.get(KEY_LATEST_PART_ID));
                if (UIUtil.isNotNullAndNotEmpty(latestPartId)) {
                    latestPartIdMap.put(partId, latestPartId);
                } else {
                    unresolvedPartIds.add(partId);
                }
            }
        }
        if (!unresolvedPartIds.isEmpty()) {
            StringList partIds = new StringList();
            partIds.addAll(unresolvedPartIds);
            latestPartIdMap.putAll(getLatestPartIdMap(context, partIds));
        }
        Map customerPartInfoCache = new HashMap();
        JF_DR_mxJPO dr = new JF_DR_mxJPO();
        for (Object partObj : partList) {
            Map partMap = (Map) partObj;
            String partId = normalizeSelectValue(partMap.get(SELECT_ID));
            String latestPartId = normalizeSelectValue(latestPartIdMap.get(partId));
            if (UIUtil.isNullOrEmpty(latestPartId)) {
                latestPartId = partId;
            }
            Map customerPartInfo = (Map) customerPartInfoCache.get(latestPartId);
            if (customerPartInfo == null) {
                customerPartInfo = dr.getDRCustomerPartsDBRelationInfo(context, latestPartId, projectId);
                customerPartInfoCache.put(latestPartId, customerPartInfo);
            }
            partMap.put(ATTR_CUSTOMER_PART_NUMBER, normalizeSelectValue(customerPartInfo.get(SELECT_REL_ATTR_CUSTOMER_PART_NUMBER)));
            partMap.put(ATTR_CUSTOMER_PART_NAME, normalizeSelectValue(customerPartInfo.get(SELECT_REL_ATTR_CUSTOMER_PART_NAME)));
            partMap.put(COLUMN_DIRECT_BUY, normalizeSelectValue(customerPartInfo.get(SELECT_REL_ATTR_DIRECT_BUY)));
        }
    }

    /**
     * 两次批量查询零件Major Revision，并按V_isLastVersion获取最新版ID
     **
     * @param context
     * @param partIds 当前零件ID列表
     * @return Map 当前零件ID与最新版零件ID映射
     * @throws Exception
     * @author caipan
     * @date 2026/7/21 16:13
     */
    private Map getLatestPartIdMap(Context context, StringList partIds) throws Exception {
        Map result = new HashMap();
        if (partIds == null || partIds.isEmpty()) {
            return result;
        }
        String revisionIdSelect = "majorids.id";
        StringList selects = new StringList(SELECT_ID);
        selects.add(revisionIdSelect);
        String[] partIdArray = (String[]) partIds.toArray(new String[partIds.size()]);
        MapList revisionInfoList = DomainObject.getInfo(context, partIdArray, selects);
        LOGGER.info("revisionInfoList:{}",revisionInfoList);
        Map revisionIdsByPartId = new HashMap();
        Set allRevisionIds = new LinkedHashSet();
        for (Object revisionInfoObj : revisionInfoList) {
            Map revisionInfo = (Map) revisionInfoObj;
            String partId = normalizeSelectValue(revisionInfo.get(SELECT_ID));
            StringList revisionIds = toStringList(revisionInfo.get(revisionIdSelect));
            for (Object entryObj : revisionInfo.entrySet()) {
                Map.Entry entry = (Map.Entry) entryObj;
                String key = normalizeSelectValue(entry.getKey());
                String revisionId = normalizeSelectValue(entry.getValue());
                if (key.startsWith("majorids[") && key.endsWith("].id")
                        && UIUtil.isNotNullAndNotEmpty(revisionId) && !revisionIds.contains(revisionId)) {
                    revisionIds.add(revisionId);
                }
            }
            if (revisionIds.isEmpty() && UIUtil.isNotNullAndNotEmpty(partId)) {
                revisionIds.add(partId);
            }
            revisionIdsByPartId.put(partId, revisionIds);
            allRevisionIds.addAll(revisionIds);
        }
        Map latestRevisionIdMap = new HashMap();
        if (!allRevisionIds.isEmpty()) {
            StringList revisionSelects = new StringList(SELECT_ID);
            revisionSelects.add(SELECT_PHYSICAL_ID);
            revisionSelects.add(SELECT_ATTR_V_IS_LAST_VERSION);
            String[] revisionIdArray = (String[]) allRevisionIds.toArray(new String[allRevisionIds.size()]);
            MapList revisionList = DomainObject.getInfo(context, revisionIdArray, revisionSelects);
            for (Object revisionObj : revisionList) {
                Map revision = (Map) revisionObj;
                if (!"TRUE".equalsIgnoreCase(normalizeSelectValue(revision.get(SELECT_ATTR_V_IS_LAST_VERSION)))) {
                    continue;
                }
                String revisionId = normalizeSelectValue(revision.get(SELECT_ID));
                String physicalId = normalizeSelectValue(revision.get(SELECT_PHYSICAL_ID));
                String latestPartId = UIUtil.isNotNullAndNotEmpty(physicalId) ? physicalId : revisionId;
                latestRevisionIdMap.put(revisionId, latestPartId);
                latestRevisionIdMap.put(physicalId, latestPartId);
            }
        }
        for (Object entryObj : revisionIdsByPartId.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            String partId = normalizeSelectValue(entry.getKey());
            String latestPartId = partId;
            StringList revisionIds = (StringList) entry.getValue();
            for (int i = revisionIds.size() - 1; i >= 0; i--) {
                String revisionId = normalizeSelectValue(revisionIds.get(i));
                String resolvedLatestPartId = normalizeSelectValue(latestRevisionIdMap.get(revisionId));
                if (UIUtil.isNotNullAndNotEmpty(resolvedLatestPartId)) {
                    latestPartId = resolvedLatestPartId;
                    break;
                }
            }
            result.put(partId, latestPartId);
        }
        return result;
    }

    private void fillPartMatrixValues(Context context, String positionId, MapList partList) throws Exception {
        Map positionVehicleRelMap = getPositionVehicleConfigRelMap(context, positionId);
        Map matrixValueMap = getPositionMatrixValueMap(context, positionVehicleRelMap);
        for (Object partObj : partList) {
            Map partMap = (Map) partObj;
            String partId = (String) partMap.get(SELECT_ID);
            Map matrixValues = new HashMap();
            for (Object entryObj : positionVehicleRelMap.entrySet()) {
                Map.Entry entry = (Map.Entry) entryObj;
                String vehicleConfigId = (String) entry.getKey();
                Map matrixMap = (Map) matrixValueMap.get(vehicleConfigId + "|" + partId);
                if (matrixMap == null) {
                    continue;
                }
                matrixValues.put(vehicleConfigId, matrixMap);
            }
            partMap.put("matrix", matrixValues);
        }
    }

    /**
     * 批量获取位置产品矩阵值
     **
     * @param context
     * @param positionVehicleRelMap 位置产品与整车配置关系Map
     * @return Map 矩阵值Map
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private Map getPositionMatrixValueMap(Context context, Map positionVehicleRelMap) throws Exception {
        Map result = new HashMap();
        Map matrixVehicleMap = new HashMap();
        StringList matrixRelIds = new StringList();
        for (Object entryObj : positionVehicleRelMap.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            String vehicleConfigId = (String) entry.getKey();
            String positionVehicleRelId = (String) entry.getValue();
            String selectPrefix = "frommid[" + REL_PV_TO_VPM + "]";
            StringList vehicleMatrixRelIds = getConnectionInfoListByMql(context, positionVehicleRelId, selectPrefix + ".id");
            for (Object matrixRelIdObj : vehicleMatrixRelIds) {
                String matrixRelId = normalizeSelectValue(matrixRelIdObj);
                if (UIUtil.isNotNullAndNotEmpty(matrixRelId)) {
                    matrixRelIds.add(matrixRelId);
                    matrixVehicleMap.put(matrixRelId, vehicleConfigId);
                }
            }
        }
        if (matrixRelIds.isEmpty()) {
            return result;
        }
        StringList matrixSelects = new StringList(SELECT_ID);
        matrixSelects.add("to.id");
        matrixSelects.add(SELECT_REL_ATTR_NUMBER_OF_CHAIRS);
        matrixSelects.add(SELECT_REL_ATTR_OPTIONAL_OR_NOT);
        String[] matrixRelIdArray = (String[]) matrixRelIds.toArray(new String[matrixRelIds.size()]);
        MapList matrixInfoList = DomainRelationship.getInfo(context, matrixRelIdArray, matrixSelects);
        for (Object matrixInfoObj : matrixInfoList) {
            Map matrixInfo = (Map) matrixInfoObj;
            String matrixRelId = normalizeSelectValue(matrixInfo.get(SELECT_ID));
            String partId = normalizeSelectValue(matrixInfo.get("to.id"));
            String vehicleConfigId = normalizeSelectValue(matrixVehicleMap.get(matrixRelId));
            if (UIUtil.isNullOrEmpty(matrixRelId) || UIUtil.isNullOrEmpty(partId) || UIUtil.isNullOrEmpty(vehicleConfigId)) {
                continue;
            }
            Map matrixMap = new HashMap();
            matrixMap.put("quantity", normalizeSelectValue(matrixInfo.get(SELECT_REL_ATTR_NUMBER_OF_CHAIRS)));
            matrixMap.put("optional", normalizeSelectValue(matrixInfo.get(SELECT_REL_ATTR_OPTIONAL_OR_NOT)));
            matrixMap.put("relId", matrixRelId);
            result.put(vehicleConfigId + "|" + partId, matrixMap);
        }
        return result;
    }

    private String getMatrixRelId(Context context, String positionVehicleRelId, String targetVpmId) throws Exception {
        DomainObject targetVpmObj = DomainObject.newInstance(context, targetVpmId);
        StringList relIds = targetVpmObj.getInfoList(context, "to[" + REL_PV_TO_VPM + "].id");
        StringList fromRelIds = targetVpmObj.getInfoList(context, "to[" + REL_PV_TO_VPM + "].fromrel.id");
        for (int i = 0; i < relIds.size() && i < fromRelIds.size(); i++) {
            if (positionVehicleRelId.equals(normalizeSelectValue(fromRelIds.get(i)))) {
                return normalizeSelectValue(relIds.get(i));
            }
        }
        return "";
    }

    private String resolvePositionPartVpmId(Context context, String positionPartRelId) throws Exception {
        StringList vpmIds = getConnectionInfoListByMql(context, positionPartRelId, "to.id");
        return vpmIds.isEmpty() ? "" : (String) vpmIds.get(0);
    }

    /**
     * 获取位置产品与零件关系信息
     **
     * @param context
     * @param positionPartRelId 位置产品与零件关系ID
     * @return Map 关系信息
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private Map getPositionPartRelInfo(Context context, String positionPartRelId) throws Exception {
        Map result = new HashMap();
        String output = MqlUtil.mqlCommand(context, "print connection $1 select $2 $3 $4 dump $5", positionPartRelId, "from.id", "type", "to.id", "|");
        String[] items = output == null ? new String[0] : output.split("\\|", -1);
        result.put("fromId", items.length > 0 ? normalizeSelectValue(items[0]) : "");
        result.put("type", items.length > 1 ? normalizeSelectValue(items[1]) : "");
        result.put("toId", items.length > 2 ? normalizeSelectValue(items[2]) : "");
        return result;
    }

    /**
     * 判断关系是否为当前位置产品零件关系
     **
     * @param context
     * @param positionId 位置产品ID
     * @param relId 关系ID
     * @return boolean 是当前位置产品零件关系时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private boolean isPositionPartRel(Context context, String positionId, String relId) throws Exception {
        if (UIUtil.isNullOrEmpty(positionId) || UIUtil.isNullOrEmpty(relId)) {
            return false;
        }
        try {
            String fromId = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump", relId, "from.id");
            String relType = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump", relId, "type");
            return positionId.equals(fromId) && REL_POSITION_TO_VPM.equals(relType);
        } catch (Exception e) {
            return false;
        }
    }

    private BigDecimal getNextPositionPartOrder(Context context, String positionId) throws Exception {
        DomainObject positionObj = DomainObject.newInstance(context, positionId);
        StringList orderList = positionObj.getInfoList(context, "from[" + REL_POSITION_TO_VPM + "].attribute[" + ATTR_ORDER + "]");
        BigDecimal maxOrder = BigDecimal.ZERO;
        for (Object orderObj : orderList) {
            BigDecimal order = parseOrder(String.valueOf(orderObj));
            if (order.compareTo(maxOrder) > 0) {
                maxOrder = order;
            }
        }
        return maxOrder.add(new BigDecimal("1000"));
    }

    private BigDecimal calculateNewOrder(Context context, String positionId, String previousRelId, String nextRelId) throws Exception {
        boolean hasPrevious = UIUtil.isNotNullAndNotEmpty(previousRelId) && isPositionPartRel(context, positionId, previousRelId);
        boolean hasNext = UIUtil.isNotNullAndNotEmpty(nextRelId) && isPositionPartRel(context, positionId, nextRelId);
        if (!hasPrevious && hasNext) {
            return parseOrder(getConnectionAttribute(context, nextRelId, ATTR_ORDER)).subtract(new BigDecimal("1000"));
        }
        if (hasPrevious && hasNext) {
            BigDecimal previousOrder = parseOrder(getConnectionAttribute(context, previousRelId, ATTR_ORDER));
            BigDecimal nextOrder = parseOrder(getConnectionAttribute(context, nextRelId, ATTR_ORDER));
            return previousOrder.add(nextOrder).divide(new BigDecimal("2"));
        }
        if (hasPrevious) {
            return parseOrder(getConnectionAttribute(context, previousRelId, ATTR_ORDER)).add(new BigDecimal("1000"));
        }
        return getNextPositionPartOrder(context, positionId);
    }

    private void sortPartListByOrder(MapList partList) {
        Collections.sort(partList, new Comparator() {
            public int compare(Object leftObj, Object rightObj) {
                Map left = (Map) leftObj;
                Map right = (Map) rightObj;
                return parseOrder(String.valueOf(left.get(SELECT_REL_ATTR_ORDER))).compareTo(parseOrder(String.valueOf(right.get(SELECT_REL_ATTR_ORDER))));
            }
        });
    }

    private BigDecimal parseOrder(String orderValue) {
        if (UIUtil.isNullOrEmpty(orderValue) || "null".equals(orderValue)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(orderValue);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String formatOrder(BigDecimal order) {
        return order.stripTrailingZeros().toPlainString();
    }

    private String getAssemblyLevel(String partNumber) {
        return UIUtil.isNotNullAndNotEmpty(partNumber) && partNumber.startsWith("GX") ? "-1" : "0";
    }

    private String getConnectionAttribute(Context context, String relId, String attrName) throws Exception {
        return MqlUtil.mqlCommand(context, "print connection $1 select $2 dump", relId, "attribute[" + attrName + "]");
    }

    private StringList getConnectionInfoListByMql(Context context, String relId, String select) throws Exception {
        StringList list = new StringList();
        String output = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump $3", relId, select, "|");
        if (UIUtil.isNullOrEmpty(output)) {
            return list;
        }
        String[] items = output.split("\\|");
        for (String item : items) {
            if (UIUtil.isNotNullAndNotEmpty(item)) {
                list.add(normalizeSelectValue(item));
            }
        }
        return list;
    }

    /**
     * 获取连接select列表并保留空值
     **
     * @param context
     * @param relId 关系ID
     * @param select select表达式
     * @return StringList select结果列表
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private StringList getConnectionInfoListByMqlKeepEmpty(Context context, String relId, String select) throws Exception {
        StringList list = new StringList();
        String output = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump $3", relId, select, "|");
        if (output == null) {
            return list;
        }
        String[] items = output.split("\\|", -1);
        for (String item : items) {
            list.add(UIUtil.isNotNullAndNotEmpty(item) ? normalizeSelectValue(item) : "");
        }
        return list;
    }

    private String normalizeSelectValue(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).trim();
        int lineIndex = text.lastIndexOf('\n');
        if (lineIndex >= 0) {
            text = text.substring(lineIndex + 1).trim();
        }
        int equalsIndex = text.lastIndexOf('=');
        if (equalsIndex >= 0) {
            text = text.substring(equalsIndex + 1).trim();
        }
        return text;
    }

    /**
     * 格式化销量数字
     **
     * @param context
     * @param value 输入值
     * @return String 空值或纯数字
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    private String normalizeQuantityValue(Context context, Object value) throws Exception {
        String text = normalizeSelectValue(value);
        if (UIUtil.isNullOrEmpty(text)) {
            return "";
        }
        text = text.replace(",", "");
        if (!text.matches("\\d+")) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigParts.QuantityNumberOnly", "Quantity can only contain numbers."));
        }
        return text;
    }

    /**
     * 格式化ID列表
     **
     * @param value 输入值
     * @return StringList ID列表
     * @author caipan
     * @date 2026/7/3 16:13
     */
    private StringList normalizeIdList(Object value) {
        StringList result = new StringList();
        if (value == null) {
            return result;
        }
        if (value instanceof Collection) {
            for (Object item : (Collection) value) {
                String id = normalizeSelectValue(item);
                if (UIUtil.isNotNullAndNotEmpty(id)) {
                    result.add(id);
                }
            }
            return result;
        }
        if (value instanceof String[]) {
            for (String item : (String[]) value) {
                String id = normalizeSelectValue(item);
                if (UIUtil.isNotNullAndNotEmpty(id)) {
                    result.add(id);
                }
            }
            return result;
        }
        String text = normalizeSelectValue(value);
        if (UIUtil.isNullOrEmpty(text)) {
            return result;
        }
        String[] items = text.split(",");
        for (String item : items) {
            String id = normalizeSelectValue(item);
            if (UIUtil.isNotNullAndNotEmpty(id)) {
                result.add(id);
            }
        }
        return result;
    }

    /**
     * 格式化0-100范围内的比例数字，支持小数
     **
     * @param context
     * @param value 输入值
     * @return String 空值或0-100数字
     * @throws Exception
     * @author caipan
     * @date 2026/7/2 16:13
     */
    private String normalizePercentValue(Context context, Object value) throws Exception {
        String text = normalizeSelectValue(value);
        if (UIUtil.isNullOrEmpty(text)) {
            return "";
        }
        //20260831 update by caipan 配置比例改用BigDecimal校验并支持小数
        if (!text.matches("\\d+(?:\\.\\d+)?")) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.RatioRangeInvalid", "Configuration ratio must be a number from 0 to 100; decimals are allowed."));
        }
        BigDecimal percent = new BigDecimal(text);
        if (percent.compareTo(BigDecimal.ZERO) < 0 || percent.compareTo(new BigDecimal("100")) > 0) {
            throw new Exception(getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.RatioRangeInvalid", "Configuration ratio must be a number from 0 to 100; decimals are allowed."));
        }
        return percent.stripTrailingZeros().toPlainString();
    }

    private DomainObject createAutoNamedObject(Context context, String symbolicType, String symbolicPolicy) throws Exception {
        String objectId = FrameworkUtil.autoName(context, symbolicType, symbolicPolicy);
        DomainObject object = DomainObject.newInstance(context);
        object.setId(objectId);
        return object;
    }

    private String joinSet(Set values) {
        StringBuilder builder = new StringBuilder();
        for (Object value : values) {
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(value);
        }
        return builder.toString();
    }

    /**
     * 解析搜索结果中的零件logicalid对象ID
     **
     * @param context
     * @param searchResultIds 搜索结果ID
     * @return StringList 零件logicalid对象ID集合
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private StringList resolveVpmReferenceIds(Context context, StringList searchResultIds) throws Exception {
        StringList resolvedIds = new StringList();
        for (Object searchResultIdObj : searchResultIds) {
            String searchResultId = String.valueOf(searchResultIdObj);
            String resolvedId = resolveVpmReferenceId(context, searchResultId);
            if (UIUtil.isNullOrEmpty(resolvedId)) {
                throw new Exception("Failed to resolve selected part: " + searchResultId);
            }
            if (!resolvedIds.contains(resolvedId)) {
                resolvedIds.add(resolvedId);
            }
        }
        return resolvedIds;
    }

    /**
     * 解析搜索结果中的零件logicalid对象ID
     **
     * @param context
     * @param searchResultId 搜索结果ID
     * @return String 零件logicalid对象ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private String resolveVpmReferenceId(Context context, String searchResultId) throws Exception {
        if (UIUtil.isNullOrEmpty(searchResultId)) {
            return "";
        }
        try {
            DomainObject selectedObj = DomainObject.newInstance(context, searchResultId);
            String selectedType = selectedObj.getInfo(context, SELECT_TYPE);
            if (TYPE_VPM_REFERENCE.equals(selectedType)) {
                return getVpmReferenceLogicalObjectId(context, selectedObj.getInfo(context, SELECT_ID));
            }
        } catch (Exception e) {
            LOGGER.debug("resolveVpmReferenceId cannot open selected id directly: {}", searchResultId);
        }
        try {
            String safeValue = searchResultId.replace("\\", "\\\\").replace("\"", "\\\"");
            String where = "type == \"" + TYPE_VPM_REFERENCE + "\" && (id == \"" + safeValue + "\" || physicalid == \"" + safeValue + "\" || name == \"" + safeValue + "\")";
            String output = MqlUtil.mqlCommand(context,
                    "temp query bus $1 $2 $3 where $4 select $5 dump $6",
                    "*",
                    "*",
                    "*",
                    where,
                    SELECT_ID,
                    "|");
            if (UIUtil.isNullOrEmpty(output)) {
                return "";
            }
            String firstLine = output.split("\\r?\\n")[0];
            String[] values = firstLine.split("\\|");
            return values.length == 0 ? "" : getVpmReferenceLogicalObjectId(context, values[values.length - 1]);
        } catch (Exception e) {
            LOGGER.warn("resolveVpmReferenceId error for {}", searchResultId, e);
            throw e;
        }
    }

    /**
     * 获取零件logicalid对象ID
     **
     * @param context
     * @param vpmReferenceId 零件ID
     * @return String 零件logicalid对象ID
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private String getVpmReferenceLogicalObjectId(Context context, String vpmReferenceId) throws Exception {
        String logicalId = getVpmReferenceLogicalId(context, vpmReferenceId);
        if (UIUtil.isNullOrEmpty(logicalId)) {
            return "";
        }
        try {
            DomainObject logicalObj = DomainObject.newInstance(context, logicalId);
            if (TYPE_VPM_REFERENCE.equals(logicalObj.getInfo(context, SELECT_TYPE))) {
                return logicalObj.getInfo(context, SELECT_ID);
            }
        } catch (Exception e) {
            LOGGER.debug("getVpmReferenceLogicalObjectId cannot open logicalid directly: {}", logicalId);
        }
        String safeValue = logicalId.replace("\\", "\\\\").replace("\"", "\\\"");
        String where = "type == \"" + TYPE_VPM_REFERENCE + "\" && (id == \"" + safeValue + "\" || physicalid == \"" + safeValue + "\")";
        String output = MqlUtil.mqlCommand(context,
                "temp query bus $1 $2 $3 where $4 select $5 dump $6",
                "*",
                "*",
                "*",
                where,
                SELECT_ID,
                "|");
        if (UIUtil.isNullOrEmpty(output)) {
            throw new Exception("Failed to resolve selected part logicalid: " + vpmReferenceId);
        }
        String firstLine = output.split("\\r?\\n")[0];
        String[] values = firstLine.split("\\|");
        return values.length == 0 ? "" : values[values.length - 1];
    }

    /**
     * 获取零件logicalid
     **
     * @param context
     * @param vpmReferenceId 零件ID
     * @return String 零件logicalid
     * @throws Exception
     * @author caipan
     * @date 2026/6/29 16:13
     */
    private String getVpmReferenceLogicalId(Context context, String vpmReferenceId) throws Exception {
        if (UIUtil.isNullOrEmpty(vpmReferenceId)) {
            return "";
        }
        DomainObject partObj = DomainObject.newInstance(context, vpmReferenceId);
        return partObj.getInfo(context, SELECT_LOGICAL_ID);
    }

    private String getOwnerDepartment(Context context, String owner) {
        if (UIUtil.isNullOrEmpty(owner)) {
            return "";
        }
        try {
            String personId = com.matrixone.apps.domain.util.PersonUtil.getPersonObjectID(context, owner);
            if (UIUtil.isNotNullAndNotEmpty(personId)) {
                DomainObject personObj = DomainObject.newInstance(context, personId);
                String department = personObj.getInfo(context, "to[Member].from.name");
                return UIUtil.isNullOrEmpty(department) ? "" : department;
            }
        } catch (Exception e) {
            LOGGER.warn("getOwnerDepartment error for {}", owner, e);
        }
        return "";
    }

    /**
     * 产品配置表或售后件清单审批申请重复提交校验
     **
     * @param context
     * @param args 请求参数
     * @return Map 所选对象有效且不存在申请单关系时返回成功，否则返回失败及提示信息
     * @throws Exception
     * @author caipan
     * @date 2026/7/20 16:13
     */
    public Map checkProductConfigApprovalDuplicate(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String selectedObjectId = normalizeSelectValue(params.get("selectedObjectId"));
        if (UIUtil.isNullOrEmpty(selectedObjectId)) {
            selectedObjectId = normalizeSelectValue(params.get("productConfigId"));
        }
        if (UIUtil.isNullOrEmpty(selectedObjectId)) {
            return fail(getFrameworkString(context, RESOURCE_ROUTE_CONTENT_INVALID_SELECTION,
                    "Please select a valid product configuration table or service parts list."));
        }
        String selectedObjectType = DomainObject.newInstance(context, selectedObjectId).getInfo(context, SELECT_TYPE);
        //20260828 update by caipan 根据入口所选对象类型执行对应的重复审批校验
        String duplicateMessage;
        if (TYPE_PRODUCT_CONFIG_TABLE.equals(selectedObjectType)) {
            duplicateMessage = getProductConfigApprovalDuplicateMessage(context, selectedObjectId);
        } else if (TYPE_SERVICE_PARTS_LIST.equals(selectedObjectType)) {
            duplicateMessage = getServicePartsListApprovalDuplicateMessage(context, selectedObjectId);
        } else {
            return fail(getFrameworkString(context, RESOURCE_ROUTE_CONTENT_INVALID_SELECTION,
                    "Please select a valid product configuration table or service parts list."));
        }
        return UIUtil.isNullOrEmpty(duplicateMessage) ? success() : fail(duplicateMessage);
    }

    /**
     * 创建产品配置审批申请并校验已选择内容是否重复提交
     **
     * @param context
     * @param args 请求参数
     * @return Map 创建结果，重复提交时返回失败，客户件信息缺失时返回206提醒前端确认
     * @throws Exception
     * @author caipan
     * @date 2026/7/20 16:13
     */
    public Map createProductConfigApproval(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String title = normalizeSelectValue(params.get("title"));
        String productConfigId = normalizeSelectValue(params.get("productConfigId"));
        String servicePartsListId = normalizeSelectValue(params.get("servicePartsListId"));
        String description = normalizeSelectValue(params.get("description"));
        boolean forceSubmit = "true".equalsIgnoreCase(normalizeSelectValue(params.get("forceSubmit")));
        if (UIUtil.isNullOrEmpty(title)) {
            return fail("名称不能为空。");
        }
        //20260828 update by caipan 产品配置表改为非必填，仅校验已选择的审批内容
        String duplicateMessage = UIUtil.isNullOrEmpty(productConfigId)
                ? "" : getProductConfigApprovalDuplicateMessage(context, productConfigId);
        if (UIUtil.isNotNullAndNotEmpty(duplicateMessage)) {
            return fail(duplicateMessage);
        }
        duplicateMessage = UIUtil.isNullOrEmpty(servicePartsListId)
                ? "" : getServicePartsListApprovalDuplicateMessage(context, servicePartsListId);
        if (UIUtil.isNotNullAndNotEmpty(duplicateMessage)) {
            return fail(duplicateMessage);
        }
        List missingPartList = getMissingCustomerPartInfoList(context, productConfigId, servicePartsListId);
        if (!forceSubmit && !missingPartList.isEmpty()) {
            Map result = new HashMap();
            result.put("code", "206");
            result.put("mess", buildMissingCustomerPartMessage(context, missingPartList));
            return result;
        }
        String selectedContentId = UIUtil.isNotNullAndNotEmpty(productConfigId)
                ? productConfigId : servicePartsListId;
        String projectId = getConfigTableRouteProjectId(context, params, selectedContentId);
        if (UIUtil.isNullOrEmpty(projectId)) {
            return fail(getFrameworkString(context, RESOURCE_ROUTE_EMPTY_PROJECT,
                    "The approval application is not associated with a project."));
        }

        boolean isPush = false;
        try {
            ContextUtil.startTransaction(context, true);
            ContextUtil.pushContext(context);
            isPush = true;
            DomainObject routeObj = createAutoNamedObject(context, SYMBOLIC_TYPE_CONFIG_TABLE_ROUTE, SYMBOLIC_POLICY_CONFIG_TABLE_ROUTE);
            String routeId = routeObj.getId(context);
            routeObj.setAttributeValue(context, ATTR_TITLE, title);
            routeObj.setDescription(context, description);
            routeObj.addToObject(context, new RelationshipType(REL_CONFIG_ROUTE_TO_PROJECT), projectId);
            if (UIUtil.isNotNullAndNotEmpty(productConfigId)) {
                routeObj.addToObject(context, new RelationshipType(REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG), productConfigId);
            }
            if (UIUtil.isNotNullAndNotEmpty(servicePartsListId)) {
                routeObj.addToObject(context, new RelationshipType(REL_CONFIG_ROUTE_TO_SERVICE_PARTS_LIST), servicePartsListId);
            }
            createProductConfigApprovalDocuments(context, routeObj, params);
            ContextUtil.commitTransaction(context);
            Map result = success();
            result.put("objectId", routeId);
            result.put("mess", "创建成功。");
            return result;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("createProductConfigApproval error", e);
            return fail(e.getMessage());
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 产品配置审批申请提交前校验已选择内容的重复关系及客户件信息
     **
     * @param context
     * @param args 请求参数
     * @return Map 校验结果，重复提交时返回失败，客户件信息缺失时返回206
     * @throws Exception
     * @author caipan
     * @date 2026/7/20 16:13
     */
    public Map preCheckProductConfigApproval(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = normalizeSelectValue(params.get("productConfigId"));
        String servicePartsListId = normalizeSelectValue(params.get("servicePartsListId"));
        //20260828 update by caipan 产品配置表和售后件清单允许同时为空，仅校验已选择内容
        String duplicateMessage = UIUtil.isNullOrEmpty(productConfigId)
                ? "" : getProductConfigApprovalDuplicateMessage(context, productConfigId);
        if (UIUtil.isNotNullAndNotEmpty(duplicateMessage)) {
            return fail(duplicateMessage);
        }
        duplicateMessage = UIUtil.isNullOrEmpty(servicePartsListId)
                ? "" : getServicePartsListApprovalDuplicateMessage(context, servicePartsListId);
        if (UIUtil.isNotNullAndNotEmpty(duplicateMessage)) {
            return fail(duplicateMessage);
        }
        List missingPartList = getMissingCustomerPartInfoList(context, productConfigId, servicePartsListId);
        if (!missingPartList.isEmpty()) {
            Map result = new HashMap();
            result.put("code", "206");
            result.put("mess", buildMissingCustomerPartMessage(context, missingPartList));
            return result;
        }
        return success();
    }

    /**
     * 产品配置表审批申请附件上传
     **
     * @param context
     * @param args 请求参数
     * @return Map 上传结果，成功时返回Document ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/13 16:13
     */
    public Map uploadProductConfigApprovalAttachment(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        Object filesObj = params.get("files");
        if (!(filesObj instanceof List)) {
            return success();
        }
        List files = (List) filesObj;
        for (Object itemObj : files) {
            FileItem item = (FileItem) itemObj;
            if (item.isFormField() || item.getSize() <= 0) {
                continue;
            }
            String fileName = getUploadFileName(item.getName());
            if (UIUtil.isNullOrEmpty(fileName)) {
                continue;
            }
            try {
                Map result = success();
                result.put("documentId", createProductConfigApprovalDocument(context, item, fileName));
                return result;
            } catch (Exception e) {
                LOGGER.error("uploadProductConfigApprovalAttachment error", e);
                return fail(e.getMessage());
            }
        }
        return success();
    }

    /**
     * 产品配置审批申请创建后校验已选择内容并关联项目及审批内容
     **
     * @param context
     * @param args 请求参数
     * @throws Exception
     * @author caipan
     * @date 2026/7/20 16:13
     */
    @PostProcessCallable
    public void connectProductConfigApprovalAfterCreate(Context context, String[] args) throws Exception {
        Map parameter = JPO.unpackArgs(args);
        Map requestMap = (Map) parameter.get("requestMap");
        Map paramMap = (Map) parameter.get("paramMap");
        String routeId = paramMap == null ? "" : getFirstValue(paramMap.get("newObjectId"));
        String productConfigId = requestMap == null ? "" : getFirstValue(requestMap.get("JFApprovalProductConfigId"));
        String servicePartsListId = requestMap == null ? "" : getFirstValue(requestMap.get("JFApprovalServicePartsListId"));
        String attachmentDocId = requestMap == null ? "" : getFirstValue(requestMap.get("JFApprovalAttachmentDocId"));
        boolean forceSubmit = requestMap != null && "true".equalsIgnoreCase(getFirstValue(requestMap.get("JFApprovalForceSubmit")));
        if (UIUtil.isNullOrEmpty(routeId)) {
            throw new Exception("审批申请对象为空。");
        }
        //20260828 update by caipan 产品配置表改为非必填，仅校验并关联已选择的审批内容
        String duplicateMessage = UIUtil.isNullOrEmpty(productConfigId)
                ? "" : getProductConfigApprovalDuplicateMessage(context, productConfigId);
        if (UIUtil.isNotNullAndNotEmpty(duplicateMessage)) {
            throw new Exception(duplicateMessage);
        }
        duplicateMessage = UIUtil.isNullOrEmpty(servicePartsListId)
                ? "" : getServicePartsListApprovalDuplicateMessage(context, servicePartsListId);
        if (UIUtil.isNotNullAndNotEmpty(duplicateMessage)) {
            throw new Exception(duplicateMessage);
        }
        List missingPartList = getMissingCustomerPartInfoList(context, productConfigId, servicePartsListId);
        if (!forceSubmit && !missingPartList.isEmpty()) {
            throw new Exception(buildMissingCustomerPartMessage(context, missingPartList));
        }
        String selectedContentId = UIUtil.isNotNullAndNotEmpty(productConfigId)
                ? productConfigId : servicePartsListId;
        String projectId = getConfigTableRouteProjectId(context, requestMap, selectedContentId);
        if (UIUtil.isNullOrEmpty(projectId)) {
            throw new Exception(getFrameworkString(context, RESOURCE_ROUTE_EMPTY_PROJECT,
                    "The approval application is not associated with a project."));
        }
        ContextUtil.startTransaction(context, true);
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            DomainObject routeObj = DomainObject.newInstance(context, routeId);
            routeObj.addToObject(context, new RelationshipType(REL_CONFIG_ROUTE_TO_PROJECT), projectId);
            if (UIUtil.isNotNullAndNotEmpty(productConfigId)) {
                routeObj.addToObject(context, new RelationshipType(REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG), productConfigId);
            }
            if (UIUtil.isNotNullAndNotEmpty(servicePartsListId)) {
                routeObj.addToObject(context, new RelationshipType(REL_CONFIG_ROUTE_TO_SERVICE_PARTS_LIST), servicePartsListId);
            }
            if (UIUtil.isNotNullAndNotEmpty(attachmentDocId)) {
                routeObj.addToObject(context, new RelationshipType(RELATIONSHIP_REFERENCE_DOCUMENT), attachmentDocId);
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 产品配置审批申请工作中提升审核中审批内容非空及关系数量校验
     **
     * @param context
     * @param args 触发器参数
     * @return int 0表示校验通过，1表示阻止提升
     * @throws Exception
     * @author caipan
     * @date 2026/7/23 16:13
     */
    public int checkConfigTableRouteApprovalContentQuantity(Context context, String[] args) throws Exception {
        String routeApplyId = args != null && args.length > 0 ? args[0] : "";
        if (UIUtil.isNullOrEmpty(routeApplyId)) {
            routeApplyId = MqlUtil.mqlCommand(context, false, "get env OBJECTID", true);
        }
        if (UIUtil.isNullOrEmpty(routeApplyId)) {
            emxContextUtil_mxJPO.mqlNotice(context,
                    getFrameworkString(context, RESOURCE_ROUTE_EMPTY_OBJECT, RESOURCE_ROUTE_EMPTY_OBJECT));
            return 1;
        }
        DomainObject routeApplyObj = DomainObject.newInstance(context, routeApplyId);
        StringList productConfigRelationshipIds = routeApplyObj.getInfoList(context,
                "from[" + REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG + "].id");
        StringList servicePartsListRelationshipIds = routeApplyObj.getInfoList(context,
                "from[" + REL_CONFIG_ROUTE_TO_SERVICE_PARTS_LIST + "].id");
        //20260828 update by caipan 提升Review时产品配置表和售后件清单至少关联一个
        if (productConfigRelationshipIds.isEmpty() && servicePartsListRelationshipIds.isEmpty()) {
            emxContextUtil_mxJPO.mqlNotice(context, getFrameworkString(context,
                    RESOURCE_ROUTE_CONTENT_EMPTY,
                    "The approval application must be associated with at least one product configuration table or service parts list."));
            return 1;
        }
        //20260828 update by caipan 产品配置表允许不关联，但最多只能关联一个
        if (productConfigRelationshipIds.size() > 1) {
            emxContextUtil_mxJPO.mqlNotice(context, getFrameworkString(context,
                    RESOURCE_ROUTE_PRODUCT_CONFIG_QUANTITY,
                    "The approval application can be associated with at most one product configuration table."));
            return 1;
        }
        if (servicePartsListRelationshipIds.size() > 1) {
            emxContextUtil_mxJPO.mqlNotice(context, getFrameworkString(context,
                    RESOURCE_ROUTE_SERVICE_PARTS_LIST_QUANTITY,
                    "The approval application can be associated with at most one service parts list."));
            return 1;
        }
        return 0;
    }

    /**
     * 产品配置表审批申请工作中提升审核中数量校验；0级件合计仅计入首个选配件
     **
     * @param context
     * @param args 触发器参数
     * @return int 0表示校验通过，1表示阻止提升
     * @throws Exception
     * @author caipan
     * @date 2026/7/15 16:13
     */
    public int checkConfigTableRouteProductConfigQuantity(Context context, String[] args) throws Exception {
        String routeApplyId = args != null && args.length > 0 ? args[0] : "";
        if (UIUtil.isNullOrEmpty(routeApplyId)) {
            routeApplyId = MqlUtil.mqlCommand(context, false, "get env OBJECTID", true);
        }
        if (UIUtil.isNullOrEmpty(routeApplyId)) {
            emxContextUtil_mxJPO.mqlNotice(context, getFrameworkString(context, RESOURCE_ROUTE_EMPTY_OBJECT, RESOURCE_ROUTE_EMPTY_OBJECT));
            return 1;
        }
        DomainObject routeApplyObj = DomainObject.newInstance(context, routeApplyId);
        StringList productConfigRelationshipIds = routeApplyObj.getInfoList(context,
                "from[" + REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG + "].id");
        if (productConfigRelationshipIds.size() != 1) {
            return 0;
        }
        String productConfigId = routeApplyObj.getInfo(context,
                "from[" + REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG + "].to.id");

        MapList vehicleConfigList = getVehicleConfigList(context, productConfigId);
        MapList positionList = getPositionProductList(context, productConfigId, true);
        Map zeroPartTotalMap = new LinkedHashMap();
        Map zeroPartDisplayMap = new LinkedHashMap();
        List errorList = new ArrayList();
        for (Object positionObj : positionList) {
            Map positionMap = (Map) positionObj;
            String positionTitle = normalizeSelectValue(positionMap.get(SELECT_ATTR_TITLE));
            if (UIUtil.isNullOrEmpty(positionTitle)) {
                positionTitle = normalizeSelectValue(positionMap.get(SELECT_NAME));
            }
            MapList partList = (MapList) positionMap.get("parts");
            if (partList == null) {
                partList = new MapList();
            }
            for (Object partObj : partList) {
                Map partMap = (Map) partObj;
                if (!"0".equals(normalizeSelectValue(partMap.get("assemblyLevel")))) {
                    continue;
                }
                String partId = normalizeSelectValue(partMap.get(SELECT_ID));
                if (!zeroPartTotalMap.containsKey(partId)) {
                    zeroPartTotalMap.put(partId, BigDecimal.ZERO);
                    String partNumber = normalizeSelectValue(partMap.get(SELECT_ATTR_PART_NUMBER));
                    if (UIUtil.isNullOrEmpty(partNumber)) {
                        partNumber = normalizeSelectValue(partMap.get(SELECT_NAME));
                    }
                    String partName = normalizeSelectValue(partMap.get(SELECT_ATTR_PART_CN));
                    zeroPartDisplayMap.put(partId, UIUtil.isNullOrEmpty(partName) ? partNumber : partNumber + " " + partName);
                }
            }
            for (Object vehicleConfigObj : vehicleConfigList) {
                Map vehicleConfigMap = (Map) vehicleConfigObj;
                String vehicleConfigId = normalizeSelectValue(vehicleConfigMap.get(SELECT_ID));
                String vehicleConfigTitle = normalizeSelectValue(vehicleConfigMap.get(SELECT_ATTR_TITLE));
                if (UIUtil.isNullOrEmpty(vehicleConfigTitle)) {
                    vehicleConfigTitle = normalizeSelectValue(vehicleConfigMap.get(SELECT_NAME));
                }
                BigDecimal zeroPartQuantity = BigDecimal.ZERO;
                boolean hasMinusOnePart = false;
                boolean optionalZeroPartCounted = false;
                StringBuilder zeroPartDisplay = new StringBuilder();
                Set zeroPartIdSet = new HashSet();
                for (Object partObj : partList) {
                    Map partMap = (Map) partObj;
                    String partId = normalizeSelectValue(partMap.get(SELECT_ID));
                    Map matrixValueMap = (Map) partMap.get("matrix");
                    Map matrixValue = matrixValueMap == null ? null : (Map) matrixValueMap.get(vehicleConfigId);
                    BigDecimal quantity = matrixValue == null ? BigDecimal.ZERO : parseDecimal(matrixValue.get("quantity"));
                    String assemblyLevel = normalizeSelectValue(partMap.get("assemblyLevel"));
                    if ("0".equals(assemblyLevel)) {
                        String optional = matrixValue == null ? "" : normalizeSelectValue(matrixValue.get("optional"));
                        //20260831 update by caipan 0级件合计只累加按JFOrder排序后的首个选配件，其他选配件不参与阈值判断
                        if (!"O".equals(optional) || !optionalZeroPartCounted) {
                            zeroPartQuantity = zeroPartQuantity.add(quantity);
                            if ("O".equals(optional)) {
                                optionalZeroPartCounted = true;
                            }
                        }
                        zeroPartTotalMap.put(partId, ((BigDecimal) zeroPartTotalMap.get(partId)).add(quantity));
                        if (zeroPartIdSet.add(partId)) {
                            if (zeroPartDisplay.length() > 0) {
                                zeroPartDisplay.append(", ");
                            }
                            zeroPartDisplay.append(normalizeSelectValue(zeroPartDisplayMap.get(partId)));
                        }
                    } else if ("-1".equals(assemblyLevel) && quantity.compareTo(BigDecimal.ZERO) > 0) {
                        hasMinusOnePart = true;
                    }
                }
                if (zeroPartQuantity.compareTo(new BigDecimal("2")) >= 0 && !hasMinusOnePart) {
                    String message = getFrameworkString(context, RESOURCE_QUANTITY_CHECK_MINUS_ONE, RESOURCE_QUANTITY_CHECK_MINUS_ONE);
                    message = message.replace("{0}", positionTitle)
                            .replace("{1}", vehicleConfigTitle)
                            .replace("{2}", zeroPartDisplay.toString())
                            .replace("{3}", zeroPartQuantity.stripTrailingZeros().toPlainString());
                    errorList.add(message);
                }
            }
        }
        for (Object entryObj : zeroPartTotalMap.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            BigDecimal totalQuantity = (BigDecimal) entry.getValue();
            if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
                continue;
            }
            String message = getFrameworkString(context, RESOURCE_QUANTITY_CHECK_ZERO_TOTAL, RESOURCE_QUANTITY_CHECK_ZERO_TOTAL);
            message = message.replace("{0}", normalizeSelectValue(zeroPartDisplayMap.get(entry.getKey())))
                    .replace("{1}", totalQuantity.stripTrailingZeros().toPlainString());
            errorList.add(message);
        }
        if (!errorList.isEmpty()) {
            StringBuilder error = new StringBuilder(getFrameworkString(context, RESOURCE_QUANTITY_CHECK_TITLE, RESOURCE_QUANTITY_CHECK_TITLE));
            for (int i = 0; i < errorList.size(); i++) {
                error.append("\n").append(i + 1).append(". ").append(errorList.get(i));
            }
            emxContextUtil_mxJPO.mqlNotice(context, error.toString());
            return 1;
        }
        return 0;
    }

    /**
     * 产品配置审批申请Review状态提升校验项目角色、关联对象并创建审批流程
     **
     * @param context
     * @param args 触发器参数
     * @return int 0表示执行成功
     * @throws Exception
     * @author caipan
     * @date 2026/7/20 16:13
     */
    public int createConfigTableRouteReviewRoute(Context context, String[] args) throws Exception {
        String routeApplyId = args != null && args.length > 0 ? args[0] : "";
        if (UIUtil.isNullOrEmpty(routeApplyId)) {
            routeApplyId = MqlUtil.mqlCommand(context, false, "get env OBJECTID", true);
        }
        if (UIUtil.isNullOrEmpty(routeApplyId)) {
            emxContextUtil_mxJPO.mqlNotice(context, getFrameworkString(context, RESOURCE_ROUTE_EMPTY_OBJECT, RESOURCE_ROUTE_EMPTY_OBJECT));
            return 1;
        }
        DomainObject routeApplyObj = DomainObject.newInstance(context, routeApplyId);
        String productConfigId = routeApplyObj.getInfo(context, "from[" + REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG + "].to.id");
        String servicePartsListId = routeApplyObj.getInfo(context, "from[" + REL_CONFIG_ROUTE_TO_SERVICE_PARTS_LIST + "].to.id");
        //20260828 update by caipan 产品配置表非必填时从审批申请项目关系获取当前项目
        String projectId = routeApplyObj.getInfo(context, "from[" + REL_CONFIG_ROUTE_TO_PROJECT + "].to.id");
        if (UIUtil.isNullOrEmpty(projectId)) {
            emxContextUtil_mxJPO.mqlNotice(context, getFrameworkString(context, RESOURCE_ROUTE_EMPTY_PROJECT, RESOURCE_ROUTE_EMPTY_PROJECT));
            return 1;
        }
        StringList missingRoleNames = new StringList();
        String[] requiredRoles = new String[]{PROJECT_ROLE_CHAIR_MANAGER, PROJECT_ROLE_BUSINESS_MANAGER};
        for (String requiredRole : requiredRoles) {
            if (getProjectMemberIdsByRole(context, projectId, requiredRole).isEmpty()) {
                missingRoleNames.add(i18nNow.getRangeI18NString(ATTR_PROJECT_ROLE, requiredRole,
                        context.getLocale().getLanguage()));
            }
        }
        if (!missingRoleNames.isEmpty()) {
            String message = getFrameworkString(context, RESOURCE_ROUTE_MISSING_ROLE, RESOURCE_ROUTE_MISSING_ROLE);
            emxContextUtil_mxJPO.mqlNotice(context, message.replace("{0}", FrameworkUtil.join(missingRoleNames, ", ")));
            return 1;
        }
        MapList approverList = buildConfigTableRouteApprovers(context, projectId);
        String routeTitle = routeApplyObj.getInfo(context, SELECT_ATTR_TITLE);
        if (UIUtil.isNullOrEmpty(routeTitle)) {
            routeTitle = routeApplyObj.getInfo(context, SELECT_NAME);
        }
        promoteToReviewIfNeeded(context, productConfigId);
        promoteToReviewIfNeeded(context, servicePartsListId);
        JF_Route_mxJPO routeJpo = new JF_Route_mxJPO(context, args);
        LOGGER.info("routeApplyId:{} routeTitle:{}",routeApplyObj.getInfo(context,"current"),routeTitle);
        routeJpo.createAndStartRoute(context, approverList, routeApplyId, SYMBOLIC_STATE_REVIEW, SYMBOLIC_POLICY_CONFIG_TABLE_ROUTE, routeTitle);
        return 0;
    }

    /**
     * 产品配置表进入客户经理填写状态前创建客户经理填写任务
     **
     * @param context
     * @param args 产品配置表ID
     * @return int 0表示执行成功
     * @throws Exception
     * @author caipan
     * @date 2026/7/16 16:13
     */
    public int createProductConfigManagerTask(Context context, String[] args) throws Exception {
        String productConfigId = args != null && args.length > 0 ? args[0] : "";
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            productConfigId = MqlUtil.mqlCommand(context, false, "get env OBJECTID", true);
        }
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            throw new Exception(getFrameworkString(context, RESOURCE_ROUTE_EMPTY_PRODUCT_CONFIG, RESOURCE_ROUTE_EMPTY_PRODUCT_CONFIG));
        }
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        String projectId = getProjectIdByProductConfig(context, productConfigId);
        if (UIUtil.isNullOrEmpty(projectId)) {
            throw new Exception(getFrameworkString(context, RESOURCE_ROUTE_EMPTY_PROJECT, RESOURCE_ROUTE_EMPTY_PROJECT));
        }
        StringList personIds = getProjectMemberIdsByRole(context, projectId, PROJECT_ROLE_BUSINESS_MANAGER);
        if (personIds.isEmpty()) {
            String message = getFrameworkString(context, RESOURCE_ROUTE_MISSING_ROLE, RESOURCE_ROUTE_MISSING_ROLE);
            throw new Exception(message.replace("{0}", PROJECT_ROLE_BUSINESS_MANAGER));
        }
        String personId = normalizeSelectValue(personIds.get(0));
        String taskOwner = DomainObject.newInstance(context, personId).getInfo(context, SELECT_NAME);
        String taskTitle = getFrameworkString(context, RESOURCE_ROUTE_NODE_BUSINESS_MANAGER, RESOURCE_ROUTE_NODE_BUSINESS_MANAGER);
        Map taskMap = new HashMap();
        taskMap.put("taskName", taskTitle);
        taskMap.put("taskType", SYMBOLIC_TYPE_PRODUCT_CONFIG_TASK);
        taskMap.put("taskOwner", taskOwner);
        taskMap.put("parentId", productConfigId);
        String taskId = new JF_Util_mxJPO().createTask(context, JPO.packArgs(taskMap));
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            DomainObject taskObj = DomainObject.newInstance(context, taskId);
            DomainRelationship.connect(context, productConfigObj, REL_PRODUCT_CONFIG_TO_PRODUCT_CONFIG_TASK, taskObj);
            MqlUtil.mqlCommand(context, false, "mod bus " + taskId + " current Active", true);
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        return 0;
    }

    /**
     * 产品配置审批申请审核完成后发布关联对象和文档，产品配置表进入客户经理填写状态
     **
     * @param context
     * @param args 审批申请ID
     * @return int 0表示执行成功
     * @throws Exception
     * @author caipan
     * @date 2026/7/16 16:13
     */
    public int releaseConfigTableRouteRelatedObjects(Context context, String[] args) throws Exception {
        String routeApplyId = args != null && args.length > 0 ? args[0] : "";
        if (UIUtil.isNullOrEmpty(routeApplyId)) {
            routeApplyId = MqlUtil.mqlCommand(context, false, "get env OBJECTID", true);
        }
        if (UIUtil.isNullOrEmpty(routeApplyId)) {
            throw new Exception(getFrameworkString(context, RESOURCE_ROUTE_EMPTY_OBJECT, RESOURCE_ROUTE_EMPTY_OBJECT));
        }
        DomainObject routeApplyObj = DomainObject.newInstance(context, routeApplyId);
        String productConfigId = routeApplyObj.getInfo(context, "from[" + REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG + "].to.id");
        String servicePartsListId = routeApplyObj.getInfo(context, "from[" + REL_CONFIG_ROUTE_TO_SERVICE_PARTS_LIST + "].to.id");
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            //20260828 update by caipan 产品配置表非必填时仅处理实际关联的审批内容
            promoteToReleasedIfNeeded(context, productConfigId);
            promoteToReleasedIfNeeded(context, servicePartsListId);
            Set documentIds = new LinkedHashSet();
            StringList relatedObjectIds = new StringList();
            if (UIUtil.isNotNullAndNotEmpty(productConfigId)) {
                relatedObjectIds.add(productConfigId);
            }
            if (UIUtil.isNotNullAndNotEmpty(servicePartsListId)) {
                relatedObjectIds.add(servicePartsListId);
            }
            relatedObjectIds.add(routeApplyId);
            for (Object relatedObjectIdObj : relatedObjectIds) {
                String relatedObjectId = normalizeSelectValue(relatedObjectIdObj);
                StringList objectDocumentIds = DomainObject.newInstance(context, relatedObjectId).getInfoList(context,
                        "from[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].to.id");
                for (Object documentIdObj : objectDocumentIds) {
                    String documentId = normalizeSelectValue(documentIdObj);
                    if (UIUtil.isNotNullAndNotEmpty(documentId)) {
                        documentIds.add(documentId);
                    }
                }
            }
            for (Object documentIdObj : documentIds) {
                String documentId = normalizeSelectValue(documentIdObj);
                MqlUtil.mqlCommand(context, false, "mod bus " + documentId + " current RELEASED", true);
            }
            if (UIUtil.isNotNullAndNotEmpty(productConfigId)) {
                promoteToManagerFillsInIfNeeded(context, productConfigId);
            }
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        return 0;
    }

    /**
     * 产品配置表进入客户经理填写状态前启动项目供货件异步同步
     **
     * @param context
     * @param args 产品配置表ID
     * @return int 0表示异步任务提交完成
     * @throws Exception
     * @author caipan
     * @date 2026/7/16 16:13
     */
    public int startProjectSupplyPartSyncAsync(Context context, String[] args) throws Exception {
        String productConfigId = args != null && args.length > 0 ? args[0] : "";
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            productConfigId = MqlUtil.mqlCommand(context, false, "get env OBJECTID", true);
        }
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            throw new Exception(getFrameworkString(context, RESOURCE_ROUTE_EMPTY_PRODUCT_CONFIG, RESOURCE_ROUTE_EMPTY_PRODUCT_CONFIG));
        }
        JF_Util_mxJPO.runAsync(context,
                new String[]{productConfigId},
                "JF_ProductConfig",
                "syncProjectSupplyPartsByProductConfig");
        return 0;
    }

    /**
     * 按产品配置表供货件异步同步项目全部版本供货件
     **
     * @param context
     * @param args 产品配置表ID
     * @return void
     * @throws Exception
     * @author caipan
     * @date 2026/7/16 16:13
     */
    public void syncProjectSupplyPartsByProductConfig(Context context, String[] args) throws Exception {
        String productConfigId = args != null && args.length > 0 ? args[0] : "";
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            throw new Exception(getFrameworkString(context, RESOURCE_ROUTE_EMPTY_PRODUCT_CONFIG, RESOURCE_ROUTE_EMPTY_PRODUCT_CONFIG));
        }
        String projectId = getProjectIdByProductConfig(context, productConfigId);
        if (UIUtil.isNullOrEmpty(projectId)) {
            throw new Exception(getFrameworkString(context, RESOURCE_ROUTE_EMPTY_PROJECT, RESOURCE_ROUTE_EMPTY_PROJECT));
        }

        ContextUtil.startTransaction(context, true);
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            Map<String, String> desiredPartIdsByLogicalId = new LinkedHashMap<>();
            MapList desiredPartList = getProductConfigSupplyPartList(context, productConfigId);
            for (Object desiredPartObj : desiredPartList) {
                Map desiredPartMap = (Map) desiredPartObj;
                String partId = UIUtil.getValue(desiredPartMap, SELECT_ID);
                String logicalId = UIUtil.getValue(desiredPartMap, SELECT_LOGICAL_ID);
                if (UIUtil.isNullOrEmpty(logicalId)) {
                    throw new Exception("Product config supply part logicalid is empty: " + partId);
                }
                if (UIUtil.isNotNullAndNotEmpty(partId) && !desiredPartIdsByLogicalId.containsKey(logicalId)) {
                    desiredPartIdsByLogicalId.put(logicalId, partId);
                }
            }

            StringList objectSelects = new StringList(SELECT_ID);
            objectSelects.add(SELECT_LOGICAL_ID);
            StringList relSelects = new StringList(DomainRelationship.SELECT_ID);
            relSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JFZeroPart);
            relSelects.add(JF_PLMConstants_mxJPO.SELECT_ATTR_JF_BelongPart);
            MapList projectPartList = projectObj.getRelatedObjects(context,
                    JF_PLMConstants_mxJPO.rel_JFProject2RootPart,
                    TYPE_VPM_REFERENCE,
                    objectSelects,
                    relSelects,
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    (short) 0);
            Map<String, Map> projectRelByPartId = new HashMap<>();
            Map<String, String> projectSupplyPartIdsByLogicalId = new LinkedHashMap<>();
            for (Object projectPartObj : projectPartList) {
                Map projectPartMap = (Map) projectPartObj;
                String partId = UIUtil.getValue(projectPartMap, SELECT_ID);
                if (UIUtil.isNotNullAndNotEmpty(partId)) {
                    projectRelByPartId.put(partId, projectPartMap);
                }
                if (!"Y".equalsIgnoreCase(UIUtil.getValue(projectPartMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFZeroPart))) {
                    continue;
                }
                String logicalId = UIUtil.getValue(projectPartMap, SELECT_LOGICAL_ID);
                if (UIUtil.isNullOrEmpty(logicalId)) {
                    throw new Exception("Project supply part logicalid is empty: " + partId);
                }
                if (!projectSupplyPartIdsByLogicalId.containsKey(logicalId)) {
                    projectSupplyPartIdsByLogicalId.put(logicalId, partId);
                }
            }

            int createdCount = 0;
            int enabledCount = 0;
            int disconnectedCount = 0;
            int disabledCount = 0;
            for (Map.Entry<String, String> desiredEntry : desiredPartIdsByLogicalId.entrySet()) {
                StringList revisionIds = getMajorRevisionIds(context, desiredEntry.getValue());
                for (Object revisionIdObj : revisionIds) {
                    String revisionId = normalizeSelectValue(revisionIdObj);
                    if (UIUtil.isNullOrEmpty(revisionId)) {
                        continue;
                    }
                    DomainObject revisionObj = DomainObject.newInstance(context, revisionId);
                    Map relMap = projectRelByPartId.get(revisionId);
                    if (relMap == null) {
                        DomainRelationship rel = projectObj.addToObject(context,
                                new RelationshipType(JF_PLMConstants_mxJPO.rel_JFProject2RootPart),
                                revisionId);
                        Map relAttributeMap = new HashMap();
                        relAttributeMap.put(JF_PLMConstants_mxJPO.ATTR_JFZeroPart, "Y");
                        relAttributeMap.put(JF_PLMConstants_mxJPO.ATTR_JF_BelongPart, "N");
                        rel.setAttributeValues(context, relAttributeMap);
                        createdCount++;
                    } else {
                        String relId = UIUtil.getValue(relMap, DomainRelationship.SELECT_ID);
                        if (UIUtil.isNotNullAndNotEmpty(relId)
                                && !"Y".equalsIgnoreCase(UIUtil.getValue(relMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFZeroPart))) {
                            DomainRelationship.newInstance(context, relId).setAttributeValue(context,
                                    JF_PLMConstants_mxJPO.ATTR_JFZeroPart,
                                    "Y");
                            enabledCount++;
                        }
                    }
                    revisionObj.setAttributeValue(context, JF_PLMConstants_mxJPO.attr_JF_ISWholeChair, "WholeChair");
                }
            }

            for (Map.Entry<String, String> projectEntry : projectSupplyPartIdsByLogicalId.entrySet()) {
                if (desiredPartIdsByLogicalId.containsKey(projectEntry.getKey())) {
                    continue;
                }
                StringList revisionIds = getMajorRevisionIds(context, projectEntry.getValue());
                for (Object revisionIdObj : revisionIds) {
                    String revisionId = normalizeSelectValue(revisionIdObj);
                    if (UIUtil.isNullOrEmpty(revisionId)) {
                        continue;
                    }
                    DomainObject revisionObj = DomainObject.newInstance(context, revisionId);
                    Map relMap = projectRelByPartId.get(revisionId);
                    if (relMap != null
                            && "Y".equalsIgnoreCase(UIUtil.getValue(relMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JFZeroPart))) {
                        String relId = UIUtil.getValue(relMap, DomainRelationship.SELECT_ID);
                        if (UIUtil.isNotNullAndNotEmpty(relId)) {
                            if ("N".equalsIgnoreCase(UIUtil.getValue(relMap, JF_PLMConstants_mxJPO.SELECT_ATTR_JF_BelongPart))) {
                                DomainRelationship.disconnect(context, relId);
                                disconnectedCount++;
                            } else {
                                DomainRelationship.newInstance(context, relId).setAttributeValue(context,
                                        JF_PLMConstants_mxJPO.ATTR_JFZeroPart,
                                        "N");
                                disabledCount++;
                            }
                        }
                    }
                    revisionObj.setAttributeValue(context, JF_PLMConstants_mxJPO.attr_JF_ISWholeChair, "");
                }
            }
            projectObj.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_SupplyAffirm, "N");
            ContextUtil.commitTransaction(context);
            LOGGER.info("Project supply part async sync completed. productConfigId:{} projectId:{} desiredLogicalCount:{} created:{} enabled:{} disconnected:{} disabled:{}",
                    productConfigId,
                    projectId,
                    desiredPartIdsByLogicalId.size(),
                    createdCount,
                    enabledCount,
                    disconnectedCount,
                    disabledCount);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("Project supply part async sync failed. productConfigId:{} projectId:{}",
                    productConfigId,
                    projectId,
                    e);
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 将审核中的产品配置关联对象提升到发布状态
     **
     * @param context
     * @param objectId 对象ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/16 16:13
     */
    private void promoteToReleasedIfNeeded(Context context, String objectId) throws Exception {
        if (UIUtil.isNullOrEmpty(objectId)) {
            return;
        }
        DomainObject obj = DomainObject.newInstance(context, objectId);
        String current = obj.getInfo(context, SELECT_CURRENT);
        if (STATE_RELEASED.equals(current)) {
            return;
        }
        if (!STATE_REVIEW.equals(current)) {
            String message = getFrameworkString(context, RESOURCE_ROUTE_INVALID_STATE, RESOURCE_ROUTE_INVALID_STATE);
            message = message.replace("{0}", obj.getInfo(context, SELECT_NAME)).replace("{1}", current);
            throw new Exception(message);
        }
        obj.promote(context);
    }

    /**
     * 将发布状态的产品配置表提升到客户经理填写状态
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/16 16:13
     */
    private void promoteToManagerFillsInIfNeeded(Context context, String productConfigId) throws Exception {
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        String current = productConfigObj.getInfo(context, SELECT_CURRENT);
        if (STATE_MANAGER_FILLS_IN.equals(current)) {
            return;
        }
        if (!STATE_RELEASED.equals(current)) {
            String message = getFrameworkString(context, RESOURCE_ROUTE_INVALID_STATE, RESOURCE_ROUTE_INVALID_STATE);
            message = message.replace("{0}", productConfigObj.getInfo(context, SELECT_NAME)).replace("{1}", current);
            throw new Exception(message);
        }
        productConfigObj.promote(context);
    }

    /**
     * 对象状态提升到审核中
     **
     * @param context
     * @param objectId 对象ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/14 16:13
     */
    private void promoteToReviewIfNeeded(Context context, String objectId) throws Exception {
        if (UIUtil.isNullOrEmpty(objectId)) {
            return;
        }
        DomainObject obj = DomainObject.newInstance(context, objectId);
        String current = obj.getInfo(context, SELECT_CURRENT);
        if (STATE_REVIEW.equals(current)) {
            return;
        }
        if (!STATE_IN_WORK.equals(current)) {
            String message = getFrameworkString(context, RESOURCE_ROUTE_INVALID_STATE, RESOURCE_ROUTE_INVALID_STATE);
            message = message.replace("{0}", obj.getInfo(context, SELECT_NAME)).replace("{1}", current);
            throw new Exception(message);
        }
        try {
            ContextUtil.pushContext(context);
            obj.promote(context);
        }finally {
            ContextUtil.popContext(context);
        }
    }

    /**
     * 获取产品配置表关联项目
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return String 项目ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/14 16:13
     */
    private String getProjectIdByProductConfig(Context context, String productConfigId) throws Exception {
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        StringList projectIds = productConfigObj.getInfoList(context, "to[" + REL_PROJECT_TO_PRODUCT_CONFIG + "].from.id");
        if (projectIds == null || projectIds.isEmpty()) {
            return "";
        }
        if (projectIds.size() > 1) {
            LOGGER.warn("Product config {} connected to multiple projects: {}", productConfigId, projectIds);
        }
        return normalizeSelectValue(projectIds.get(0));
    }

    /**
     * 构造产品配置表审批流程节点
     **
     * @param context
     * @param projectId 项目ID
     * @return MapList 流程节点
     * @throws Exception
     * @author caipan
     * @date 2026/7/14 16:13
     */
    private MapList buildConfigTableRouteApprovers(Context context, String projectId) throws Exception {
        MapList approverList = new MapList();
        String chairTitle = getFrameworkString(context, RESOURCE_ROUTE_NODE_CHAIR_MANAGER, RESOURCE_ROUTE_NODE_CHAIR_MANAGER);
        String businessTitle = getFrameworkString(context, RESOURCE_ROUTE_NODE_BUSINESS_MANAGER, RESOURCE_ROUTE_NODE_BUSINESS_MANAGER);
        addProjectRoleApprovers(context, projectId, PROJECT_ROLE_CHAIR_MANAGER, chairTitle, "1", approverList);
        //addProjectRoleApprovers(context, projectId, PROJECT_ROLE_BUSINESS_MANAGER, businessTitle, "2", approverList);
        return approverList;
    }

    /**
     * 添加项目角色审批人
     **
     * @param context
     * @param projectId 项目ID
     * @param projectRole 项目角色
     * @param title 节点标题
     * @param sequence 节点序号
     * @param approverList 流程节点
     * @throws Exception
     * @author caipan
     * @date 2026/7/14 16:13
     */
    private void addProjectRoleApprovers(Context context, String projectId, String projectRole, String title, String sequence, MapList approverList) throws Exception {
        StringList personIds = getProjectMemberIdsByRole(context, projectId, projectRole);
        if (personIds.isEmpty()) {
            return;
        }
        for (Object personIdObj : personIds) {
            String personId = normalizeSelectValue(personIdObj);
            if (UIUtil.isNotNullAndNotEmpty(personId)) {
                approverList.add(JF_PublicMethodClass_mxJPO.getMapAnyOrAll(personId, title, "true", sequence, "All"));
            }
        }
    }

    /**
     * 获取项目角色成员
     **
     * @param context
     * @param projectId 项目ID
     * @param projectRole 项目角色
     * @return StringList 人员ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/14 16:13
     */
    private StringList getProjectMemberIdsByRole(Context context, String projectId, String projectRole) throws Exception {
        StringList personIds = new StringList();
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            StringList objectSelects = new StringList(SELECT_ID);
            StringList relSelects = new StringList("attribute[" + ATTR_PROJECT_ROLE + "]");
            MapList memberList = projectObj.getRelatedObjects(context,
                    DomainRelationship.RELATIONSHIP_MEMBER,
                    DomainConstants.TYPE_PERSON,
                    objectSelects,
                    relSelects,
                    false,
                    true,
                    (short) 1,
                    "",
                    "attribute[" + ATTR_PROJECT_ROLE + "]=='" + projectRole + "'",
                    (short) 0);
            Set idSet = new HashSet();
            for (Object memberObj : memberList) {
                Map memberMap = (Map) memberObj;
                String personId = UIUtil.getValue(memberMap, SELECT_ID);
                if (UIUtil.isNotNullAndNotEmpty(personId) && idSet.add(personId)) {
                    personIds.add(personId);
                }
            }
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
        return personIds;
    }

    /**
     * 校验产品配置审批申请已关联内容的零件客户件信息
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @param servicePartsListId 售后件清单ID
     * @return List 缺失客户件信息的零件显示值
     * @throws Exception
     * @author caipan
     * @date 2026/7/13 16:13
     */
    private List getMissingCustomerPartInfoList(Context context, String productConfigId, String servicePartsListId) throws Exception {
        MapList partProjectList = new MapList();
        //20260828 update by caipan 产品配置表非必填时仅校验实际选择的产品配置表零件
        if (UIUtil.isNotNullAndNotEmpty(productConfigId)) {
            String productProjectId = getProjectIdByProductConfig(context, productConfigId);
            StringList productPartIds = getLatestDirectPartIds(context, productConfigId, REL_PRODUCT_CONFIG_TO_VPM);
            for (Object partIdObj : productPartIds) {
                Map partProjectMap = new HashMap();
                partProjectMap.put(SELECT_ID, normalizeSelectValue(partIdObj));
                partProjectMap.put("projectId", productProjectId);
                partProjectList.add(partProjectMap);
            }
        }
        if (UIUtil.isNotNullAndNotEmpty(servicePartsListId)) {
            String serviceProjectId = DomainObject.newInstance(context, servicePartsListId)
                    .getInfo(context, "to[" + REL_PROJECT_TO_SERVICE_PARTS_LIST + "].from.id");
            StringList servicePartIds = getLatestDirectPartIds(context, servicePartsListId, REL_SERVICE_PARTS_LIST_TO_VPM);
            for (Object partIdObj : servicePartIds) {
                Map partProjectMap = new HashMap();
                partProjectMap.put(SELECT_ID, normalizeSelectValue(partIdObj));
                partProjectMap.put("projectId", serviceProjectId);
                partProjectList.add(partProjectMap);
            }
        }
        Set checkedPartProjects = new HashSet();
        List missingPartList = new ArrayList();
        JF_DR_mxJPO dr = new JF_DR_mxJPO();
        for (Object partProjectObj : partProjectList) {
            Map partProjectMap = (Map) partProjectObj;
            String partId = normalizeSelectValue(partProjectMap.get(SELECT_ID));
            String projectId = normalizeSelectValue(partProjectMap.get("projectId"));
            if (UIUtil.isNullOrEmpty(partId) || !checkedPartProjects.add(projectId + "|" + partId)) {
                continue;
            }
            if (isCustomerPartInfoMissing(context, dr, partId, projectId)) {
                missingPartList.add(getPartDisplayValue(context, partId));
            }
        }
        return missingPartList;
    }

    /**
     * 获取对象直接关联零件的最新版ID
     **
     * @param context
     * @param objectId 对象ID
     * @param relationship 关联关系
     * @return StringList 最新版零件ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/13 16:13
     */
    private StringList getLatestDirectPartIds(Context context, String objectId, String relationship) throws Exception {
        StringList result = new StringList();
        if (UIUtil.isNullOrEmpty(objectId)) {
            return result;
        }
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        StringList partIds = domainObject.getInfoList(context, "from[" + relationship + "].to.id");
        StringList validPartIds = new StringList();
        Set sourcePartIds = new LinkedHashSet();
        for (Object partIdObj : partIds) {
            String partId = normalizeSelectValue(partIdObj);
            if (UIUtil.isNotNullAndNotEmpty(partId) && sourcePartIds.add(partId)) {
                validPartIds.add(partId);
            }
        }
        Map latestPartIdMap = getLatestPartIdMap(context, validPartIds);
        Set latestIds = new HashSet();
        for (Object partIdObj : validPartIds) {
            String partId = normalizeSelectValue(partIdObj);
            String latestPartId = normalizeSelectValue(latestPartIdMap.get(partId));
            if (UIUtil.isNullOrEmpty(latestPartId)) {
                latestPartId = partId;
            }
            if (UIUtil.isNotNullAndNotEmpty(latestPartId) && latestIds.add(latestPartId)) {
                result.add(latestPartId);
            }
        }
        return result;
    }

    /**
     * 判断零件客户零件号或客户零件名称是否缺失
     **
     * @param context
     * @param dr DR业务对象
     * @param partId 最新版零件ID
     * @param projectId 当前对象所属项目ID
     * @return boolean 缺失时返回true
     * @throws Exception
     * @author caipan
     * @date 2026/7/13 16:13
     */
    private boolean isCustomerPartInfoMissing(Context context, JF_DR_mxJPO dr, String partId, String projectId) throws Exception {
        Map customerPartInfo = dr.getDRCustomerPartsDBRelationInfo(context, partId, projectId);
        String customerPartNumber = normalizeSelectValue(customerPartInfo.get(SELECT_REL_ATTR_CUSTOMER_PART_NUMBER));
        String customerPartName = normalizeSelectValue(customerPartInfo.get(SELECT_REL_ATTR_CUSTOMER_PART_NAME));
        return UIUtil.isNullOrEmpty(customerPartNumber) || UIUtil.isNullOrEmpty(customerPartName);
    }

    /**
     * 获取零件提示显示值
     **
     * @param context
     * @param partId 零件ID
     * @return String 零件显示值
     * @throws Exception
     * @author caipan
     * @date 2026/7/13 16:13
     */
    private String getPartDisplayValue(Context context, String partId) throws Exception {
        DomainObject partObj = DomainObject.newInstance(context, partId);
        StringList selects = new StringList();
        selects.add(SELECT_ATTR_PART_NUMBER);
        selects.add(SELECT_ATTR_PART_CN);
        selects.add(SELECT_NAME);
        Map info = partObj.getInfo(context, selects);
        String partNumber = normalizeSelectValue(info.get(SELECT_ATTR_PART_NUMBER));
        if (UIUtil.isNullOrEmpty(partNumber)) {
            partNumber = normalizeSelectValue(info.get(SELECT_NAME));
        }
        String partName = normalizeSelectValue(info.get(SELECT_ATTR_PART_CN));
        return UIUtil.isNullOrEmpty(partName) ? partNumber : partNumber + " " + partName;
    }

    /**
     * 构造客户件信息缺失提示
     **
     * @param context
     * @param missingPartList 缺失客户件信息的零件
     * @return String 提示信息
     * @author caipan
     * @date 2026/7/13 16:13
     */
    private String buildMissingCustomerPartMessage(Context context, List missingPartList) {
        StringBuilder sb = new StringBuilder();
        String message = RESOURCE_MISSING_CUSTOMER_PART_MESSAGE;
        try {
            message = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), RESOURCE_MISSING_CUSTOMER_PART_MESSAGE);
        } catch (Exception e) {
            LOGGER.warn("Get product config approval i18n message failed.", e);
        }
        sb.append(message);
        int limit = Math.min(missingPartList.size(), 20);
        for (int i = 0; i < limit; i++) {
            sb.append("\n").append(i + 1).append(". ").append(normalizeSelectValue(missingPartList.get(i)));
        }
        if (missingPartList.size() > limit) {
            String totalMessage = RESOURCE_MISSING_CUSTOMER_PART_TOTAL + " {0}";
            try {
                totalMessage = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), RESOURCE_MISSING_CUSTOMER_PART_TOTAL);
            } catch (Exception e) {
                LOGGER.warn("Get product config approval total i18n message failed.", e);
            }
            sb.append("\n").append(totalMessage.replace("{0}", String.valueOf(missingPartList.size())));
        }
        return sb.toString();
    }

    /**
     * 创建产品配置表审批申请附件文档
     **
     * @param context
     * @param routeObj 审批申请对象
     * @param params 请求参数
     * @throws Exception
     * @author caipan
     * @date 2026/7/13 16:13
     */
    private void createProductConfigApprovalDocuments(Context context, DomainObject routeObj, Map params) throws Exception {
        Object filesObj = params.get("files");
        if (!(filesObj instanceof List)) {
            return;
        }
        List files = (List) filesObj;
        for (Object itemObj : files) {
            FileItem item = (FileItem) itemObj;
            if (item.isFormField() || item.getSize() <= 0) {
                continue;
            }
            String fileName = getUploadFileName(item.getName());
            if (UIUtil.isNullOrEmpty(fileName)) {
                continue;
            }
            createAndConnectDocument(context, routeObj, item, fileName);
        }
    }

    /**
     * 创建并关联文档对象
     **
     * @param context
     * @param routeObj 审批申请对象
     * @param item 上传文件
     * @param fileName 文件名
     * @throws Exception
     * @author caipan
     * @date 2026/7/13 16:13
     */
    private void createAndConnectDocument(Context context, DomainObject routeObj, FileItem item, String fileName) throws Exception {
        String documentId = createProductConfigApprovalDocument(context, item, fileName);
        routeObj.addToObject(context, new RelationshipType(RELATIONSHIP_REFERENCE_DOCUMENT), documentId);
    }

    /**
     * 创建产品配置表审批申请附件文档
     **
     * @param context
     * @param item 上传文件
     * @param fileName 文件名
     * @return String 文档ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/13 16:13
     */
    private String createProductConfigApprovalDocument(Context context, FileItem item, String fileName) throws Exception {
        String tempDir = System.getProperty("java.io.tmpdir");
        File uploadDir = new File(tempDir, "JFConfigTableRoute_" + System.currentTimeMillis());
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        File uploadFile = new File(uploadDir, fileName);
        try {
            item.write(uploadFile);
            String generator = UICache.getObjectGenerator(context, "type_Document", "");
            String docName = DomainObject.getAutoGeneratedName(context, generator, "");
            String docPolicy = PropertyUtil.getSchemaProperty(EnoviaResourceBundle.getProperty(context, "emxFrameowrk.FileUpload.Default.Policy"));
            if (UIUtil.isNullOrEmpty(docPolicy)) {
                docPolicy = DomainConstants.POLICY_DOCUMENT;
            }
            Policy policy = new Policy(docPolicy);
            String revision = policy.getFirstInSequence(context);
            CommonDocument document = new CommonDocument();
            document.createObject(context, DomainObject.TYPE_DOCUMENT, docName, revision, docPolicy, context.getVault().getName());
            document.setAttributeValue(context, ATTR_TITLE, fileName);
            String store = DocumentUtil.getStoreFromBL(context, DomainObject.TYPE_DOCUMENT);
            document.checkinFile(context, true, true, "", "generic", store, fileName, uploadDir.getAbsolutePath());
            return document.getId(context);
        } finally {
            if (uploadFile.exists()) {
                uploadFile.delete();
            }
            if (uploadDir.exists()) {
                uploadDir.delete();
            }
        }
    }

    private String getUploadFileName(String filePath) {
        if (UIUtil.isNullOrEmpty(filePath)) {
            return "";
        }
        String fileName = filePath;
        int slashIndex = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if (slashIndex >= 0) {
            fileName = fileName.substring(slashIndex + 1);
        }
        return fileName;
    }

    /**
     * 构造产品配置审批申请搜索字段
     **
     * @param actualName 实际值字段名
     * @param policy 状态过滤条件
     * @param displayName 显示值字段名
     * @param actualValue 实际值
     * @param displayValue 显示值
     * @param typeName 搜索类型注册名
     * @param projectRelationship 项目到搜索对象的关系名
     * @param projectId 当前项目ID
     * @param required 是否必填
     * @return String 搜索字段HTML
     * @author caipan
     * @date 2026/7/16 16:13
     */
    private String buildConfigTableRouteChooserHtml(String actualName, String policy, String displayName,
                                                     String actualValue, String displayValue, String typeName,
                                                     String projectRelationship, String projectId, boolean required) {
        StringBuilder sb = new StringBuilder();
        String searchUrl = "../common/emxFullSearch.jsp?field=TYPES=" + typeName+":"+policy
                + "&table=AEFGeneralSearchResults&showInitialResults=true&selection=single&submitAction=refreshCaller&uiType=createForm"
                + "&includeOIDprogram=JF_ProductConfig:getConfigTableRouteSearchObjectIds"
                + "&projectId=" + projectId
                + "&projectRelationship=" + projectRelationship
                + "&fieldNameActual=" + actualName
                + "&fieldNameDisplay=" + displayName
                + "&submitURL=../common/JF_AEFSearchUtil.jsp";
        sb.append("<input type=\"hidden\" id=\"").append(actualName).append("\" name=\"").append(actualName).append("\" value=\"").append(escapeHtml(actualValue)).append("\"/>");
        sb.append("<input type=\"text\" id=\"").append(displayName).append("\" name=\"").append(displayName).append("\" value=\"").append(escapeHtml(displayValue)).append("\" readonly=\"readonly\" style=\"width:260px;\"");
        if (required) {
            sb.append(" data-jf-required=\"true\"");
        }
        sb.append("/>");
        sb.append("<input type=\"button\" value=\"...\" onclick=\"showModalDialog('").append(escapeHtml(searchUrl)).append("',850,630,true,'Large');\"/>");
        sb.append("<input type=\"button\" value=\"clear\" onclick=\"document.getElementById('").append(actualName).append("').value='';document.getElementById('").append(displayName).append("').value='';\"/>");
        return sb.toString();
    }

    /**
     * 获取产品配置审批申请搜索范围
     **
     * @param context
     * @param args 搜索参数，包含projectId和projectRelationship
     * @return StringList 当前项目关联的产品配置表或售后件清单ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/16 16:13
     */
    public StringList getConfigTableRouteSearchObjectIds(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String projectId = getFirstValue(params.get("projectId"));
        String relationship = getFirstValue(params.get("projectRelationship"));
        StringList objectIds = new StringList();
        if (UIUtil.isNullOrEmpty(projectId)) {
            projectId = getConfigTableRouteProjectId(context, params, "");
        }
        if (UIUtil.isNullOrEmpty(projectId)
                || !(REL_PROJECT_TO_PRODUCT_CONFIG.equals(relationship)
                || REL_PROJECT_TO_SERVICE_PARTS_LIST.equals(relationship))) {
            return objectIds;
        }
        return DomainObject.newInstance(context, projectId).getInfoList(context, "from[" + relationship + "].to.id");
    }

    /**
     * 获取产品配置审批申请当前项目
     **
     * @param context
     * @param requestMap 表单请求参数
     * @param selectedObjectId 已选产品配置表或售后件清单ID
     * @return String 当前项目ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/16 16:13
     */
    private String getConfigTableRouteProjectId(Context context, Map requestMap, String selectedObjectId) throws Exception {
        if (UIUtil.isNotNullAndNotEmpty(selectedObjectId)) {
            DomainObject selectedObject = DomainObject.newInstance(context, selectedObjectId);
            String selectedObjectType = selectedObject.getInfo(context, SELECT_TYPE);
            //20260828 update by caipan 支持通过产品配置表或售后件清单解析审批申请所属项目
            if (DomainConstants.TYPE_PROJECT_SPACE.equals(selectedObjectType)) {
                return selectedObjectId;
            }
            if (TYPE_PRODUCT_CONFIG_TABLE.equals(selectedObjectType)) {
                return getProjectIdByProductConfig(context, selectedObjectId);
            }
            if (TYPE_SERVICE_PARTS_LIST.equals(selectedObjectType)) {
                return selectedObject.getInfo(context, "to[" + REL_PROJECT_TO_SERVICE_PARTS_LIST + "].from.id");
            }
        }
        if (requestMap == null) {
            return "";
        }
        String contextObjectId = getFirstValue(requestMap.get("parentOID"));
        if (UIUtil.isNullOrEmpty(contextObjectId)) {
            contextObjectId = getFirstValue(requestMap.get("objectId"));
        }
        //20260828 update by caipan 工具栏入口清空审批内容后通过原勾选行保留当前项目上下文
        if (UIUtil.isNullOrEmpty(contextObjectId)) {
            contextObjectId = getObjectIdFromTableRowId(getFirstValue(requestMap.get("emxTableRowId")));
        }
        if (UIUtil.isNullOrEmpty(contextObjectId)) {
            return "";
        }
        DomainObject contextObj = DomainObject.newInstance(context, contextObjectId);
        String objectType = contextObj.getInfo(context, SELECT_TYPE);
        if (DomainConstants.TYPE_PROJECT_SPACE.equals(objectType)) {
            return contextObjectId;
        }
        if (TYPE_PRODUCT_CONFIG_TABLE.equals(objectType)) {
            return getProjectIdByProductConfig(context, contextObjectId);
        }
        if (TYPE_SERVICE_PARTS_LIST.equals(objectType)) {
            return contextObj.getInfo(context, "to[" + REL_PROJECT_TO_SERVICE_PARTS_LIST + "].from.id");
        }
        if (TYPE_CONFIG_TABLE_ROUTE.equals(objectType)) {
            String routeProjectId = contextObj.getInfo(context,
                    "from[" + REL_CONFIG_ROUTE_TO_PROJECT + "].to.id");
            if (UIUtil.isNotNullAndNotEmpty(routeProjectId)) {
                return routeProjectId;
            }
            String routeProductConfigId = contextObj.getInfo(context, "from[" + REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG + "].to.id");
            if (UIUtil.isNotNullAndNotEmpty(routeProductConfigId)) {
                return getProjectIdByProductConfig(context, routeProductConfigId);
            }
            String routeServicePartsListId = contextObj.getInfo(context,
                    "from[" + REL_CONFIG_ROUTE_TO_SERVICE_PARTS_LIST + "].to.id");
            return UIUtil.isNullOrEmpty(routeServicePartsListId) ? ""
                    : DomainObject.newInstance(context, routeServicePartsListId).getInfo(context,
                    "to[" + REL_PROJECT_TO_SERVICE_PARTS_LIST + "].from.id");
        }
        return "";
    }

    private String getObjectDisplayValue(Context context, String objectId) throws Exception {
        if (UIUtil.isNullOrEmpty(objectId)) {
            return "";
        }
        DomainObject obj = DomainObject.newInstance(context, objectId);
        String displayValue = obj.getInfo(context, SELECT_ATTR_TITLE);
        if (UIUtil.isNullOrEmpty(displayValue)) {
            displayValue = obj.getInfo(context, SELECT_NAME);
        }
        return displayValue;
    }

    private String getObjectIdFromTableRowId(String tableRowId) throws Exception {
        if (UIUtil.isNullOrEmpty(tableRowId)) {
            return "";
        }
        String[] objectIds = ComponentsUIUtil.getSplitTableRowIds(new String[]{tableRowId});
        return objectIds != null && objectIds.length > 0 ? objectIds[0] : "";
    }

    private StringList toStringList(Object value) {
        StringList list = new StringList();
        if (value instanceof StringList) {
            list.addAll((StringList) value);
        } else if (value instanceof String[]) {
            for (String item : (String[]) value) {
                if (UIUtil.isNotNullAndNotEmpty(item)) {
                    list.add(item);
                }
            }
        } else if (value instanceof Collection) {
            for (Object itemObj : (Collection) value) {
                String item = String.valueOf(itemObj);
                if (UIUtil.isNotNullAndNotEmpty(item)) {
                    list.add(item);
                }
            }
        } else if (value instanceof String) {
            String text = (String) value;
            if (UIUtil.isNotNullAndNotEmpty(text)) {
                String[] items = text.split(",");
                for (String item : items) {
                    if (UIUtil.isNotNullAndNotEmpty(item)) {
                        list.add(item.trim());
                    }
                }
            }
        }
        return list;
    }

    /**
     * 获取产品配置表重复提交审批申请的提示信息
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return String 已存在申请单关系时返回提示信息，否则返回空字符串
     * @throws Exception
     * @author caipan
     * @date 2026/7/20 16:13
     */
    private String getProductConfigApprovalDuplicateMessage(Context context, String productConfigId) throws Exception {
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        StringList routeIds = productConfigObj.getInfoList(context, "to[" + REL_CONFIG_ROUTE_TO_PRODUCT_CONFIG + "].from.id");
        if (routeIds != null && !routeIds.isEmpty()) {
            return getFrameworkString(context, RESOURCE_DUPLICATE_PRODUCT_CONFIG_APPROVAL,
                    "The current object already has a product configuration application and does not need to be submitted again.");
        }
        return "";
    }

    /**
     * 获取售后件清单重复提交审批申请的提示信息
     **
     * @param context
     * @param servicePartsListId 售后件清单ID
     * @return String 已存在申请单关系时返回提示信息，否则返回空字符串
     * @throws Exception
     * @author caipan
     * @date 2026/7/20 16:13
     */
    private String getServicePartsListApprovalDuplicateMessage(Context context, String servicePartsListId) throws Exception {
        if (UIUtil.isNullOrEmpty(servicePartsListId)) {
            return "";
        }
        DomainObject servicePartsListObj = DomainObject.newInstance(context, servicePartsListId);
        StringList routeIds = servicePartsListObj.getInfoList(context, "to[" + REL_CONFIG_ROUTE_TO_SERVICE_PARTS_LIST + "].from.id");
        if (routeIds != null && !routeIds.isEmpty()) {
            return getFrameworkString(context, RESOURCE_DUPLICATE_SERVICE_PARTS_LIST_APPROVAL,
                    "The current service parts list already has a product configuration application and does not need to be submitted again.");
        }
        return "";
    }

    private Map success() {
        Map result = new HashMap();
        result.put("code", "200");
        result.put("mess", "");
        return result;
    }

    private Map fail(String message) {
        Map result = new HashMap();
        result.put("code", "404");
        result.put("mess", UIUtil.isNullOrEmpty(message) ? "Operation failed." : message);
        return result;
    }

    private String getFirstValue(Object value) {
        if (value instanceof String[]) {
            String[] values = (String[]) value;
            return values.length > 0 ? values[0] : "";
        }
        return value == null ? "" : String.valueOf(value);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /**
     * 提交目标销量并完成产品配置任务
     **
     * @param context
     * @param args 请求参数
     * @return String 提交结果JSON
     * @throws Exception
     * @author caipan
     * @date 2026/7/17 16:13
     */
    public String submitTargetSales(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = (String) params.get("objectId");
        String approvalComment = params.get("approvalComment") == null ? "" : String.valueOf(params.get("approvalComment")).trim();
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            return new Gson().toJson(fail(getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.ProductConfigTableIdEmpty", "Current product config table id is empty.")));
        }
        Map editableCheck = checkTargetSalesEditable(context, productConfigId);
        if (editableCheck != null) {
            return new Gson().toJson(editableCheck);
        }
        Map validateResult = validateTargetSalesSubmit(context, args);
        if (!"200".equals(normalizeSelectValue(validateResult.get("code")))) {
            return new Gson().toJson(validateResult);
        }
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        StringList taskIds = productConfigObj.getInfoList(context,
                "from[" + REL_PRODUCT_CONFIG_TO_PRODUCT_CONFIG_TASK + "].to.id");
        ContextUtil.startTransaction(context, true);
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            for (Object taskIdObj : taskIds) {
                String taskId = normalizeSelectValue(taskIdObj);
                if (UIUtil.isNotNullAndNotEmpty(taskId)) {
                    DomainObject.newInstance(context, taskId).setDescription(context, approvalComment);
                    MqlUtil.mqlCommand(context, false, "mod bus " + taskId + " current " + STATE_COMPLETE, true);
                }
            }
            productConfigObj.promote(context);
            MapList managerTasks = getProductConfigTaskList(context, productConfigId);
            ContextUtil.commitTransaction(context);
            Map result = success();
            result.put("mess", getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.SubmitSuccess", "Submitted successfully."));
            result.put("managerTasks", managerTasks);
            return new Gson().toJson(result);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("submitTargetSales error", e);
            return new Gson().toJson(fail(e.getMessage()));
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 校验目标销量提交数据，预留后续业务校验入口
     **
     * @param context
     * @param args 请求参数
     * @return Map 校验结果，审批意见为空时返回失败
     * @throws Exception
     * @author caipan
     * @date 2026/7/17 16:13
     */
    public Map validateTargetSalesSubmit(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String approvalComment = params.get("approvalComment") == null ? "" : String.valueOf(params.get("approvalComment")).trim();
        if (UIUtil.isNullOrEmpty(approvalComment)) {
            return fail(getFrameworkString(context, "emxFramework.JFProductConfigTargetSales.ApprovalCommentRequired", "Approval comment is required."));
        }
        return success();
    }

    /**
     * 获取产品配置表关联的客户经理任务
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return MapList 客户经理任务列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/17 16:13
     */
    private MapList getProductConfigTaskList(Context context, String productConfigId) throws Exception {
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            return new MapList();
        }
        String completeActualSelect = "state[Complete].actual";
        StringList taskSelects = JF_Util_mxJPO.basicBolistSel();
        taskSelects.add(SELECT_ATTR_TITLE);
        taskSelects.add(SELECT_DESCRIPTION);
        taskSelects.add(SELECT_ORIGINATED);
        taskSelects.add(completeActualSelect);
        DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
        MapList managerTasks = productConfigObj.getRelatedObjects(context,
                REL_PRODUCT_CONFIG_TO_PRODUCT_CONFIG_TASK,
                PropertyUtil.getSchemaProperty(context, SYMBOLIC_TYPE_PRODUCT_CONFIG_TASK),
                taskSelects,
                JF_Util_mxJPO.basicRellistSel(),
                false,
                true,
                (short) 1,
                "",
                "",
                (short) 0);
        double timeZoneOffset = JF_ECRRESTService_mxJPO.convertOffsetToDouble(context.getTimezone());
        Map ownerFullNameMap = new HashMap();
        Map currentNlsMap = new HashMap();
        for (Object taskObj : managerTasks) {
            Map taskMap = (Map) taskObj;
            String current = normalizeSelectValue(taskMap.get(SELECT_CURRENT));
            String title = normalizeSelectValue(taskMap.get(SELECT_ATTR_TITLE));
            String owner = normalizeSelectValue(taskMap.get(SELECT_OWNER));
            String ownerFullName = normalizeSelectValue(ownerFullNameMap.get(owner));
            if (UIUtil.isNullOrEmpty(ownerFullName)) {
                ownerFullName = com.matrixone.apps.domain.util.PersonUtil.getFullName(context, owner);
                ownerFullNameMap.put(owner, ownerFullName);
            }
            String currentNls = normalizeSelectValue(currentNlsMap.get(current));
            if (UIUtil.isNullOrEmpty(currentNls)) {
                currentNls = EnoviaResourceBundle.getStateI18NString(context,
                        DomainConstants.POLICY_PROJECT_TASK, current, context.getLocale().toString());
                currentNlsMap.put(current, currentNls);
            }
            String createTime = normalizeSelectValue(taskMap.get(SELECT_ORIGINATED));
            String completeTime = normalizeSelectValue(taskMap.get(completeActualSelect));
            if (UIUtil.isNotNullAndNotEmpty(createTime)) {
                createTime = eMatrixDateFormat.getFormattedDisplayDateTime(createTime, true, 2,
                        timeZoneOffset, context.getLocale());
            }
            if (UIUtil.isNotNullAndNotEmpty(completeTime)) {
                completeTime = eMatrixDateFormat.getFormattedDisplayDateTime(completeTime, true, 2,
                        timeZoneOffset, context.getLocale());
            }
            taskMap.put("title", UIUtil.isNotNullAndNotEmpty(title) ? title : taskMap.get(SELECT_NAME));
            taskMap.put("currentNls", UIUtil.isNotNullAndNotEmpty(currentNls) ? currentNls : current);
            taskMap.put("ownerFullName", UIUtil.isNotNullAndNotEmpty(ownerFullName) ? ownerFullName : owner);
            taskMap.put("createTime", createTime);
            taskMap.put("completeTime", completeTime);
        }
        return managerTasks;
    }

    /**
     * 产品配置表升版按钮后台入口
     **
     * @param context
     * @param args 请求参数
     * @return Map 升版结果，成功时包含新版本对象ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/17 16:13
     */
    public Map reviseProductConfigTable(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String productConfigId = getFirstValue(params.get("objectId"));
        String validationMessage = getProductConfigRevisionValidationMessage(context, productConfigId);
        if (UIUtil.isNotNullAndNotEmpty(validationMessage)) {
            return fail(validationMessage);
        }
        ContextUtil.startTransaction(context, true);
        try {
            DomainObject productConfigObj = DomainObject.newInstance(context, productConfigId);
            BusinessObject revisedObject = productConfigObj.reviseObject(context, productConfigObj.getNextSequence(context), false);
            String newObjectId = revisedObject.getObjectId(context);
            ContextUtil.commitTransaction(context);
            Map result = success();
            result.put("mess", getFrameworkString(context, RESOURCE_PRODUCT_CONFIG_REVISION_SUCCESS, "Revised successfully."));
            result.put("newObjectId", newObjectId);
            return result;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("reviseProductConfigTable error, objectId={}", productConfigId, e);
            return fail(e.getMessage());
        }
    }

    /**
     * 产品配置表升版状态和所有者校验Trigger
     **
     * @param context
     * @param args Trigger参数，args[0]为产品配置表ID
     * @return int 0表示校验通过，1表示阻止升版
     * @throws Exception
     * @author caipan
     * @date 2026/7/17 16:13
     */
    public int checkProductConfigTableRevision(Context context, String[] args) throws Exception {
        String productConfigId = args != null && args.length > 0 ? args[0] : "";
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            productConfigId = MqlUtil.mqlCommand(context, false, "get env OBJECTID", true);
        }
        String validationMessage = getProductConfigRevisionValidationMessage(context, productConfigId);
        if (UIUtil.isNotNullAndNotEmpty(validationMessage)) {
            emxContextUtil_mxJPO.mqlNotice(context, validationMessage);
            return 1;
        }
        return 0;
    }

    /**
     * 产品配置表升版后复制下属配置结构Trigger
     **
     * @param context
     * @param args Trigger参数，args[0]为旧版本ID，args[1]为新版本ID
     * @return int 0表示复制成功
     * @throws Exception
     * @author caipan
     * @date 2026/7/17 16:13
     */
    public int copyProductConfigTableRevisionStructure(Context context, String[] args) throws Exception {
        String oldProductConfigId = args != null && args.length > 0 ? args[0] : "";
        String newProductConfigId = args != null && args.length > 1 ? args[1] : "";
        if (UIUtil.isNullOrEmpty(oldProductConfigId) || UIUtil.isNullOrEmpty(newProductConfigId)) {
            throw new Exception("Product config revision object id is empty.");
        }
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            DomainObject oldProductConfigObj = DomainObject.newInstance(context, oldProductConfigId);
            DomainObject newProductConfigObj = DomainObject.newInstance(context, newProductConfigId);
            MapList oldVehicleConfigs = getVehicleConfigList(context, oldProductConfigId);
            MapList oldPositions = getPositionProductList(context, oldProductConfigId, true);
            MapList oldTargetSales = getTargetSalesList(context, oldProductConfigId);
            StringList supplyPartIds = oldProductConfigObj.getInfoList(context, "from[" + REL_PRODUCT_CONFIG_TO_VPM + "].to.id");

            newProductConfigObj.setAttributeValue(context, ATTR_SUMMARY_RATIO_READY, "FALSE");
            Map vehicleRevisionMap = reviseProductConfigVehicleConfigurations(context, newProductConfigId, oldVehicleConfigs);
            reviseProductConfigPositions(context, newProductConfigId, oldPositions, vehicleRevisionMap);
            reviseProductConfigTargetSales(context, newProductConfigId, oldTargetSales);
            ensureProductConfigSupplyParts(context, newProductConfigId, supplyPartIds);
            return 0;
        } catch (Exception e) {
            LOGGER.error("copyProductConfigTableRevisionStructure error, oldId={}, newId={}", oldProductConfigId, newProductConfigId, e);
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 校验产品配置表是否处于允许升版的状态且为当前用户所有的最新版
     **
     * @param context
     * @param productConfigId 产品配置表ID
     * @return String 校验通过返回空字符串，否则返回提示
     * @throws Exception
     * @author caipan
     * @date 2026/7/17 16:13
     */
    private String getProductConfigRevisionValidationMessage(Context context, String productConfigId) throws Exception {
        String noAccessMessage = getFrameworkString(context, RESOURCE_PRODUCT_CONFIG_REVISION_NO_ACCESS,
                "Only the latest product config table in Released, ManagerFillsIn, or Obsolete state owned by the current user can be revised.");
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            return noAccessMessage;
        }
        StringList selects = new StringList(SELECT_TYPE);
        selects.add(SELECT_CURRENT);
        selects.add(SELECT_OWNER);
        selects.add(SELECT_IS_LAST);
        Map info = DomainObject.newInstance(context, productConfigId).getInfo(context, selects);
        String current = normalizeSelectValue(info.get(SELECT_CURRENT));
        //20260831 update by caipan 产品配置表在已发布、客户经理填写或废弃状态均允许升版
        if (!TYPE_PRODUCT_CONFIG_TABLE.equals(normalizeSelectValue(info.get(SELECT_TYPE)))
                || !(STATE_RELEASED.equals(current)
                || STATE_MANAGER_FILLS_IN.equals(current)
                || STATE_OBSOLETE.equals(current))
                || !context.getUser().equals(normalizeSelectValue(info.get(SELECT_OWNER)))) {
            return noAccessMessage;
        }
        if (!"TRUE".equalsIgnoreCase(normalizeSelectValue(info.get(SELECT_IS_LAST)))) {
            return getFrameworkString(context,
                    RESOURCE_REVISION_NOT_LATEST,
                    "The selected object is not the latest revision. Please select the latest revision to revise.");
        }
        return "";
    }

    /**
     * 升版整车配置并关联新产品配置表
     **
     * @param context
     * @param newProductConfigId 新产品配置表ID
     * @param oldVehicleConfigs 旧整车配置列表
     * @return Map 旧整车配置ID到新版本ID映射
     * @throws Exception
     * @author caipan
     * @date 2026/7/17 16:13
     */
    private Map reviseProductConfigVehicleConfigurations(Context context, String newProductConfigId, MapList oldVehicleConfigs) throws Exception {
        Map revisionMap = new LinkedHashMap();
        for (Object vehicleObj : oldVehicleConfigs) {
            Map vehicleMap = (Map) vehicleObj;
            String oldVehicleId = normalizeSelectValue(vehicleMap.get(SELECT_ID));
            if (UIUtil.isNullOrEmpty(oldVehicleId)) {
                continue;
            }
            String newVehicleId = reviseProductConfigOwnedObject(context, oldVehicleId);
            revisionMap.put(oldVehicleId, newVehicleId);
            DomainRelationship.connect(context,
                    DomainObject.newInstance(context, newProductConfigId),
                    REL_PRODUCT_CONFIG_TO_VEHICLE_CONFIG,
                    DomainObject.newInstance(context, newVehicleId));
        }
        return revisionMap;
    }

    /**
     * 升版位置产品并重建整车配置矩阵
     **
     * @param context
     * @param newProductConfigId 新产品配置表ID
     * @param oldPositions 旧位置产品列表
     * @param vehicleRevisionMap 旧整车配置ID到新版本ID映射
     * @throws Exception
     * @author caipan
     * @date 2026/7/17 16:13
     */
    private void reviseProductConfigPositions(Context context, String newProductConfigId, MapList oldPositions, Map vehicleRevisionMap) throws Exception {
        for (Object positionObj : oldPositions) {
            Map positionMap = (Map) positionObj;
            String oldPositionId = normalizeSelectValue(positionMap.get(SELECT_ID));
            if (UIUtil.isNullOrEmpty(oldPositionId)) {
                continue;
            }
            String newPositionId = reviseProductConfigOwnedObject(context, oldPositionId);
            DomainRelationship.connect(context,
                    DomainObject.newInstance(context, newProductConfigId),
                    REL_PRODUCT_CONFIG_TO_POSITION,
                    DomainObject.newInstance(context, newPositionId));

            MapList parts = positionMap.get("parts") instanceof MapList ? (MapList) positionMap.get("parts") : new MapList();
            for (Object partObj : parts) {
                Map partMap = (Map) partObj;
                String partId = normalizeSelectValue(partMap.get(SELECT_ID));
                if (UIUtil.isNullOrEmpty(partId)) {
                    continue;
                }
                String newPositionPartRelId = getPositionPartRelId(context, newPositionId, partId);
                DomainRelationship newPositionPartRel;
                if (UIUtil.isNullOrEmpty(newPositionPartRelId)) {
                    newPositionPartRel = DomainRelationship.connect(context,
                            DomainObject.newInstance(context, newPositionId),
                            REL_POSITION_TO_VPM,
                            DomainObject.newInstance(context, partId));
                } else {
                    newPositionPartRel = DomainRelationship.newInstance(context, newPositionPartRelId);
                }
                Map relAttributeMap = new HashMap();
                relAttributeMap.put(ATTR_ASSEMBLY_LEVEL, normalizeSelectValue(partMap.get(SELECT_REL_ATTR_ASSEMBLY_LEVEL)));
                relAttributeMap.put(ATTR_ORDER, normalizeSelectValue(partMap.get(SELECT_REL_ATTR_ORDER)));
                newPositionPartRel.setAttributeValues(context, relAttributeMap);
            }

            Map oldPositionVehicleRelMap = getPositionVehicleConfigRelMap(context, oldPositionId);
            for (Object entryObj : oldPositionVehicleRelMap.entrySet()) {
                Map.Entry entry = (Map.Entry) entryObj;
                String oldVehicleId = normalizeSelectValue(entry.getKey());
                String oldPositionVehicleRelId = normalizeSelectValue(entry.getValue());
                String newVehicleId = normalizeSelectValue(vehicleRevisionMap.get(oldVehicleId));
                if (UIUtil.isNullOrEmpty(newVehicleId)) {
                    throw new Exception("Revised vehicle configuration is missing: " + oldVehicleId);
                }
                String newPositionVehicleRelId = ensurePositionVehicleConfigRel(context, newPositionId, newVehicleId);
                for (Object partObj : parts) {
                    String partId = normalizeSelectValue(((Map) partObj).get(SELECT_ID));
                    String oldMatrixRelId = UIUtil.isNullOrEmpty(partId) ? "" : getMatrixRelId(context, oldPositionVehicleRelId, partId);
                    if (UIUtil.isNotNullAndNotEmpty(oldMatrixRelId)) {
                        ensureMatrixRel(context, newPositionVehicleRelId, partId, oldPositionVehicleRelId);
                    }
                }
            }
        }
    }

    /**
     * 升版目标销量并关联新产品配置表
     **
     * @param context
     * @param newProductConfigId 新产品配置表ID
     * @param oldTargetSales 旧目标销量列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/17 16:13
     */
    private void reviseProductConfigTargetSales(Context context, String newProductConfigId, MapList oldTargetSales) throws Exception {
        for (Object targetObj : oldTargetSales) {
            Map targetMap = (Map) targetObj;
            String oldTargetSalesId = normalizeSelectValue(targetMap.get(SELECT_ID));
            if (UIUtil.isNullOrEmpty(oldTargetSalesId)) {
                continue;
            }
            String newTargetSalesId = reviseProductConfigOwnedObject(context, oldTargetSalesId);
            StringList newTargetSalesPartRelIds = DomainObject.newInstance(context, newTargetSalesId)
                    .getInfoList(context, "from[" + REL_TARGET_SALES_TO_VPM + "].id");
            for (Object relIdObj : newTargetSalesPartRelIds) {
                String relId = normalizeSelectValue(relIdObj);
                if (UIUtil.isNotNullAndNotEmpty(relId)) {
                    DomainRelationship.newInstance(context, relId)
                            .setAttributeValue(context, ATTR_ANNUAL_QUANTITY, "");
                }
            }
            DomainRelationship.connect(context,
                    DomainObject.newInstance(context, newProductConfigId),
                    REL_PRODUCT_CONFIG_TO_TARGET_SALES,
                    DomainObject.newInstance(context, newTargetSalesId));
        }
    }

    /**
     * 升版产品配置下属对象
     **
     * @param context
     * @param objectId 旧版本对象ID
     * @return String 新版本对象ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/17 16:13
     */
    private String reviseProductConfigOwnedObject(Context context, String objectId) throws Exception {
        DomainObject oldObject = DomainObject.newInstance(context, objectId);
        BusinessObject revisedObject = oldObject.reviseObject(context, oldObject.getNextSequence(context), false);
        return revisedObject.getObjectId(context);
    }

    /**
     * 确保新产品配置表保留原供货件关系
     **
     * @param context
     * @param newProductConfigId 新产品配置表ID
     * @param supplyPartIds 原供货件ID列表
     * @throws Exception
     * @author caipan
     * @date 2026/7/17 16:13
     */
    private void ensureProductConfigSupplyParts(Context context, String newProductConfigId, StringList supplyPartIds) throws Exception {
        DomainObject newProductConfigObj = DomainObject.newInstance(context, newProductConfigId);
        for (Object partIdObj : supplyPartIds) {
            String partId = normalizeSelectValue(partIdObj);
            if (UIUtil.isNullOrEmpty(partId)) {
                continue;
            }
            StringList relIds = newProductConfigObj.getInfoList(context,
                    "from[" + REL_PRODUCT_CONFIG_TO_VPM + "|to.id==" + partId + "].id");
            if (relIds.isEmpty()) {
                DomainRelationship.connect(context, newProductConfigObj, REL_PRODUCT_CONFIG_TO_VPM,
                        DomainObject.newInstance(context, partId));
            }
        }
    }

    /**
     * 售后件清单发布时同步项目售后件全部版本关系
     **
     * @param context
     * @param args trigger参数，args[0]为售后件清单ID
     * @return int 0表示同步成功
     * @throws Exception
     * @author caipan
     * @date 2026/7/20 16:13
     */
    public int syncProjectServicePartsOnRelease(Context context, String[] args) throws Exception {
        String servicePartsListId = args != null && args.length > 0 ? args[0] : "";
        if (UIUtil.isNullOrEmpty(servicePartsListId)) {
            throw new Exception("售后件清单ID为空，无法同步项目售后件");
        }

        ContextUtil.startTransaction(context, true);
        boolean isPush = false;
        try {
            ContextUtil.pushContext(context);
            isPush = true;

            DomainObject servicePartsListObj = DomainObject.newInstance(context, servicePartsListId);
            String projectId = servicePartsListObj.getInfo(context,
                    "to[" + REL_PROJECT_TO_SERVICE_PARTS_LIST + "].from.id");
            if (UIUtil.isNullOrEmpty(projectId)) {
                throw new Exception("当前售后件清单未关联项目，无法同步项目售后件");
            }

            Set<String> desiredPartIds = new LinkedHashSet<>();
            StringList servicePartIds = servicePartsListObj.getInfoList(context,
                    "from[" + REL_SERVICE_PARTS_LIST_TO_VPM + "].to.id");
            for (Object servicePartIdObj : servicePartIds) {
                String servicePartId = normalizeSelectValue(servicePartIdObj);
                StringList revisionIds = getMajorRevisionIds(context, servicePartId);
                for (Object revisionIdObj : revisionIds) {
                    String revisionId = normalizeSelectValue(revisionIdObj);
                    if (UIUtil.isNotNullAndNotEmpty(revisionId)) {
                        desiredPartIds.add(revisionId);
                    }
                }
            }

            DomainObject projectObj = DomainObject.newInstance(context, projectId);
            StringList objectSelects = new StringList(SELECT_ID);
            StringList relationshipSelects = new StringList(DomainRelationship.SELECT_ID);
            MapList projectServicePartList = projectObj.getRelatedObjects(context,
                    REL_PROJECT_TO_SERVICE_PARTS,
                    TYPE_VPM_REFERENCE,
                    objectSelects,
                    relationshipSelects,
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    (short) 0);

            Set<String> existingPartIds = new HashSet<>();
            StringList removeRelIds = new StringList();
            for (Object projectServicePartObj : projectServicePartList) {
                Map projectServicePartMap = (Map) projectServicePartObj;
                String partId = UIUtil.getValue(projectServicePartMap, SELECT_ID);
                String relId = UIUtil.getValue(projectServicePartMap, DomainRelationship.SELECT_ID);
                if (desiredPartIds.contains(partId) && existingPartIds.add(partId)) {
                    continue;
                }
                if (UIUtil.isNotNullAndNotEmpty(relId)) {
                    removeRelIds.add(relId);
                }
            }
            if (!removeRelIds.isEmpty()) {
                DomainRelationship.disconnect(context, removeRelIds.toStringArray());
            }

            int createdCount = 0;
            for (String desiredPartId : desiredPartIds) {
                if (existingPartIds.contains(desiredPartId)) {
                    continue;
                }
                DomainRelationship.connect(context,
                        projectObj,
                        REL_PROJECT_TO_SERVICE_PARTS,
                        DomainObject.newInstance(context, desiredPartId));
                createdCount++;
            }

            ContextUtil.commitTransaction(context);
            LOGGER.info("Project service parts sync completed. servicePartsListId:{} projectId:{} desiredCount:{} created:{} removed:{}",
                    servicePartsListId,
                    projectId,
                    desiredPartIds.size(),
                    createdCount,
                    removeRelIds.size());
            return 0;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("Project service parts sync failed. servicePartsListId:{}", servicePartsListId, e);
            throw e;
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 售后件清单升版按钮后台入口
     **
     * @param context
     * @param args 请求参数
     * @return Map 升版结果，成功时包含新版本对象ID
     * @throws Exception
     * @author caipan
     * @date 2026/7/20 16:13
     */
    public Map reviseServicePartsList(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        String servicePartsListId = getFirstValue(params.get("objectId"));
        String noAccessMessage = getFrameworkString(context,
                RESOURCE_SERVICE_PARTS_LIST_REVISION_NO_ACCESS,
                "Only a released service parts list owned by the current user can be revised.");
        if (UIUtil.isNullOrEmpty(servicePartsListId)) {
            return fail(noAccessMessage);
        }

        StringList selects = new StringList(SELECT_TYPE);
        selects.add(SELECT_CURRENT);
        selects.add(SELECT_OWNER);
        selects.add(SELECT_IS_LAST);
        Map info = DomainObject.newInstance(context, servicePartsListId).getInfo(context, selects);
        if (!TYPE_SERVICE_PARTS_LIST.equals(normalizeSelectValue(info.get(SELECT_TYPE)))
                || !STATE_RELEASED.equals(normalizeSelectValue(info.get(SELECT_CURRENT)))
                || !context.getUser().equals(normalizeSelectValue(info.get(SELECT_OWNER)))) {
            return fail(noAccessMessage);
        }
        if (!"TRUE".equalsIgnoreCase(normalizeSelectValue(info.get(SELECT_IS_LAST)))) {
            return fail(getFrameworkString(context,
                    RESOURCE_REVISION_NOT_LATEST,
                    "The selected object is not the latest revision. Please select the latest revision to revise."));
        }

        ContextUtil.startTransaction(context, true);
        try {
            DomainObject servicePartsListObj = DomainObject.newInstance(context, servicePartsListId);
            BusinessObject revisedObject = servicePartsListObj.reviseObject(context,
                    servicePartsListObj.getNextSequence(context),
                    false);
            String newObjectId = revisedObject.getObjectId(context);
            ContextUtil.commitTransaction(context);
            Map result = success();
            result.put("mess", getFrameworkString(context,
                    RESOURCE_SERVICE_PARTS_LIST_REVISION_SUCCESS,
                    "Revised successfully."));
            result.put("newObjectId", newObjectId);
            return result;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("reviseServicePartsList error, objectId={}", servicePartsListId, e);
            return fail(e.getMessage());
        }
    }

    /**
     * 获取产品配置审批申请当前审批节点
     **
     * @param context
     * @param args 表格参数
     * @return Vector 当前Assigned任务的首个Title，流程完成或无正在审批任务时返回空
     * @throws Exception
     * @author caipan
     * @date 2026/7/23 16:13
     */
    public Vector getProductConfigRouteCurrentApprovalNode(Context context, String[] args) throws Exception {
        Vector result = new Vector();
        Map programMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        if (objectList == null || objectList.isEmpty()) {
            return result;
        }

        Set routeApplyIds = new LinkedHashSet();
        for (Object rowObj : objectList) {
            String routeApplyId = normalizeSelectValue(((Map) rowObj).get(SELECT_ID));
            if (UIUtil.isNotNullAndNotEmpty(routeApplyId)) {
                routeApplyIds.add(routeApplyId);
            }
        }

        String objectRouteIdSelect = "from[" + DomainConstants.RELATIONSHIP_OBJECT_ROUTE + "].to.id";
        Map objectRouteIdsByApplyId = new HashMap();
        Set objectRouteIds = new LinkedHashSet();
        if (!routeApplyIds.isEmpty()) {
            StringList routeApplyIdList = new StringList();
            routeApplyIdList.addAll(routeApplyIds);
            StringList routeApplySelects = new StringList(SELECT_ID);
            routeApplySelects.add(objectRouteIdSelect);
            MapList routeApplyInfoList = DomainObject.getInfo(context, routeApplyIdList.toStringArray(), routeApplySelects);
            for (Object routeApplyInfoObj : routeApplyInfoList) {
                Map routeApplyInfo = (Map) routeApplyInfoObj;
                String routeApplyId = normalizeSelectValue(routeApplyInfo.get(SELECT_ID));
                StringList routeIds = toStringList(routeApplyInfo.get(objectRouteIdSelect));
                objectRouteIdsByApplyId.put(routeApplyId, routeIds);
                objectRouteIds.addAll(routeIds);
            }
        }

        String assignedState = PropertyUtil.getSchemaProperty(context, "policy", DomainObject.POLICY_INBOX_TASK, "state_Assigned");
        String completeState = PropertyUtil.getSchemaProperty(context, "policy", DomainObject.POLICY_ROUTE, "state_Complete");
        String taskIdSelect = "to[" + DomainConstants.RELATIONSHIP_ROUTE_TASK + "].from.id";
        Map taskIdsByRouteId = new HashMap();
        Set taskIds = new LinkedHashSet();
        if (!objectRouteIds.isEmpty()) {
            StringList routeIdList = new StringList();
            routeIdList.addAll(objectRouteIds);
            StringList routeSelects = new StringList(SELECT_ID);
            routeSelects.add(SELECT_CURRENT);
            routeSelects.add(taskIdSelect);
            MapList routeInfoList = DomainObject.getInfo(context, routeIdList.toStringArray(), routeSelects);
            for (Object routeInfoObj : routeInfoList) {
                Map routeInfo = (Map) routeInfoObj;
                if (completeState.equals(normalizeSelectValue(routeInfo.get(SELECT_CURRENT)))) {
                    continue;
                }
                String routeId = normalizeSelectValue(routeInfo.get(SELECT_ID));
                StringList routeTaskIds = toStringList(routeInfo.get(taskIdSelect));
                taskIdsByRouteId.put(routeId, routeTaskIds);
                taskIds.addAll(routeTaskIds);
            }
        }

        Map assignedTaskTitleById = new HashMap();
        if (!taskIds.isEmpty()) {
            StringList taskIdList = new StringList();
            taskIdList.addAll(taskIds);
            StringList taskSelects = new StringList(SELECT_ID);
            taskSelects.add(SELECT_CURRENT);
            taskSelects.add(SELECT_ATTR_TITLE);
            MapList taskInfoList = DomainObject.getInfo(context, taskIdList.toStringArray(), taskSelects);
            for (Object taskInfoObj : taskInfoList) {
                Map taskInfo = (Map) taskInfoObj;
                if (assignedState.equals(normalizeSelectValue(taskInfo.get(SELECT_CURRENT)))) {
                    assignedTaskTitleById.put(normalizeSelectValue(taskInfo.get(SELECT_ID)),
                            normalizeSelectValue(taskInfo.get(SELECT_ATTR_TITLE)));
                }
            }
        }

        for (Object rowObj : objectList) {
            String routeApplyId = normalizeSelectValue(((Map) rowObj).get(SELECT_ID));
            String title = "";
            StringList routeIds = (StringList) objectRouteIdsByApplyId.get(routeApplyId);
            if (routeIds != null) {
                for (Object routeIdObj : routeIds) {
                    StringList routeTaskIds = (StringList) taskIdsByRouteId.get(normalizeSelectValue(routeIdObj));
                    if (routeTaskIds != null) {
                        for (Object taskIdObj : routeTaskIds) {
                            title = normalizeSelectValue(assignedTaskTitleById.get(normalizeSelectValue(taskIdObj)));
                            if (UIUtil.isNotNullAndNotEmpty(title)) {
                                break;
                            }
                        }
                    }
                    if (UIUtil.isNotNullAndNotEmpty(title)) {
                        break;
                    }
                }
            }
            result.add(title);
        }
        return result;
    }

    /**
     * 客户经理完成产品配置任务后发送邮件通知。
     * 该方法由产品配置表ManagerFillsIn状态的提升Action Trigger调用。
     * 邮件发送失败时只记录日志，不拦截产品配置表的状态提升。
     *
     * @param context 上下文
     * @param args 产品配置表ID
     * @return int 0表示触发器执行完成
     * @author LIUJR
     * @date 2026/7/31
     */
    public int sendManagerCompleteNotification(Context context, String[] args) {
        String productConfigId = args != null && args.length > 0 ? args[0] : "";
        if (UIUtil.isNullOrEmpty(productConfigId)) {
            LOGGER.warn("客户经理完成产品配置任务邮件通知未执行：产品配置表ID为空。");
            return 0;
        }
        try {
            new JF_NotificationUtils_mxJPO().sendProductConfigManagerCompleteNotification(context, new String[]{productConfigId});
        } catch (Exception e) {
            // 邮件属于业务完成后的通知，发送失败只记录日志，不影响产品配置表状态提升。
            LOGGER.error("客户经理完成产品配置任务邮件通知发送失败，productConfigId:{}", productConfigId, e);
        }
        return 0;
    }

    /**
     * 产品配置表转移Owner预校验
     **
     * @param context
     * @param args 请求参数，包含勾选的产品配置表ID
     * @return Map 校验结果
     * @throws Exception
     * @author caipan
     * @date 2026/8/4 16:13
     */
    public Map checkProductConfigTransferOwner(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        StringList productConfigIds = toStringList(params.get("productConfigIds"));
        return validateProductConfigTransferOwnerAccess(context, productConfigIds);
    }

    /**
     * 获取产品配置表或售后件清单转移Owner的候选人员
     **
     * @param context
     * @param args 搜索参数，包含所选对象ID和转移类型
     * @return StringList 所选对象所属项目内研发中心本级的人员ID
     * @throws Exception
     * @author caipan by codex
     * @date 2026/8/28 16:13
     */
    public StringList getTransferOwnerCandidatePersonIds(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        StringList objectIds = toStringList(getFirstValue(params.get("jfTransferIds")));
        boolean isServicePartsList = "servicePartsList".equals(getFirstValue(params.get("jfTransferType")));
        return getTransferOwnerCandidatePersonIds(context, objectIds,
                isServicePartsList ? TYPE_SERVICE_PARTS_LIST : TYPE_PRODUCT_CONFIG_TABLE,
                isServicePartsList ? REL_PROJECT_TO_SERVICE_PARTS_LIST : REL_PROJECT_TO_PRODUCT_CONFIG);
    }

    /**
     * 转移产品配置表及其位置产品、整车配置Owner，新Owner须为当前项目研发中心本级在职人员
     **
     * @param context
     * @param args 请求参数，包含产品配置表ID和新Owner人员ID
     * @return Map 转移结果
     * @throws Exception
     * @author caipan
     * @date 2026/8/4 16:13
     */
    public Map transferProductConfigOwner(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        StringList productConfigIds = toStringList(params.get("productConfigIds"));
        String personId = normalizeSelectValue(params.get("personId"));
        Map accessResult = validateProductConfigTransferOwnerAccess(context, productConfigIds);
        if (!"200".equals(normalizeSelectValue(accessResult.get("code")))) {
            return accessResult;
        }

        if (UIUtil.isNullOrEmpty(personId)) {
            return fail(getFrameworkString(context, RESOURCE_TRANSFER_OWNER_INVALID_PERSON,
                    "Please select an active R&D department member of the current project."));
        }
        StringList personSelects = new StringList(SELECT_TYPE);
        personSelects.add(SELECT_NAME);
        personSelects.add(SELECT_CURRENT);
        Map personInfo = DomainObject.newInstance(context, personId).getInfo(context, personSelects);
        if (!TYPE_PERSON.equals(normalizeSelectValue(personInfo.get(SELECT_TYPE)))
                || !"Active".equals(normalizeSelectValue(personInfo.get(SELECT_CURRENT)))) {
            return fail(getFrameworkString(context, RESOURCE_TRANSFER_OWNER_INVALID_PERSON,
                    "Please select a valid active person."));
        }

        //20260828 update by caipan 校验新Owner属于所选产品配置表项目及研发中心本级部门
        StringList candidatePersonIds = getTransferOwnerCandidatePersonIds(context, productConfigIds,
                TYPE_PRODUCT_CONFIG_TABLE, REL_PROJECT_TO_PRODUCT_CONFIG);
        if (!candidatePersonIds.contains(personId)) {
            return fail(getFrameworkString(context, RESOURCE_TRANSFER_OWNER_INVALID_PERSON,
                    "Please select an active R&D department member of the current project."));
        }

        String newOwner = normalizeSelectValue(personInfo.get(SELECT_NAME));
        String organization = JF_Util_mxJPO.getPersonOrganization(context, newOwner);
        if (UIUtil.isNullOrEmpty(organization)) {
            return fail(getFrameworkString(context, RESOURCE_TRANSFER_OWNER_PERSON_NO_ORGANIZATION,
                    "The selected person has no valid organization."));
        }

        StringList transferObjectIds = getProductConfigTransferObjectIds(context, productConfigIds);
        String positionParentProjectSelect = "to[" + REL_PRODUCT_CONFIG_TO_POSITION + "].from.project";
        String vehicleParentProjectSelect = "to[" + REL_PRODUCT_CONFIG_TO_VEHICLE_CONFIG + "].from.project";
        StringList objectSelects = new StringList(SELECT_ID);
        objectSelects.add(SELECT_TYPE);
        objectSelects.add("project");
        objectSelects.add(positionParentProjectSelect);
        objectSelects.add(vehicleParentProjectSelect);
        MapList transferObjectInfoList = DomainObject.getInfo(context, transferObjectIds.toStringArray(), objectSelects);
        Set allowedTypes = new HashSet();
        allowedTypes.add(TYPE_PRODUCT_CONFIG_TABLE);
        allowedTypes.add(TYPE_POSITION_PRODUCT);
        allowedTypes.add(TYPE_VEHICLE_CONFIGURATION);
        for (Object infoObj : transferObjectInfoList) {
            Map info = (Map) infoObj;
            if (!allowedTypes.contains(normalizeSelectValue(info.get(SELECT_TYPE)))) {
                return fail(getFrameworkString(context, RESOURCE_TRANSFER_OWNER_INVALID_SELECTION,
                        "Please select a valid product configuration table."));
            }
        }

        boolean isPush = false;
        try {
            ContextUtil.startTransaction(context, true);
            ContextUtil.pushContext(context);
            isPush = true;
            for (Object infoObj : transferObjectInfoList) {
                Map info = (Map) infoObj;
                String transferObjectId = normalizeSelectValue(info.get(SELECT_ID));
                String collaborativeSpace = normalizeSelectValue(info.get("project"));
                if (UIUtil.isNullOrEmpty(collaborativeSpace)) {
                    String objectType = normalizeSelectValue(info.get(SELECT_TYPE));
                    String parentProjectSelect = TYPE_POSITION_PRODUCT.equals(objectType)
                            ? positionParentProjectSelect : vehicleParentProjectSelect;
                    StringList parentProjects = toStringList(info.get(parentProjectSelect));
                    for (Object projectObj : parentProjects) {
                        collaborativeSpace = normalizeSelectValue(projectObj);
                        if (UIUtil.isNotNullAndNotEmpty(collaborativeSpace)) {
                            break;
                        }
                    }
                }
                if (UIUtil.isNullOrEmpty(collaborativeSpace)) {
                    throw new Exception("Collaborative space is empty for object: " + transferObjectId);
                }
                DomainObject transferObject = DomainObject.newInstance(context, transferObjectId);
                transferObject.setOwner(context, newOwner);
                transferObject.setPrimaryOwnership(context, collaborativeSpace, organization);
            }
            ContextUtil.commitTransaction(context);
            Map result = success();
            result.put("mess", getFrameworkString(context, RESOURCE_TRANSFER_OWNER_SUCCESS,
                    "Owner transferred successfully."));
            return result;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("transferProductConfigOwner error, productConfigIds={}", productConfigIds, e);
            return fail(getFrameworkString(context, RESOURCE_TRANSFER_OWNER_FAILED,
                    "Failed to transfer owner."));
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 校验当前用户是否可以转移所选产品配置表Owner
     **
     * @param context
     * @param productConfigIds 产品配置表ID
     * @return Map 校验结果
     * @throws Exception
     * @author caipan
     * @date 2026/8/4 16:13
     */
    private Map validateProductConfigTransferOwnerAccess(Context context, StringList productConfigIds) throws Exception {
        Set uniqueIds = new LinkedHashSet();
        for (Object idObj : productConfigIds) {
            String objectId = normalizeSelectValue(idObj);
            if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                uniqueIds.add(objectId);
            }
        }
        if (uniqueIds.isEmpty()) {
            return fail(getFrameworkString(context, RESOURCE_TRANSFER_OWNER_INVALID_SELECTION,
                    "Please select a valid product configuration table."));
        }

        Map roleParams = new HashMap();
        roleParams.put("roleName", "JfITAdmin");
        roleParams.put("userName", context.getUser());
        boolean isITAdmin = JF_Util_mxJPO.isIncludeRole(context, JPO.packArgs(roleParams));

        StringList idList = new StringList();
        idList.addAll(uniqueIds);
        StringList selects = new StringList(SELECT_ID);
        selects.add(SELECT_TYPE);
        selects.add(SELECT_OWNER);
        MapList infoList = DomainObject.getInfo(context, idList.toStringArray(), selects);
        if (infoList.size() != uniqueIds.size()) {
            return fail(getFrameworkString(context, RESOURCE_TRANSFER_OWNER_INVALID_SELECTION,
                    "Please select a valid product configuration table."));
        }
        for (Object infoObj : infoList) {
            Map info = (Map) infoObj;
            if (!TYPE_PRODUCT_CONFIG_TABLE.equals(normalizeSelectValue(info.get(SELECT_TYPE)))) {
                return fail(getFrameworkString(context, RESOURCE_TRANSFER_OWNER_INVALID_SELECTION,
                        "Please select a valid product configuration table."));
            }
            if (!isITAdmin && !context.getUser().equals(normalizeSelectValue(info.get(SELECT_OWNER)))) {
                return fail(getFrameworkString(context, RESOURCE_TRANSFER_OWNER_NO_ACCESS,
                        "Only the owner or a user with the JfITAdmin role can transfer the owner."));
            }
        }
        return success();
    }

    /**
     * 获取所选对象所属项目与研发中心本级部门的交集人员
     **
     * @param context
     * @param objectIds 所选产品配置表或售后件清单ID
     * @param expectedType 所选对象的预期类型
     * @param projectRelationship 项目与所选对象的关系名
     * @return StringList 同时属于全部关联项目及研发中心本级部门的人员ID
     * @throws Exception
     * @author caipan by codex
     * @date 2026/8/28 16:13
     */
    private StringList getTransferOwnerCandidatePersonIds(Context context, StringList objectIds,
                                                           String expectedType, String projectRelationship) throws Exception {
        StringList result = new StringList();
        Set uniqueObjectIds = new LinkedHashSet();
        for (Object idObj : objectIds) {
            String objectId = normalizeSelectValue(idObj);
            if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                uniqueObjectIds.add(objectId);
            }
        }
        if (uniqueObjectIds.isEmpty()) {
            return result;
        }

        String projectIdSelect = "to[" + projectRelationship + "].from.id";
        StringList selects = new StringList(SELECT_TYPE);
        selects.add(projectIdSelect);
        StringList normalizedObjectIds = new StringList();
        normalizedObjectIds.addAll(uniqueObjectIds);
        MapList objectInfoList = DomainObject.getInfo(context, normalizedObjectIds.toStringArray(), selects);
        if (objectInfoList.size() != uniqueObjectIds.size()) {
            return result;
        }

        Set projectIds = new LinkedHashSet();
        for (Object infoObj : objectInfoList) {
            Map info = (Map) infoObj;
            if (!expectedType.equals(normalizeSelectValue(info.get(SELECT_TYPE)))) {
                return result;
            }
            StringList relatedProjectIds = toStringList(info.get(projectIdSelect));
            if (relatedProjectIds.isEmpty()) {
                return result;
            }
            projectIds.addAll(relatedProjectIds);
        }

        Set candidatePersonIds = null;
        for (Object projectIdObj : projectIds) {
            String projectId = normalizeSelectValue(projectIdObj);
            StringList projectPersonIds = JF_PublicMethodClass_mxJPO.getProjectAllPersons(context, projectId, SELECT_ID);
            if (candidatePersonIds == null) {
                candidatePersonIds = new LinkedHashSet(projectPersonIds);
            } else {
                candidatePersonIds.retainAll(projectPersonIds);
            }
        }
        if (candidatePersonIds == null || candidatePersonIds.isEmpty()) {
            return result;
        }

        String rdDepartmentConfig = JF_PublicMethodClass_mxJPO.getBasicUrl(context,
                new String[]{CONFIG_RD_CENTER_DEPARTMENT_ID});
        String rdDepartmentId = UIUtil.isNullOrEmpty(rdDepartmentConfig)
                ? "" : rdDepartmentConfig.split(",")[0].trim();
        if (UIUtil.isNullOrEmpty(rdDepartmentId)) {
            return result;
        }
        StringList rdPersonIds = DomainObject.newInstance(context, rdDepartmentId)
                .getInfoList(context, "from[" + RELATIONSHIP_MEMBER + "].to.id");
        candidatePersonIds.retainAll(rdPersonIds);
        result.addAll(candidatePersonIds);
        return result;
    }

    /**
     * 获取产品配置表转移Owner涉及的对象ID
     **
     * @param context
     * @param productConfigIds 产品配置表ID
     * @return StringList 产品配置表、位置产品和整车配置ID
     * @throws Exception
     * @author caipan
     * @date 2026/8/4 16:13
     */
    private StringList getProductConfigTransferObjectIds(Context context, StringList productConfigIds) throws Exception {
        Set transferIds = new LinkedHashSet();
        StringList normalizedProductConfigIds = new StringList();
        for (Object idObj : productConfigIds) {
            String productConfigId = normalizeSelectValue(idObj);
            if (UIUtil.isNotNullAndNotEmpty(productConfigId) && !normalizedProductConfigIds.contains(productConfigId)) {
                normalizedProductConfigIds.add(productConfigId);
                transferIds.add(productConfigId);
            }
        }
        String positionIdSelect = "from[" + REL_PRODUCT_CONFIG_TO_POSITION + "].to.id";
        String vehicleConfigIdSelect = "from[" + REL_PRODUCT_CONFIG_TO_VEHICLE_CONFIG + "].to.id";
        StringList selects = new StringList(SELECT_ID);
        selects.add(positionIdSelect);
        selects.add(vehicleConfigIdSelect);
        MapList infoList = DomainObject.getInfo(context, normalizedProductConfigIds.toStringArray(), selects);
        for (Object infoObj : infoList) {
            Map info = (Map) infoObj;
            transferIds.addAll(toStringList(info.get(positionIdSelect)));
            transferIds.addAll(toStringList(info.get(vehicleConfigIdSelect)));
        }
        StringList result = new StringList();
        result.addAll(transferIds);
        return result;
    }

    /**
     * 售后件清单转移Owner预校验
     **
     * @param context
     * @param args 请求参数，包含勾选的售后件清单ID
     * @return Map 校验结果
     * @throws Exception
     * @author caipan
     * @date 2026/8/4 16:13
     */
    public Map checkServicePartsListTransferOwner(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        StringList servicePartsListIds = toStringList(params.get("servicePartsListIds"));
        return validateServicePartsListTransferOwnerAccess(context, servicePartsListIds);
    }

    /**
     * 转移勾选售后件清单Owner，新Owner须为当前项目研发中心本级在职人员
     **
     * @param context
     * @param args 请求参数，包含售后件清单ID和新Owner人员ID
     * @return Map 转移结果
     * @throws Exception
     * @author caipan
     * @date 2026/8/4 16:13
     */
    public Map transferServicePartsListOwner(Context context, String[] args) throws Exception {
        Map params = JPO.unpackArgs(args);
        StringList servicePartsListIds = toStringList(params.get("servicePartsListIds"));
        String personId = normalizeSelectValue(params.get("personId"));
        Map accessResult = validateServicePartsListTransferOwnerAccess(context, servicePartsListIds);
        if (!"200".equals(normalizeSelectValue(accessResult.get("code")))) {
            return accessResult;
        }

        if (UIUtil.isNullOrEmpty(personId)) {
            return fail(getFrameworkString(context, RESOURCE_TRANSFER_OWNER_INVALID_PERSON,
                    "Please select an active R&D department member of the current project."));
        }
        StringList personSelects = new StringList(SELECT_TYPE);
        personSelects.add(SELECT_NAME);
        personSelects.add(SELECT_CURRENT);
        Map personInfo = DomainObject.newInstance(context, personId).getInfo(context, personSelects);
        if (!TYPE_PERSON.equals(normalizeSelectValue(personInfo.get(SELECT_TYPE)))
                || !"Active".equals(normalizeSelectValue(personInfo.get(SELECT_CURRENT)))) {
            return fail(getFrameworkString(context, RESOURCE_TRANSFER_OWNER_INVALID_PERSON,
                    "Please select a valid active person."));
        }

        //20260828 update by caipan 校验新Owner属于所选售后件清单项目及研发中心本级部门
        StringList candidatePersonIds = getTransferOwnerCandidatePersonIds(context, servicePartsListIds,
                TYPE_SERVICE_PARTS_LIST, REL_PROJECT_TO_SERVICE_PARTS_LIST);
        if (!candidatePersonIds.contains(personId)) {
            return fail(getFrameworkString(context, RESOURCE_TRANSFER_OWNER_INVALID_PERSON,
                    "Please select an active R&D department member of the current project."));
        }

        String newOwner = normalizeSelectValue(personInfo.get(SELECT_NAME));
        String organization = JF_Util_mxJPO.getPersonOrganization(context, newOwner);
        if (UIUtil.isNullOrEmpty(organization)) {
            return fail(getFrameworkString(context, RESOURCE_TRANSFER_OWNER_PERSON_NO_ORGANIZATION,
                    "The selected person has no valid organization."));
        }

        Set uniqueIds = new LinkedHashSet();
        for (Object idObj : servicePartsListIds) {
            String servicePartsListId = normalizeSelectValue(idObj);
            if (UIUtil.isNotNullAndNotEmpty(servicePartsListId)) {
                uniqueIds.add(servicePartsListId);
            }
        }
        StringList transferIds = new StringList();
        transferIds.addAll(uniqueIds);
        StringList objectSelects = new StringList(SELECT_ID);
        objectSelects.add(SELECT_TYPE);
        objectSelects.add("project");
        MapList transferObjectInfoList = DomainObject.getInfo(context, transferIds.toStringArray(), objectSelects);
        if (transferObjectInfoList.size() != uniqueIds.size()) {
            return fail(getFrameworkString(context, RESOURCE_SERVICE_PARTS_TRANSFER_OWNER_INVALID_SELECTION,
                    "Please select a valid service parts list."));
        }
        for (Object infoObj : transferObjectInfoList) {
            if (!TYPE_SERVICE_PARTS_LIST.equals(normalizeSelectValue(((Map) infoObj).get(SELECT_TYPE)))) {
                return fail(getFrameworkString(context, RESOURCE_SERVICE_PARTS_TRANSFER_OWNER_INVALID_SELECTION,
                        "Please select a valid service parts list."));
            }
        }

        boolean isPush = false;
        try {
            ContextUtil.startTransaction(context, true);
            ContextUtil.pushContext(context);
            isPush = true;
            for (Object infoObj : transferObjectInfoList) {
                Map info = (Map) infoObj;
                String transferObjectId = normalizeSelectValue(info.get(SELECT_ID));
                String collaborativeSpace = normalizeSelectValue(info.get("project"));
                if (UIUtil.isNullOrEmpty(collaborativeSpace)) {
                    throw new Exception("Collaborative space is empty for object: " + transferObjectId);
                }
                DomainObject transferObject = DomainObject.newInstance(context, transferObjectId);
                transferObject.setOwner(context, newOwner);
                transferObject.setPrimaryOwnership(context, collaborativeSpace, organization);
            }
            ContextUtil.commitTransaction(context);
            Map result = success();
            result.put("mess", getFrameworkString(context, RESOURCE_SERVICE_PARTS_TRANSFER_OWNER_SUCCESS,
                    "Service parts list owner transferred successfully."));
            return result;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOGGER.error("transferServicePartsListOwner error, servicePartsListIds={}", servicePartsListIds, e);
            return fail(getFrameworkString(context, RESOURCE_SERVICE_PARTS_TRANSFER_OWNER_FAILED,
                    "Failed to transfer service parts list owner."));
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * 校验当前用户是否可以转移所选售后件清单Owner
     **
     * @param context
     * @param servicePartsListIds 售后件清单ID
     * @return Map 校验结果
     * @throws Exception
     * @author caipan
     * @date 2026/8/4 16:13
     */
    private Map validateServicePartsListTransferOwnerAccess(Context context, StringList servicePartsListIds) throws Exception {
        Set uniqueIds = new LinkedHashSet();
        for (Object idObj : servicePartsListIds) {
            String objectId = normalizeSelectValue(idObj);
            if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                uniqueIds.add(objectId);
            }
        }
        if (uniqueIds.isEmpty()) {
            return fail(getFrameworkString(context, RESOURCE_SERVICE_PARTS_TRANSFER_OWNER_INVALID_SELECTION,
                    "Please select a valid service parts list."));
        }

        Map roleParams = new HashMap();
        roleParams.put("roleName", "JfITAdmin");
        roleParams.put("userName", context.getUser());
        boolean isITAdmin = JF_Util_mxJPO.isIncludeRole(context, JPO.packArgs(roleParams));

        StringList idList = new StringList();
        idList.addAll(uniqueIds);
        StringList selects = new StringList(SELECT_ID);
        selects.add(SELECT_TYPE);
        selects.add(SELECT_OWNER);
        MapList infoList = DomainObject.getInfo(context, idList.toStringArray(), selects);
        if (infoList.size() != uniqueIds.size()) {
            return fail(getFrameworkString(context, RESOURCE_SERVICE_PARTS_TRANSFER_OWNER_INVALID_SELECTION,
                    "Please select a valid service parts list."));
        }
        for (Object infoObj : infoList) {
            Map info = (Map) infoObj;
            if (!TYPE_SERVICE_PARTS_LIST.equals(normalizeSelectValue(info.get(SELECT_TYPE)))) {
                return fail(getFrameworkString(context, RESOURCE_SERVICE_PARTS_TRANSFER_OWNER_INVALID_SELECTION,
                        "Please select a valid service parts list."));
            }
            if (!isITAdmin && !context.getUser().equals(normalizeSelectValue(info.get(SELECT_OWNER)))) {
                return fail(getFrameworkString(context, RESOURCE_SERVICE_PARTS_TRANSFER_OWNER_NO_ACCESS,
                        "Only the owner or a user with the JfITAdmin role can transfer the owner."));
            }
        }
        return success();
    }

}
