import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import java.util.Iterator;


public class JF_ChangeEvent_mxJPO {

    private static final Logger _logger =  LoggerFactory.getLogger(JF_DRW_mxJPO.class);
    private final String RELATIONSHIP_JFCHANGEEVENTECR = "JFChangeEventECR";
    private final String TYPE_JFECR = "JFECR";

    /**
     * 获取所有的JFChangeEvent更改事件对象
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public MapList getJFChangeEvent(Context context, String[] args) throws Exception{
        StringList objectSelects = basicBolistSel();
        objectSelects.add("attribute[Title]");
        objectSelects.add("attribute[JFCustomerECRNumber]");
        MapList mapList = DomainObject.findObjects(context, "JFChangeEventType", "eService Production", "", objectSelects);
        return mapList;
    }

    /**
     * 获取JFChangeEvent变更事件所关联的ECR
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public MapList getJFCERelECR(Context context, String[] args) throws Exception{
        MapList mapList = new MapList();
        StringList objectSelects = basicBolistSel();
        objectSelects.add("attribute[Title]");
        objectSelects.add("attribute[JFChangeSource]");
        objectSelects.add("relationship[JFChange2Project].to.name");
        Map paramMap = (Map) JPO.unpackArgs(args);
        String objectId = (String)paramMap.get("objectId");
        DomainObject dr = DomainObject.newInstance(context, objectId);
        mapList = dr.getRelatedObjects(context,RELATIONSHIP_JFCHANGEEVENTECR,TYPE_JFECR+","+JF_PLMConstants_mxJPO.TYPE_JFNewECR+","+JF_PLMConstants_mxJPO.TYPE_JFFormalECR,objectSelects,null,
                false,true,(short) 1,
                "","",0);
        return mapList;
    }


    /**
     * 获取跟CE有关系的ECR的id做过滤
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList getUnrelatedECR(Context context, String[] args)throws Exception{
        StringList result = new StringList();
        StringList bosel = basicBolistSel();
        MapList mapList = DomainObject.findObjects(context, TYPE_JFECR+","+JF_PLMConstants_mxJPO.TYPE_JFNewECR+","+JF_PLMConstants_mxJPO.TYPE_JFFormalECR, "*", "to[JFChangeEventECR] == true" , bosel);
        Iterator iterator = mapList.iterator();
        while (iterator.hasNext()){
            Map map = (Map) iterator.next();
            String id = (String) map.get("id");
            result.add(id);
        }
        return result;
    }


    /**
     *
     *@description 创建ECR后处理连接关系
     *@param context
     *@param args
     *@return void
     *@throws
     *@author CHENYAN
     *@date 2024/7/23 10:24
     */
    public void postCEProcessInCreateECR(Context context,String[] args) throws Exception{
        try{
            ContextUtil.pushContext(context);
            Map parameter = (Map) JPO.unpackArgs(args);

            Map requestMap = (Map)parameter.get("requestMap");
            Map paramMap = (Map)parameter.get("paramMap");
            //objectId  CE的ID
            String objectId = (String) requestMap.get("objectId");
            //整椅开发工程师ID
            String strWCharOIDs = (String)requestMap.get("WCharDevEngineeringOID");
            //经理OID
            String strManagerReviewOID = (String)requestMap.get("ManagerReviewOID");
            //CE ID
            String strCEOID = objectId;
            //关联项目ID
            String strProjectID = (String)requestMap.get("JFProjectNameOID");
            //创建CRID
            String strNewObjectId = (String)paramMap.get("newObjectId");
            //关联CE
            if (UIUtil.isNotNullAndNotEmpty(strCEOID)){
                DomainRelationship.connect(context,strCEOID,"JFChangeEventECR",strNewObjectId,false);
            }
            //关联项目
            if (UIUtil.isNotNullAndNotEmpty(strProjectID)){
                DomainRelationship.connect(context,strNewObjectId,"JFChange2Project",strProjectID,false);
            }
            DomainObject ecr = DomainObject.newInstance(context, strNewObjectId);
            Map relAttrMap = new HashMap();
            if (UIUtil.isNotNullAndNotEmpty(strWCharOIDs)){
                StringTokenizer strtk = new StringTokenizer(strWCharOIDs, "|");
                StringList mCharPersonList = new StringList();
                while (strtk.hasMoreTokens()) {
                    mCharPersonList.add(strtk.nextToken());
                }
                //判断整椅开发工程师中是否包含了经理
                if (UIUtil.isNotNullAndNotEmpty(strManagerReviewOID)) {
                    if (mCharPersonList.contains(strManagerReviewOID)){
                        //防止重复连接
                        mCharPersonList.remove(strManagerReviewOID);
                        DomainRelationship rel = DomainRelationship.connect(context,ecr , JF_PLMConstants_mxJPO.REL_ECR2PERSON, DomainObject.newInstance(context, strManagerReviewOID));
                        relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE,JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_BOTH);
                        relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTEOBJECTSTATUS,"Create");
                        rel.setAttributeValues(context,relAttrMap);

                    }else {
                        if (UIUtil.isNotNullAndNotEmpty(strManagerReviewOID)) {
                            DomainRelationship rel = DomainRelationship.connect(context,ecr, JF_PLMConstants_mxJPO.REL_ECR2PERSON, DomainObject.newInstance(context, strManagerReviewOID));
                            relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE,JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_MANGER);
                            relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTEOBJECTSTATUS,"Create");
                            rel.setAttributeValues(context,relAttrMap);
                        }
                    }
                }
                //清空
                relAttrMap.clear();
                relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE,JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_DEV_ENGINEER);
                relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTEOBJECTSTATUS,"Create");
                for (int i = 0; i < mCharPersonList.size(); i++) {
                    String strWcharPersonId = mCharPersonList.get(i);
                    DomainRelationship rel = DomainRelationship.connect(context, ecr, JF_PLMConstants_mxJPO.REL_ECR2PERSON, DomainObject.newInstance(context, strWcharPersonId));
                    rel.setAttributeValues(context,relAttrMap);

                }
            }else {
                if (UIUtil.isNotNullAndNotEmpty(strManagerReviewOID)) {
                    DomainRelationship rel = DomainRelationship.connect(context,ecr, JF_PLMConstants_mxJPO.REL_ECR2PERSON, DomainObject.newInstance(context, strManagerReviewOID));
                    relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE,JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_MANGER);
                    relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTEOBJECTSTATUS,"Create");
                    rel.setAttributeValues(context,relAttrMap);
                }
            }
        }catch (Exception e){
            _logger.info(e.getMessage());
        }finally {
            ContextUtil.popContext(context);
        }
    }


    public StringList basicBolistSel(){
        StringList boSel = new StringList();
        boSel.add(DomainConstants.SELECT_ID);
        boSel.add(DomainConstants.SELECT_NAME);
        boSel.add(DomainConstants.SELECT_TYPE);
        boSel.add(DomainConstants.SELECT_REVISION);
        boSel.add(DomainConstants.SELECT_DESCRIPTION);
        return boSel;
    }


    /**
     * 变更事件修改ECR编号时能修改的状态
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public boolean eidtCustomerECRNumber(Context context, String[] args) throws Exception {

        Map paramMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map) paramMap.get("requestMap");
        String objectId = (String)requestMap.get("objectId");
        DomainObject dr = DomainObject.newInstance(context, objectId);
        String current = dr.getInfo(context,"current");
        return "Create".equals(current)?true:false;
    }


}
