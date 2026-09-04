import com.aspose.pdf.operators.EX;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProjectSpace;
import jakarta.enterprise.inject.New;
import javassist.compiler.ast.StringL;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.matrixone.apps.domain.DomainConstants.*;

/**
 * @ClassName JF_ColorPart_mxJPO
 * @Author: LIUJR
 * @CreateDate: 2024/11/25 15:00
 * @UpdateRemark:
 * @Version: 1.0
 * @Description:
 */
    public class JF_ColorPart_mxJPO  implements JF_PLMConstants_mxJPO {
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_ColorPart_mxJPO.class);
    private static final String SUITE_KEY = "emxComponentsStringResource";
    private static final String SUITE_KEY_FRAMEWORK = "emxFrameworkStringResource";

    private static final String REL_PROJECT_COLORMATRIX = "JFProject2JFColorMatrix";
    private static final String REL_JFColorMatrix2JFColorGroup = "JFColorMatrix2JFColorGroup";
    private static final String REL_JFColorGroup2JFColorStyle = "JFColorGroup2JFColorStyle";
    private static final String REL_JFVPMReference2JFColorMatrix = "JFVPMReference2JFColorMatrix";
    private static final String TYPE_COLORMATRIX = "JFColorMatrix";
    private static final String TYPE_COLOR_GROUP = "JFColorGroup";
    private static final String ATTRIBUTE_COLOR_GROUP_NAME = "JF_ColorGroupName";
    private static final String TYPE_COLOR_STYLE = "JFColorStyle";
    private static final String POLICY_COLORMATRIX = "JFColorMatrix";
    private static final String POLICY_COLOR_STYLE  = "JFColorStyle";
    private static final String POLICY_COLOR_GROUP = "JFColorGroup";
    private static final String POLICY_POLICY_COLORMATRIX = "policy_JFColorMatrix";
    private static final String TYPE_TYPE_COLORMATRIX = "type_JFColorMatrix";
    private static final String SELECT_ATTRIBUTE = "attribute[%s]" ;

    //颜色分组自动命名中间标识
    private static final String FLAG_COLOR_GROUP_NAME_NUMBER = "G" ;
    //颜色风格自动命名中间标识
    private static final String FLAG_COLOR_STYLE_NAME_NUMBER = "S" ;

    private static final StringList busSelectsList = new StringList();
    private static final StringList relSelectsList = new StringList();
    private static final StringList relSelList = new StringList();

    static {
        busSelectsList.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
        busSelectsList.add(DomainConstants.SELECT_DESCRIPTION);
        busSelectsList.add(DomainConstants.SELECT_REVISION);
        busSelectsList.add(DomainConstants.SELECT_NAME);
        busSelectsList.add(DomainConstants.SELECT_ID);
        busSelectsList.add(DomainConstants.SELECT_TYPE);
        busSelectsList.add(DomainConstants.SELECT_CURRENT);
        busSelectsList.add(DomainConstants.SELECT_OWNER);
        busSelectsList.add(DomainConstants.SELECT_PROJECT);
        busSelectsList.add(DomainConstants.SELECT_ORGANIZATION);
        busSelectsList.add(DomainConstants.SELECT_ORIGINATED);
        relSelectsList.add(DomainRelationship.SELECT_ID);
        relSelectsList.add(SELECT_JF_InternalColorCode);
        relSelectsList.add(SELECT_ATTR_JF_ColorStyleName);
        relSelList.add(SELECT_JF_InternalColorCode);
        relSelList.add(SELECT_ATTR_JF_ColorStyleName);
        relSelList.add(SELECT_ATTR_JF_CustormColorCode);
    }


    /**
     * 创建项目颜色矩阵 后置jpo 设置属性 关联关系
     * @description
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2024/12/30 13:03
     */
    public void createJFColorMatrixProcess(Context context, String[] args) throws Exception {
        try {
            JF_LOGGER.info("method:createJFColorMatrixProcess start...");
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) paramsMap.get(STRING_REQUESTMAP);
            Map paramMap = (Map) paramsMap.get(STRING_PARAMMAP);
            String objectId = (String) paramMap.get(STRING_OBJECTID);
            String parentOID = (String) requestMap.get(STRING_PARENTOID);
            if (UIUtil.isNullOrEmpty(objectId)) {
                objectId = (String) paramMap.get("newObjectId");
            }
            DomainObject objectColorMatrix = DomainObject.newInstance(context, objectId);
            Map projectPersonAndProjectRole = JF_SignTask_mxJPO.getProjectPersonAndProjectRole(context, parentOID, "PS", DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
            MapList mapList = (MapList) projectPersonAndProjectRole.get("mapList");
            StringList list = (StringList) mapList.stream().map(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, SELECT_NAME);
            }).collect(Collectors.toCollection(StringList::new));
            JF_LOGGER.info("@@@@@list:{}", list.toString());
            JF_LOGGER.info("@@@@@user:{}", context.getUser().toString());
            if (list.contains(context.getUser().toString())) {
                ContextUtil.pushContext(context);
                objectColorMatrix.addFromObject(context, new RelationshipType(REL_PROJECT_COLORMATRIX), parentOID);
                ContextUtil.popContext(context);
            } else {
                objectColorMatrix.addFromObject(context, new RelationshipType(REL_PROJECT_COLORMATRIX), parentOID);
            }
            ContextUtil.startTransaction(context, true);
            //创建颜色分组  ->  新建矩阵完成后，系统自动将UA和NA的分组进行初始化;
            //调用方法
            String colorMatrixName = objectColorMatrix.getInfo(context, SELECT_NAME);
            MapList colorGroupInitMapList = JF_PublicMethodClass_mxJPO.getJFCostChangeHistoryTemplateMapping(context, "ColorGroupInit");
            colorGroupInitMapList.addSortKey("Number", ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            colorGroupInitMapList.sort();
            Iterator iterator = colorGroupInitMapList.iterator();
            Policy policy = new Policy(POLICY_COLOR_GROUP);
            String colorGroupRev = policy.getFirstInSequence(context);
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String jfColorGroupName = UIUtil.getValue(map, ATTRIBUTE_COLOR_GROUP_NAME);
                String title = UIUtil.getValue(map, DomainConstants.ATTRIBUTE_TITLE);
                String number = UIUtil.getValue(map, "Number");
                String colorGroupName = colorMatrixName + "G" + number;
                Map attributeMap = new HashMap();
                attributeMap.put(ATTRIBUTE_COLOR_GROUP_NAME, jfColorGroupName);
                attributeMap.put(DomainConstants.ATTRIBUTE_TITLE, title);
                DomainObject domainObject = DomainObject.newInstance(context);
                domainObject.createObject(context, TYPE_COLOR_GROUP, colorGroupName, colorGroupRev, POLICY_COLOR_GROUP, context.getVault().getName());
                domainObject.setAttributeValues(context, attributeMap);
                domainObject.addFromObject(context, new RelationshipType(REL_JFColorMatrix2JFColorGroup), objectId);
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            throw e;
        }
        JF_LOGGER.info("method:createJFDataOutSourceApply end...");
    }


    /**
    * 获取项目的颜色矩阵
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2024/12/30 14:47
    * @description
    */
    public MapList getColorMatrix(Context context, String[] args) throws Exception{
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
            DomainObject objectPart = DomainObject.newInstance(context, strObjectId);
            StringList colorPartIdList = objectPart.getInfoList(context, "from[JFProject2JFColorMatrix].to.id");
            JF_LOGGER.info("ColorMatrix:{}", colorPartIdList);
            MapList mlPartInfoList = DomainObject.getInfo(context, colorPartIdList.toStringArray(), busSelectsList);
            JF_LOGGER.info("mlPartInfoList:{}", mlPartInfoList);
            return mlPartInfoList;
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.popContext(context);
        }
        return new MapList();
    }


    /**
    * 创建颜色矩阵权限
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2025/2/13 10:39
    * @description
    */
    public Boolean createJFColorMatrixAccess(Context context, String[] args) throws Exception {
        Boolean flag = Boolean.TRUE;
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            JF_Snapshot_mxJPO jfSnapshotMxJPO = new JF_Snapshot_mxJPO();
            Boolean snapshotAccess = jfSnapshotMxJPO.createSnapshotAccess(context, args);
            ContextUtil.pushContext(context);
            String strObjectId = (String) paramsMap.get(STRING_OBJECTID);
            DomainObject objectProject = DomainObject.newInstance(context, strObjectId);
            StringList colorPartIdList = objectProject.getInfoList(context, "from[JFProject2JFColorMatrix].to.id");
            if (colorPartIdList.size() > 0) {
                flag = Boolean.FALSE;
            } else {
                //判断 是否是项目内的研发人员
                JF_LOGGER.info("snapshotAccess:{}", snapshotAccess);
                flag = snapshotAccess;
            }
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.popContext(context);
        }
        return flag;
    }

    /**
    * 颜色矩阵升版权限判定
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2025/1/6 10:15
    * @description
    */
    public Boolean colorMatrixUpgradeVersionAccess(Context context, String[] args) {
        Boolean flag = Boolean.TRUE;
        try{
            Map params = JPO.unpackArgs(args);
            String strObjectId = (String) params.get("objectId");
            MapList projectSpaceNewColorMatrix = getProjectSpaceNewColorMatrix(context, args);
            if (projectSpaceNewColorMatrix.isEmpty()) {
                flag = Boolean.FALSE;
            } else {
                Map map = (Map) projectSpaceNewColorMatrix.get(0);
                String current = UIUtil.getValue(map, SELECT_CURRENT);
                String owner = UIUtil.getValue(map, SELECT_OWNER);
                // add by chenyan 其它人也可以升版 2025/02/17
//                if ("Release".equalsIgnoreCase(current) && context.getUser().equalsIgnoreCase(owner)) {
                //项目成员可以升版
                DomainObject projectObject = DomainObject.newInstance(context, strObjectId);
                //拿取项目的成员以及关系属性project role
                MapList mapList = projectObject.getRelatedObjects(
                        context,
                        DomainRelationship.RELATIONSHIP_MEMBER,
                        DomainConstants.TYPE_PERSON,
                        new StringList(SELECT_NAME),
                        new StringList(),
                        false,
                        true,
                        (short) 1, // recursion level
                        "", //object where clause
                        "", //relationship where clause
                        0
                );
                StringList projectRoleList = (StringList) mapList.stream().map(m -> {
                    Map map1= (Map) m;
                    return UIUtil.getValue(map1, DomainConstants.SELECT_NAME);
                }).collect(Collectors.toCollection(StringList::new));
                if ("Release".equalsIgnoreCase(current) && projectRoleList.contains(context.getUser())) {
                    flag = Boolean.TRUE;
                } else {
                    flag = Boolean.FALSE;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
        }
        return flag;
    }

    /**
     * 颜色矩阵升版权限判定
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.Boolean
     * @date 2025/1/6 10:15
     * @description
     */
    public Boolean colorMatrixEditAccess(Context context, String[] args) {
        Boolean flag = Boolean.TRUE;
        try{
            Map params = JPO.unpackArgs(args);
            String strObjectId = (String) params.get("objectId");
            DomainObject domainObject = DomainObject.newInstance(context, strObjectId);
            String psId = domainObject.getInfo(context, "to[JFProject2JFColorMatrix].from.id");
            if (UIUtil.isNullOrEmpty(psId)) {
                flag = Boolean.FALSE;
                return flag;
            }
            Map hashMap = new HashMap();
            hashMap.put("objectId", psId);
            hashMap.put("busWhere", "");
            MapList projectSpaceNewColorMatrix = getProjectSpaceNewColorMatrix(context, JPO.packArgs(hashMap));
            if (projectSpaceNewColorMatrix.isEmpty() || projectSpaceNewColorMatrix.size() == 1) {
                flag = Boolean.FALSE;
            } else {
                Map map = (Map) projectSpaceNewColorMatrix.get(0);
                String current = UIUtil.getValue(map, SELECT_CURRENT);
                String id = UIUtil.getValue(map, SELECT_ID);
                if (id.equalsIgnoreCase(strObjectId) && "Create".equalsIgnoreCase(current)) {
                    //最新版本
                    flag = Boolean.TRUE;
                } else {
                    flag = Boolean.FALSE;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = Boolean.FALSE;
        }
        return flag;
    }


    /**
    * 获取项目的颜色矩阵  按照创建时间倒序排序
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2025/1/6 10:44
    * @description
    */
    public MapList getProjectSpaceNewColorMatrix(Context context, String[] args) {
        MapList mapList = new MapList();
        try{
            Map params = JPO.unpackArgs(args);
            String strObjectId = (String) params.get("objectId");
            String busWhere = (String) params.get("busWhere");
            if (UIUtil.isNullOrEmpty(busWhere)) {
                busWhere = EMPTY_STRING;
            }
            DomainObject domainObject = DomainObject.newInstance(context, strObjectId);
            mapList = domainObject.getRelatedObjects(
                    context,
                    REL_PROJECT_COLORMATRIX,
                    TYPE_COLORMATRIX,
                    busSelectsList,
                    new StringList(),
                    false,
                    true,
                    (short) 1,
                    busWhere,
                    "",
                    0
            );
            JF_LOGGER.info("mapList1:{}", mapList);
            if (!mapList.isEmpty()) {
                mapList.sort(SELECT_ORIGINATED, "descending", "date");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapList;
    }

    /**
    * 颜色矩阵升版
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.Boolean
    * @date 2025/1/2 16:40
    * @description
    */
    public Boolean colorMatrixUpgradeVersion(Context context, String[] args) throws FrameworkException {
        Boolean flag = Boolean.TRUE;
        Boolean isPush = Boolean.FALSE;
        try {
            Map params = JPO.unpackArgs(args);
            ContextUtil.startTransaction(context, true);
            String strObjectId = (String) params.get("objectId");
            String revision = (String) params.get("revision");
            JF_LOGGER.info("revision:{}", revision);
            //拿去最新发布版本的颜色矩阵
            MapList projectSpaceNewColorMatrix = getProjectSpaceNewColorMatrix(context, args);
            Map map = (Map) projectSpaceNewColorMatrix.get(0);
            //需要升版的颜色矩阵
            String colorMatrixId =  UIUtil.getValue(map, SELECT_ID);
            DomainObject colorMatrixObject = DomainObject.newInstance(context, colorMatrixId);
            Map infoMap = colorMatrixObject.getInfo(context, busSelectsList);
            String strRevision = getObjectNextRevision(context, revision, colorMatrixObject, infoMap);
            JF_LOGGER.info("strRevision:{}", strRevision);
            //颜色矩阵升版
            BusinessObject newColorMatrix = colorMatrixObject.reviseObject(context, colorMatrixObject.getNextSequence(context), false);
            String newColorMatrixId = newColorMatrix.getObjectId(context);
            DomainObject newColorMatrixObject = DomainObject.newInstance(context, newColorMatrixId);
            String colorMatrixName = colorMatrixObject.getInfo(context, SELECT_NAME);
            String sName = newColorMatrixObject.getInfo(context, SELECT_NAME);
            newColorMatrixObject.setDescription(context, UIUtil.getValue(infoMap, SELECT_DESCRIPTION));
            newColorMatrixObject.setAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE,UIUtil.getValue(infoMap, SELECT_ATTRIBUTE_TITLE));
            ContextUtil.pushContext(context);
            isPush = Boolean.TRUE;
            newColorMatrixObject.addFromObject(context, new RelationshipType(REL_PROJECT_COLORMATRIX), strObjectId);
            ContextUtil.popContext(context);
            isPush = Boolean.FALSE;
            //修改新的颜色矩阵版本
            String mql2 = "mod bus $1 name $2 revision $3";
            MqlUtil.mqlCommand(context,false,false,mql2,true,newColorMatrixId,sName,strRevision);
            //颜色分组  颜色矩阵下的颜色分组
            busSelectsList.add(String.format(SELECT_ATTRIBUTE, ATTRIBUTE_COLOR_GROUP_NAME));
            MapList mapList = colorMatrixObject.getRelatedObjects(
                    context,
                    REL_JFColorMatrix2JFColorGroup,
                    TYPE_COLOR_GROUP,
                    busSelectsList,
                    new StringList(),
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    0
            );
            if (!mapList.isEmpty()) {

                Map mapColorGroup  = (Map) mapList.get(0);
                DomainObject colorGroupObject = DomainObject.newInstance(context, UIUtil.getValue(mapColorGroup, SELECT_ID));
                //颜色风格
                MapList mapStyleList = colorGroupObject.getRelatedObjects(
                        context,
                        REL_JFColorGroup2JFColorStyle,
                        TYPE_COLOR_STYLE,
                        busSelectsList,
                        new StringList(),
                        false,
                        true,
                        (short) 1,
                        "",
                        "",
                        0
                );
                Map<String, String> styleMap = new HashMap<>();
                if (!mapStyleList.isEmpty()) {
                    Iterator iterator1 = mapStyleList.iterator();
                    while (iterator1.hasNext()) {
                        Map iiMap = (Map) iterator1.next();
                        String colorStyleId = UIUtil.getValue(iiMap, SELECT_ID);
                        DomainObject colorStyleObject = DomainObject.newInstance(context, colorStyleId);
                        BusinessObject newColorStyle = colorStyleObject.reviseObject(context, colorStyleObject.getNextSequence(context), false);
                        String newColorStyleId = newColorStyle.getObjectId(context);
                        DomainObject newColorStyleObject = DomainObject.newInstance(context, newColorStyleId);
                        Map attributeStyleMap = new HashMap();
                        attributeStyleMap.put(DomainConstants.ATTRIBUTE_TITLE, UIUtil.getValue(iiMap, SELECT_ATTRIBUTE_TITLE));
                        newColorStyleObject.setAttributeValues(context, attributeStyleMap);
                        newColorStyleObject.setDescription(context, UIUtil.getValue(iiMap, SELECT_DESCRIPTION));
                        MqlUtil.mqlCommand(context, false, false, mql2, true, newColorStyleId, newColorStyleObject.getInfo(context, SELECT_NAME), strRevision);
                        styleMap.put(newColorStyleObject.getInfo(context, SELECT_NAME), newColorStyleId);
                    }
                }
                Iterator iterator = mapList.iterator();
                StringList newColorGroupList = new StringList();
                while (iterator.hasNext()) {
                    Map iMap = (Map) iterator.next();
                    //颜色分组
                    String id = UIUtil.getValue(iMap, SELECT_ID);
                    DomainObject oldColorGroupObject = DomainObject.newInstance(context, id);
                    BusinessObject newColorGroup = oldColorGroupObject.reviseObject(context, oldColorGroupObject.getNextSequence(context), false);
                    String objectId = newColorGroup.getObjectId(context);
                    DomainObject newColorGroupObject = DomainObject.newInstance(context, objectId);
                    newColorGroupList.add(objectId);
                    String jfColorGroupName = UIUtil.getValue(iMap, String.format(SELECT_ATTRIBUTE, ATTRIBUTE_COLOR_GROUP_NAME));
                    Map attributeMap = new HashMap();
                    attributeMap.put(ATTRIBUTE_COLOR_GROUP_NAME, jfColorGroupName);
                    attributeMap.put(DomainConstants.ATTRIBUTE_TITLE, UIUtil.getValue(iMap, SELECT_ATTRIBUTE_TITLE));
                    newColorGroupObject.setAttributeValues(context, attributeMap);
                    newColorGroupObject.setDescription(context, UIUtil.getValue(iMap, SELECT_DESCRIPTION));
                    newColorGroupObject.addFromObject(context, new RelationshipType(REL_JFColorMatrix2JFColorGroup), newColorMatrixId);
                    MqlUtil.mqlCommand(context, false, false, mql2, true, objectId, newColorGroupObject.getInfo(context, SELECT_NAME), strRevision);
                    MapList oldStyleList = oldColorGroupObject.getRelatedObjects(
                            context,
                            REL_JFColorGroup2JFColorStyle,
                            TYPE_COLOR_STYLE,
                            busSelectsList,
                            relSelList,
                            false,
                            true,
                            (short) 1,
                            "",
                            "",
                            0
                    );
                    JF_LOGGER.info("oldStyleList:{}",oldStyleList);
                    Iterator iterator1 = oldStyleList.iterator();
                    while (iterator1.hasNext()) {
                        Map map1 = (Map) iterator1.next();
                        String styleName = UIUtil.getValue(map1, SELECT_NAME);
                        String newColorStyleId = styleMap.get(styleName);
                        DomainRelationship domainRelationship = newColorGroupObject.addToObject(context, new RelationshipType(REL_JFColorGroup2JFColorStyle), newColorStyleId);
                        Map relStyleMap = new HashMap();
                        relStyleMap.put(ATTR_JF_InternalColorCode, UIUtil.getValue(map1, SELECT_JF_InternalColorCode));
                        relStyleMap.put(ATTR_JF_ColorStyleName, UIUtil.getValue(map1, SELECT_ATTR_JF_ColorStyleName));
                        relStyleMap.put(ATTR_JF_CustormColorCode, UIUtil.getValue(map1, SELECT_ATTR_JF_CustormColorCode));
                        domainRelationship.setAttributeValues(context, relStyleMap);
                    }
                }
            }
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            if (isPush) {
                ContextUtil.popContext(context);
            }
            ContextUtil.abortTransaction(context);
            e.printStackTrace();
            flag = Boolean.FALSE;
        }
        return flag;
    }

    /**
    * 升版的下一个版本是什么
    * @param context
	* @param revision
	* @param domainObject
	* @param infoMap
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2025/1/6 14:51
    * @description
    */
    public static  String getObjectNextRevision(Context context, String revision, DomainObject domainObject, Map infoMap) throws Exception{
        String strRevision = EMPTY_STRING;
        String nextSequence = domainObject.getNextSequence(context);
        String strThisRevision = UIUtil.getValue(infoMap, SELECT_REVISION);
        String[] split = strThisRevision.split("\\.");
        if ("Major".equalsIgnoreCase(revision)) {
            //大版本
            strRevision = nextSequence;
            if (strRevision.contains(split[0])) {
                //大版本没生效手动设置
                char ch = split[0].charAt(0);
                String major = String.valueOf((char)(ch + 1));
                strRevision = major + ".1";
            }
        } else {
            //小版本
            Integer integer = Integer.valueOf(split[1]);
            integer += 1;
            strRevision = split[0] + "." + integer;
        }
        return strRevision;
    }

    /**
    * 拿取零件关联项目的最新发布颜色矩阵的颜色分组
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.lang.String
    * @date 2025/1/7 17:18
    * @description
    */
    public  MapList getVPMConnColorMatrixColorGroup(Context context, String[] args) throws Exception{
        MapList mapList = new MapList();
        try {
            Map parameters = JPO.unpackArgs(args);
            String strObjectId = (String) parameters.get("strPsId");
            JF_LOGGER.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            JF_LOGGER.info("strObjectId:{}", strObjectId);
            HashMap params = new HashMap();
            params.put("objectId", strObjectId);
            params.put("busWhere", "current==Release");
            MapList projectSpaceNewColorMatrix = getProjectSpaceNewColorMatrix(context, JPO.packArgs(params));
            if (!projectSpaceNewColorMatrix.isEmpty()) {
                Map map = (Map) projectSpaceNewColorMatrix.get(0);
                String colorMatrixId = UIUtil.getValue(map, SELECT_ID);
                JF_LOGGER.info("colorMatrixId:{}", colorMatrixId);
                HashMap params1 = new HashMap();
                params1.put("objectId", colorMatrixId);
                mapList = getColorInfo(context, JPO.packArgs(params1));
            }
            DomainObject bo = DomainObject.newInstance(context, strObjectId);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return mapList;
    }

    /**
     * 数据转发邮箱的格式自定义
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return java.lang.String
     * @date 2024/11/21 14:25
     * @description
     */
    public String buildColorPartCodeHtml(Context context,String[] args) throws Exception {
        String result = DomainConstants.EMPTY_STRING;
        try{
            Map map = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("map:{}", map.toString());
            Map requestMap = (Map) map.get(STRING_REQUESTMAP);
            String form = (String) requestMap.get("form");
            String strMode = (String) requestMap.get("mode");
            StringBuffer sb = new StringBuffer();
            String language = context.getLocale().toString();
            String title = EnoviaResourceBundle.getAttributeI18NString(context,"ATTRIBUTE_JF_COLORCODE",language);
            String zhMess = "\u989c\u8272\u4ee3\u7801\u4ec5\u652f\u6301A,B,C...\u4ee5\u53ca1\u30012\u3001...0，\u603b\u957f\u5ea6\u53ea\u5141\u8bb83\u4e2a\u5b57\u7b26!";
            String enMess = "Color codes only support A, B, C... and 1、 2、... 0; The total length only allows 3 characters!";
            if ("JFColorPartForm".equalsIgnoreCase(form)) {
                sb.append("<input id=\"JF_ColorCode\" name=\"JF_ColorCode\" type=\"text\"  title=\"" + StringEscapeUtils.escapeHtml4(title) + "\" size=\"20\" value=\"\"/>");
                String info = DomainConstants.EMPTY_STRING;
                info = language.contains("zh") ? zhMess : enMess;
                sb.append("<p style=\"color: #660000;font-size: 13px;\" id=\"character-count\">" + StringEscapeUtils.escapeHtml4(info) + "</p>");
                if ("create".equals(strMode)){
                    sb.append("<script>window.addEventListener('load', function JFOnloadHandler() {\n" +
                            "document.getElementById('JF_ColorCode').customValidate = JFCheckColorCode }, false);</script>");
                }
            } else if ("JFEditJFDataOutSourceForm".equalsIgnoreCase(form)) {
                String mode = (String) requestMap.get("mode");
                String objectId = (String) requestMap.get("objectId");
                DomainObject domainObject = DomainObject.newInstance(context, objectId);
                String attrValue = domainObject.getAttributeValue(context, "ATTRIBUTE_JF_COLORCODE");
                if ("view".equalsIgnoreCase(mode)) {
                    sb.append(attrValue);
                } else if ("edit".equalsIgnoreCase(mode)) {
                    sb.append("<textarea cols=\"25\" rows=\"5\" name=\"JSAddressee\" title=\"" + title + "\" id=\"JSAddressee\" value=\"\">"+ StringEscapeUtils.escapeHtml4(attrValue) +"</textarea>");
                    String info = "sample: yanf@xxx.com;leicy@xxx.com";
                    info += language.contains("zh") ? "(使用英文分号分隔)" : "Separate with English semicolons";
                    sb.append("<span style=\"color: #660000;font-size: 13px;\" id=\"character-count\">" + StringEscapeUtils.escapeHtml4(info) + "</span>");
                }
            }
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  result;
    }

    /**
    * 获取物理产品的颜色矩阵版本 并带上链接
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2025/1/9 13:41
    * @description
    */
    public Vector getVpmColorMatrixRevision(Context context, String[] args) throws Exception{
        JF_LOGGER.info("method:getVpmColorMatrixRevision start...");

        Vector vector = new Vector();
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            JF_LOGGER.info("paramsMap:{}", paramsMap.toString());
            Map paramList = (Map) paramsMap.get("paramList");
            String vpmObjectId = (String) paramList.get(STRING_OBJECTID);
            DomainObject domainObject = DomainObject.newInstance(context, vpmObjectId);
            String href = EMPTY_STRING;
            String colorMatrixId = domainObject.getInfo(context, "from[" + REL_JFVPMReference2JFColorMatrix + "].to.id");
            if (UIUtil.isNotNullAndNotEmpty(colorMatrixId)) {
                DomainObject domainObject1 = DomainObject.newInstance(context, colorMatrixId);
                String revision = domainObject1.getInfo(context, SELECT_REVISION);
                href = JF_PublicMethodClass_mxJPO.getConstructDataString(context, colorMatrixId, revision);
            }
            StringList strObjectIdList = JF_PublicMethodClass_mxJPO.getObjectIdList(args);
            for (String objectId : strObjectIdList) {
                vector.add(href);
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("method:getVpmColorMatrixRevision end...");
        return vector;
    }

    /**
     * 获取物理产品的关系上的颜色分组字段
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2025/1/9 13:41
     * @description
     */
    public Vector getVpmInstanceColorGroup(Context context, String[] args) throws Exception{
        JF_LOGGER.info("method:getVpmInstanceColorGroup start...");
        Vector vector = new Vector();
        try {
            ContextUtil.pushContext(context);
            Map paramsMap = (Map) JPO.unpackArgs(args);
            MapList objectList = (MapList)paramsMap.get(STRING_OBJECTLIST);
            Map paramList = (Map) paramsMap.get("paramList");
            String parentObjectId = (String) paramList.get(STRING_OBJECTID);
            DomainObject domainObject = DomainObject.newInstance(context);
            domainObject.setId(parentObjectId);
//            String psId = domainObject.getInfo(context, "to[JFProject2RootPart].from.id");
            String psId = new JF_VPMReferenceEBOM_mxJPO().getPartBelongProject(context, new String[]{parentObjectId});
            Map<String, String> partColorGroupNameMap = new HashMap<>();
            if (UIUtil.isNotNullAndNotEmpty(psId)) {
                partColorGroupNameMap = getProjectColorGroupMap(context, psId);
            }
            JF_VPMReferenceEBOM_mxJPO ebom = new JF_VPMReferenceEBOM_mxJPO();
            if (!objectList.isEmpty()) {
                int iTemp = 0;
                for(int iSize = objectList.size(); iTemp < iSize; ++iTemp) {
                    Map objectMap = (Map)objectList.get(iTemp);
                    String objectId = (String)objectMap.get(SELECT_ID);
                    String code = "NA";
                    if (UIUtil.isNullOrEmpty(psId)) {
                        vector.add(code);
                    } else {
                        String strColorGroupName = EMPTY_STRING;
                        if (partColorGroupNameMap.containsKey(objectId)) {
                            strColorGroupName = partColorGroupNameMap.get(objectId);
                        }
                        if (UIUtil.isNullOrEmpty(strColorGroupName)) {
                            if (iTemp == 0) {
                                boolean flag = ebom.getPartIsSupplyPart(context, new String[]{parentObjectId,psId});
                                code = flag ? "UA" : "NA";
                            }
                            vector.add(code);
                        } else {
                            vector.add(strColorGroupName);
                        }
                    }
//                    if (UIUtil.isNullOrEmpty(psId)) {
//                        boolean flag = ebom.getPartIsSupplyPart(context, new String[]{objectId,psId});
//                        vector.add(flag ? "UA" : "NA");
//                    } else {
//                        String strColorGroupName = EMPTY_STRING;
//                        if (partColorGroupNameMap.containsKey(objectId)) {
//                            strColorGroupName = partColorGroupNameMap.get(objectId);
//                        }
//                        if (UIUtil.isNullOrEmpty(strColorGroupName)) {
//                            boolean flag = ebom.getPartIsSupplyPart(context, new String[]{objectId,psId});
//                            vector.add(flag ? "UA" : "NA");
//                        } else {
//                            vector.add(strColorGroupName);
//                        }
//                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        JF_LOGGER.info("method:getVpmInstanceColorGroup end...");
        return vector;
    }

    /**
    * 解析颜色码
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return java.util.Vector
    * @date 2025/1/13 13:39
    * @description
    */
    public Vector getVpmCMUniqueColorCode(Context context, String[] args) throws Exception{
        JF_LOGGER.info("method:getVpmCMUniqueColorCode start...");

        Vector vector = new Vector();
        StringList strObjectIdList = JF_PublicMethodClass_mxJPO.getObjectIdList(args);
        try {
            Map paramsMap = (Map) JPO.unpackArgs(args);
            Map paramList = (Map) paramsMap.get("paramList");
            MapList objectList = (MapList)paramsMap.get(STRING_OBJECTLIST);
            //颜色title
            String strMode = (String)paramList.get("mode");
            String strJFColorStyleInputContainsCmd = (String)paramList.get("JFColorStyleInputContainsCmd");
            JF_LOGGER.info("strMode:{}", strMode);
            JF_LOGGER.info("strJFColorStyleInputContainsCmd:{}", strJFColorStyleInputContainsCmd);
            if (UIUtil.isNullOrEmpty(strJFColorStyleInputContainsCmd)) {
                for (String objectId : strObjectIdList) {
                    vector.add(EMPTY_STRING);
                }
                return vector;
            }
            JF_LOGGER.info("strMode:{}", strMode);
            JF_LOGGER.info("strJFColorStyleInputContainsCmd:{}", strJFColorStyleInputContainsCmd);
            String vpmObjectId = (String) paramList.get(STRING_OBJECTID);
            JF_LOGGER.info("vpmObjectId:{}",vpmObjectId);
            DomainObject domainObject = DomainObject.newInstance(context, vpmObjectId);
//            String psId = domainObject.getInfo(context, "to[JFProject2RootPart].from.id");
            String psId = new JF_VPMReferenceEBOM_mxJPO().getPartBelongProject(context, new String[]{vpmObjectId});
            Map<String, String> partColorGroupNameMap = new HashMap<>();
            if (UIUtil.isNotNullAndNotEmpty(psId)) {
                partColorGroupNameMap = getProjectColorGroupMap(context, psId);
            }
            String colorMatrixId = domainObject.getInfo(context, "from[" + REL_JFVPMReference2JFColorMatrix + "].to.id");
            if ((UIUtil.isNotNullAndNotEmpty(psId)) && (UIUtil.isNotNullAndNotEmpty(colorMatrixId) && "analysis".equalsIgnoreCase(strMode) && UIUtil.isNotNullAndNotEmpty(strJFColorStyleInputContainsCmd))) {
                DomainObject domainObject1 = DomainObject.newInstance(context, colorMatrixId);
                String cRevision = domainObject1.getInfo(context, SELECT_REVISION);
                String[] split = cRevision.split("\\.");
                String major = split[0];
                BusinessObjectList revisions = domainObject1.getRevisions(context);
                String sColorMatrixId = colorMatrixId;
                for (int i = 0; i < revisions.size(); i++) {
                    BusinessObject businessObject = revisions.get(i);
                    String revision = businessObject.getRevision();
                    String objectId = businessObject.getObjectId(context);
                    DomainObject domainObject2 = DomainObject.newInstance(context, objectId);
                    if (revision.contains(cRevision)) {
                        continue;
                    }
                    if (revision.contains(major)){
                        //是改类小版本的时候
                        if ((revision.compareTo(cRevision)) > 0 && "Release".equalsIgnoreCase(domainObject2.getInfo(context, SELECT_CURRENT))) {
                            cRevision = revision;
                            sColorMatrixId = objectId;
                        }
                    }
                }
                JF_LOGGER.info("sColorMatrixId:{}", sColorMatrixId);
                JF_LOGGER.info("cRevision:{}", cRevision);
                DomainObject colorMatrixObject = DomainObject.newInstance(context, sColorMatrixId);
                MapList mapList = colorMatrixObject.getRelatedObjects(
                        context,
                        REL_JFColorMatrix2JFColorGroup,
                        TYPE_COLOR_GROUP,
                        busSelectsList,
                        new StringList(),
                        false,
                        true,
                        (short) 0,
                        "",
                        "",
                        0
                );
                JF_LOGGER.info("mapList:{}", mapList);
                Iterator iterator = mapList.iterator();
                Map<String, String> internalColorCodeMap = new HashMap<>();
                while (iterator.hasNext()) {
                    Map map = (Map)iterator.next();
                    String title = UIUtil.getValue(map, SELECT_ATTRIBUTE_TITLE);
                    String id = UIUtil.getValue(map, SELECT_ID);
                    DomainObject colorGroupObject = DomainObject.newInstance(context, id);
                    MapList mapList1 = colorGroupObject.getRelatedObjects(
                            context,
                            REL_JFColorGroup2JFColorStyle,
                            TYPE_COLOR_STYLE,
                            busSelectsList,
                            relSelectsList,
                            false,
                            true,
                            (short) 0,
                            "",
                            "",
                            0
                    );
                    mapList1 = (MapList) mapList1.stream().filter(m -> {
                        Map map1 = (Map) m;
                        String strTitle = UIUtil.getValue(map1, SELECT_ATTRIBUTE_TITLE);
                        if (strJFColorStyleInputContainsCmd.equalsIgnoreCase(strTitle)) {
                            return true;
                        } else {
                            return false;
                        }
                    }).collect(Collectors.toCollection(MapList::new));
                    JF_LOGGER.info("mapList1:{}", mapList1);
                    String strInternalColorCode = EMPTY_STRING;
                    if (mapList1.size() > 0) {
                        Map map1 = (Map) mapList1.get(0);
                        strInternalColorCode = UIUtil.getValue(map1, SELECT_JF_InternalColorCode);
                    }
                    internalColorCodeMap.put(title, strInternalColorCode);
                }
                JF_LOGGER.info("internalColorCodeMap:{}", internalColorCodeMap);
                JF_VPMReferenceEBOM_mxJPO ebom = new JF_VPMReferenceEBOM_mxJPO();
                if (!objectList.isEmpty()) {
                    int iTemp = 0;
                    for(int iSize = objectList.size(); iTemp < iSize; ++iTemp) {
                        Map objectMap = (Map)objectList.get(iTemp);
                        String attributeValue = EMPTY_STRING;
                        DomainObject vpmObject = DomainObject.newInstance(context);
                        if (objectMap.containsKey(SELECT_ID)) {
                            String objectId = (String)objectMap.get(SELECT_ID);
                            vpmObject.setId(objectId);
                            String partType = vpmObject.getAttributeValue(context, "JF_VPMReference.JF_PartType");
                            if (partColorGroupNameMap.containsKey(objectId)) {
                                attributeValue = partColorGroupNameMap.get(objectId);
                            } else {
                                //判断是否在当前项目是供货件
                                boolean flag = ebom.getPartIsSupplyPart(context, new String[]{objectId,psId});
                                attributeValue = flag ? "UA" : "NA";
                            }
                        }
                        String vpmRevision = vpmObject.getInfo(context, SELECT_REVISION);
                        String[] split1 = vpmRevision.split("\\.");
                        String vpmVName = vpmObject.getAttributeValue(context, "EnterpriseExtension.V_PartNumber");
                        if (UIUtil.isNullOrEmpty(vpmVName)) {
                            vpmVName = vpmObject.getInfo(context, SELECT_NAME);
                        }
                        if (UIUtil.isNotNullAndNotEmpty(attributeValue)) {
                            String code = vpmVName + split1[0] + internalColorCodeMap.get(attributeValue);
                            vector.add(code);
                        } else {
                            vector.add("");
                        }

                    }
                }
            } else {
                for (String objectId : strObjectIdList) {
                    vector.add(EMPTY_STRING);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            for (String objectId : strObjectIdList) {
                vector.add(EMPTY_STRING);
            }
        }
        JF_LOGGER.info("method:getVpmCMUniqueColorCode end...");
        return vector;
    }

    /**
    * 获取颜色分组和颜色矩阵
    * @param context
	* @param colorMatrixId
	* @param flag
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2025/4/16 14:18
    * @description
    */
    public static Map getColorMatrixGroupAndStyle(Context context, String colorMatrixId, String flag) throws Exception {
        DomainObject domainObject1 = DomainObject.newInstance(context, colorMatrixId);
        String cRevision = domainObject1.getInfo(context, SELECT_REVISION);
        String[] split = cRevision.split("\\.");
        String major = split[0];
        BusinessObjectList revisions = domainObject1.getRevisions(context);
        String sColorMatrixId = colorMatrixId;
        for (int i = 0; i < revisions.size(); i++) {
            BusinessObject businessObject = revisions.get(i);
            String revision = businessObject.getRevision();
            String objectId = businessObject.getObjectId(context);
            DomainObject domainObject2 = DomainObject.newInstance(context, objectId);
            if (revision.contains(cRevision)) {
                continue;
            }
            if (revision.contains(major)){
                //是改类小版本的时候
                if ((revision.compareTo(cRevision)) > 0 && "Release".equalsIgnoreCase(domainObject2.getInfo(context, SELECT_CURRENT))) {
                    cRevision = revision;
                    sColorMatrixId = objectId;
                }
            }
        }
        JF_LOGGER.info("sColorMatrixId:{}", sColorMatrixId);
        JF_LOGGER.info("cRevision:{}", cRevision);
        DomainObject colorMatrixObject = DomainObject.newInstance(context, sColorMatrixId);
        MapList mapList = colorMatrixObject.getRelatedObjects(
                context,
                REL_JFColorMatrix2JFColorGroup,
                TYPE_COLOR_GROUP,
                busSelectsList,
                new StringList(),
                false,
                true,
                (short) 0,
                "",
                "",
                0
        );
        Iterator iterator = mapList.iterator();
        Map<String, String> internalColorCodeMap = new HashMap<>();
        while (iterator.hasNext()) {
            Map map = (Map)iterator.next();
            String title = UIUtil.getValue(map, SELECT_ATTRIBUTE_TITLE);
            String id = UIUtil.getValue(map, SELECT_ID);
            DomainObject colorGroupObject = DomainObject.newInstance(context, id);
            MapList mapList1 = colorGroupObject.getRelatedObjects(
                    context,
                    REL_JFColorGroup2JFColorStyle,
                    TYPE_COLOR_STYLE,
                    busSelectsList,
                    relSelectsList,
                    false,
                    true,
                    (short) 0,
                    "",
                    "",
                    0
            );
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < mapList1.size(); i++) {
                Map map1 = (Map) mapList1.get(0);
                String strInternalColorCode = UIUtil.getValue(map1, SELECT_JF_InternalColorCode);
                String strColorStyleName = UIUtil.getValue(map1, "JF_ColorStyleName");
                stringBuilder.append(strColorStyleName).append("_").append(strInternalColorCode).append("\n");
            }
            internalColorCodeMap.put(title, stringBuilder.toString());
        }
        return internalColorCodeMap;
    }

    /**
    * 零件颜色信息
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2025/4/16 13:46
    * @description
    */
    public StringList getPartColorDetails(Context context,String[] args) throws Exception{
        Map argMaps = JPO.unpackArgs(args);
        String strPartId = (String) argMaps.get("objectId");
        if (UIUtil.isNullOrEmpty(strPartId)){
            Map paramList = (Map) argMaps.get("paramList");
            strPartId = (String) paramList.get("parentOID");
        }
        DomainObject partC = DomainObject.newInstance(context, strPartId);
        String header = EnoviaResourceBundle.getProperty(context, SUITE_KEY_FRAMEWORK, context.getLocale(), "emxFramework.Title.PartColorDetails");
        String strLink = "<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxIndentedTable.jsp?program=JF_ColorPart:getPartIds&amp;table=JFPartColorDetails&amp;header=$3&amp;cancelButton=true&amp;cancelLabel=emxFramework.State.Incident.Close";
        String sLink = strLink + "&amp;objectId=$1&amp;psId=$2','700','600',false,'popup','')\">";
        sLink +=  "<img border='0' src='../common/images/I_PPRSeqInterrupt.gif' alt=\"$4\" title=\"$4\"></img></a>&#160;";
        JF_LOGGER.info("sLink：{}", sLink);

        MapList argMapList = (MapList) argMaps.get(JF_PLMConstants_mxJPO.STRING_OBJECTLIST);
//        String psId = partC.getInfo(context, "to[JFProject2RootPart].from.id");
        String psId = new JF_VPMReferenceEBOM_mxJPO().getPartBelongProject(context, new String[]{strPartId});
        Map<String, String> partColorGroupNameMap = new HashMap<>();
        if (UIUtil.isNotNullAndNotEmpty(psId)) {
            partColorGroupNameMap = getProjectColorGroupMap(context, psId);
        }
        StringList res = new StringList();
        DomainObject domainObject = DomainObject.newInstance(context);
        //有值的情况
        JF_VPMReferenceEBOM_mxJPO ebom = new JF_VPMReferenceEBOM_mxJPO();
        for (int i = 0; i < argMapList.size(); i++) {
            String strObjColHtml = EMPTY_STRING;
            if (UIUtil.isNullOrEmpty(psId)) {
                res.add(EMPTY_STRING);
            } else {
                Map infoMap = (Map) argMapList.get(i);
                String strId = (String) infoMap.get(SELECT_ID);
                domainObject.setId(strId);
                String partType = domainObject.getAttributeValue(context, "JF_VPMReference.JF_PartType");
                String number = domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_V_PART_NUMBER);
                number = UIUtil.isNullOrEmpty(number) ? domainObject.getInfo(context, SELECT_NAME) : number;
                String revision = domainObject.getInfo(context, SELECT_REVISION);
                String head =  header + "  " + number + "_" + revision;
                String strColorGroupName = EMPTY_STRING;
//                if (partColorGroupNameMap.containsKey(strId)) {
//                    strColorGroupName = partColorGroupNameMap.get(strId);
//                } else {
//                    boolean flag = ebom.getPartIsSupplyPart(context, new String[]{strId,psId});
//                    strColorGroupName = flag ? "UA" : "NA";
//                }
                if (partColorGroupNameMap.containsKey(strId)) {
                    strColorGroupName = partColorGroupNameMap.get(strId);
                } else {
                    String code = "NA";
                    if (i == 0) {
                        boolean flag = ebom.getPartIsSupplyPart(context, new String[]{strId, psId});
                        code = flag ? "UA" : "NA";
                    }
                    strColorGroupName = code;
                }
                strObjColHtml = sLink.replace("$1", strId).replace("$2", psId).replace("$3", head).replace("$4", strColorGroupName);
            }
            res.add(strObjColHtml);
        }
        return res;
    }

    /**
    * 显示当前点击的零件
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return com.matrixone.apps.domain.util.MapList
    * @date 2025/4/16 17:36
    * @description
    */
    public static MapList getPartIds(Context context, String[] args) throws Exception{
        HashMap map = (HashMap) JPO.unpackArgs(args);
        JF_LOGGER.info("map:{}", map);
        String objectId = (String) map.get("objectId");
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        MapList mapList = new MapList();
        try {
            ContextUtil.pushContext(context);
            StringList infoList = domainObject.getInfoList(context, "to[JFProject2ColorGroup].from.id");
             mapList = new MapList();
            for (String id : infoList) {
                HashMap<String, String> map1 = new HashMap<>();
                map1.put(SELECT_ID, id);
                mapList.add(map1);
            }
        }catch (Exception e){
            throw e;
        }finally {
            ContextUtil.popContext(context);
        }
        return mapList;
    }

    /**
    * 获取零件的颜色分组
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2025/4/16 17:37
    * @description
    */
    public StringList getPartColorGroupDetails(Context context, String[] args)  throws Exception{
        JF_LOGGER.info("getPartColorGroup 。。。。。。。。。。。。。。。。");
        StringList res = new StringList();
        Map paramsMap = JPO.unpackArgs(args);
        String user = context.getUser();
        JF_LOGGER.info("user:{}", user);
        try {
            ContextUtil.pushContext(context);
            //表格中的零件
            StringList strObjectIdList = JF_PublicMethodClass_mxJPO.getObjectIdList(args);
            Map paramList = (Map) paramsMap.get("paramList");
            Map columnMap = (Map) paramsMap.get("columnMap");
            Map colAttrMap = (Map) columnMap.get("colAttrMap");
            String colName = (String) colAttrMap.get(SELECT_NAME);
            //零件id
            String objectId = (String) paramList.get("objectId");
            String mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Notice.partNotHadColor", new String[]{});
            DomainObject projectObject = DomainObject.newInstance(context);
            for (String strPsId : strObjectIdList) {
                HashMap map = new HashMap<>();
                JF_LOGGER.info("strPsId:{}", strPsId);
                map.put("relName", JF_PLMConstants_mxJPO.rel_JFProject2ColorGroup);
                map.put("fromId", strPsId);
                map.put("toId", objectId);
                String connId = JF_PublicMethodClass_mxJPO.getTwoBusinessObjectConnId(context, JPO.packArgs(map));
                if (UIUtil.isNullOrEmpty(connId)) {
                    res.add(mess);
                } else {
                    projectObject.setId(strPsId);
                    String desc = projectObject.getInfo(context, SELECT_DESCRIPTION);
                    StringList projectMemberList = projectObject.getInfoList(context, "from[Member].to.name");
                    JF_LOGGER.info("projectMemberList:{}", projectMemberList);
                    DomainRelationship domainRelationship = DomainRelationship.newInstance(context, connId);
                    String strColorGroupName = domainRelationship.getAttributeValue(context, "JF_ColorGroupName");
                    String strColorMatrixName = domainRelationship.getAttributeValue(context, "JF_ColorMatrixName");
                    //获取颜色矩阵下的颜色分组
                    HashMap<String, Object> colorMatrixChildrenMap = JF_ColorPart_mxJPO.getColorMatrixChildrenMap(context, strColorMatrixName, strPsId);
                    String flag = (String) colorMatrixChildrenMap.get("flag");
                    if ("N".equalsIgnoreCase(flag)) {
                        res.add(mess);
                    } else {
                        Map<String, StringList> internalColorCodeMap = (HashMap<String, StringList>) colorMatrixChildrenMap.get("internalColorCodeMap");
                        Map<String, Map> colorGroupMap = (HashMap<String, Map>) colorMatrixChildrenMap.get("colorGroupMap");
                        switch (colName) {
                            case "ProjectSapce" : {
                                //项目的名称  需要展示链接
                                if (projectMemberList.contains(user)) {
                                    res.add(JF_PublicMethodClass_mxJPO.getConstructDataString(context, strPsId, desc));
                                } else {
                                    res.add(desc);
                                }
                                break;
                            }
                            case "JFColorGroup": {
                                Map map1 = colorGroupMap.get(strColorGroupName);
                                if (projectMemberList.contains(user)) {
                                    res.add(JF_PublicMethodClass_mxJPO.getConstructDataString(context, UIUtil.getValue(map1, SELECT_ID), strColorGroupName));
                                } else {
                                    res.add(strColorGroupName);
                                }
                                break;
                            }
                            case "JFColorGroupDesc": {
                                Map map1 = colorGroupMap.get(strColorGroupName);
                                res.add(UIUtil.getValue(map1, SELECT_DESCRIPTION));
                                break;
                            }
                            case "JFColorStyleCode": {
                                StringList stringList = internalColorCodeMap.get(strColorGroupName);
                                StringBuffer sb = new StringBuffer();
                                for (String str : stringList) {
                                    sb.append("<table>");
                                    sb.append("<tbody>");
                                    sb.append("<tr>");
                                    sb.append("<td>");
                                    sb.append("</td>");
                                    sb.append(StringEscapeUtils.escapeHtml4(str));
                                    sb.append("</tr>");
                                    sb.append("</tbody>");
                                    sb.append("</table>");
                                }
                                res.add(sb.toString());
                                break;
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ContextUtil.popContext(context);
        }
        return res;
    }


    /**
     * 获取颜色矩阵下所有的颜色风格
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return matrix.util.StringList
     * @date 2025/1/13 9:51
     * @description
     */
    public static StringList getColorMatrixChildrenLists(Context context, String[] args) throws Exception{
        StringList resultList = new StringList();
        Boolean isPush = Boolean.FALSE;
        try {
            Map program = (Map)JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            JF_LOGGER.info("program:{}", program.toString());
            String strVpmId = (String)program.get("parentOID");
            JF_LOGGER.info("parentOID:{}", strVpmId.toString());
            DomainObject objectVpm = DomainObject.newInstance(context, strVpmId);
            String colorMatrixId = objectVpm.getInfo(context, "from[" + REL_JFVPMReference2JFColorMatrix + "].to.id");
            if (UIUtil.isNotNullAndNotEmpty(colorMatrixId)) {
                DomainObject objectColorMatrix = DomainObject.newInstance(context, colorMatrixId);
                MapList mapList = objectColorMatrix.getRelatedObjects(
                        context,
                        REL_JFColorMatrix2JFColorGroup + "," + REL_JFColorGroup2JFColorStyle,
                        TYPE_COLOR_GROUP + "," + TYPE_COLOR_STYLE,
                        busSelectsList,
                        new StringList(),
                        false,
                        true,
                        (short) 0,
                        "",
                        "",
                        0
                );
                JF_LOGGER.info("mapList:{}", mapList.toString());
                resultList = (StringList) mapList.stream().filter(m -> {
                    Map map = (Map) m;
                    String strType = (String) map.get(SELECT_TYPE);
                    return TYPE_COLOR_STYLE.equalsIgnoreCase(strType);
                }).map(m1 -> {
                    Map map1 = (Map) m1;
                    return UIUtil.getValue(map1, SELECT_ID);
                }).collect(Collectors.toCollection(StringList::new));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (isPush) {
                ContextUtil.popContext(context);
            }
            throw new RuntimeException(e);
        }
        JF_LOGGER.info("resultList:{}", resultList.toString());
        return resultList;
    }

    /**
    * 获取颜色矩阵下所有的颜色风格
    * @param context
	* @param strColorMatrixName
	* @param strPsId
    * @author LIUJR
    * @throws
    * @return java.util.HashMap<java.lang.String,java.lang.Object>
    * @date 2025/4/22 10:52
    * @description
    */
    public static HashMap<String, Object> getColorMatrixChildrenMap(Context context, String strColorMatrixName, String strPsId) throws Exception{
        HashMap<String, Object> returnMap = new HashMap<>();
        try {
            ContextUtil.pushContext(context);
            HashMap params = new HashMap();
            params.put("objectId", strPsId);
            params.put("busWhere", "current==Release");
            JF_ColorPart_mxJPO jfColorPartMxJPO = new JF_ColorPart_mxJPO();
            MapList projectSpaceNewColorMatrix = jfColorPartMxJPO.getProjectSpaceNewColorMatrix(context, JPO.packArgs(params));
            if (projectSpaceNewColorMatrix.isEmpty()) {
                returnMap.put("flag", "N");
                return returnMap;
            }
            Map colorMatrixMap = (Map) projectSpaceNewColorMatrix.get(0);
            //需要升版的颜色矩阵
            String colorMatrixId =  UIUtil.getValue(colorMatrixMap, SELECT_ID);
            String colorMatrixName =  UIUtil.getValue(colorMatrixMap, SELECT_NAME);
            if (!colorMatrixName.equalsIgnoreCase(strColorMatrixName)) {
                returnMap.put("flag", "N");
                return returnMap;
            }
            DomainObject colorMatrixObject = DomainObject.newInstance(context, colorMatrixId);
            //获取颜色矩阵的分组 和颜色风格
            MapList mapList = colorMatrixObject.getRelatedObjects(
                    context,
                    REL_JFColorMatrix2JFColorGroup,
                    TYPE_COLOR_GROUP,
                    busSelectsList,
                    new StringList(),
                    false,
                    true,
                    (short) 0,
                    "",
                    "",
                    0
            );
            Iterator iterator = mapList.iterator();
            Map<String, StringList> internalColorCodeMap = new HashMap<>();
            Map<String, Map> colorGroupMap = new HashMap<>();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String title = UIUtil.getValue(map, SELECT_ATTRIBUTE_TITLE);
                String desc = UIUtil.getValue(map, SELECT_DESCRIPTION);
                String id = UIUtil.getValue(map, SELECT_ID);
                HashMap<String, String> colorMap = new HashMap<>();
                colorMap.put(SELECT_ID, id);
                colorMap.put(SELECT_DESCRIPTION, desc);
                colorGroupMap.put(title, colorMap);
                DomainObject colorGroupObject = DomainObject.newInstance(context, id);
                MapList mapList1 = colorGroupObject.getRelatedObjects(
                        context,
                        REL_JFColorGroup2JFColorStyle,
                        TYPE_COLOR_STYLE,
                        busSelectsList,
                        relSelectsList,
                        false,
                        true,
                        (short) 0,
                        "",
                        "",
                        0
                );
                HashSet colorStyleList = new HashSet();
                for (int i = 0; i < mapList1.size(); i++) {
                    Map map1 = (Map) mapList1.get(i);
                    String strInternalColorCode = UIUtil.getValue(map1, SELECT_JF_InternalColorCode);
                    String strColorStyleName = UIUtil.getValue(map1, SELECT_ATTR_JF_ColorStyleName);
                    colorStyleList.add(strInternalColorCode + "_" + strColorStyleName);
                }
                internalColorCodeMap.put(title, StringList.create(colorStyleList));
            }
            returnMap.put("flag", "Y");
            returnMap.put("internalColorCodeMap", internalColorCodeMap);
            returnMap.put("colorGroupMap", colorGroupMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            ContextUtil.popContext(context);
        }
        JF_LOGGER.info("returnMap:{}", returnMap.toString());
        return returnMap;
    }

    /**
    * 颜色矩阵创建窗台提升到审核中的检查trigger
     * 颜色矩阵从草稿提升到审核状态增加Trigger，校验颜色风格内部编码必填
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return int
    * @date 2025/1/16 14:14
    * @description
    */
    public int checkJFColorMatrixCreatePromote(Context context, String[] args) throws Exception{
        int iReturn = 0;
        String mess = EMPTY_STRING;
        try {
            String objectId = args[0];
            DomainObject colorMatrixObj = DomainObject.newInstance(context);
            colorMatrixObj.setId(objectId);
            //判断颜色矩阵中的颜色内部码是否必填
            MapList mapList = colorMatrixObj.getRelatedObjects(
                    context,
                    REL_JFColorMatrix2JFColorGroup + "," + REL_JFColorGroup2JFColorStyle,
                      TYPE_COLOR_GROUP + "," + TYPE_COLOR_STYLE,
                    busSelectsList,
                    relSelectsList,
                    false,
                    true,
                    (short) 0,
                    "",
                    "",
                    0
            );
            mapList = (MapList)mapList.stream().filter(m ->{
                Map map = (Map)m;
                String strType = (String) map.get(SELECT_TYPE);
                return TYPE_COLOR_STYLE.equalsIgnoreCase(strType);
            }).map(m1 -> {
                Map map1 = (Map) m1;
                return map1;
            }).collect(Collectors.toCollection(MapList::new));
            JF_LOGGER.info("mapList:{}", mapList.toString());
            Set strIsNullStyleNameList = new HashSet<>();
            Iterator iterator = mapList.iterator();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String interCode = UIUtil.getValue(map, SELECT_JF_InternalColorCode);
                if (UIUtil.isNullOrEmpty(interCode)) {
                    strIsNullStyleNameList.add(UIUtil.getValue(map, SELECT_ATTRIBUTE_TITLE));
                }
            }
            //校验颜色矩阵所在的项目中的整椅经理是否填写
            String projectId = colorMatrixObj.getInfo(context, "to[JFProject2JFColorMatrix].from.id");
            String chairManagerId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_ID, "", "attribute[Project Role]=='Chair manager'");
            if (UIUtil.isNullOrEmpty(chairManagerId)) {
                mess += EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.Project.NotChairManager");
            }
            //校验颜色矩阵的owner的直线经理是否填写
            String strLineManagerId = JF_PublicMethodClass_mxJPO.getPersonLineManager(context, null, colorMatrixObj.getOwner(context).getName());
            if (UIUtil.isNullOrEmpty(strLineManagerId)) {
                mess += EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DataOutSource.FillInTheLineManage");
            }
            if (strIsNullStyleNameList.size() > 0) {
                mess += ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ColorMatrix.CheckMessage", new String[]{});
                StringList strIsNullStyleNames = StringList.create(strIsNullStyleNameList);
                mess += strIsNullStyleNames.join(",");
            }
            if (UIUtil.isNotNullAndNotEmpty(mess)) {
                emxContextUtil_mxJPO.mqlNotice(context, mess);
                return 1;
            }
        } catch (FrameworkException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    /**
    *
    *@description 获取颜色分组和颜色风格的命名编码
    *@param context
	*@param args [0] matrixId 颜色矩阵id
	*@param args [1] groupId 颜色分组id
	*@param args [2] nameFlag 命名中间分割标识
    *@return java.lang.String 自动命名
    *@throws
    *@author CHENYAN
    *@date 2025/1/6 10:34
    */
    public String getColorGroupOrStyleNameNumber(Context context,String[] args) throws Exception{
        Map argsMap = (Map) JPO.unpackArgs(args);
        String strMatrixId = (String) argsMap.get("matrixId");
        String strGroupId = (String) argsMap.get("groupId");
        String strNameFlag = (String) argsMap.get("nameFlag");
        DomainObject bo ;
        //颜色矩阵
        DomainObject matrixBo = DomainObject.newInstance(context, strMatrixId);
        String strName = matrixBo.getInfo(context, SELECT_NAME);
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        typeSelectList.add(SELECT_ORIGINATED);
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        String strRel = "";
        String strType = "";
        switch (strNameFlag){
            case FLAG_COLOR_GROUP_NAME_NUMBER :{
                strRel = REL_JFColorMatrix2JFColorGroup;
                strType = TYPE_COLOR_GROUP;
                bo = matrixBo ;
                break;
            }
            case FLAG_COLOR_STYLE_NAME_NUMBER :{
                if (UIUtil.isNullOrEmpty(strGroupId)){
                    StringList groupIdList = matrixBo.getInfoList(context, "from[JFColorMatrix2JFColorGroup].to.id");
                    if ( groupIdList.size()> 0 ) {
                        strGroupId = groupIdList.get(0);
                    }
                }
                DomainObject groupBo = DomainObject.newInstance(context);
                groupBo.setId(strGroupId);
                strRel = REL_JFColorGroup2JFColorStyle;
                strType = TYPE_COLOR_STYLE;
                bo = groupBo ;
                break;
            }
            default:{
                bo = matrixBo ;
            }
        }
        MapList maps = bo.getRelatedObjects(context, strRel , // relationship pattern
                strType,                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        JF_LOGGER.info("maps:{}",maps);
        //默认最开始为1
        Integer numBerIndex = 1 ;
        if (maps.size() > 0){
           /* maps.addSortKey(SELECT_NAME, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            maps.addSortKey(SELECT_ORIGINATED, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_DATE);
            maps.sort();
            JF_LOGGER.info("maps:{}",maps);
            Map infoMap = (Map) maps.get(0);
            String strColorName = (String) infoMap.get(SELECT_NAME);
            String[] split = strColorName.split(strNameFlag);
            String strRealIndex = split[1];
            //说明小于10
            if (strRealIndex.startsWith("0")) {
                strRealIndex = strRealIndex.substring(1);
            }*/
            Integer iRealCount =getMaxNumberAfterG(maps,strNameFlag);
            numBerIndex = iRealCount+1;
        }
        // 定义格式为至少两位数字
        //改成三位编码
        DecimalFormat df = new DecimalFormat("000");
        // 格式化数字
        String formattedNum = df.format(numBerIndex);
        String strNameNumber = JF_PublicMethodClass_mxJPO.buildStringInStrings(strName, strNameFlag, formattedNum);
        return strNameNumber;
    }

    /**
     * 获取最多的编码
     * @param mapList
     * @return
     */
    public static int getMaxNumberAfterG(MapList mapList,String strNameFlag) {
        if (mapList == null || mapList.isEmpty()) {
            return -1;
        }

        int maxNum = -1;
        Pattern pattern = Pattern.compile(strNameFlag+"(\\d+)");

        for(int i=0;i<mapList.size();i++){
            try {
                Map map = (Map)mapList.get(i);
                String name = map.get("name").toString();
                Matcher matcher = pattern.matcher(name);

                if (matcher.find()) {
                    int num = Integer.parseInt(matcher.group(1));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                }
            } catch (Exception e) {
                // 忽略异常数据
            }
        }
        return maxNum;
    }
    /**
    *
    *@description 创建颜色分组并关联颜色矩阵和颜色风格
    *@param context
	*@param args
    *@return java.util.Map
    *@throws
    *@author CHENYAN
    *@date 2025/1/10 9:55
    */
    @com.matrixone.apps.framework.ui.CreateProcessCallable
    public Map createColorGroupObject(Context context, String args[]) throws FrameworkException {
        try {
            HashMap requestMap = (HashMap)JPO.unpackArgs(args);
            String strObjectId = (String) requestMap.get("objectId");
            String type = (String) requestMap.get("TypeActual");
            String name = (String) requestMap.get("Name");
            String strJFColorGroupName = (String) requestMap.get("JF_ColorGroupName");
            String strTitle = (String) requestMap.get("Title");
            String strDescription = (String) requestMap.get("Description");
            String strPolicy = (String) requestMap.get("policy");
            JF_LOGGER.info("type:{}",type);
            JF_LOGGER.info("strPolicy:{}",strPolicy);
            strPolicy = PropertyUtil.getSchemaProperty(context, strPolicy);
            JF_LOGGER.info("strPolicy:{}",strPolicy);
            HashMap map = null;
            try {

                ContextUtil.startTransaction(context,true);
                DomainObject bo   = new DomainObject(strObjectId);
                //颜色矩阵版本
                String strRevision = bo.getInfo(context, SELECT_REVISION);
                map = new HashMap(1);
                Policy policy = new Policy(strPolicy);
                String strPolicyRev = policy.getFirstInSequence(context);
                DomainObject newObj = DomainObject.newInstance(context);
                newObj.createObject(context, type, name, strRevision, strPolicy, context.getVault().getName());
                String strNewId = newObj.getId(context);
//                //保存属性
                HashMap<String, String> attrMap = new HashMap<>();
                attrMap.put("JF_ColorGroupName",strJFColorGroupName);
                attrMap.put("Title",strTitle);
                newObj.setAttributeValues(context,attrMap);
                newObj.setDescription(context,strDescription);
                //链接颜色矩阵
                DomainRelationship.connect(context,bo,REL_JFColorMatrix2JFColorGroup,newObj);
                MapList maps = bo.getRelatedObjects(context, REL_JFColorMatrix2JFColorGroup+","+REL_JFColorGroup2JFColorStyle , // relationship pattern
                        TYPE_COLOR_GROUP+","+TYPE_COLOR_STYLE,                                    // object pattern
                        JF_Util_mxJPO.basicBolistSel(),                            // object selects
                        JF_Util_mxJPO.basicRellistSel(), // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 2,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
                Set styleIdSet = (Set)maps.stream().filter(m ->{
                    Map info = (Map)m;
                    String strType = (String) info.get(SELECT_TYPE);
                    return TYPE_COLOR_STYLE.equals(strType);
                }).map(m ->{
                    Map info = (Map)m;
                    String strId = (String) info.get(SELECT_ID);
                    return strId;
                }).collect(Collectors.toSet());
                //如有有颜色风格需要关联颜色风格
                if (styleIdSet != null && styleIdSet.size() > 0){
                    DomainRelationship.connect(context, newObj, REL_JFColorGroup2JFColorStyle, true, StringList.create(styleIdSet).toStringArray());
                }
                map.put(SELECT_ID, strNewId);
                ContextUtil.commitTransaction(context);
            } catch (Exception e) {
                ContextUtil.abortTransaction(context);
                throw new RuntimeException(e);
            }
            return map;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * 创建颜色风格并关联颜色分组，校验座椅分组颜色码一致性
     **
     * @param context
     * @param args 请求参数
     * @return Map 新建颜色风格对象信息
     * @throws FrameworkException 创建颜色风格失败
     * @author CHENYAN
     * @date 2026/7/27 11:34
     */
    @com.matrixone.apps.framework.ui.CreateProcessCallable
    public Map createColorStyleObject(Context context, String args[]) throws FrameworkException {
        try {
            HashMap requestMap = (HashMap)JPO.unpackArgs(args);
            JF_LOGGER.info("requestMap:{}",requestMap);
            String strObjectId = (String) requestMap.get("objectId");
            String type = (String) requestMap.get("TypeActual");
            String name = (String) requestMap.get("Name");
            String strTitle = (String) requestMap.get("Title");
            String strJFColorStyleName = (String) requestMap.get("JF_ColorStyleName");
            String strJFCustormColorCode = (String) requestMap.get("JF_CustormColorCode");
            String strJFInternalColorCode = (String) requestMap.get("JF_InternalColorCode");
            String strDescription = (String) requestMap.get("Description");
            String strPolicy = (String) requestMap.get("policy");
            JF_LOGGER.info("strPolicy:{}",strPolicy);
            strPolicy = PropertyUtil.getSchemaProperty(context, strPolicy);
            JF_LOGGER.info("strPolicy:{}",strPolicy);
            HashMap map = null;
            try {
                ContextUtil.startTransaction(context,true);
                DomainObject bo   = new DomainObject(strObjectId);
                //颜色矩阵版本
                String strRevision = bo.getInfo(context, SELECT_REVISION);
                StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
                typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
                StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
                MapList groupMapList = bo.getRelatedObjects(context, REL_JFColorMatrix2JFColorGroup, // relationship pattern
                        TYPE_COLOR_GROUP,                                    // object pattern
                        typeSelectList,                            // object selects
                        relSelectList, // relationship selects
                        false,                                        // to direction
                        true,                                        // from direction
                        (short) 1,                                    // recursion level
                        "",                // object where clause
                        "",
                        (short) 0);
                DomainObject colorGroupObject = DomainObject.newInstance(context);
                for (int i = 0; i < groupMapList.size(); i++) {
                    Map groupInfo = (Map) groupMapList.get(i);
                    String strGroupNumber = UIUtil.getValue(groupInfo, SELECT_ATTRIBUTE_TITLE);
                    if (!"UA".equals(strGroupNumber)) {
                        continue;
                    }
                    colorGroupObject.setId(UIUtil.getValue(groupInfo, SELECT_ID));
                    MapList colorStyleMapList = colorGroupObject.getRelatedObjects(context,
                            REL_JFColorGroup2JFColorStyle,
                            TYPE_COLOR_STYLE,
                            busSelectsList,
                            relSelList,
                            false,
                            true,
                            (short) 1,
                            "",
                            "",
                            (short) 0);
                    colorStyleMapList.addSortKey(SELECT_ATTRIBUTE_TITLE, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
                    colorStyleMapList.sort();
                    for (int j = 0; j < colorStyleMapList.size(); j++) {
                        Map colorStyleInfo = (Map) colorStyleMapList.get(j);
                        String strExistInternalColorCode = UIUtil.getValue(colorStyleInfo, SELECT_JF_InternalColorCode);
                        String strExistCustormColorCode = UIUtil.getValue(colorStyleInfo, SELECT_ATTR_JF_CustormColorCode);
                        if (Objects.equals(strJFInternalColorCode, strExistInternalColorCode)
                                && !Objects.equals(strJFCustormColorCode, strExistCustormColorCode)) {
                            String strExistColorStyleTitle = UIUtil.getValue(colorStyleInfo, SELECT_ATTRIBUTE_TITLE);
                            String strMessage = EnoviaResourceBundle.getProperty(context,
                                    SUITE_KEY_FRAMEWORK,
                                    context.getLocale(),
                                    "emxFramework.ColorMatrix.InconsistentCustormColorCode");
                            strMessage = strMessage.replace("{0}", strExistColorStyleTitle).replace("{1}", strExistCustormColorCode);
                            emxContextUtil_mxJPO.mqlNotice(context, strMessage);
                            throw new FrameworkException(strMessage);
                        }
                    }
                }
                map = new HashMap(1);
                Policy policy = new Policy(strPolicy);
                DomainObject newObj = DomainObject.newInstance(context);
                newObj.createObject(context, type, name, strRevision, strPolicy, context.getVault().getName());
                String strNewId = newObj.getId(context);
//                //保存属性
                HashMap<String, String> attrMap = new HashMap<>();
                attrMap.put("Title",strTitle);
                newObj.setAttributeValues(context,attrMap);
                newObj.setDescription(context,strDescription);
                DomainObject groupBo = DomainObject.newInstance(context);
                //链接颜色分组
                if (groupMapList.size() > 0){
                    for (int i = 0; i < groupMapList.size(); i++) {
                        Map groupInfo = (Map) groupMapList.get(i);
                        String strGroupId = (String)groupInfo.get(SELECT_ID);
                        String strGroupNumber = (String)groupInfo.get(SELECT_ATTRIBUTE_TITLE);
                        groupBo.setId(strGroupId);
                        DomainRelationship rel = DomainRelationship.connect(context, groupBo, REL_JFColorGroup2JFColorStyle, newObj);
                        HashMap<String, String> attrRelMap = new HashMap<>();

                        //NA的时候颜色风格内部颜色码为NUL
                        // add by chenyan 2025/04/29 整椅分组设置默认值其他分组默认为空
                        if ("NA".equals(strGroupNumber)){
                            attrRelMap.put(ATTR_JF_InternalColorCode,"000");//原来是NUL
                            attrRelMap.put(ATTR_JF_ColorStyleName,"000");//原来是NUL
                            attrRelMap.put(ATTR_JF_CustormColorCode,"000");//原来是NUL
                        }else if ("UA".equals(strGroupNumber)){
                            attrRelMap.put("JF_InternalColorCode",strJFInternalColorCode);
                            attrRelMap.put("JF_ColorStyleName",strJFColorStyleName);
                            attrRelMap.put("JF_CustormColorCode",strJFCustormColorCode);
                        }
                        JF_LOGGER.info("attrRelMap:{}",attrRelMap);
                        if (attrRelMap.size() > 0){
                            rel.setAttributeValues(context,attrRelMap);
                        }
                    }

                }
                map.put(SELECT_ID, strNewId);
                ContextUtil.commitTransaction(context);
            } catch (Exception e) {
                JF_LOGGER.error("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                JF_LOGGER.error("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                JF_LOGGER.error(e.getMessage());
                e.printStackTrace();
                ContextUtil.abortTransaction(context);
                throw new RuntimeException(e);
            }
            return map;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
    /**
    *
    *@description 获取颜色分组和颜色风格编码
    *@param context
	*@param args
    *@return java.lang.String
    *@throws
    *@author CHENYAN
    *@date 2025/1/10 9:54
    */
    public String getNameNumberByForm(Context context ,String[] args) throws Exception{
        Map argsMap = JPO.unpackArgs(args);
        Map requestMap = (Map) argsMap.get("requestMap");
        String strPId = (String) requestMap.get("objectId");
//        String strPId = (String) requestMap.get("parentOID");
        String strFormName = (String) requestMap.get("form");
        String strNameFlag = "";
        if ("JFColorGroupCreateForm".equals(strFormName)){
            strNameFlag = FLAG_COLOR_GROUP_NAME_NUMBER;
        }else if ("JFColorGroupStyleForm".equals(strFormName)){
            strNameFlag = FLAG_COLOR_STYLE_NAME_NUMBER;
        }
        Map newArgsMap = new HashMap<String,String>();
        newArgsMap.put("nameFlag",strNameFlag);
        newArgsMap.put("matrixId",strPId);
        String[] argsArr = JPO.packArgs(newArgsMap);
        String strNameNumber = getColorGroupOrStyleNameNumber(context, argsArr);
        return strNameNumber ;
    }

    /**
     * 获取颜色风格动态列
     **
     * @param context
     * @param args 请求参数
     * @return List 颜色风格动态列
     * @throws Exception
     * @author CHENYAN
     * @date 2026/7/27 11:47
     */
    public List getColorStyleDynamicCol(Context context, String[] args) throws Exception{
        MapList fieldMapList = new MapList();
        try {
            HashMap inputMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) inputMap.get("requestMap");
            String strLoginUser = context.getUser();
            //ECO ID
            String strObjectId = null;
            if (requestMap == null) {
                strObjectId = (String) inputMap.get("objectId");
            } else {
                strObjectId = (String) requestMap.get("objectId");
            }
            boolean isBom = Boolean.FALSE;
            //add by ljr 20250219  bom界面搜素颜色分组颜色风格 传参不一致
            if (UIUtil.isNullOrEmpty(strObjectId)) {
                //是bom选择颜色分组界面
                String strProjectId = (String) requestMap.get("strPsId");
                HashMap params = new HashMap();
                params.put("objectId", strProjectId);
                params.put("busWhere", "current==Release");
                MapList projectSpaceNewColorMatrix = getProjectSpaceNewColorMatrix(context, JPO.packArgs(params));
                if (!projectSpaceNewColorMatrix.isEmpty()) {
                    Map map = (Map) projectSpaceNewColorMatrix.get(0);
                    strObjectId = UIUtil.getValue(map, SELECT_ID);
                    isBom = Boolean.TRUE;
                }
            }
            //end
            JF_LOGGER.info("requestMap：{}", requestMap.toString());
            JF_LOGGER.info("strObjectId：{}", strObjectId);
            DomainObject bo = new DomainObject(strObjectId);
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
            StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
            StringList styleColAttrList = new StringList(3);
            styleColAttrList.add(SELECT_JF_InternalColorCode);
            styleColAttrList.add(SELECT_ATTR_JF_ColorStyleName);
            styleColAttrList.add(SELECT_ATTR_JF_CustormColorCode);
            relSelectList.addAll(styleColAttrList);
            MapList maps = bo.getRelatedObjects(context, REL_JFColorMatrix2JFColorGroup + "," + REL_JFColorGroup2JFColorStyle, // relationship pattern
                    TYPE_COLOR_GROUP + "," + TYPE_COLOR_STYLE,                                    // object pattern
                    typeSelectList,                            // object selects
                    relSelectList, // relationship selects
                    false,                                        // to direction
                    true,                                        // from direction
                    (short) 2,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            HashSet filterStyleSet = new HashSet<>();
            MapList filterStyleMapList = new MapList();
            JF_LOGGER.info("maps：{}", maps.toString());
            for (int i = 0; i < maps.size(); i++) {
                Map info = (Map) maps.get(i);
                String strType = (String) info.get(SELECT_TYPE);
                if (TYPE_COLOR_STYLE.equals(strType)) {
                    String strStyleId = (String) info.get(SELECT_ID);
                    if (!filterStyleSet.contains(strStyleId)) {
                        filterStyleSet.add(strStyleId);
                        filterStyleMapList.add(info);
                    }
                }
            }
            filterStyleMapList.addSortKey(SELECT_NAME, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            filterStyleMapList.sort();
            JF_LOGGER.info("filterStyleSet：{}", filterStyleSet.toString());
            JF_LOGGER.info("filterStyleMapList：{}", filterStyleMapList.toString());
            //返回列集合
            for (int i = 0; i < filterStyleMapList.size(); i++) {
                Map info = (Map) filterStyleMapList.get(i);
                String strTitle = (String) info.get(SELECT_ATTRIBUTE_TITLE);
                String strStyleId = (String) info.get(SELECT_ID);
                for (int i1 = 0; i1 < styleColAttrList.size(); i1++) {
                    Map<Object, Object> colMap = new HashMap<>();
                    Map settingsMap = new HashMap<>();
                    String strColName = styleColAttrList.get(i1);
                    String strActualName = strColName.replace("attribute[", "").replace("]", "");
                    String strFiledName = JF_PublicMethodClass_mxJPO.buildStringInStrings(strStyleId, "@@", strActualName);
                    String strEditable = "true";
                    String strRequired = "false";
                    if (SELECT_JF_InternalColorCode.equals(strColName)) {
                        strRequired = "true";
                        settingsMap.put("Validate", "checkJFInternalColorCode");
                    } else if (SELECT_ATTR_JF_ColorStyleName.equals(strColName)) {
                        strRequired = "true";
                        settingsMap.put("Validate", "validateInputIsStringLengthForColor");
                    }
                    //add by ljr 20250219  bom界面搜素颜色分组颜色风格 传参不一致
                    if (isBom) {
                        String strLabel = "emxFramework.Attribute." + strActualName;
                        settingsMap.put("Column Type", "program");
                        settingsMap.put("function", "getColorStyleInfoValue");
                        settingsMap.put("program", "JF_ColorPart");
                        settingsMap.put("Group Header", strTitle);
                        settingsMap.put("Group Name", "g" + i);
                        colMap.put("settings", settingsMap);
                        colMap.put("name", strFiledName);
                        colMap.put("label", strLabel);
                        //end
                    } else {
                        String strLabel = "emxFramework.Attribute." + strActualName;
                        String strRegisteredSuite = "Framework";
//                String strInputType = "textbox";
                        settingsMap.put("Column Type", "program");
//                settingsMap.put("Width","280");
                        settingsMap.put("Required", strRequired);
                        settingsMap.put("Editable", strEditable);
                        settingsMap.put("function", "getColorStyleInfoValue");
                        settingsMap.put("program", "JF_ColorPart");
                        settingsMap.put("Input Type", "textbox");
                        settingsMap.put("Group Header", strTitle);
                        settingsMap.put("Group Name", "g" + i);
                        settingsMap.put("Edit Access Function", "getUpdateColorStyleFiledAccess");
                        settingsMap.put("Edit Access Program", "JF_ColorPart");
                        colMap.put("settings", settingsMap);
                        colMap.put("name", strFiledName);
                        colMap.put("label", strLabel);
                    }
//                colMap.put("expression_businessobject", strColName);
                    fieldMapList.add(colMap);
                }
            }
            JF_LOGGER.info("fieldMapList：{}", fieldMapList.toString());

        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }


        return fieldMapList ;
    }

    /**
    *
    *@description 获取颜色矩阵关联颜色分组
    *@param context
	*@param args
    *@return com.matrixone.apps.domain.util.MapList
    *@throws
    *@author CHENYAN
    *@date 2025/1/10 9:53
    */
    public MapList getColorInfo(Context context , String[] args) throws Exception{
        Map parameters = JPO.unpackArgs(args);
        String strObjectId = (String) parameters.get("objectId");
        DomainObject bo = DomainObject.newInstance(context, strObjectId);
        StringList reSelectList = new StringList(SELECT_RELATIONSHIP_ID);
        StringList typeSelectList = new StringList(SELECT_ID);
        typeSelectList.add(SELECT_ATTRIBUTE_TITLE);
        typeSelectList.add(SELECT_IS_LAST);
        MapList maps = bo.getRelatedObjects(context, REL_JFColorMatrix2JFColorGroup, // relationship pattern
                TYPE_COLOR_GROUP,                                    // object pattern
                typeSelectList,                            // object selects
                reSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 1,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        maps.addSortKey(SELECT_NAME, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
        maps.sort();
        JF_LOGGER.info("@@@@@@@@@@@@@maps:{}", maps.toString());
        return maps;
    }

    /**
    *
    *@description 获取颜色风格显示值
    *@param context
	*@param args
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2025/1/10 12:19
    */
    public StringList getColorStyleInfoValue(Context context ,String[] args) throws Exception{
        StringList res = new StringList();
        try {
            Map parameter = (Map)JPO.unpackArgs(args);
            JF_LOGGER.info("parameter:{}", parameter.toString());
            MapList colorGroupList = (MapList)parameter.get("objectList");
            Map columnMap = (Map)parameter.get("columnMap");
            Map colAttrMap = (Map)columnMap.get("colAttrMap");
            //table列名称
            //add by ljr 20250219  bom界面搜素颜色分组颜色风格 传参不一致
            String strColName = EMPTY_STRING;
            if (columnMap.containsKey("colAttrMap")) {
                strColName = (String)colAttrMap.get("name");
            } else {
                strColName = (String)columnMap.get("name");
            }
            //end
            String[] split = strColName.split("@@");
            String strStyleId = split[0];
            String strStyleAttrName = split[1];
            DomainObject bo = DomainObject.newInstance(context, strStyleId);
            StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
            StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
//        String strActualName = strStyleAttrName.replace("attribute[","").replace("]","");
            String strActualName = JF_PublicMethodClass_mxJPO.buildStringInStrings("attribute[",strStyleAttrName,"]");
            relSelectList.add(strActualName);
            MapList maps = bo.getRelatedObjects(context, REL_JFColorGroup2JFColorStyle , // relationship pattern
                    TYPE_COLOR_GROUP,                                    // object pattern
                    typeSelectList,                            // object selects
                    relSelectList, // relationship selects
                    true,                                        // to direction
                    false,                                        // from direction
                    (short) 1,                                    // recursion level
                    "",                // object where clause
                    "",
                    (short) 0);
            for (int i = 0; i < colorGroupList.size(); i++) {
                Map groupInfo = (Map) colorGroupList.get(i);
                String strGroupId = (String) groupInfo.get(SELECT_ID);
                String strValue = "";
                for (int i1 = 0; i1 < maps.size(); i1++) {
                    Map info = (Map) maps.get(i1);
                    String strGroupId2 = (String) info.get(SELECT_ID);
                    if (strGroupId.equals(strGroupId2)){
                        strValue = (String) info.get(strActualName);
                        break;
                    }
                }
                res.add(strValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return res;
    }

    /**
    *
    *@description 更新零件分组中得颜色风格属性
    *@param context
	*@param args
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/1/10 12:02
    */
    public void  updateColorStyleFiled(Context context ,String[] args) throws Exception {
        Map parameter = (Map)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)parameter.get("paramMap");
        HashMap requestMap = (HashMap)parameter.get("requestMap");
        HashMap columnMap = (HashMap)parameter.get("columnMap");
        //partId
        String strGroupId = (String)paramMap.get("objectId");
        //修改value值
        String strNewValue = (String)paramMap.get("New Value");
        //修改列信息
        String strColName = (String)columnMap.get("name");
        JF_LOGGER.info("strGroupId:{}",strGroupId);
        String[] split = strColName.split("@@");
        String strStyleId = split[0];
        String strStyleAttrName = split[1];
        String strSelectMql = JF_PublicMethodClass_mxJPO.buildStringInStrings("print bus ",strGroupId," select from[", REL_JFColorGroup2JFColorStyle, "|to.id==", strStyleId, "].id dump ;");
        //获取颜色分组和颜色风格关系
        String strRelId = MqlUtil.mqlCommand(context, false, strSelectMql, false);
        JF_LOGGER.info("strSelectMql:{}",strSelectMql);
        //获取颜色分组和颜色风格关系
//        String strRelId = group.getInfo(context, strSelectMql);
        JF_LOGGER.info("strRelId:{}",strRelId);
        if (UIUtil.isNotNullAndNotEmpty(strRelId)){
            DomainRelationship rel = DomainRelationship.newInstance(context, strRelId);
            String strActualName = strStyleAttrName.replace("attribute[","").replace("]","");
            try {
                ContextUtil.startTransaction(context,true);
                rel.setAttributeValue(context,strActualName,strNewValue);
                ContextUtil.commitTransaction(context);
            } catch (FrameworkException e) {
                ContextUtil.abortTransaction(context);
                throw new RuntimeException(e);
            }
        }else {
            throw new Exception("update fail ");
        }
    }

    /**
    *
    *@description 获取颜色分组表格编辑权限
    *@param context
	*@param args
    *@return matrix.util.StringList
    *@throws
    *@author CHENYAN
    *@date 2025/1/15 16:18
    */

    public StringList  getUpdateColorStyleFiledAccess(Context context ,String[] args) throws Exception {
        String strLoginUser = context.getUser();
        Map parameter = (Map)JPO.unpackArgs(args);
        Map requestMap = (Map) parameter.get("requestMap");
        String strObjectId = (String) requestMap.get("objectId");
        MapList colorGroupList = (MapList)parameter.get("objectList");
        Map columnMap = (Map)parameter.get("columnMap");
        Map colAttrMap = (Map)columnMap.get("colAttrMap");
        StringList res = new StringList();
        //table列名称
        String strColName = (String)colAttrMap.get("name");
        DomainObject bo = DomainObject.newInstance(context, strObjectId);
        Map boInfoMap = bo.getInfo(context, StringList.create(SELECT_REVISION, SELECT_IS_LAST,SELECT_CURRENT,SELECT_OWNER));
        String strIsLast = (String)boInfoMap.get(SELECT_IS_LAST);
        String strRevision = (String)boInfoMap.get(SELECT_REVISION);
        String strCurrent = (String)boInfoMap.get(SELECT_CURRENT);
        String strOwner = (String)boInfoMap.get(SELECT_OWNER);
        //只有最新版才能操作并且草稿状态
        if ("TRUE".equalsIgnoreCase(strIsLast)&& "Create".equalsIgnoreCase(strCurrent)){
            for (int i = 0; i < colorGroupList.size(); i++) {
                Map groupInfoMap = (Map) colorGroupList.get(i);
                String strGroupNum = (String) groupInfoMap.get(SELECT_ATTRIBUTE_TITLE);
                //只有owner才能编辑
                if (strLoginUser.equals(strOwner)){
                    //无色件和座椅分组对应的所有信息不允许编辑
                    if ("NA".equals(strGroupNum) || "UA".equals(strGroupNum)){
                        res.add(Boolean.FALSE.toString());
                    }else {
                        res.add(Boolean.TRUE.toString());
                        boolean isStartRevision = strRevision.endsWith("1");
                        //限制颜色风格修改
//                        if (strColName.contains("@@")){
//                            if (!isStartRevision){
//                                res.add(Boolean.FALSE.toString());
//                            }
//                        }
                    }
                }else {
                    res.add(Boolean.FALSE.toString());
                }

            }
        }else {
            for (int i = 0; i < colorGroupList.size(); i++) {
                res.add(Boolean.FALSE.toString());
            }
        }
        JF_LOGGER.info("res:{}",res);
        return res;
    }
    public StringList getColorStyleByMatrixId(Context context ,String[] args) throws Exception{
        ProjectSpace projectSpace = new ProjectSpace();
        Map parameterMap = JPO.unpackArgs(args);
        String strObjectId = (String) parameterMap.get("objectId");
        DomainObject bo = DomainObject.newInstance(context, strObjectId);
        StringList typeSelectList = JF_Util_mxJPO.basicBolistSel();
        StringList relSelectList = JF_Util_mxJPO.basicRellistSel();
        MapList maps = bo.getRelatedObjects(context, REL_JFColorMatrix2JFColorGroup+","+REL_JFColorGroup2JFColorStyle , // relationship pattern
                TYPE_COLOR_GROUP+","+TYPE_COLOR_STYLE,                                    // object pattern
                typeSelectList,                            // object selects
                relSelectList, // relationship selects
                false,                                        // to direction
                true,                                        // from direction
                (short) 2,                                    // recursion level
                "",                // object where clause
                "",
                (short) 0);
        HashSet filterStyleSet = new HashSet<>();
        for (int i = 0; i < maps.size(); i++) {
            Map info = (Map)maps.get(i);
            String strType = (String) info.get(SELECT_TYPE);
            if (TYPE_COLOR_STYLE.equals(strType)){
                String strStyleId = (String) info.get(SELECT_ID);
                if (!filterStyleSet.contains(strStyleId)){
                    filterStyleSet.add(strStyleId);
                }
            }
        }
        StringList oidList = StringList.create(filterStyleSet);
        return oidList;
    }

    /**
    *
    *@description 获取颜色矩阵中颜色分组、颜色风格操作权限
    *@param context
	*@param args
    *@return java.lang.Boolean
    *@throws
    *@author CHENYAN
    *@date 2025/1/15 13:13
    */
    public Boolean getColorActionCmdAccess(Context context, String[] args)throws Exception {
        Boolean flag = Boolean.FALSE;
        try{
            String strLoginUser = context.getUser();
            Map params = JPO.unpackArgs(args);
            Map settingMap = (Map) params.get("SETTINGS");
            String strObjectId = (String) params.get("objectId");
            DomainObject domainObject = DomainObject.newInstance(context, strObjectId);
            String strCmdName = (String) settingMap.get("CommandName");
            //颜色矩阵为大版本的第一个版本都有权限 颜色矩阵为大版本的第一个版本都有权限
            Map boInfoMap = domainObject.getInfo(context, StringList.create(SELECT_REVISION, SELECT_IS_LAST,SELECT_CURRENT,SELECT_OWNER));
            String strIsLast = (String)boInfoMap.get(SELECT_IS_LAST);
            String strRevision = (String)boInfoMap.get(SELECT_REVISION);
            String strCurrent = (String)boInfoMap.get(SELECT_CURRENT);
            String strOwner = (String)boInfoMap.get(SELECT_OWNER);
            //只有最新版才能操作并且草稿状态
            if ("TRUE".equalsIgnoreCase(strIsLast)&& "Create".equalsIgnoreCase(strCurrent)){
                //判断是否是大版本
                boolean isStartRevision = strRevision.endsWith("1");
                //只有owner才能操作
                if (strLoginUser.equals(strOwner)){
                    //小版涉及颜色风格新增和删除 大版全都可以
                    if ((!isStartRevision)){
                        if ("JFDelColorStyleCmd".equals(strCmdName)){
                            flag = Boolean.TRUE;
                        }else if ("JFCreateColorStyleCmd".equals(strCmdName)){
                            flag = Boolean.TRUE;
                        }else if ("JFDelColorGroupCmd".equals(strCmdName)){
                            flag = Boolean.FALSE ;
                        }else if ("JFCreateColorGroupCmd".equals(strCmdName)){
                            flag = Boolean.FALSE ;
                        }
                    }else {
                        flag = Boolean.TRUE ;
                    }
                }
            }
        } catch (Exception e) {
            JF_LOGGER.error(e.getMessage());
            flag = Boolean.FALSE;
        }
        return flag;
    }

    /**
    * 创建流程 审批流程
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/2/18 16:07
    * @description
    */
    public void actionJFColorMatrixCreatePromote(Context context, String[] args) throws Exception {
        JF_LOGGER.info("method:actionJFColorMatrixCreatePromote -创建流程- start...");
        try {
            String objectId = args[0]; //审核对象Id
            ContextUtil.startTransaction(context, true);
            //项目整椅经理
            DomainObject colorMatrixObject = DomainObject.newInstance(context, objectId);
            String projectId = colorMatrixObject.getInfo(context, "to[JFProject2JFColorMatrix].from.id");
            String chairManagerId = JF_PublicMethodClass_mxJPO.getProjectWherePerson(context, projectId, SELECT_ID, "", "attribute[Project Role]=='Chair manager'");
            if (UIUtil.isNotNullAndNotEmpty(chairManagerId)) {
                //创建审批流程
                // 获取直线经理名称
                JF_PublicMethodClass_mxJPO jfPublicMethodClassMxJPO = new JF_PublicMethodClass_mxJPO();
                String lineManagerID = jfPublicMethodClassMxJPO.getPersonLineManager(context, null, colorMatrixObject.getOwner(context).getName());
                //创建流程
                JF_Route_mxJPO jf_route = new JF_Route_mxJPO(context, args);
                //组装审批人员
                MapList approveList = new MapList();
                String tileMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.ColorMatrixRouteInfo.TitleMessage");
                String ProjectManage = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DARouteInfo.TitleMessage.WholeChair");
                String LineManage = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DARouteInfo.TitleMessage.LineManage");
                if (lineManagerID.equalsIgnoreCase(chairManagerId)) {
                    //审批人 直线经理
                    Map nReceiverMapOne = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(lineManagerID, tileMess + "-" + LineManage, "true", "1", "All");//设置审批信息 标题
                    approveList.add(nReceiverMapOne);
                } else {
                    //审批人 直线经理
                    Map nReceiverMapOne = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(lineManagerID, tileMess + "-" + LineManage, "true", "1", "All");//设置审批信息 标题
                    approveList.add(nReceiverMapOne);
                    //审批人 整椅经理
                    Map nReceiverMapTwo = (Map) JF_PublicMethodClass_mxJPO.getMapAnyOrAll(chairManagerId, tileMess + "-" + ProjectManage, "true", "2", "All");//设置审批信息 标题
                    approveList.add(nReceiverMapTwo);
                }

                String state = STATE_POLICY_JFColorMatrix_Review;
                String policy = POLICY_POLICY_JFColorMatrix;
                String routeDescription = tileMess;
                String routeId = jf_route.createAndStartRoute(context, approveList, objectId, state, policy, routeDescription);
                JF_LOGGER.info("routeId:{}", routeId);
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            e.printStackTrace();
            throw e;
        }
        JF_LOGGER.info("method:actionJFColorMatrixCreatePromote -创建流程- end...");
    }

    /**
    * 获取项目成员id
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2025/3/4 14:38
    * @description
    */
    public StringList getColorConnProjectPerson(Context context, String[] args) throws Exception{
        StringList resultList = new StringList();
        Boolean isPush = Boolean.FALSE;
        String personObjectID = PersonUtil.getPersonObjectID(context, context.getUser());
        try {
            Map program = (Map)JPO.unpackArgs(args);
            ContextUtil.pushContext(context);
            JF_LOGGER.info("program:{}", program.toString());
            String strColorId = (String)program.get("parentOID");
            JF_LOGGER.info("parentOID:{}", strColorId.toString());
            DomainObject objectColor = DomainObject.newInstance(context, strColorId);
            String projectId = objectColor.getInfo(context, "to[JFProject2JFColorMatrix].from.id");
            if (UIUtil.isNotNullAndNotEmpty(projectId)) {
                resultList = JF_PublicMethodClass_mxJPO.getProjectAllPersons(context, projectId, SELECT_ID);
                if (resultList.contains(personObjectID)) {
                    resultList.remove(personObjectID);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (isPush) {
                ContextUtil.popContext(context);
            }
            throw new RuntimeException(e);
        }
        JF_LOGGER.info("resultList:{}", resultList.toString());
        return resultList;
    }

    /**
    * 获取项目的关联的颜色分组的零件
    * @param context
	* @param psId
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 20/05/2025 15:28
    * @description
    */
    public Map<String, String> getProjectColorGroupMap(Context context, String psId) throws Exception{
        Map<String, String> partColorGroupNameMap = new HashMap<>();
        if (UIUtil.isNullOrEmpty(psId)) {
            return partColorGroupNameMap;
        }
        DomainObject domainObject = DomainObject.newInstance(context);
        domainObject.setId(psId);
        String hasMatrix = domainObject.getInfo(context, "from[JFProject2JFColorMatrix|to.current=='Release']");
        if ("FALSE".equalsIgnoreCase(hasMatrix)) {
            return partColorGroupNameMap;
        }
        StringList bolistSel = JF_Util_mxJPO.basicBolistSel();
        StringList rellistSel = JF_Util_mxJPO.basicRellistSel();
        rellistSel.add("attribute[JF_ColorGroupName]");
        MapList relatedObjects = domainObject.getRelatedObjects(
                context,
                JF_PLMConstants_mxJPO.rel_JFProject2ColorGroup,
                "*",
                bolistSel,
                rellistSel,
                false,
                true,
                (short) 1,
                "",
                "",
                0
        );
        relatedObjects.stream().forEach(m -> {
            Map map = (Map) m;
            String oid = UIUtil.getValue(map, SELECT_ID);
            String rid = UIUtil.getValue(map, DomainRelationship.SELECT_ID);
            String strColorGroupName = UIUtil.getValue(map, "attribute[JF_ColorGroupName]");
            partColorGroupNameMap.put(oid, strColorGroupName);
        });
        return partColorGroupNameMap;
    }
    /*
     * @description:供货件才显示颜色分组按钮
     * @author: caipan
     * @date: 2025/7/24 17:50:23
     * @param: * @param[1] context
     * @param[2] args
     * @return: 
     **/
    public boolean JFColorGroupVpmBomCmdShow(Context context,String[] args) throws Exception{
        Map request = JPO.unpackArgs(args);
        String objectId = (String)request.get("objectId");
        String projectId = new JF_VPMReferenceEBOM_mxJPO().getPartZeroBelowProject(context, objectId);
        if(UIUtil.isNotNullAndNotEmpty(projectId)){
            return true;
        }
        return false;
    }

    /**
    *
    * @param context
	* @param strProjectId
    * @author LIUJR
    * @throws
    * @return java.util.Map
    * @date 2025/11/13 9:42
    * @description
    */
    public static Map  getColorMatrixGroupAndStyle(Context context, String strProjectId) throws Exception{
        Map<String, Object> returnMap = new HashMap<>();
        HashMap params = new HashMap();
        params.put("objectId", strProjectId);
        params.put("busWhere", "current==Release");
        String colorMatrixId = EMPTY_STRING;
        JF_ColorPart_mxJPO jfColorPartMxJPO = new JF_ColorPart_mxJPO();
        MapList projectSpaceNewColorMatrix = jfColorPartMxJPO.getProjectSpaceNewColorMatrix(context, JPO.packArgs(params));
        if (!projectSpaceNewColorMatrix.isEmpty()) {
            Map map = (Map) projectSpaceNewColorMatrix.get(0);
            colorMatrixId = UIUtil.getValue(map, SELECT_ID);
        }
        if (UIUtil.isNullOrEmpty(colorMatrixId)) {
            return returnMap;
        }
        DomainObject colorMatrixObject = DomainObject.newInstance(context, colorMatrixId);
        //获取颜色矩阵的分组 和颜色风格
        MapList colorGroupMapList = colorMatrixObject.getRelatedObjects(
                context,
                REL_JFColorMatrix2JFColorGroup,
                TYPE_COLOR_GROUP,
                busSelectsList,
                new StringList(),
                false,
                true,
                (short) 0,
                "",
                "",
                0
        );
        //颜色矩阵中颜色分组下的颜色风格
        StringList styleSet = new StringList();
        DomainObject colorGroupObject = DomainObject.newInstance(context);
        Map<String, Map<String, Map<String, String>>> colorGroupCodeMap = new HashMap<>();
        for (int i = 0; i < colorGroupMapList.size(); i++) {
            Map map = (Map) colorGroupMapList.get(i);
            String title = UIUtil.getValue(map, SELECT_ATTRIBUTE_TITLE);
            String id = UIUtil.getValue(map, SELECT_ID);
            HashMap<String, String> colorGroupStyleMap = new HashMap<>();
            colorGroupObject.setId(id);
            MapList colorStyleMapList = colorGroupObject.getRelatedObjects(
                    context,
                    REL_JFColorGroup2JFColorStyle,
                    TYPE_COLOR_STYLE,
                    busSelectsList,
                    relSelList,
                    false,
                    true,
                    (short) 0,
                    "",
                    "",
                    0
            );
            colorStyleMapList.addSortKey(SELECT_NAME, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
//            colorStyleMapList.addSortKey(SELECT_ORIGINATED, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_DATE);
            colorStyleMapList.sort();
            Map<String, Map<String, String>> colorCodeMap = new HashMap<>();
            Map<String, String> internalColorMap = new HashMap<>();
            Map<String, String> custormColorMap = new HashMap<>();
            for (int i1 = 0; i1 < colorStyleMapList.size(); i1++) {
                Map colorStyleMap = (Map) colorStyleMapList.get(i1);
                String strInternalColorCode = UIUtil.getValue(colorStyleMap, SELECT_JF_InternalColorCode);
                String strCustormColorCode = UIUtil.getValue(colorStyleMap, SELECT_ATTR_JF_CustormColorCode);
                String strColorStyleTitle = UIUtil.getValue(colorStyleMap, SELECT_ATTRIBUTE_TITLE);
                if (i == 0) {
                    styleSet.add(strColorStyleTitle);
                }
                custormColorMap.put(strColorStyleTitle, strCustormColorCode);
                internalColorMap.put(strColorStyleTitle, strInternalColorCode);
            }
            colorCodeMap.put("JF_InternalColorCode", internalColorMap);
            colorCodeMap.put("JF_CustormColorCode", custormColorMap);
            colorGroupCodeMap.put(title, colorCodeMap);
        }
        returnMap.put("styleList", StringList.create(styleSet));
        returnMap.put("colorGroupCodeMap", colorGroupCodeMap);
        return returnMap;
    }

    /**
     * 统一校验并保存颜色分组下的颜色风格字段
     **
     * @param context
     * @param args 表格保存参数
     * @return Map post处理返回值
     * @throws Exception
     * @author caipan
     * @date 2026/7/27 11:47
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map postProcessColorStyleFields(Context context, String[] args) throws Exception {
        Map returnMap = new HashMap();
        Map<String, Map<String, Map<String, String>>> groupChangeMap = new LinkedHashMap<>();
        Map programMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
        String strRequestMatrixId = requestMap == null ? EMPTY_STRING : UIUtil.getValue(requestMap, "objectId");
        com.matrixone.jdom.Document xmlDoc = (com.matrixone.jdom.Document) programMap.get("XMLDoc");
        if (xmlDoc == null) {
            return returnMap;
        }

        String[] columnNameArray = new String[]{
                "name", "columnName", "column", "columnId", "columnID", "id", "attrName", "attribute", "adminName", "fieldName"};
        String[] oldValueNameArray = new String[]{"oldValue", "oldActualValue", "oldDisplayValue"};
        String[] newValueNameArray = new String[]{"newValue", "value", "actualValue", "displayValue"};
        List objectElementList = xmlDoc.getRootElement().getChildren("object");
        for (int i = 0; i < objectElementList.size(); i++) {
            com.matrixone.jdom.Element objectElement = (com.matrixone.jdom.Element) objectElementList.get(i);
            String strGroupId = objectElement.getAttributeValue("objectId");
            if (UIUtil.isNullOrEmpty(strGroupId)) {
                strGroupId = objectElement.getAttributeValue("id");
            }
            if (UIUtil.isNullOrEmpty(strGroupId)) {
                continue;
            }
            List columnElementList = objectElement.getChildren("column");
            for (int j = 0; j < columnElementList.size(); j++) {
                com.matrixone.jdom.Element columnElement = (com.matrixone.jdom.Element) columnElementList.get(j);
                String strColumnName = getColorStyleXMLFirstValue(columnElement, columnNameArray);
                String[] strColumnNameArray = strColumnName.split("@@", 2);
                if (strColumnNameArray.length != 2) {
                    continue;
                }
                String strStyleId = strColumnNameArray[0];
                String strAttrName = strColumnNameArray[1].replace("attribute[", "").replace("]", "");
                if (!ATTR_JF_InternalColorCode.equals(strAttrName)
                        && !ATTR_JF_ColorStyleName.equals(strAttrName)
                        && !ATTR_JF_CustormColorCode.equals(strAttrName)) {
                    continue;
                }
                String strEdited = columnElement.getAttributeValue("edited");
                String strChanged = columnElement.getAttributeValue("changed");
                String strModified = columnElement.getAttributeValue("modified");
                String strStatus = columnElement.getAttributeValue("status");
                String strOldValue = getColorStyleXMLFirstValue(columnElement, oldValueNameArray);
                String strNewValue = getColorStyleXMLFirstValue(columnElement, newValueNameArray);
                boolean isChanged = "true".equalsIgnoreCase(strEdited)
                        || "true".equalsIgnoreCase(strChanged)
                        || "true".equalsIgnoreCase(strModified)
                        || "changed".equalsIgnoreCase(strStatus)
                        || (hasColorStyleXMLValue(columnElement, oldValueNameArray)
                        && hasColorStyleXMLValue(columnElement, newValueNameArray)
                        && !strOldValue.equals(strNewValue));
                if (!isChanged) {
                    continue;
                }
                if (!hasColorStyleXMLValue(columnElement, newValueNameArray)) {
                    strNewValue = columnElement.getText();
                }
                Map<String, Map<String, String>> styleChangeMap = groupChangeMap.get(strGroupId);
                if (styleChangeMap == null) {
                    styleChangeMap = new LinkedHashMap<>();
                    groupChangeMap.put(strGroupId, styleChangeMap);
                }
                Map<String, String> attrChangeMap = styleChangeMap.get(strStyleId);
                if (attrChangeMap == null) {
                    attrChangeMap = new LinkedHashMap<>();
                    styleChangeMap.put(strStyleId, attrChangeMap);
                }
                attrChangeMap.put(strAttrName, strNewValue);
            }
        }
        if (groupChangeMap.isEmpty()) {
            return returnMap;
        }

        Map<String, Map<String, String>> relChangeMap = new LinkedHashMap<>();
        String strGroupNameSelect = "attribute[" + ATTRIBUTE_COLOR_GROUP_NAME + "]";
        String strMatrixIdSelect = "to[" + REL_JFColorMatrix2JFColorGroup + "].from.id";
        for (Map.Entry<String, Map<String, Map<String, String>>> groupEntry : groupChangeMap.entrySet()) {
            String strGroupId = groupEntry.getKey();
            DomainObject colorGroupObject = DomainObject.newInstance(context, strGroupId);
            Map groupInfoMap = colorGroupObject.getInfo(context,
                    StringList.create(SELECT_ATTRIBUTE_TITLE, strGroupNameSelect, strMatrixIdSelect));
            String strGroupTitle = UIUtil.getValue(groupInfoMap, SELECT_ATTRIBUTE_TITLE);
            String strGroupName = UIUtil.getValue(groupInfoMap, strGroupNameSelect);
            String strMatrixId = UIUtil.getValue(groupInfoMap, strMatrixIdSelect);
            if ("UA".equals(strGroupTitle)
                    || "NA".equals(strGroupTitle)
                    || UIUtil.isNullOrEmpty(strMatrixId)
                    || (UIUtil.isNotNullAndNotEmpty(strRequestMatrixId) && !strRequestMatrixId.equals(strMatrixId))) {
                throw new FrameworkException(EnoviaResourceBundle.getProperty(context,
                        SUITE_KEY_FRAMEWORK, context.getLocale(), "emxFramework.Access.NoAccess"));
            }
            DomainObject colorMatrixObject = DomainObject.newInstance(context, strMatrixId);
            Map colorMatrixInfoMap = colorMatrixObject.getInfo(context,
                    StringList.create(SELECT_IS_LAST, SELECT_CURRENT, SELECT_OWNER));
            if (!"TRUE".equalsIgnoreCase(UIUtil.getValue(colorMatrixInfoMap, SELECT_IS_LAST))
                    || !"Create".equals(UIUtil.getValue(colorMatrixInfoMap, SELECT_CURRENT))
                    || !context.getUser().equals(UIUtil.getValue(colorMatrixInfoMap, SELECT_OWNER))) {
                throw new FrameworkException(EnoviaResourceBundle.getProperty(context,
                        SUITE_KEY_FRAMEWORK, context.getLocale(), "emxFramework.Access.NoAccess"));
            }

            StringList styleSelectList = StringList.create(SELECT_ID, SELECT_ATTRIBUTE_TITLE);
            StringList styleRelSelectList = JF_Util_mxJPO.basicRellistSel();
            styleRelSelectList.add(SELECT_JF_InternalColorCode);
            styleRelSelectList.add(SELECT_ATTR_JF_ColorStyleName);
            styleRelSelectList.add(SELECT_ATTR_JF_CustormColorCode);
            MapList colorStyleMapList = colorGroupObject.getRelatedObjects(context,
                    REL_JFColorGroup2JFColorStyle,
                    TYPE_COLOR_STYLE,
                    styleSelectList,
                    styleRelSelectList,
                    false,
                    true,
                    (short) 1,
                    "",
                    "",
                    (short) 0);
            colorStyleMapList.addSortKey(SELECT_ATTRIBUTE_TITLE,
                    ProgramCentralConstants.ASCENDING_SORT,
                    ProgramCentralConstants.SORTTYPE_STRING);
            colorStyleMapList.sort();

            Map<String, Map<String, String>> styleChangeMap = groupEntry.getValue();
            Set<String> processedStyleIdSet = new HashSet<>();
            Map<String, Map<String, String>> internalColorCodeMap = new LinkedHashMap<>();
            for (int i = 0; i < colorStyleMapList.size(); i++) {
                Map colorStyleInfoMap = (Map) colorStyleMapList.get(i);
                String strStyleId = UIUtil.getValue(colorStyleInfoMap, SELECT_ID);
                String strStyleTitle = UIUtil.getValue(colorStyleInfoMap, SELECT_ATTRIBUTE_TITLE);
                String strRelId = UIUtil.getValue(colorStyleInfoMap, SELECT_RELATIONSHIP_ID);
                String strInternalColorCode = UIUtil.getValue(colorStyleInfoMap, SELECT_JF_InternalColorCode);
                String strCustormColorCode = UIUtil.getValue(colorStyleInfoMap, SELECT_ATTR_JF_CustormColorCode);
                Map<String, String> attrChangeMap = styleChangeMap.get(strStyleId);
                if (attrChangeMap != null) {
                    processedStyleIdSet.add(strStyleId);
                    if (attrChangeMap.containsKey(ATTR_JF_InternalColorCode)) {
                        strInternalColorCode = attrChangeMap.get(ATTR_JF_InternalColorCode);
                    }
                    if (attrChangeMap.containsKey(ATTR_JF_CustormColorCode)) {
                        strCustormColorCode = attrChangeMap.get(ATTR_JF_CustormColorCode);
                    }
                    if (UIUtil.isNullOrEmpty(strRelId)) {
                        throw new FrameworkException("update fail");
                    }
                    relChangeMap.put(strRelId, attrChangeMap);
                }
                if (UIUtil.isNullOrEmpty(strInternalColorCode)) {
                    continue;
                }
                Map<String, String> existColorStyleMap = internalColorCodeMap.get(strInternalColorCode);
                if (existColorStyleMap == null) {
                    existColorStyleMap = new HashMap<>();
                    existColorStyleMap.put("styleTitle", strStyleTitle);
                    existColorStyleMap.put("custormColorCode", strCustormColorCode);
                    internalColorCodeMap.put(strInternalColorCode, existColorStyleMap);
                } else if (!Objects.equals(existColorStyleMap.get("custormColorCode"), strCustormColorCode)) {
                    String strMessage = EnoviaResourceBundle.getProperty(context,
                            SUITE_KEY_FRAMEWORK,
                            context.getLocale(),
                            "emxFramework.ColorMatrix.InconsistentGroupCustormColorCode");
                    strMessage = strMessage.replace("{0}", strGroupName)
                            .replace("{1}", strGroupTitle)
                            .replace("{2}", existColorStyleMap.get("styleTitle"))
                            .replace("{3}", strStyleTitle)
                            .replace("{4}", strInternalColorCode)
                            .replace("{5}", existColorStyleMap.get("custormColorCode"))
                            .replace("{6}", strCustormColorCode);
                    throw new FrameworkException(strMessage);
                }
            }
            if (processedStyleIdSet.size() != styleChangeMap.size()) {
                throw new FrameworkException(EnoviaResourceBundle.getProperty(context,
                        SUITE_KEY_FRAMEWORK, context.getLocale(), "emxFramework.Access.NoAccess"));
            }
        }

        boolean isTransactionStarted = false;
        try {
            ContextUtil.startTransaction(context, true);
            isTransactionStarted = true;
            for (Map.Entry<String, Map<String, String>> relEntry : relChangeMap.entrySet()) {
                DomainRelationship.newInstance(context, relEntry.getKey()).setAttributeValues(context, relEntry.getValue());
            }
            ContextUtil.commitTransaction(context);
            isTransactionStarted = false;
        } catch (Exception e) {
            if (isTransactionStarted) {
                ContextUtil.abortTransaction(context);
            }
            throw e;
        }
        returnMap.put("Action", "execScript");
        returnMap.put("Message", "{ main:function() { if(window.emxEditableTable){window.emxEditableTable.refreshStructure();} } }");
        return returnMap;
    }

    /**
     * 获取颜色风格保存XML中的首个有效值
     **
     * @param element XML列节点
     * @param nameArray 候选名称
     * @return String XML值
     * @author caipan
     * @date 2026/7/27 11:47
     */
    private String getColorStyleXMLFirstValue(com.matrixone.jdom.Element element, String[] nameArray) {
        for (int i = 0; i < nameArray.length; i++) {
            String strValue = element.getAttributeValue(nameArray[i]);
            if (strValue != null) {
                return strValue;
            }
        }
        for (int i = 0; i < nameArray.length; i++) {
            com.matrixone.jdom.Element childElement = element.getChild(nameArray[i]);
            if (childElement != null) {
                return childElement.getText();
            }
        }
        return EMPTY_STRING;
    }

    /**
     * 判断颜色风格保存XML是否包含指定值
     **
     * @param element XML列节点
     * @param nameArray 候选名称
     * @return boolean 是否包含指定值
     * @author caipan
     * @date 2026/7/27 11:47
     */
    private boolean hasColorStyleXMLValue(com.matrixone.jdom.Element element, String[] nameArray) {
        for (int i = 0; i < nameArray.length; i++) {
            if (element.getAttributeValue(nameArray[i]) != null || element.getChild(nameArray[i]) != null) {
                return true;
            }
        }
        return false;
    }
}
