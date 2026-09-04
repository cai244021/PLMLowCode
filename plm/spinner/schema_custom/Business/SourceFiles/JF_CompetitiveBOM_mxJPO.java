import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import java.util.HashMap;
import java.util.Map;

public class JF_CompetitiveBOM_mxJPO implements JF_PLMConstants_mxJPO{
    /** 
     * @description: 查询所有的竞品车型品牌 
     * @param: context
	args 
     * @return: com.matrixone.apps.domain.util.MapList 
     * @author JJS
     * @date:  18:19
     */
    public MapList findAllCompetitiveModel(Context context, String[] args)throws Exception{
        StringList boSels = JF_Util_mxJPO.basicBolistSel();
        MapList mapList = DomainObject.findObjects(context,TYPE_JFCOMPETITIVEBRAND,"*","",new StringList("id"));
        return mapList;
    }
    /**
     * @description: 展开子级零件
     * @param: context
	args
     * @return: com.matrixone.apps.domain.util.MapList
     * @author JJS
     * @date:  13:39
     */
    public MapList findExpand(Context context, String[] args)throws Exception{
        Map paramMap = JPO.unpackArgs(args);
        String objId = (String)paramMap.get("objectId");
        String expandLevel = (String)paramMap.get("expandLevel");
        if("All".equals(expandLevel)){
            expandLevel = "0";
        }
        DomainObject obj = DomainObject.newInstance(context,objId);
        StringList boSel = JF_Util_mxJPO.basicBolistSel();
        StringList relSel = new StringList();
        relSel.add(DomainRelationship.SELECT_ID);
        MapList childPartList =  obj.getRelatedObjects(context,REL_Instance+","+REL_JFMODEL2COMPETITIVEBOM,TYPE_JF_CompetitiveBOM+","+TYPE_JFCOMPETITIVEMODEL,
                boSel,relSel,false,true,Short.parseShort(expandLevel),"","",0);
        return childPartList;
    }
/** 
 * @description: 创建
 * @param: context
	args 
 * @return: java.lang.String 
 * @author JJS
 * @date:  15:27
 */
    public Map createCompetitiveBOM(Context context,String[] args)throws Exception{
        Map paramMap = JPO.unpackArgs(args);
        JF_VPMT_mxJPO jfVpmtMxJPO = JF_VPMT_mxJPO.getInstance(context);
        String partName = jfVpmtMxJPO.autoName(context,null,"BM");
        String title = (String)paramMap.get("title");
        String parentId = (String)paramMap.get("objectId");
        DomainObject parent = DomainObject.newInstance(context,parentId);
        System.out.println("owner>>>>1"+parent.getInfo(context,"owner"));
        System.out.println("project>>>>1"+parent.getInfo(context,"project"));
        System.out.println("organization>>>>1"+parent.getInfo(context,"organization"));
        String owner = context.getUser();
        String id = JF_Util_mxJPO.copyCompetitiveBOMPart(context,partName,title,parentId,owner);
        Map<String,String> map = new HashMap<>();
        DomainObject obj = DomainObject.newInstance(context,id);
        System.out.println("owner>>>>"+obj.getInfo(context,"owner"));
        System.out.println("project>>>>"+obj.getInfo(context,"project"));
        System.out.println("organization>>>>"+obj.getInfo(context,"organization"));
        map.put("id",id);
        return map;
    }

    /**
     * PLM低代码页面创建独立竞品BOM零件
     **
     * @param context 当前PLM上下文
     * @param args 页面提交参数
     * @return Map 创建对象的objectId和name
     * @throws Exception 创建或属性写入失败
     * @author caipan by codex
     * @date 2026/9/3 16:13
     */
    public Map createCompetitiveBOMLowCode(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        String title = paramMap.get("title") == null ? "" : String.valueOf(paramMap.get("title")).trim();
        String description = paramMap.get("description") == null ? "" : String.valueOf(paramMap.get("description")).trim();
        String partType = paramMap.get("partType") == null ? "" : String.valueOf(paramMap.get("partType")).trim();
        String loginUser = context.getUser();
        if (loginUser == null || loginUser.trim().length() == 0) {
            throw new IllegalArgumentException("当前用户未登录");
        }
        if (title.length() == 0 || title.length() > 100) {
            throw new IllegalArgumentException("标题不能为空且长度不能超过100个字符");
        }
        if (!("C".equals(partType) || "T".equals(partType) || "U".equals(partType))) {
            throw new IllegalArgumentException("零件子类型只能是C、T或U");
        }

        boolean pushed = false;
        ContextUtil.startTransaction(context, true);
        try {
            ContextUtil.pushContext(context);
            pushed = true;
            JF_VPMT_mxJPO jfVpmtMxJPO = JF_VPMT_mxJPO.getInstance(context);
            String partName = jfVpmtMxJPO.autoName(context, null, "BM");
            String[] propertyArgs = new String[]{"JF_CompetitiveBOM.Template.id"};
            String templateId = JF_PublicMethodClass_mxJPO.getBasicUrl(context, propertyArgs);
            if (templateId == null || templateId.trim().length() == 0) {
                throw new IllegalStateException("未配置竞品BOM模板");
            }

            MqlUtil.mqlCommand(context, true, false,
                    "copy bus $1 to $2 $3 $4 $5;", true,
                    templateId, partName, "-", "history", "!path");
            String objectId = MqlUtil.mqlCommand(context, true, false,
                    "print bus $1 $2 $3 select $4 dump;", true,
                    TYPE_JF_CompetitiveBOM, partName, "-", "id");
            if (objectId == null || objectId.trim().length() == 0) {
                throw new IllegalStateException("竞品BOM对象创建失败");
            }

            DomainObject object = DomainObject.newInstance(context, objectId);
            Map attributeMap = new HashMap();
            attributeMap.put("PLMEntity.PLM_ExternalID", partName);
            attributeMap.put("PLMEntity.V_Name", title);
            attributeMap.put(ATTR_JF_PartType, partType);
            object.setAttributeValues(context, attributeMap);
            object.setDescription(context, description);
            MqlUtil.mqlCommand(context, true, false, "mod bus $1 current $2 owner $3;", true,
                    objectId, "IN_WORK", loginUser);

            ContextUtil.commitTransaction(context);
            Map result = new HashMap();
            result.put("objectId", objectId);
            result.put("name", partName);
            return result;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        } finally {
            if (pushed) {
                ContextUtil.popContext(context);
            }
        }
    }

    /**
     * PLM低代码Table分页查询竞品BOM零件
     **
     * @param context 当前PLM上下文
     * @param args 查询参数，支持page和perPage
     * @return Map 标准items和total结果
     * @throws Exception 查询失败
     * @author caipan by codex
     * @date 2026/9/3 16:13
     */
    public Map getCompetitiveBOMListLowCode(Context context, String[] args) throws Exception {
        Map paramMap = JPO.unpackArgs(args);
        int page = paramMap.get("page") instanceof Number ? ((Number) paramMap.get("page")).intValue() : 1;
        int perPage = paramMap.get("perPage") instanceof Number ? ((Number) paramMap.get("perPage")).intValue() : 20;
        page = page < 1 ? 1 : page;
        perPage = perPage < 1 ? 20 : Math.min(perPage, 200);

        StringList selects = new StringList();
        selects.add("id");
        selects.add("name");
        selects.add("revision");
        selects.add("description");
        selects.add("current");
        selects.add("attribute[PLMEntity.V_Name]");
        selects.add("attribute[JF_VPMReference.JF_PartType]");
        MapList allObjects = DomainObject.findObjects(context, TYPE_JF_CompetitiveBOM, "*", "", selects);
        int fromIndex = Math.min((page - 1) * perPage, allObjects.size());
        int toIndex = Math.min(fromIndex + perPage, allObjects.size());
        MapList items = new MapList();
        for (int i = fromIndex; i < toIndex; i++) {
            Map source = (Map) allObjects.get(i);
            Map item = new HashMap();
            item.put("objectId", source.get("id"));
            item.put("name", source.get("name"));
            item.put("revision", source.get("revision"));
            item.put("title", source.get("attribute[PLMEntity.V_Name]"));
            item.put("description", source.get("description"));
            item.put("partType", source.get("attribute[JF_VPMReference.JF_PartType]"));
            item.put("current", source.get("current"));
            items.add(item);
        }

        Map result = new HashMap();
        result.put("items", items);
        result.put("total", allObjects.size());
        return result;
    }
}
