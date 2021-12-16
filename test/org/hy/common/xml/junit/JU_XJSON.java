package org.hy.common.xml.junit;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.Queue;
import org.hy.common.Queue.QueueType;
import org.hy.common.net.data.Command;
import org.hy.common.xml.XJSON;
import org.hy.common.xml.XJSONToJava;
import org.hy.common.xml.log.Logger;
import org.junit.Test;





/**
 * 测试单元
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-07-10
 * @version     v1.0
 */
public class JU_XJSON
{
    
    private static final Logger $Logger = new Logger(JU_XJSON.class ,true);
    
    private String     valueString;
    
    private Date       valueDate;
    
    private Object     valueObject;
    
    private byte    [] valueByteArr;
    
    private int     [] valueIntArr;
    
    private Integer [] valueIntegerArr;
    
    private String  [] valueStringArr;
    
    
    
    /**
     * 测试Getter方法的Object返回结果
     * 
     * 如下报文是Java生成的报文
        {
            "valueDate": "2021-10-08 11:06:16",
            "valueObject@java.util.HashMap": {
                "b@org.hy.common.xml.junit.JU_XJSON": {
                    "valueDate": "2021-10-07 08:30:00",
                    "valueObject@java.lang.String": "DataB：常规对象类型",
                    "valueString": "DataB：子对象中的字符串"
                },
                "c@org.hy.common.xml.junit.JU_XJSON": {
                    "valueDate": "2021-10-08 09:00:00",
                    "valueObject@java.util.ArrayList@org.hy.common.xml.junit.JU_XJSON": [
                        {
                            "valueDate": "2021-10-08 10:00:00",
                            "valueObject@java.lang.String": "DataD：常规对象类型",
                            "valueString": "DataD：子对象中的字符串"
                        }
                    ],
                    "valueString": "DataC：子对象中的字符串"
                }
            },
            "valueString": "字符串"
        }
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-09-30
     * @version     v1.0
     * 
     * @throws Exception
     */
    @Test
    public void test_Json() throws Exception
    {
        List<Object>        v_DataList = new ArrayList<Object>();
        Map<String ,Object> v_DataMap  = new HashMap<String ,Object>();
        JU_XJSON            v_DataA    = new JU_XJSON();
        JU_XJSON            v_DataB    = new JU_XJSON();
        JU_XJSON            v_DataC    = new JU_XJSON();
        JU_XJSON            v_DataD    = new JU_XJSON();
        JU_XJSON            v_DataNew  = null;
        
        v_DataD.setValueString("DataD：子对象中的字符串");
        v_DataD.setValueDate(new Date("2021-10-08 10:00:00"));
        v_DataD.setValueObject("DataD：常规对象类型");
        v_DataD.setValueByteArr(v_DataD.getValueObject().toString().getBytes());
        v_DataD.setValueIntArr(new int[]{1 ,2});
        v_DataD.setValueIntegerArr(new Integer[]{1 ,2});
        v_DataD.setValueStringArr(new String[] {"D" ,"Arr"});
        
        v_DataList.add(v_DataD);
        
        v_DataC.setValueString("DataC：子对象中的字符串");
        v_DataC.setValueDate(new Date("2021-10-08 09:00:00"));
        v_DataC.setValueObject(v_DataList);                    // 对象套娃
        v_DataC.setValueByteArr(v_DataC.getValueObject().toString().getBytes());
        v_DataC.setValueIntArr(new int[]{3 ,4});
        v_DataC.setValueIntegerArr(new Integer[]{3 ,4});
        v_DataC.setValueStringArr(new String[] {"C" ,"Arr"});
        
        v_DataB.setValueString("DataB：子对象中的字符串");
        v_DataB.setValueDate(new Date("2021-10-07 08:30:00"));
        v_DataB.setValueObject("DataB：常规对象类型");
        v_DataB.setValueByteArr(v_DataB.getValueObject().toString().getBytes());
        v_DataB.setValueIntArr(new int[]{5 ,6});
        v_DataB.setValueIntegerArr(new Integer[]{5 ,6});
        v_DataB.setValueStringArr(new String[] {"B" ,"Arr"});
        
        v_DataMap.put("b" ,v_DataB);
        v_DataMap.put("c" ,v_DataC);
        
        v_DataA.setValueString("字符串");
        v_DataA.setValueDate(new Date());
        v_DataA.setValueObject(v_DataMap);                     // 对象套娃
        v_DataA.setValueByteArr(v_DataA.getValueObject().toString().getBytes());
        v_DataA.setValueIntArr(new int[]{7 ,8});
        v_DataA.setValueIntegerArr(new Integer[]{7 ,8});
        v_DataA.setValueStringArr(new String[] {"A" ,"Arr"});
        
        XJSON    v_Json       = new XJSON();
        String   v_JsonString = "";
        
        $Logger.info("按普通Json转换");
        v_JsonString = v_Json.toJson(v_DataA ,"data").toJSONString();
        v_DataNew    = (JU_XJSON) v_Json.toJava(v_JsonString ,"data" ,JU_XJSON.class);
        
        v_JsonString = v_Json.toJson(v_DataA).toJSONString();
        v_DataNew    = (JU_XJSON) v_Json.toJava(v_JsonString ,JU_XJSON.class);

        $Logger.info(v_JsonString);
        $Logger.info(v_DataNew.getValueString());
        $Logger.info(v_DataNew.getValueDate());
        $Logger.info(v_DataNew.getValueObject() + " -> " + v_DataNew.getValueObject().getClass().getName());
        
        
        
        $Logger.info("\n\n支持Object对象的Json转换");
        v_Json.setSerializable(true);
        
        v_JsonString = v_Json.toJson(v_DataA).toJSONString();
        v_DataNew    = (JU_XJSON) v_Json.toJava(v_JsonString ,JU_XJSON.class);
        
        $Logger.info(v_JsonString);
        $Logger.info(v_DataNew.getValueString());
        $Logger.info(v_DataNew.getValueDate());
        $Logger.info(v_DataNew.getValueObject() + " -> " + v_DataNew.getValueObject().getClass().getName());
    }
    
    
    
    @Test
    public void test_JsonToJava_20211209() throws Exception
    {
        XJSON   v_XJson   = new XJSON();
        String  v_JsonStr = "";
        Command v_Command = null;
        
        v_JsonStr = "{\"XJavaCloudDatas\":{\"methodName\":\"zipFile\",\"params\":{\"0@java.lang.String\":\"$WebHome\",\"1@java.lang.String\":\"windows\",\"2@java.lang.String\":\"20211213153722613\"}}}";
        v_Command = (Command) v_XJson.toJava(v_JsonStr ,"XJavaCloudDatas" ,Command.class);
        
        v_JsonStr = "{\"XJavaCloudDatas\":{\"methodName\":\"calcFileSize\",\"params\":{\"0@java.lang.String\":\"$WebHome\",\"1@java.lang.String\":\"windows\"}}}";
        v_Command = (Command) v_XJson.toJava(v_JsonStr ,"XJavaCloudDatas" ,Command.class);

        v_JsonStr = "{\"XJavaCloudDatas\":{\"methodName\":\"cloneFileUpload\",\"params\":{\"0@java.lang.String\":\"$WebHome\",\"1@org.hy.common.file.FileDataPacket\":{\"dataCount\":\"1\",\"dataNo\":\"1\",\"name\":\"video.html\",\"size\":\"1376\"}}}}";
        v_Command = (Command) v_XJson.toJava(v_JsonStr ,"XJavaCloudDatas" ,Command.class);
        
        $Logger.info(v_Command);
    }
    
    
    
    @Test
    public void test_executeToJava() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException
    {
        Object   v_Value      = new Long(1);
        Object   v_JavaValue  = null;
        Class<?> v_ParamClass = Long.class;
        
        v_JavaValue = XJSONToJava.executeToJava(new XJSON() ,v_Value.toString() ,v_ParamClass ,null);
        
        $Logger.info(v_JavaValue);
    }
    
    
    
    /**
     * null转Java的测试
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-10
     * @version     v1.0
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_001() throws Exception
    {
        String json = "{\"maching\":[{\"PART_PLAN_NO\":null,\"GY_TYPE\":\"砂铸\",\"FINISH_DATE\":\"2018\\/06\\/25\",\"FINISH_QTY\":\"1\",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"FTZJ\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-24\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"上阀盖\",\"RECEIVE_MATERIAL_QTY\":\"1\",\"ITEM_CODE\":\"1AM30441-210-003C\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-3-0014\",\"GY_TYPE\":\"砂铸\",\"FINISH_DATE\":\"2018\\/06\\/21\",\"FINISH_QTY\":\"1\",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"FTZJ\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-29\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"阀体\",\"RECEIVE_MATERIAL_QTY\":\"1\",\"ITEM_CODE\":\"AM30781-205-103C\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-1-0567\",\"GY_TYPE\":\"部件\",\"FINISH_DATE\":\" \",\"FINISH_QTY\":\" \",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"ZXJG\",\"RECEIVE_MATERIAL_DATE\":\" \",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"上膜盖部件\",\"RECEIVE_MATERIAL_QTY\":\" \",\"ITEM_CODE\":\"8Z51-5100-1\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-1-0566\",\"GY_TYPE\":\"下料件\",\"FINISH_DATE\":\" \",\"FINISH_QTY\":\" \",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"ZXJG\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-21\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"推杆\",\"RECEIVE_MATERIAL_QTY\":\"10\",\"ITEM_CODE\":\"8Z52-106-405\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-3-0004\",\"GY_TYPE\":\"精铸\",\"FINISH_DATE\":\"2018\\/06\\/01\",\"FINISH_QTY\":\"5\",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"FTZJ\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-23\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"套筒\",\"RECEIVE_MATERIAL_QTY\":\"5\",\"ITEM_CODE\":\"ABM30421-207-004\",\"CL\":\" \"}],\"casting\":[{\"ZJ_KF_RKRQ2\":\"2018-04-20 17:14:53\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-3-0004\",\"ZJ_WGS\":\"5\",\"ZJ_TCS\":\"5\",\"ZJ_SX\":\"5\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"套筒\",\"ITEM_CODE\":\"ABM30421-207-004\",\"ZJ_KF_RKS\":\"5\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-3-0013\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"上阀盖\",\"ITEM_CODE\":\"1AM30441-210-003C\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0568\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮箱\",\"ITEM_CODE\":\"8Z51-8102-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2018-05-29 13:46:08\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-3-0014\",\"ZJ_WGS\":\"1\",\"ZJ_TCS\":\"1\",\"ZJ_SX\":\"1\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"阀体\",\"ITEM_CODE\":\"AM30781-205-103C\",\"ZJ_KF_RKS\":\"1\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"14\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"6\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0569\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮法兰\",\"ITEM_CODE\":\"8Z52-8101-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":null,\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0569\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮法兰\",\"ITEM_CODE\":\"8Z52-8101-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0568\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮箱\",\"ITEM_CODE\":\"8Z51-8102-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":null,\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"}]}";
        
        XJSON v_XJson = new XJSON();
        
        // 当需要Map.value为NULL时，请在转换前设置此句this.setObjectClass(HashMap.class);
        // 否则，NULL的Json字符串将转成""空字符串写入Hashtable中。
        v_XJson.setObjectClass(HashMap.class);
        
        Map<String, Object> v_Datas = (Map<String, Object>)v_XJson.toJava(json);
        
        Help.print(v_Datas);
    }
    
    
    
    @Test
    public void test_IsJson()
    {
        String v_Json = "{\"maching\":[{\"PART_PLAN_NO\":null,\"GY_TYPE\":\"砂铸\",\"FINISH_DATE\":\"2018\\/06\\/25\",\"FINISH_QTY\":\"1\",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"FTZJ\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-24\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"上阀盖\",\"RECEIVE_MATERIAL_QTY\":\"1\",\"ITEM_CODE\":\"1AM30441-210-003C\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-3-0014\",\"GY_TYPE\":\"砂铸\",\"FINISH_DATE\":\"2018\\/06\\/21\",\"FINISH_QTY\":\"1\",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"FTZJ\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-29\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"阀体\",\"RECEIVE_MATERIAL_QTY\":\"1\",\"ITEM_CODE\":\"AM30781-205-103C\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-1-0567\",\"GY_TYPE\":\"部件\",\"FINISH_DATE\":\" \",\"FINISH_QTY\":\" \",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"ZXJG\",\"RECEIVE_MATERIAL_DATE\":\" \",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"上膜盖部件\",\"RECEIVE_MATERIAL_QTY\":\" \",\"ITEM_CODE\":\"8Z51-5100-1\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-1-0566\",\"GY_TYPE\":\"下料件\",\"FINISH_DATE\":\" \",\"FINISH_QTY\":\" \",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"ZXJG\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-21\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"推杆\",\"RECEIVE_MATERIAL_QTY\":\"10\",\"ITEM_CODE\":\"8Z52-106-405\",\"CL\":\" \"},{\"PART_PLAN_NO\":\"1806-3-0004\",\"GY_TYPE\":\"精铸\",\"FINISH_DATE\":\"2018\\/06\\/01\",\"FINISH_QTY\":\"5\",\"PLANFINISHTIME\":\"2018-06-20\",\"COMPONENT_FLAG\":\"FTZJ\",\"RECEIVE_MATERIAL_DATE\":\"2018-05-23\",\"PLANSTARTTIME\":\"2018-06-11\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"套筒\",\"RECEIVE_MATERIAL_QTY\":\"5\",\"ITEM_CODE\":\"ABM30421-207-004\",\"CL\":\" \"}],\"casting\":[{\"ZJ_KF_RKRQ2\":\"2018-04-20 17:14:53\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-3-0004\",\"ZJ_WGS\":\"5\",\"ZJ_TCS\":\"5\",\"ZJ_SX\":\"5\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"套筒\",\"ITEM_CODE\":\"ABM30421-207-004\",\"ZJ_KF_RKS\":\"5\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-3-0013\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"上阀盖\",\"ITEM_CODE\":\"1AM30441-210-003C\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0568\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮箱\",\"ITEM_CODE\":\"8Z51-8102-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2018-05-29 13:46:08\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-3-0014\",\"ZJ_WGS\":\"1\",\"ZJ_TCS\":\"1\",\"ZJ_SX\":\"1\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"阀体\",\"ITEM_CODE\":\"AM30781-205-103C\",\"ZJ_KF_RKS\":\"1\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"14\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"6\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0569\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮法兰\",\"ITEM_CODE\":\"8Z52-8101-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":null,\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0569\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮法兰\",\"ITEM_CODE\":\"8Z52-8101-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":\"铸件库存满足\",\"PART_PLAN_NO\":\"1806-1-0568\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"1\",\"ITEM_NAME\":\"蜗轮箱\",\"ITEM_CODE\":\"8Z51-8102-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"砂铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":\"2000-01-01 00:00:00\",\"PU_PROGRESS_NAME\":\"\",\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":\"\",\"ZJ_TCS\":\"\",\"ZJ_SX\":\"\",\"ORDER_QTY\":\"4\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"},{\"ZJ_KF_RKRQ2\":{\"nanos\":0},\"PU_PROGRESS_NAME\":null,\"PART_PLAN_NO\":\"1806-1-0550\",\"ZJ_WGS\":null,\"ZJ_TCS\":null,\"ZJ_SX\":null,\"ORDER_QTY\":\"2\",\"ITEM_NAME\":\"吊钩\",\"ITEM_CODE\":\"6Z51-1501-003\",\"ZJ_KF_RKS\":\"0\",\"GY_TYPE\":\"精铸\",\"ORDERNO8\":\"18394038\"}]}";
        
        $Logger.info(XJSON.isJson(v_Json));
    }
    
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void test_复杂对象ToJson() throws Exception
    {
        Queue<String> v_Data  = new Queue<String>(QueueType.$First_IN_Last_OUT);
        Queue<String> v_New   = null;
        XJSON         v_XJson = new XJSON();
        
        String v_Json = v_XJson.toJson(v_Data).toJSONString();
        v_New = (Queue<String>)v_XJson.toJava(v_Json ,v_Data.getClass());
        $Logger.info(v_Json);
        $Logger.info(v_New);
        $Logger.info("由构造器初始化的对象，并且未开放对应的Setter方法时，无法做到Json的反序列化"); // TODO
    }

    
    
    public String getValueString()
    {
        return valueString;
    }


    public void setValueString(String valueString)
    {
        this.valueString = valueString;
    }

    
    public Date getValueDate()
    {
        return valueDate;
    }

    
    public void setValueDate(Date valueDate)
    {
        this.valueDate = valueDate;
    }


    public Object getValueObject()
    {
        return valueObject;
    }

    
    public void setValueObject(Object valueObject)
    {
        this.valueObject = valueObject;
    }

    
    public byte [] getValueByteArr()
    {
        return valueByteArr;
    }

    
    public void setValueByteArr(byte [] valueByteArr)
    {
        this.valueByteArr = valueByteArr;
    }

    
    public int [] getValueIntArr()
    {
        return valueIntArr;
    }

    
    public void setValueIntArr(int [] valueIntArr)
    {
        this.valueIntArr = valueIntArr;
    }


    public Integer [] getValueIntegerArr()
    {
        return valueIntegerArr;
    }

    
    public void setValueIntegerArr(Integer [] valueIntegerArr)
    {
        this.valueIntegerArr = valueIntegerArr;
    }

    
    public String [] getValueStringArr()
    {
        return valueStringArr;
    }

    
    public void setValueStringArr(String [] valueStringArr)
    {
        this.valueStringArr = valueStringArr;
    }
    
}
