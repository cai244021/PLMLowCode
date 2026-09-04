import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import matrix.db.Context;
import matrix.db.JPO;

import java.lang.reflect.Method;
import java.util.Map;

public class emxECMDocument_mxJPO extends emxECMDocumentBase_mxJPO{


    /** copy base  文档的owner才有升版权限
     * Method to check if Revise commands can be shown wrt. ECM
     * @param context
     * @param args[] will contain objectID of Document
     * @return false - if Document is enabled with Change Control or if connected to any CA, which is not completed
     * @return true - if Document is enabled with Change Control or if connected to any completed CA or if Document is not connected to any CA
     * @throws Exception
     */
    public boolean showReviseCommands(Context context, String[] args) throws Exception {
        boolean showReviseCommands = true;
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            String strDocId = (String) programMap.get("objectId");
            //只有owner才有升版权限
            DomainObject docObj = DomainObject.newInstance(context,strDocId);
            String currentUser = context.getUser();
            String user = docObj.getInfo(context, DomainConstants.SELECT_OWNER);
            if(!currentUser.equalsIgnoreCase(user)){
                return false;
            }

            Class<?> lClass = Class.forName("com.dassault_systemes.enovia.document.ECMDocumentUtil");
            Object inst = lClass.newInstance();
            Object[] lMethodArgumentArray = new Object[2];
            lMethodArgumentArray[0] = context;
            lMethodArgumentArray[1] = strDocId;
            Method method = lClass.getMethod("showReviseCommands", matrix.db.Context.class, String.class);
            showReviseCommands = (boolean) method.invoke(inst, lMethodArgumentArray);
        } catch (Exception e) {
            showReviseCommands=true;
        }
        return showReviseCommands;
    }

}
