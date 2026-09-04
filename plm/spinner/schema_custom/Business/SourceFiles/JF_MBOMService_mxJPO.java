import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.ProgramCallable;
import matrix.db.Context;
import matrix.db.JPO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName JF_MBOMService_mxJPO
 * @Author: LIUJR
 * @CreateDate: 2025/3/31 16:36
 * @UpdateRemark:
 * @Version: 1.0
 * @Description:
 */
public class JF_MBOMService_mxJPO {
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_PublicMethodClass_mxJPO.class);

    @ProgramCallable
    public MapList getMBOMItemStructure(Context context, String[] args) throws Exception{
        MapList mlPartInfoList = new MapList();
        try {
            Map paramsMap = (Map)JPO.unpackArgs(args);
            String strObjectId = (String) paramsMap.get(JF_PLMConstants_mxJPO.STRING_OBJECTID);
            DomainObject psObject = DomainObject.newInstance(context, strObjectId);
            String strMBOMId = psObject.getInfo(context, "from[JFProject2MBOMHead].to.id");
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(JF_PLMConstants_mxJPO.STRING_OBJECTID, "14585.59252.17636.19683");
            hashMap.put("PPRObjectType", "Process");
            hashMap.put("expandLevel", "1");
            DELJPlanningExpandUtil_mxJPO deljPlanningExpandUtilMxJPO = new DELJPlanningExpandUtil_mxJPO(context, args);
            mlPartInfoList = deljPlanningExpandUtilMxJPO.getMfgItemStructure(context, JPO.packArgs(hashMap));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return mlPartInfoList;
    }
}
