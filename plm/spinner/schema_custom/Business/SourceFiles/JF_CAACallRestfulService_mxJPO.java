import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.dscn.plm.util.NioJDUtils;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.json.JSONObject;
import jakarta.json.JsonArray;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;
/*
 * @description: 拿华润代码
 * @author: caipan
 * @date:
 * @param: * @param[1] null
 * @return:
 **/
public class JF_CAACallRestfulService_mxJPO {
    private static final org.slf4j.Logger _logger = LoggerFactory.getLogger(JF_DataInterface_mxJPO.class);



    /**
     * CAA: 获取装配结构
     *
     * @param context
     * @param args
     * @return com.matrixone.json.JSONObject
     * @Author heyf
     */
    public JSONObject getProductStructure(Context context, String[] args) throws Exception {
        JSONObject resultJson = new JSONObject();
        try {
            ContextUtil.pushContext(context);
            resultJson = JF_VPLMReference_mxJPO.getProductStructure(context, args);
        } catch (Exception e) {
            resultJson.put("Status", "false");
            resultJson.put("Message", e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return resultJson;
    }


    /*
     * @description: 查询结构
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public JSONObject getProductStructureInputRevision(Context context, String[] args) throws Exception {
        JSONObject resultJson = new JSONObject();
        try {
            ContextUtil.pushContext(context);
            resultJson = JF_VPLMReference_mxJPO.getProductStructureAndRevision(context, args);
        } catch (Exception e) {
            resultJson.put("Status", "false");
            resultJson.put("Message", e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return resultJson;
    }

    /*
     * @description: 获取最新版本的数模
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public JSONObject getProductStructureLaster(Context context, String[] args) throws Exception {
        JSONObject resultJson = new JSONObject();
        try {
            ContextUtil.pushContext(context);
            resultJson = JF_VPLMReference_mxJPO.getProductStructureLaster(context, args);
        } catch (Exception e) {
            resultJson.put("Status", "false");
            resultJson.put("Message", e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return resultJson;
    }
    /**
     * CAA: 创建一次性Ticket
     *
     * @param context
     * @param args
     * @return com.matrixone.json.JSONObject
     * @Author qubibo
     */
    public  JSONObject getLoginTicket(Context context, String[] args) throws Exception {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson = JF_VPLMReference_mxJPO.getLoginTicket(context, args);
        } catch (Exception e) {
            resultJson.put("Status", "false");
            resultJson.put("Message", e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return resultJson;
    }


    /**
     * CAA: 登录验证 账号密码
     *
     * @param context
     * @param args
     * @return com.matrixone.json.JSONObject
     * @Author heyf
     */
    public JSONObject checkUserLoginInfo(Context context, String[] args) throws Exception {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson = JF_VPLMReference_mxJPO.checkUserLoginInfo(context, args);
        } catch (Exception e) {
            resultJson.put("Status", "false");
            resultJson.put("Message", e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return resultJson;
    }
    /*
     * @description: 修改零件版本
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public com.alibaba.fastjson.JSONObject modifyRevision(Context context, String[] args) throws Exception{
        try {
            Map<Object, Object> map = JPO.unpackArgs(args);
            ArrayList array = (ArrayList) map.get("partList");
            _logger.info("partList:{}", array);
            String PartName = "";
            String PartType = "";
            String ModifyRevision = "";
            String CurrentRevision = "";
            String FlexiblePart = "";
            String ID = "";
            Map temp = null;
            StringList selList = JF_VPLMReference_mxJPO.slBusSelect;
            selList.add("attribute[XCADExtension.V_CADOrigin]");
            selList.add("from[VPMRepInstance].to.id");
            selList.add("from[VPMRepInstance].to.name");
            selList.add("from[XCADAssemblyRepInstance].to.id");
            selList.add("from[XCADAssemblyRepInstance].to.name");
            String ShapeId = "";
            String ShapeName = "";
            String XCADAssemblyRepReference = "";
            String XCADAssemblyRepInstanceName = "";
            for (int i = 0; i < array.size(); i++) {
                temp = (Map) array.get(i);
                PartName = UIUtil.getValue(temp, "PartName");
                PartType = UIUtil.getValue(temp, "PartType");//VPMReference、Drawing
                ModifyRevision = UIUtil.getValue(temp, "ModifyRevision");//需要修改的版本
                CurrentRevision = UIUtil.getValue(temp, "CurrentRevision"); //系统的版本
                FlexiblePart = UIUtil.getValue(temp, "FlexiblePart"); //是否变形计
                ID = UIUtil.getValue(temp, "ID"); //
                if(UIUtil.isNullOrEmpty(FlexiblePart)){
                    FlexiblePart ="N";
                }
                MapList mlVPM = null;
                if(!"Drawing".equalsIgnoreCase(PartType)) {
                    if(UIUtil.isNotNullAndNotEmpty(ID)){
                        mlVPM = DomainObject.findObjects(context, "VPMReference", null,
                                "name=='" + ID + "' && revision=='" + CurrentRevision + "' && attribute[JF_VPMReference.JF_FlexiblePart]=='" + FlexiblePart + "'", selList);
                    }else {
                        mlVPM = DomainObject.findObjects(context, "VPMReference", null,
                                "attribute[EnterpriseExtension.V_PartNumber]=='" + PartName + "' && revision=='" + CurrentRevision + "' && attribute[JF_VPMReference.JF_FlexiblePart]=='" + FlexiblePart + "'", selList);
                    }
                }else{
                    mlVPM = DomainObject.findObjects(context, "Drawing", null,
                            "attribute[PLMEntity.V_Name]=='" + PartName + "' && revision=='" + CurrentRevision + "'", selList);
                }
                _logger.info("mlVPM:{}",mlVPM);
                if (mlVPM.size() > 0) {
                    temp = (Map) mlVPM.get(0);
                    _logger.info("temp:{}",temp);
                    String name = UIUtil.getValue(temp, DomainConstants.SELECT_NAME);
                    String id = UIUtil.getValue(temp, DomainConstants.SELECT_ID);
                     XCADAssemblyRepReference = UIUtil.getValue(temp, "from[XCADAssemblyRepInstance].to.id");
                    XCADAssemblyRepInstanceName = UIUtil.getValue(temp, "from[XCADAssemblyRepInstance].to.name");
                     ShapeId = UIUtil.getValue(temp, "from[VPMRepInstance].to.id");
                    ShapeName = UIUtil.getValue(temp, "from[VPMRepInstance].to.name");
                     _logger.info("ShapeId:{}",ShapeId);
                    //XCADExtension.V_CADOrigin
                    //XCADAssemblyRepInstance 总成
                    //VPMRepInstance  零件
                    String mql = "mod bus '" + id + "' name '" + name + "' revision '" + ModifyRevision + "'";
                    MqlUtil.mqlCommand(context, false, mql, true);
                    if(UIUtil.isNotNullAndNotEmpty(ShapeId)){//3d零件
                         mql = "mod bus '" + ShapeId + "' name '" + ShapeName + "' revision '" + ModifyRevision + "'";
                        MqlUtil.mqlCommand(context, false, mql, true);
                    }
                    if(UIUtil.isNotNullAndNotEmpty(XCADAssemblyRepReference)&&!"AA.1".equalsIgnoreCase(ModifyRevision)){//总成
                        mql = "mod bus '" + XCADAssemblyRepReference + "' name '" + XCADAssemblyRepInstanceName + "' revision '" + ModifyRevision + "'";
                        MqlUtil.mqlCommand(context, false, mql, true);
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            com.alibaba.fastjson.JSONObject returnObj = new com.alibaba.fastjson.JSONObject();
            returnObj = NioJDUtils.setErrormsg(returnObj,e.getMessage());
            return returnObj;
        }
        com.alibaba.fastjson.JSONObject returnObj = new com.alibaba.fastjson.JSONObject();
        returnObj = NioJDUtils.setSuccessmsg(returnObj);
        return returnObj;
    }


    /*
     * @description: CAA XPMD 接口修改关系属性FNA、Usage
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public com.alibaba.fastjson.JSONObject modifyInstanceUsage(Context context, String[] args) throws Exception{
        _logger.info("modifyInstanceUsage start");
        boolean ispush = false;
        try {
            ContextUtil.pushContext(context);
            ispush = true;
            Map<Object, Object> map = JPO.unpackArgs(args);
            ArrayList array = (ArrayList) map.get("partList");
            _logger.info("partList:{}", array);
            String fromPartName = "";
            String fromPartrevision = "";
            String toPartName = "";
            String toPartrevision = "";
            String usage = "";
            String fna = "";
            Map temp = null;
            for (int i = 0; i < array.size(); i++) {
                temp = (Map) array.get(i);
                fromPartName = UIUtil.getValue(temp, "fromPartName");//from name
                fromPartrevision = UIUtil.getValue(temp, "fromPartRevision");//from revision
                toPartName = UIUtil.getValue(temp, "toPartName");//to name
                toPartrevision = UIUtil.getValue(temp, "toPartRevision"); //to revision
                usage = UIUtil.getValue(temp, "usage"); //Usage
                fna = UIUtil.getValue(temp, "fna"); //FNA
              if(UIUtil.isNullOrEmpty(fromPartName)||UIUtil.isNullOrEmpty(fromPartrevision)||UIUtil.isNullOrEmpty(toPartName)||UIUtil.isNullOrEmpty(toPartrevision)){
                  _logger.info("from 和 to 端 不可以为空");
              }else {
                  String mql = "query connection REL VPMInstance from VPMReference  '" + fromPartName + "'  '" + fromPartrevision + "' to VPMReference '" + toPartName + "'  '" + toPartrevision + "' where \"attribute[JF_VPMInstance.JF_FNA]=='' && attribute[JF_VPMInstance.JF_Dosage]=='1' \" select id dump";
                  _logger.info("mql:{}",mql);
                  String relId = MqlUtil.mqlCommand(context, false, mql, true);
                  if(UIUtil.isNullOrEmpty(relId)){
                      mql = "query connection REL VPMInstance from VPMReference  '" + fromPartName + "'  '" + fromPartrevision + "' to VPMReference '" + toPartName + "'  '" + toPartrevision + "' select id dump";
                      relId = MqlUtil.mqlCommand(context, false, mql, true);
                  }
                  _logger.info("relId:{}", relId);
                  if (UIUtil.isNotNullAndNotEmpty(relId)) {
                      //如果有多个的情况下，怎么考虑呢
                      String[] relList =  relId.split("\n");
                    if(relList.length>0) {
                        StringList list = FrameworkUtil.split(relList[0], ",");
                        if (list.size() == 2) {
                            relId = list.get(1);
                            _logger.info("relId:{}",relId);
                            DomainRelationship ship = new DomainRelationship(relId);
                            if (UIUtil.isNotNullAndNotEmpty(usage)) {
                                ship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_Dosage, usage);
                            }
                            if (UIUtil.isNotNullAndNotEmpty(fna)) {
                                ship.setAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_FNA, fna);
                            }
                        }
                    }
                  }
              }
            }
            _logger.info("modifyInstanceUsage end");
        }catch (Exception e){
            e.printStackTrace();
            com.alibaba.fastjson.JSONObject returnObj = new com.alibaba.fastjson.JSONObject();
            returnObj = NioJDUtils.setErrormsg(returnObj,e.getMessage());
            return returnObj;
        }finally {
            if(ispush){
                ContextUtil.popContext(context);
            }
        }
        com.alibaba.fastjson.JSONObject returnObj = new com.alibaba.fastjson.JSONObject();
        returnObj = NioJDUtils.setSuccessmsg(returnObj);
        return returnObj;
    }
}
