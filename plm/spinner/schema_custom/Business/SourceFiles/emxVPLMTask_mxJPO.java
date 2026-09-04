//
// $Id: ${CLASSNAME}.java.rca 1.1.1.4.2.2 Thu Dec  4 07:56:08 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.1.1.4.2.1 Thu Dec  4 01:54:59 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.1.1.4 Wed Oct 22 15:50:25 2008 przemek Experimental przemek $
//
// emxVPLMTask.java
//
// Copyright (c) 2007-2020 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.program.ProgramCentralUtil;
import matrix.db.*;
import matrix.util.StringList;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * The <code>emxVPLMTask</code> class represents the VPLM Task JPO
 * functionality for the AEF type VPLM Task.
 *
 * @version AEF 10.7.SP1 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class emxVPLMTask_mxJPO extends emxVPLMTaskBase_mxJPO
{
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(emxVPLMTask_mxJPO.class);
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10-7-SP1
     */

    public emxVPLMTask_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }

    /**
     * 获取交付物owner的部门
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     * @throws Exception if operation fails
     * @since AEF 10.7.1.0
     */
    public Object getVPLMTaskDeliverableOwnersORG(Context context, String[] args)
            throws Exception
    {
        try
        {
//       get values from args.

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            //Added:11-June-09:yox:R207:PRG:Bug :372619
            HashMap paramList = (HashMap) programMap.get("paramList");
            String strExportFormat = (String)paramList.get("exportFormat");
            //End:R207:PRG:Bug :372619
            MapList objectList = (MapList)programMap.get("objectList");
            Map tempMap = null;

            String taskId = "";

            Vector taskList = new Vector();
            String prefixDeliverableUrl = "<b><img src='../common/images/utilSpace.gif' width='1' height='25' />";
            String suffixDeliverableUrl = "</b>";

            StringBuffer tempURL = null;  //Used to form url of all objects if multiple tasks are connected.
            String[] taskIds = new String[objectList.size()];
            for (int i=0;i<objectList.size();i++ )
            {
                tempMap = (Map)objectList.get(i);
                taskId = (String)tempMap.get("id");
                if (taskId != null && !"".equals(taskId) )
                {
                    taskIds[i] = taskId;
                }
            }
            StringList busSel = new StringList();
            busSel.add("from["+ DomainObject.RELATIONSHIP_TASK_DELIVERABLE+"].to.owner");

            BusinessObjectWithSelectList tasksObjectWithSelectList = null;
            tasksObjectWithSelectList =  ProgramCentralUtil.getObjectWithSelectList(context, taskIds, busSel);

            for (BusinessObjectWithSelect bws : tasksObjectWithSelectList) {
                tempURL = new StringBuffer();
                StringList deliverablesOwnerList = bws.getSelectDataList("from["+DomainObject.RELATIONSHIP_TASK_DELIVERABLE+"].to.owner");
                int deliverablesAdded = 0;

                if (deliverablesOwnerList != null)
                {
                    int deliverableOwnerListSize = deliverablesOwnerList.size();
                    DomainObject person = DomainObject.newInstance(context);
                    for (int x = 0; x < deliverableOwnerListSize; x++)
                    {
                        String deliverablesOwner =(String)deliverablesOwnerList.get(x);
                        String personId = PersonUtil.getPersonObjectID(context, deliverablesOwner);
                        person.setId(personId);
                       logger.info("personID:{}",personId);
                        deliverablesOwner =person.getInfo(context, "to[Member|from.type==Department].from.attribute[Title]");
                        ContextUtil.pushContext(context);
                        deliverablesOwner =  MqlUtil.mqlCommand(context, true, "print bus "+personId+" select to[Member|from.type==Department].from.attribute[Title] dump @ ", false);
                        logger.info("deliverablesOwner:{}",deliverablesOwner);
                        ContextUtil.popContext(context);
                        if("CSV".equals(strExportFormat)){
                            if(x==0){
                                tempURL.append("\"");
                            }
                            tempURL.append(deliverablesOwner);
                            if(x != (deliverableOwnerListSize-1)){
                                tempURL.append(",");
                            }else{
                                tempURL.append("\"");
                            }
                        }else {
                            tempURL.append(prefixDeliverableUrl);
                            tempURL.append(deliverablesOwner);
                            tempURL.append(suffixDeliverableUrl);
                            tempURL.append("<br></br>"); //To show each owner in different line.
                        }
                        deliverablesAdded ++;
                        //End:R207:PRG:Bug :372619
                    }
                }

                if (deliverablesAdded == 0) tempURL.append(" ");
                taskList.add(tempURL.toString());


            }


            return taskList;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }
}
