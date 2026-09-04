import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.matrixone.apps.domain.DomainConstants.SELECT_ID;
import static com.matrixone.apps.domain.DomainConstants.SELECT_LEVEL;

public class JF_Library_mxJPO {
    private static final Logger _logger = LoggerFactory.getLogger(JF_Library_mxJPO.class);

    public MapList getPartition(Context context, String[] args)throws Exception{
        Map argsMap = JPO.unpackArgs(args);
        String strPartType = (String) argsMap.get("typeName");
        _logger.info("strPartType:{}",strPartType);
        // add by chenyan 新增判断登录人是否包含标准件管理员
        String strLoginUser = context.getUser();
        String strMQLRes = MqlUtil.mqlCommand(context, "print role $1 select person dump ;", true, new String[]{"JfStandardAdmin"});
        String[] splitPerson = strMQLRes.trim().split(",");
        StringBuilder sbWhere = new StringBuilder();
        StringList boSel = JF_Util_mxJPO.basicBolistSel();
        boSel.add(DomainConstants.SELECT_ID);
        boSel.add(DomainConstants.SELECT_DESCRIPTION);
        boSel.add(DomainConstants.SELECT_NAME);
        boSel.add("attribute[Title]");
        StringList relSelect = new StringList();
        relSelect.add("from.id");
        MapList classMapList = null;

        //  标准件管理员才有GF-标准件库
        if (UIUtil.isNullOrEmpty(strPartType)){
            boolean hasRole = StringList.create(splitPerson).contains(strLoginUser);
            String strLibTitle = "partitionLibrary.NotContainStandardTitle";
            if (hasRole){
                strLibTitle = "partitionLibrary.ContainStandardTitle";
            }
            Properties properties = JF_Util_mxJPO.readPageObject(context,"JFJDConfig");
            String libraryName = properties.getProperty(strLibTitle);

            sbWhere.append("attribute[Title]");
            sbWhere.append( " matchlist '") ;
            sbWhere.append(libraryName);
            sbWhere.append("'");
            sbWhere.append(" ','");
        }else {
            sbWhere.append("attribute[Title]~~'G");
            sbWhere.append(strPartType);
            sbWhere.append("*'");
        }

        MapList librarys = DomainObject.findObjects(context,"General Library","*",sbWhere.toString(),boSel);
        for(int i=0; i<librarys.size(); i++){
            Map library = (Map)librarys.get(i);
            library.put("disableSelection","true");
        }
        return librarys;
    }
    public MapList getChild(Context context, String[] args)throws Exception{
        Map paramMap = JPO.unpackArgs(args);
        String objId = (String) paramMap.get("objectId");
        //标识是零件还是总成
        String strTypeName = (String) paramMap.get("typeName");
        _logger.info("strTypeName:{}",strTypeName);
        MapList ObjectList = (MapList) paramMap.get("ObjectList");
        _logger.info("ObjectList:{}",ObjectList);
        String expandLevel = (String) paramMap.get("expandLevel");
        String mode = (String) paramMap.get("Mode");
        if ("All".equals(expandLevel)) {
            expandLevel = "0";
        }
        Properties properties = JF_Util_mxJPO.readPageObject(context,"JFJDConfig");
        //标准库标题
//        String strGFLibraryName = properties.getProperty("partitionLibrary.GF.Title");
//        _logger.info("strGFLibraryName:{}",strGFLibraryName);
        DomainObject object = DomainObject.newInstance(context,objId);
        //第一次就全展开了 所有bo一定是库
        Map boInfoMap = object.getInfo(context, StringList.create(DomainConstants.SELECT_ATTRIBUTE_TITLE,DomainConstants.SELECT_TYPE));
        String strLibTitle = (String) boInfoMap.get(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        String strLibType = (String) boInfoMap.get(DomainConstants.SELECT_TYPE);
        StringList boSel = JF_Util_mxJPO.basicBolistSel();
        boSel.add( DomainConstants.SELECT_ATTRIBUTE_TITLE);
        boSel.add("from[Subclass]");
        MapList classMapList = object.getRelatedObjects(context,"Subclass","*",boSel,null,false,true,(short)0,"","",0);

        //20260825 update by ljr EBOM零件分区按标准件角色过滤标准件分类。
        StringList standardClassIdList = getConfiguredClassIdList(context, "JfStandardPart.GeneralClassId");
        StringList allowedStandardClassIdList = JF_FasteningPiece_mxJPO.getAllowedStandardClassIdsByUser(context, context.getUser());
        String currentStandardClassId = getStandardRootClassId(context, objId, standardClassIdList);
        if (UIUtil.isNotNullAndNotEmpty(currentStandardClassId)
                && !allowedStandardClassIdList.contains(currentStandardClassId)) {
            return new MapList();
        }
        for (int classIndex = 0; classIndex < classMapList.size();) {
            Map classMap = (Map) classMapList.get(classIndex);
            String classId = UIUtil.getValue(classMap, DomainConstants.SELECT_ID);
            if (standardClassIdList.contains(classId) && !allowedStandardClassIdList.contains(classId)) {
                int endOfSubtree = JF_NewECRService_mxJPO.findEndOfSubtree(classMapList, classIndex);
                for (int removeIndex = endOfSubtree - 1; removeIndex >= classIndex; removeIndex--) {
                    classMapList.remove(removeIndex);
                }
                continue;
            }
            classIndex++;
        }

        for(int i=0;i<classMapList.size();i++){
            Map temp = (Map)classMapList.get(i);
            String strClassTitle = (String) temp.get(DomainConstants.SELECT_ATTRIBUTE_TITLE);
            String strClassId = (String) temp.get(DomainConstants.SELECT_ID);
            if ("General Library".equals(strLibType)){
                /************************************** 注释时间 2025/08/18 取消单件和组合件的限制******************************************/
                //标准库下都是零件 不做单件和组合件处理
//                if (!strLibTitle.equals(strGFLibraryName)){
//                    //不为空就是零件
//                    if (UIUtil.isNotNullAndNotEmpty(strTypeName)){
//                        if (JF_PLMConstants_mxJPO.CLASS_TITLE_COMPONENT.equals(strClassTitle)) {
//                            int endOfSubtree = JF_NewECRService_mxJPO.findEndOfSubtree(classMapList, i);
//                            setParentClassInSunClass(classMapList,i,endOfSubtree,"parentClassTitle",strClassTitle);
//                        }
//                    }else {
//                        if (JF_PLMConstants_mxJPO.CLASS_TITLE_ASSEMBLY.equals(strClassTitle)) {
//                            int endOfSubtree = JF_NewECRService_mxJPO.findEndOfSubtree(classMapList, i);
//                            setParentClassInSunClass(classMapList,i,endOfSubtree,"parentClassTitle",strClassTitle);
//                        }
//                    }
//                    String hasChild = (String)temp.get("from[Subclass]");
//                    if(!("FALSE".equalsIgnoreCase(hasChild) && (temp.containsKey("parentClassTitle")))){
//                        temp.put("disableSelection","true");
//                    }
//                }else {
//                    if (UIUtil.isNotNullAndNotEmpty(strTypeName)){
//                        String hasChild = (String)temp.get("from[Subclass]");
//                        if("TRUE".equalsIgnoreCase(hasChild)){
//                            temp.put("disableSelection","true");
//                        }
//                    }else {
//                            temp.put("disableSelection","true");
//                    }
//                }
/************************************** 注释时间 2025/08/18 取消单件和组合件的限制******************************************/
                String hasChild = (String)temp.get("from[Subclass]");
                if("TRUE".equalsIgnoreCase(hasChild)){
                    temp.put("disableSelection","true");
                }
                temp.put("rootLibId",objId);
                temp.put("rootLibTitle",strLibTitle);
            }else {
                Map rootLibMap = getRootLibByClassId(context, boSel, JF_Util_mxJPO.basicRellistSel(), object);
                String strRootLibId = (String) rootLibMap.get(SELECT_ID);
                //如果是点击分类的话直接拿第一次的结果
                List filterList = (List) ObjectList.stream().filter(m -> {
                    Map infoMap = (Map) m;
                    return strRootLibId.equals(infoMap.get(SELECT_ID));
                }).limit(1).collect(Collectors.toList());
//                _logger.info("filterList:{}",filterList);
                //理应会存在
                if (filterList.size() > 0) {
                    Map matchMap = (Map) filterList.get(0);
//                    _logger.info("matchMap:{}",matchMap);
                    MapList allSunNodeMapList = new MapList();
                    getAllSunNodeMapByRootMap(matchMap,allSunNodeMapList);
                    for (int i1 = 0; i1 < allSunNodeMapList.size(); i1++) {
                        Map oldInfoMap = (Map) allSunNodeMapList.get(i1);
                        String  strOldClassId = (String) oldInfoMap.get(SELECT_ID);
                        if (strClassId.equals(strOldClassId)){
                            if (Objects.nonNull(oldInfoMap) && oldInfoMap.containsKey("disableSelection")) {
                                temp.put("disableSelection",oldInfoMap.get("disableSelection"));
                            }
                            break;
                        }
                    }
                }else {
                    String hasChild = (String)temp.get("from[Subclass]");
                    if("TRUE".equalsIgnoreCase(hasChild)){
                        temp.put("disableSelection","true");
                    }
                }
            }

        }
        _logger.info("classMapList:{}",classMapList);
        return classMapList;
    }
    /**
    *
    *@description 设置一个父分类下的所有子分类特殊标识
    *@param classMapList
	*@param startIndex
	*@param endIndex
	*@param strMapKey
	*@param strMapKeyValue
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/7/27 0:44
    */
    public void setParentClassInSunClass(MapList classMapList ,int startIndex ,int endIndex,String strMapKey,String strMapKeyValue){
        for (; startIndex < endIndex; startIndex++) {
            Map temp = (Map)classMapList.get(startIndex);
            temp.put(strMapKey,strMapKeyValue);
        }
    }
    /**
    *
    *@description TODO
    *@param context
	*@param typeSelectList
	*@param relSelectList
	*@param classBo
    *@return java.util.Map
    *@throws
    *@author CHENYAN
    *@date 2025/7/27 2:00
    */
    public static Map getRootLibByClassId(Context context ,StringList typeSelectList ,StringList relSelectList,DomainObject classBo ) throws Exception{
        Map rootNodeMap = null ;
        MapList objectList = classBo.getRelatedObjects(context, "Subclass", // relationship pattern
                "General Library,General Class",                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                true,                                        // to direction
                false,                                        // from direction
                (short) 0,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        objectList.addSortKey(SELECT_LEVEL, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);
        objectList.sort();
        // add by chenyan 新增 创建零件时只能选择单件 创建 总成时只能选择组合件 component(零件) assembly(装配) 2025/07/22
        _logger.info("objectList:{}",objectList);

        if (objectList.size() > 0) {
             rootNodeMap = (Map) objectList.get(0);
        }else {
            //返回本身
            rootNodeMap = classBo.getInfo(context,typeSelectList);
        }
        return rootNodeMap;
    }

    public MapList getAllSunNodeMapByRootMap(Map rootMap,MapList allSunMapList){
        if (Objects.isNull(allSunMapList)){
            allSunMapList = new MapList();
        }
        allSunMapList.add(rootMap);
        if (rootMap.containsKey("children")) {
            List sunList = (List) rootMap.get("children");
            for (int i = 0; i < sunList.size(); i++) {
                Map sunMap = (Map) sunList.get(i);
                getAllSunNodeMapByRootMap(sunMap,allSunMapList);
            }
        }
        return  allSunMapList;
    }

    /**
     * 校验当前用户是否允许在零件分区中选择指定分类。
     * 普通分类不受影响；标准件分类仅允许负责对应分类的标准件工程师选择。
     *
     * @param context
     * @param classId 待选择的分类ID
     * @return boolean 允许选择返回true
     * @throws Exception
     * @author caipan by codex
     * @date 2026/8/25 16:00
     */
    public static boolean isPartitionClassAllowed(Context context, String classId) throws Exception {
        if (UIUtil.isNullOrEmpty(classId)) {
            return false;
        }
        StringList standardClassIdList = getConfiguredClassIdList(context, "JfStandardPart.GeneralClassId");
        String standardRootClassId = getStandardRootClassId(context, classId, standardClassIdList);
        if (UIUtil.isNullOrEmpty(standardRootClassId)) {
            return true;
        }
        StringList allowedStandardClassIdList = JF_FasteningPiece_mxJPO.getAllowedStandardClassIdsByUser(context, context.getUser());
        return allowedStandardClassIdList.contains(standardRootClassId);
    }

    /**
     * 提供给零件分区提交页面调用的标准件分类权限校验入口。
     *
     * @param context
     * @param args 第一个参数为待选择的分类ID
     * @return boolean 允许选择返回true
     * @throws Exception
     * @author caipan by codex
     * @date 2026/8/25 16:00
     */
    public boolean checkPartitionClassAllowed(Context context, String[] args) throws Exception {
        String classId = args != null && args.length > 0 ? args[0] : DomainConstants.EMPTY_STRING;
        return isPartitionClassAllowed(context, classId);
    }

    /**
     * 获取配置项中的分类ID列表。
     *
     * @param context
     * @param configKey JFJDConfig配置项名称
     * @return StringList 分类ID列表
     * @throws Exception
     * @author caipan by codex
     * @date 2026/8/25 16:00
     */
    private static StringList getConfiguredClassIdList(Context context, String configKey) throws Exception {
        String classIds = JF_PublicMethodClass_mxJPO.getBasicUrl(context, new String[]{configKey});
        if (UIUtil.isNullOrEmpty(classIds)) {
            return new StringList();
        }
        return FrameworkUtil.split(classIds, ",");
    }

    /**
     * 获取分类自身或上级路径中命中的标准件根分类ID。
     *
     * @param context
     * @param classId 分类ID
     * @param standardClassIdList 全部标准件根分类ID
     * @return String 命中的标准件根分类ID，非标准件分类返回空
     * @throws Exception
     * @author caipan by codex
     * @date 2026/8/25 16:00
     */
    private static String getStandardRootClassId(Context context, String classId, StringList standardClassIdList) throws Exception {
        if (UIUtil.isNullOrEmpty(classId) || standardClassIdList.isEmpty()) {
            return DomainConstants.EMPTY_STRING;
        }
        if (standardClassIdList.contains(classId)) {
            return classId;
        }
        DomainObject classObject = DomainObject.newInstance(context, classId);
        MapList parentClassMapList = classObject.getRelatedObjects(context,
                "Subclass",
                "General Library,General Class",
                new StringList(DomainConstants.SELECT_ID),
                null,
                true,
                false,
                (short) 0,
                "",
                "",
                (short) 0);
        for (Object parentClassObj : parentClassMapList) {
            Map parentClassMap = (Map) parentClassObj;
            String parentClassId = UIUtil.getValue(parentClassMap, DomainConstants.SELECT_ID);
            if (standardClassIdList.contains(parentClassId)) {
                return parentClassId;
            }
        }
        return DomainConstants.EMPTY_STRING;
    }
}
