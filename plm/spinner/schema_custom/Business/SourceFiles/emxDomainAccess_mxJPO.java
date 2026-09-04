/*
**  emxDomainAccess
**
**  Copyright (c) 1992-2020 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program.
**
*/

import java.util.*;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;

import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Person;
import matrix.util.StringList;
import org.slf4j.LoggerFactory;


/**

 */
public class emxDomainAccess_mxJPO extends emxDomainAccessBase_mxJPO {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(emxDomainAccess_mxJPO.class);
    private String PROJECT_ROLE = "Project Role";
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation
     * @since V6R2011x
     * @grade 0
     */
    public emxDomainAccess_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

    public void updateProjectRole(Context context,String[] args) throws Exception{
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        String objectId = UIUtil.getValue(paramMap, "objectId");
        String newValue = UIUtil.getValue(paramMap, "New Value");
        String[] list = objectId.split("::");
        String strObjId = "";
        String userStr ="";
        if(list.length==2){
             strObjId = list[0];
             userStr = FrameworkUtil.split(list[1],":").get(0);
            if (userStr.contains("_PRJ")) {
                userStr = userStr.replace("_PRJ", "");
            }
        }
      if(UIUtil.isNotNullAndNotEmpty(strObjId)&&UIUtil.isNotNullAndNotEmpty(userStr)){
          String mql = "print bus '"+strObjId+"' select from[Member|to.name=='"+userStr+"'].id dump ";
          String relId = MqlUtil.mqlCommand(context,false , mql, true);
          if(UIUtil.isNotNullAndNotEmpty(relId)){
              DomainRelationship ship = new DomainRelationship(relId);
              ship.setAttributeValue(context, PROJECT_ROLE, newValue);
          }

      }
    }

    /*
     * @description:  获取项目角色
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public Vector getProjectRole(Context context, String[] args)
            throws Exception {
        logger.info("getProjectRole+++++++++start");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector retVector = new Vector();
        String role ="";
        if (objectList != null && objectList.size() > 0) {
            Map objectMap = null;
            DomainObject domainObject = DomainObject.newInstance(context);
            for (int i = 0; i < objectList.size(); i++) {
                String displayName = "";
                objectMap = (Map) objectList.get(i);
                String objectId = (String) objectMap.get("id");
                String[] list = objectId.split("::");
                String strObjId = "";
                String userStr ="";
                if(list.length==2){
                    strObjId = list[0];
                    userStr = FrameworkUtil.split(list[1],":").get(0);
                    if (userStr.contains("_PRJ")) {
                        userStr = userStr.replace("_PRJ", "");
                    }
                }
                if(UIUtil.isNotNullAndNotEmpty(strObjId)&&UIUtil.isNotNullAndNotEmpty(userStr)){
                    String mql = "print bus '"+strObjId+"' select from[Member|to.name=='"+userStr+"'].id dump ";
                    String relId = MqlUtil.mqlCommand(context,false , mql, true);
                    if(UIUtil.isNotNullAndNotEmpty(relId)){
                        DomainRelationship ship = new DomainRelationship(relId);
                         role = ship.getAttributeValue(context, PROJECT_ROLE);
                        displayName =   i18nNow.getRangeI18NString(PROJECT_ROLE, role, context.getLocale().getLanguage());
                    }

                }
                retVector.add(displayName);
            }
        }
        return retVector;
    }
    /*
     * @description:获取人组织
     * @author: caipan
     * @date: 2025/1/9 10:42:52
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public Vector getProjectUserDep(Context context, String[] args)
            throws Exception {
        logger.info("getProjectUserDep+++++++++start");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
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
                String objectId = (String) objectMap.get("id");
                String[] list = objectId.split("::");
                String strObjId = "";
                String userStr = "";
                if (list.length == 2) {
                    strObjId = list[0];
                    userStr = FrameworkUtil.split(list[1], ":").get(0);
                    if (userStr.contains("_PRJ")) {
                        userStr = userStr.replace("_PRJ", "");
                    }
                }
                if (UIUtil.isNotNullAndNotEmpty(strObjId) && UIUtil.isNotNullAndNotEmpty(userStr)) {
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
}
