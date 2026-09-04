import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class CUS_BuilderConfigFile_mxJPO {
    //private static String person_split="``|``";
    public static Properties config_properties =new Properties();
    private final Logger _logger = Logger.getLogger("CUS_BuilderConfigFile_mxJPO");
    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public CUS_BuilderConfigFile_mxJPO (Context context, String[] args)
            throws Exception {

        this.config_properties = (Properties) JPO.invoke(context, "CUS_ConfigConstants", new String[]{""}, "getPagePropertiesValue", new String[]{""}, Properties.class);
    }

        public void build3DeSysPersonInfo(Context context, String args[]){
        try{


            StringList objectSelects = new StringList();
            objectSelects.add(DomainObject.SELECT_ID);
            objectSelects.add(DomainObject.SELECT_NAME);
            objectSelects.add(DomainObject.SELECT_ORIGINATED);
            objectSelects.add("attribute[First Name]");
            objectSelects.add("attribute[Last Name]");
            objectSelects.add("attribute[Email Address]");

            MapList personML = DomainObject.findObjects(context,
                    "Person",
                    "*",
                    ""  ,
                    objectSelects);
            personML.sort(DomainObject.SELECT_ORIGINATED,"ascending","date");
            StringBuffer personInfoSB=new StringBuffer();
            //String person_split=${CLASS:Aer_ConfigConstants}.person_split_Key;
            String person_split= config_properties.getProperty("person_split_Key");
            System.out.println("person_split >>"+person_split);

            int personSize=personML.size();
            System.out.println("系统用户数量 "+personSize);
            for (int i = 0; i <personSize ; i++) {
                Map valMap= (Map) personML.get(i);
                String name= (String) valMap.get(DomainObject.SELECT_NAME);
                String FirstName= (String) valMap.get("attribute[First Name]");
                String LastName= (String) valMap.get("attribute[Last Name]");
                String EmailAddress= (String) valMap.get("attribute[Email Address]");
               // System.out.println("DomainObject.SELECT_ORIGINATED >>>"+(String) valMap.get(DomainObject.SELECT_ORIGINATED));
                String appendStr=(i+1)+ person_split + name + person_split +(FirstName+LastName) + person_split + EmailAddress;
                personInfoSB.append(appendStr);
                if(i!=personSize-1){
                    personInfoSB.append("\n");
                }
            }
            if (personInfoSB.toString().length()==0){
                return ;
            }
            try {
               String userPath= config_properties.getProperty("userInfoPath");
               if(UIUtil.isNotNullAndNotEmpty(userPath)){
                   File userFile=new File(userPath);
                   if (!userFile.exists()){
                       userFile.mkdirs();
                   }

               }

                BufferedWriter writer = new BufferedWriter(new FileWriter((String) config_properties.getProperty("userInfoPath")));
                writer.write(personInfoSB.toString());
                writer.close();
                System.out.println("写入成功 》》》文件路径"+config_properties.getProperty("userInfoPath"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public static String attribute_Aer_UserFellName="Aer_UserFullName";
    public static String attribute_First_Name="First Name";
    public static String attribute_Last_Name="Last Name";
    public static String select_attribute_Last_Name="attribute["+attribute_Last_Name+"]";
    public static String select_attribute_First_Name="attribute["+attribute_First_Name+"]";
    public static String select_attribute_Aer_UserFellName="attribute["+attribute_Aer_UserFellName+"]";
    public static String interfaceAer_PersonExt="Aer_PersonExt";
    public void setUserFullName(Context context, String[] args){
        boolean isPush=false;
        try{
            ContextUtil.pushContext(context);
            isPush=true;
            String personId=args[0];
            DomainObject doPerson=DomainObject.newInstance(context,personId);
            String firstName=doPerson.getAttributeValue(context,attribute_First_Name);
            String lastName=doPerson.getAttributeValue(context,select_attribute_Last_Name);
            doPerson.setAttributeValue(context,attribute_Aer_UserFellName,firstName+lastName);
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            try{
                if(isPush){
                    ContextUtil.popContext(context);
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
    public void processHistorySetUseFullName(Context context, String[] args){
        boolean isPush=false;
        try{
            ContextUtil.pushContext(context);
            StringList objectSelects = new StringList();
            objectSelects.add(DomainObject.SELECT_ID);
            objectSelects.add(DomainObject.SELECT_NAME);
            objectSelects.add(DomainObject.SELECT_ORIGINATED);
            objectSelects.add(select_attribute_First_Name);
            objectSelects.add(select_attribute_Last_Name);
            objectSelects.add(select_attribute_Aer_UserFellName);
            objectSelects.add("interface");
            MapList personML = DomainObject.findObjects(context,
                    "Person",
                    "*",
                    ""  ,
                    objectSelects);
            for (int i = 0; i <personML.size() ; i++) {
                Map personMap= (Map) personML.get(i);
                String fullName= (String) personMap.get(select_attribute_Aer_UserFellName);
                String firstName=(String)personMap.get(select_attribute_First_Name);
                String lastName=(String)personMap.get(select_attribute_Last_Name);
                String Name=(String)personMap.get(DomainObject.SELECT_NAME);
                _logger.info("username"+Name+" fullName>>>>>"+fullName +"==" +firstName +""+lastName );

                if(UIUtil.isNullOrEmpty(fullName)||(!fullName.equals(firstName+lastName))){
                    String personId=(String)personMap.get(DomainObject.SELECT_ID);
                    Object interfaceStr=(Object)personMap.get("interface");
                    if(interfaceStr==null){
                        interfaceStr="";
                    }

                    if(!interfaceStr.toString().contains(interfaceAer_PersonExt)){
                        MqlUtil.mqlCommand(context, "mod bus $1 add interface $2", personId,interfaceAer_PersonExt);
                    }

                    DomainObject doPerson=DomainObject.newInstance(context,personId);
                    doPerson.setAttributeValue(context,attribute_Aer_UserFellName,firstName+lastName);
                }

            }

        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            try{
                if(isPush){
                    ContextUtil.popContext(context);
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
