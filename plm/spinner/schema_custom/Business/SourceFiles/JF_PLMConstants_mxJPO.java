
/* srmSAD  JPO
*
* Copyright (c) 2015 RP.
*
* All Rights Reserved.
* This program contains proprietary and trade secret information of
* L&T Infotech. Copyright notice is precautionary only and does
* not evidence any actual or intended publication of such program.
*
*/

import com.matrixone.apps.domain.DomainConstants;
import matrix.util.StringList;

import static com.matrixone.apps.domain.DomainConstants.ATTRIBUTE_PROJECT_ROLE;

/**
 * SRM Constants.
 */
public interface JF_PLMConstants_mxJPO {

    // PDF转换接口地址
    String PDF_CONVERT_URL = "http://172.16.31.40:8080/office2pdf/pdf/convert";
    //需要转换pdf的文件类型 mod by chenyan 移除excel 类型
     StringList officeFileList = StringList.create("doc", "docx", "ppt", "pptx", "xls", "xlsx");
//     StringList officeFileList = StringList.create("doc", "docx", "ppt", "pptx");

    //office文件转pdf地址
    String pdfConvertUrl = "http://172.16.20.244:8080/office2pdf/pdf/convert";

    /**String "Promote Connected Object". */
    String ATTRIBUTE_ROUTE_COMPLETION_ACTION_RANGE_PROMOTE_CONNECTED_OBJECT = "Promote Connected Object";
    String ATTRIBUTE_ROUTE_COMPLETION_ACTION_RANGE_Notify_Route_Owner = "Notify Route Owner";

    String STRING_Y = "Y";

    String STRING_N = "N";

    /**String "All". */
    String STRING_All = "All";

    /**String "Part". */
    String STRING_PART = "Part";

    /**String "revision == last". */
    String SELECT_LAST_REVISION = "revision == last";

    /**String "label". */
    String STRING_LABEL = "label";

    /**String "settings". */
    String STRING_SETTINGS = "settings";

    /**String "paramMap". */
    String STRING_PARAMMAP = "paramMap";

    /**String "requestMap". */
    String STRING_REQUESTMAP = "requestMap";

    /**String "languageStr". */
    String STRING_LANGUAGESTR = "languageStr";

    /**String "fieldMap".*/
    String STRING_FIELDMAP = "fieldMap";

    /**String "RequestValuesMap". */
    String STRING_REQUESTVALUESMAP = "RequestValuesMap";

    /**String "paramList". */
    String STRING_PARAMLIST = "paramList";

    /**String "parentOID". */
    String STRING_PARENTOID = "parentOID";

    /**String "objectId". */
    String STRING_OBJECTID = "objectId";

    /**String "New Value". */
    String STRING_NEW_VALUE = "New Value";

    /**String "relId". */
    String STRING_RELID = "relId";

    /**String "New OID". */
    String STRING_NEW_OID = "New OID";

    /**String "objectList". */
    String STRING_OBJECTLIST = "objectList";

    /**String "columnMap". */
    String STRING_COLUMNMAP = "columnMap";

    /**String "timeZone". */
    String STRING_TIMEZONE = "timeZone";

    /**String "locale". */
    String STRING_LOCALE = "locale";

    /**String "first". */
    String SELECT_FIRST = "first";

    /**String "create". */
    String STRING_CREATE = "create";

    /**String "delete". */
    String STRING_DELETE = "delete";

    /**String "action". */
    String STRING_ACTION = "action";

    /**String "Standard". */
    String STRING_STANDARD = "Standard";

    /**String "Approve". */
    String STRING_APPROVE = "Approve";

    /**String "null". */
    String STRING_NULL = "null";

    /**String ",". */
    String STRING_SYMB_COMMA = ",";

    /**String "-". */
    String STRING_SYMB_DASH = "-";

    /**String "*". */
    String STRING_SYMB_ASTERISK = "*";

    /**String "==". */
    String STRING_SYMB_EQUAL = "==";

    /**String "!=". */
    String STRING_SYMB_UNEQUAL = "!=";

    /**String "<". */
    String STRING_SYMB_LESS_THAN = "<";

    /**String ">". */
    String STRING_SYMB_GREATER_THAN = ">";

    /**String "<=". */
    String STRING_SYMB_LESS_THAN_OR_EQUAL = "<=";

    /**String ">=". */
    String STRING_SYMB_GREATER_THAN_OR_EQUAL = ">=";

    /**String "~=". */
    String STRING_SYMB_APPROXIMATE = "~=";

    /**String " ". */
    String STRING_SYMB_SPACE = " ";

    /**String "||". */
    String STRING_SYMB_OR = "||";

    /**String "&&". */
    String STRING_SYMB_AND = "&&";

    /**String "(". */
    String STRING_SYMB_OPEN_PAREN = "(";

    /**String ")". */
    String STRING_SYMB_CLOSE_PAREN = ")";

    /**String "/". */
    String STRING_SYMB_OBLIQUE_LINE = "/";

    /**String "\\". */
    String STRING_SYMB_BACKSLASH = "\\";

    /**String "|". */
    String STRING_SYMB_BAR = "|";

    /**String "'". */
    String STRING_SYMB_QUOTE = "'";

    /**String "<br>". */
    String STRING_SYMB_BR = "<br/>";

    /**String "<br></br>". */
    String STRING_SYMB_HTML_BR = "<br></br>";

    /**String "_". */
    String STRING_SYMB_UNDERLINE = "_";

    /**String "Expand Level". */
    String STRING_EXPAND_LEVEL = "Expand Level";

    /**String "level". */
    String STRING_LEVEL = "level";

    /**String "0". */
    String STRING_ZERO = "0";

    /**String "1". */
    String STRING_ONE = "1";

    /**String "2". */
    String STRING_TWO = "2";

    /**String "3". */
    String STRING_THREE = "3";

    /**String "4". */
    String STRING_FOUR = "4";

    /**String "100". */
    String STRING_ONE_HUNDRED = "100";

    /**String "Editable". */
    String STRING_EDITABLE = "Editable";

    /**String "true". */
    String STRING_TRUE = "true";

    /**String "false". */
    String STRING_FALSE = "false";

    /**String "refli". */
    String STRING_REFLI = "refli";

    /**String "effectivitydate". */
    String STRING_EFFECTIVITYDATE = "effectivitydate";

    /**String "99999". */
    String STRING_MAX_SEQUENCE = "99999";

    /**String "\n". */
    String STRING_BACKSLASH_N = "\n";

    /**String "\t". */
    String STRING_BACKSLASH_T = "\t";

    /**String "Above". */
    String STRING_ABOVE = "Above";

    /**String "0.00". */
    String FORMAT_2_DECIMAL_PLACES = "0.00";

    /**String "<img border='0' src='../common/images/iconStatusRed.gif' name='red' id='red' alt='*' />". */
    String SELECT_RED_IMAGE = "<img border='0' src='../common/images/iconStatusRed.gif' name='red' id='red' alt='*' />";

    /**String "<img border='0' src='../common/images/iconStatusGreen.gif' name='green' id='green' alt='*' />". */
    String SELECT_GREEN_IMAGE = "<img border='0' src='../common/images/iconStatusGreen.gif' name='green' id='green' alt='*' />";
    
    /**int 9.*/
    int INT_NINE = 9;
    
    /**int 99.*/
    int INT_NINETY = 99;

    String RELATIONSHIP_IDMPARTNER2MASTERPART = "IdmPartner2MasterPart";
    String RELTAIONSHIP_SUBTASK = "Subtask";
    String TYPE_BUSINESSUNIT = "Business Unit";
    String RELATIOINSHIP_IDMPARTNERCONNIDMPLANT = "IdmPartnerConnIdmPlant";
    String TYPE_IDMPLANT = "IdmPlant";
    String TYPE_IDMMASTERPART = "IdmMasterPart";
    String RELATIONSHIP_IDMPPAP2MASTERPART = "IdmPPAP2MasterPart";
    String RELATIONSHIP_IDMTASK2IDMPPAP = "IdmTask2IdmPPAP";

    String RELATIONSHIP_IDMPV2IDMMASTERPART = "IdmPV2IdmMasterPart";
    String RELATIONSHIP_IDMPV2DOCUMENT = "IdmPV2Document";
    String RELATIONSHIP_IDMPV2IDMPVITEM = "IdmPV2IdmPVItem";
    String RELATIONSHIP_IDMTASK2IDMPV = "IdmTask2IdmPV";
    String TYPE_IDMPV = "IdmPV";
    String TYPE_IDMPVITEM = "IdmPVItem";


    String TYPE_JFDR = "JFDR";
    String RELATIONSHIP_JFDRCHAIRMANGER2PERSON = "JFDRChairManger2Person";
    String RELATIONSHIP_JFDRMANGER2PERSON = "JFDRManger2Person";

    String OBJECT_MAP_FILE_ID = "fileId";
    String ATTR_PDF_SOURCE_FILE_ID = "attribute[JF_PDFSourceFileId]";
    String PDF_SOURCE_FILE_ID = "JF_PDFSourceFileId";
    String ATTR_PDF_SOURCE = "attribute[JF_PDFSource]";
    //PDF转换标识
    String PDF_COVERT_FLAG = "autoConvert";
    //标识审批人角色
    String ATTR_JFROUTENOTE_APPROVALAROLE = "JFRouteNoteApprovalRole";
    String SELECT_ATTR_JFROUTENOTE_APPROVALAROLE = "attribute["+ATTR_JFROUTENOTE_APPROVALAROLE+"]" ;
    //标识审批对象状态
    String ATTR_JFROUTEOBJECTSTATUS = "JFRouteObjectStatus";
    String SELECT_ATTR_JFROUTEOBJECTSTATUS = "attribute["+ATTR_JFROUTEOBJECTSTATUS+"]" ;

    String REL_ECR2PERSON = "JFECR2Person";
    //经理
    String ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_MANGER = "manager" ;
    //经理+整椅
    String ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_BOTH = "both" ;
    //整椅
    String ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_DEV_ENGINEER = "dev_engineer" ;

    //零件采购类型
    String ATTR_JF_ProcurementType = "JF_VPMReference.JF_ProcurementType" ;


    String ATTR_JF_ProcurementType_RANGE_MAKE = "make" ;
    String ATTR_JF_ProcurementType_RANGE_BUY = "buy" ;
    String ATTR_JF_ProcurementType_RANGE_ICO = "ICO" ;

    String SELECT_ATTR_JF_ProcurementType = "attribute["+ATTR_JF_ProcurementType+"]" ;

    String ATTR_JFDIRECT_BUY = "JF_VPMReference.JF_DirectBuy" ;
    String ATTR_JFPartType = "JF_VPMReference.JF_PartType" ;

    String ATTR_ATTR_JFDIRECT_BUY_RANGE_Y = "direct-buy" ;//Y改成direct-buy
    String ATTR_ATTR_JFDIRECT_BUY_RANGE_N = "non-DB" ;//N改成 non-DB
    String ATTR_ATTR_JFDIRECT_BUY_RANGE_consignment = "consignment" ;//和direct-buy是一个意思

    String SELECT_ATTR_JFDIRECT_BUY = "attribute["+ATTR_JFDIRECT_BUY+"]" ;
    String SELECT_ATTR_JFPartType = "attribute["+ATTR_JFPartType+"]" ;
    //零件号
    String ATTR_JFPART_NUMBER = "JF_VPMReference.JF_PartNumbe" ;
    String SELECT_ATTR_JFPART_NUMBER = "attribute["+ATTR_JFPART_NUMBER+"]" ;
    String ATTR_V_USAGE = "PLMEntity.V_usage" ;
    String SELECT_ATTR_V_USAGE = "attribute["+ATTR_V_USAGE+"]" ;

    String ATTR_JF_PartNameCN = "JF_VPMReference.JF_PartNameCN";
    String ATTR_JF_PartNameEN = "JF_VPMReference.JF_PartNameEN";
    String SELECT_ATTR_JF_PartNameCN = "attribute["+ATTR_JF_PartNameCN+"]" ;
    String SELECT_ATTR_JF_PartNameEN = "attribute["+ATTR_JF_PartNameEN+"]" ;
    String REL_JFRelateItem="JFRelateItem";
    String REL_JFRelateItemParent="JFRelateItemParent";
    String REL_Instance="VPMInstance";
    String REL_XCADBaseDependency="XCADBaseDependency";
    String REL_ReferenceDocument="Reference Document";
    String TYPE_VPMReference="VPMReference";
    String TYPE_Document="Document";
    String TYPE_Drawing="Drawing";
    String STRING_CATIA_V5 = "CATIAV5";
    String STRING_OTHER_DOC = "OtherDoc";
    String STRING_SEND_DATA = "SendData";
    String STRING_DOWNLOAD_V5_FILE = "DownloadV5File";
    String ATTR_JFSubPartId="JFSubPartId";

    String SELECT_ATTR_JFSubPartId = "attribute["+ATTR_JFSubPartId+"]" ;

    String ATTR_JFSubIFReplace="JFSubIFReplace";
    String SELECT_ATTR_JFSubIFReplace = "attribute["+ATTR_JFSubIFReplace+"]" ;

    String ATTR_JFIslocked="JFIslocked";
    String SELECT_ATTR_JFIslocked = "attribute["+ATTR_JFIslocked+"]" ;


    String ATTR_JFSubConnectId="JFSubConnectId";
    String SELECT_ATTR_JFSubConnectId = "attribute["+ATTR_JFSubConnectId+"]" ;

    String ATTR_V_PART_NUMBER = "EnterpriseExtension.V_PartNumber";
    String SELECT_ATTR_V_PART_NUMBER = "attribute["+ATTR_V_PART_NUMBER+"]" ;
    String TYPE_JFECR = "JFECR";
    String TYPE_JFNewECR = "JFNewECR";
    String TYPE_JFFormalECR = "JFFormalECR";

    String ATTR_JFECRTYPE="JFECRType";
    String SELECT_ATTR_JFECRTYPE = "attribute["+ATTR_JFECRTYPE+"]" ;

    String ATTR_JFBREAKPOINTMODE="JFBreakpointMode";
    String SELECT_ATTR_JFBREAKPOINTMODE = "attribute["+ATTR_JFBREAKPOINTMODE+"]" ;

    String ATTR_JFBREAKPOINTTIME="JFBreakpointTime";
    String SELECT_ATTR_JFBREAKPOINTTIME = "attribute["+ATTR_JFBREAKPOINTTIME+"]" ;

    String TYPE_JFDELAYEDFILING = "JFDelayedFiling";
    String POLICY_JFDELAYEDFILING = "JFDelayedFiling";
    String SYMBOLIC_TYPE_JFDELAYEDFILING = "type_JFDelayedFiling";
    String SYMBOLIC_POLICY_JFDELAYEDFILING = "policy_JFDelayedFiling";
    String RELATIONSHIP_JFDELAYEDFILING2TASK = "JFDelayedFiling2Task";
    String ATTR_JFREASONFORDELAY = "JFReasonForDelay";
    String SELECT_ATTR_JFREASONFORDELAY = "attribute["+ATTR_JFREASONFORDELAY+"]" ;
    String ATTR_JFDELAYTHEPLANNEDDATE = "JFDelayThePlannedDate";
    String SELECT_ATTR_JFDELAYTHEPLANNEDDATE = "attribute["+ATTR_JFDELAYTHEPLANNEDDATE+"]" ;
    String ATTR_JFORIGINALPLANNEDDATE = "JFOriginalPlannedDate";
    String ATTR_WHETHERTOPOSTPONE = "JF_ECPwhetherToPostpone";
    String SELECT_ATTR_WHETHERTOPOSTPONE = "attribute["+ATTR_WHETHERTOPOSTPONE+"]" ;
    String ATTR_DAWHETHERTOPOSTPONE = "JF_DAwhetherToPostpone";
    String SELECT_ATTR_DAWHETHERTOPOSTPONE = "attribute["+ATTR_DAWHETHERTOPOSTPONE+"]" ;
    String ATTR_ROUTE_BASE_POLICY = "Route Base Policy";
    String SELECT_ATTR_ROUTE_BASE_POLICY = "attribute["+ATTR_ROUTE_BASE_POLICY+"]" ;
    String ATTR_ROUTE_BASE_STATE = "Route Base State";
    String SELECT_ATTR_ROUTE_BASE_STATE = "attribute["+ATTR_ROUTE_BASE_STATE+"]" ;
    String ATTR_ROUTE_BASE_PURPOSE = "Route Base Purpose";
    String SELECT_ATTR_ROUTE_BASE_PURPOSE = "attribute["+ATTR_ROUTE_BASE_PURPOSE+"]" ;

    // ECR 和会签任务关系
    String RELATIONSHIP_JF_ECR_TASK = "JFECR2Task";
    //会签任务
    String TYPE_JS_SIGN_TASK = "JF_SignTask";
    //APR 任务
    String TYPE_JF_APRTask = "JF_APRTask";
    String TYPE_JF_CustomerTask = "JF_CustomerTask";
    String ATTR_PROJECT_ROLE= "Project Role";
    //内部供应商
    String ATTR_PROJECT_ROLE_Range_InternalSupplier= "Internal Supplier";
    //Costing
    String ATTR_PROJECT_ROLE_Range_Costing= "Costing";
    // AQE
    String ATTR_PROJECT_ROLE_Range_AQE = "AQE representative/PQL";
    //物流
    String ATTR_PROJECT_ROLE_Range_LOR = "Logistics representative";
    //整椅 AME
    String ATTR_PROJECT_ROLE_Range_AME = "AME representative";
    //发泡 AME
    String ATTR_PROJECT_ROLE_Range_Foam_AME = "Foam AME representative";
    //面套AME
    String ATTR_PROJECT_ROLE_Range_Trim_AME = "Trim AME representative";
    String ATTR_PROJECT_ROLE_Range_Launch_Manager= "Launch manager";
    //采购
    String ATTR_PROJECT_ROLE_Range_PRR = "Purchasing representative";
    //商务代表
    String ATTR_PROJECT_ROLE_Range_BU = "Business manager";
    //项目经理
    String ATTR_PROJECT_ROLE_Range_PM = "Project manager";
    String ATTR_PROJECT_ROLE_Range_FinancialBP = "Financial BP";
    String SELECT_ATTR_PROJECT_ROLE = "attribute["+ATTR_PROJECT_ROLE+"]" ;


    //ECR 属性
    String ATTR_JFChangeCyclesOutsourcing = "JFChangeCyclesOutsourcing";
    String SELECT_ATTR_JFChangeCyclesOutsourcing = "attribute["+ATTR_JFChangeCyclesOutsourcing+"]" ;

    String ATTR_JFChangeDevelopmentCostsOutsourcing = "JFChangeDevelopmentCostsOutsourcing";

    String SELECT_ATTR_JFChangeDevelopmentCostsOutsourcing = "attribute["+ATTR_JFChangeDevelopmentCostsOutsourcing+"]" ;

    String ATTR_JFSupplierOrScrapDepotOutsourcing = "JFSupplierOrScrapDepotOutsourcing";

    String SELECT_ATTR_JFSupplierOrScrapDepotOutsourcing = "attribute["+ATTR_JFSupplierOrScrapDepotOutsourcing+"]" ;


    String ATTR_JFChangeCyclesSelfMake = "JFChangeCyclesSelfMake";

    String SELECT_ATTR_JFChangeCyclesSelfMake = "attribute["+ATTR_JFChangeCyclesSelfMake+"]" ;

    String ATTR_JFChangeDevelopmentCostsSelfMake = "JFChangeDevelopmentCostsSelfMake";

    String SELECT_ATTR_JFChangeDevelopmentCostsSelfMake = "attribute["+ATTR_JFChangeDevelopmentCostsSelfMake+"]" ;

    String ATTR_JFSupplierOrScrapDepotSelfMake = "JFSupplierOrScrapDepotSelfMake";

    String SELECT_ATTR_JFSupplierOrScrapDepotSelfMake = "attribute["+ATTR_JFSupplierOrScrapDepotSelfMake+"]" ;

    String ATTR_JFChangesInvestmentsWholeChair = "JFChangesInvestmentsWholeChair";

    String SELECT_ATTR_JFChangesInvestmentsWholeChair = "attribute["+ATTR_JFChangesInvestmentsWholeChair+"]" ;

    String ATTR_JFChangesInvestmentsFoaming = "JFChangesInvestmentsFoaming";

    String SELECT_ATTR_JFChangesInvestmentsFoaming = "attribute["+ATTR_JFChangesInvestmentsFoaming+"]" ;

    String ATTR_JFChangesInvestmentsFaceCovers = "JFChangesInvestmentsFaceCovers";

    String SELECT_ATTR_JFChangesInvestmentsFaceCovers = "attribute["+ATTR_JFChangesInvestmentsFaceCovers+"]" ;

    String ATTR_JFChangesInvestmentsWholeChairExternal = "JFChangesInvestmentsWholeChairExternal";

    String SELECT_ATTR_JFChangesInvestmentsWholeChairExternal = "attribute["+ATTR_JFChangesInvestmentsWholeChairExternal+"]" ;


    String ATTR_JFChangesInvestmentsFoamingExternal = "JFChangesInvestmentsFoamingExternal";

    String SELECT_ATTR_JFChangesInvestmentsFoamingExternal = "attribute["+ATTR_JFChangesInvestmentsFoamingExternal+"]" ;

    String ATTR_JFChangesInvestmentsFaceCoversExternal = "JFChangesInvestmentsFaceCoversExternal";

    String SELECT_ATTR_JFChangesInvestmentsFaceCoversExternal = "attribute["+ATTR_JFChangesInvestmentsFaceCoversExternal+"]" ;

    String ATTR_JFChangesFixtures = "JFChangesFixtures";

    String SELECT_ATTR_JFChangesFixtures = "attribute["+ATTR_JFChangesFixtures+"]" ;

    String ATTR_JFCostOfQuality = "JFCostOfQuality";

    String SELECT_ATTR_JFCostOfQuality = "attribute["+ATTR_JFCostOfQuality+"]" ;

    String ATTR_JFCostOfTrial = "JFCostOfTrial";

    String SELECT_ATTR_JFCostOfTrial = "attribute["+ATTR_JFCostOfTrial+"]" ;

    String ATTR_JFChangesFixturesExternal = "JFChangesFixturesExternal";

    String SELECT_ATTR_JFChangesFixturesExternal = "attribute["+ATTR_JFChangesFixturesExternal+"]" ;


    String ATTR_JFCostOfQualityExternal = "JFCostOfQualityExternal";

    String SELECT_ATTR_JFCostOfQualityExternal = "attribute["+ATTR_JFCostOfQualityExternal+"]" ;

    String ATTR_JFCostOfTrialExternal = "JFCostOfTrialExternal";

    String SELECT_ATTR_JFCostOfTrialExternal = "attribute["+ATTR_JFCostOfTrialExternal+"]" ;

    String ATTR_JFCostPackagingLogistics = "JFCostPackagingLogistics";

    String SELECT_ATTR_JFCostPackagingLogistics = "attribute["+ATTR_JFCostPackagingLogistics+"]" ;

    String ATTR_JFCostPackagingLogisticsExternal = "JFCostPackagingLogisticsExternal";

    String SELECT_ATTR_JFCostPackagingLogisticsExternal = "attribute["+ATTR_JFCostPackagingLogisticsExternal+"]" ;

    String ATTR_JFMaterialsFinishedProductsRework = "JFMaterialsFinishedProductsRework";

    String SELECT_ATTR_JFMaterialsFinishedProductsRework = "attribute["+ATTR_JFMaterialsFinishedProductsRework+"]" ;

    String ATTR_JFChangesUnitPriceCost = "JFChangesUnitPriceCost";

    String SELECT_ATTR_JFChangesUnitPriceCost = "attribute["+ATTR_JFChangesUnitPriceCost+"]" ;

    String ATTR_JFChangesMouldCost = "JFChangesMouldCost";

    String SELECT_ATTR_JFChangesMouldCost = "attribute["+ATTR_JFChangesMouldCost+"]" ;

    String ATTR_JFChangesUnitPriceCostExternal = "JFChangesUnitPriceCostExternal";

    String SELECT_ATTR_JFChangesUnitPriceCostExternal = "attribute["+ATTR_JFChangesUnitPriceCostExternal+"]" ;


    String ATTR_JFChangesMouldCostExternal = "JFChangesMouldCostExternal";

    String SELECT_ATTR_JFChangesMouldCostExternal = "attribute["+ATTR_JFChangesMouldCostExternal+"]" ;

    String ATTR_JFChangesInvestment = "JFChangesInvestment";

    String SELECT_ATTR_JFChangesInvestment = "attribute["+ATTR_JFChangesInvestment+"]" ;

    String ATTR_JFChangesDevelopment = "JFChangesDevelopment";

    String SELECT_ATTR_JFChangesDevelopment = "attribute["+ATTR_JFChangesDevelopment+"]" ;

    String ATTR_JFChangesMould = "JFChangesMould";

    String SELECT_ATTR_JFChangesMould = "attribute["+ATTR_JFChangesMould+"]" ;

    String ATTR_JFSumInventoryScrapAmount = "JFSumInventoryScrapAmount";

    String SELECT_ATTR_JFSumInventoryScrapAmount = "attribute["+ATTR_JFSumInventoryScrapAmount+"]" ;

    String ATTR_JFChangesInvestmentExternal = "JFChangesInvestmentExternal";

    String SELECT_ATTR_JFChangesInvestmentExternal = "attribute["+ATTR_JFChangesInvestmentExternal+"]" ;

    String ATTR_JFChangesDevelopmentExternal = "JFChangesDevelopmentExternal";

    String SELECT_ATTR_JFChangesDevelopmentExternal = "attribute["+ATTR_JFChangesDevelopmentExternal+"]" ;

    String ATTR_JFChangesMouldExternal = "JFChangesMouldExternal";

    String SELECT_ATTR_JFChangesMouldExternal = "attribute["+ATTR_JFChangesMouldExternal+"]" ;

    String ATTR_JFSumInventoryScrapAmountExternal = "JFSumInventoryScrapAmountExternal";

    String SELECT_ATTR_JFSumInventoryScrapAmountExternal = "attribute["+ATTR_JFSumInventoryScrapAmountExternal+"]" ;

    String ATTR_JFAcknowledgmentContentAmount = "JFAcknowledgmentContentAmount";

    String SELECT_ATTR_JFAcknowledgmentContentAmount = "attribute["+ATTR_JFAcknowledgmentContentAmount+"]" ;

    String ATTR_JFTestFee = "JFTestFee";

    String SELECT_ATTR_JFTestFee = "attribute["+ATTR_JFTestFee+"]" ;

    String ATTR_JFAPRNotes = "JFAPRNotes";

    String SELECT_ATTR_JFAPRNotes = "attribute["+ATTR_JFAPRNotes+"]" ;

    String ATTR_JFOtherFee = "JFOtherFee";

    String SELECT_ATTR_JFOtherFee = "attribute["+ATTR_JFOtherFee+"]" ;

    String REL_JFECR2OnelevelPart = "JFECR2OnelevelPart";
    String REL_JFOneLevelPart2Part = "JFOneLevelPart2Part";
    String REL_JFECR2Manufacturing = "JFECR2Manufacturing";

    String ATTR_JFECRName = "JFECRName";

    String SELECT_ATTR_JFECRName = "attribute["+ATTR_JFECRName+"]" ;

    String ATTR_JFWholeChair = "JFWholeChair";

    String SELECT_ATTR_JFWholeChair = "attribute["+ATTR_JFWholeChair+"]" ;
    String TYPE_JFCOMPETITIVEMODEL = "JFCompetitiveModel";
    String TYPE_JF_CompetitiveBOM = "JF_CompetitiveBOM";
    String TYPE_JFCOMPETITIVEBRAND = "JFCompetitiveBrand";
    String REL_JFMODEL2COMPETITIVEBOM = "JFModel2CompetitiveBOM";

    String ATTR_JF_PartType = "JF_VPMReference.JF_PartType";

    String SELECT_ATTR_JF_PartType = "attribute["+ATTR_JF_PartType+"]" ;

    String ATTR_JFCHANGESOURCE_RANGE_BOTH = "Both";
    String ATTR_JFCHANGESOURCE_RANGE_InternalChanges = "Internal Changes";
    String ATTR_JFCHANGESOURCE_RANGE_ExternalChanges = "External Changes";
    String ATTR_JFChangeManHour = "JFChangeMan-hour";
    String SELECT_ATTR_JFChangeManHour = "attribute["+ATTR_JFChangeManHour+"]" ;
    String ATTR_JFChangeManHour_External = "JFChangeMan-hourExternal";
    String SELECT_ATTR_JFChangeManHour_External = "attribute["+ATTR_JFChangeManHour_External+"]" ;

    String ATTR_JFChangeSeatCost = "JFChangeSeatCost";
    String SELECT_ATTR_JFChangeSeatCost = "attribute["+ATTR_JFChangeSeatCost+"]" ;

    String ATTR_JFChangeSeatCostExternal = "JFChangeSeatCostExternal";
    String SELECT_ATTR_JFChangeSeatCostExternal = "attribute["+ATTR_JFChangeSeatCostExternal+"]" ;

    String ATTR_JFChangeTargetPrice = "JFChangeTargetPrice";
    String SELECT_ATTR_JFChangeTargetPrice = "attribute["+ATTR_JFChangeTargetPrice+"]" ;

    String ATTR_JFChangeTargetPriceExternal = "JFChangeTargetPriceExternal";

    String SELECT_ATTR_JFChangeTargetPriceExternal = "attribute["+ATTR_JFChangeTargetPriceExternal+"]" ;


    String ATTR_JFChangeUnitPrice = "JFChangeUnitPrice";

    String SELECT_ATTR_JFChangeUnitPrice = "attribute["+ATTR_JFChangeUnitPrice+"]" ;
    String ATTR_JFChangeMold = "JFChangeMold";

    String SELECT_ATTR_JFChangeMold = "attribute["+ATTR_JFChangeMold+"]" ;


    String ATTR_JFStagnationOfSuppliersInternal = "JFStagnationOfSuppliersInternal";

    String SELECT_ATTR_JFStagnationOfSuppliersInternal = "attribute["+ATTR_JFStagnationOfSuppliersInternal+"]" ;
    String ATTR_JFStagnationOfSuppliersExternal = "JFStagnationOfSuppliersExternal";

    String SELECT_ATTR_JFStagnationOfSuppliersExternal = "attribute["+ATTR_JFStagnationOfSuppliersExternal+"]" ;


    String ATTR_JFChangesTrialExpensesManHours = "JFChangesTrialExpensesManHours";

    String SELECT_ATTR_JFChangesTrialExpensesManHours = "attribute["+ATTR_JFChangesTrialExpensesManHours+"]" ;
    String ATTR_JFChangesTrialExpensesManHoursExternal = "JFChangesTrialExpensesManHoursExternal";

    String SELECT_ATTR_JFChangesTrialExpensesManHoursExternal = "attribute["+ATTR_JFChangesTrialExpensesManHoursExternal+"]" ;


    String ATTR_JFChangeUnitPriceExternal = "JFChangeUnitPriceExternal";

    String SELECT_ATTR_JFChangeUnitPriceExternal = "attribute["+ATTR_JFChangeUnitPriceExternal+"]" ;
    String ATTR_JFChangeMoldCostExternal = "JFChangeMoldCostExternal";

    String SELECT_ATTR_JFChangeMoldCostExternal = "attribute["+ATTR_JFChangeMoldCostExternal+"]" ;
    String ATTR_JFChangeUnitPriceCost = "JFChangeUnitPriceCost";

    String SELECT_ATTR_JFChangeUnitPriceCost = "attribute["+ATTR_JFChangeUnitPriceCost+"]" ;
    String ATTR_JFChangeMoldCost = "JFChangeMoldCost";

    String SELECT_ATTR_JFChangeMoldCost = "attribute["+ATTR_JFChangeMoldCost+"]" ;
    String ATTR_JFChangeUnitPriceCostExternal = "JFChangeUnitPriceCostExternal";

    String SELECT_ATTR_JFChangeUnitPriceCostExternal = "attribute["+ATTR_JFChangeUnitPriceCostExternal+"]" ;
    String ATTR_JFChangeMoldPriceCostExternal = "JFChangeMoldPriceCostExternal";

    String SELECT_ATTR_JFChangeMoldPriceCostExternal = "attribute["+ATTR_JFChangeMoldPriceCostExternal+"]" ;

    String TO_REL_JFRELATEITEM_JFChangeSource = "to[JFRelateItem].attribute[JFChangeSource].value";
    String ATTR_V_CADOrigin = "XCADExtension.V_CADOrigin";
    String SELECT_ATTR_V_CADOrigin = "attribute["+ATTR_V_CADOrigin +"]";
    String  SELECT_TO_INSTANCE_ATTR_V_CADOrigin = "to[VPMInstance].from.attribute[XCADExtension.V_CADOrigin]";
    String  SELECT_TO_INSTANCE_FROM_ID = "to[VPMInstance].from.id";

    //变更来源
    String ATTR_JFCHANGESOURCE = "JFChangeSource";
    String ATTR_JFIsFollow = "JFIsFollow";
     String SELECT_ATTR_JFCHANGESOURCE = "attribute[" + ATTR_JFCHANGESOURCE + "]";
    String SELECT_ATTR_JJFIsFollow = "attribute[" + ATTR_JFIsFollow + "]";

    String REL_JFDR2VPMREFERENCE = "JFDR2VPMReference";
    String REL_JFDA2VPMREFERENCE = "JFDA2VPMReference";

    String ROLE_ECRADMIN = "JfECRAdmin";
    String ATTR_Task_Estimated_Start_Date = "Task Estimated Start Date";
    String ATTR_Task_Estimated_Finish_Date = "Task Estimated Finish Date";
    String ATTR_Task_Estimated_Duration = "Task Estimated Duration";
    String SELECT_ATTR_Task_Estimated_Start_Date = "attribute[Task Estimated Start Date]";
    String SELECT_ATTR_Task_Estimated_Finish_Date = "attribute[Task Estimated Finish Date]";
    String SELECT_ATTR_Task_Estimated_Duration = "attribute[Task Estimated Duration]";

    String ATTR_JF_Area = "JF_VPMReference.JF_Area";
    String ATTR_JF_Dosage = "JF_VPMInstance.JF_Dosage";
    String SELECT_ATTR_JF_Dosage = "attribute[" + ATTR_JF_Dosage+ "]";
    String ATTR_JF_FNA = "JF_VPMInstance.JF_FNA";
    String ATTR_JF_Circumference = "JF_VPMReference.JF_Circumference";
    String ATTR_JF_Material = "JF_VPMReference.JF_Material";
    String SELECT_ATTR_JF_Material="attribute["+ATTR_JF_Material+"]";
    String ATTR_JF_Fabric = "JF_VPMReference.JF_Fabric";
    String SELECT_ATTR_JF_Fabric="attribute["+ATTR_JF_Fabric+"]";
    String ATTR_JF_PartDes = "JF_VPMReference.JF_PartDes";
    String SELECT_ATTR_JF_PartDes="attribute["+ATTR_JF_PartDes+"]";

    String ATTR_JF_Lon = "JFLength";
    String SELECT_ATTR_JF_Lon = "attribute["+ATTR_JF_Lon+"]";

    String ATTR_JF_Wid = "JFWidth";
    String SELECT_ATTR_JF_Wid = "attribute["+ATTR_JF_Wid+"]";

    String ATTR_JF_Hig = "JFHeight";
    String SELECT_ATTR_JF_Hig = "attribute["+ATTR_JF_Hig+"]";


    String ATTR_JF_GramWeight = "JF_VPMReference.JF_GramWeight";
    String SELECT_ATTR_JF_GramWeight = "attribute["+ATTR_JF_GramWeight+"]";

    String ATTR_JF_SurfaceTreatment = "JF_VPMReference.JF_SurfaceTreatment";
    String SELECT_ATTR_JF_SurfaceTreatment = "attribute["+ATTR_JF_SurfaceTreatment+"]";


    String ATTR_JF_Weight = "JF_VPMReference.JF_Weight";
    String SELECT_ATTR_JF_Weight = "attribute["+ATTR_JF_Weight+"]";

    String ATTR_JF_WeightTarget = "JF_VPMReference.JF_WeightTarget";
    String SELECT_ATTR_JF_WeightTarget = "attribute["+ATTR_JF_WeightTarget+"]";

    String ATTR_JF_Detail_EN = "JF_VPMReference.JF_Detail_EN";
    String ATTR_JF_Detail_CN = "JF_VPMReference.JF_Detail_CN";
    String SELECT_ATTR_JF_Detail_CN = "attribute["+ATTR_JF_Detail_CN+"]";

    String ATTR_JF_ProcurementGroup = "JF_ProcurementGroup";
    String ATTR_JF_VPMReference_JF_ProcurementGroup = "JF_VPMReference.JF_ProcurementGroup";

    String SELECT_ATTR_JF_ProcurementGroup = "attribute["+ATTR_JF_ProcurementGroup+"]";
    String SELECT_ATTR_JF_VPMReference_JF_ProcurementGroup = "attribute["+ATTR_JF_VPMReference_JF_ProcurementGroup+"]";

    String ATTR_JF_quantity = "JF_CompetitiveBOM.JF_quantity";
    String SELECT_ATTR_JF_quantity = "attribute["+ATTR_JF_quantity+"]";


    String ATTR_V_IsLastVersion = "PLMReference.V_isLastVersion";
    String SELECT_ATTR_V_IsLastVersion = "attribute["+ATTR_V_IsLastVersion+"]";

    String ATTR_JF_isKeyTask = "JF_isKeyTask";

    String REL_JFChange2Project = "JFChange2Project";
    String REL_JFChangeEventECR = "JFChangeEventECR";
    String REL_JFECR2AffectedProject = "JFECR2AffectedProject";
    String REL_JFECR2CO = "JFECR2CO";
    String TYPE_JFECO = "JFECO";
    String TYPE_JFChangeEventType = "JFChangeEventType";
    String TYPE_JF_relPart2Raw = "JFRelPart2Raw";

    String REL_JFECR2PartPrice = "JFECR2PartPrice";
    String REL_JFECR2MakePartPrice = "JFECR2MakePartPrice";
    String PLMEntity_V_Name = "PLMEntity.V_Name";

    String SELECT_PROJECT_ROOT_PART = "from[JFProject2RootPart|attribute[JFZeroPart]==Y&&to.attribute[PLMReference.V_isLastVersion]==TRUE].to.id";

    String REL_JFPC2EBOM = "JFPC2EBOM";
    String REL_MainProduct = "Main Product";
    String REL_Products = "Products";
    String REL_ProductConfiguration = "Product Configuration";
    String type_ProductConfiguration = "Product Configuration";
    String type_Products = "Products";
    String attr_V_DerivedFrom= "PLMReference.V_DerivedFrom";
    String Select_attr_V_DerivedFrom="attribute["+attr_V_DerivedFrom+"]";

    String RELATIONSHIP_JFRootPart2OnePart = "JFRootPart2OnePart";
    String attr_JF_QuoteData="JF_VPMInstance.JF_QuoteData";
    String attr_JF_copySorce="JF_VPMReference.JF_copySorce";
    String select_attr_JF_QuoteData="attribute["+attr_JF_QuoteData+"]";
    String select_attr_JF_copySorce="attribute["+attr_JF_copySorce+"]";
    String vpmReleased="RELEASED";
    String vpmFROZEN="FROZEN";

    String attr_JF_ISWholeChair = "JF_VPMReference.JF_ISWholeChair";
    String select_attr_JF_ISWholeChair = "attribute["+attr_JF_ISWholeChair+"]";

    String attr_JF_Code = "JF_Code";
    String select_attr_JF_Code = "attribute["+attr_JF_Code+"]";
    String attr_Marketing_Name  = "Marketing Name" ;

    String select_attr_Marketing_Name = "attribute["+attr_Marketing_Name+"]";

    String SELECT_PC_CONNECTION_PC = "from[JFPC2EBOM].to.attribute[EnterpriseExtension.V_PartNumber]";
    String SELECT_PC_CONNECTION_PC_NAME = "from[JFPC2EBOM].to.name";
    String SELECT_PC_CONNECTION_PC_ID = "from[JFPC2EBOM].to.id";
    String SELECT_PC_CONNECTION_PC_PART_TYPE = "from[JFPC2EBOM].to.attribute[JF_VPMReference.JF_PartType]";
    String SELECT_PC_CONNECTION_PC_2 = "from[JFPC2EBOM].to.from[VPMInstance].to.attribute[EnterpriseExtension.V_PartNumber]";
    String SELECT_PC_CONNECTION_PC_NAME_2 = "from[JFPC2EBOM].to.from[VPMInstance].to.name";
    String SELECT_PC_CONNECTION_PC_ID_2 = "from[JFPC2EBOM].to.from[VPMInstance].to.id";

    String type_JFRapidOffer="JFRapidOffer";
    String type_JFCost="JFCost";
    String TYPE_JFCost="type_JFCost";
    String POLICY_JFCost="policy_JFCost";
    String type_JFManufactureFee="JFManufactureFee";
    String TYPE_JFManufactureFee="type_JFManufactureFee";
    String POLICY_JFManufactureFee="polciy_JFManufactureFee";
    String type_JFFunctionModuleTemplate="JFFunctionModuleTemplate";
    String type_JFFunctionModule="JFFunctionModule";
    String islast="islast==true";
    String USER_Admin_Platform = "admin_platform";
    String PROJECT_JFSeat = "JFSeat";

    String ATTR_JF_InternalColorCode = "JF_InternalColorCode";
    String SELECT_JF_InternalColorCode = "attribute["+ATTR_JF_InternalColorCode+"]";

    String ATTR_JF_ColorStyleName = "JF_ColorStyleName";
    String SELECT_ATTR_JF_ColorStyleName = "attribute["+ATTR_JF_ColorStyleName+"]";
    String ATTR_JF_CostClass = "JF_CostClass";
    String SELECT_ATTR_JF_CostClass = "attribute["+ATTR_JF_CostClass+"]";
    String ATTR_JF_EvaluatePrice = "JF_EvaluatePrice";
    String SELECT_ATTR_JF_EvaluatePrice = "attribute["+ATTR_JF_EvaluatePrice+"]";

    String ATTR_JF_CustormColorCode = "JF_CustormColorCode";
    String SELECT_ATTR_JF_CustormColorCode = "attribute["+ATTR_JF_CustormColorCode+"]";
    String rel_JFOffer2JFCostConfig ="JFOffer2JFCostConfig";
    String rel_JFRapidOffer2CostBreakDown ="JFRapidOffer2CostBreakDown";
    String rel_JFModule2Module ="JFModule2Module";
    String rel_JFModule2ReferConst ="JFModule2ReferConst";
    String rel_JFCostReferConst2Function ="JFCostReferConst2Function";
    String type_JFCostConfig ="JFCostConfig";
    String type_JFModules ="JFModules";
    String type_JFCostReferConst ="JFCostReferConst";
    String type_JFCostBreakDown ="JFCostBreakDown";
    //制费库属性
    //总装-总投资（万元）
    String ATTR_JF_assemblyTotalPrice = "JF_assemblyTotalPrice";
    //总装-质量费（万元）
    String ATTR_JF_assemblyQualityPrice = "JF_assemblyQualityPrice";
    //总装-启动费（万元）
    String ATTR_JF_assemblyStartPrice = "JF_assemblyStartPrice";
    //总装-研发费（万元）
    String ATTR_JF_assemblyRDPrice = "JF_assemblyRDPrice";
    //总装-物流费（辆份）
    String ATTR_JF_assemblyLogisticsPrice = "JF_assemblyLogisticsPrice";
    //总装-工时（小时）
    String ATTR_JF_assemblyTimePrice = "JF_assemblyTimePrice";
    //总装-制费（辆份）
    String ATTR_JF_assemblyManufacturingPrice = "JF_assemblyManufacturingPrice";
    //发泡-总投资（万元）
    String ATTR_JF_FoamTotalPrice = "JF_FoamTotalPrice";
    //发泡-制费（辆份）
    String ATTR_JF_FoamManufacturingPrice = "JF_FoamManufacturingPrice";
    //面套-	总投资（万元）
    String ATTR_JF_TrimTotalPrice = "JF_TrimTotalPrice";
    //面套-	工时（小时）
    String ATTR_JF_TrimTotalTimePrice = "JF_TrimTotalTimePrice";
    //面套-	制费（辆份）
    String ATTR_JF_TrimTotalManufacturingPrice = "JF_TrimTotalManufacturingPrice";
    //费用明细 - OEM
    String ATTR_JF_OEMName = "JF_OEMName";
    //费用明细 - 产品
    String ATTR_JF_FreeProject = "JF_FreeProject";
    //费用明细 - 年产量
    String ATTR_JF_Annual = "JF_Annual";
    //费用明细 - 生命周期产量
    String ATTR_JF_FreeLife = "JF_FreeLife";

    String ATTR_JF_ProjectRel = "JF_VPMReference.JF_ProjectRel";
    String SELECT_ATTR_JF_ProjectRel =  "attribute["+ATTR_JF_ProjectRel+"]";
    String ATTR_ProjectRole = "Project Role";
    String SELECT_ATTR_ProjectRole = "attribute["+ATTR_ProjectRole+"]";

    String ATTR_PROJECTROLE_RANGE_Chairmanager = "Chair manager";
    String ATTR_PROJECTROLE_RANGE_Purchasingrepresentative = "Purchasing representative";
    String ATTR_PROJECTROLE_RANGE_FinancialBP = "Financial BP";
    String ATTR_PROJECTROLE_RANGE_Businessmanager = "Business manager";
    String ATTR_PROJECTROLE_RANGE_Projectmanager = "Project manager";
    String ATTR_PROJECTROLE_RANGE_SQDRepresentative = "SQD Representative";
    String rel_JFProject2Snapshot ="JFProject2Snapshot";
    String rel_JFSnapshot2VPMReference ="JFSnapshot2VPMReference";
    String TYPE_JFSnapshot ="JFSnapshot";
    String POLICY_JFSnapshot ="JFSnapshot";
    String POLICY_POLICY_JFSnapshot ="policy_JFSnapshot";
    String POLICY_POLICY_JFColorMatrix ="policy_JFColorMatrix";
    String STATE_POLICY_JFSnapshot_DRAFT ="state_DRAFT";
    String STATE_POLICY_JFSnapshot_APPROVE ="state_APPROVE";
    String STATE_POLICY_JFColorMatrix_Create ="state_Create";
    String STATE_POLICY_JFColorMatrix_Review ="state_Review";
    String STATE_POLICY_JFSnapshot_FROZEN ="state_FROZEN";
    String STRING_SHT ="SHT";
    String ATTR_JF_JFSnapshotType = "JFSnapshotType";
    String SELECT_ATTR_JFSnapshotType = "attribute["+ATTR_JF_JFSnapshotType+"]";
    String Range_JFSnapshotType_KeyShot = "KeyShot";
    String Range_JFSnapshotType_TempShot = "TempShot";

    /**
     * 成本报价数据模型
     */
    String ATTR_JFActualcCost = "JFActualcCost";
    String SELECT_ATTR_JFActualcCost = "attribute["+ATTR_JFActualcCost+"]";

    String ATTR_JFRollUpCost = "JFRollUpCost";
    String SELECT_ATTR_JFRollUpCost = "attribute["+ATTR_JFRollUpCost+"]";
    String TYPE_JFCostAnalysis ="JFCostAnalysis";
    String POLICY_JFCostAnalysis ="JFCostAnalysis";

    String TYPE_JFCostAnalysisItem ="JFCostAnalysisItem";
    String POLICY_JFCostAnalysisItem ="JFCostAnalysisItem";

    String rel_JFCostAnalysis2CostItem ="JFCostAnalysis2CostItem";
    String rel_JFVPMReference2CostAnalysis ="JFVPMReference2CostAnalysis";
    String rel_JFProject2ColorGroup ="JFProject2ColorGroup";

    String rel_JFProject2RootPart ="JFProject2RootPart";

    String SELECT_ATTR_PLMEntity_V_Name="attribute["+PLMEntity_V_Name+"]";

    //DA管理员角色
    String ROLE_DAADMIN = "JfDAAdmin";
    //DR管理员角色
    String ROLE_DRADMIN = "JfDRAdmin";
    //DRW管理员角色
    String ROLE_DRWADMIN = "JfDRWAdmin";
    //ECO管理员角色
    String ROLE_ECOADMIN = "JfECOAdmin";

    String ATTR_JF_VPMReferenceJF_Unit = "JF_VPMReference.JF_Unit";
    String SELECT_ATTR_JF_VPMReferenceJF_Unit = "attribute["+ATTR_JF_VPMReferenceJF_Unit+"]";

    String REL_JFRELATEITEM = "JFRelateItem";
    String REL_JFECRRelateRoot = "JFECRRelateRoot";
    String REL_JFECRRoot2Item = "JFECRRoot2Item";
    String ATTRIBUTE_JF_BOMQuantity = "JF_BOMQuantity";
    String ATTRIBUTE_JF_BOMBeforeQuantity = "JF_BOMBeforeQuantity";
    String ATTRIBUTE_JF_BOMChangeQuantity = "JF_BOMChangeQuantity";
    String ATTRIBUTE_JF_ECRID = "JF_ECRID";
    String ATTRIBUTE_JF_ChangeBeforeRev = "JF_ChangeBeforeRev";
    String ATTRIBUTE_JF_freeState = "JF_freeState";
    String SELECT_ATTRIBUTE_JF_ECRID = "attribute[" + ATTRIBUTE_JF_ECRID + "]";
    String SELECT_ATTRIBUTE_JF_BOMBeforeQuantity = "attribute[" + ATTRIBUTE_JF_BOMBeforeQuantity + "]";
    String SELECT_ATTRIBUTE_JF_freeState = "attribute[" + ATTRIBUTE_JF_freeState + "]";
    String SELECT_ATTRIBUTE_JF_ChangeBeforeRev = "attribute[" + ATTRIBUTE_JF_ChangeBeforeRev + "]";
    String SELECT_ATTRIBUTE_JF_BOMQuantity = "attribute[" + ATTRIBUTE_JF_BOMQuantity + "]";
    String SELECT_ATTRIBUTE_JF_BOMChangeQuantity = "attribute[" + ATTRIBUTE_JF_BOMChangeQuantity + "]";
    String ATTRIBUTE_JF_BOMChangeDes = "JF_BOMChangeDes";
    String SELECT_ATTRIBUTE_JF_BOMChangeDes = "attribute[" + ATTRIBUTE_JF_BOMChangeDes + "]";
    String LOGICAL_ID = "logicalid";
    String RELATIONSHIP = "relationship";
    //面套发泡
    StringList FoamCoatingFlagList = StringList.create("C","U","T");

    String SELECT_ATTRIBUTE_PROJECT_TYPE = "attribute[" + ATTRIBUTE_PROJECT_ROLE + "]";

    String ATTRIBUTE_JFAMENotes = "JFAMENotes";
    String SELECT_ATTRIBUTE_JFAMENotes = "attribute[" + ATTRIBUTE_JFAMENotes + "]";

    String ATTRIBUTE_JFFoamAMENotes = "JFFoamAMENotes";
    String SELECT_ATTRIBUTE_JFFoamAMENotes = "attribute[" + ATTRIBUTE_JFFoamAMENotes + "]";

    String ATTRIBUTE_JFTrimAMENotes = "JFTrimAMENotes";
    String SELECT_ATTRIBUTE_JFTrimAMENotes = "attribute[" + ATTRIBUTE_JFTrimAMENotes + "]";
    String ATTR_JFAFFECTEDFACTORY = "JFAffectedFactory";
    String SELECT_ATTR_JFAFFECTEDFACTORY = "attribute[" + ATTR_JFAFFECTEDFACTORY + "]";

    //开发费工时变更（小时）
    String ATTR_JFCHANGESDEVEEXPENSESMANHOURS = "JFChangesDeveExpensesManHours";
    String SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURS = "attribute[" + ATTR_JFCHANGESDEVEEXPENSESMANHOURS + "]";
    //开发费工时变更（小时）- 外部
    String ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL = "JFChangesDeveExpensesManHoursExternal";
    String SELECT_ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL = "attribute[" + ATTR_JFCHANGESDEVEEXPENSESMANHOURSEXTERNAL + "]";

    String ATTR_JFCHANGESTRIALEXPENSESMANHOURS = "JFChangesTrialExpensesManHours";
    String SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURS = "attribute[" + ATTR_JFCHANGESTRIALEXPENSESMANHOURS + "]";

    String ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL = "JFChangesTrialExpensesManHoursExternal";
    String SELECT_ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL = "attribute[" + ATTR_JFCHANGESTRIALEXPENSESMANHOURSEXTERNAL + "]";
    //是否平台件
    String ATTR_JFISPLATFORMPART = "JFIsPlatformPart";
    String SELECT_ATTR_JFISPLATFORMPART = "attribute[" + ATTR_JFISPLATFORMPART + "]";

    String ATTR_JFQQID = "JFQQ";
    String SELECT_ATTR_JFQQID = "attribute[" + ATTR_JFQQID + "]";

    String ATTR_JFECRID = "JF_ECRID";
    String SELECT_ATTR_JFECRID = "attribute[" + ATTR_JFECRID + "]";
    String ATTR_JFAssociatedOtherProjects = "JFAssociatedOtherProjects";

    String SELECT_ATTR_JFAssociatedOtherProjects = "attribute[" + ATTR_JFAssociatedOtherProjects + "]";

    String ATTR_JFFREEState = "JF_freeState";
    String SELECT_ATTR_JFFREEState = "attribute[" + ATTR_JFFREEState + "]";


    String ATTR_JFChangeWholeSeatPrice = "JFChangeWholeSeatPrice";
    String ATTR_JFChangeWholeSeatPriceExternal = "JFChangeWholeSeatPriceExternal";

    String SELECT_ATTR_JFChangeWholeSeatPrice = "attribute[" + ATTR_JFChangeWholeSeatPrice + "]";
    String SELECT_ATTR_JFChangeWholeSeatPriceExternal = "attribute[" + ATTR_JFChangeWholeSeatPriceExternal + "]";
    String ATTR_JF_ProjectDoc = "JF_ProjectDoc";
    String SELECT_ATTR_JF_ProjectDoc = "attribute[" + ATTR_JF_ProjectDoc + "]";
    String ATTR_JF_ProjectDocType = "JF_ProjectDocType";
    String SELECT_ATTR_JF_ProjectDocType = "attribute[" + ATTR_JF_ProjectDocType + "]";
    String ATTR_JF_ConnProjectName = "JF_ConnProjectName";
    String SELECT_ATTR_JF_ConnProjectName = "attribute[" + ATTR_JF_ConnProjectName + "]";
    String ATTR_JF_ConnProjectPhase = "JF_ConnProjectPhase";
    String SELECT_ATTR_JF_ConnProjectPhase = "attribute[" + ATTR_JF_ConnProjectPhase + "]";
    String ATTR_JF_CounterSign = "JF_CounterSign";
    String SELECT_ATTR_JF_CounterSign = "attribute[" + ATTR_JF_CounterSign + "]";
    String ATTR_JF_Sign = "JF_Sign";
    String SELECT_ATTR_JF_Sign = "attribute[" + ATTR_JF_Sign + "]";
    String REL_JFDoc2Countersign = "JFDoc2Countersign";


     String ATTR_JFCHANGERESON = "JFChangeReson";
     String SELECT_ATTR_JFCHANGERESON = "attribute[" + ATTR_JFCHANGERESON + "]";

     String ATTR_JFProjectPhase = "JFProjectPhase";

     String SELECT_ATTR_JFProjectPhase = "attribute[" + ATTR_JFProjectPhase + "]";

    String ATTR_JFConnectECR = "JF_VPMReference.JF_ConnectECR";

    String SELECT_ATTR_JFConnectECR = "attribute[" + ATTR_JFConnectECR + "]";

    String ATTR_V_isLastVersion = "PLMReference.V_isLastVersion";
    String SELECT_ATTR_V_isLastVersion = "attribute[" + ATTR_V_isLastVersion + "]";

    String ATTR_JF_Function = "JF_Function";
    String SELECT_ATTR_JF_Function = "attribute[" + ATTR_JF_Function + "]";
    String SELECT_ATTRIBUTE_TASK_ESTIMATED_DURATION = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION + "]";
    String SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE + "]";
    String SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE + "]";

    String TYPE_JF_ESOTask = "JF_ESOTask";
    String POLICY_JF_ESOTask = "JF_ESOTask";
    String ATTR_JF_Rows = "JF_Rows";
    String SELECT_ATTR_JF_Rows = "attribute[" + ATTR_JF_Rows + "]";
    String ATTR_JF_Modules = "JF_Modules";
    String SELECT_ATTR_JF_Modules = "attribute[" + ATTR_JF_Modules + "]";
    String ATTR_JF_Locations = "JF_Locations";
    String SELECT_ATTR_JF_Locations = "attribute[" + ATTR_JF_Locations + "]";
    String ATTR_JF_Department = "JF_Department";
    String SELECT_ATTR_JF_Department = "attribute[" + ATTR_JF_Department + "]";
    String ATTR_JF_R2_TKO = "JF_R2_TKO";
    String ATTR_JF_R3TKO = "JF_R3TKO";
    String SELECT_ATTR_JF_R2_TKO = "attribute[" + ATTR_JF_R2_TKO + "]";
    String SELECT_ATTR_JF_R3TKO = "attribute[" + ATTR_JF_R3TKO + "]";
    String ATTR_JF_ESOType = "JF_ESOType";
    String ATTR_JF_BackESOType = "JF_BackESOType";
    String SELECT_ATTR_JF_ESOType = "attribute[" + ATTR_JF_ESOType + "]";
    String SELECT_ATTR_JF_BackESOType = "attribute[" + ATTR_JF_BackESOType + "]";
    String ATTR_JF_Grade = "JF_Grade";
    String ATTR_JF_IsSyncMBOM = "JF_IsSyncMBOM";
    String SELECT_ATTR_JF_Grade = "attribute[" + ATTR_JF_Grade + "]";
    String SELECT_ATTR_JF_IsSyncMBOM = "attribute[" + ATTR_JF_IsSyncMBOM + "]";
    String ATTR_JF_ProjectGrade = "JF_ProjectGrade";
    String SELECT_ATTR_JF_ProjectGrade = "attribute[" + ATTR_JF_ProjectGrade + "]";
    String RELATIONSHIP_JF_relProject2MBOM = "JF_relProject2MBOM";
    String REL_JF_relManufacturedItem = "JF_relManufacturedItem";
    String TYPE_JF_ManufacturedItem= "JF_ManufacturedItem";
    String ATTR_JF_ECR2AffectedPartId = "JF_ECR2AffectedPartId";
    String ATTR_JF_ECR2AffectedGCPartId = "JF_ECR2AffectedGCPartId";
    String ATTR_JFProject2Environment = "JFProject2Environment";

    String ATTR_JF_SupplyAffirm = "JF_SupplyAffirm";
    String ATTR_JF_BelongPart = "JF_BelongPart";
    String SELECT_ATTR_JF_BelongPart = "attribute[" + ATTR_JF_BelongPart + "]";
    String SELECT_ATTR_JF_SupplyAffirm = "attribute[" + ATTR_JF_SupplyAffirm + "]";

    String ATTR_JFZeroPart = "JFZeroPart";
    String SELECT_ATTR_JFZeroPart = "attribute[" + ATTR_JFZeroPart + "]";

    String TYPE_JF_ChangeRecord= "JF_ChangeRecord";
    String POLICY_JF_ChangeRecord= "JF_ChangeRecord";
    String TYPE_type_JF_ChangeRecord= "type_JF_ChangeRecord";
    String ATTR_JF_UpdateDateTime = "JF_UpdateDateTime";
    String SELECT_ATTR_JF_UpdateDate = "attribute[" + ATTR_JF_UpdateDateTime + "]";
    String ATTR_JF_UpdatePerson = "JF_UpdatePerson";
    String SELECT_ATTR_JF_UpdatePerson = "attribute[" + ATTR_JF_UpdatePerson + "]";
    String REL_JF_relProject2ChangeRecord = "JF_relProject2ChangeRecord";
    String REL_JF_relChangeRecord2ECR = "JF_relChangeRecord2ECR";
    String CLASS_TITLE_ASSEMBLY = "组合件";
    String CLASS_TITLE_COMPONENT = "单件";
    String CLASS_TITLE_COMPONENT_RANGE = "component";
    String CLASS_TITLE_ASSEMBLY_RANGE = "assembly";


    String rel_JFProject2PartList ="JFProject2PartList";
    String rel_JFPartList2Snapshot ="JFPartList2Snapshot";
    String rel_JFPartList2VPMReference ="JFPartList2VPMReference";
    String TYPE_JFPartList = "JFPartList";

    String ATTR_JFSyncSRM = "JFSyncSRM";
    String SELECT_ATTR_JFSyncSRM = "attribute[" + ATTR_JFSyncSRM + "]";

    String ATTR_JFIsInit = "JFIsInit";
    String SELECT_ATTR_JFIsInit = "attribute[" + ATTR_JFIsInit + "]";


    String ATTR_JFPartListType = "JFPartListType";
    String SELECT_ATTR_JFPartListType = "attribute[" + ATTR_JFPartListType + "]";
    String ATTR_JF_PartSubType = "JF_PartSubType";

    String SELECT_ATTR_JF_PartSubType = "attribute[" + ATTR_JF_PartSubType + "]";

    String ATTR_JF_VPMReference_JF_PartSubType = "JF_VPMReference.JF_PartSubType";

    String SELECT_ATTR_JF_VPMReference_JF_PartSubType = "attribute[" + ATTR_JF_VPMReference_JF_PartSubType + "]";

    /** partlist 属性 **/
    String ATTR_JFCarryOver = "JFCarryOver";
    String ATTR_JFCarryOver_RANGE_New = "New";
    String ATTR_JFCarryOver_RANGE_CarryOver = "CarryOver";
    String SELECT_ATTR_JFCarryOver = "attribute[" + ATTR_JFCarryOver + "]";
    String ATTR_JFBicycleUsage = "JFBicycleUsage";

    String SELECT_ATTR_JFBicycleUsage = "attribute[" + ATTR_JFBicycleUsage + "]";

    String ATTR_JFPartSpecifications = "JFPartSpecifications";
    String SELECT_ATTR_JFPartSpecifications = "attribute[" + ATTR_JFPartSpecifications + "]";

    String ATTR_JFTotalDemand = "JFTotalDemand";

    String SELECT_ATTR_JFTotalDemand = "attribute[" + ATTR_JFTotalDemand + "]";

    String ATTR_JFDeliveryDate = "JFDeliveryDate";

    String SELECT_ATTR_JFDeliveryDate = "attribute[" + ATTR_JFDeliveryDate + "]";

    String ATTR_JFCurrency = "JFCurrency";

    String SELECT_ATTR_JFCurrency = "attribute[" + ATTR_JFCurrency + "]";

    String ATTR_JFOutputLocation = "JFOutputLocation";

    String SELECT_ATTR_JFOutputLocation = "attribute[" + ATTR_JFOutputLocation + "]";

    String ATTR_JFBudgetUnitPrice = "JFBudgetUnitPrice";

    String SELECT_ATTR_JFBudgetUnitPrice = "attribute[" + ATTR_JFBudgetUnitPrice + "]";

    String ATTR_JFBudgetMoldFee = "JFBudgetMoldFee";

    String SELECT_ATTR_JFBudgetMoldFee = "attribute[" + ATTR_JFBudgetMoldFee + "]";

    String ATTR_JFBudgetExperimentalFee = "JFBudgetExperimentalFee";

    String SELECT_ATTR_JFBudgetExperimentalFee = "attribute[" + ATTR_JFBudgetExperimentalFee + "]";

    String ATTR_JFBudgetInspectionToolFee = "JFBudgetInspectionToolFee";

    String SELECT_ATTR_JFBudgetInspectionToolFee = "attribute[" + ATTR_JFBudgetInspectionToolFee + "]";

    String ATTR_JFBudgetLeatherTextureFee = "JFBudgetLeatherTextureFee";

    String SELECT_ATTR_JFBudgetLeatherTextureFee = "attribute[" + ATTR_JFBudgetLeatherTextureFee + "]";

    String ATTR_JFDeliveryPhase = "JFDeliveryPhase";

    String SELECT_ATTR_JFDeliveryPhase = "attribute[" + ATTR_JFDeliveryPhase + "]";

    String ATTR_JFSSOWIssueDate = "JFSSOWIssueDate";

    String SELECT_ATTR_JFSSOWIssueDate = "attribute[" + ATTR_JFSSOWIssueDate + "]";

    String ATTR_JFDrawDataCompletionPlanDate = "JFDrawDataCompletionPlanDate";

    String SELECT_ATTR_JFDrawDataCompletionPlanDate = "attribute[" + ATTR_JFDrawDataCompletionPlanDate + "]";
    String ATTR_JF3DReleasedDesignatedPlanDate = "JF3DReleasedDesignatedPlanDate";

    String SELECT_ATTR_JF3DReleasedDesignatedPlanDate = "attribute[" + ATTR_JF3DReleasedDesignatedPlanDate + "]";

    String ATTR_JFSupplierDesignatedPlanDate = "JFSupplierDesignatedPlanDate";

    String SELECT_ATTR_JFSupplierDesignatedPlanDate = "attribute[" + ATTR_JFSupplierDesignatedPlanDate + "]";


    String ATTR_JFStopUsingFlag = "JFStopUsingFlag";

    String SELECT_ATTR_JFStopUsingFlag = "attribute[" + ATTR_JFStopUsingFlag + "]";

    String ATTR_JFPartRequirementPlanDate  = "JFPartRequirementPlanDate";
    String SELECT_ATTR_JFPartRequirementPlanDate = "attribute[" + ATTR_JFPartRequirementPlanDate + "]";

    String ATTR_JFProducingArea  = "JF_ProducingArea";

    String SELECT_ATTR_JFProducingArea = "attribute[" + ATTR_JFProducingArea + "]";
    String ATTR_JFPartRequirementCommitmentDate  = "JFPartRequirementCommitmentDate";

    String SELECT_ATTR_JFPartRequirementCommitmentDate = "attribute[" + ATTR_JFPartRequirementCommitmentDate + "]";

    String ATTR_PROJECT_ROLE_RANGE_EBOMFILE = "EBOMFile";
/** partlist 属性结束**/
    String modNameRevMql = "mod bus Id name 'N' revision 'R'";
    /** part 零件属性更新 begin  2025/08/25**/
    String ATTR_SurfaceTreatment = "Surface Treatment";
    String SELECT_ATTR_SurfaceTreatment = "attribute["+ATTR_SurfaceTreatment+"]";

    String ATTR_JFLength = "JFLength";
    String SELECT_ATTR_JFLength = "attribute["+ATTR_JFLength+"]";

    String ATTR_JFWidth = "JFWidth";
    String SELECT_ATTR_JFWidth = "attribute["+ATTR_JFWidth+"]";

    String ATTR_JFHeight = "JFHeight";
    String SELECT_ATTR_JFHeight = "attribute["+ATTR_JFHeight+"]";
    String ATTR_CustomerPartNumber = "CustomerPartNumber";
    String SELECT_ATTR_CustomerPartNumber = "attribute["+ATTR_CustomerPartNumber+"]";
    String ATTR_CustomerPartRevision = "CustomerPartRevision";
    String SELECT_ATTR_CustomerPartRevision = "attribute["+ATTR_CustomerPartRevision+"]";

    /** part 零件属性更新  end  2025/08/25**/

    String ATTR_JSdataProcessProgress = "JSdataProcessProgress";
    String ATTR_JF_SyncStatus = "JF_SyncStatus";
    String SELECT_ATTR_JSdataProcessProgress = "attribute["+ATTR_JSdataProcessProgress+"]";
    String SELECT_ATTR_JF_SyncStatus = "attribute["+ATTR_JF_SyncStatus+"]";

    String RANGE_PROCESS_PROGRESS_OUTSOURCEDATACOMPLETE = "OutsourceDataComplete";  //外发数据完成
    String RANGE_PROCESS_PROGRESS_CAAPROCESSINGCOMPLETE = "CAAProcessingComplete";  //CAA处理完成
    String RANGE_PROCESS_PROGRESS_CAAPROCEFAILD = "CAAProcessingFailed";  //CAA处理失败
    String RANGE_synchronized = "synchronized";  //已同步
    String RANGE_unsynchronized = "unsynchronized";  //未同步

    String attr_JF_DocReceiptConfirmation="JF_DocReceiptConfirmation";
    String select_attr_JF_DocReceiptConfirmation="attribute["+attr_JF_DocReceiptConfirmation+"]";

    String ATTR_JF_LeadReview = "JF_LeadReview";
    String ATTR_JF_ChairManagerReview = "JF_ChairManagerReview";
    String ATTR_JF_DepartmentManager = "JF_DepartmentManager";
    String ATTR_JF_ProjectReview = "JF_ProjectReview";
    String ATTR_JF_SpecialistReview = "JF_SpecialistReview";
    String ATTR_JF_SpecialistReceipt = "JF_SpecialistReceipt";
    String ATTR_JF_ReceiptConfirmation = "JF_ReceiptConfirmation";
    String ATTR_JF_FileFormatRequirements = "JF_FileFormatRequirements";
    String ATTR_JF_DocDepartmentManager = "JF_DocDepartmentManager";
    String ATTR_JF_DocChairManagerReview = "JF_DocChairManagerReview";
    String SELECT_ATTR_JF_DocChairManagerReview = "attribute["+ATTR_JF_DocChairManagerReview+"]";
    String SELECT_ATTR_JF_DocDepartmentManager = "attribute["+ATTR_JF_DocDepartmentManager+"]";
    String ATTR_JF_DocFileFormatRequirements = "JF_DocFileFormatRequirements";
    String SELECT_ATTR_JF_DocFileFormatRequirements = "attribute["+ATTR_JF_DocFileFormatRequirements+"]";
    String ATTR_JF_DocLeadReview = "JF_DocLeadReview";
    String SELECT_ATTR_JF_DocLeadReview = "attribute["+ATTR_JF_DocLeadReview+"]";
    String ATTR_JF_DocProjectReview = "JF_DocProjectReview";
    String SELECT_ATTR_JF_DocProjectReview = "attribute["+ATTR_JF_DocProjectReview+"]";
    String ATTR_JF_DocSecurity = "JF_DocSecurity";
    String SELECT_ATTR_JF_DocSecurity = "attribute["+ATTR_JF_DocSecurity+"]";
    String ATTR_JF_DocSpecialistReceipt = "JF_DocSpecialistReceipt";
    String SELECT_ATTR_JF_DocSpecialistReceipt = "attribute["+ATTR_JF_DocSpecialistReceipt+"]";
    String ATTR_JF_DocSpecialistReview = "JF_DocSpecialistReview";
    String SELECT_ATTR_JF_DocSpecialistReview = "attribute["+ATTR_JF_DocSpecialistReview+"]";
    String ATTR_JF_DocSpecialistReviewGroup = "JF_DocSpecialistReviewGroup";
    String SELECT_ATTR_JF_DocSpecialistReviewGroup = "attribute["+ATTR_JF_DocSpecialistReviewGroup+"]";
    String ATTR_JF_DocSpecialty = "JF_DocSpecialty";
    String SELECT_ATTR_JF_DocSpecialty = "attribute["+ATTR_JF_DocSpecialty+"]";
    String ATTR_JF_DocumentType = "JF_DocumentType";
    String SELECT_ATTR_JF_DocumentType = "attribute["+ATTR_JF_DocumentType+"]";
    String ATTR_JF_ConnProjectId = "JF_ConnProjectId";
    String SELECT_ATTR_JF_ConnProjectId = "attribute["+ATTR_JF_ConnProjectId+"]";

    String ATTR_JFPartListProfessional = "JFPartListProfessional";
    String SELECT_ATTR_JFPartListProfessional = "attribute["+ATTR_JFPartListProfessional+"]";

    String ATTR_PartList_JFProcurementType = "JFProcurementType";
    String SELECT_PartList_ATTR_JFProcurementType = "attribute["+ATTR_PartList_JFProcurementType+"]";

    //PCR相关属性
    String ATTR_JF_PCRType="JF_PCRType";
    String SELECT_ATTR_JF_PCRType= "attribute["+ATTR_JF_PCRType+"]";

    String ATTR_JF_PCRLevel="JF_PCRLevel";
    String SELECT_ATTR_JF_PCRLevel= "attribute["+ATTR_JF_PCRLevel+"]";

    String ATTR_JF_PCRProductUnit="JF_PCRProductUnit";
    String SELECT_ATTR_JF_PCRProductUnit= "attribute["+ATTR_JF_PCRProductUnit+"]";

    String ATTR_JFPCRFactory="JFPCRFactory";
    String SELECT_ATTR_JFPCRFactory= "attribute["+ATTR_JFPCRFactory+"]";

    String ATTR_JF_PCRIsInOFactories="JF_PCRIsInOFactories";
    String SELECT_ATTR_JF_PCRIsInOFactories= "attribute["+ATTR_JF_PCRIsInOFactories+"]";

    String ATTR_JF_PCRProjectPhase="JF_PCRProjectPhase";
    String ATTR_JF_EvaluationConclusion="JF_EvaluationConclusion";
    String SELECT_ATTR_JF_PCRProjectPhase= "attribute["+ATTR_JF_PCRProjectPhase+"]";
    String SELECT_ATTR_JF_EvaluationConclusion= "attribute["+ATTR_JF_EvaluationConclusion+"]";

    String ATTR_JF_PCRRelatedZone="JF_PCRRelatedZone";
    String SELECT_ATTR_JF_PCRRelatedZone= "attribute["+ATTR_JF_PCRRelatedZone+"]";

    String ATTR_JF_PCRRelatedProcess="JF_PCRRelatedProcess";
    String SELECT_ATTR_JF_PCRRelatedProcess= "attribute["+ATTR_JF_PCRRelatedProcess+"]";

    String ATTR_JF_PCRIsAProductChar="JF_PCRIsAProductChar";
    String SELECT_ATTR_JF_PCRIsAProductChar= "attribute["+ATTR_JF_PCRIsAProductChar+"]";

    String ATTR_JF_PCRProductSAChar="JF_PCRProductSAChar";
    String SELECT_ATTR_JF_PCRProductSAChar= "attribute["+ATTR_JF_PCRProductSAChar+"]";

    String ATTR_JF_PCRChangeReason="JF_PCRChangeReason";
    String SELECT_ATTR_JF_PCRChangeReason= "attribute["+ATTR_JF_PCRChangeReason+"]";

    String ATTR_JF_PCRDBeforeChange="JF_PCRDBeforeChange";
    String SELECT_ATTR_JF_PCRDBeforeChange= "attribute["+ATTR_JF_PCRDBeforeChange+"]";

    String ATTR_JF_PCRDAfterChange="JF_PCRDAfterChange";
    String SELECT_ATTR_JF_PCRDAfterChange= "attribute["+ATTR_JF_PCRDAfterChange+"]";

    String ATTR_JF_PCRIsAffectFKPI="JF_PCRIsAffectFKPI";
    String SELECT_ATTR_JF_PCRIsAffectFKPI= "attribute["+ATTR_JF_PCRIsAffectFKPI+"]";

    String ATTR_JF_PCRKPI="JF_PCRKPI";
    String SELECT_ATTR_JF_PCRKPI= "attribute["+ATTR_JF_PCRKPI+"]";
    String ATTR_CustomerDrawingNumber="CustomerDrawingNumber";
    String SELECT_ATTR_CustomerDrawingNumber= "attribute["+ATTR_CustomerDrawingNumber+"]";
    String ATTR_JF_PCRDemandBPDate="JF_PCRDemandBPDate";
    String SELECT_ATTR_JF_PCRDemandBPDate= "attribute["+ATTR_JF_PCRDemandBPDate+"]";

    String ATTR_JF_PCRSwitchingMethod="JF_PCRSwitchingMethod";
    String SELECT_ATTR_JF_PCRSwitchingMethod= "attribute["+ATTR_JF_PCRSwitchingMethod+"]";

    String Type_JFPCRTask="JF_PCRTask";
    String Type_JF_PCR="JF_PCR";
    String Type_JF_PCRExecuteTask="JF_PCRExecuteTask";
    String Type_JF_PCRVerificationTask="JF_PCRVerificationTask";
    String SYMBOLIC_Type_JFPCRTask="type_JF_PCRTask";
    String SYMBOLIC_JF_PCRExecuteTask="type_JF_PCRExecuteTask";
    String SYMBOLIC_JF_PCRVerificationTask="type_JF_PCRVerificationTask";
    String Rel_JFPCR2Task="JF_PCR2Task";
    String Rel_JF_PCR2ExecuteTask="JF_PCR2ExecuteTask";
    String Rel_JF_PCR2VerificationTask="JF_PCR2VerificationTask";
    String ATTR_Supplier="Supplier";
    String SELECT_ATTR_Supplier= "attribute["+ATTR_Supplier+"]";
    String ATTR_SupplierPartNumber="SupplierPartNumber";
    String SELECT_ATTR_SupplierPartNumber= "attribute["+ATTR_SupplierPartNumber+"]";
    String ATTR_JF_MaterialStar="JF_VPMReference.JF_MaterialStar";
    String SELECT_ATTR_JF_MaterialStar= "attribute["+ATTR_JF_MaterialStar+"]";
    String ATTR_JF_MaterialDensity="JF_VPMReference.JF_MaterialDensity";
    String SELECT_ATTR_JF_MaterialDensity= "attribute["+ATTR_JF_MaterialDensity+"]";
    String ATTR_SurfaceTreatmentGrade="SurfaceTreatmentGrade";
    String SELECT_ATTR_SurfaceTreatmentGrade= "attribute["+ATTR_SurfaceTreatmentGrade+"]";
    String ATTR_SurfaceTreatmentArea="SurfaceTreatmentArea";
    String SELECT_ATTR_SurfaceTreatmentArea= "attribute["+ATTR_SurfaceTreatmentArea+"]";
    String ATTR_JFThickness="JFThickness";
    String SELECT_ATTR_JFThickness= "attribute["+ATTR_JFThickness+"]";
    String ATTR_Diam="Diam";
    String SELECT_ATTR_Diam= "attribute["+ATTR_Diam+"]";
    String ATTR_InsideDiameter="Inside Diameter";
    String SELECT_ATTR_InsideDiameter= "attribute["+ATTR_InsideDiameter+"]";

    String ATTR_JF_NeedValidation="JF_NeedValidation";
    String ATTR_JF_IsEvaluationInformCustomer="JF_IsEvaluationInformCustomer";
    String ATTR_JF_validateConclusion="JF_validateConclusion";
    String ATTR_JFIsLastQuote="JFIsLastQuote";
    String ATTR_JFIsTKOData="JFIsTKOData";

    String Attr_JF_Participant="JF_Participant";
    String SELECT_Attr_JF_Participant="attribute["+Attr_JF_Participant+"]";
    String Attr_JF_Content="JF_Content";
    String SELECT_Attr_JF_Content="attribute["+Attr_JF_Content+"]";
    String Attr_JF_Record="JF_Record";
    String SELECT_Attr_JF_Record="attribute["+Attr_JF_Record+"]";
    String Attr_JF_DateOfSignature="JF_DateOfSignature";
    String SELECT_Attr_JF_DateOfSignature="attribute["+Attr_JF_DateOfSignature+"]";

    String attr_JF_BreakpointSwitchingDate="JF_BreakpointSwitchingDate";
    String Attr_SynchroEBOMCAD="SynchroEBOMExt.V_InEBOMUser";
    String SELECT_Attr_SynchroEBOMCAD="attribute["+Attr_SynchroEBOMCAD+"]";
    String Attr_JF_OriginalPart="JF_VPMReference.JF_OriginalPart";
    String SELECT_Attr_JF_OriginalPart="attribute["+Attr_JF_OriginalPart+"]";
    String Attr_JF_FlexiblePart="JF_VPMReference.JF_FlexiblePart";
    String SELECT_Attr_JF_FlexiblePart="attribute["+Attr_JF_FlexiblePart+"]";
    String REL_JFOriginalPart2Flexible="JFOriginalPart2Flexible";
    String Attr_JF_IsBubbling="JF_VPMReferenceCost.JF_IsBubbling";
    String Attr_JF_JoinBubbling="JF_JoinBubbling";
    String Attr_JFBubblingFlag="JF_BubblingFlag";
    String Attr_JF_BubblingECR="JF_BubblingECR";
    String Attr_JF_EvaluateMessage="JF_EvaluateMessage";
    String SELECT_Attr_JF_JoinBubbling="attribute["+Attr_JF_JoinBubbling+"]";
    String SELECT_Attr_JF_BubblingECR="attribute["+Attr_JF_BubblingECR+"]";
    String SELECT_Attr_JFBubblingFlag="attribute["+Attr_JFBubblingFlag+"]";
    String REL_VPMRepInstance="VPMRepInstance";
    String REL_XCADAssemblyRepInstance="XCADAssemblyRepInstance";

    String REL_JFECRRelateRootBubble = "JFECRRelateRootBubble";
    String REL_JFECRRoot2ItemBubble = "JFECRRoot2ItemBubble";
    String REL_JFPS2VPM = "JFPS2VPM";


    String ATTR_PREFIX = "attribute[" ;
    String ATTR_SUFFIX = "]" ;

    /***********************************************formalECR 属性 **************************************************************************/
    //原始成本
    String ATTR_JF_VPMReferenceCost_JF_UnitPrice = "JF_VPMReferenceCost.JF_UnitPrice";
    String ATTR_JF_VPMReferenceCost_JF_MoldPrice = "JF_VPMReferenceCost.JF_MoldPrice";
    String ATTR_JF_VPMReferenceCost_JF_TrialExpensesPrice = "JF_VPMReferenceCost.JF_TrialExpensesPrice";
    String ATTR_JF_VPMReferenceCost_JF_StagnationPrice = "JF_VPMReferenceCost.JF_StagnationPrice";
    String ATTR_JF_VPMReferenceCost_JF_CostMoldPrice = "JF_VPMReferenceCost.JF_CostMoldPrice";
    String ATTR_JF_VPMReferenceCost_JF_TargetPrice = "JF_VPMReferenceCost.JF_TargetPrice";
    String ATTR_JF_VPMReferenceCost_JF_Man_hour = "JF_VPMReferenceCost.JF_Man_hour";
    String ATTR_JF_VPMReferenceCost_JF_UnitPriceCost = "JF_VPMReferenceCost.JF_UnitPriceCost";
    //自制件
    String ATTR_JF_VPMReferenceCost_JF_ChangeManHour = "JF_VPMReferenceCost.JF_ChangeMan_hour";
    String ATTR_JF_VPMReferenceCost_JF_ChangeManHourExternal = "JF_VPMReferenceCost.JF_ChangeMan_hourExternal";
    String ATTR_JF_VPMReferenceCost_JF_ChangeSeatCost = "JF_VPMReferenceCost.JF_ChangeSeatCost";
    String ATTR_JF_VPMReferenceCost_JF_ChangeTargetPrice = "JF_VPMReferenceCost.JF_ChangeTargetPrice";
    String ATTR_JF_VPMReferenceCost_JF_ChangeSeatCostExternal = "JF_VPMReferenceCost.JF_ChangeSeatCostExternal";
    String ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPrice = "JF_VPMReferenceCost.JF_ChangeWholeSeatPrice";
    String ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPriceExternal = "JF_VPMReferenceCost.JF_ChangeWholeSeatPriceExternal";

    //采购件
    String ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice = "JF_VPMReferenceCost.JF_ChangeUnitPrice";
    String ATTR_JF_VPMReferenceCost_JF_ChangeMold = "JF_VPMReferenceCost.JF_ChangeMold";
    String ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceExternal = "JF_VPMReferenceCost.JF_ChangeUnitPriceExternal";
    String ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal = "JF_VPMReferenceCost.JF_ChangeMoldCostExternal";
    String ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCost = "JF_VPMReferenceCost.JF_ChangeUnitPriceCost";
    String ATTR_JF_VPMReferenceCost_JF_ChangeMoldCost = "JF_VPMReferenceCost.JF_ChangeMoldCost";
    String ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal = "JF_VPMReferenceCost.JF_ChangeUnitPriceCostExternal";
    String ATTR_JF_VPMReferenceCost_JF_ChangeMoldPriceCostExternal = "JF_VPMReferenceCost.JF_ChangeMoldPriceCostExternal";
    String ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHours = "JF_VPMReferenceCost.JF_ChangesTrialExpensesManHours";
    String ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHoursExternal = "JF_VPMReferenceCost.JF_ChangesTrialExpensesManHoursExternal";
    String ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersInternal = "JF_VPMReferenceCost.JF_StagnationOfSuppliersInternal";
    String ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersExternal = "JF_VPMReferenceCost.JF_StagnationOfSuppliersExternal";
    String ATTR_JF_VPMReferenceCost_JF_Evaluate = "JF_VPMReferenceCost.JF_Evaluate";

    String SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPrice = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_UnitPrice + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_MoldPrice = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_MoldPrice + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_TrialExpensesPrice = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_TrialExpensesPrice + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_StagnationPrice = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_StagnationPrice + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_CostMoldPrice = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_CostMoldPrice + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_TargetPrice = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_TargetPrice + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_Man_hour = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_Man_hour + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_UnitPriceCost = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_UnitPriceCost + ATTR_SUFFIX;

    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeManHour = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangeManHour + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeManHourExternal = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangeManHourExternal + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeSeatCost = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangeSeatCost + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeTargetPrice = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangeTargetPrice + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeSeatCostExternal = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangeSeatCostExternal + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPrice = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPrice + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPriceExternal = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangeWholeSeatPriceExternal + ATTR_SUFFIX;

    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangeUnitPrice + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMold = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangeMold + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceExternal = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceExternal + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangeMoldCostExternal + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCost = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCost + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldCost = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangeMoldCost + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangeUnitPriceCostExternal + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangeMoldPriceCostExternal = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangeMoldPriceCostExternal + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHours = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHours + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHoursExternal = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_ChangesTrialExpensesManHoursExternal + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersInternal = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersInternal + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersExternal = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_StagnationOfSuppliersExternal + ATTR_SUFFIX;
    String SELECT_ATTR_JF_VPMReferenceCost_JF_Evaluate = ATTR_PREFIX + ATTR_JF_VPMReferenceCost_JF_Evaluate + ATTR_SUFFIX;

    String ATTR_JF_LogisticsFees = "JF_LogisticsFees";

    String SELECT_ATTR_JF_LogisticsFees = ATTR_PREFIX + ATTR_JF_LogisticsFees + ATTR_SUFFIX;

    String STRING_SELECT_TABLE = "selectedTable";
    String ATTR_JFPartType_RANGE_U = "U";
    String ATTR_JFPartType_RANGE_T = "T";
    //整椅
    String ATTR_JFPartType_RANGE_C = "C";

    //零级件
    String ATTR_JFPartType_Flag_Zero = "ZeroPart";
    String ATTR_JF_VPMReferenceCost_JF_Evaluate_RANGE_Y = "Y";
    String ATTR_JF_VPMReferenceCost_JF_Evaluate_RANGE_N = "N";
    String STRING_TABLE = "table" ;
    String ATTR_Function = "Function";
    String SELECT_ATTR_Function="attribute["+ATTR_Function+"]";
    String ATTR_RollPartNumber = "RollPartNumber";
    String SELECT_ATTR_RollPartNumber="attribute["+ATTR_RollPartNumber+"]";
    String ATTR_JF_BubbleStartTime = "JF_BubbleStartTime";
    String SELECT_JF_BubbleStartTime="attribute["+ATTR_JF_BubbleStartTime+"]";
    String ATTR_JF_BubbleEndTime = "JF_BubbleEndTime";
    String SELECT_ATTR_JF_BubbleEndTime="attribute["+ATTR_JF_BubbleEndTime+"]";
    String RELATIONSHIP_JFRelateItem = "JFRelateItem";
    String REL_JFS_PARTS_APPLICATION_2_VPM = "JFSPartsApplication2VPMReference";
    String TYPE_JFS_PARTS_APPLICATION = "JFSPartsApplication";
    String Attr_JF_LibUnit="JF_LibUnit";
    String Select_Attr_JF_LibUnit="attribute["+Attr_JF_LibUnit+"]";
    String RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS = "JFVPMReference2CustomerParts";
    String RELATIONSHIP_JFVPMREFERENCE2CUSTOMERPARTS2PROJECT = "JFVPMReference2CustomerParts2Project";
    String TYPE_JFCUSTOMERPARTS = "JFCustomerParts";
    String Attr_JFCustomerPartNumber="JFCustomerPartNumber";
    String Select_Attr_JFCustomerPartNumber="attribute["+Attr_JFCustomerPartNumber+"]";
    String Attr_JFCustomerPartName="JFCustomerPartName";
    String Select_Attr_JFCustomerPartName="attribute["+Attr_JFCustomerPartName+"]";
    String Attr_JFCustomerPartRevision="JFCustomerPartRevision";
    String Select_Attr_JFCustomerPartRevision="attribute["+Attr_JFCustomerPartRevision+"]";
    String Attr_JF_DirectBuy="JF_DirectBuy";
    String Select_Attr_JF_DirectBuy="attribute["+Attr_JF_DirectBuy+"]";
    String Attr_JF_RelDescription="JF_RelDescription";
    String Select_Attr_JF_RelDescription="attribute["+Attr_JF_RelDescription+"]";
    String TYPE_JFExpertsGroup = "JFExpertsGroup";
    //20260810 update by ljr 增加批量文档审批对象、项目关系及状态常量。
    String TYPE_JFBatchDocumentReview = "JFBatchDocumentReview";
    String POLICY_JFBatchDocumentReview = "JFBatchDocumentReview";
    String RELATIONSHIP_JFBatchDocumentReview2Project = "JFBatchDocumentReview2Project";
    String STATE_JFBatchDocumentReview_INWORK = "InWork";
    String STATE_JFBatchDocumentReview_REVIEW = "Review";
    String STATE_JFBatchDocumentReview_RELEASED = "Released";
    String ATTR_JF_Notice_AME_representative = "JF_Notice_AME_representative";
    String SELECT_ATTR_JF_Notice_AME_representative = "attribute["+ATTR_JF_Notice_AME_representative+"]";
    String ATTR_JF_Notice_AQE_representativePQL = "JF_Notice_AQE_representativePQL";
    String SELECT_ATTR_JF_Notice_AQE_representativePQL = "attribute["+ATTR_JF_Notice_AQE_representativePQL+"]";
    String ATTR_JF_Notice_SQD_Representative = "JF_Notice_SQD_Representative";
    String SELECT_ATTR_JF_Notice_SQD_Representative = "attribute["+ATTR_JF_Notice_SQD_Representative+"]";
    //20260819 update by ljr 增加面套BOM转换使用的源属性、MBOM属性及关系属性常量。
    String ATTR_JF_PartENDes = "JF_VPMReference.JF_PartENDes";
    String SELECT_ATTR_JF_PartENDes = "attribute[" + ATTR_JF_PartENDes + "]";
    String ATTR_NET_AREA = "NetArea";
    String SELECT_ATTR_NET_AREA = "attribute[" + ATTR_NET_AREA + "]";
    String ATTR_JF_FABRIC_MATERIAL_TYPE = "JF Fabric Material Type";
    String SELECT_ATTR_JF_FABRIC_MATERIAL_TYPE = "attribute[" + ATTR_JF_FABRIC_MATERIAL_TYPE + "]";
    String ATTR_JF_MBOM_PART_NUMBER = "JF_PartNumber";
    String ATTR_JF_MBOM_PART_NAME_CN = "JF_PartNameCN";
    String ATTR_JF_MBOM_PART_NAME_EN = "JF_PartNameEN";
    String ATTR_JF_MBOM_PART_TYPE = "JF_PartType";
    String ATTR_JF_MBOM_PROCUREMENT_TYPE = "JF_ProcurementType";
    String ATTR_JF_MBOM_UNIT = "JF_Unit";
    String ATTR_JF_MBOM_PART_DES = "JF_PartDes";
    String ATTR_JF_MBOM_PART_EN_DES = "JF_PartENDes";
    String ATTR_JF_ROLL_PART_NUMBER = "JFRollPartNumber";
    String ATTR_JF_MBOM_WIDTH = "JF_Width";
    String ATTR_JF_MBOM_UTILIZATION_RATE = "JF_Utilizationrate";
    String ATTR_JF_MBOM_DOSAGE = "JF_Dosage";
    String ATTR_JF_ORIGINATE_FLAG = "JF_OriginateFlag";
    String ATTR_JF_ORIGINATE_FLAG_RANGE_EBOM = "EBOM";
}

