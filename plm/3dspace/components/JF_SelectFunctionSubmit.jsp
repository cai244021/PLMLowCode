<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="matrix.db.*" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="static com.matrixone.apps.domain.MultiValueSelects.ATTRIBUTE_TITLE" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../common/emxNavigatorTopErrorInclude.inc" %>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_SelectFunctionSubmit.jsp");
%>
<%
    boolean isSuccess = true ;
    String mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.FunctionModule.Successful", new String[]{});
    try {
        String ids = emxGetParameter(request, "ids");
        JF_LOGGER.info("ids:{}",ids);
        String[] split = ids.split(",");
        String strReferProjectId  = "";
        if (split.length > 0){
            strReferProjectId = split[0];
        }
        DomainObject referProject = DomainObject.newInstance(context, strReferProjectId);
        //获取模版参考项目
        String strTmpReferProjectId = referProject.getInfo(context, "from[JFCostReferConst2Cost].to.id");
        JF_LOGGER.info("strTmpReferProjectId:{}",strTmpReferProjectId);
        referProject.setId(strTmpReferProjectId);
        String[] tableRowIds = emxGetParameterValues(request, "emxTableRowId");
        StringList tableRowIdList = StringList.create(tableRowIds);
        JF_LOGGER.info("tableRowIdList:{}",tableRowIdList);
        for (int i = 0; i < tableRowIdList.size(); i++) {
            String strCosLibId = (String) tableRowIdList.get(i);
            JF_LOGGER.info("strCosLibId:{}",strCosLibId);
            Map rowMap = ProgramCentralUtil.parseTableRowId(context,strCosLibId);
            strCosLibId = (String) rowMap.get("objectId");
            JF_LOGGER.info("strCosLibId:{}",strCosLibId);
            DomainObject oldJFFunctionModule = DomainObject.newInstance(context, strCosLibId);
            //获取功能库对象保存的接口名称
            String strInterface = oldJFFunctionModule.getAttributeValue(context, "JF_ConnInterface");
            Attribute attrTitle = oldJFFunctionModule.getAttributeValues(context,"Title");
            String strDes = oldJFFunctionModule.getDescription(context);
            //创建功能组件
            String strNewOid = FrameworkUtil.autoName(context, "type_JFFunctionModule", "policy_JFFunctionModule");
            DomainObject newObj = DomainObject.newInstance(context, strNewOid);
            //需要保存标题和分类信息
            StringList attrNameList = StringList.create();
//            StringList attrNameList = StringList.create(ATTRIBUTE_TITLE,"JF_CostClass");
            Map saveFunAndPriceAttrMap = new HashMap<>();
            //参考用量和实际用量默认为1
            saveFunAndPriceAttrMap.put("JF_ReferUsage","1");
            saveFunAndPriceAttrMap.put("JF_OfferUsage","1");
            saveFunAndPriceAttrMap.put("JF_FuntionOption","-");
            if (UIUtil.isNotNullAndNotEmpty(strInterface)) {
                //添加interface
                BusinessInterface bInterface = new BusinessInterface(strInterface, context.getVault());
                AttributeTypeList attrTypeList = bInterface.getAttributeTypes(context);
                attrTypeList.stream().forEach(attrType -> {
                    String strAttrName = attrType.getName();
                    attrNameList.add(strAttrName);
                });
                newObj.addBusinessInterface(context, bInterface);
                //获取参考价格、功能选项可能不存在
                String strMqlRes = MqlUtil.mqlCommand(context,false,  "print interface '"+strInterface+ "' select property[function] property[price] property[usage] dump ",true);
                JF_LOGGER.info("strAttrInfo:{}",strMqlRes);
                if (UIUtil.isNotNullAndNotEmpty(strMqlRes)){
                    String strFunAttrName = "";
                    String strPriceAttrName = "";
                    String strUsageAttrName = "";
                    String[] attrNames = strMqlRes.split(",");
                    for (int i1 = 0; i1 < attrNames.length; i1++) {
                        String strProperty = attrNames[i1];
                        String[] strPropertySplit = strProperty.split("value");
                        String strPropertyName = strPropertySplit[0].trim();
                        String strPropertyValue = strPropertySplit[1].trim();
                        if ("function".equals(strPropertyName)){
                            strFunAttrName = strPropertyValue;
                        }else if ("price".equals(strPropertyName)){
                            strPriceAttrName = strPropertyValue;
                        }
                        else if ("usage".equals(strPropertyName)){
                            strUsageAttrName = strPropertyValue;
                        }
                    }
                    if (UIUtil.isNotNullAndNotEmpty(strFunAttrName)){
                        //功能选项
                        String strFunctionValue = referProject.getAttributeValue(context,strFunAttrName);
                        saveFunAndPriceAttrMap.put("JF_FuntionOption",strFunctionValue);

                    }
                    if (UIUtil.isNotNullAndNotEmpty(strPriceAttrName)){
                        //参考价格 评估价格
                        String strPriceValue = referProject.getAttributeValue(context,strPriceAttrName);
                        saveFunAndPriceAttrMap.put("JF_ReferPrice",strPriceValue);
                        saveFunAndPriceAttrMap.put("JF_EvaluatePrice",strPriceValue);

                    }
                    if (UIUtil.isNotNullAndNotEmpty(strUsageAttrName)){
                        //参考用量
                        String strUsageValue = referProject.getAttributeValue(context,strUsageAttrName);
                        saveFunAndPriceAttrMap.put("JF_ReferUsage",strUsageValue);
                        saveFunAndPriceAttrMap.put("JF_OfferUsage",strUsageValue);
                    }

                }
            }
            //从模版里面获取
            AttributeList oldAttrValueList = referProject.getAttributes(context, attrNameList);
            //添加标题属性
            oldAttrValueList.add(attrTitle);
            //复制属性
            newObj.setAttributes(context, oldAttrValueList);
            JF_LOGGER.info("saveFunAndPriceAttrMap:{}",saveFunAndPriceAttrMap);
            newObj.setAttributeValues(context, saveFunAndPriceAttrMap);
            //设置描述
            newObj.setDescription(context, strDes);
            newObj.setAttributeValue(context,"JF_CostClass",oldJFFunctionModule.getAttributeValue(context,"JF_CostClass"));
            //关联参考项目
            DomainRelationship.connect(context, newObj, "JFCostReferConst2Function", false, split);
        }
    } catch (Exception e) {
        isSuccess = false ;
        e.printStackTrace();
        JF_LOGGER.error("error:添加功能组件失败");
        mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.FunctionModule.Failed", new String[]{});
    }
%>
<%@include file="../common/emxNavigatorBottomErrorInclude.inc" %>
<script>
    alert("<%=mess%>")
    if (<%=isSuccess%>){
        parent.closeWindow();
        const  iFra = getTopWindow().getWindowOpener().parent;
        if (iFra){
            iFra.refreshSBTable();
        }
    }
</script>
