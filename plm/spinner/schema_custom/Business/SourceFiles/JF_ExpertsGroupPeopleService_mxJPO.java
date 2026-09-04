import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.matrixone.apps.domain.DomainConstants.*;

/**
 * @author liuxg
 * @version v1.0.0
 * 创建时间：2025/10/09 16:24
 * @description  专家组处理类
 */
public class JF_ExpertsGroupPeopleService_mxJPO {

    private static final Logger _logger =  LoggerFactory.getLogger(JF_ExpertsGroupPeopleService_mxJPO.class);

    /**
     * @Author Liuxg
     * @Description 获取所有关联的专家
     * @Date 2025/10/12 21:15
     * @Param [args]
     * @return com.matrixone.apps.domain.util.MapList
    **/
    public MapList getAllExpertsGroupPeople(Context context,String[]args)throws Exception{
        MapList mapList=new MapList();
        try {
            StringList selList = JF_Util_mxJPO.basicBolistSel();
            selList.add(DomainConstants.SELECT_DESCRIPTION);
            StringList relList = JF_Util_mxJPO.basicRellistSel();
            //专业
            relList.add("attribute[JF_ExpertsDocSpecialty]");
            //文档类型
            relList.add("attribute[JF_ExpertsDocType]");
            relList.add(JF_PLMConstants_mxJPO.Select_Attr_JF_RelDescription);
            Map paramsMap = (Map) JPO.unpackArgs(args);
//            _logger.info("paramsMap---->{}",paramsMap);
            String projectid = (String) paramsMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            String parentId = UIUtil.getValue(paramsMap, JF_PLMConstants_mxJPO.STRING_PARENTOID);
            _logger.info("projectid---->{}",projectid);
            DomainObject projectObj = DomainObject.newInstance(context, projectid);

            //20260805 update by ljr 项目页面先返回专家组，点击专家组后复用本方法查询关联人员，实现按需展开。
            String sourceType = projectObj.getInfo(context, DomainConstants.SELECT_TYPE);
            boolean isExpertsGroupSource = JF_PLMConstants_mxJPO.TYPE_JFExpertsGroup.equals(sourceType);
            String targetType = isExpertsGroupSource
                    ? DomainConstants.TYPE_PERSON
                    : JF_PLMConstants_mxJPO.TYPE_JFExpertsGroup;

             mapList = projectObj.getRelatedObjects(context,
                     "JFProject2ExpertsGroup", //pattern to match relationships
                     targetType, //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                    EMPTY_STRING, //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0);//limit

            if (!isExpertsGroupSource) {
                // 初始项目列表只给专家组增加展开标识；人员行由点击“+”后按需查询，不在首次加载时返回。
                for (Object result : mapList) {
                    ((Map) result).put("hasChildren", "true");
                }
            } else if (UIUtil.isNotNullAndNotEmpty(parentId)
                    && DomainObject.newInstance(context, parentId).isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE)) {
                //20260806 update by ljr 项目页展开专家组时，仅禁选人员行；项目专家组及专家组对象页人员保持可选。
                for (Object result : mapList) {
                    Map resultMap = (Map) result;
                    if (DomainConstants.TYPE_PERSON.equals(UIUtil.getValue(resultMap, DomainConstants.SELECT_TYPE))) {
                        resultMap.put("disableSelection", "true");
                    }
                }
            }
            _logger.info("mapList.size---->{}"+mapList);
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }

        return mapList;
    }


    /**
     * @Author Liuxg
     * @Description 检查库管理员角色权限
     * @Date 2025/10/12 21:36
     * @Param [context, args]
     * @return boolean
    **/
    public boolean checkjfLibAdminAccess(Context context,String []args)throws  Exception{
        boolean flag=false;
        try {
            Vector assignments = PersonUtil.getAssignments(context, context.getUser());
            if (assignments.contains("jfLibAdmin")) {
                flag = Boolean.TRUE;
            } else {
                flag = Boolean.FALSE;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return flag;
//        return true;
    }



    public Vector getProjectUserDep(Context context, String[] args)
            throws Exception {
        _logger.info("getProjectUserDep+++++++++start");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        _logger.info("getProjectUserDep+++++++++>{}"+objectList);
        Vector retVector = new Vector();
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        selList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        StringList relList = JF_Util_mxJPO.basicRellistSel();
        String role = "";
        if (objectList != null && objectList.size() > 0) {
            Map objectMap = null;
            DomainObject domainObject = DomainObject.newInstance(context);
            for (int i = 0; i < objectList.size(); i++) {
                String displayName = "";
                objectMap = (Map) objectList.get(i);
                String strObjId = (String) objectMap.get("id");
                String userStr = (String) objectMap.get("name");
                String objectType = UIUtil.getValue(objectMap, DomainConstants.SELECT_TYPE);

                //20260805 update by ljr 项目关联专家组时部门列返回空，仅人员行查询所属部门。
                if (DomainConstants.TYPE_PERSON.equals(objectType)
                        && UIUtil.isNotNullAndNotEmpty(strObjId)
                        && UIUtil.isNotNullAndNotEmpty(userStr)) {
                    String personId = PersonUtil.getPersonObjectID(context, userStr);
                    domainObject.setId(personId);
                    MapList DepartmentList = domainObject.getRelatedObjects(context,
                            DomainConstants.RELATIONSHIP_MEMBER, //pattern to match relationships
                            DomainConstants.TYPE_DEPARTMENT, //pattern to match types
                            selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                            relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                            true, //get To relationships
                            false, //get From relationships
                            (short) 1, //the number of levels to expand, 0 equals expand all.
                            DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
                            DomainConstants.EMPTY_STRING, //where clause to apply to relationship, can be empty ""
                            (short) 10 //limit
                    );
                    for (int k = 0; k < DepartmentList.size(); k++) {
                        Map map = (Map) DepartmentList.get(k);
                        if (UIUtil.isNotNullAndNotEmpty(displayName)) {
                            displayName = displayName + "," + UIUtil.getValue(map, DomainConstants.SELECT_ATTRIBUTE_TITLE);
                        } else {
                            displayName = UIUtil.getValue(map, DomainConstants.SELECT_ATTRIBUTE_TITLE);
                        }
                    }
                }
                retVector.add(displayName);
            }
        }
        return retVector;
    }



    /**
     * @Author Liuxg
     * @Description 获取专家组表格编辑权限
     * @Date 2025/10/13 3:48
     * @Param [context, args]
     * @return matrix.util.StringList
    **/
    public StringList getExpertsGroupPeopleEditTableAccess(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strLoginUser = context.getUser();
        MapList objectList = (MapList) programMap.get("objectList");
        StringList editAccessList = new StringList(objectList.size());

        boolean access=checkjfLibAdminAccess(context,args);

        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
            String objectType = UIUtil.getValue(objectMap, DomainConstants.SELECT_TYPE);
            //20260805 update by ljr 文档类型和专业只属于专家组与人员的关系，项目关联专家组时不允许编辑。
            boolean isEdit = access && DomainConstants.TYPE_PERSON.equals(objectType);
            editAccessList.add(String.valueOf(isEdit));
        }
        return editAccessList;
    }


    public void updateExpertsGroupPeopleTable(Context context, String[] args) throws Exception {
        _logger.info("updateExpertsGroupPeopleTable:-----------");
        _logger.info("updateExpertsGroupPeopleTable:-----------");
        _logger.info("updateExpertsGroupPeopleTable:-----------");
        Map paramsMap = (Map) JPO.unpackArgs(args);
        Map columnMap = (Map) paramsMap.get(JF_PLMConstants_mxJPO.STRING_COLUMNMAP);
        Map requestMap = (Map) paramsMap.get("requestMap");
        String strTableName = (String) requestMap.get("selectedTable");
        String strParentOID = (String) requestMap.get("parentOID");
        String strAttrName = (String) columnMap.get(DomainConstants.SELECT_NAME);
        HashMap paramMap = (HashMap) paramsMap.get(JF_PLMConstants_mxJPO.STRING_PARAMMAP);
        String strObjectId = (String) paramMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
        String strNewValue = (String) paramMap.get("New Value");
        String strRelId = (String) paramMap.get("relId");
        _logger.info("strTableName:{}", strTableName);
        _logger.info("strParentOID:{}", strParentOID);
        _logger.info("strAttrName:{}", strAttrName);
        _logger.info("strObjectId:{}", strObjectId);
        _logger.info("strRelId:{}", strRelId);
        _logger.info("strNewValue:{}", strNewValue);

        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            DomainRelationship relationship=DomainRelationship.newInstance(context,strRelId);
            if("JF_ExpertsDocType".equals(strAttrName)){
                String[] newobjids=strNewValue.split("\\|");
                String titles="";
                for(String objid:newobjids){
                    _logger.info("objid:{}", objid);
                    DomainObject domainObject=DomainObject.newInstance(context,objid);
                    String title= domainObject.getAttributeValue(context,"Title");
                    if(UIUtil.isNullOrEmpty(titles)){
                        titles=title;
                    }else {
                        titles=titles+","+title;
                    }
                }

                relationship.setAttributeValue(context, strAttrName,titles);
            } else if (JF_PLMConstants_mxJPO.Attr_JF_RelDescription.equals(strAttrName)) {
                //20260805 update by ljr 说明列编辑时更新当前人员与专家组之间的关系属性，空值按清空处理。
                relationship.setAttributeValue(
                        context,
                        JF_PLMConstants_mxJPO.Attr_JF_RelDescription,
                        UIUtil.isNullOrEmpty(strNewValue) ? DomainConstants.EMPTY_STRING : strNewValue);
            } else {


                StringList slSetValue=FrameworkUtil.split(strNewValue,",");
                AttributeList attList=new AttributeList();
                AttributeType attCheckListItemSet = new AttributeType("JF_ExpertsDocSpecialty");
                Attribute att =new Attribute(attCheckListItemSet,slSetValue);
                attList.add(att);
                relationship.setAttributes(context,attList);

            }


            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw new RuntimeException(e);
        }finally {
            ContextUtil.popContext(context);
        }
    }


    public StringList getJF_ExpertsDocType(Context context, String[] args) {
        StringList vector = new StringList();
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            _logger.info("objectList---->"+objectList);

            for (int i = 0; i < objectList.size(); i++) {
                Map map = (Map) objectList.get(i);
                //20260805 update by ljr 项目关联专家组行不查询人员关系上的文档类型。
                if (!DomainConstants.TYPE_PERSON.equals(UIUtil.getValue(map, DomainConstants.SELECT_TYPE))) {
                    vector.add(EMPTY_STRING);
                    continue;
                }
                String relid = (String) map.get("id[connection]");
                DomainRelationship domainRelationship=DomainRelationship.newInstance(context,relid);
                String JF_ExpertsDocType=domainRelationship.getAttributeValue(context,"JF_ExpertsDocType");

                String a = JF_ExpertsDocType;
                vector.add(a);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return vector;
    }

    public StringList getJF_ExpertsDocSpecialty(Context context, String[] args) {
        StringList vector = new StringList();
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            _logger.info("objectList---->"+objectList);

            for (int i = 0; i < objectList.size(); i++) {
                Map map = (Map) objectList.get(i);
                //20260805 update by ljr 项目关联专家组行不查询人员关系上的专业。
                if (!DomainConstants.TYPE_PERSON.equals(UIUtil.getValue(map, DomainConstants.SELECT_TYPE))) {
                    vector.add(EMPTY_STRING);
                    continue;
                }
                String relid = (String) map.get("id[connection]");
                DomainRelationship domainRelationship=DomainRelationship.newInstance(context,relid);
                String JF_ExpertsDocSpecialty=domainRelationship.getAttributeValue(context,"JF_ExpertsDocSpecialty");
                _logger.info("JF_ExpertsDocSpecialty---->"+JF_ExpertsDocSpecialty);
                String aa="";
                if(UIUtil.isNotNullAndNotEmpty(JF_ExpertsDocSpecialty)){
                    if(JF_ExpertsDocSpecialty.contains(",")){
                        String newStr= JF_ExpertsDocSpecialty.substring(1,JF_ExpertsDocSpecialty.length()-1);
                        _logger.info("newStr---->"+newStr);
                        String []strs=newStr.split(",");
                        for(String str:strs){
                            if(UIUtil.isNotNullAndNotEmpty(str)){
                                str=str.trim();
                                String a= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Range.JF_ExpertsDocSpecialty."+str, context.getLocale());
                                if(UIUtil.isNullOrEmpty(aa)){
                                    aa=a;
                                }else {
                                    aa=aa+","+a;
                                }
                            }
                        }
                    }else {
                         aa= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Range.JF_ExpertsDocSpecialty."+JF_ExpertsDocSpecialty, context.getLocale());

                    }

                }

                vector.add(aa);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return vector;
    }


    public Map getJF_ExpertsDocSpecialtyRanges(Context context, String[] args) throws Exception {
        _logger.info("-------------------------- JF_ExpertsDocSpecialty begin ------------------------------------------");
        HashMap<Object, Object> res = new HashMap<>();
        StringList ranges = FrameworkUtil.getRanges(context, "JF_ExpertsDocSpecialty");
        StringList nlsRanges = EnoviaResourceBundle.getAttrRangeI18NStringList(context, "JF_ExpertsDocSpecialty", ranges, context.getLocale().toString());
        res.put("field_choices", ranges);
        res.put("field_display_choices", nlsRanges);
        _logger.info("res:{}", res);
        _logger.info("-------------------------- JF_ExpertsDocSpecialty end ------------------------------------------");
        return res;
    }


    /**
     * @Author Liuxg
     * @Description 专家搜索条件
     * @Date 2025/10/16 16:15
     * @Param [context, args]
     * @return java.lang.String
    **/
    public StringList getExpertsGroupPerson(Context context, String[] args) throws Exception{
        StringList resultList = new StringList();

        try {
            ContextUtil.pushContext(context);
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);
            String projectId = (String) paramMap.get("objectId");

            DomainObject projectObj=DomainObject.newInstance(context,projectId);
            resultList= projectObj.getInfoList(context,"from[JFProject2ExpertsGroup].to.id");
        } catch (Exception e) {
            e.printStackTrace();

        }finally {
            ContextUtil.popContext(context);
        }
        _logger.info("resultList:{}", resultList.toString());
        return resultList;
    }

    /**
     * 首页 技术文档专家组管理 按钮列出 专家组列表
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return com.matrixone.apps.domain.util.MapList
     * @date 2026/8/4 13:24
     * @description
     */
    public MapList getTechDocExpertGroupList(Context context, String[] args) throws Exception{
        MapList applicationList = DomainObject.findObjects(context,JF_PLMConstants_mxJPO.TYPE_JFExpertsGroup, "*", "", JF_Util_mxJPO.basicBolistSel());
        applicationList.sort(DomainConstants.SELECT_ORIGINATED, "descending", "date");
        return applicationList;
    }

    /**
     * 创建技术文档专家组，名称使用系统自动命名，Title和Description保持为空。
     * @param context 系统上下文
     * @param args 请求参数
     * @return java.lang.String 新建专家组对象ID
     * @author LIUJR
     * @throws Exception 创建失败时抛出异常
     * @date 2026/8/4
     * @description
     */
    public String createTechDocExpertGroup(Context context, String[] args) throws Exception {
        try {
            ContextUtil.startTransaction(context, true);

            // 使用已配置的自动命名规则和专家组策略直接创建对象，不设置Title和Description。
            String expertGroupId = FrameworkUtil.autoName(
                    context,
                    "type_JFExpertsGroup",
                    "policy_JFExpertsGroup");
            if (UIUtil.isNullOrEmpty(expertGroupId)) {
                throw new FrameworkException("Failed to create expert group.");
            }
            ContextUtil.commitTransaction(context);
            _logger.info("Create technical document expert group success, objectId:{}", expertGroupId);
            return expertGroupId;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            _logger.error("Create technical document expert group failed", e);
            throw e;
        }
    }

    /**
     * 查询当前技术文档专家组关联的项目，用于专家组Tree中的关联项目页面。
     * @param context 系统上下文
     * @param args 页面参数，包含专家组objectId
     * @return com.matrixone.apps.domain.util.MapList 关联项目列表
     * @author LIUJR
     * @throws Exception 查询失败时抛出异常
     * @date 2026/8/4
     * @description
     */
    public MapList getTechDocExpertGroupRelatedProjectList(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String expertGroupId = UIUtil.getValue(paramMap, JF_PLMConstants_mxJPO.STRING_OBJECTID);
        if (UIUtil.isNullOrEmpty(expertGroupId)) {
            return new MapList();
        }

        // JFProject2ExpertsGroup由项目指向专家组，这里从专家组端反向查询，只返回关联的项目对象。
        return DomainObject.newInstance(context, expertGroupId).getRelatedObjects(
                context,
                "JFProject2ExpertsGroup",
                DomainConstants.TYPE_PROJECT_SPACE,
                JF_Util_mxJPO.basicBolistSel(),
                new StringList(),
                true,
                false,
                (short) 1,
                DomainConstants.EMPTY_STRING,
                DomainConstants.EMPTY_STRING,
                0);
    }

    /**
     * 生成专家组人员Table字段，并移除Form单元格为嵌入字段预留的边框和间距。
     * @param context 系统上下文
     * @param args Form字段参数
     * @return java.lang.String 专家组人员Table页面HTML
     * @author LIUJR
     * @throws Exception 页面生成失败时抛出异常
     * @date 2026/8/5
     * @description
     */
    public String getTechDocExpertGroupPeopleTableField(Context context, String[] args) throws Exception {
        // 继续调用OOTB fieldURL生成iframe，避免复制公共Table字段的拼装逻辑。
        String tableFieldHtml = JPO.invoke(
                context,
                "emxGenericFields",
                null,
                "fieldURL",
                args,
                String.class);

        // 仅清理当前专家组Table所在Form单元格的间距，不影响系统内其他fieldURL字段。
        StringBuilder result = new StringBuilder(tableFieldHtml);
        result.append("<script type='text/javascript'>")
                .append("$(document).ready(function(){")
                .append("var frame=$('#frameFieldExpertsTable');")
                .append("frame.css({'display':'block','width':'100%','height':'100%','padding':'0px','margin':'0px','border':'none'});")
                .append("frame.parent().css({'padding':'0px','margin':'0px'});")
                .append("frame.closest('td').css({'padding':'0px','border':'none'});")
                .append("});")
                .append("</script>");
        return result.toString();
    }

    /**
     * 获取专家组Table说明列：人员显示关系说明，专家组显示对象说明。
     * @param context 系统上下文
     * @param args Table列参数
     * @return matrix.util.StringList 每行对应的说明
     * @author LIUJR
     * @throws Exception 参数解析失败时抛出异常
     * @date 2026/8/5
     * @description
     */
    public StringList getExpertsGroupRelationDescription(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        StringList result = new StringList(objectList == null ? 0 : objectList.size());
        if (objectList == null) {
            return result;
        }

        // 查询程序已一次性返回对象类型、对象说明和关系说明，此处不再逐行访问数据库。
        for (Object item : objectList) {
            Map objectMap = (Map) item;
            String objectType = UIUtil.getValue(objectMap, DomainConstants.SELECT_TYPE);
            String description = DomainConstants.TYPE_PERSON.equals(objectType)
                    ? UIUtil.getValue(objectMap, JF_PLMConstants_mxJPO.Select_Attr_JF_RelDescription)
                    : UIUtil.getValue(objectMap, DomainConstants.SELECT_DESCRIPTION);
            result.add(description);
        }
        return result;
    }
}
