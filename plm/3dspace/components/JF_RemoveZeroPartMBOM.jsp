<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUtil" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="javassist.compiler.ast.StringL" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    private static final Logger JF_LOGGER =  LoggerFactory.getLogger("JF_RemoveZeroPartMBOM.jsp");
%>
<%
    try {
        String objectId = emxGetParameter(request, "objectId");
        JF_LOGGER.info("objectId:{}", objectId);
        //选择移除的件
        String[] tableRowIds = emxGetParameterValues(request, "emxTableRowId");
        StringList tableRowIdList = StringList.create(tableRowIds);
        JF_LOGGER.info("tableRowIdList:{}", tableRowIdList);
        //项目对象
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        StringList bosel = StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_REVISION);
        StringList relSel = StringList.create(DomainRelationship.SELECT_ID);
        //获取项目下的第一层级的整椅节点
        String rootMBOMId = domainObject.getInfo(context, "from[JF_relProject2MBOM].to.id");
        DomainObject rootMBOM = DomainObject.newInstance(context, rootMBOMId);
        StringList firstMBOMIdList = rootMBOM.getInfoList(context, "from[JF_relManufacturedItem].to.id");
        JF_LOGGER.info("rootMBOMId:{}", rootMBOMId);
        JF_LOGGER.info("firstMBOMIdList:{}", firstMBOMIdList);
        //获取项目的供货件列表
        String relWhere = "attribute[JFZeroPart]==Y";
        StringList zeroPartList = (StringList) domainObject.getRelatedObjects(
            context,
            "JFProject2RootPart",
            "VPMReference",
            bosel,
            relSel,
            false,
            true,
            (short) 1,
            null,
            relWhere,
            0
        ).stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, DomainConstants.SELECT_ID);
        }).collect(Collectors.toCollection(StringList::new));
        JF_LOGGER.info("zeroPartList:{}", zeroPartList);

        //获取项目的ECR
        StringList ecrIdList = domainObject.getInfoList(context, "to[JFChange2Project].from.id");
        JF_LOGGER.info("ecrIdList:{}", ecrIdList);
        //判断如果选择的MBOM结构中是否包含在供货件中，在供货件中不可以移除，否则可以移除
        StringList errorList = new StringList();
        StringList zeroErrorList = new StringList();
        Map<String, String> oidMap = new HashMap<String, String>();
        Map<String, String> oidECRMap = new HashMap<String, String>();
        DomainObject objectPart = DomainObject.newInstance(context);
        for (int i = 0; i < tableRowIdList.size(); i++) {
            String strId = (String) tableRowIdList.get(i);
            Map rowMap = ProgramCentralUtil.parseTableRowId(context, strId);
            strId = (String) rowMap.get("objectId");
            String relId = (String) rowMap.get("relId");
            objectPart.setId(strId);
            String number = objectPart.getAttributeValue(context, "JF_PartNumber");
            String rev = objectPart.getInfo(context, DomainConstants.SELECT_REVISION);
            //需要是MBOM的整椅节点
            if (firstMBOMIdList.contains(strId)) {
                //找到MBOM的EBOM
                JF_LOGGER.info("找到MBOM的EBOM:{}", strId);
//                if (!rev.contains("-")) {
//                    rev += "-000";
//                }
                JF_LOGGER.info("number:{}", number);
                JF_LOGGER.info("rev:{}", rev);
                //可能有多个
                MapList mlVPMList = DomainObject.findObjects(context, "VPMReference", null,
                "attribute[EnterpriseExtension.V_PartNumber]=='" + number + "' && revision=='" + rev + "'", StringList.create(DomainConstants.SELECT_ID));
                JF_LOGGER.info("mlVPMList:{}", mlVPMList);

                Boolean flag = Boolean.FALSE;
                for (int i1 = 0; i1 < mlVPMList.size(); i1++) {
                    Map map = (Map) mlVPMList.get(i1);
                    String id = UIUtil.getValue(map, DomainConstants.SELECT_ID);
                    JF_LOGGER.info("id: {}, zeroPartList.contains(id):{}", id, zeroPartList.contains(id));
                    if (zeroPartList.contains(id)) {
                        //包含不让移除
                        flag = Boolean.FALSE;
                        break;
                    } else {
                        //可以移除
                        objectPart.setId(id);
                        String ecrId = objectPart.getInfo(context, "to[JFECRRelateRoot].from.id");
                        JF_LOGGER.info("ecrId:{}", ecrId);
                        if (UIUtil.isNotNullAndNotEmpty(ecrId) && ecrIdList.contains(ecrId)) {
                            oidECRMap.put(strId, ecrId);
                            flag = Boolean.TRUE;
                            break;
                        }
                    }
                }
                JF_LOGGER.info("flag:{}", flag);
                if (!flag) {
                    //包含不允许移除
                    zeroErrorList.add(number);
                }  else  {
                    //允许移除
                    oidMap.put(strId, relId);
                }
            } else {
                errorList.add(number);
            }
        }
        JF_LOGGER.info("errorList:{}", errorList);
        JF_LOGGER.info("oidMap:{}", oidMap);
        JF_LOGGER.info("oidECRMap:{}", oidECRMap);
        JF_LOGGER.info("zeroErrorList:{}", zeroErrorList);

        String errorMsg = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.common.RemoveMbomPartChairErrorMsg");
        String error1Msg = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.common.RemoveMbomPartZeroErrorMsg");
        JF_LOGGER.info("errorMsg:{}", errorMsg);
        String mess = DomainConstants.EMPTY_STRING;
        if (!errorList.isEmpty()) {
            mess = String.format(errorMsg, errorList.join(","));
        }
        if (!zeroErrorList.isEmpty()) {
            mess += UIUtil.isNullOrEmpty(mess) ? "" : ",";
            mess += String.format(error1Msg, zeroErrorList.join(","));
        }
        if (UIUtil.isNotNullAndNotEmpty(mess)) {
%>
<script language="Javascript">
    alert("<%=mess%>");
</script>
<%
            return;
        }

        //选择的数据均可以移除
        DomainObject object = DomainObject.newInstance(context);
        Iterator<Map.Entry<String, String>> iterator = oidMap.entrySet().iterator();
        StringList relIdList = new StringList();
        //开始遍历数据  需要找到mbom结构对应的零件  根据零件找到他所在的ecr,判断ecr是否在改项目中，如果在需要将ecr设置为未同步，然后移除该mbom结构
    try {
        ContextUtil.pushContext(context);
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String oid = (String) entry.getKey();
            String rid = (String) entry.getValue();
            if (oidECRMap.containsKey(oid)) {
                String ecrId = oidECRMap.get(oid);
                object.setId(ecrId);
                object.setAttributeValue(context, "JF_IsSyncMBOM", "No");
            }
            relIdList.add(rid);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }finally {
        ContextUtil.popContext(context);
    }

    JF_LOGGER.info("relIdList:{}", relIdList);
    if (!relIdList.isEmpty()) {
            // 移除成功
            DomainRelationship.disconnect(context, relIdList.toStringArray());
            errorMsg = ComponentsUtil.i18nStringNow("emxComponents.PartList.RemovePartSuccess", request.getHeader("Accept-Language"));
%>
<script language="Javascript">
    alert("<%=errorMsg%>");
    var refreshURL = window.parent.location.href;
    window.parent.location.href = refreshURL;
</script>
<%
            return;
        }
    }catch (Exception e) {
        e.printStackTrace();
    }
%>
