import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.MultiValueSelects;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.framework.ui.PostProcessCallable;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static com.matrixone.apps.domain.MultiValueSelects.RELATIONSHIP_REFERENCE_DOCUMENT;

/**
 * @author CHENYAN
 * @version v1.0.0
 * 创建时间：2024/7/23 09:39
 * @description ECR 业务处理延迟类
 */
public class JF_ECRServiceDeffer_mxJPO {
    private static final Logger _logger =  LoggerFactory.getLogger(JF_ECRService_mxJPO.class);

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
    @PostProcessCallable
    public void postProcessInCreateECR(Context context,String[] args) throws Exception{
        try{
            ContextUtil.pushContext(context);
            Map parameter = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map)parameter.get("requestMap");
            Map paramMap = (Map)parameter.get("paramMap");
            String strAffectedProjectIds = (String)requestMap.get("JFAffectedProject");
            _logger.info("requestMap:{}",requestMap);
//            String[] JFAffectsFactory = (String[])requestMap.get("JFAffectsFactory");
            //经理OID
            String strManagerReviewOID = (String)requestMap.get("ManagerReviewOID");
            //CE ID
            String strCEOID = (String)requestMap.get("JFChangeEventTypeOID");
            //关联项目ID
            String strProjectID = (String)requestMap.get("JFProjectNameOID");
            String strJFECRQQFileId = (String)requestMap.get("JFECRQQFileId");
            String strJFECRQQFileId2 = (String)requestMap.get("JFECRQQFileId2");
            String strJFECRQQ = (String)requestMap.get("JFQQ");
            //创建CRID
            String strNewObjectId = (String)paramMap.get("newObjectId");
            _logger.info("strManagerReviewOID:{}",strManagerReviewOID);
            _logger.info("strCEOID:{}",strCEOID);
            _logger.info("strProjectID:{}",strProjectID);
            _logger.info("strNewObjectId:{}",strNewObjectId);
            _logger.info("strJFECRQQFileId:{}",strJFECRQQFileId);
            _logger.info("strJFECRQQ:{}",strJFECRQQ);
//            _logger.info("JFAffectsFactory:{}",JFAffectsFactory);
//            _logger.info("JFAffectsFactory1111:{}", StringList.create(JFAffectsFactory));

            DomainObject ecr = DomainObject.newInstance(context, strNewObjectId);
            //设置QQ属性
            if (UIUtil.isNotNullAndNotEmpty(strJFECRQQ)){
                ecr.setAttributeValue(context,"JFQQ",strJFECRQQ);
            }
            //保存受影响工厂
//            if (null != JFAffectsFactory && JFAffectsFactory.length > 0){
//                String strJFAffectsFactory = StringList.create(JFAffectsFactory).join(",");
//                ecr.setAttributeValue(context,"JFAffectedFactory",strJFAffectsFactory);
//            }
            //关联CE
            if (UIUtil.isNotNullAndNotEmpty(strCEOID)){
                DomainRelationship.connect(context,strCEOID,"JFChangeEventECR",strNewObjectId,false);
            }
            //关联项目
            if (UIUtil.isNotNullAndNotEmpty(strProjectID)){
                DomainRelationship.connect(context,strNewObjectId,"JFChange2Project",strProjectID,false);
            }
            //关联受影响项目
            if (UIUtil.isNotNullAndNotEmpty(strAffectedProjectIds)){
                String[] affectedProjectIds = strAffectedProjectIds.split(",");
                DomainRelationship.connect(context, ecr, "JFECR2AffectedProject", true,affectedProjectIds);
            }
            //关联QQ附件
            if (UIUtil.isNotNullAndNotEmpty(strJFECRQQFileId)){
                DomainRelationship rel = DomainRelationship.connect(context,ecr, RELATIONSHIP_REFERENCE_DOCUMENT, DomainObject.newInstance(context, strJFECRQQFileId));
                Map relAttrMap = new HashMap<>();
                relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE,"QQ");
                rel.setAttributeValues(context,relAttrMap);
            }
            if (UIUtil.isNotNullAndNotEmpty(strJFECRQQFileId2)){
                DomainRelationship rel = DomainRelationship.connect(context,ecr, RELATIONSHIP_REFERENCE_DOCUMENT, DomainObject.newInstance(context, strJFECRQQFileId2));
                Map relAttrMap = new HashMap<>();
                relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_PROJECT_ROLE,"Questions List");
                rel.setAttributeValues(context,relAttrMap);
            }
            if (UIUtil.isNotNullAndNotEmpty(strManagerReviewOID)) {
                DomainRelationship.connect(context,ecr, JF_PLMConstants_mxJPO.REL_ECR2PERSON, DomainObject.newInstance(context, strManagerReviewOID));
            }
//            Map relAttrMap = new HashMap();
//            if (UIUtil.isNotNullAndNotEmpty(strWCharOIDs)){
//                StringTokenizer strtk = new StringTokenizer(strWCharOIDs, ",");
//                StringList mCharPersonList = new StringList();
//                while (strtk.hasMoreTokens()) {
//                    mCharPersonList.add(strtk.nextToken());
//                }
//                //判断整椅开发工程师中是否包含了经理
//                if (UIUtil.isNotNullAndNotEmpty(strManagerReviewOID)) {
//                    if (mCharPersonList.contains(strManagerReviewOID)){
//                        //防止重复连接
//                        mCharPersonList.remove(strManagerReviewOID);
//                        DomainRelationship rel = DomainRelationship.connect(context,ecr , JF_PLMConstants_mxJPO.REL_ECR2PERSON, DomainObject.newInstance(context, strManagerReviewOID));
//                        relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE,JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_BOTH);
//                        relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTEOBJECTSTATUS,"Create");
//                        rel.setAttributeValues(context,relAttrMap);
//
//                    }else {
//                        if (UIUtil.isNotNullAndNotEmpty(strManagerReviewOID)) {
//                            DomainRelationship rel = DomainRelationship.connect(context,ecr, JF_PLMConstants_mxJPO.REL_ECR2PERSON, DomainObject.newInstance(context, strManagerReviewOID));
//                            relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE,JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_MANGER);
//                            relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTEOBJECTSTATUS,"Create");
//                            rel.setAttributeValues(context,relAttrMap);
//                        }
//                    }
//                }
//                //清空
//                relAttrMap.clear();
//                relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE,JF_PLMConstants_mxJPO.ATTR_JFROUTENOTE_APPROVALAROLE_FLAG_DEV_ENGINEER);
//                relAttrMap.put(JF_PLMConstants_mxJPO.ATTR_JFROUTEOBJECTSTATUS,"Create");
//                for (int i = 0; i < mCharPersonList.size(); i++) {
//                    String strWcharPersonId = mCharPersonList.get(i);
//                    DomainRelationship rel = DomainRelationship.connect(context, ecr, JF_PLMConstants_mxJPO.REL_ECR2PERSON, DomainObject.newInstance(context, strWcharPersonId));
//                    rel.setAttributeValues(context,relAttrMap);
//
//                }
//            }
        }catch (Exception e){
            _logger.info(e.getMessage());
        }finally {
            ContextUtil.popContext(context);
        }
    }

    /**
    *
    *@description 创建快速报价时需要连接制费责任者
    *@param context
	*@param args
    *@return void
    *@throws
    *@author CHENYAN
    *@date 2025/3/25 16:00
    */
    @PostProcessCallable
    public void postProcessInCreateRapidOffer(Context context,String[] args) throws Exception{
        try {
            ContextUtil.pushContext(context);
            Map parameter = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) parameter.get("requestMap");
            Map paramMap = (Map) parameter.get("paramMap");
            String strNewObjectId = (String)paramMap.get("newObjectId");
            //制造费责任人
            String strManufacturingPerson = (String) requestMap.get("ManufacturingPerson");
            _logger.info("strManufacturingPerson:{}", strManufacturingPerson);
            DomainObject personBO = PersonUtil.getPersonObject(context, strManufacturingPerson);
            DomainObject newBO = DomainObject.newInstance(context, strNewObjectId);
            //连接制造费责任人
            DomainRelationship rel = DomainRelationship.connect(context, newBO, "JFRapidOffer2ManufacturingPerson", personBO);
        }catch (Exception e){
            _logger.error(e.getMessage());
            throw e;
        }
    }

}
